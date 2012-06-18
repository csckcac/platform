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
package org.wso2.carbon.identity.entitlement.dto;

import java.util.Arrays;

/**
 * This class encapsulate the XACML policy related the data
 */
public class PolicyDTO {

    private String policy;

    private String policyId;
    
    private boolean active;

    private String policyType;

    private String policyEditor;

    private String[] basicPolicyEditorMetaData = new String[0];

    private boolean policyEditable;

    private boolean policyCanDelete;

    private int policyOrder;

    private String neighborId;    

    private AttributeValueDTO[] policyMetaData = new AttributeValueDTO[0];

    private String[] policySetIdReferences = new String[0];

    private String[] policyIdReferences = new String[0];

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getPolicyEditor() {
        return policyEditor;
    }

    public void setPolicyEditor(String policyEditor) {
        this.policyEditor = policyEditor;
    }

    public String[] getBasicPolicyEditorMetaData() {
        return Arrays.copyOf(basicPolicyEditorMetaData, basicPolicyEditorMetaData.length);
    }

    public void setBasicPolicyEditorMetaData(String[] basicPolicyEditorMetaData) {
        this.basicPolicyEditorMetaData = Arrays.copyOf(basicPolicyEditorMetaData,
                                                                basicPolicyEditorMetaData.length);
    }

    public AttributeValueDTO[] getPolicyMetaData() {
        return Arrays.copyOf(policyMetaData, policyMetaData.length);
    }

    public void setPolicyMetaData(AttributeValueDTO[] policyMetaData) {
        this.policyMetaData = Arrays.copyOf(policyMetaData, policyMetaData.length);
    }

    public boolean isPolicyEditable() {
        return policyEditable;
    }

    public void setPolicyEditable(boolean policyEditable) {
        this.policyEditable = policyEditable;
    }

    public boolean isPolicyCanDelete() {
        return policyCanDelete;
    }

    public void setPolicyCanDelete(boolean policyCanDelete) {
        this.policyCanDelete = policyCanDelete;
    }

    public int getPolicyOrder() {
        return policyOrder;
    }

    public void setPolicyOrder(int policyOrder) {
        this.policyOrder = policyOrder;
    }

    public String getNeighborId() {
        return neighborId;
    }

    public void setNeighborId(String neighborId) {
        this.neighborId = neighborId;
    }

    public String[] getPolicySetIdReferences() {
        return Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public void setPolicySetIdReferences(String[] policySetIdReferences) {
        this.policySetIdReferences = Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public String[] getPolicyIdReferences() {
        return Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public void setPolicyIdReferences(String[] policyIdReferences) {
        this.policyIdReferences = Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }
}
