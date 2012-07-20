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
import org.wso2.carbon.identity.entitlement.stub.dto.AttributeValueTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.*;
import java.util.*;

/**
 * This Bean is used to keep the user data temporary while travelling through
 * the UI wizard
 */
public class EntitlementPolicyBean {

	private String policyName;

	private String algorithmName;

	private String policyDescription;

	private String currentRuleId;

	private int subElementNumber;

	private int currentSubElementNumber;

	private int matchElementNumber;

	private int applyElementNumber = 2;

	private int attributeValueElementNumber;

	private int attributeDesignatorElementNumber;

	private int attributeSelectorElementNumber;

	private int currentPageNumber;

	private String currentPageId;

	private String userInputData;

	private String subjectType;

	private Map<String, String> subjectTypeMap = new HashMap<String, String>();

	private Map<String, String> categoryMap = new HashMap<String, String>();

	private Map<String, String> targetFunctionMap = new HashMap<String, String>();

	private Map<String, String> ruleFunctionMap = new HashMap<String, String>();

	private boolean editPolicy;

	private String[] ruleCombiningAlgorithms = new String[0];

    private String[] policyCombiningAlgorithms = new String[0];

	private String[] functionIds = new String[0];

	private String[] matchIds = new String[0];

	private String[] mustBePresentValues = new String[0];

	private String[] ruleEffectValues = new String[0];

	private String[] dataTypes = new String[0];

	private ConditionElementDT0 conditionElementDT0 = new ConditionElementDT0();

	private BasicRequestDTO basicRequestDTO = null;

	private BasicTargetElementDTO basicTargetElementDTO = null;

    private BasicTargetDTO targetDTO = null;

    private PolicySetDTO policySetDTO = null;

	public Map<String, String> functionIdMap = new HashMap<String, String>();

	public Map<String, String> functionIdElementValueMap = new HashMap<String, String>();

	private List<MatchElementDTO> matchElementDTOs = new ArrayList<MatchElementDTO>();

	private List<ApplyElementDTO> applyElementDTOs = new ArrayList<ApplyElementDTO>();

	private List<RuleElementDTO> ruleElementDTOs = new ArrayList<RuleElementDTO>();

	private List<BasicRuleElementDTO> basicRuleElementDTOs = new ArrayList<BasicRuleElementDTO>();

	private List<RuleDTO> ruleDTOs = new ArrayList<RuleDTO>();

	private List<SubElementDTO> subElementDTOs = new ArrayList<SubElementDTO>();

	private List<AttributeValueElementDTO> attributeValueElementDTOs = new ArrayList<AttributeValueElementDTO>();

	private List<AttributeDesignatorDTO> attributeDesignatorDTOs = new ArrayList<AttributeDesignatorDTO>();

	private List<AttributeSelectorDTO> attributeSelectorDTOs = new ArrayList<AttributeSelectorDTO>();

    private Map<String, Set<AttributeValueTreeNodeDTO>> attributeValueNodeMap =
                                            new HashMap<String, Set<AttributeValueTreeNodeDTO>>();

    private String ruleElementOrder;

    private String ruleEffect;

    private String ruleDescription;

	private Set<String> preFunctions = new HashSet<String>();

    private Map<String, Set<String>> defaultAttributeIdMap =
                                            new HashMap<String, Set<String>>();

    private Map<String, Set<String>> defaultDataTypeMap =
                                            new HashMap<String, Set<String>>();
	/**
	 * This method is temporally used to clear the entitlement bean. Need to
	 * update with a method proper implementation TODO
	 */
	public void cleanEntitlementPolicyBean() {

		policyName = null;

		algorithmName = null;

		policyDescription = null;

		currentRuleId = null;

		subElementNumber = 0;

		currentSubElementNumber = 0;

		matchElementNumber = 0;

		attributeValueElementNumber = 0;

		attributeDesignatorElementNumber = 0;

		attributeSelectorElementNumber = 0;

		currentPageNumber = 0;

		userInputData = null;

		editPolicy = false;

		conditionElementDT0 = null;

        policySetDTO = null;

		functionIdMap.clear();

		functionIdElementValueMap.clear();

		matchElementDTOs.clear();

		applyElementDTOs.clear();

		attributeValueElementDTOs.clear();

		attributeDesignatorDTOs.clear();

		attributeSelectorDTOs.clear();

		subElementDTOs.clear();

		basicRuleElementDTOs.clear();

		ruleElementDTOs.clear();

		removeBasicTargetElementDTO();

		subjectTypeMap.clear();

        ruleEffect = null;

        ruleDescription = null;

        targetDTO = null;

        ruleDTOs.clear();
        
	}

