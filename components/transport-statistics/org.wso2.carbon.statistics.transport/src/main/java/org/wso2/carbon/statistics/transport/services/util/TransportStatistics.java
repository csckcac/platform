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

package org.wso2.carbon.statistics.transport.services.util;

import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.base.ManagementSupport;

/**
 * Java bean class to store Statistics data for a given Transport.
 */
public class TransportStatistics {
    private int transportListenerActiveThreadCount;
    private double transportListenerAvgSizeReceived;
    private double transportListenerAvgSizeSent;
    private long transportListenerBytesReceived;
    private long transportListenerBytesSent;
    private long transportListenerFaultsReceiving;
    private long transportListenerFaultsSending;
    private long transportListenerLastResetTime;
    private long transportListenerMaxSizeReceived;
    private long transportListenerMaxSizeSent;
    private long transportListenerMessagesReceived;
    private long transportListenerMessagesSent;
    private long transportListenerMetricsWindow;
    private long transportListenerMinSizeReceived;
    private long transportListenerMinSizeSent;
    private int transportListenerQueueSize;
    private long transportListenerTimeoutsReceiving;
    private long transportListenerTimeoutsSending;
    private String transportListenerClassName;

    private int transportSenderActiveThreadCount;
    private double transportSenderAvgSizeReceived;
    private double transportSenderAvgSizeSent;
    private long transportSenderBytesReceived;
    private long transportSenderBytesSent;
    private long transportSenderFaultsReceiving;
    private long transportSenderFaultsSending;
    private long transportSenderLastResetTime;
    private long transportSenderMaxSizeReceived;
    private long transportSenderMaxSizeSent;
    private long transportSenderMessagesReceived;
    private long transportSenderMessagesSent;
    private long transportSenderMetricsWindow;
    private long transportSenderMinSizeReceived;
    private long transportSenderMinSizeSent;
    private int transportSenderQueueSize;
    private long transportSenderTimeoutsReceiving;
    private long transportSenderTimeoutsSending;
    private String transportSenderClassName;

