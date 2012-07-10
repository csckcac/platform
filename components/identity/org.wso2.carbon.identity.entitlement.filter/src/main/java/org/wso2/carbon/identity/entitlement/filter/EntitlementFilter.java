/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.filter;

import net.sf.jsr107cache.Cache;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.filter.client.AbstractEntitlementServiceClient;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementFilterException;
import org.wso2.carbon.identity.entitlement.filter.util.EntitlementFilterUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class EntitlementFilter implements Filter {

    private static final Log log = LogFactory.getLog(EntitlementFilter.class);

    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceHost;
    private String remoteServicePort;
    private String clientClass;
    private String subjectScope;
    private String subjectAttributeName;
    private String decisionCaching;
    private ConfigurationContext cfgCtx = null;
    private AbstractEntitlementServiceClient client = null;
    private Cache decisionCache = null;
    private Map<String, EntitlementDecision> simpleDecisionCache = null;
    private int maxCacheEntries = -1;
    private int cacheInvalidationInterval;
    private String thriftHost;
    private String thriftPort;
    private String authRedirectURL;

    private FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws EntitlementFilterException {

        this.filterConfig = filterConfig;

        remoteServiceUserName = filterConfig.getServletContext().getInitParameter(EntitlementConstants.USER);
        remoteServicePassword = filterConfig.getServletContext().getInitParameter(EntitlementConstants.PASSWORD);
        remoteServiceHost = filterConfig.getServletContext().getInitParameter(EntitlementConstants.HOST);
        remoteServicePort = filterConfig.getServletContext().getInitParameter(EntitlementConstants.PORT);
        clientClass = filterConfig.getInitParameter(EntitlementConstants.CLIENT_CLASS);
        subjectScope = filterConfig.getServletContext().getInitParameter(EntitlementConstants.SUBJECT_SCOPE);
        subjectAttributeName = filterConfig.getServletContext().getInitParameter(EntitlementConstants.SUBJECT_ATTRIBUTE_NAME);
        decisionCaching = filterConfig.getInitParameter(EntitlementConstants.DECISION_CACHING);
        maxCacheEntries = Integer.parseInt(filterConfig.getInitParameter(EntitlementConstants.MAX_CACHE_ENTRIES));
        cacheInvalidationInterval = Integer.parseInt(filterConfig.getInitParameter(EntitlementConstants.CACHE_INVALIDATION_INTERVAL));
        thriftHost = filterConfig.getInitParameter(EntitlementConstants.THRIFT_HOST);
        thriftPort = filterConfig.getInitParameter(EntitlementConstants.THRIFT_PORT);
        authRedirectURL = filterConfig.getInitParameter(EntitlementConstants.AUTH_REDIRECT_URL);

        // init the decision cache if is set to true
        if (decisionCaching.equals(EntitlementConstants.DEFAULT) || decisionCaching.equals(EntitlementConstants.ENABLE)) {

            simpleDecisionCache = new ConcurrentHashMap<String, EntitlementDecision>();
            if (maxCacheEntries < 0 || maxCacheEntries > EntitlementConstants.SIMPLE_CACHE_MAX_ENTRIES) {
                maxCacheEntries = EntitlementConstants.SIMPLE_CACHE_MAX_ENTRIES;
            }

        } else if (decisionCaching.equals(EntitlementConstants.WSO2_AS)) {

            decisionCache = EntitlementFilterUtils.getCommonCache(EntitlementConstants.DECISION_CACHE);

        } else if (!decisionCaching.equals(EntitlementConstants.DISABLE)) {

            throw new EntitlementFilterException(decisionCaching + " is an invalid"
                                                 + " configuration for decisionCaching parameter in web.xml. Valid configurations are"
                                                 + " \'" + EntitlementConstants.ENABLE + "\' (or \'default\'), \'" + EntitlementConstants.WSO2_AS + "\'"
                                                 + " and \'disable\'");

        }

        // load the client class that is configured
        client = (AbstractEntitlementServiceClient) loadClass(clientClass);

        // init configuration context for entitlement client
        try {
            cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        } catch (AxisFault e) {
            log.error("Error while creating configuration context from file system");
            throw new EntitlementFilterException("Error while creating configuration context from file system", e);
        }

        // init client class
        Properties properties = new Properties();
        properties.put(EntitlementConstants.USER, remoteServiceUserName);
        properties.put(EntitlementConstants.PASSWORD, remoteServicePassword);
        properties.put(EntitlementConstants.HOST, remoteServiceHost);
        properties.put(EntitlementConstants.PORT, remoteServicePort);
        properties.put(EntitlementConstants.CONTEXT, cfgCtx);
        properties.put(EntitlementConstants.THRIFT_HOST, thriftHost);
        properties.put(EntitlementConstants.THRIFT_PORT, thriftPort);

        client.init(properties);

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws EntitlementFilterException {

        String decision = EntitlementConstants.DENY;
        String userName;
        String action;
        String resource;
        String[] env = new String[0];

        userName = findUserName((HttpServletRequest) servletRequest, subjectScope, subjectAttributeName);
        resource = findResource((HttpServletRequest) servletRequest);
        action = findAction((HttpServletRequest) servletRequest);

        if(((HttpServletRequest) servletRequest).getRequestURI().contains("/updateCacheAuth.do")) {

            decision = client.getDecision(userName, resource, action, env);

        } else {

            if (simpleDecisionCache != null || decisionCache != null) {

                String key = userName + resource + action;

                EntitlementDecision entitlementDecision = null;

                if (decisionCache != null) {

                    int tenantId = CarbonContext.getCurrentContext().getTenantId();
                    IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry) decisionCache.get(cacheKey);

                    if (cacheEntry != null) {
                        log.debug("Decision Cache Hit");
                        decision = cacheEntry.getCacheEntry();
                    } else {
                        log.debug("Decision Cache Miss");
                        decision = client.getDecision(userName, resource, action, env);
                        cacheEntry = new IdentityCacheEntry(decision);
                        decisionCache.put(cacheKey, cacheEntry);
                    }

                } else if (simpleDecisionCache != null) {

                    if (maxCacheEntries < simpleDecisionCache.size()) {
                        simpleDecisionCache.clear();
                    } else {
                        entitlementDecision = simpleDecisionCache.get(key);
                    }

                    if (entitlementDecision != null && (entitlementDecision.getCachedTime() + (long) cacheInvalidationInterval > Calendar.getInstance().getTimeInMillis())) {

                        log.debug("Decision Cache Hit");
                        decision = entitlementDecision.getResponse();

                    } else {
                        simpleDecisionCache.remove(key);
                        log.debug("Decision Cache Miss");
                        decision = client.getDecision(userName, resource, action, env);

                        entitlementDecision = new EntitlementDecision();
                        entitlementDecision.setCachedTime(Calendar.getInstance().getTimeInMillis());
                        entitlementDecision.setResponse(decision);
                        simpleDecisionCache.put(key, entitlementDecision);

                    }
                }

            } else {

                decision = client.getDecision(userName, resource, action, env);

            }
        }

        completeAuthorization(decision, servletRequest, servletResponse, filterConfig, filterChain);

    }

    @Override
    public void destroy() {
        decisionCaching = null;
        simpleDecisionCache = null;
        cfgCtx = null;
        maxCacheEntries = 0;
    }

    private Object loadClass(String className) throws EntitlementFilterException {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("Error occurred while loading " + className, e);
            throw new EntitlementFilterException("Error occurred while loading " + className, e);
        }
    }

    private String findUserName(HttpServletRequest request, String subjectScope,
                                String subjectAttributeName) throws EntitlementFilterException {
        String subject;
        if (subjectScope.equals(EntitlementConstants.SESSION)) {
            subject = (String) request.getSession(false).getAttribute(subjectAttributeName);
        } else if (subjectScope.equals(EntitlementConstants.REQUEST_PARAM)) {
            subject = request.getParameter(subjectAttributeName);
        } else if (subjectScope.equals(EntitlementConstants.REQUEST_ATTIBUTE)) {
            subject = (String) request.getAttribute(subjectAttributeName);
        } else {
            log.error(subjectScope + " is an invalid"
                      + " configuration for subjectScope parameter in web.xml. Valid configurations are"
                      + " \'" + EntitlementConstants.REQUEST_PARAM + "\', " + EntitlementConstants.REQUEST_ATTIBUTE + "\' and \'"
                      + EntitlementConstants.SESSION + "\'");

            throw new EntitlementFilterException(subjectScope + " is an invalid"
                                                 + " configuration for subjectScope parameter in web.xml. Valid configurations are"
                                                 + " \'" + EntitlementConstants.REQUEST_PARAM + "\', " + EntitlementConstants.REQUEST_ATTIBUTE + "\' and \'"
                                                 + EntitlementConstants.SESSION + "\'");
        }
        if (subject == null || subject.equals("null")) {
            log.error("Username not provided in " + subjectScope);
            throw new EntitlementFilterException("Username not provided in " + subjectScope);
        };
        return subject;
    }

    private String findResource(HttpServletRequest request) {
        String resource = request.getRequestURI();
        return resource;
    }

    private String findAction(HttpServletRequest request) {
        String action = request.getMethod();
        return action;
    }

    private void completeAuthorization(String decision, ServletRequest servletRequest,
                                       ServletResponse servletResponse, FilterConfig filterConfig,
                                       FilterChain filterChain)
            throws EntitlementFilterException {
        try {
            if (decision.equals(EntitlementConstants.PERMIT)) {
                if (((HttpServletRequest) servletRequest).getRequestURI().contains("/updateCacheAuth.do")) {
                    try {
                        init(filterConfig);
                    } catch (EntitlementFilterException e) {
                        throw new EntitlementFilterException("Error while updating PEP cache", e);
                    }
                    log.info("PEP cache has been updated");
                    servletResponse.getWriter().print("PEP cache has been updated");
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            } else if (decision.equals(EntitlementConstants.DENY)) {
                log.info("User not authorized to perform the action");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            } else if (decision.equals(EntitlementConstants.NOT_APPLICABLE)) {
                log.info("No applicable policies found");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            } else {
                log.error("Unrecognized decision returned from PDP");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("Error occurred while completing authorization", e);
            throw new EntitlementFilterException("Error occurred while completing authorization", e);
        }
    }

}
