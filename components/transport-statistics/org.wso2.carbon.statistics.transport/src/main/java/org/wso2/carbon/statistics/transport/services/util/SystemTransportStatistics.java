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
package org.wso2.carbon.statistics.transport.services.util;

/**
 * Java bean class to store Statistics data for all exposed Transports.
 */
public class SystemTransportStatistics {
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

    public void addTransportStatistics(TransportStatistics transportStatisitics) {
        this.transportListenerActiveThreadCount = this.transportListenerActiveThreadCount +
                transportStatisitics.getTransportListenerActiveThreadCount();
        this.transportListenerAvgSizeReceived = this.transportListenerAvgSizeReceived +
                transportStatisitics.getTransportListenerAvgSizeReceived();
        this.transportListenerAvgSizeSent = this.transportListenerAvgSizeSent +
                transportStatisitics.getTransportListenerAvgSizeSent();
        this.transportListenerBytesReceived = this.transportListenerBytesReceived +
                transportStatisitics.getTransportListenerBytesReceived();
        this.transportListenerBytesSent = this.transportListenerBytesSent +
                transportStatisitics.getTransportListenerBytesSent();
        this.transportListenerFaultsReceiving = this.transportListenerFaultsReceiving +
                transportStatisitics.getTransportListenerFaultsReceiving();
        this.transportListenerFaultsSending = this.transportListenerFaultsSending +
                transportStatisitics.getTransportListenerFaultsSending();
        this.transportListenerLastResetTime = this.transportListenerLastResetTime +
                transportStatisitics.getTransportListenerLastResetTime();
        this.transportListenerMaxSizeReceived = this.transportListenerMaxSizeReceived +
                transportStatisitics.getTransportListenerMaxSizeReceived();
        this.transportListenerMaxSizeSent = this.transportListenerMaxSizeSent +
                transportStatisitics.getTransportListenerMaxSizeSent();
        this.transportListenerMessagesReceived = this.transportListenerMessagesReceived +
                transportStatisitics.getTransportListenerMessagesReceived();
        this.transportListenerMessagesSent = this.transportListenerMessagesSent +
                transportStatisitics.getTransportListenerMessagesSent();
        this.transportListenerMetricsWindow = this.transportListenerMetricsWindow +
                transportStatisitics.getTransportListenerMetricsWindow();
        this.transportListenerMinSizeReceived = this.transportListenerMinSizeReceived +
                transportStatisitics.getTransportListenerMinSizeReceived();
        this.transportListenerMinSizeSent = this.transportListenerMinSizeSent +
                transportStatisitics.getTransportListenerMinSizeSent();
        this.transportListenerQueueSize = this.transportListenerQueueSize +
                transportStatisitics.getTransportListenerQueueSize();
        this.transportListenerTimeoutsReceiving = this.transportListenerTimeoutsReceiving +
                transportStatisitics.getTransportListenerTimeoutsReceiving();
        this.transportListenerTimeoutsSending = this.transportListenerTimeoutsSending +
                transportStatisitics.getTransportListenerTimeoutsSending();

        this.transportSenderActiveThreadCount = this.transportSenderActiveThreadCount +
                transportStatisitics.getTransportSenderActiveThreadCount();
        this.transportSenderAvgSizeReceived = this.transportSenderAvgSizeReceived +
                transportStatisitics.getTransportSenderAvgSizeReceived();
        this.transportSenderAvgSizeSent = this.transportSenderAvgSizeSent +
                transportStatisitics.getTransportSenderAvgSizeSent();
        this.transportSenderBytesReceived = this.transportSenderBytesReceived +
                transportStatisitics.getTransportSenderBytesReceived();
        this.transportSenderBytesSent = this.transportSenderBytesSent +
                transportStatisitics.getTransportSenderBytesSent();
        this.transportSenderFaultsReceiving = this.transportSenderFaultsReceiving +
                transportStatisitics.getTransportSenderFaultsReceiving();
        this.transportSenderFaultsSending = this.transportSenderFaultsSending +
                transportStatisitics.getTransportSenderFaultsSending();
        this.transportSenderLastResetTime = this.transportSenderLastResetTime +
                transportStatisitics.getTransportSenderLastResetTime();
        this.transportSenderMaxSizeReceived = this.transportSenderMaxSizeReceived +
                transportStatisitics.getTransportSenderMaxSizeReceived();
        this.transportSenderMaxSizeSent = this.transportSenderMaxSizeSent +
                transportStatisitics.getTransportSenderMaxSizeSent();
        this.transportSenderMessagesReceived = this.transportSenderMessagesReceived +
                transportStatisitics.getTransportSenderMessagesReceived();
        this.transportSenderMessagesSent = this.transportSenderMessagesSent +
                transportStatisitics.getTransportSenderMessagesSent();
        this.transportSenderMetricsWindow = this.transportSenderMetricsWindow +
                transportStatisitics.getTransportSenderMetricsWindow();
        this.transportSenderMinSizeReceived = this.transportSenderMinSizeReceived +
                transportStatisitics.getTransportSenderMinSizeReceived();
        this.transportSenderMinSizeSent = this.transportSenderMinSizeSent +
                transportStatisitics.getTransportSenderMinSizeSent();
        this.transportSenderQueueSize = this.transportSenderQueueSize +
                transportStatisitics.getTransportSenderQueueSize();
        this.transportSenderTimeoutsReceiving = this.transportSenderTimeoutsReceiving +
                transportStatisitics.getTransportSenderTimeoutsReceiving();
        this.transportSenderTimeoutsSending = this.transportSenderTimeoutsSending +
                transportStatisitics.getTransportSenderTimeoutsSending();
    }

