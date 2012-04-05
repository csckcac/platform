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

package org.wso2.carbon.bam.data.publisher.activity.mediation;

import org.wso2.carbon.bam.data.publisher.activity.mediation.config.XPathConfigData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class acts like a Java ben which encapsulates all the data related to a single
 * BAM activity event. An activity contains a unique ID, a related message ID, runtime
 * meta data and zero or more properties. An activity will always have the technical_failure,
 * application_failure and arc_key properties.
 */
public class MessageActivity {

    private String service;
    private String operation;
    private String activityId;
    private String messageId;
    private int direction = ActivityPublisherConstants.DIRECTION_IN_OUT;
    private BAMCalendar timestamp;
    private String senderHost;
    private String receiverHost;
    private String activityName;
    private String description;
    private String payload;
    private String userAgent;
    private int requestStatus = -1;
    private int responseStatus = -1;

    private Map<String, String> properties = new HashMap<String, String>();
    private Map<XPathConfigData, String> xpaths = new HashMap<XPathConfigData, String>();

    public MessageActivity() {
        properties.put(ActivityPublisherConstants.PROP_ARC_KEY,
                       ActivityPublisherConstants.EMPTY_STRING);
        properties.put(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE,
                       ActivityPublisherConstants.EMPTY_STRING);
        properties.put(ActivityPublisherConstants.PROP_APPLICATION_FAILURE,
                       ActivityPublisherConstants.EMPTY_STRING);
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setTimestamp(BAMCalendar timestamp) {
        this.timestamp = timestamp;
    }

    public void setSenderHost(String senderHost) {
        this.senderHost = senderHost;
    }

    public void setReceiverHost(String receiverHost) {
        this.receiverHost = receiverHost;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public void setXpath(XPathConfigData xPathConfigData, String value) {
        xpaths.put(xPathConfigData, value);
    }

    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }


    // ------------------- GETTERS ------------------- //

    public String getService() {
        return service;
    }

    public String getOperation() {
        return operation;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getDirection() {
        return direction;
    }

    public BAMCalendar getTimestamp() {
        return timestamp;
    }

    public String getSenderHost() {
        return senderHost;
    }

    public String getReceiverHost() {
        return receiverHost;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getDescription() {
        return description;
    }

    public String getPayload() {
        return payload;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    public String getXpath(XPathConfigData key) {
        return xpaths.get(key);
    }

    public Set<XPathConfigData> getXpathKeys() {
        return xpaths.keySet();
    }

    public int getRequestStatus() {
        return requestStatus;
    }

    public int getResponseStatus() {
        return responseStatus;
    }
}