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
package org.wso2.carbon.messagebox;

import java.util.ArrayList;
import java.util.List;

public class MessageBoxDetails {
    private String messageBoxName;
    private int numberOfMessages;
    private List<String> sharedUsersList;
    private long defaultVisibilityTimeout;
    private String messageBoxOwner;
    private String messageBoxId;
    private long createdTimeStamp;
    private String tenantDomain;


    public MessageBoxDetails(String messageBoxName, String messageBoxOwner,
                             long defaultVisibilityTimeout,
                             int numberOfMessages) {
        this.messageBoxName = messageBoxName;
        this.messageBoxOwner = messageBoxOwner;
        this.defaultVisibilityTimeout = defaultVisibilityTimeout;
        this.numberOfMessages = numberOfMessages;
        this.sharedUsersList = new ArrayList<String>();
        messageBoxId = messageBoxOwner + MessageBoxConstants.COMPOSITE_QUEUE_NAME_SYMBOL + messageBoxName;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public void setMessageBoxName(String messageBoxName) {
        this.messageBoxName = messageBoxName;
    }

    public String getMessageBoxOwner() {
        return messageBoxOwner;
    }

    public long getDefaultVisibilityTimeout() {
        return defaultVisibilityTimeout;
    }

    public List<String> getSharedUsersList() {
        return sharedUsersList;
    }

    public String getMessageBoxName() {
        return messageBoxName;
    }

    public void setSharedUsersList(List<String> sharedUsersList) {
        for (String sharedUser : sharedUsersList) {
            if (!this.sharedUsersList.contains(sharedUser)) {
                this.sharedUsersList.add(sharedUser);
            }
        }
    }

    public void removeSharedUsers(List<String> usersList) {
        for (String user : usersList) {
            this.sharedUsersList.remove(user);
        }
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public String getMessageBoxId() {
        return messageBoxId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}