	public String[] getRuleCombiningAlgorithms() {
		return Arrays.copyOf(ruleCombiningAlgorithms,
				ruleCombiningAlgorithms.length);
	}

	public void setRuleCombiningAlgorithms(String[] ruleCombiningAlgorithms) {
		this.ruleCombiningAlgorithms = Arrays.copyOf(ruleCombiningAlgorithms, ruleCombiningAlgorithms.length);
	}

	public String[] getFunctionIds() {
		return Arrays.copyOf(functionIds, functionIds.length);
	}

	public void setFunctionIds(String[] functionIds) {
		this.functionIds = Arrays.copyOf(functionIds, functionIds.length);
	}

	public String[] getMatchIds() {
		return Arrays.copyOf(matchIds, matchIds.length);
	}

	public void setMatchIds(String[] matchIds) {
		this.matchIds = Arrays.copyOf(matchIds, matchIds.length);
	}

	public String[] getMustBePresentValues() {
		return Arrays.copyOf(mustBePresentValues, mustBePresentValues.length);
	}

	public void setMustBePresentValues(String[] mustBePresentValues) {
		this.mustBePresentValues = Arrays.copyOf(mustBePresentValues, mustBePresentValues.length);
	}

	public String[] getRuleEffectValues() {
		return Arrays.copyOf(ruleEffectValues, ruleEffectValues.length);
	}

	public void setRuleEffectValues(String[] ruleEffectValues) {
		this.ruleEffectValues = Arrays.copyOf(ruleEffectValues, ruleEffectValues.length);
	}

	public String[] getDataTypes() {
		return Arrays.copyOf(dataTypes, dataTypes.length);
	}

	public void setDataTypes(String[] dataTypes) {
		this.dataTypes = Arrays.copyOf(dataTypes, dataTypes.length);
	}

	public int getCurrentPageNumber() {
		return currentPageNumber++;
	}

	public void setCurrentPageNumber(int currentPageNumber) {
		this.currentPageNumber = currentPageNumber;
	}

	public int getAttributeDesignatorElementNumber() {
		return attributeDesignatorElementNumber++;
	}

	public String getCurrentPageId() {
		return currentPageId;
	}

	public void setCurrentPageId(String currentPageId) {
		this.currentPageId = currentPageId;
	}

	public void setAttributeDesignatorElementNumber(
			int attributeDesignatorElementNumber) {
		this.attributeDesignatorElementNumber = attributeDesignatorElementNumber;
	}

	public int getAttributeSelectorElementNumber() {
		return attributeSelectorElementNumber++;
	}

	public void setAttributeSelectorElementNumber(
			int attributeSelectorElementNumber) {
		this.attributeSelectorElementNumber = attributeSelectorElementNumber;
	}

	public List<ApplyElementDTO> getApplyElementDTOs(String applyElementIdPreFix) {
		List<ApplyElementDTO> applyElementDTOList = new ArrayList<ApplyElementDTO>();
		if (applyElementDTOs.size() > 0) {
			for (ApplyElementDTO applyElementDTO : applyElementDTOs) {
				if (applyElementDTO.getApplyElementId().startsWith(
						applyElementIdPreFix)) {
					String applyElementId = applyElementDTO.getApplyElementId();
					if (applyElementId.split(applyElementIdPreFix).length > 1) {
						if (applyElementId.split(applyElementIdPreFix)[1].trim().length() > 0) {
							applyElementId = (applyElementId
									.split(applyElementIdPreFix)[1])
									.substring(1);
							if (!applyElementId.contains("/")) {
								applyElementDTOList.add(applyElementDTO);
							}
						}
					}
				}
			}
		}
		return applyElementDTOList;
	}

	public List<ApplyElementDTO> getApplyElementDTOs() {
		return applyElementDTOs;
	}

	public ApplyElementDTO getApplyElementDTO(String applyElementId) {
		if (applyElementDTOs.size() > 0) {
			for (ApplyElementDTO applyElementDTO : applyElementDTOs) {
				if (applyElementDTO.getApplyElementId().equals(applyElementId)) {
					return applyElementDTO;
				}
			}
		}
		return null;
	}

