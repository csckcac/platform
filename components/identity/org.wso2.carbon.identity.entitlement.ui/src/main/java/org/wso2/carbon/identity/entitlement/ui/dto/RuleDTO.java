/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RuleDTO {


    private String ruleId;

    private String ruleEffect;

    private String ruleDescription;

    private BasicTargetDTO targetDTO;

    private List<RowDTO> rowDTOList = new ArrayList<RowDTO>();

    private boolean completedRule;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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

    public List<RowDTO> getRowDTOList() {
        return rowDTOList;
    }

    public void setRowDTOList(List<RowDTO> rowDTOList) {
        this.rowDTOList = rowDTOList;
    }

    public void addRowDTO(RowDTO rowDTO) {
        this.rowDTOList.add(rowDTO);
    }

    public BasicTargetDTO getTargetDTO() {
        return targetDTO;
    }

    public void setTargetDTO(BasicTargetDTO targetDTO) {
        this.targetDTO = targetDTO;
    }

    public boolean isCompletedRule() {
        return completedRule;
    }

    public void setCompletedRule(boolean completedRule) {
        this.completedRule = completedRule;
    }
}
