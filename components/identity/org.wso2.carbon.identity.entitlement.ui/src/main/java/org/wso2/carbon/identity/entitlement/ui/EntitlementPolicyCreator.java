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

package org.wso2.carbon.identity.entitlement.ui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.entitlement.ui.dto.*;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.*;
import java.util.List;

/**
 * create XACML policy and convert it to a String Object
 */
public class EntitlementPolicyCreator {

    /**
     * Create XACML policy using the data received from XACML policy wizard
     * @param policyElementDTO  policy element
     * @param subElementDTOs   target elements
     * @param ruleElementDTOs  rule elements
     * @return  String object of the XACML policy
     * @throws EntitlementPolicyCreationException throws
     */
    public String createPolicy(PolicyElementDTO policyElementDTO, List<SubElementDTO>
            subElementDTOs, List<RuleElementDTO> ruleElementDTOs)
            throws EntitlementPolicyCreationException {

        Element policyElement = null;
        String ruleElementOrder = null;
        try {
            Document doc = createNewDocument();
            if(doc != null) {
                if (policyElementDTO != null) {
                    policyElement = PolicyCreatorUtil.createPolicyElement(policyElementDTO, doc);
                    doc.appendChild(policyElement);
                    ruleElementOrder = policyElementDTO.getRuleElementOrder();
                }

                if(policyElementDTO != null){                    
                    if(subElementDTOs != null && subElementDTOs.size() > 0) {
                        policyElement.appendChild(PolicyCreatorUtil.
                                createTargetElement(subElementDTOs, doc));
                    } else if(ruleElementDTOs != null && ruleElementDTOs.size() > 0){
                            policyElement.appendChild(doc.createElement(EntitlementPolicyConstants.
                                    TARGET_ELEMENT));
                    }

                    if(ruleElementDTOs != null && ruleElementDTOs.size() > 0) {
                        if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
                            String[] ruleIds = ruleElementOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                            for(String ruleId : ruleIds){
                                for(RuleElementDTO ruleElementDTO : ruleElementDTOs) {
                                    if(ruleId.trim().equals(ruleElementDTO.getRuleId())){
                                        policyElement.appendChild(PolicyCreatorUtil.
                                                    createRuleElement(ruleElementDTO, doc));
                                    }
                                }
                            }
                        } else {
                            for(RuleElementDTO ruleElementDTO : ruleElementDTOs) {
                                policyElement.appendChild(PolicyCreatorUtil.
                                                createRuleElement(ruleElementDTO, doc));
                            }
                        }
                    }                  
                }

                return PolicyCreatorUtil.getStringFromDocument(doc);
            }
        } catch (EntitlementPolicyCreationException e) {
            throw new EntitlementPolicyCreationException("Error While Creating XACML Policy", e);    
        }

        return null;
    }


    /**
     * Create XACML policy using the data received from basic policy wizard
     * @param policyElementDTO  policy element
     * @param ruleDTOs  rule elements
     * @param targetDTO  target element
     * @return  String object of the XACML policy
     * @throws EntitlementPolicyCreationException throws
     */
    public String createBasicPolicy(PolicyElementDTO policyElementDTO, List<RuleDTO> ruleDTOs,
                                    BasicTargetDTO targetDTO) throws EntitlementPolicyCreationException {

        Element policyElement = null;
        String ruleElementOrder = null;
        try {
            Document doc = createNewDocument();
            if(doc != null) {
                if (policyElementDTO != null) {
                    policyElement = PolicyCreatorUtil.createPolicyElement(policyElementDTO, doc);
                    doc.appendChild(policyElement);
                    ruleElementOrder = policyElementDTO.getRuleElementOrder();
                }

                if(policyElement != null) {

                    if(targetDTO != null && targetDTO.getRowDTOList() != null &&
                                                            targetDTO.getRowDTOList().size() > 0){
                        policyElement.appendChild(PolicyEditorUtil.createTarget(targetDTO, doc));
                    } else {
                        policyElement.appendChild(doc.createElement(EntitlementPolicyConstants.
                                TARGET_ELEMENT));
                    }
                    
                    if(ruleDTOs != null && ruleDTOs.size() > 0) {
                        if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
                            String[] ruleIds = ruleElementOrder.
                                    split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                            for(String ruleId : ruleIds){
                                for(RuleDTO ruleDTO : ruleDTOs) {
                                    if(ruleId.trim().equals(ruleDTO.getRuleId())){
                                        policyElement.appendChild(PolicyEditorUtil.createRule(ruleDTO, doc));
                                    }
                                }
                            }
                        } else {
                            for(RuleDTO ruleDTO : ruleDTOs) {
                                policyElement.appendChild(PolicyEditorUtil.createRule(ruleDTO, doc));
                            }
                        }
                    }
                }
                return PolicyCreatorUtil.getStringFromDocument(doc);
            }
        } catch (EntitlementPolicyCreationException e) {
            throw new EntitlementPolicyCreationException("Error While Creating XACML Policy", e);
        } catch (PolicyEditorException e) {
             throw new EntitlementPolicyCreationException("Error While Creating XACML Policy", e);
        }
        return null;
    }


    /**
     * Create policy set using the added policy ot policy sets
     * @param policySetDTO   policy set element
     * @return String object of the XACML policy Set
     * @throws EntitlementPolicyCreationException  throws
     */
    public String createPolicySet(PolicySetDTO policySetDTO)
            throws EntitlementPolicyCreationException {
        try {
            Document doc = createNewDocument();
            if(doc != null) {
                doc.appendChild(PolicyCreatorUtil.createPolicySetElement(policySetDTO, doc));
                StringBuilder policySet = new StringBuilder(PolicyCreatorUtil.getStringFromDocument(doc));
                if(policySetDTO.getPolicies() != null){
                    for(String policy : policySetDTO.getPolicies()){
                        policySet.insert(policySet.indexOf(">") + 1, policy);
                    }
                }
                return policySet.toString();
            }
        } catch (EntitlementPolicyCreationException e) {
            throw new EntitlementPolicyCreationException("Error While Creating Policy Set", e);
        }
        return null;
    }

    /**
     * Create basic XACML request
     * @param requestElementDTO  request element
     * @return String object of the XACML request
     * @throws EntitlementPolicyCreationException  throws
     */
    public String createBasicRequest(RequestElementDTO requestElementDTO)
            throws EntitlementPolicyCreationException {
        try {
            Document doc = createNewDocument();
            if(doc != null) {
                doc.appendChild(PolicyCreatorUtil.createBasicRequestElement(requestElementDTO, doc));
                return PolicyCreatorUtil.getStringFromDocument(doc);
            }
        } catch (EntitlementPolicyCreationException e) {
            throw new EntitlementPolicyCreationException("Error While Creating XACML Request", e);
        }
        return null;
    }

    /**
     * Create Document Object
     * @return Document Object
     * @throws EntitlementPolicyCreationException  throws
     */
    private Document createNewDocument() throws EntitlementPolicyCreationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new EntitlementPolicyCreationException("While creating Document Object", e);
        }

        return doc;
    }
}
