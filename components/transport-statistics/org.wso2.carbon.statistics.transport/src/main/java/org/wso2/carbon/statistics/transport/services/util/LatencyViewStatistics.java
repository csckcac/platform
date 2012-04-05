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
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Java bean class to store Statistics data for Latency. This class will hold the latency related statistics exposed by
 * LatencyView class of Apache Synapse.
 */
public class LatencyViewStatistics {
    private static final Log log = LogFactory.getLog(LatencyViewStatistics.class);

    private double nioHttpAllTimeAvgLatency;
    private double nioHttpLast15MinuteAvgLatency;
    private double nioHttpLast24HourAvgLatency;
    private double nioHttpLast5MinuteAvgLatency;

    private double nioHttpLast8HourAvgLatency;
    private double nioHttpLastHourAvgLatency;
    private double nioHttpLastMinuteAvgLatency;
    private String nioHttpLastResetTime;

    private double nioHttpsAllTimeAvgLatency;
    private double nioHttpsLast15MinuteAvgLatency;
    private double nioHttpsLast24HourAvgLatency;
    private double nioHttpsLast5MinuteAvgLatency;

    private double nioHttpsLast8HourAvgLatency;
    private double nioHttpsLastHourAvgLatency;
    private double nioHttpsLastMinuteAvgLatency;
    private String nioHttpsLastResetTime;

    public LatencyViewStatistics(MBeanServer server) {
        updateNioHttpStats(server);
        updateNioHttpsStats(server);
    }

    private void updateNioHttpStats(MBeanServer server) {
        ObjectName clientWorkerObjectName = null;
        try {
            clientWorkerObjectName = new ObjectName(
                    "org.apache.synapse:Type=NhttpTransportLatency,Name=nio-http");
        } catch (MalformedObjectNameException e) {
            log.warn("Malformed Object Name for MBean",e);
        }

        String [] clientWorkerAttributes = {"AllTimeAvgLatency" , "Last15MinuteAvgLatency",
                "Last24HourAvgLatency", "Last5MinuteAvgLatency", "Last8HourAvgLatency",
                "LastHourAvgLatency", "LastMinuteAvgLatency", "LastResetTime"};

        ArrayList list = null;
        try {
            list = server.getAttributes(clientWorkerObjectName, clientWorkerAttributes);
        } catch (InstanceNotFoundException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        } catch (ReflectionException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        }

        Hashtable<String, String> ht = new Hashtable<String, String>();
        String keyValueArray [];

        if (list != null) {
            for (Object obj : list) {
                keyValueArray = obj.toString().split("=");
                ht.put(keyValueArray[0], keyValueArray[1]);
            }
        }

        this.setNioHttpAllTimeAvgLatency(Double.parseDouble(ht.get("AllTimeAvgLatency")));
        this.setNioHttpLast15MinuteAvgLatency(Double.parseDouble(ht.get("Last15MinuteAvgLatency")));
        this.setNioHttpLast24HourAvgLatency(Double.parseDouble(ht.get("Last24HourAvgLatency")));
        this.setNioHttpLast5MinuteAvgLatency(Double.parseDouble(ht.get("Last5MinuteAvgLatency")));

        this.setNioHttpLast8HourAvgLatency(Double.parseDouble(ht.get("Last8HourAvgLatency")));
        this.setNioHttpLastHourAvgLatency(Double.parseDouble(ht.get("LastHourAvgLatency")));
        this.setNioHttpLastMinuteAvgLatency(Double.parseDouble(ht.get("LastMinuteAvgLatency")));
        this.setNioHttpLastResetTime(("LastResetTime"));
    }

    private void updateNioHttpsStats(MBeanServer server) {
        ObjectName clientWorkerObjectName = null;
        try {
            clientWorkerObjectName = new ObjectName(
                    "org.apache.synapse:Type=NhttpTransportLatency,Name=nio-https");
        } catch (MalformedObjectNameException e) {
            log.warn("Malformed Object Name for MBean",e);
        }

        String [] clientWorkerAttributes = {"AllTimeAvgLatency" , "Last15MinuteAvgLatency",
                "Last24HourAvgLatency", "Last5MinuteAvgLatency", "Last8HourAvgLatency",
                "LastHourAvgLatency", "LastMinuteAvgLatency", "LastResetTime"};

        ArrayList list = null;
        try {
            list = server.getAttributes(clientWorkerObjectName, clientWorkerAttributes);
        } catch (InstanceNotFoundException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        } catch (ReflectionException e) {
            log.warn("Could not retrieve attributes for MBean", e);
        }

        Hashtable<String, String> ht = new Hashtable<String, String>();
        String keyValueArray [];

        if (list != null) {
            for (Object obj : list) {
                keyValueArray = obj.toString().split("=");
                ht.put(keyValueArray[0], keyValueArray[1]);
            }
        }

        this.setNioHttpsAllTimeAvgLatency(Double.parseDouble(ht.get("AllTimeAvgLatency")));
        this.setNioHttpsLast15MinuteAvgLatency(Double.parseDouble(ht.get("Last15MinuteAvgLatency")));
        this.setNioHttpsLast24HourAvgLatency(Double.parseDouble(ht.get("Last24HourAvgLatency")));
        this.setNioHttpsLast5MinuteAvgLatency(Double.parseDouble(ht.get("Last5MinuteAvgLatency")));

        this.setNioHttpsLast8HourAvgLatency(Double.parseDouble(ht.get("Last8HourAvgLatency")));
        this.setNioHttpsLastHourAvgLatency(Double.parseDouble(ht.get("LastHourAvgLatency")));
        this.setNioHttpsLastMinuteAvgLatency(Double.parseDouble(ht.get("LastMinuteAvgLatency")));
        this.setNioHttpsLastResetTime(("LastResetTime"));
    }

