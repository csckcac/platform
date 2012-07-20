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

import org.wso2.balana.*;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.EvaluationCtxFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.EntitlementService;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.dto.AttributeValueDTO;
import org.wso2.carbon.identity.entitlement.dto.EntitledAttributesDTO;
import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.finder.RegistryBasedPolicyFinder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *  This contains the searching methods for policies based on policy attribute values and how subjects
 * are entitled to resources
 */
public class PolicySearch {

	private static Log log = LogFactory.getLog(PolicySearch.class);

	/**
	 * This returns resource name as the list of the entitled attributes for given
     * user or role and action, after evaluating the all the active policies in the PDP
	 *
     * @param subjectName  subject name 
     * @param resourceName resource name
     * @param subjectId subject attribute Id
     * @param action  Action Name
     * @param enableChildSearch   whether search is done for the child resources under the given  resource name
     * @param useApplicablePolices whether search is done only for applicable policies in given search parameters
     * @return entitled resource id set
     * @throws IdentityException throws
	 */
	public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                             String subjectId, String action, boolean enableChildSearch,
                                             boolean useApplicablePolices) throws IdentityException {

		PolicyStoreReader policyStoreReader;
        AttributeValueDTO subjectAttributeDTO;
        List<PolicyDTO> policyDTOs = new ArrayList<PolicyDTO>();
        boolean hierarchicalResource = false;
        EntitledResultSetDTO resultSetDTO = new EntitledResultSetDTO();
        Set<EntitledAttributesDTO> resultSet = new HashSet<EntitledAttributesDTO>();
        EntitlementService entitlementService = new EntitlementService();

        try {
			policyStoreReader = new PolicyStoreReader(new PolicyStore(EntitlementServiceComponent
					.getRegistryService().getGovernanceSystemRegistry()));
		} catch (RegistryException e) {
			throw new IdentityException("Error while initializing policy store");
		}

        if(useApplicablePolices){
            List<String> matchingPolicyIds = getMatchPolicyIds(subjectName, resourceName, subjectId, action);
            if(matchingPolicyIds != null && matchingPolicyIds.size() > 0){
                for(String policyId : matchingPolicyIds){
                    policyDTOs.add(policyStoreReader.readPolicyDTO(policyId));
                }
            } else {
                log.warn("Matching policies can not be found. Therefore all the policies are evaluated");
                policyDTOs.addAll(Arrays.asList(policyStoreReader.readAllLightPolicyDTOs()));
            }
        } else {
            policyDTOs.addAll(Arrays.asList(policyStoreReader.readAllLightPolicyDTOs()));
        }


        if(subjectName != null && subjectName.trim().length() > 0){
            subjectAttributeDTO = new AttributeValueDTO();
            subjectAttributeDTO.setAttributeType(EntitlementConstants.SUBJECT_ELEMENT);
            subjectAttributeDTO.setAttribute(subjectName);
            subjectAttributeDTO.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
            if(subjectId != null && subjectId.trim().length() > 0){
                subjectAttributeDTO.setAttributeId(subjectId);
            } else {
                subjectAttributeDTO.setAttributeId(EntitlementConstants.SUBJECT_ID_DEFAULT);
            }
        } else {
            throw new IdentityException("Error : subject value can not be null");
        }

        for (PolicyDTO policyDTO : policyDTOs) {
            if(policyDTO.isActive()){
                PolicyDTO metaDataPolicyDTO = policyStoreReader.readMetaDataPolicyDTO(policyDTO.getPolicyId());
                List<AttributeValueDTO> attributeValueDTOs = Arrays.asList(metaDataPolicyDTO.getPolicyMetaData());
                String[] policyIdRef = policyDTO.getPolicyIdReferences();
                String[] policySetIdRef = policyDTO.getPolicySetIdReferences();                
                for(String policyId : policyIdRef){
                    PolicyDTO dto =  policyStoreReader.readMetaDataPolicyDTO(policyId);
                    attributeValueDTOs.addAll(Arrays.asList(dto.getPolicyMetaData()));
                }
                for(String policySetId : policySetIdRef){
                    PolicyDTO dto =  policyStoreReader.readMetaDataPolicyDTO(policySetId);
                    attributeValueDTOs.addAll(Arrays.asList(dto.getPolicyMetaData()));
                }
                if (attributeValueDTOs != null) {
                    String response = null;
                    List <AttributeValueDTO> actions = new ArrayList<AttributeValueDTO>();
                    List <AttributeValueDTO> resources = new ArrayList<AttributeValueDTO>();
                    List <AttributeValueDTO> requestAttributes = new ArrayList<AttributeValueDTO>();
                    if(resourceName != null && resourceName.trim().length() > 0){
                        AttributeValueDTO resourceAttribute = new AttributeValueDTO();
                        resourceAttribute.setAttribute(resourceName);
                        resourceAttribute.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
                        resourceAttribute.setAttributeId(EntitlementConstants.RESOURCE_ID_DEFAULT);
                        resourceAttribute.setAttributeType(EntitlementConstants.RESOURCE_ELEMENT);
                        resources.add(resourceAttribute);
                        hierarchicalResource = true;
                    }

                    AttributeValueDTO resourceScopeAttribute = new AttributeValueDTO();
                    resourceScopeAttribute.setAttribute(EntitlementConstants.RESOURCE_DESCENDANTS);
                    resourceScopeAttribute.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
                    resourceScopeAttribute.setAttributeId(EntitlementConstants.RESOURCE_SCOPE_ID);
                    resourceScopeAttribute.setAttributeType(EntitlementConstants.RESOURCE_ELEMENT);

                    for(AttributeValueDTO attributeValueDTO : attributeValueDTOs){
                        if (EntitlementConstants.ENVIRONMENT_ELEMENT.equals(attributeValueDTO
                                                .getAttributeType())) {
                            requestAttributes.add(attributeValueDTO);
                            attributeValueDTO.setAttributeId(EntitlementConstants.ENVIRONMENT_ID_DEFAULT);
                            requestAttributes.add(attributeValueDTO);
                        } else if (EntitlementConstants.ACTION_ELEMENT.equals(attributeValueDTO
                                                .getAttributeType())) {
                            if(action != null && action.trim().length() > 0){
                                attributeValueDTO.setAttribute(action);
                            }
                            actions.add(attributeValueDTO);
                            attributeValueDTO.setAttributeId(EntitlementConstants.ACTION_ID_DEFAULT);
                            actions.add(attributeValueDTO);
                        } else if (EntitlementConstants.RESOURCE_ELEMENT.equals(attributeValueDTO
                                                .getAttributeType()) && !hierarchicalResource){
                            attributeValueDTO.setAttributeId(EntitlementConstants.RESOURCE_ID_DEFAULT);
                            resources.add(attributeValueDTO);
                        } 
//                        else if(EntitlementConstants.UNKNOWN.equals(attributeValueDTO.getAttributeType())){
//                            // TODO finding other possible cases for invalid search scenarios and providing errors
//                            resultSetDTO.setMessage(attributeValueDTO.getAttribute() + " in policy : "
//                                + policyDTO.getPolicyId() + " " + EntitlementConstants.SEARCH_ERROR_MESSAGE);
//                            resultSetDTO.setMessageType(EntitlementConstants.SEARCH_ERROR);
//                            resultSetDTO.setEntitledAttributesDTOs(null);
//                            break;
//                        }
                    }

                    if(resultSetDTO.getMessage() == null){
                        for (AttributeValueDTO resource : resources) {
                            if (EntitlementConstants.RESOURCE_ELEMENT.equals(resource.getAttributeType())) {
                                for(AttributeValueDTO actionAttributeDTO : actions){
                                    int noOfRequests = 1;
                                    if(enableChildSearch){
                                        noOfRequests = 0;
                                    }
                                    while(noOfRequests < 2){
                                        List<AttributeValueDTO>  currentRequestAttributes =
                                                                    new ArrayList<AttributeValueDTO>();
                                        for(AttributeValueDTO dto : requestAttributes){
                                            currentRequestAttributes.add(dto);
                                        }
                                        if(noOfRequests  < 1){
                                            currentRequestAttributes.add(resourceScopeAttribute);
                                        }
                                        currentRequestAttributes.add(subjectAttributeDTO);
                                        currentRequestAttributes.add(resource);
                                        currentRequestAttributes.add(actionAttributeDTO);

                                        Document doc = EntitlementUtil.createRequestElement(currentRequestAttributes);
                                        if (doc != null){
                                            String request = EntitlementUtil.getStringFromDocument(doc);
                                            try {
                                                response = entitlementService.getDecision(request);
                                            } catch (Exception e) {
                                               throw new IdentityException("Error while searching entitled resources");
                                            }
                                        }

                                        if (response != null) {
                                            OMElement omResponse = null;
                                            OMElement result = null;
                                            OMElement decision = null;
                                            try {
                                                omResponse = AXIOMUtil.stringToOM(response);
                                            } catch (XMLStreamException e) {
                                                throw new IdentityException(
                                                        "Error while evaluating XACML policy");
                                            }
                                            if (omResponse != null) {
                                                Iterator results = omResponse.getChildrenWithName(new QName(
                                                        EntitlementConstants.RESPONSE_RESULT));
                                                while(results.hasNext()){
                                                    result = (OMElement) results.next();
                                                    if (result != null) {
                                                        decision = result.getFirstChildWithName(new QName(
                                                                EntitlementConstants.RESPONSE_DECISION));
                                                        if (decision != null) {
                                                            if (EntitlementConstants.RULE_EFFECT_PERMIT
                                                                    .equals(decision.getText())) {
                                                                EntitledAttributesDTO dto = new EntitledAttributesDTO();
                                                                String decisionResource = result
                                                                        .getAttributeValue(new QName(
                                                                                EntitlementConstants.RESPONSE_RESOURCE_ID));
                                                                dto.setResourceName(decisionResource);
                                                                dto.setAction(actionAttributeDTO.getAttribute());
                                                                resultSet.add(dto);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        noOfRequests ++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        resultSetDTO.setEntitledAttributesDTOs(resultSet.
                        toArray(new EntitledAttributesDTO[resultSet.size()]));

		return resultSetDTO;
	}

    /**
     * Gets matching policies for given request
     * @param subjectName  subject name
     * @param resourceName resource name
     * @param subjectId subject attribute Id
     * @param actionName action name
     * @return List of policy ids
     * @throws IdentityException throws, if fails
     */
    private List<String> getMatchPolicyIds(String subjectName, String resourceName, String subjectId,
                                          String actionName) throws IdentityException {

        EntitlementEngine entitlementEngine = null;
        RegistryBasedPolicyFinder policyFinder;
		PolicyStoreReader policyStoreReader;
        PolicyDTO[] policyDTOs;
        boolean oneAction = false;
        boolean oneResource = false;
        List<String> result = null;
        List<AttributeValueDTO> requestAttributes = new ArrayList<AttributeValueDTO>();
        List<AttributeValueDTO> attributeValueDTOs = new ArrayList<AttributeValueDTO>();
        try {
			policyStoreReader = new PolicyStoreReader(new PolicyStore(EntitlementServiceComponent
					.getRegistryService().getGovernanceSystemRegistry()));
		} catch (RegistryException e) {
			throw new IdentityException("Error while initializing policy store");
		}

        AttributeValueDTO subject = new AttributeValueDTO();
        subject.setAttributeId(subjectId);
        subject.setAttribute(subjectName);
        subject.setAttributeType(EntitlementConstants.SUBJECT_ELEMENT);
        subject.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
        requestAttributes.add(subject);

        AttributeValueDTO resource = new AttributeValueDTO();
        resource.setAttributeId(EntitlementConstants.RESOURCE_ID_DEFAULT);
        resource.setAttributeType(EntitlementConstants.RESOURCE_ELEMENT);
        resource.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
        if(resourceName != null && resourceName.trim().length() > 0){
            resource.setAttribute(resourceName);
        } else {
            resource.setAttribute("  ");         // TODO    
        }
        requestAttributes.add(resource);

        if(actionName != null && actionName.trim().length() > 0){
            AttributeValueDTO action = new AttributeValueDTO();
            action.setAttributeId(EntitlementConstants.ACTION_ID_DEFAULT);
            action.setAttribute(actionName);
            action.setAttributeType(EntitlementConstants.ACTION_ELEMENT);
            action.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
            requestAttributes.add(action);
            oneAction = true;
        }

        policyDTOs = policyStoreReader.readAllLightPolicyDTOs();

        for (PolicyDTO policyDTO : policyDTOs) {
            if(policyDTO.isActive()){
                PolicyDTO metaDataPolicyDTO = policyStoreReader.readMetaDataPolicyDTO(policyDTO.getPolicyId());
                attributeValueDTOs.addAll(Arrays.asList(metaDataPolicyDTO.getPolicyMetaData()));
                String[] policyIdRef = policyDTO.getPolicyIdReferences();
                String[] policySetIdRef = policyDTO.getPolicySetIdReferences();
                for(String policyId : policyIdRef){
                    PolicyDTO dto =  policyStoreReader.readMetaDataPolicyDTO(policyId);
                    attributeValueDTOs.addAll(Arrays.asList(dto.getPolicyMetaData()));
                }
                for(String policySetId : policySetIdRef){
                    PolicyDTO dto =  policyStoreReader.readMetaDataPolicyDTO(policySetId);
                    attributeValueDTOs.addAll(Arrays.asList(dto.getPolicyMetaData()));
                }
            }
        }

        for(AttributeValueDTO dto : attributeValueDTOs){
            if(!oneAction){
                if(EntitlementConstants.ACTION_ELEMENT.equals(dto.getAttributeType())){
                    dto.setAttributeId(EntitlementConstants.ACTION_ID_DEFAULT);
                    dto.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
                    requestAttributes.add(dto);
                }
            }
            if(EntitlementConstants.ENVIRONMENT_ELEMENT.equals(dto.getAttributeType())){
                dto.setAttributeId(EntitlementConstants.ENVIRONMENT_ID_DEFAULT);
                dto.setAttributeDataType(EntitlementConstants.STRING_DATA_TYPE);
                requestAttributes.add(dto);
            }
        }

        try {
            entitlementEngine = EntitlementEngine.
                    getInstance(EntitlementServiceComponent.getRegistryService().
                            getGovernanceSystemRegistry(CarbonContext.getCurrentContext().getTenantId()),
                                CarbonContext.getCurrentContext().getTenantId());

        } catch (RegistryException e) {
            throw new IdentityException("Entitlement Engine can not be initialized", e);
        }

        policyFinder = entitlementEngine.getRegistryModule();

        if(policyFinder != null){
            Document doc = EntitlementUtil.createRequestElement(requestAttributes);
            if(doc != null){
                try {
                    AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().
                                                            getRequestCtx((doc.getDocumentElement()));
                    EvaluationCtx ctx = EvaluationCtxFactory.getFactory().
                            getEvaluationCtx(requestCtx, entitlementEngine.getPdpConfig());
                    result = policyFinder.getMatchingPolicies(ctx);
                } catch (ParsingException e) {
                    throw new IdentityException("Error while creating XACML Request context", e);
                }
            }
        }

        return result;
    }
}
