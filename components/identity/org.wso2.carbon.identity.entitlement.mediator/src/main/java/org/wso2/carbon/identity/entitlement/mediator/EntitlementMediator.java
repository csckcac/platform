/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.mediator;

import java.lang.Exception;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jsr107cache.Cache;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.mediator.callback.EntitlementCallbackHandler;
import org.wso2.carbon.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler;
import org.wso2.carbon.identity.entitlement.mediator.client.DefaultAuthenticatorServiceClient;
import org.wso2.carbon.identity.entitlement.mediator.client.EntitlementServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;

public class EntitlementMediator extends AbstractMediator implements ManagedLifecycle{

    private static final Log log = LogFactory.getLog(EntitlementMediator.class);

    private boolean remote = true;
    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private String callbackClass;
    private String clientClass;
	private Cache decisionCache = null;
    private String basicAuth;
    private String thriftPort;
    private String thriftHost;
    private String reuseSession;
	private Map<String, EntitlementDecision> simpleDecisionCache = null;
    private int tenantId;
    private String decisionCaching;
    private int decisionCachingInterval = 0;
    private int maxCacheEntries = 0;
    ConfigurationContext cfgCtx = null;
    private String clientRepository = null;
    private String axis2xml = null;
    EntitlementServiceClient client = null;
    EntitlementCallbackHandler callback = null;

    public final static String DECISION_CACHE = "DECISION_CACHE";
    public final static String DEFAULT_CLIENT_REPO = "./samples/axis2Client/client_repo";
    public final static String DEFAULT_AXIS2_XML = "./samples/axis2Client/client_repo/conf/axis2.xml";


    /**
     * {@inheritDoc}
     */
    public boolean mediate(MessageContext synCtx) {

        String decision = null;
        String userName = null;
        String serviceName = null;
        String operationName = null;
        String action = null;
        String resourceName = null;
        String[] env = null;


        if (log.isDebugEnabled()) {
            log.debug("Mediation for Entitlement started");
        }

        try {
            userName = callback.getUserName(synCtx);
            serviceName = callback.findServiceName(synCtx);
            operationName = callback.findOperationName(synCtx);
            action = callback.findAction(synCtx);
            env = callback.findEnvironment(synCtx);

            if (userName == null) {
                log.error("User name not provided for the Entitlement mediator - can't proceed");
                throw new SynapseException(
                        "User name not provided for the Entitlement mediator - can't proceed");
            }

            if (operationName != null) {
                resourceName = serviceName + "/" + operationName;
            } else {
                resourceName = serviceName;
            }

            if (env == null) {
                env = new String[0];
            }

            if(decisionCache != null || simpleDecisionCache != null){

                // if decision cache is enabled
                String key = userName + (resourceName != null ? resourceName : "") +
                                (action != null  ? action : "");

                if(env.length > 0 ){
                    StringBuffer buffer = new StringBuffer();
                    for(String s : env){
                        buffer.append(s);
                    }
                    key += buffer.toString();
                }

                if(decisionCachingInterval == 0){
                    //If caching is not used with invalidation interval for each entry
                    IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry)decisionCache.get(cacheKey);

                    if(cacheEntry != null){
                        decision = cacheEntry.getCacheEntry();
                        if (log.isDebugEnabled()) {
                            log.debug("Decision Cache Hit");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Decision Cache Miss");
                        }
                        decision = client.getDecision(userName, resourceName, action, env);
                        cacheEntry = new IdentityCacheEntry(decision);
                        decisionCache.put(cacheKey, cacheEntry);
                    }

                } else {
                    //If caching is used with invalidation interval for each entry
                    EntitlementDecision entitlementDecision = null;

                    if(maxCacheEntries < simpleDecisionCache.size()){
                        simpleDecisionCache.clear();
                    } else {
                        entitlementDecision = simpleDecisionCache.get(key);
                    }

                    if (entitlementDecision != null
                        && (entitlementDecision.getCachedTime() + (long) decisionCachingInterval > Calendar
                                .getInstance().getTimeInMillis())) {

                        if (log.isDebugEnabled()) {
                            log.debug("Decision Cache Hit");
                        }
                        decision = entitlementDecision.getResponse();

                    } else {
                        simpleDecisionCache.remove(key);
                        if (log.isDebugEnabled()) {
                            log.debug("Decision Cache Miss");
                        }
                        decision = client.getDecision(userName, resourceName, action, env);

                        entitlementDecision = new EntitlementDecision();
                        entitlementDecision.setCachedTime(Calendar.getInstance().getTimeInMillis());
                        entitlementDecision.setResponse(decision);
                        simpleDecisionCache.put(key, entitlementDecision);

                    }
                }
            } else {
                // if decision cache is disabled
                decision = client.getDecision(userName, resourceName, action, env);
            }

            if ("Permit".equals(decision)) {
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("User not authorized to perform the action :" + decision);
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while evaluating the policy", e);
            throw new SynapseException("Error occurred while evaluating the policy");
        }

        throw new SynapseException("User not authorized to perform the action");
    }