    public double getNioHttpsAllTimeAvgLatency() {
        return nioHttpsAllTimeAvgLatency;
    }

    public void setNioHttpsAllTimeAvgLatency(double nioHttpsAllTimeAvgLatency) {
        this.nioHttpsAllTimeAvgLatency = nioHttpsAllTimeAvgLatency;
    }

    public double getNioHttpsLast15MinuteAvgLatency() {
        return nioHttpsLast15MinuteAvgLatency;
    }

    public void setNioHttpsLast15MinuteAvgLatency(double nioHttpsLast15MinuteAvgLatency) {
        this.nioHttpsLast15MinuteAvgLatency = nioHttpsLast15MinuteAvgLatency;
    }

    public double getNioHttpsLast24HourAvgLatency() {
        return nioHttpsLast24HourAvgLatency;
    }

    public void setNioHttpsLast24HourAvgLatency(double nioHttpsLast24HourAvgLatency) {
        this.nioHttpsLast24HourAvgLatency = nioHttpsLast24HourAvgLatency;
    }

    public double getNioHttpsLast5MinuteAvgLatency() {
        return nioHttpsLast5MinuteAvgLatency;
    }

    public void setNioHttpsLast5MinuteAvgLatency(double nioHttpsLast5MinuteAvgLatency) {
        this.nioHttpsLast5MinuteAvgLatency = nioHttpsLast5MinuteAvgLatency;
    }

    public double getNioHttpsLast8HourAvgLatency() {
        return nioHttpsLast8HourAvgLatency;
    }

    public void setNioHttpsLast8HourAvgLatency(double nioHttpsLast8HourAvgLatency) {
        this.nioHttpsLast8HourAvgLatency = nioHttpsLast8HourAvgLatency;
    }

    public double getNioHttpsLastHourAvgLatency() {
        return nioHttpsLastHourAvgLatency;
    }

    public void setNioHttpsLastHourAvgLatency(double nioHttpsLastHourAvgLatency) {
        this.nioHttpsLastHourAvgLatency = nioHttpsLastHourAvgLatency;
    }

    public double getNioHttpsLastMinuteAvgLatency() {
        return nioHttpsLastMinuteAvgLatency;
    }

    public void setNioHttpsLastMinuteAvgLatency(double nioHttpsLastMinuteAvgLatency) {
        this.nioHttpsLastMinuteAvgLatency = nioHttpsLastMinuteAvgLatency;
    }

    public String getNioHttpsLastResetTime() {
        return nioHttpsLastResetTime;
    }

    public void setNioHttpsLastResetTime(String nioHttpsLastResetTime) {
        this.nioHttpsLastResetTime = nioHttpsLastResetTime;
    }

    public double getNioHttpAllTimeAvgLatency() {
        return nioHttpAllTimeAvgLatency;
    }

    public void setNioHttpAllTimeAvgLatency(double nioHttpAllTimeAvgLatency) {
        this.nioHttpAllTimeAvgLatency = nioHttpAllTimeAvgLatency;
    }

    public double getNioHttpLast15MinuteAvgLatency() {
        return nioHttpLast15MinuteAvgLatency;
    }

    public void setNioHttpLast15MinuteAvgLatency(double nioHttpLast15MinuteAvgLatency) {
        this.nioHttpLast15MinuteAvgLatency = nioHttpLast15MinuteAvgLatency;
    }

    public double getNioHttpLast24HourAvgLatency() {
        return nioHttpLast24HourAvgLatency;
    }

    public void setNioHttpLast24HourAvgLatency(double nioHttpLast24HourAvgLatency) {
        this.nioHttpLast24HourAvgLatency = nioHttpLast24HourAvgLatency;
    }

    public double getNioHttpLast5MinuteAvgLatency() {
        return nioHttpLast5MinuteAvgLatency;
    }

    public void setNioHttpLast5MinuteAvgLatency(double nioHttpLast5MinuteAvgLatency) {
        this.nioHttpLast5MinuteAvgLatency = nioHttpLast5MinuteAvgLatency;
    }

    public double getNioHttpLast8HourAvgLatency() {
        return nioHttpLast8HourAvgLatency;
    }

    public void setNioHttpLast8HourAvgLatency(double nioHttpLast8HourAvgLatency) {
        this.nioHttpLast8HourAvgLatency = nioHttpLast8HourAvgLatency;
    }

    public double getNioHttpLastHourAvgLatency() {
        return nioHttpLastHourAvgLatency;
    }

    public void setNioHttpLastHourAvgLatency(double nioHttpLastHourAvgLatency) {
        this.nioHttpLastHourAvgLatency = nioHttpLastHourAvgLatency;
    }

    public double getNioHttpLastMinuteAvgLatency() {
        return nioHttpLastMinuteAvgLatency;
    }

    public void setNioHttpLastMinuteAvgLatency(double nioHttpLastMinuteAvgLatency) {
        this.nioHttpLastMinuteAvgLatency = nioHttpLastMinuteAvgLatency;
    }

    public String getNioHttpLastResetTime() {
        return nioHttpLastResetTime;
    }

    public void setNioHttpLastResetTime(String nioHttpLastResetTime) {
        this.nioHttpLastResetTime = nioHttpLastResetTime;
    }
}
