/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.common.dataobjects.mediation;

/*
 * Mediation Summary Statistics Data class
 */
public class MediationSummaryStatistic {

    private int timeDimensionId;

    private int serverId;

    private String name;

    private String direction;

    private double maxProcessingTime;

    private double minProcessingTime;

    private double avgProcessingTime;

    private int count;

    private int faultCount;

    public MediationSummaryStatistic() {
        initialize();
    }

    public void initialize() {
        setName("NULL");
    	setAllZeros();
    }

    public void setAllZeros() {
        setMaxProcessingTime(0);
        setMinProcessingTime(0);
        setAvgProcessingTime(0);
        setCount(0);
        setFaultCount(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public void setMaxProcessingTime(double maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    public double getMinProcessingTime() {
        return minProcessingTime;
    }

    public void setMinProcessingTime(double minProcessingTime) {
        this.minProcessingTime = minProcessingTime;
    }

    public double getAvgProcessingTime() {
        return avgProcessingTime;
    }

    public void setAvgProcessingTime(double avgProcessingTime) {
        this.avgProcessingTime = avgProcessingTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getTimeDimensionId() {
        return timeDimensionId;
    }

    public void setTimeDimensionId(int timeDimensionId) {
        this.timeDimensionId = timeDimensionId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

	public String toString() {
		return "MediationSummaryStatistic [avgProcessingTime="
				+ avgProcessingTime + ", count=" + count + ", direction="
				+ direction + ", faultCount=" + faultCount
				+ ", maxProcessingTime=" + maxProcessingTime
				+ ", minProcessingTime=" + minProcessingTime + ", name=" + name
				+ ", serverId=" + serverId + ", timeDimensionId="
				+ timeDimensionId + "]";
	}
}