    /**
     * 
     * @param url
     * @param config
     * @return
     */
    private static String getServerURL(String url, ConfigurationContext config) {
        if (url.indexOf("${carbon.https.port}") != -1) {
            String httpsPort = CarbonUtils.getTransportPort(config, "https") + "";
            url = url.replace("${carbon.https.port}", httpsPort);
        }

        if (url.indexOf("${carbon.management.port}") != -1) {
            String httpsPort = CarbonUtils.getTransportPort(config, "https") + "";
            url = url.replace("${carbon.management.port}", httpsPort);
        }

        if (url.indexOf("${carbon.context}") != -1) {
            // We need not to worry about context here - just need the server url for logging
            url = url.replace("${carbon.context}", "");
        }
        return url;
    }

    private Object loadClass(String className) throws AxisFault {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return  clazz.newInstance();
        } catch (Exception e) {
            log.error("Error occurred while loading " + className, e);
        }
        return null;
    }

    public void init(SynapseEnvironment synEnv) {

        // init the decision cache if is set to true
        if ("enable".equals(decisionCaching)) {
            if(decisionCachingInterval == 0){
                decisionCache = EntitlementMediatorUtils.getCommonCache(DECISION_CACHE);
            } else {
                simpleDecisionCache =  new ConcurrentHashMap<String, EntitlementDecision>();
                if(maxCacheEntries == 0){
                    maxCacheEntries = 10000;
                }
            }
        }

        // init tenant Id
        tenantId = CarbonContext.getCurrentContext().getTenantId();


        try {
            
            // init configuration context for entitlement client
            
            cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    clientRepository != null ? clientRepository : DEFAULT_CLIENT_REPO,
                    axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);

            // init call back handler class

            if (callbackClass != null && callbackClass.trim().length() > 0) {
                Object loadedClass = loadClass(callbackClass);
                if(loadedClass instanceof EntitlementCallbackHandler){
                    callback = (EntitlementCallbackHandler) loadedClass;
                }
            } else {
                callback = new UTEntitlementCallbackHandler();
            }

            // init client class
            
            if (clientClass != null && clientClass.trim().length() > 0) {
                Object loadedClass = loadClass(clientClass);
                if(loadedClass instanceof EntitlementServiceClient){
                    client = (EntitlementServiceClient) loadedClass;
                }
            } else {
                client = new DefaultAuthenticatorServiceClient();
            }

            Properties properties  = new Properties();
            if(remoteServiceUserName != null){
                properties.put(EntitlementConstants.USER, remoteServiceUserName);
            }
            if(remoteServicePassword!= null){
                properties.put(EntitlementConstants.PASSWORD, remoteServicePassword);
            }
            if( remoteServiceUrl != null){
                properties.put(EntitlementConstants.SERVICE_EPR, remoteServiceUrl);
            }
            properties.put(EntitlementConstants.CONTEXT, cfgCtx);
            if(thriftHost != null){
                properties.put(EntitlementConstants.THRIFT_HOST, thriftHost);
            }
            if( thriftPort != null){
                properties.put(EntitlementConstants.THRIFT_PORT, thriftPort);
            }
            if(reuseSession != null){
                properties.put(EntitlementConstants.REUSE_SESSION, reuseSession);
            }

            String serverIp = "127.0.0.1";
            try {
                serverIp = InetAddress.getByName(new URL(ServerConfiguration.getInstance().
                            getFirstProperty(CarbonConstants.SERVER_URL)).getHost()).getHostAddress();
            } catch (Exception e) {
                log.warn("Can not calculate server's ip address. Using default address " + serverIp);
                //ignore as server ip is not important and and assume 127.0.0.1 by default
            }

            if(serverIp != null){
                properties.put(EntitlementConstants.SERVER_URL, serverIp);   
            }

            client.init(properties);

        } catch (AxisFault e) {
            String msg = "Error initializing entitlement mediator : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }

    }

    @Override
    public void destroy() {

        decisionCaching = null;
        simpleDecisionCache = null;
        cfgCtx = null;
        maxCacheEntries = 0;
        
    }

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getRemoteServiceUserName() {
        return remoteServiceUserName;
    }

    public void setRemoteServiceUserName(String remoteServiceUserName) {
        this.remoteServiceUserName = remoteServiceUserName;
    }

    public String getRemoteServicePassword() {
        return remoteServicePassword;
    }

    public void setRemoteServicePassword(String remoteServicePassword) {
        this.remoteServicePassword = remoteServicePassword;
    }

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    public String getDecisionCaching() {
        return decisionCaching;
    }

    public void setDecisionCaching(String decisionCaching) {
        this.decisionCaching = decisionCaching;
    }

    public int getDecisionCachingInterval() {
        return decisionCachingInterval;
    }

    public void setDecisionCachingInterval(int decisionCachingInterval) {
        this.decisionCachingInterval = decisionCachingInterval;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    public void setMaxCacheEntries(int maxCacheEntries) {
        this.maxCacheEntries = maxCacheEntries;
    }

    public String getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(String basicAuth) {
        this.basicAuth = basicAuth;
    }

    public String getClientClass() {
        return clientClass;
    }

    public void setClientClass(String clientClass) {
        this.clientClass = clientClass;
    }

    public String getThriftPort() {
        return thriftPort;
    }

    public void setThriftPort(String thriftPort) {
        this.thriftPort = thriftPort;
    }

    public String getThriftHost() {
        return thriftHost;
    }

    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    public String getReuseSession() {
        return reuseSession;
    }

    public void setReuseSession(String reuseSession) {
        this.reuseSession = reuseSession;
    }
}
