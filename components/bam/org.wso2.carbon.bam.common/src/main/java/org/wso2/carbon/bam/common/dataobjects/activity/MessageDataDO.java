/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.common.dataobjects.activity;

/*
* This is a data class for  BAM_Message_data
*/
public class MessageDataDO {
    private String ipAddress;
    private int messageDataKeyId; // <-- primaryKey
    private String timestamp;
    private String messageDirection;
    private String messageBody;
    private String requestMessageStatus;
    private String responseMessageStatus;

    public String getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(String msgStatus) {
        this.msgStatus = msgStatus;
    }

    private String msgStatus;
    // we need these variables when we store the data @ DB.
    private int operationId;
    private int activityKeyId;
    private int serverId;
    private int serviceId;
    private int messageKeyId;

    public MessageDataDO() {
        this.messageDataKeyId = -1;
        this.messageKeyId = -1;
        this.operationId = -1;
        this.activityKeyId = -1;
        this.serverId = -1;
        this.serviceId = -1;
    }

    public MessageDataDO(String timeStamp, String ipAddress, String messageDirection, String messageBody,
                         String requestMessageStatus, String responseMessageStatus) {

        this.ipAddress = ipAddress;
        this.messageDirection = messageDirection;
        this.messageBody = messageBody;
        this.timestamp = timeStamp;
        this.requestMessageStatus = requestMessageStatus;
        this.responseMessageStatus = responseMessageStatus;
    }

    public String getRequestMessageStatus() {
        return requestMessageStatus;
    }

    public void setRequestMessageStatus(String requestMessageStatus) {
        this.requestMessageStatus = requestMessageStatus;
    }

    public String getResponseMessageStatus() {
        return responseMessageStatus;
    }

    public void setResponseMessageStatus(String responseMessageStatus) {
        this.responseMessageStatus = responseMessageStatus;
    }

    public int getMessageDataKeyId() {
        return this.messageDataKeyId;
    }

    public void setMessageDataKeyId(int messageDataKeyId) {
        this.messageDataKeyId = messageDataKeyId;
    }

    public int getMessageKeyId() {
        return this.messageKeyId;
    }

    public void setMessageKeyId(int messageKeyId) {
        this.messageKeyId = messageKeyId;
    }

    public String getMessageDirection() {
        return messageDirection;
    }

    public void setMessageDirection(String messageDirection) {
        this.messageDirection = messageDirection;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
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
