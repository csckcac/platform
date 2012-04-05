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
package org.wso2.carbon.agent.server.internal;

import org.wso2.carbon.agent.commons.AttributeType;

import java.util.HashMap;
import java.util.Map;

public class StreamDefinitionHolder {
    private String domainName;
    private Map<String, AttributeType[]> metaDataTypeMap = new HashMap<String, AttributeType[]>();
    private Map<String, AttributeType[]> correlationDataTypeMap = new HashMap<String, AttributeType[]>();
    private Map<String, AttributeType[]> payloadDataTypeMap = new HashMap<String, AttributeType[]>();

//    public StreamDefinitionHolder(String username) {
//        String[] userNameParts = username.split("@");
//        if (userNameParts.length == 1) {
//            this.domainName = "";
//        } else {
//            this.domainName = userNameParts[1];
//        }
//    }


    public StreamDefinitionHolder(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public Map<String, AttributeType[]> getMetaDataTypeMap() {
        return metaDataTypeMap;
    }

    public void setMetaDataType(String streamId, AttributeType[] metaDataType) {
        this.metaDataTypeMap.put(streamId, metaDataType);
    }

    public Map<String, AttributeType[]> getCorrelationDataTypeMap() {
        return correlationDataTypeMap;
    }

    public void setCorrelationDataType(String streamId, AttributeType[] correlationDataType) {
        this.correlationDataTypeMap.put(streamId, correlationDataType);
    }

    public Map<String, AttributeType[]> getPayloadDataTypeMap() {
        return payloadDataTypeMap;
    }

    public void setPayloadDataType(String streamId, AttributeType[] payloadDataType) {
        this.payloadDataTypeMap.put(streamId, payloadDataType);
    }

    public AttributeType[] getMetaDataType(String streamId) {
        return metaDataTypeMap.get(streamId);
    }

    public void addMetaDataType(String streamId, AttributeType[] metaDataType) {
        this.metaDataTypeMap.put(streamId, metaDataType);
    }

    public AttributeType[] getCorrelationDataType(String streamId) {
        return correlationDataTypeMap.get(streamId);
    }

    public void addCorrelationDataType(String streamId, AttributeType[] correlationDataType) {
        this.correlationDataTypeMap.put(streamId, correlationDataType);
    }

    public AttributeType[] getPayloadDataType(String streamId) {
        return payloadDataTypeMap.get(streamId);
    }

    public void addPayloadDataType(String streamId, AttributeType[] payloadDataType) {
        this.payloadDataTypeMap.put(streamId, payloadDataType);
    }
}
