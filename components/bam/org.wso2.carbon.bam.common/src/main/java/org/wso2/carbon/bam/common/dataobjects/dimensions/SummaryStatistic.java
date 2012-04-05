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
package org.wso2.carbon.bam.common.dataobjects.dimensions;

public class SummaryStatistic {
    private int timeDimensionId;
    /**
     * typeId can be either serverId, serviceId, operationId... etc.
     */
    private int typeId;
    private double avgResTime;
    private double maxResTime;
    private double minResTime;
    private int reqCount;
    private int resCount;
    private int faultCount;

    public SummaryStatistic() {
        setTypeId(-1);
        setTimeDimensionId(-1);
        setAvgResTime(0);
        setMaxResTime(0);
        setMinResTime(0);
        setReqCount(0);
        setResCount(0);
        setFaultCount(0);
    }

    public double getMaxResTime() {
        return maxResTime;
    }

    public void setMaxResTime(double maxResTime) {
        this.maxResTime = maxResTime;
    }

    public double getMinResTime() {
        return minResTime;
    }

    public void setMinResTime(double minResTime) {
        this.minResTime = minResTime;
    }

    public int getReqCount() {
        return reqCount;
    }

    public void setReqCount(int reqCount) {
        this.reqCount = reqCount;
    }

    public int getResCount() {
        return resCount;
    }

    public void setResCount(int resCount) {
        this.resCount = resCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public double getAvgResTime() {
        return avgResTime;
    }

    public void setAvgResTime(double avgResTime) {
        this.avgResTime = avgResTime;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getTimeDimensionId() {
        return timeDimensionId;
    }

    public void setTimeDimensionId(int timeDimensionId) {
        this.timeDimensionId = timeDimensionId;
    }
}
