/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.entitlement.proxy;

import org.wso2.carbon.identity.entitlement.proxy.cache.PDPCache;
import org.wso2.carbon.identity.entitlement.proxy.exception.EntitlementProxyException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PDPProxy {

    public static PDPProxy pdpProxy = new PDPProxy();

    private Map<String, AbstractPDPProxy> appToProxyMap;
    private String defaultAppId;
    private PDPConfig config;

    private boolean enableCaching=false;
    private PDPCache<Integer,Boolean> decisionCache;
    private PDPCache<Integer,String> actualDecisionCache;



    private PDPProxy() {
    }

    /**
     * @return  the singleton instance of the Entitlement PDP Proxy
     */
    public static PDPProxy getInstance() {
        return pdpProxy;
    }

    /**
     * @param pdpConfig is used to configure the singleton instance of the PDP Proxy
     * @return the configured singleton instance of the Entitlement PDP Proxy
     * @throws Exception
     */
    public static PDPProxy getInstance(PDPConfig pdpConfig) throws Exception {
        pdpProxy.init(pdpConfig);
        return pdpProxy;
    }

    /**
     * @param pdpConfig is used to initialize the singleton instance of the PDP Proxy
     * @throws Exception
     */
    public void init(PDPConfig pdpConfig) throws Exception {
        pdpProxy.validatePDPConfig(pdpConfig);
        if(pdpConfig.isEnableCaching()){
            pdpProxy.decisionCache= new PDPCache<Integer, Boolean>(pdpConfig.getMaxCacheEntries());
            pdpProxy.actualDecisionCache=new PDPCache<Integer, String>(pdpConfig.getMaxCacheEntries());
        }
        appToProxyMap = PDPFactory.getAppToPDPProxyMap(config);
    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Default AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(Attribute[] attributes) throws Exception {
        return getDecision(attributes, defaultAppId);
    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Provided AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(Attribute[] attributes, String appId) throws Exception {
        AbstractPDPProxy appProxy = null;
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        if(pdpProxy.enableCaching){
            Integer key = genarateKey(attributes);
            if(decisionCache.containsKey(key)) {
                return decisionCache.get(key);
            }
            else{
                boolean decision = appProxy.getDecision(attributes, appId);
                decisionCache.put(key,decision);
                return decision;
            }
        } else{
            return appProxy.getDecision(attributes, appId);
        }

    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Default AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(Attribute[] attributes) throws Exception {
        return getActualDecision(attributes, defaultAppId);
    }

    /**
     * This method is used to get the Entitlement decision for the set of Attributes using The Provided AppID
     *
     * @param attributes XACML 3.0 Attribute Set
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(Attribute[] attributes, String appId) throws Exception {
    	AbstractPDPProxy appProxy = null;
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        if(pdpProxy.enableCaching){
             Integer key = genarateKey(attributes);
             if (actualDecisionCache.containsKey(key)) {
                 return actualDecisionCache.get(key);
             } else {
                 String decision = appProxy.getActualDecision(attributes, appId);
                 actualDecisionCache.put(key, decision);
                 return decision;
            }
        } else{
            return appProxy.getActualDecision(attributes, appId);
        }


    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP defaultProxy
     *
     * @param subject     XACML 2.0 subject
     * @param resource    XACML 2.0 resource
     * @param action      XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(String subject, String resource, String action, String environment) throws Exception {
        return getDecision(subject, resource, action, environment, defaultAppId);
    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP defaultProxy
     *
     * @param subject     XACML 2.0 subject
     * @param resource    XACML 2.0 resource
     * @param action      XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(String subject, String resource, String action, String environment, String appId) throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        Attribute subjectAttribute = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", "urn:oasis:names:tc:xacml:1.0:subject:subject-id", ProxyConstants.DEFAULT_DATA_TYPE, subject);
        Attribute actionAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, action);
        Attribute resourceAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resource);
        Attribute environmentAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, environment);
	Attribute[] tempArr = {subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute};
    
    	return getDecision(tempArr, appId);
    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP defaultProxy
     *
     * @param subject 	    XACML 2.0 subject
     * @param resource 	    XACML 2.0 resource
     * @param action 	    XACML 2.0 action
     * @param environment   XACML 2.0 environments
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(String subject, String resource, String action, String environment) throws Exception {
        return getActualDecision(subject, resource, action, environment, defaultAppId);
    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP defaultProxy
     * @param subject      XACML 2.0 subject
     * @param resource     XACML 2.0 resource
     * @param action 	   XACML 2.0 action
     * @param environment  XACML 2.0 environments
     * @param appId       specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(String subject, String resource, String action, String environment,String appId) throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        
        	Attribute subjectAttribute = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", "urn:oasis:names:tc:xacml:1.0:subject:subject-id", ProxyConstants.DEFAULT_DATA_TYPE, subject);
        Attribute actionAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, action);
        Attribute resourceAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resource);
        Attribute environmentAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, environment);

                Attribute[] tempArr = {subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute};

                return getActualDecision(tempArr, appId);
            }
        

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, defaultAppId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId, String appId) throws Exception {
    	AbstractPDPProxy appProxy = null;
    	if (!config.getAppToPDPMap().containsKey(appId)) {
    	    throw new EntitlementProxyException("Invlaid App Id");
    	} else {
    	    appProxy = appToProxyMap.get(appId);
    	}
	return appProxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes,
                                       domainId, defaultAppId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId, String appId)
            throws Exception {
        AbstractPDPProxy appProxy = null;
        if (!config.getAppToPDPMap().containsKey(appId)) {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        return appProxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes, domainId, appId);
    }

    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action) throws Exception {
        return getActionableChidResourcesForAlias(alias, parentResource, action, defaultAppId);
    }

    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action, String appId) throws Exception {
        AbstractPDPProxy appProxy = null;
        if (!config.getAppToPDPMap().containsKey(appId)) {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        return appProxy.getActionableChidResourcesForAlias(alias, parentResource, action, appId);
    }

    public List<String> getResourcesForAlias(String alias) throws Exception {
        return getResourcesForAlias(alias, defaultAppId);
    }

    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
        AbstractPDPProxy appProxy = null;
        if (!config.getAppToPDPMap().containsKey(appId)) {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        return appProxy.getResourcesForAlias(alias, appId);
    }

    public List<String> getActionableResourcesForAlias(String alias) throws Exception {
        return getActionableResourcesForAlias(alias, defaultAppId);
    }

    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
        AbstractPDPProxy appProxy = null;
        if (!config.getAppToPDPMap().containsKey(appId)) {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        return appProxy.getActionableResourcesForAlias(alias, appId);
    }

    public List<String> getActionsForResource(String alias, String resources) throws Exception {
        return getActionsForResource(alias, resources, defaultAppId);
    }

    public List<String> getActionsForResource(String alias, String resources, String appId)
            throws Exception {
        AbstractPDPProxy appProxy = null;
        if (!config.getAppToPDPMap().containsKey(appId)) {
            throw new EntitlementProxyException("Invlaid App Id");
        } else {
            appProxy = appToProxyMap.get(appId);
        }
        return appProxy.getActionsForResource(alias, resources, appId);
    }

    private void validatePDPConfig(PDPConfig pdpConfig) {
        pdpProxy.defaultAppId = pdpConfig.getDefaultAppId();
        pdpProxy.enableCaching = pdpConfig.isEnableCaching();
        this.config = pdpConfig;
    }

    private int genarateKey(Attribute[] attributes) {
        int key = 1;
        key = 11 * key + ((attributes == null) ? 0 : Arrays.hashCode(attributes));
        key = 31 * key + ((defaultAppId == null) ? 0 : defaultAppId.hashCode());
        return key;
    }

    public void cleanCache(){
        if(pdpProxy.enableCaching){
            pdpProxy.decisionCache.clear();
            pdpProxy.actualDecisionCache.clear();
        }
    }

}
