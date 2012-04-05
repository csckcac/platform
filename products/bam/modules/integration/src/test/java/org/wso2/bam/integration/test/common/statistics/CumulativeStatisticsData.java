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

public class CumulativeStatisticsData {

    private long count;
    private long minResTime;
    private long maxResTime;
    private double totalAvgResTime;
    private int totalRequestCount;
    private int totalResponseCount;
    private int totalFaultCount;

    public CumulativeStatisticsData(int count) {
        this.count = count;
    }

    public long getMinResTime() {
        return minResTime;
    }

    public void setMinResTime(long minResTime) {
        this.minResTime = minResTime;
    }

    public long getMaxResTime() {
        return maxResTime;
    }

    public void setMaxResTime(long maxResTime) {
        this.maxResTime = maxResTime;
    }

    public double getTotalAvgResTime() {
        return totalAvgResTime;
    }

    public void setTotalAvgResTime(double totalAvgResTime) {
        this.totalAvgResTime = totalAvgResTime;
    }

    public int getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(int totalRequestCount) {
        this.totalRequestCount = totalRequestCount;
    }

    public int getTotalResponseCount() {
        return totalResponseCount;
    }

    public void setTotalResponseCount(int totalResponseCount) {
        this.totalResponseCount = totalResponseCount;
    }

    public int getTotalFaultCount() {
        return totalFaultCount;
    }

    public void setTotalFaultCount(int totalFaultCount) {
        this.totalFaultCount = totalFaultCount;
    }

}