	public void setApplyElementDTOs(List<ApplyElementDTO> applyElementDTOs) {
		for (ApplyElementDTO applyElementDTO : applyElementDTOs) {
			setApplyElementDTO(applyElementDTO);
		}
	}

	public void setApplyElementDTO(ApplyElementDTO applyElementDTO) {
		if (!isExistingApplyElement(applyElementDTO.getApplyElementId())) {
			this.applyElementDTOs.remove(applyElementDTO);
		}
		this.applyElementDTOs.add(applyElementDTO);
	}

	public BasicRequestDTO getBasicRequestDTO() {
		return basicRequestDTO;
	}

	public void setBasicRequestDTO(BasicRequestDTO basicRequestDTO) {
		this.basicRequestDTO = basicRequestDTO;
	}

	public boolean removeApplyElementDTOByAddApplyElementPageNumber(
			int addApplyElementNumber) {
		if (applyElementDTOs.size() > 0) {
			Iterator iterator = applyElementDTOs.listIterator();
			while (iterator.hasNext()) {
				ApplyElementDTO applyElementDTO = (ApplyElementDTO) iterator
						.next();
				if (applyElementDTO.getAddApplyElementPageNumber() == addApplyElementNumber) {
					iterator.remove();
				}
			}
		}
		return false;
	}

	public boolean removeApplyElementDTOByApplyElementNumber(
			String applyElementId) {
		if (applyElementDTOs.size() > 0) {
			Iterator iterator = applyElementDTOs.listIterator();
			while (iterator.hasNext()) {
				ApplyElementDTO applyElementDTO = (ApplyElementDTO) iterator
						.next();
				if (applyElementDTO.getApplyElementId().equals(applyElementId)) {
					iterator.remove();
				}
			}
		}
		return false;
	}

	public boolean removeApplyElementDTOByApplyElementNumber(
			int applyElementNumber) {
		if (applyElementDTOs.size() > 0) {
			Iterator iterator = applyElementDTOs.listIterator();
			while (iterator.hasNext()) {
				ApplyElementDTO applyElementDTO = (ApplyElementDTO) iterator
						.next();
				if (applyElementDTO.getApplyElementNumber() == applyElementNumber) {
					iterator.remove();
				}
			}
		}
		return false;
	}

