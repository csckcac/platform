/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.common.dataobjects.activity;


/*
* Message Data class
*/
public class MessageDO {

    private String messageId; // message's ID
    private String ipAddress;
    private String userAgent;
    private int messageKeyId; // <-- primaryKey
    private String timestamp;
    // we need these variables when we store the dataobjects @ DB.
    private int operationId;
    private int activityKeyId;
    private int serverId;
    private int serviceId;

    public MessageDO() {
        this.messageKeyId = -1;
        this.operationId = -1;
        this.activityKeyId = -1;
        this.serverId = -1;
        this.serviceId = -1;
    }

    public MessageDO(String messageId, String timeStamp, String ipAddress, String userAgent) {

        this.messageId = messageId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timeStamp;
    }

    public int getMessageKeyId() {
        return this.messageKeyId;
    }

    public void setMessageKeyId(int messageKeyId) {
        this.messageKeyId = messageKeyId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTimestamp() {
        return timestamp;

    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getOperationId() {
        return this.operationId;
    }

    public int getActivityKeyId() {
        return activityKeyId;
    }

    public void setActivityKeyId(int activityKeyId) {
        this.activityKeyId = activityKeyId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServerId() {
        return this.serverId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getServiceId() {
        return this.serviceId;
    }

}
