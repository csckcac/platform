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

package org.wso2.carbon.bam.common.dataobjects.common;


import org.wso2.carbon.bam.util.BAMException;

public class MonitoredServerDTO {
    private int serverId;
    private int tenantId = -1;
    private String serverURL;
    private String username;
    private String password;

    /**
     * Server type is either Polling (pull) or Eventing (push).
     */
    private String serverType;

    /**
     * Subscription ID for push (eventing) servers. This is required when unsubscribing, renewing the
     * subscription, and checking the subscription status.
     */
    private String subscriptionID;

    /**
     * Subscribers who have been registered to capture events generated from this server. This is useful
     * in clustered monitoring scenarios. For example this allows a server other than the instance
     * adding the server to collect events as in the case of a active-passive scenario.
     * Further more than one instance can be made to retrieve events in a active-active scenario.
     */
    private String[] subscriptionEprs;

    private String description;
    private int category;
    private String active;
    private long pollingInterval = 0;

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getTenantId() throws BAMException {
//        if (tenantId == 0) {
//            BAMTenantAdmin tenantAdmin = new BAMTenantAdmin();
//            tenantId = tenantAdmin.getTenantId();
//        }
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getActive() {
        return active;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

/*    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }*/

    public void setSubscriptionEprs(String[] subscriptionEprs) {
        this.subscriptionEprs = subscriptionEprs;
    }

    public String[] getSubscriptionEprs() {
        return subscriptionEprs;
    }
}
