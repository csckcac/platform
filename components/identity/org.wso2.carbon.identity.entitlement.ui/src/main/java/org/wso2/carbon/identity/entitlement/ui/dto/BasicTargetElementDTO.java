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

import java.util.Arrays;

/**
 *
 */
public class BasicTargetElementDTO {

    private String resourceList;

    private String actionList;

    private String subjectList;

    private String environmentList;

    private String userAttributeValue;

    private String resourceDataType;

    private String actionDataType;

    private String subjectDataType;

    private String environmentDataType;

    private String userAttributeValueDataType;

    private String resourceId;

    private String actionId;

    private String subjectId;

    private String environmentId;

    private String attributeId;

    private String functionOnActions;

    private String functionOnSubjects;

    private String functionOnResources;

    private String functionOnEnvironment;

    private String functionOnAttributes;

    private String subjectType;

    private String[] ruleElementsOrder;

    public String getFunctionOnAttributes() {
        return functionOnAttributes;
    }

    public void setFunctionOnAttributes(String functionOnAttributes) {
        this.functionOnAttributes = functionOnAttributes;
    }

    public String getActionList() {
        return actionList;
    }

    public void setActionList(String actionList) {
        this.actionList = actionList;
    }

    public String getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(String subjectList) {
        this.subjectList = subjectList;
    }

    public String getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(String environmentList) {
        this.environmentList = environmentList;
    }

    public String getUserAttributeValue() {
        return userAttributeValue;
    }

    public void setUserAttributeValue(String userAttributeValue) {
        this.userAttributeValue = userAttributeValue;
    }

    public String getResourceDataType() {
        return resourceDataType;
    }

    public void setResourceDataType(String resourceDataType) {
        this.resourceDataType = resourceDataType;
    }

    public String getActionDataType() {
        return actionDataType;
    }

    public void setActionDataType(String actionDataType) {
        this.actionDataType = actionDataType;
    }

    public String getSubjectDataType() {
        return subjectDataType;
    }

    public void setSubjectDataType(String subjectDataType) {
        this.subjectDataType = subjectDataType;
    }

    public String getEnvironmentDataType() {
        return environmentDataType;
    }

    public void setEnvironmentDataType(String environmentDataType) {
        this.environmentDataType = environmentDataType;
    }

    public String getUserAttributeValueDataType() {
        return userAttributeValueDataType;
    }

    public void setUserAttributeValueDataType(String userAttributeValueDataType) {
        this.userAttributeValueDataType = userAttributeValueDataType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getFunctionOnActions() {
        return functionOnActions;
    }

    public void setFunctionOnActions(String functionOnActions) {
        this.functionOnActions = functionOnActions;
    }

    public String getFunctionOnSubjects() {
        return functionOnSubjects;
    }

    public void setFunctionOnSubjects(String functionOnSubjects) {
        this.functionOnSubjects = functionOnSubjects;
    }

    public String getFunctionOnResources() {
        return functionOnResources;
    }

    public void setFunctionOnResources(String functionOnResources) {
        this.functionOnResources = functionOnResources;
    }

    public String getFunctionOnEnvironment() {
        return functionOnEnvironment;
    }

    public void setFunctionOnEnvironment(String functionOnEnvironment) {
        this.functionOnEnvironment = functionOnEnvironment;
    }

    public String getResourceList() {
        return resourceList;
    }

    public void setResourceList(String resourceList) {
        this.resourceList = resourceList;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String[] getRuleElementsOrder() {
        return Arrays.copyOf(ruleElementsOrder, ruleElementsOrder.length);
    }

    public void setRuleElementsOrder(String[] ruleElementsOrder) {
        this.ruleElementsOrder = Arrays.copyOf(ruleElementsOrder, ruleElementsOrder.length);
    }
}