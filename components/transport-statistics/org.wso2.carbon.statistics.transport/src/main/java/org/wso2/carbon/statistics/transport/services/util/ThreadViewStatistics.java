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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Java bean class to store Statistics data for Threads. This class will hold the thread related statistics exposed by
 * ThreadView class of Apache Synapse.
 */
public class ThreadViewStatistics {
    private static final Log log = LogFactory.getLog(ThreadViewStatistics.class);

    private double clientWorkerAvgBlockedWorkerPercentage = 0;
    private double clientWorkerAvgUnblockedWorkerPercentage = 0;
    private int clientWorkerDeadLockedWorkers = 0;
    private double clientWorkerLast15MinuteBlockedWorkerPercentage = 0;

    private double clientWorkerLast24HourBlockedWorkerPercentage = 0;
    private double clientWorkerLast5MinuteBlockedWorkerPercentage = 0;
    private double clientWorkerLast8HourBlockedWorkerPercentage = 0;
    private double clientWorkerLastHourBlockedWorkerPercentage = 0;

    private double clientWorkerLastMinuteBlockedWorkerPercentage = 0;
    private String clientWorkerLastResetTime = "";
    private int clientWorkerTotalWorkerCount = 0;

    private double serverWorkerAvgBlockedWorkerPercentage = 0;
    private double serverWorkerAvgUnblockedWorkerPercentage = 0;
    private int serverWorkerDeadLockedWorkers = 0;
    private double serverWorkerLast15MinuteBlockedWorkerPercentage = 0;

    private double serverWorkerLast24HourBlockedWorkerPercentage = 0;
    private double serverWorkerLast5MinuteBlockedWorkerPercentage = 0;
    private double serverWorkerLast8HourBlockedWorkerPercentage = 0;
    private double serverWorkerLastHourBlockedWorkerPercentage = 0;

    private double serverWorkerLastMinuteBlockedWorkerPercentage = 0;
    private String serverWorkerLastResetTime = "";
    private int serverWorkerTotalWorkerCount = 0;

    public ThreadViewStatistics(MBeanServer server) {
        String domains[] = server.getDomains();
        for (int i = 0; i < domains.length; i++) {
            System.out.println("Domain[" + i + "] = " + domains[i]);
        }

        updateClientWorkerStats(server);
        updateServerWorkerStats(server);
    }

    private void updateClientWorkerStats(MBeanServer server) {
        ObjectName clientWorkerObjectName = null;
        try {
            clientWorkerObjectName = new ObjectName(
                    "org.apache.synapse:Type=Threading,Name=HttpClientWorker");
        } catch (MalformedObjectNameException e) {
            log.warn("Malformed Object Name for MBean", e);
        }

        String[] clientWorkerAttributes = {"avgBlockedWorkerPercentage",
                "avgUnblockedWorkerPercentage", "deadLockedWorkers",
                "last15MinuteBlockedWorkerPercentage", "last24HourBlockedWorkerPercentage",
                "last5MinuteBlockedWorkerPercentage", "last8HourBlockedWorkerPercentage",
                "lastHourBlockedWorkerPercentage", "lastMinuteBlockedWorkerPercentage",
                "lastResetTime,totalWorkerCount"};

        ArrayList list = null;
        try {
            list = server.getAttributes(clientWorkerObjectName, clientWorkerAttributes);
        } catch (InstanceNotFoundException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        } catch (ReflectionException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        }

        Hashtable<String, String> ht = new Hashtable<String, String>();
        String keyValueArray[];

        if ((list != null) && (list.size() > 0)) {
            for (Object obj : list) {
                keyValueArray = obj.toString().split("=");
                System.out.println(keyValueArray[0] + " = " + keyValueArray[1]);
                ht.put(keyValueArray[0], keyValueArray[1]);
            }

            this.setClientWorkerAvgBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("avgBlockedWorkerPercentage")));
            this.setClientWorkerAvgUnblockedWorkerPercentage(
                    Double.parseDouble(ht.get("avgUnblockedWorkerPercentage")));
            this.setClientWorkerDeadLockedWorkers(
                    Integer.parseInt(ht.get("deadLockedWorkers")));
            this.setClientWorkerLast15MinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last15MinuteBlockedWorkerPercentage")));