	public void removeApplyElementDTOs() {
		if (applyElementDTOs.size() > 0) {
			Iterator iterator = applyElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public boolean isExistingApplyElement(String applyElementId) {
		if (applyElementDTOs.size() > 0) {
			for (ApplyElementDTO applyElementDTO : applyElementDTOs) {
				if (applyElementDTO.getApplyElementId().equals(applyElementId)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getAttributeValueElementNumber() {
		return attributeValueElementNumber++;
	}

	public void setAttributeValueElementNumber(int attributeValueElementNumber) {
		this.attributeValueElementNumber = attributeValueElementNumber;
	}

	public List<AttributeValueElementDTO> getAttributeValueElementDTOs() {
		return attributeValueElementDTOs;
	}

	public List<AttributeValueElementDTO> getAttributeValueElementDTOs(
			String applyElementId) {
		List<AttributeValueElementDTO> attributeValueElementDTOList = new ArrayList<AttributeValueElementDTO>();
		if (attributeValueElementDTOs.size() > 0) {
			for (AttributeValueElementDTO attributeValueElementDTO : attributeValueElementDTOs) {
				if (attributeValueElementDTO.getApplyElementId().equals(
						applyElementId)) {
					attributeValueElementDTOList.add(attributeValueElementDTO);
				}
			}
		}

		return attributeValueElementDTOList;
	}

	public void setAttributeValueElementDTOs(
			AttributeValueElementDTO attributeValueElementDTO) {
		if (!isExistingAttributeValueElementDTOs(attributeValueElementDTO)) {
			this.attributeValueElementDTOs.remove(attributeValueElementDTO);
		}
		this.attributeValueElementDTOs.add(attributeValueElementDTO);
	}

	public boolean isExistingAttributeValueElementDTOs(
			AttributeValueElementDTO valueElementDTO) {
		if (attributeValueElementDTOs.size() > 0) {
			Iterator iterator = attributeValueElementDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeValueElementDTO attributeValueElementDTO = (AttributeValueElementDTO) iterator
						.next();
				if (attributeValueElementDTO.getElementId() == valueElementDTO
						.getElementId()) {
					return true;
				}
			}
		}
		return false;
	}

	public AttributeValueElementDTO getAttributeValueElement(
			int attributeValueElementId) {
		if (attributeValueElementDTOs.size() > 0) {
			for (AttributeValueElementDTO attributeValueElementDTO : attributeValueElementDTOs) {
				if (attributeValueElementDTO.getElementId() == attributeValueElementId) {
					return attributeValueElementDTO;
				}
			}
		}
		return null;
	}

	public void removeAttributeValueElement(int attributeValueElementId) {
		if (attributeValueElementDTOs.size() > 0) {
			Iterator iterator = attributeValueElementDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeValueElementDTO attributeValueElementDTO = (AttributeValueElementDTO) iterator
						.next();
				if (attributeValueElementDTO.getElementId() == attributeValueElementId) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAttributeValueElementByApplyElementNumber(
			String applyElementId) {
		if (attributeValueElementDTOs.size() > 0) {
			Iterator iterator = attributeValueElementDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeValueElementDTO attributeValueElementDTO = (AttributeValueElementDTO) iterator
						.next();
				if (attributeValueElementDTO.getApplyElementId().equals(
						applyElementId)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAttributeValueElements() {
		if (attributeValueElementDTOs.size() > 0) {
			Iterator iterator = attributeValueElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public List<AttributeDesignatorDTO> getAttributeDesignatorDTOs() {
		return attributeDesignatorDTOs;
	}

	public List<AttributeDesignatorDTO> getAttributeDesignatorDTOs(
			String applyElementId) {
		List<AttributeDesignatorDTO> attributeDesignatorDTOList = new ArrayList<AttributeDesignatorDTO>();
		if (attributeDesignatorDTOs.size() > 0) {
			for (AttributeDesignatorDTO attributeDesignatorDTO : attributeDesignatorDTOs) {
				if (attributeDesignatorDTO.getApplyElementId().equals(
						applyElementId)) {
					attributeDesignatorDTOList.add(attributeDesignatorDTO);
				}
			}
		}

		return attributeDesignatorDTOList;
	}

	public void setAttributeDesignatorDTOs(
			AttributeDesignatorDTO attributeDesignatorDTO) {
		if (isExistingAttributeDesignatorDTO(attributeDesignatorDTO)) {
			attributeDesignatorDTOs.remove(attributeDesignatorDTO);
		}
		this.attributeDesignatorDTOs.add(attributeDesignatorDTO);
	}

	public boolean isExistingAttributeDesignatorDTO(
			AttributeDesignatorDTO attributeDesignatorDTO) {
		if (attributeDesignatorDTOs.size() > 0) {
			Iterator iterator = attributeDesignatorDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeDesignatorDTO designatorDTO = (AttributeDesignatorDTO) iterator
						.next();
				if (attributeDesignatorDTO.getElementId() == designatorDTO
						.getElementId()) {
					return true;
				}
			}
		}
		return false;
	}

	public AttributeDesignatorDTO getAttributeDesignatorElement(
			int attributeDesignatorElementId) {
		if (attributeDesignatorDTOs.size() > 0) {
			for (AttributeDesignatorDTO attributeDesignatorDTO : attributeDesignatorDTOs) {
				if (attributeDesignatorDTO.getElementId() == attributeDesignatorElementId) {
					return attributeDesignatorDTO;
				}
			}
		}
		return null;
	}

	public void removeAttributeDesignatorElement(
			int attributeDesignatorElementId) {
		if (attributeDesignatorDTOs.size() > 0) {
			Iterator iterator = attributeDesignatorDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeDesignatorDTO attributeDesignatorDTO = (AttributeDesignatorDTO) iterator
						.next();
				if (attributeDesignatorDTO.getElementId() == attributeDesignatorElementId) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAttributeDesignatorElementByApplyElementNumber(
			String applyElementId) {
		if (attributeDesignatorDTOs.size() > 0) {
			Iterator iterator = attributeDesignatorDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeDesignatorDTO attributeDesignatorDTO = (AttributeDesignatorDTO) iterator
						.next();
				if (attributeDesignatorDTO.getApplyElementId().equals(
						applyElementId)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAttributeDesignatorElements() {
		if (attributeDesignatorDTOs.size() > 0) {
			Iterator iterator = attributeDesignatorDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public int getCurrentSubElementNumber() {
		return currentSubElementNumber;
	}

	public void setCurrentSubElementNumber(int currentSubElementNumber) {
		this.currentSubElementNumber = currentSubElementNumber;
	}

	public int getSubElementNumber() {
		return subElementNumber;
	}

	public void setSubElementNumber(int subElementNumber) {
		this.subElementNumber = subElementNumber;
	}

	public int getMatchElementNumber() {
		matchElementNumber++;
		return matchElementNumber - 1;
	}

	public void setMatchElementNumber(int matchElementNumber) {
		this.matchElementNumber = matchElementNumber;
	}

	public List<SubElementDTO> getTargetElementDTOs() {
		return subElementDTOs;
	}

	public void setTargetElementDTOs(List<SubElementDTO> subElementDTOs) {
		this.subElementDTOs = subElementDTOs;
	}

	public void setTargetElementDTO(SubElementDTO subElementDTO) {
		this.subElementDTOs.add(subElementDTO);
	}

	public SubElementDTO getTargetElementDTO(int targetElementId) {
		if (subElementDTOs.size() > 0) {
			for (SubElementDTO subElementDTO : subElementDTOs) {
				if (subElementDTO.getElementId() == targetElementId) {
					return subElementDTO;
				}
			}
		}
		return null;
	}

	public List<SubElementDTO> getTargetElementDTO(String ruleId) {
		List<SubElementDTO> elementDTOs = new ArrayList<SubElementDTO>();
		if (subElementDTOs.size() > 0) {
			for (SubElementDTO subElementDTO : subElementDTOs) {
				if (subElementDTO.getRuleId() != null) {
					if (subElementDTO.getRuleId().equals(ruleId)) {
						elementDTOs.add(subElementDTO);
					}
				}
			}
		}
		return elementDTOs;
	}

	public boolean removeTargetElementDTO(int targetElementId) {
		if (subElementDTOs.size() > 0) {
			for (SubElementDTO subElementDTO : subElementDTOs) {
				if (subElementDTO.getElementId() == targetElementId) {
					return subElementDTOs.remove(subElementDTO);
				}
			}
		}
		return false;
	}

	public void removeTargetElementDTOs() {
		if (subElementDTOs.size() > 0) {
			Iterator iterator = subElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public boolean isExistingTargetElementDTO(int hashCode) {
		if (subElementDTOs.size() > 0) {
			for (SubElementDTO subElementDTO : subElementDTOs) {
				if (subElementDTO.hashCode() == (hashCode)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<RuleElementDTO> getRuleElements() {
		return ruleElementDTOs;
	}

	public void setRuleElements(List<RuleElementDTO> ruleElements) {
		this.ruleElementDTOs = ruleElements;
	}

	public ConditionElementDT0 getConditionElement() {
		return conditionElementDT0;
	}

	public void setConditionElement(ConditionElementDT0 conditionElement) {
		this.conditionElementDT0 = conditionElement;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public String getPolicyDescription() {
		return policyDescription;
	}

	public void setPolicyDescription(String policyDescription) {
		this.policyDescription = policyDescription;
	}

	public String getUserInputData() {
		return userInputData;
	}

	public void setUserInputData(String userInputData) {
		this.userInputData = userInputData;
	}

	public List<MatchElementDTO> getMatchElements() {
		return matchElementDTOs;
	}

	public void setRuleElement(RuleElementDTO ruleElement) {
		this.ruleElementDTOs.add(ruleElement);
	}

	public RuleElementDTO getRuleElement(String ruleId) {
		if (ruleElementDTOs.size() > 0) {
			for (RuleElementDTO ruleElementDTO : ruleElementDTOs) {
				if (ruleElementDTO.getRuleId().equals(ruleId)) {
					return ruleElementDTO;
				}
			}
		}
		return null;
	}

	public boolean removeRuleElement(String ruleId) {
		if (ruleElementDTOs.size() > 0) {
			for (RuleElementDTO ruleElementDTO : ruleElementDTOs) {
				if (ruleElementDTO.getRuleId().equals(ruleId)) {
					return ruleElementDTOs.remove(ruleElementDTO);
				}
			}
		}
		return false;
	}

	public boolean removeRuleElement(RuleElementDTO ruleElement) {
		return ruleElementDTOs.remove(ruleElement);
	}

	public boolean isExistingRuleElement(String ruleId) {
		if (ruleElementDTOs.size() > 0) {
			for (RuleElementDTO ruleElementDTO : ruleElementDTOs) {
				if (ruleElementDTO.getRuleId().equals(ruleId)) {
					return true;
				}
			}
		}
		return false;
	}

	public void removeRuleElements() {
		if (ruleElementDTOs.size() > 0) {
			Iterator iterator = ruleElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public String getCurrentRuleId() {
		return currentRuleId;
	}

	public void setCurrentRuleId(String currentRuleId) {
		this.currentRuleId = currentRuleId;
	}

	public void setMatchElement(MatchElementDTO matchElement) {
		this.matchElementDTOs.add(matchElement);
	}

	public MatchElementDTO getMatchElement(int matchElementId) {
		if (matchElementDTOs.size() > 0) {
			for (MatchElementDTO matchElementDTO : matchElementDTOs) {
				if (matchElementDTO.getElementId() == matchElementId) {
					return matchElementDTO;
				}
			}
		}
		return null;
	}

	public boolean removeMatchElement(int matchElementId) {
		if (matchElementDTOs.size() > 0) {
			for (MatchElementDTO matchElementDTO : matchElementDTOs) {
				if (matchElementDTO.getElementId() == matchElementId) {
					return matchElementDTOs.remove(matchElementDTO);
				}
			}
		}
		return false;
	}

	public void removeMatchElements(String elementName) {
		if (matchElementDTOs.size() > 0) {
			Iterator iterator = matchElementDTOs.listIterator();
			while (iterator.hasNext()) {
				MatchElementDTO matchElementDTO = (MatchElementDTO) iterator
						.next();
				if (matchElementDTO.getMatchElementName().equals(elementName)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeMatchElements() {
		if (matchElementDTOs.size() > 0) {
			Iterator iterator = matchElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public boolean isExistingMatchElement(int matchElementId) {
		if (matchElementDTOs.size() > 0) {
			for (MatchElementDTO matchElementDTO : matchElementDTOs) {
				if (matchElementDTO.getElementId() == matchElementId) {
					return true;
				}
			}
		}
		return false;
	}

	public List<BasicRuleElementDTO> getBasicRuleElementDTOs() {
		return basicRuleElementDTOs;
	}

	public void setBasicRuleElementDTOs(List<BasicRuleElementDTO> basicRuleElementDTOs) {
		this.basicRuleElementDTOs=basicRuleElementDTOs;
	}

	public void setBasicRuleElementDTOs(BasicRuleElementDTO basicRuleElementDTO) {
		if (basicRuleElementDTOs.size() > 0) {
			Iterator iterator = basicRuleElementDTOs.listIterator();
			while (iterator.hasNext()) {
				BasicRuleElementDTO elementDTO = (BasicRuleElementDTO) iterator
						.next();
				if (elementDTO.getRuleId().equals(
						basicRuleElementDTO.getRuleId())) {
					if (elementDTO.isCompletedRule()) {
						basicRuleElementDTO.setCompletedRule(true);
					}
					iterator.remove();
				}
			}
		}
		this.basicRuleElementDTOs.add(basicRuleElementDTO);
	}

	public BasicRuleElementDTO getBasicRuleElement(String ruleId) {
		if (basicRuleElementDTOs.size() > 0) {
			for (BasicRuleElementDTO basicRuleElementDTO : basicRuleElementDTOs) {
				if (basicRuleElementDTO.getRuleId().equals(ruleId)) {
					return basicRuleElementDTO;
				}
			}
		}
		return null;
	}

	public boolean removeBasicRuleElement(String ruleId) {
		if (basicRuleElementDTOs.size() > 0) {
			for (BasicRuleElementDTO basicRuleElementDTO : basicRuleElementDTOs) {
				if (basicRuleElementDTO.getRuleId().equals(ruleId)) {
					return basicRuleElementDTOs.remove(basicRuleElementDTO);
				}
			}
		}
		return false;
	}

	public void removeBasicRuleElements() {
		if (basicRuleElementDTOs.size() > 0) {
			Iterator iterator = basicRuleElementDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}



/////////////////////////////////////// new

	public List<RuleDTO> getRuleDTOs() {
		return ruleDTOs;
	}

	public void setRuleDTOs(List<RuleDTO> ruleDTOs) {
		this.ruleDTOs = ruleDTOs;
	}

	public void setRuleDTO(RuleDTO ruleDTO) {
		if (ruleDTOs.size() > 0) {
			Iterator iterator = ruleDTOs.listIterator();
			while (iterator.hasNext()) {
				RuleDTO elementDTO = (RuleDTO) iterator.next();
				if (elementDTO.getRuleId().equals(
						ruleDTO.getRuleId())) {
					if (elementDTO.isCompletedRule()) {
						ruleDTO.setCompletedRule(true);
					}
					iterator.remove();
				}
			}
		}
		this.ruleDTOs.add(ruleDTO);
	}

	public RuleDTO getRuleDTO(String ruleId) {
		if (ruleDTOs.size() > 0) {
			for (RuleDTO ruleDTO : ruleDTOs) {
				if (ruleDTO.getRuleId().equals(ruleId)) {
					return ruleDTO;
				}
			}
		}
		return null;
	}

	public boolean removeRuleDTO(String ruleId) {
		if (ruleDTOs.size() > 0) {
			for (RuleDTO ruleDTO : ruleDTOs) {
				if (ruleDTO.getRuleId().equals(ruleId)) {
					return ruleDTOs.remove(ruleDTO);
				}
			}
		}
		return false;
	}

	public void removeRuleDTOs() {
		if (ruleDTOs.size() > 0) {
			Iterator iterator = ruleDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}


///////////////////////////    ////////
	public BasicTargetElementDTO getBasicTargetElementDTO() {
		return basicTargetElementDTO;
	}

	public void setBasicTargetElementDTO(
			BasicTargetElementDTO basicTargetElementDTO) {
		this.basicTargetElementDTO = basicTargetElementDTO;
	}

	public void removeBasicTargetElementDTO() {
		this.basicTargetElementDTO = null;
	}

	public int getApplyElementNumber() {
		return applyElementNumber++;
	}

	public void setApplyElementNumber(int applyElementNumber) {
		this.applyElementNumber = applyElementNumber;
	}

	public List<AttributeSelectorDTO> getAttributeSelectorDTOs() {
		return attributeSelectorDTOs;
	}

	public List<AttributeSelectorDTO> getAttributeSelectorDTOs(
			String applyElementId) {
		List<AttributeSelectorDTO> attributeSelectorDTOList = new ArrayList<AttributeSelectorDTO>();
		if (attributeSelectorDTOs.size() > 0) {
			for (AttributeSelectorDTO attributeSelectorDTO : attributeSelectorDTOs) {
				if (attributeSelectorDTO.getApplyElementId().equals(
						applyElementId)) {
					attributeSelectorDTOList.add(attributeSelectorDTO);
				}
			}
		}

		return attributeSelectorDTOList;
	}

	public void setAttributeSelectorDTOs(
			AttributeSelectorDTO attributeSelectorDTO) {
		if (!isExistingAttributeSelectorDTO(attributeSelectorDTO)) {
			this.attributeSelectorDTOs.remove(attributeSelectorDTO);
		}
		this.attributeSelectorDTOs.add(attributeSelectorDTO);
	}

	public boolean isExistingAttributeSelectorDTO(
			AttributeSelectorDTO attributeSelectorDTO) {
		if (attributeSelectorDTOs.size() > 0) {
			Iterator iterator = attributeSelectorDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeSelectorDTO designatorDTO = (AttributeSelectorDTO) iterator
						.next();
				if (attributeSelectorDTO.getElementNumber() == designatorDTO
						.getElementNumber()) {
					return true;
				}
			}
		}
		return false;
	}

	public AttributeSelectorDTO getAttributeSelectorElement(
			int attributeSelectorElementId) {
		if (attributeSelectorDTOs.size() > 0) {
			for (AttributeSelectorDTO attributeSelectorDTO : attributeSelectorDTOs) {
				if (attributeSelectorDTO.getElementNumber() == attributeSelectorElementId) {
					return attributeSelectorDTO;
				}
			}
		}
		return null;
	}

	public void removeAttributeSelectorElement(int attributeSelectorElementId) {
		if (attributeSelectorDTOs.size() > 0) {
			Iterator iterator = attributeSelectorDTOs.listIterator();
			while (iterator.hasNext()) {
				AttributeSelectorDTO attributeSelectorDTO = (AttributeSelectorDTO) iterator
						.next();
				if (attributeSelectorDTO.getElementNumber() == attributeSelectorElementId) {
					iterator.remove();
				}
			}
		}
	}

	public void removeAttributeSelectorElements() {
		if (attributeSelectorDTOs.size() > 0) {
			Iterator iterator = attributeSelectorDTOs.listIterator();
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}
	}

	public boolean isEditPolicy() {
		return editPolicy;
	}

	public void setEditPolicy(boolean editPolicy) {
		this.editPolicy = editPolicy;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

    public String[] getPolicyCombiningAlgorithms() {
        return Arrays.copyOf(policyCombiningAlgorithms, policyCombiningAlgorithms.length);
    }

    public void setPolicyCombiningAlgorithms(String[] policyCombiningAlgorithms) {
        this.policyCombiningAlgorithms = Arrays.copyOf(policyCombiningAlgorithms, policyCombiningAlgorithms.length);
    }

    public PolicySetDTO getPolicySetDTO() {
        return policySetDTO;
    }

    public void setPolicySetDTO(PolicySetDTO policySetDTO) {
        this.policySetDTO = policySetDTO;
    }

    public String getRuleElementOrder() {
        return ruleElementOrder;
    }

    public void setRuleElementOrder(String ruleElementOrder) {
        this.ruleElementOrder = ruleElementOrder;
    }

    public String getRuleEffect() {
        return ruleEffect;
    }

    public void setRuleEffect(String ruleEffect) {
        this.ruleEffect = ruleEffect;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public Set<AttributeValueTreeNodeDTO> getAttributeValueNodeMap(String attributeType) {
        return attributeValueNodeMap.get(attributeType);
    }

    public void putAttributeValueNodeMap(String category, AttributeValueTreeNodeDTO attributeValueNodeMap) {
        Set<AttributeValueTreeNodeDTO> dtoSet = this.attributeValueNodeMap.get(category);
        if(dtoSet != null){
            dtoSet.add(attributeValueNodeMap);
        } else {
            Set<AttributeValueTreeNodeDTO> newDtoSet = new HashSet<AttributeValueTreeNodeDTO>();
            newDtoSet.add(attributeValueNodeMap);
            this.attributeValueNodeMap.put(category, newDtoSet);
        }
    }

    public BasicTargetDTO getTargetDTO() {
        return targetDTO;
    }

    public void setTargetDTO(BasicTargetDTO targetDTO) {
        this.targetDTO = targetDTO;
    }

    public void removeTargetDTO(BasicTargetDTO targetDTO) {
        this.targetDTO = null;
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public Set<String> getCategorySet() {
        return categoryMap.keySet();
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public Map<String, String> getRuleFunctionMap() {
        return ruleFunctionMap;
    }

    public void setRuleFunctionMap(Map<String, String> ruleFunctionMap) {
        this.ruleFunctionMap = ruleFunctionMap;
    }

    public Map<String, String> getTargetFunctionMap() {
        return targetFunctionMap;
    }

    public void setTargetFunctionMap(Map<String, String> targetFunctionMap) {
        this.targetFunctionMap = targetFunctionMap;
    }

    public Set<String> getPreFunctions() {
        return preFunctions;
    }

    public void addPreFunction(String preFunction) {
        this.preFunctions.add(preFunction);
    }

    public Map<String, String> getSubjectTypeMap() {
        return subjectTypeMap;
    }

    public void setSubjectTypeMap(Map<String, String> subjectTypeMap) {
        this.subjectTypeMap = subjectTypeMap;
    }

    public Map<String, Set<String>> getDefaultDataTypeMap() {
        return defaultDataTypeMap;
    }

    public void addDefaultDataType(String category, String defaultDataType) {
        Set<String> dtoSet = this.defaultDataTypeMap.get(category);
        if(dtoSet != null){
            dtoSet.add(defaultDataType);
        } else {
            Set<String> newDtoSet = new HashSet<String>();
            newDtoSet.add(defaultDataType);
            this.defaultDataTypeMap.put(category, newDtoSet);
        }
    }

    public Map<String, Set<String>> getDefaultAttributeIdMap() {
        return defaultAttributeIdMap;
    }

    public void addDefaultAttributeId(String category, String defaultAttributeId) {
        Set<String> dtoSet = this.defaultAttributeIdMap.get(category);
        if(dtoSet != null){
            dtoSet.add(defaultAttributeId);
        } else {
            Set<String> newDtoSet = new HashSet<String>();
            newDtoSet.add(defaultAttributeId);
            this.defaultAttributeIdMap.put(category, newDtoSet);
        }
    }
}