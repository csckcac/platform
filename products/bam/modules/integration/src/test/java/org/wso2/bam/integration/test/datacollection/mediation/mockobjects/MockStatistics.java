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
package org.wso2.bam.integration.test.datacollection.mediation.mockobjects;

import org.apache.synapse.aspects.statistics.view.Statistics;

public class MockStatistics extends Statistics {

    private long maxProcessingTime;
    private long minProcessingTime;
    private double avgProcessingTime;
    private int count;
    private int faultCount;

    public MockStatistics(String id) {
        super(id);
    }


    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public void setMaxProcessingTime(long maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    public void setMinProcessingTime(long minProcessingTime) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MockStatistics)) {
            return false;
        }

        MockStatistics that = (MockStatistics) o;

        if (Double.compare(that.avgProcessingTime, avgProcessingTime) != 0) {
            return false;
        }
        if (count != that.count) {
            return false;
        }
        if (faultCount != that.faultCount) {
            return false;
        }
        if (maxProcessingTime != that.maxProcessingTime) {
            return false;
        }
        if (minProcessingTime != that.minProcessingTime) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (maxProcessingTime ^ (maxProcessingTime >>> 32));
        result = 31 * result + (int) (minProcessingTime ^ (minProcessingTime >>> 32));
        temp = avgProcessingTime != +0.0d ? Double.doubleToLongBits(avgProcessingTime) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + count;
        result = 31 * result + faultCount;
        return result;
    }

}
