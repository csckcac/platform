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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.balana.AbstractPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PolicyStoreReader {

    /**
     * The property which is used to specify the schema file to validate against (if any). Note that
     * this isn't used directly by <code>PolicyReader</code>, but is referenced by many classes that
     * use this class to load policies.
     */
    public static final String POLICY_SCHEMA_PROPERTY = "com.sun.xacml.PolicySchema";

    // the optional logger used for error reporting
    private static Log log = LogFactory.getLog(PolicyStoreReader.class);

    private PolicyStore store;

    /**
     * 
     * @param store
     */
    public PolicyStoreReader(PolicyStore store) {
        this.store = store;
    }

    /**
     * 
     * @param policyId
     * @return
     * @throws IdentityException
     */
    public synchronized AbstractPolicy readPolicy(String policyId) throws IdentityException {
        Resource resource = null;
        resource = store.getPolicy(policyId);
        return readPolicy(resource);
    }

    /**
     * 
     * @return
     * @throws IdentityException
     */
    public synchronized AbstractPolicy[] readPolicies() throws IdentityException {
        Resource[] resources = null;
        AbstractPolicy[] policies = null;
        resources = store.getActivePolicies();

        if (resources == null) {
            return new AbstractPolicy[0];
        }
        policies = new AbstractPolicy[resources.length];

        for (int i = 0; i < resources.length; i++) {
            if(resources[i] != null){
                policies[i] = readPolicy(resources[i]);                
            }
        }

        return policies;
    }

    /**
     * Gets all policy targets in policies
     * @return policy targets as PolicyTarget object Array
     * @throws IdentityException throws
     */
    public synchronized PolicyTarget[] readTargets() throws IdentityException {
        Resource[] resources = null;
        PolicyTarget[] targets = null;
        resources = store.getActivePolicies();  // TODO need to give limit for this or load one my one

        if (resources == null) {
            return new PolicyTarget[0];
        }
        targets = new PolicyTarget[resources.length];

        for (int i = 0; i < resources.length; i++) {
            if(resources[i] != null){
                targets[i] = readTarget(resources[i]);                
            }
        }

        return targets;
    }

    /**
     * 
     * @param resource
     * @return
     * @throws IdentityException
     */
    private AbstractPolicy readPolicy(Resource resource) throws IdentityException {
        String policy = null;
        try {
            policy = new String((byte[]) resource.getContent());
            return PolicyReader.getInstance(null, null).getPolicy(policy);
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }

    /**
     * Gets policy targets in a policy
     * @param resource registry resource name
     * @return policy targets as PolicyTarget object 
     * @throws IdentityException throws
     */
    private PolicyTarget readTarget(Resource resource) throws IdentityException {
        String policy = null;
        try {
            policy = new String((byte[]) resource.getContent());
            return PolicyReader.getInstance(null, null).getTarget(policy);
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }

    /**
     * Reads All policies as PolicyDTO
     * @return Array of PolicyDTO 
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO[] readAllPolicyDTOs() throws IdentityException {
        Resource[] resources = null;
        PolicyDTO[] policies = null;
        resources = store.getAllPolicies();

        if (resources == null) {
            return new PolicyDTO[0];
        }
        policies = new PolicyDTO[resources.length];

        List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
        int[] policyOrder = new int[resources.length];

        for(int i = 0; i < resources.length; i++){
            PolicyDTO policyDTO = readPolicyDTO(resources[i]);
            policyDTOList.add(policyDTO);
            policyOrder[i] = policyDTO.getPolicyOrder();
        }

        // sorting array            TODO  : with Comparator class
        int[] tempArray = new int[policyOrder.length];
        Arrays.sort(policyOrder);
        for (int i = 0; i < tempArray.length; i++) {
            int j = (policyOrder.length-1)-i;
            tempArray[j] = policyOrder[i];
        }
        policyOrder = tempArray;

        for (int i = 0; i < policyOrder.length; i++) {
            for(PolicyDTO policyDTO : policyDTOList){
                if(policyOrder[i] == policyDTO.getPolicyOrder()){
                    policies[i] = policyDTO;
                }                
            }
        }
        
        return policies;
    }

    /**
     * Reads All policies as Light Weight PolicyDTO
     * @return Array of PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO[] readAllLightPolicyDTOs() throws IdentityException {
        String[] resources = null;
        PolicyDTO[] policies = null;
        resources = store.getAllPolicyIds();

        if (resources == null) {
            return new PolicyDTO[0];
        }
        policies = new PolicyDTO[resources.length];

        List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
        int[] policyOrder = new int[resources.length];

        for(int i = 0; i < resources.length; i++){
            PolicyDTO policyDTO = readLightPolicyDTO(resources[i]);
            policyDTOList.add(policyDTO);
            policyOrder[i] = policyDTO.getPolicyOrder();
        }

        // sorting array            TODO  : with Comparator class
        int[] tempArray = new int[policyOrder.length];
        Arrays.sort(policyOrder);
        for (int i = 0; i < tempArray.length; i++) {
            int j = (policyOrder.length-1)-i;
            tempArray[j] = policyOrder[i];
        }
        policyOrder = tempArray;

        for (int i = 0; i < policyOrder.length; i++) {
            for(PolicyDTO policyDTO : policyDTOList){
                if(policyOrder[i] == policyDTO.getPolicyOrder()){
                    policies[i] = policyDTO;
                }
            }
        }

        return policies;
    }

    /**
     * Reads PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO 
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readPolicyDTO(String policyId) throws IdentityException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;        
        try {
            resource = store.getPolicy(policyId);
            if (resource == null) {
                return null;
            }
            if(store.getRegistry() != null){
                String userName = ((UserRegistry)store.getRegistry()).getUserName();
                int tenantId = ((UserRegistry)store.getRegistry()).getTenantId();
                RealmService realmService = EntitlementServiceComponent.getRealmservice();
                if(realmService != null){
                    policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"write" );
                    policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"delete" );
                }
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(new String((byte[]) resource.getContent()));
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);            
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }      
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }            
            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setBasicPolicyEditorMetaData(basicPolicyEditorMetaData);
            }
            PolicyMetaDataBuilder policyMetaDataBuilder = new PolicyMetaDataBuilder();
            dto.setPolicyMetaData(policyMetaDataBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }


    /**
     * Reads Light Weight PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readLightPolicyDTO(String policyId) throws IdentityException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            resource = store.getPolicy(policyId);
            if (resource == null) {
                return null;
            }
            if(store.getRegistry() != null){
                String userName = ((UserRegistry)store.getRegistry()).getUserName();
                int tenantId = ((UserRegistry)store.getRegistry()).getTenantId();
                RealmService realmService = EntitlementServiceComponent.getRealmservice();
                if(realmService != null){
                    policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"write" );
                    policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"delete" );
                }
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }
            
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));

            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }

    /**
     * Reads Light Weight PolicyDTO with Attribute meta data for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readMetaDataPolicyDTO(String policyId) throws IdentityException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            resource = store.getPolicy(policyId);
            if (resource == null) {
                return null;
            }
            if(store.getRegistry() != null){
                String userName = ((UserRegistry)store.getRegistry()).getUserName();
                int tenantId = ((UserRegistry)store.getRegistry()).getTenantId();
                RealmService realmService = EntitlementServiceComponent.getRealmservice();
                if(realmService != null){
                    policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"write" );
                    policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"delete" );
                }
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }

            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setBasicPolicyEditorMetaData(basicPolicyEditorMetaData);
            }
            PolicyMetaDataBuilder policyMetaDataBuilder = new PolicyMetaDataBuilder();
            dto.setPolicyMetaData(policyMetaDataBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }

    /**
     * Reads PolicyDTO for given registry resource
     * @param resource Registry resource
     * @return  PolicyDTO
     * @throws IdentityException throws, if fails
     */
    private PolicyDTO readPolicyDTO(Resource resource) throws IdentityException {
        String policy = null;
        AbstractPolicy absPolicy = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            if(store.getRegistry() != null){
                String userName = ((UserRegistry)store.getRegistry()).getUserName();
                int tenantId = ((UserRegistry)store.getRegistry()).getTenantId();
                RealmService realmService = EntitlementServiceComponent.getRealmservice();
                if(realmService != null){
                    policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"write" );
                    policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                            isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                       resource.getPath(),"delete" );
                }
            }

            policy = new String((byte[]) resource.getContent());
            absPolicy = PolicyReader.getInstance(null, null).getPolicy(policy);
            dto = new PolicyDTO();
            dto.setPolicyId(absPolicy.getId().toASCIIString());
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);                
            }
            
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setBasicPolicyEditorMetaData(basicPolicyEditorMetaData);
            }
            PolicyMetaDataBuilder policyMetaDataBuilder = new PolicyMetaDataBuilder();
            dto.setPolicyMetaData(policyMetaDataBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy", e);
            throw new IdentityException("Error while loading entitlement policy", e);
        }
    }

    /**
     * This reads the policy combining algorithm from registry resource property
     * @return policy combining algorithm as String
     * @throws IdentityException throws
     */
    public String readPolicyCombiningAlgorithm() throws IdentityException {

        try {
            Collection policyCollection = store.getPolicyCollection();
            if(policyCollection != null){
                return policyCollection.getProperty("globalPolicyCombiningAlgorithm");
            }            
            return null;
        } catch (IdentityException e) {
            log.error("Error while reading policy combining algorithm", e);
            throw new IdentityException("Error while reading policy combining algorithm", e);
        }
    }    
}
