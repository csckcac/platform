/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.data.publisher.activity.service.config;

import org.wso2.carbon.bam.data.publisher.activity.service.ActivityPublisherConstants;

/**
 * Eventing configuration data that goes into the registry. Consists of the enable/disable status
 * for statistics eventing.
 */
public class EventingConfigData {
    private String enableEventing;
    private int messageThreshold;
    private String enableMessageLookup;
    private String[] xPathExpressions;
    private String enableMessageDumping;

    public EventingConfigData() {
    }

    public String getEnableEventing() {
        return enableEventing;
    }

    public void setEnableEventing(String enableEventing) {
        this.enableEventing = enableEventing;
    }

    public boolean eventingEnabled() {
        return ActivityPublisherConstants.EVENTING_ON.equals(enableEventing);
    }

    public int getMessageThreshold() {
        return messageThreshold;
    }

    public void setMessageThreshold(int messageThreshold) {
        this.messageThreshold = messageThreshold;
    }

    public String getEnableMessageLookup() {
        return enableMessageLookup;
    }

    public void setEnableMessageLookup(String enableMessageLookup) {
        this.enableMessageLookup = enableMessageLookup;
    }

    public boolean messageLookupEnabled() {
        return ActivityPublisherConstants.MESSAGE_LOOKUP_ON.equals(enableMessageLookup);
    }

    public String[] getXPathExpressions() {
        return xPathExpressions;
    }

    public void setXPathExpressions(String[] pathExpression) {
        xPathExpressions = pathExpression;
    }
    public String getEnableMessageDumping() {
        return enableMessageDumping;
    }

    public void setEnableMessageDumping(String enableMessageDumping) {
        this.enableMessageDumping = enableMessageDumping;
    }

    public boolean messageDumpingEnabled() {
        return ActivityPublisherConstants.MESSAGE_DUMPING_ON.equals(enableMessageDumping);
    }
}