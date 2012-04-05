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
package org.wso2.bam.integration.test.common.statistics;

public class StatisticsData {

    private String serverName;
    private String serviceName;
    private String operationName;

    private int tenantID;
    private long minResTime;
    private double avgResTime;
    private long maxResTime;
    private int requestCount;
    private int responseCount;
    private int faultCount;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public int getTenantID() {
        return tenantID;
    }

    public void setTenantID(int tenantID) {
        this.tenantID = tenantID;
    }

    public long getMinResTime() {
        return minResTime;
    }

    public void setMinResTime(long minResTime) {
        this.minResTime = minResTime;
    }

    public double getAvgResTime() {
        return avgResTime;
    }

    public void setAvgResTime(double avgResTime) {
        this.avgResTime = avgResTime;
    }

    public long getMaxResTime() {
        return maxResTime;
    }

    public void setMaxResTime(long maxResTime) {
        this.maxResTime = maxResTime;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }
}