    public TransportStatistics(String transportName) {
        ConfigHolder configHolder = ConfigHolder.getInstance();
        TransportListener transportListener = configHolder.getTransportListener(transportName);
        TransportSender transportSender = configHolder.getTransportSender(transportName);

        if (transportListener != null && transportListener instanceof ManagementSupport) {
            setTransportListenerActiveThreadCount(((ManagementSupport) transportListener).
                    getActiveThreadCount());
            setTransportListenerAvgSizeReceived(((ManagementSupport) transportListener).
                    getAvgSizeReceived());
            setTransportListenerAvgSizeSent(((ManagementSupport) transportListener).
                    getAvgSizeSent());
            setTransportListenerBytesReceived(((ManagementSupport) transportListener).
                    getBytesReceived());
            setTransportListenerBytesSent(((ManagementSupport) transportListener)
                    .getBytesSent());
            setTransportListenerFaultsReceiving(((ManagementSupport) transportListener).
                    getFaultsReceiving());
            setTransportListenerFaultsSending(((ManagementSupport) transportListener).
                    getFaultsSending());
            setTransportListenerLastResetTime(((ManagementSupport) transportListener).
                    getLastResetTime());
            setTransportListenerMaxSizeReceived(((ManagementSupport) transportListener).
                    getMaxSizeReceived());
            setTransportListenerMaxSizeSent(((ManagementSupport) transportListener).
                    getMaxSizeSent());
            setTransportListenerMessagesReceived(((ManagementSupport) transportListener).
                    getMessagesReceived());
            setTransportListenerMessagesSent(((ManagementSupport) transportListener).
                    getMessagesSent());
            setTransportListenerMetricsWindow(((ManagementSupport) transportListener).
                    getMetricsWindow());
            setTransportListenerMinSizeReceived(((ManagementSupport) transportListener).
                    getMinSizeReceived());
            setTransportListenerMinSizeSent(((ManagementSupport) transportListener).
                    getMinSizeSent());
            setTransportListenerQueueSize(((ManagementSupport) transportListener).
                    getQueueSize());
            setTransportListenerTimeoutsReceiving(((ManagementSupport) transportListener).
                    getTimeoutsReceiving());
            setTransportListenerTimeoutsSending(((ManagementSupport) transportListener).
                    getTimeoutsSending());
            setTransportListenerClassName(ConfigHolder.getInstance().
                    getTransportListenerClassName(transportName));
        }

        if (transportSender != null && transportSender instanceof ManagementSupport) {
            setTransportSenderActiveThreadCount(((ManagementSupport) transportSender).
                    getActiveThreadCount());
            setTransportSenderAvgSizeReceived(((ManagementSupport) transportSender).
                    getAvgSizeReceived());
            setTransportSenderAvgSizeSent(((ManagementSupport) transportSender).
                    getAvgSizeSent());
            setTransportSenderBytesReceived(((ManagementSupport) transportSender).
                    getBytesReceived());
            setTransportSenderBytesSent(((ManagementSupport) transportSender)
                    .getBytesSent());
            setTransportSenderFaultsReceiving(((ManagementSupport) transportSender).
                    getFaultsReceiving());
            setTransportSenderFaultsSending(((ManagementSupport) transportSender).
                    getFaultsSending());
            setTransportSenderLastResetTime(((ManagementSupport) transportSender).
                    getLastResetTime());
            setTransportSenderMaxSizeReceived(((ManagementSupport) transportSender).
                    getMaxSizeReceived());
            setTransportSenderMaxSizeSent(((ManagementSupport) transportSender).
                    getMaxSizeSent());
            setTransportSenderMessagesReceived(((ManagementSupport) transportSender).
                    getMessagesReceived());
            setTransportSenderMessagesSent(((ManagementSupport) transportSender).
                    getMessagesSent());
            setTransportSenderMetricsWindow(((ManagementSupport) transportSender).
                    getMetricsWindow());
            setTransportSenderMinSizeReceived(((ManagementSupport) transportSender).
                    getMinSizeReceived());
            setTransportSenderMinSizeSent(((ManagementSupport) transportSender).
                    getMinSizeSent());
            setTransportSenderQueueSize(((ManagementSupport) transportSender).
                    getQueueSize());
            setTransportSenderTimeoutsReceiving(((ManagementSupport) transportSender).
                    getTimeoutsReceiving());
            setTransportSenderTimeoutsSending(((ManagementSupport) transportSender).
                    getTimeoutsSending());
            setTransportSenderClassName(ConfigHolder.getInstance().
                    getTransportSenderClassName(transportName));
        }

    }

    // Transport Listener

    public void setTransportListenerActiveThreadCount(int activeThreadCount) {
        this.transportListenerActiveThreadCount = activeThreadCount;
    }

    public int getTransportListenerActiveThreadCount() {
        return transportListenerActiveThreadCount;
    }

    public void setTransportListenerAvgSizeReceived(double avgSizeReceived) {
        this.transportListenerAvgSizeReceived = avgSizeReceived;
    }

    public double getTransportListenerAvgSizeReceived() {
        return transportListenerAvgSizeReceived;
    }

    public void setTransportListenerAvgSizeSent(double avgSizeSent) {
        this.transportListenerAvgSizeSent = avgSizeSent;
    }

    public double getTransportListenerAvgSizeSent() {
        return transportListenerAvgSizeSent;
    }

    public void setTransportListenerBytesReceived(long bytesReceived) {
        this.transportListenerBytesReceived = bytesReceived;
    }

    public long getTransportListenerBytesReceived() {
        return transportListenerBytesReceived;
    }

    public void setTransportListenerBytesSent(long bytesSent) {
        this.transportListenerBytesSent = bytesSent;
    }

    public long getTransportListenerBytesSent() {
        return transportListenerBytesSent;
    }

