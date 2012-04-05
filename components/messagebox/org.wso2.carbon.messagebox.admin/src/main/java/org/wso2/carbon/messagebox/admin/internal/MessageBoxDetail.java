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
package org.wso2.carbon.messagebox.admin.internal;

public class MessageBoxDetail {
    private String messageBoxName;
    private int numberOfMessages;
    private String[] sharedUsers;
    private String owner;
    private String messageBoxId;
    private String visibilityTimeout;
    private String tenantDomain;
    private String[] epr;

    public void setMessageBoxName(String messageBoxName) {
        this.messageBoxName = messageBoxName;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public void setSharedUsers(String[] sharedUsers) {
        this.sharedUsers = sharedUsers.clone();
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNumberOfMessages() {

        return numberOfMessages;
    }

    public String getMessageBoxName() {
        return messageBoxName;
    }

    public String[] getSharedUsers() {
        return sharedUsers;
    }

    public String getOwner() {
        return owner;
    }

    public String getMessageBoxId() {
        return messageBoxId;
    }

    public void setMessageBoxId(String messageBoxId) {
        this.messageBoxId = messageBoxId;
    }

    public String getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(String visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String[] getEpr() {
        return epr;
    }

    public void setEpr(String[] epr) {
        this.epr = epr;
    }
}