    // Transport Listener

       public int getTransportListenerActiveThreadCount() {
           return transportListenerActiveThreadCount;
       }

       public double getTransportListenerAvgSizeReceived() {
           return transportListenerAvgSizeReceived;
       }

       public double getTransportListenerAvgSizeSent() {
           return transportListenerAvgSizeSent;
       }

       public long getTransportListenerBytesReceived() {
           return transportListenerBytesReceived;
       }

       public long getTransportListenerBytesSent() {
           return transportListenerBytesSent;
       }

       public long getTransportListenerFaultsReceiving() {
           return transportListenerFaultsReceiving;
       }

       public long getTransportListenerFaultsSending() {
           return transportListenerFaultsSending;
       }

       public long getTransportListenerLastResetTime() {
           return transportListenerLastResetTime;
       }

       public long getTransportListenerMaxSizeReceived() {
           return transportListenerMaxSizeReceived;
       }

       public long getTransportListenerMaxSizeSent() {
           return transportListenerMaxSizeSent;
       }

       public long getTransportListenerMessagesReceived() {
           return transportListenerMessagesReceived;
       }

       public long getTransportListenerMessagesSent() {
           return transportListenerMessagesSent;
       }

       public long getTransportListenerMetricsWindow() {
           return transportListenerMetricsWindow;
       }

       public long getTransportListenerMinSizeReceived() {
           return transportListenerMinSizeReceived;
       }

       public long getTransportListenerMinSizeSent() {
           return transportListenerMinSizeSent;
       }

       public int getTransportListenerQueueSize() {
           return transportListenerQueueSize;
       }

       public long getTransportListenerTimeoutsReceiving() {
           return transportListenerTimeoutsReceiving;
       }

       public long getTransportListenerTimeoutsSending() {
           return transportListenerTimeoutsSending;
       }

       // Transport Sender

       public int getTransportSenderActiveThreadCount() {
           return transportSenderActiveThreadCount;
       }

       public double getTransportSenderAvgSizeReceived() {
           return transportSenderAvgSizeReceived;
       }

       public double getTransportSenderAvgSizeSent() {
           return transportSenderAvgSizeSent;
       }

       public long getTransportSenderBytesReceived() {
           return transportSenderBytesReceived;
       }

       public long getTransportSenderBytesSent() {
           return transportSenderBytesSent;
       }

       public long getTransportSenderFaultsReceiving() {
           return transportSenderFaultsReceiving;
       }

       public long getTransportSenderFaultsSending() {
           return transportSenderFaultsSending;
       }

       public long getTransportSenderLastResetTime() {
           return transportSenderLastResetTime;
       }

       public long getTransportSenderMaxSizeReceived() {
           return transportSenderMaxSizeReceived;
       }

       public long getTransportSenderMaxSizeSent() {
           return transportSenderMaxSizeSent;
       }

       public long getTransportSenderMessagesReceived() {
           return transportSenderMessagesReceived;
       }

       public long getTransportSenderMessagesSent() {
           return transportSenderMessagesSent;
       }

       public long getTransportSenderMetricsWindow() {
           return transportSenderMetricsWindow;
       }

       public long getTransportSenderMinSizeReceived() {
           return transportSenderMinSizeReceived;
       }

       public long getTransportSenderMinSizeSent() {
           return transportSenderMinSizeSent;
       }

       public int getTransportSenderQueueSize() {
           return transportSenderQueueSize;
       }

       public long getTransportSenderTimeoutsReceiving() {
           return transportSenderTimeoutsReceiving;
       }

       public long getTransportSenderTimeoutsSending() {
           return transportSenderTimeoutsSending;
       }

}