    public void setTransportListenerFaultsReceiving(long faultsReceiving) {
        this.transportListenerFaultsReceiving = faultsReceiving;
    }

    public long getTransportListenerFaultsReceiving() {
        return transportListenerFaultsReceiving;
    }

    public void setTransportListenerFaultsSending(long faultsSending) {
        this.transportListenerFaultsSending = faultsSending;
    }

    public long getTransportListenerFaultsSending() {
        return transportListenerFaultsSending;
    }

    public void setTransportListenerLastResetTime(long lastResetTime) {
        this.transportListenerLastResetTime = lastResetTime;
    }

    public long getTransportListenerLastResetTime() {
        return transportListenerLastResetTime;
    }

    public void setTransportListenerMaxSizeReceived(long maxSizeReceived) {
        this.transportListenerMaxSizeReceived = maxSizeReceived;
    }

    public long getTransportListenerMaxSizeReceived() {
        return transportListenerMaxSizeReceived;
    }

    public void setTransportListenerMaxSizeSent(long maxSizeSent) {
        this.transportListenerMaxSizeSent = maxSizeSent;
    }

    public long getTransportListenerMaxSizeSent() {
        return transportListenerMaxSizeSent;
    }

    public void setTransportListenerMessagesReceived(long messagesReceived) {
        this.transportListenerMessagesReceived = messagesReceived;
    }

    public long getTransportListenerMessagesReceived() {
        return transportListenerMessagesReceived;
    }

    public void setTransportListenerMessagesSent(long messagesSent) {
        this.transportListenerMessagesSent = messagesSent;
    }

    public long getTransportListenerMessagesSent() {
        return transportListenerMessagesSent;
    }

    public void setTransportListenerMetricsWindow(long metricsWindow) {
        this.transportListenerMetricsWindow = metricsWindow;
    }

    public long getTransportListenerMetricsWindow() {
        return transportListenerMetricsWindow;
    }

    public void setTransportListenerMinSizeReceived(long minSizeReceived) {
        this.transportListenerMinSizeReceived = minSizeReceived;
    }

    public long getTransportListenerMinSizeReceived() {
        return transportListenerMinSizeReceived;
    }

    public void setTransportListenerMinSizeSent(long minSizeSent) {
        this.transportListenerMinSizeSent = minSizeSent;
    }

    public long getTransportListenerMinSizeSent() {
        return transportListenerMinSizeSent;
    }

    public void setTransportListenerQueueSize(int queueSize) {
        this.transportListenerQueueSize = queueSize;
    }

    public int getTransportListenerQueueSize() {
        return transportListenerQueueSize;
    }

    public void setTransportListenerTimeoutsReceiving(long timeoutsReceiving) {
        this.transportListenerTimeoutsReceiving = timeoutsReceiving;
    }

    public long getTransportListenerTimeoutsReceiving() {
        return transportListenerTimeoutsReceiving;
    }

    public void setTransportListenerTimeoutsSending(long timeoutsSending) {
        this.transportListenerTimeoutsSending = timeoutsSending;
    }

    public long getTransportListenerTimeoutsSending() {
        return transportListenerTimeoutsSending;
    }

    public void setTransportListenerClassName(String transportListenerClassName) {
        this.transportListenerClassName = transportListenerClassName;
    }

    public String getTransportListenerClassName() {
        return transportListenerClassName;
    }

    // Transport Sender

    public void setTransportSenderActiveThreadCount(int activeThreadCount) {
        this.transportSenderActiveThreadCount = activeThreadCount;
    }

    public int getTransportSenderActiveThreadCount() {
        return transportSenderActiveThreadCount;
    }

    public void setTransportSenderAvgSizeReceived(double avgSizeReceived) {
        this.transportSenderAvgSizeReceived = avgSizeReceived;
    }

    public double getTransportSenderAvgSizeReceived() {
        return transportSenderAvgSizeReceived;
    }

    public void setTransportSenderAvgSizeSent(double avgSizeSent) {
        this.transportSenderAvgSizeSent = avgSizeSent;
    }

