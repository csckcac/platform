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
package org.wso2.carbon.identity.entitlement.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.xml.stream.XMLStreamException;

public class PolicyStore {

    private Registry registry;
    // The logger we'll use for all messages
    private static Log log = LogFactory.getLog(PolicyStore.class);

    public PolicyStore(Registry registry) throws IdentityException {
        if (registry == null) {
            log.error("Registry reference not set");
            throw new IdentityException("Registry reference not set");
        }
        this.registry = registry;
    }

    /**
     * This returns active policies as Registry resources. Also ordered resources according to ordering
     * number of the policy.
     * @return active policies as Resource[]
     * @throws IdentityException throws
     */
    public Resource[] getActivePolicies() throws IdentityException {
        String path = null;
        Collection collection = null;
        Resource resource;
        List<Resource> resourceList = new ArrayList<Resource>();
        String[] children = null;
        int[] policyOrdering = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving active entitlement policies");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            policyOrdering = new int[children.length];

            for (int i = 0; i < children.length; i++) {
                resource = registry.get(children[i]);
                if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                    resourceList.add(resource);
                    String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
                    if(policyOrder != null){
                        policyOrdering[i] = Integer.parseInt(policyOrder);
                    } else {
                        policyOrdering[i] = 0;
                    }
                }
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving active entitlement policies", e);
            throw new IdentityException("Error while retrieving active entitlement policies", e);
        }

        Resource[] resources = new Resource[resourceList.size()];
        if(resourceList.size() > 0){
            // sorting array            TODO  : with Comparator class
            int[] tempArray = new int[policyOrdering.length];
            Arrays.sort(policyOrdering);
            for (int i = 0; i < tempArray.length; i++) {
              int j = (policyOrdering.length-1)-i;
              tempArray[j] = policyOrdering[i];
            }
            policyOrdering = tempArray;

            for (int i = 0; i < policyOrdering.length; i++) {
                for(Resource res : resourceList){
                    String policyOrder =  res.getProperty(EntitlementConstants.POLICY_ORDER);
                    int order = 0;
                    if(policyOrder != null){
                        order =  Integer.parseInt(policyOrder);
                    }
                    if(policyOrdering[i] == order){
                        resources[i] = res;
                    }
                }
            }
        }

