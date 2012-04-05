/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.statistics.transport.services.util;

/**
 * Java bean class to store Statistics data for Connection. This class will hold the Connection related statistics
 * exposed by ConnectionView class of Apache Synapse.
 */
public class ConnectionViewStatistics {
    private int activeConnections;
    private int last15MinuteConnections;
    private int last24HourConnections;
    private int last5MinuteConnections;

    private int last8HourConnections;
    private int lastHourConnections;
    private int lastMinuteConnections;
    private String lastResetTime;

    private String requestSizesMap;
    private String responseSizesMap;

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public int getLast15MinuteConnections() {
        return last15MinuteConnections;
    }

    public void setLast15MinuteConnections(int last15MinuteConnections) {
        this.last15MinuteConnections = last15MinuteConnections;
    }

    public int getLast24HourConnections() {
        return last24HourConnections;
    }

    public void setLast24HourConnections(int last24HourConnections) {
        this.last24HourConnections = last24HourConnections;
    }

    public int getLast5MinuteConnections() {
        return last5MinuteConnections;
    }

    public void setLast5MinuteConnections(int last5MinuteConnections) {
        this.last5MinuteConnections = last5MinuteConnections;
    }

    public int getLast8HourConnections() {
        return last8HourConnections;
    }

    public void setLast8HourConnections(int last8HourConnections) {
        this.last8HourConnections = last8HourConnections;
    }

    public int getLastHourConnections() {
        return lastHourConnections;
    }

    public void setLastHourConnections(int lastHourConnections) {
        this.lastHourConnections = lastHourConnections;
    }

    public int getLastMinuteConnections() {
        return lastMinuteConnections;
    }

    public void setLastMinuteConnections(int lastMinuteConnections) {
        this.lastMinuteConnections = lastMinuteConnections;
    }

    public String getLastResetTime() {
        return lastResetTime;
    }

    public void setLastResetTime(String lastResetTime) {
        this.lastResetTime = lastResetTime;
    }

    public String getRequestSizesMap() {
        return requestSizesMap;
    }

    public void setRequestSizesMap(String requestSizesMap) {
        this.requestSizesMap = requestSizesMap;
    }

    public String getResponseSizesMap() {
        return responseSizesMap;
    }

    public void setResponseSizesMap(String responseSizesMap) {
        this.responseSizesMap = responseSizesMap;
    }
}