            this.setClientWorkerLast24HourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last24HourBlockedWorkerPercentage")));
            this.setClientWorkerLast5MinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last5MinuteBlockedWorkerPercentage")));
            this.setClientWorkerLast8HourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last8HourBlockedWorkerPercentage")));
            this.setClientWorkerLastHourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("lastHourBlockedWorkerPercentage")));

            this.setClientWorkerLastMinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("lastMinuteBlockedWorkerPercentage")));
            this.setClientWorkerLastResetTime(ht.get("lastResetTime"));
            this.setClientWorkerTotalWorkerCount(Integer.parseInt(ht.get("totalWorkerCount")));
        }
    }

    private void updateServerWorkerStats(MBeanServer server) {
        ObjectName serverWorkerObjectName = null;
        try {
            serverWorkerObjectName = new ObjectName(
                    "org.apache.synapse:Type=Threading,Name=HttpServerWorker");
        } catch (MalformedObjectNameException e) {
            log.warn("Malformed Object Name for MBean", e);
        }

        String[] serverWorkerAttributes = {"avgBlockedWorkerPercentage",
                "avgUnblockedWorkerPercentage", "deadLockedWorkers",
                "last15MinuteBlockedWorkerPercentage", "last24HourBlockedWorkerPercentage",
                "last5MinuteBlockedWorkerPercentage", "last8HourBlockedWorkerPercentage",
                "lastHourBlockedWorkerPercentage", "lastMinuteBlockedWorkerPercentage",
                "lastResetTime,totalWorkerCount"};

        ArrayList list = null;
        try {
            list = server.getAttributes(serverWorkerObjectName, serverWorkerAttributes);
        } catch (InstanceNotFoundException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        } catch (ReflectionException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        }

        Hashtable<String, String> ht = new Hashtable<String, String>();
        String keyValueArray[];

        if ((list != null) && (list.size() > 0)) {
            for (Object obj : list) {
                keyValueArray = obj.toString().split("=");
                ht.put(keyValueArray[0], keyValueArray[1]);
            }

            this.setServerWorkerAvgBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("avgBlockedWorkerPercentage")));
            this.setServerWorkerAvgUnblockedWorkerPercentage(
                    Double.parseDouble(ht.get("avgUnblockedWorkerPercentage")));
            this.setServerWorkerDeadLockedWorkers(
                    Integer.parseInt(ht.get("deadLockedWorkers")));
            this.setServerWorkerLast15MinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last15MinuteBlockedWorkerPercentage")));

            this.setServerWorkerLast24HourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last24HourBlockedWorkerPercentage")));
            this.setServerWorkerLast5MinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last5MinuteBlockedWorkerPercentage")));
            this.setServerWorkerLast8HourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("last8HourBlockedWorkerPercentage")));
            this.setServerWorkerLastHourBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("lastHourBlockedWorkerPercentage")));

            this.setServerWorkerLastMinuteBlockedWorkerPercentage(
                    Double.parseDouble(ht.get("lastMinuteBlockedWorkerPercentage")));
            this.setServerWorkerLastResetTime(ht.get("lastResetTime"));
            this.setServerWorkerTotalWorkerCount(Integer.parseInt(ht.get("totalWorkerCount")));
        }
    }

    public double getClientWorkerAvgBlockedWorkerPercentage() {
        return clientWorkerAvgBlockedWorkerPercentage;
    }

    public void setClientWorkerAvgBlockedWorkerPercentage(double clientWorkerAvgBlockedWorkerPercentage) {
        this.clientWorkerAvgBlockedWorkerPercentage = clientWorkerAvgBlockedWorkerPercentage;
    }

    public double getClientWorkerAvgUnblockedWorkerPercentage() {
        return clientWorkerAvgUnblockedWorkerPercentage;
    }

    public void setClientWorkerAvgUnblockedWorkerPercentage(double clientWorkerAvgUnblockedWorkerPercentage) {
        this.clientWorkerAvgUnblockedWorkerPercentage = clientWorkerAvgUnblockedWorkerPercentage;
    }

    public int getClientWorkerDeadLockedWorkers() {
        return clientWorkerDeadLockedWorkers;
    }

    public void setClientWorkerDeadLockedWorkers(int clientWorkerDeadLockedWorkers) {
        this.clientWorkerDeadLockedWorkers = clientWorkerDeadLockedWorkers;
    }

    public double getClientWorkerLast15MinuteBlockedWorkerPercentage() {
        return clientWorkerLast15MinuteBlockedWorkerPercentage;
    }

    public void setClientWorkerLast15MinuteBlockedWorkerPercentage(double clientWorkerLast15MinuteBlockedWorkerPercentage) {
        this.clientWorkerLast15MinuteBlockedWorkerPercentage = clientWorkerLast15MinuteBlockedWorkerPercentage;
    }

    public double getClientWorkerLast24HourBlockedWorkerPercentage() {
        return clientWorkerLast24HourBlockedWorkerPercentage;
    }

    public void setClientWorkerLast24HourBlockedWorkerPercentage(double clientWorkerLast24HourBlockedWorkerPercentage) {
        this.clientWorkerLast24HourBlockedWorkerPercentage = clientWorkerLast24HourBlockedWorkerPercentage;
    }

    public double getClientWorkerLast5MinuteBlockedWorkerPercentage() {
        return clientWorkerLast5MinuteBlockedWorkerPercentage;
    }

    public void setClientWorkerLast5MinuteBlockedWorkerPercentage(double clientWorkerLast5MinuteBlockedWorkerPercentage) {
        this.clientWorkerLast5MinuteBlockedWorkerPercentage = clientWorkerLast5MinuteBlockedWorkerPercentage;
    }

    public double getClientWorkerLast8HourBlockedWorkerPercentage() {
        return clientWorkerLast8HourBlockedWorkerPercentage;
    }

    public void setClientWorkerLast8HourBlockedWorkerPercentage(double clientWorkerLast8HourBlockedWorkerPercentage) {
        this.clientWorkerLast8HourBlockedWorkerPercentage = clientWorkerLast8HourBlockedWorkerPercentage;
    }

    public double getClientWorkerLastHourBlockedWorkerPercentage() {
        return clientWorkerLastHourBlockedWorkerPercentage;
    }

    public void setClientWorkerLastHourBlockedWorkerPercentage(double clientWorkerLastHourBlockedWorkerPercentage) {
        this.clientWorkerLastHourBlockedWorkerPercentage = clientWorkerLastHourBlockedWorkerPercentage;
    }

    public double getClientWorkerLastMinuteBlockedWorkerPercentage() {
        return clientWorkerLastMinuteBlockedWorkerPercentage;
    }

    public void setClientWorkerLastMinuteBlockedWorkerPercentage(double clientWorkerLastMinuteBlockedWorkerPercentage) {
        this.clientWorkerLastMinuteBlockedWorkerPercentage = clientWorkerLastMinuteBlockedWorkerPercentage;
    }

    public String getClientWorkerLastResetTime() {
        return clientWorkerLastResetTime;
    }

    public void setClientWorkerLastResetTime(String clientWorkerLastResetTime) {
        this.clientWorkerLastResetTime = clientWorkerLastResetTime;
    }

    public int getClientWorkerTotalWorkerCount() {
        return clientWorkerTotalWorkerCount;
    }

    public void setClientWorkerTotalWorkerCount(int clientWorkerTotalWorkerCount) {
        this.clientWorkerTotalWorkerCount = clientWorkerTotalWorkerCount;
    }

    public double getServerWorkerAvgBlockedWorkerPercentage() {
        return serverWorkerAvgBlockedWorkerPercentage;
    }

    public void setServerWorkerAvgBlockedWorkerPercentage(double serverWorkerAvgBlockedWorkerPercentage) {
        this.serverWorkerAvgBlockedWorkerPercentage = serverWorkerAvgBlockedWorkerPercentage;
    }

    public double getServerWorkerAvgUnblockedWorkerPercentage() {
        return serverWorkerAvgUnblockedWorkerPercentage;
    }

    public void setServerWorkerAvgUnblockedWorkerPercentage(double serverWorkerAvgUnblockedWorkerPercentage) {
        this.serverWorkerAvgUnblockedWorkerPercentage = serverWorkerAvgUnblockedWorkerPercentage;
    }

    public int getServerWorkerDeadLockedWorkers() {
        return serverWorkerDeadLockedWorkers;
    }

    public void setServerWorkerDeadLockedWorkers(int serverWorkerDeadLockedWorkers) {
        this.serverWorkerDeadLockedWorkers = serverWorkerDeadLockedWorkers;
    }

    public double getServerWorkerLast15MinuteBlockedWorkerPercentage() {
        return serverWorkerLast15MinuteBlockedWorkerPercentage;
    }

    public void setServerWorkerLast15MinuteBlockedWorkerPercentage(double serverWorkerLast15MinuteBlockedWorkerPercentage) {
        this.serverWorkerLast15MinuteBlockedWorkerPercentage = serverWorkerLast15MinuteBlockedWorkerPercentage;
    }

    public double getServerWorkerLast24HourBlockedWorkerPercentage() {
        return serverWorkerLast24HourBlockedWorkerPercentage;
    }

    public void setServerWorkerLast24HourBlockedWorkerPercentage(double serverWorkerLast24HourBlockedWorkerPercentage) {
        this.serverWorkerLast24HourBlockedWorkerPercentage = serverWorkerLast24HourBlockedWorkerPercentage;
    }

    public double getServerWorkerLast5MinuteBlockedWorkerPercentage() {
        return serverWorkerLast5MinuteBlockedWorkerPercentage;
    }

    public void setServerWorkerLast5MinuteBlockedWorkerPercentage(double serverWorkerLast5MinuteBlockedWorkerPercentage) {
        this.serverWorkerLast5MinuteBlockedWorkerPercentage = serverWorkerLast5MinuteBlockedWorkerPercentage;
    }

    public double getServerWorkerLast8HourBlockedWorkerPercentage() {
        return serverWorkerLast8HourBlockedWorkerPercentage;
    }

    public void setServerWorkerLast8HourBlockedWorkerPercentage(double serverWorkerLast8HourBlockedWorkerPercentage) {
        this.serverWorkerLast8HourBlockedWorkerPercentage = serverWorkerLast8HourBlockedWorkerPercentage;
    }

    public double getServerWorkerLastHourBlockedWorkerPercentage() {
        return serverWorkerLastHourBlockedWorkerPercentage;
    }

    public void setServerWorkerLastHourBlockedWorkerPercentage(double serverWorkerLastHourBlockedWorkerPercentage) {
        this.serverWorkerLastHourBlockedWorkerPercentage = serverWorkerLastHourBlockedWorkerPercentage;
    }

    public double getServerWorkerLastMinuteBlockedWorkerPercentage() {
        return serverWorkerLastMinuteBlockedWorkerPercentage;
    }

    public void setServerWorkerLastMinuteBlockedWorkerPercentage(double serverWorkerLastMinuteBlockedWorkerPercentage) {
        this.serverWorkerLastMinuteBlockedWorkerPercentage = serverWorkerLastMinuteBlockedWorkerPercentage;
    }

    public String getServerWorkerLastResetTime() {
        return serverWorkerLastResetTime;
    }

    public void setServerWorkerLastResetTime(String serverWorkerLastResetTime) {
        this.serverWorkerLastResetTime = serverWorkerLastResetTime;
    }

    public int getServerWorkerTotalWorkerCount() {
        return serverWorkerTotalWorkerCount;
    }

    public void setServerWorkerTotalWorkerCount(int serverWorkerTotalWorkerCount) {
        this.serverWorkerTotalWorkerCount = serverWorkerTotalWorkerCount;
    }
}