        return resources;
    }

    /**
     * This returns all the policies as Registry resources.
     * @return policies as Resource[]
     * @throws IdentityException throws if fails
     */
    public Resource[] getAllPolicies() throws IdentityException {
        String path = null;
        Collection collection = null;
        List<Resource> resources = new ArrayList<Resource>();
        String[] children = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();

            for (int i = 0; i < children.length; i++) {
                resources.add(registry.get(children[i]));
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy", e);
            throw new IdentityException("Error while retrieving entitlement policies", e);
        }

        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * This returns all the policy ids as String list. Here we assume registry resource name as
     * the policy id.
     * @return policy ids as String[]
     * @throws IdentityException throws if fails
     */
    public String[] getAllPolicyIds() throws IdentityException {
        String path = null;
        Collection collection = null;
        List<String> resources = new ArrayList<String>();
        String[] children = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            for (String child : children) {
                String[] resourcePath = child.split("/");
                if(resourcePath != null && resourcePath.length > 0 ){
                    resources.add(resourcePath[resourcePath.length -1]);                    
                }
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy resources", e);
            throw new IdentityException("Error while retrieving entitlement policy resources", e);
        }

        return resources.toArray(new String[resources.size()]);
    }


    /**
     * This returns given policy as Registry resource
     * @param policyId policy id
     * @return policy as Registry resource
     * @throws IdentityException throws, if fails
     */
    public Resource getPolicy(String policyId) throws IdentityException {
        String path = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT + policyId;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            return registry.get(path);
        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy", e);
            throw new IdentityException("Error while retrieving entitlement policy", e);
        }
    }

    /**
     *
     * @param policy
     * @throws IdentityException
     */
    public void addOrUpdatePolicy(PolicyDTO policy) throws IdentityException {
        String path = null;
        Resource resource = null;
        AuthorizationManager authorizationManager = null;
        boolean newResource = false;
        boolean newPolicy = false;
        OMElement omElement = null;        
        RealmService realmService = EntitlementServiceComponent.getRealmservice();
        String userName = ((UserRegistry)registry).getUserName();

        if (log.isDebugEnabled()) {
            log.debug("Creating or updating entitlement policy");
        }

        if(policy == null || policy.getPolicyId() == null){
            log.error("Error while creating or updating entitlement policy: " +
                      "Policy DTO or Policy Id can not be null");
            throw new IdentityException("Error while creating or updating entitlement policy: " +
                                        "Policy DTO or Policy Id can not be null");            
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT + policy.getPolicyId();
            int tenantId = ((UserRegistry)registry).getTenantId();
            if(realmService != null){
                authorizationManager = realmService.
                        getTenantUserRealm(tenantId).getAuthorizationManager();
                if(authorizationManager != null){
                    if(authorizationManager.isUserAuthorized(userName,
                                                          EntitlementConstants.AUTHORIZATION_PERMISSION,
                                                          "ui.execute" )){
                        authorizationManager.authorizeUser(userName,
                                                           RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                           IdentityRegistryResources.ENTITLEMENT,
                                                           "write");
                    } else {
                        log.error("User is not authorize to create or update entitlement policy");
                        throw new IdentityException("User is not authorize to create or update entitlement policy");
                    }
                } else {
                    log.error("Error while creating or updating entitlement policy: " +
                              "Authorization Manager can not be null");
                    throw new IdentityException("Error while creating or updating entitlement policy: " +
                                                "Authorization Manager can not be null");                    
                }
            } else {
                log.error("Error while creating or updating entitlement policy: " +
                          "Realm Service can not be null");
                throw new IdentityException("Error while creating or updating entitlement policy: " +
                                            "Realm Service can not be null");
            }

            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newResource();
                newResource = true;
            }

            Collection policyCollection;
            if(registry.resourceExists(IdentityRegistryResources.ENTITLEMENT)){
                policyCollection = (Collection) registry.get(IdentityRegistryResources.ENTITLEMENT);
            } else {
                policyCollection = registry.newCollection();
            }

            if(policy.getNeighborId() != null && policy.getNeighborId().trim().length() > 0){
                String neighborPath = IdentityRegistryResources.ENTITLEMENT + policy.getNeighborId();
                if (registry.resourceExists(neighborPath)) {
                    Resource neighborPolicy = registry.get(neighborPath);
                    String neighborPolicyOrder = neighborPolicy.
                            getProperty(EntitlementConstants.POLICY_ORDER);
                    if(neighborPolicyOrder != null){
                        policy.setPolicyOrder(Integer.parseInt(neighborPolicyOrder));
                    }
                } else {
                    if(log.isWarnEnabled()){
                        log.warn("Invalid policy Id as neighbor policy id");
                    }
                }
            }

            if(policy.getPolicyOrder() > 0){
                String noOfPolicies = policyCollection.getProperty(EntitlementConstants.MAX_POLICY_ORDER);
                if(noOfPolicies != null &&  Integer.parseInt(noOfPolicies) < policy.getPolicyOrder()){
                    policyCollection.setProperty(EntitlementConstants.MAX_POLICY_ORDER,
                                                 Integer.toString(policy.getPolicyOrder()));
                    registry.put(IdentityRegistryResources.ENTITLEMENT, policyCollection);
                } else {
                    reOrderPolicy(policyCollection,policy.getPolicyOrder());
                }
                resource.setProperty(EntitlementConstants.POLICY_ORDER,
                                     Integer.toString(policy.getPolicyOrder()));                
            } else {
                String previousOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
                if(previousOrder == null){
                    if(policyCollection != null){
                        int policyOrder = 1;
                        String noOfPolicies = policyCollection.getProperty(EntitlementConstants.MAX_POLICY_ORDER);
                        if(noOfPolicies != null){
                            policyOrder = policyOrder + Integer.parseInt(noOfPolicies);
                        }
                        policyCollection.setProperty(EntitlementConstants.MAX_POLICY_ORDER,
                                                     Integer.toString(policyOrder));
                        resource.setProperty(EntitlementConstants.POLICY_ORDER, Integer.toString(policyOrder));
                    }
                    registry.put(IdentityRegistryResources.ENTITLEMENT, policyCollection);
                }
            }

            // Cleat authorization after re-ordering of policies
            if(!newResource){
                authorizationManager.clearUserAuthorization(userName,
                                                           RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                           IdentityRegistryResources.ENTITLEMENT,
                                                           "write");                
            }

            if(policy.getPolicy() != null && policy.getPolicy().trim().length() > 0){
                resource.setContent(policy.getPolicy());
                newPolicy = true;
                PolicyMetaDataBuilder policyMetaDataBuilder = new PolicyMetaDataBuilder();
                Properties properties = policyMetaDataBuilder.getPolicyMetaDataFromPolicy(policy.getPolicy());
                for (Object o : properties.keySet()) {
                    String key = o.toString();
                    resource.setProperty(key, properties.getProperty(key));
                }                                
            }
            resource.setProperty(EntitlementConstants.ACTIVE_POLICY, Boolean.toString(policy.isActive()));

            if(policy.getPolicyType() != null && policy.getPolicyType().trim().length() > 0){
                resource.setProperty(EntitlementConstants.POLICY_TYPE, policy.getPolicyType());
            } else {
                try {
                    if(newPolicy){
                        omElement = AXIOMUtil.stringToOM(policy.getPolicy());
                        resource.setProperty(EntitlementConstants.POLICY_TYPE, omElement.getLocalName());
                    }
                } catch (XMLStreamException e) {
                    policy.setPolicyType(EntitlementConstants.POLICY_ELEMENT);
                    log.warn("Policy Type can not be found. Default type is set");
                }                
            }

            if(omElement != null){
                Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.
                        POLICY_REFERENCE);
                if(iterator1 != null){
                    String policyReferences = "";
                    while(iterator1.hasNext()){
                        OMElement policyReference = (OMElement) iterator1.next();
                        if(!"".equals(policyReferences)){
                            policyReferences = policyReferences + EntitlementConstants.ATTRIBUTE_SEPARATOR
                                               + policyReference.getText();
                        } else {
                            policyReferences =  policyReference.getText();
                        }
                    }
                    resource.setProperty(EntitlementConstants.POLICY_REFERENCE, policyReferences);
                }

                Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.
                        POLICY_SET_REFERENCE);
                if(iterator2 != null){
                    String policySetReferences = "";
                    while(iterator1.hasNext()){
                        OMElement policySetReference = (OMElement) iterator2.next();
                        if(!"".equals(policySetReferences)){
                            policySetReferences = policySetReferences + EntitlementConstants.ATTRIBUTE_SEPARATOR
                                                  + policySetReference.getText();
                        } else {
                            policySetReferences =  policySetReference.getText();
                        }
                    }
                    resource.setProperty(EntitlementConstants.POLICY_SET_REFERENCE, policySetReferences);
                }
            }

            //before writing basic policy editor meta data as properties,
            //delete any properties related to them
            String policyEditor = resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE);
            if(newPolicy && policyEditor != null){
                resource.removeProperty(EntitlementConstants.POLICY_EDITOR_TYPE);
            }

            //write policy meta data that is used for basic policy editor
            if(policy.getPolicyEditor() != null && policy.getPolicyEditor().trim().length() > 0){
                resource.setProperty(EntitlementConstants.POLICY_EDITOR_TYPE, policy.getPolicyEditor().trim());
            }
            String[] policyMetaData = policy.getBasicPolicyEditorMetaData();
            if(policyMetaData != null && policyMetaData.length > 0){
                String BasicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                        BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
                if(newPolicy && BasicPolicyEditorMetaDataAmount != null){
                    int amount = Integer.parseInt(BasicPolicyEditorMetaDataAmount);
                    for(int i = 0; i < amount; i++){
                        resource.removeProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                    }
                    resource.removeProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
                }

                int i = 0;
                for(String policyData : policyMetaData){
                    if(policyData != null && !"".equals(policyData)){
                        resource.setProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i,
                                             policyData);
                    }
                    i++;
                }
                resource.setProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT,
                                     Integer.toString(i));
            }

            registry.put(path, resource);

            authorizationManager.clearUserAuthorization(userName,
                                                       RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       IdentityRegistryResources.ENTITLEMENT,
                                                       "write");
            authorizationManager.authorizeUser(userName,
                                               RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                               path , "write");
            authorizationManager.authorizeUser(userName,
                                               RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                               path , "delete");

        } catch (RegistryException e) {
            log.error("Error while adding or updating entitlement policy", e);
            throw new IdentityException("Error while adding or updating entitlement policy", e);
        } catch (UserStoreException e) {
            log.error("Error while adding or updating entitlement policy", e);
            throw new IdentityException("Error while adding or updating entitlement policy", e);
        }
    }

    /**
     * Re-orders the policy property values called policyOrder in all policies
     * when new policy is added or updated
     * @param collection Collection that contains all policies
     * @param orderNo new order number
     * @throws org.wso2.carbon.identity.base.IdentityException throws
     */
    private void reOrderPolicy(Collection collection, int orderNo)
            throws IdentityException {

        try {
            String[] resources = collection.getChildren();
            for(int i = 0; i < resources.length; i++){
                Resource resource = registry.get(resources[i]);
                String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
                if(policyOrder != null){
                    int currentOrder = Integer.parseInt(policyOrder);
                    if(orderNo <= currentOrder){
                        currentOrder = currentOrder + 1;
                        resource.setProperty(EntitlementConstants.POLICY_ORDER,
                                             Integer.toString(currentOrder));
                        registry.put(resources[i], resource);
                        String maxPolicyOrder = collection.getProperty(EntitlementConstants.MAX_POLICY_ORDER);
                        if(maxPolicyOrder != null &&  Integer.parseInt(maxPolicyOrder) < currentOrder){
                            collection.setProperty(EntitlementConstants.MAX_POLICY_ORDER,
                                                         Integer.toString(currentOrder));
                            registry.put(IdentityRegistryResources.ENTITLEMENT, collection);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while re-ordering entitlement policy", e);
            throw new IdentityException("Error while re-ordering entitlement policy", e);
        }

    }

    /**
     *
     * @param policyId
     * @throws IdentityException
     */
    public void removePolicy(String policyId) throws IdentityException {
        String path = null;

        if (log.isDebugEnabled()) {
            log.debug("Removing entitlement policy");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT + policyId;
            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return;
            }
            registry.delete(path);
        } catch (RegistryException e) {
            log.error("Error while removing entitlement policy", e);
            throw new IdentityException("Error while removing policy", e);
        }
    }

    /**
     * This method is used get resources which contains bags of pre-defined data related to XACML
     * Policy from registry
     *
     * @param resourceName
     *            registry resource name
     * @return registry resource
     * @throws org.wso2.carbon.identity.base.IdentityException
     */
    public Resource getEntitlementPolicyResources(String resourceName) throws IdentityException {
        String path = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy resources");
        }

        try {
            path = IdentityRegistryResources.ENTITLEMENT_POLICY_RESOURCES + resourceName;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy resource which does not exist");
                }
                return null;
            }
            return registry.get(path);
        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy resources", e);
            throw new IdentityException("Error while retrieving entitlement policy resources", e);
        }
    }

    /**
     * This persist the policy combining algorithm in to the registry
     * @param policyCombiningAlgorithm   policy combining algorithm name to persist
     * @throws IdentityException throws
     */
    public void addPolicyCombiningAlgorithm(String policyCombiningAlgorithm) throws IdentityException {
        try {
            Collection policyCollection;
            if(registry.resourceExists(IdentityRegistryResources.ENTITLEMENT)){
                policyCollection = (Collection) registry.get(IdentityRegistryResources.ENTITLEMENT);
            } else {
                policyCollection = registry.newCollection();
            }
            policyCollection.setProperty("globalPolicyCombiningAlgorithm", policyCombiningAlgorithm);
            registry.put(IdentityRegistryResources.ENTITLEMENT, policyCollection);
        } catch (RegistryException e) {
            log.error("Error while writing entitlement policy resources", e);
            throw new IdentityException("Error while writing entitlement policy resources", e);
        }
    }

    /**
     * This gets the policy collection
     * @return policy collection as Registry collection
     * @throws IdentityException throws, if fails
     */
    public Collection getPolicyCollection() throws IdentityException {
        try {
            if(registry.resourceExists(IdentityRegistryResources.ENTITLEMENT)){
                return  (Collection) registry.get(IdentityRegistryResources.ENTITLEMENT);
            }
            return null;
        } catch (RegistryException e) {
            log.error("Error while reading policy collection", e);
            throw new IdentityException("Error while reading policy collection", e);
        }
    }

    /**
     * returns the registry
     * @return  Registry
     */
    public Registry getRegistry() {
        return registry;
    }
}
