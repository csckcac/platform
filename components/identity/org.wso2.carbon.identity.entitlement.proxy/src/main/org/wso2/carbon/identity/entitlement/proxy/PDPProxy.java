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

public class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    PDPProxy {

    public static PDPProxy pdpProxy = new PDPProxy();

    private String messageFormat;
    private String appId;
    private AbstractPDPProxy proxy;
    private PDPConfig config;

    private static final String DEFAULT_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";

    private boolean enableCaching=false;
    private PDPCache<String,Boolean> decisionByAttributeCache;
    private PDPCache<Integer,Boolean> decisionCache;
    private PDPCache<String,String> actualDecisionByAttributeCache;
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
            pdpProxy.decisionByAttributeCache=new PDPCache<String, Boolean>(pdpConfig.getMaxCacheEntries());
            pdpProxy.decisionCache= new PDPCache<Integer, Boolean>(pdpConfig.getMaxCacheEntries());
            pdpProxy.actualDecisionCache=new PDPCache<Integer, String>(pdpConfig.getMaxCacheEntries());
            pdpProxy.actualDecisionByAttributeCache= new PDPCache<String, String>(pdpConfig.getMaxCacheEntries());
        }
        proxy = PDPFactory.getPDPProxy(config);
    }

    /**
     * This method is used to get the Entitlement decision for the provided attributes using the default appID of the PDP proxy
     * @param subjectAttrs  XACML 2.0 subjects
     * @param rescAttrs   XACML 2.0 resources
     * @param actionAttrs  XACML 2.0 actions
     * @param envAttrs XACML 2.0 environments
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                               Attribute[] actionAttrs, Attribute[] envAttrs) throws Exception {
        return getDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
    }

    /**
     * This method is used to get the Entitlement decision for the provided attributes using the provided appID of the PDP proxy
     * @param subjectAttrs  XACML 2.0 subjects
     * @param rescAttrs   XACML 2.0 resources
     * @param actionAttrs  XACML 2.0 actions
     * @param envAttrs  XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                               Attribute[] actionAttrs, Attribute[] envAttrs, String appId)
            throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            int key=genarateKey(subjectAttrs,rescAttrs,actionAttrs,envAttrs,appId);

            if(decisionCache.containsKey(key)) {
                return decisionCache.get(key);
            }
            else{
                boolean decision=proxy.getDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
                decisionCache.put(key,decision);
                return decision;
            }
        } else{
            return proxy.getDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
        }

    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP proxy
     * @param subject XACML 2.0 subject
     * @param resource XACML 2.0 resource
     * @param action XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(String subject, String resource, String action, String environment) throws Exception {
        return getDecision(subject,resource,action,environment,appId);
    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP proxy
     * @param subject XACML 2.0 subject
     * @param resource XACML 2.0 resource
     * @param action XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean getDecision(String subject, String resource, String action, String environment,String appId) throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            String key = appId+subject+resource+action+environment;
            if(decisionByAttributeCache.containsKey(key)) {
                return decisionByAttributeCache.get(key);
            }
            else{
                Attribute subjectAttribute = new Attribute();
                subjectAttribute.setId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
                subjectAttribute.setType(DEFAULT_DATA_TYPE);
                subjectAttribute.setValue(subject);

                Attribute actionAttribute = new Attribute();
                actionAttribute.setId("urn:oasis:names:tc:xacml:1.0:action:action-id");
                actionAttribute.setType(DEFAULT_DATA_TYPE);
                actionAttribute.setValue(action);

                Attribute resourceAttribute = new Attribute();
                resourceAttribute.setId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
                resourceAttribute.setType(DEFAULT_DATA_TYPE);
                resourceAttribute.setValue(resource);

                Attribute environmentAttribute = new Attribute();
                environmentAttribute.setId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
                environmentAttribute.setType(DEFAULT_DATA_TYPE);
                environmentAttribute.setValue(environment);

                boolean decision=getDecision(new Attribute[]{subjectAttribute}, new Attribute[]{resourceAttribute},
                                             new Attribute[]{actionAttribute}, new Attribute[]{environmentAttribute}, appId);

                decisionByAttributeCache.put(key,decision);
                return decision;
            }
        } else{
            Attribute subjectAttribute = new Attribute();
            subjectAttribute.setId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
            subjectAttribute.setType(DEFAULT_DATA_TYPE);
            subjectAttribute.setValue(subject);

            Attribute actionAttribute = new Attribute();
            actionAttribute.setId("urn:oasis:names:tc:xacml:1.0:action:action-id");
            actionAttribute.setType(DEFAULT_DATA_TYPE);
            actionAttribute.setValue(action);

            Attribute resourceAttribute = new Attribute();
            resourceAttribute.setId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
            resourceAttribute.setType(DEFAULT_DATA_TYPE);
            resourceAttribute.setValue(resource);

            Attribute environmentAttribute = new Attribute();
            environmentAttribute.setId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
            environmentAttribute.setType(DEFAULT_DATA_TYPE);
            environmentAttribute.setValue(environment);

            return getDecision(new Attribute[]{subjectAttribute}, new Attribute[]{resourceAttribute},
                               new Attribute[]{actionAttribute}, new Attribute[]{environmentAttribute}, appId);
        }


    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP proxy
     * @param subjectAttr XACML 2.0 subject
     * @param rescAttr XACML 2.0 resource
     * @param actionAttr XACML 2.0 action
     * @param envAttrs XACML 2.0 environments
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean  getDecisionByAttributes(String subjectAttr, String rescAttr,
                                            String actionAttr, String[] envAttrs) throws Exception {
        return getDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);

    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP proxy
     * @param subjectAttr XACML 2.0 subject
     * @param rescAttr XACML 2.0 resource
     * @param actionAttr XACML 2.0 action
     * @param envAttrs XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public boolean  getDecisionByAttributes(String subjectAttr, String rescAttr,
                                            String actionAttr, String[] envAttrs, String appId)
            throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            String key = appId+subjectAttr+rescAttr+actionAttr;
            if(envAttrs!=null){
                for(String env:envAttrs){
                    key+=env;
                }
            }

            if(decisionByAttributeCache.containsKey(key)) {
                return decisionByAttributeCache.get(key);
            }
            else{
                boolean decision=proxy.getDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);
                decisionByAttributeCache.put(key,decision);
                return decision;
            }
        } else{
            return proxy.getDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);
        }

    }

    /**
     * This method is used to get the Entitlement decision for the provided attributes using the default appID of the PDP proxy
     * @param subjectAttrs  XACML 2.0 subjects
     * @param rescAttrs   XACML 2.0 resources
     * @param actionAttrs  XACML 2.0 actions
     * @param envAttrs XACML 2.0 environments
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                                    Attribute[] actionAttrs, Attribute[] envAttrs) throws Exception {
        return getActualDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
    }

    /**
     * This method is used to get the Entitlement decision for the provided attributes using the provided appID of the PDP proxy
     * @param subjectAttrs  XACML 2.0 subjects
     * @param rescAttrs   XACML 2.0 resources
     * @param actionAttrs  XACML 2.0 actions
     * @param envAttrs  XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                                    Attribute[] actionAttrs, Attribute[] envAttrs, String appId)
            throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            int key=genarateKey(subjectAttrs,rescAttrs,actionAttrs,envAttrs,appId);

            if(actualDecisionCache.containsKey(key)) {
                return actualDecisionCache.get(key);
            }
            else{
                String decision=proxy.getActualDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
                actualDecisionCache.put(key,decision);
                return decision;
            }
        } else{
            return proxy.getActualDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, appId);
        }

    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP proxy
     * @param subject XACML 2.0 subject
     * @param resource XACML 2.0 resource
     * @param action XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(String subject, String resource, String action, String environment) throws Exception {
        return getActualDecision(subject, resource, action, environment, appId);
    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP proxy
     * @param subject XACML 2.0 subject
     * @param resource XACML 2.0 resource
     * @param action XACML 2.0 action
     * @param environment XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecision(String subject, String resource, String action, String environment,String appId) throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            String key = appId+subject+resource+action+environment;
            if(actualDecisionByAttributeCache.containsKey(key)) {
                return actualDecisionByAttributeCache.get(key);
            }
            else{
                Attribute subjectAttribute = new Attribute();
                subjectAttribute.setId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
                subjectAttribute.setType(DEFAULT_DATA_TYPE);
                subjectAttribute.setValue(subject);

                Attribute actionAttribute = new Attribute();
                actionAttribute.setId("urn:oasis:names:tc:xacml:1.0:action:action-id");
                actionAttribute.setType(DEFAULT_DATA_TYPE);
                actionAttribute.setValue(action);

                Attribute resourceAttribute = new Attribute();
                resourceAttribute.setId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
                resourceAttribute.setType(DEFAULT_DATA_TYPE);
                resourceAttribute.setValue(resource);

                Attribute environmentAttribute = new Attribute();
                environmentAttribute.setId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
                environmentAttribute.setType(DEFAULT_DATA_TYPE);
                environmentAttribute.setValue(environment);

                String decision=getActualDecision(new Attribute[]{subjectAttribute}, new Attribute[]{resourceAttribute},
                                                  new Attribute[]{actionAttribute}, new Attribute[]{environmentAttribute}, appId);

                actualDecisionByAttributeCache.put(key,decision);
                return decision;
            }
        } else{
            Attribute subjectAttribute = new Attribute();
            subjectAttribute.setId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
            subjectAttribute.setType(DEFAULT_DATA_TYPE);
            subjectAttribute.setValue(subject);

            Attribute actionAttribute = new Attribute();
            actionAttribute.setId("urn:oasis:names:tc:xacml:1.0:action:action-id");
            actionAttribute.setType(DEFAULT_DATA_TYPE);
            actionAttribute.setValue(action);

            Attribute resourceAttribute = new Attribute();
            resourceAttribute.setId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
            resourceAttribute.setType(DEFAULT_DATA_TYPE);
            resourceAttribute.setValue(resource);

            Attribute environmentAttribute = new Attribute();
            environmentAttribute.setId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
            environmentAttribute.setType(DEFAULT_DATA_TYPE);
            environmentAttribute.setValue(environment);

            return getActualDecision(new Attribute[]{subjectAttribute}, new Attribute[]{resourceAttribute},
                                     new Attribute[]{actionAttribute}, new Attribute[]{environmentAttribute}, appId);
        }

    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the default appID of the PDP proxy
     * @param subjectAttr XACML 2.0 subject
     * @param rescAttr XACML 2.0 resource
     * @param actionAttr XACML 2.0 action
     * @param envAttrs XACML 2.0 environments
     * @return the Entitlement Decision as a String
     * @throws Exception
     */
    public String getActualDecisionByAttributes(String subjectAttr, String rescAttr,
                                                String actionAttr, String[] envAttrs) throws Exception {
        return getActualDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);

    }

    /**
     * This methode is used to get the Entitlement decision for the provided subject,resource,action and environment using the provided appID of the PDP proxy
     * @param subjectAttr XACML 2.0 subject
     * @param rescAttr XACML 2.0 resource
     * @param actionAttr XACML 2.0 action
     * @param envAttrs XACML 2.0 environments
     * @param appId specific appID in the PDP Proxy, there can be many PDPs configured for appID. Each App can have distinct PDPs
     * @return the Entitlement Decision as a Boolean
     * @throws Exception
     */
    public String  getActualDecisionByAttributes(String subjectAttr, String rescAttr,
                                                 String actionAttr, String[] envAttrs, String appId)
            throws Exception {
        if(!config.getAppToPDPMap().containsKey(appId))   {
            throw new EntitlementProxyException("Invlaid App Id");
        }
        if(pdpProxy.enableCaching){
            String key = appId+subjectAttr+rescAttr+actionAttr;
            if(envAttrs!=null){
                for(String env:envAttrs){
                    key+=env;
                }
            }

            if(actualDecisionByAttributeCache.containsKey(key)) {
                return actualDecisionByAttributeCache.get(key);
            }
            else{
                String decision=proxy.getActualDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);
                actualDecisionByAttributeCache.put(key,decision);
                return decision;
            }
        } else{
            return proxy.getActualDecisionByAttributes(subjectAttr, rescAttr, actionAttr, envAttrs, appId);
        }

    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId, String appId) throws Exception {
        return proxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId,
                                             appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId) throws Exception {
        return subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes,
                                       domainId, appId);
    }

    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId, String appId)
            throws Exception {
        return proxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes,
                                             domainId, appId);
    }

    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action) throws Exception {
        return getActionableChidResourcesForAlias(alias, parentResource, action, appId);
    }

    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action, String appId) throws Exception {
        return proxy.getActionableChidResourcesForAlias(alias, parentResource, action, appId);
    }

    public List<String> getResourcesForAlias(String alias) throws Exception {
        return getResourcesForAlias(alias, appId);
    }

    public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
        return proxy.getResourcesForAlias(alias, appId);
    }

    public List<String> getActionableResourcesForAlias(String alias) throws Exception {
        return getActionableResourcesForAlias(alias, appId);
    }

    public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
        return proxy.getActionableResourcesForAlias(alias, appId);
    }

    public List<String> getActionsForResource(String alias, String resources) throws Exception {
        return getActionsForResource(alias, resources, appId);
    }

    public List<String> getActionsForResource(String alias, String resources, String appId)
            throws Exception {
        return proxy.getActionsForResource(alias, resources, appId);
    }

    private void validatePDPConfig(PDPConfig pdpConfig) {
        pdpProxy.messageFormat = pdpConfig.getMessageFormat();
        pdpProxy.appId = pdpConfig.getAppId();
        pdpProxy.enableCaching=pdpConfig.isEnableCaching();

        if (pdpProxy.messageFormat == null || pdpProxy.messageFormat.trim().length() == 0) {
            throw new IllegalArgumentException("Message format cannot be null or empty");
        }
        if (!ProxyConstants.JSON.equals(pdpProxy.messageFormat)
            && !ProxyConstants.SOAP.equals(pdpProxy.messageFormat)
            && !ProxyConstants.THRIFT.equals(pdpProxy.messageFormat)) {
            throw new IllegalArgumentException(
                    "Invalid message format. Should be json, soap or thrift");
        }
        this.config = pdpConfig;
    }

    private int genarateKey(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                            Attribute[] actionAttrs, Attribute[] envAttrs, String appId){
        int key = 1;
        key= 11 * key+ ((subjectAttrs == null) ? 0 : Arrays.hashCode(subjectAttrs));
        key= 31 * key+ ((rescAttrs == null) ? 0 : Arrays.hashCode(rescAttrs));
        key= 51 * key+ ((actionAttrs == null) ? 0 : Arrays.hashCode(actionAttrs));
        key= 71 * key+ ((envAttrs == null) ? 0 : Arrays.hashCode(envAttrs));
        key= 91 * key+ ((appId == null) ? 0 : appId.hashCode());
        return key;
    }

    public void cleanCache(){
        if(pdpProxy.enableCaching){
            pdpProxy.decisionByAttributeCache.clear();
            pdpProxy.decisionCache.clear();
            pdpProxy.actualDecisionByAttributeCache.clear();
            pdpProxy.actualDecisionCache.clear();
        }
    }

}