    public double getTransportSenderAvgSizeSent() {
        return transportSenderAvgSizeSent;
    }

    public void setTransportSenderBytesReceived(long bytesReceived) {
        this.transportSenderBytesReceived = bytesReceived;
    }

    public long getTransportSenderBytesReceived() {
        return transportSenderBytesReceived;
    }

    public void setTransportSenderBytesSent(long bytesSent) {
        this.transportSenderBytesSent = bytesSent;
    }

    public long getTransportSenderBytesSent() {
        return transportSenderBytesSent;
    }

    public void setTransportSenderFaultsReceiving(long faultsReceiving) {
        this.transportSenderFaultsReceiving = faultsReceiving;
    }

    public long getTransportSenderFaultsReceiving() {
        return transportSenderFaultsReceiving;
    }

    public void setTransportSenderFaultsSending(long faultsSending) {
        this.transportSenderFaultsSending = faultsSending;
    }

    public long getTransportSenderFaultsSending() {
        return transportSenderFaultsSending;
    }

    public void setTransportSenderLastResetTime(long lastResetTime) {
        this.transportSenderLastResetTime = lastResetTime;
    }

    public long getTransportSenderLastResetTime() {
        return transportSenderLastResetTime;
    }

    public void setTransportSenderMaxSizeReceived(long maxSizeReceived) {
        this.transportSenderMaxSizeReceived = maxSizeReceived;
    }

    public long getTransportSenderMaxSizeReceived() {
        return transportSenderMaxSizeReceived;
    }

    public void setTransportSenderMaxSizeSent(long maxSizeSent) {
        this.transportSenderMaxSizeSent = maxSizeSent;
    }

    public long getTransportSenderMaxSizeSent() {
        return transportSenderMaxSizeSent;
    }

    public void setTransportSenderMessagesReceived(long messagesReceived) {
        this.transportSenderMessagesReceived = messagesReceived;
    }

    public long getTransportSenderMessagesReceived() {
        return transportSenderMessagesReceived;
    }

    public void setTransportSenderMessagesSent(long messagesSent) {
        this.transportSenderMessagesSent = messagesSent;
    }

    public long getTransportSenderMessagesSent() {
        return transportSenderMessagesSent;
    }

    public void setTransportSenderMetricsWindow(long metricsWindow) {
        this.transportSenderMetricsWindow = metricsWindow;
    }

    public long getTransportSenderMetricsWindow() {
        return transportSenderMetricsWindow;
    }

    public void setTransportSenderMinSizeReceived(long minSizeReceived) {
        this.transportSenderMinSizeReceived = minSizeReceived;
    }

    public long getTransportSenderMinSizeReceived() {
        return transportSenderMinSizeReceived;
    }

    public void setTransportSenderMinSizeSent(long minSizeSent) {
        this.transportSenderMinSizeSent = minSizeSent;
    }

    public long getTransportSenderMinSizeSent() {
        return transportSenderMinSizeSent;
    }

    public void setTransportSenderQueueSize(int queueSize) {
        this.transportSenderQueueSize = queueSize;
    }

    public int getTransportSenderQueueSize() {
        return transportSenderQueueSize;
    }

    public void setTransportSenderTimeoutsReceiving(long timeoutsReceiving) {
        this.transportSenderTimeoutsReceiving = timeoutsReceiving;
    }

    public long getTransportSenderTimeoutsReceiving() {
        return transportSenderTimeoutsReceiving;
    }

    public void setTransportSenderTimeoutsSending(long timeoutsSending) {
        this.transportSenderTimeoutsSending = timeoutsSending;
    }

    public long getTransportSenderTimeoutsSending() {
        return transportSenderTimeoutsSending;
    }

    public void setTransportSenderClassName(String transportSenderClassName) {
        this.transportSenderClassName = transportSenderClassName;
    }

    public String getTransportSenderClassName() {
        return transportSenderClassName;
    }

}
