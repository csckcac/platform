/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.activity.mediation.data.publisher.data;


import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherConstants;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MessageActivity {


    private String service;
    private String operation;
    private String activityId;
    private String messageId;
    private String direction = ActivityPublisherConstants.DIRECTION_IN_OUT;
    private Timestamp timestamp;
    private String senderHost;
    private String receiverHost;
    private String activityName;
    private String description;
    private String payload;
    private String soapEnvelopNamespaceURI;
    private int tenantId;
    private int requestStatus = -1;
    private int responseStatus = -1;

    private Map<String, String> properties = new HashMap<String, String>();

    public String getSoapEnvelopNamespaceURI() {
        return soapEnvelopNamespaceURI;
    }

    public void setSoapEnvelopNamespaceURI(String soapEnvelopNamespaceURI) {
        this.soapEnvelopNamespaceURI = soapEnvelopNamespaceURI;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Map<String, String> getProperty() {
        return properties;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderHost() {
        return senderHost;
    }

    public void setSenderHost(String senderHost) {
        this.senderHost = senderHost;
    }

    public String getReceiverHost() {
        return receiverHost;
    }

    public void setReceiverHost(String receiverHost) {
        this.receiverHost = receiverHost;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

}
