/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.statistics.transport.services;


import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.statistics.transport.services.util.*;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.base.ManagementSupport;

import java.lang.management.ManagementFactory;

/**
 *  Admin service for the transport-statistics component.
 */
public class TransportStatisticsAdmin extends AbstractAdmin {

    public String[] getExposedTransports() {
        return ConfigHolder.getInstance().getAllTransports();
    }

    public TransportStatistics getTransportStatistic(String transportName) {
        return new TransportStatistics(transportName);
    }

    public ThreadViewStatistics getThreadViewStatistics() {
        return new ThreadViewStatistics(ManagementFactory.getPlatformMBeanServer());
    }

    // TODO : should update the wsdl
    public LatencyViewStatistics getLatencyViewStatistics() {
        return new LatencyViewStatistics(ManagementFactory.getPlatformMBeanServer());
    }

    public SystemTransportStatistics getAllTransportStatistics() {
        String[] transports = ConfigHolder.getInstance().getAllTransports();
        SystemTransportStatistics sysTransportStat = new SystemTransportStatistics();
        for (String transport : transports) {
            sysTransportStat.addTransportStatistics(new TransportStatistics(transport));
        }
        return sysTransportStat;
    }


    public String getTransportClassName(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ConfigHolder.getInstance().getTransportListenerClassName(transportName);
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ConfigHolder.getInstance().getTransportSenderClassName(transportName);
        }
        return "";
    }

    public int getActiveThreadCount(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getActiveThreadCount();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getActiveThreadCount();
        }
        return 0;
    }

    public double getAvgSizeReceived(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getAvgSizeReceived();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getAvgSizeReceived();
        }
        return 0;
    }

    public double getAvgSizeSent(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getAvgSizeSent();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getAvgSizeSent();
        }
        return 0;
    }

    public long getBytesReceived(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getBytesReceived();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getBytesReceived();
        }
        return 0;
    }

    public long getBytesSent(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getBytesSent();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getBytesSent();
        }
        return 0;
    }

    public long getFaultsReceiving(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getFaultsReceiving();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getFaultsReceiving();
        }
        return 0;
    }

    public long getFaultsSending(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getFaultsSending();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getFaultsSending();
        }
        return 0;
    }

    public long getLastResetTime(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getLastResetTime();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getLastResetTime();
        }
        return 0;
    }

    public long getMaxSizeReceived(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMaxSizeReceived();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMaxSizeReceived();
        }
        return 0;
    }

    public long getMaxSizeSent(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMaxSizeSent();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMaxSizeSent();
        }
        return 0;
    }

    public long getMessagesReceived(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMessagesReceived();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMessagesReceived();
        }
        return 0;
    }

    public long getMessagesSent(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMessagesSent();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().
                    getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMessagesSent();
        }
        return 0;
    }

    public long getMetricsWindow(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMetricsWindow();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMetricsWindow();
        }
        return 0;
    }

    public long getMinSizeReceived(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMinSizeReceived();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMinSizeReceived();
        }
        return 0;
    }

    public long getMinSizeSent(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getMinSizeSent();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getMinSizeSent();
        }
        return 0;
    }

    public int getQueueSize(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getQueueSize();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getQueueSize();
        }
        return 0;
    }

    public long getTimeoutsReceiving(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getTimeoutsReceiving();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getTimeoutsReceiving();
        }
        return 0;
    }

    public long getTimeoutsSending(String transportType, String transportName) {
        if (TransportStatisticsConstants.LISTENER.equalsIgnoreCase(transportType)) {
            TransportListener trpListener = ConfigHolder.getInstance().
                    getTransportListener(transportName);
            if (trpListener instanceof ManagementSupport)
                return ((ManagementSupport) trpListener).getTimeoutsSending();
        }
        if (TransportStatisticsConstants.SENDER.equalsIgnoreCase(transportType)) {
            TransportSender trpSender = ConfigHolder.getInstance().getTransportSender(transportName);
            if (trpSender instanceof ManagementSupport)
                return ((ManagementSupport) trpSender).getTimeoutsSending();
        }
        return 0;
    }

}
