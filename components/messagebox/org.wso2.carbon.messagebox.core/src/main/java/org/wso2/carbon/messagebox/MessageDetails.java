package org.wso2.carbon.messagebox;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MessageDetails {
    private String messageId;
    private String messageBody;
    private String receivedCount;
    private String receiptHandler;
    private String defaultVisibilityTimeout;
    private String sentTimestamp;

    public String getReceiptHandler() {
        return receiptHandler;
    }

    public void setReceiptHandler(String receiptHandler) {
        this.receiptHandler = receiptHandler;
    }

    public String getDefaultVisibilityTimeout() {
        return defaultVisibilityTimeout;
    }

    public void setDefaultVisibilityTimeout(String defaultVisibilityTimeout) {
        this.defaultVisibilityTimeout = defaultVisibilityTimeout;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getReceivedCount() {
        return receivedCount;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setReceivedCount(String receivedCount) {
        this.receivedCount = receivedCount;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }
}