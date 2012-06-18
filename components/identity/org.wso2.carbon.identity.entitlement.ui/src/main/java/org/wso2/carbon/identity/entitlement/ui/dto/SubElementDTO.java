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
public class SubElementDTO {

    private List<MatchElementDTO> matchElementDTOs = new ArrayList<MatchElementDTO>();

    private String ruleId;

    private String elementName;

    private int elementId;

    private int matchElementCount;

    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getElementName() {
        return elementName;
    }

    public int getMatchElementCount() {
        return matchElementCount;
    }

    public void setMatchElementCount(int matchElementCount) {
        this.matchElementCount = matchElementCount;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public List<MatchElementDTO> getMatchElementDTOs() {
        return matchElementDTOs;
    }

    public void setMatchElementDTOs(MatchElementDTO matchElementDTOs) {
        this.matchElementDTOs.add(matchElementDTOs);
    }
}
