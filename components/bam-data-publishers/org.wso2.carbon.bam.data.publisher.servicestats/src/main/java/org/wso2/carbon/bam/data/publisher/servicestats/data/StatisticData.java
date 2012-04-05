/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.data.publisher.servicestats.data;


import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;
import java.util.Collection;

public class StatisticData {

    private SystemStatistics systemStatistics;

    private Collection<OperationStatisticData> operationStatisticsList;

    private Collection<ServiceStatisticData> serviceStatisticsList;

    private MessageContext msgCtxOfStatData;

    private Timestamp timestamp;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public MessageContext getMsgCtxOfStatData() {
        return msgCtxOfStatData;
    }

    public void setMsgCtxOfStatData(MessageContext msgCtxOfStatData) {
        this.msgCtxOfStatData = msgCtxOfStatData;
    }

    public SystemStatistics getSystemStatistics() {
        return systemStatistics;
    }

    public void setSystemStatistics(SystemStatistics systemStatistics) {
        this.systemStatistics = systemStatistics;
    }

    public Collection<OperationStatisticData> getOperationStatisticsList() {
        return operationStatisticsList;
    }

    public void setOperationStatisticsList(
            Collection<OperationStatisticData> operationStatisticsList) {
        this.operationStatisticsList = operationStatisticsList;
    }

    public Collection<ServiceStatisticData> getServiceStatisticsList() {
        return serviceStatisticsList;
    }

    public void setServiceStatisticsList(Collection<ServiceStatisticData> serviceStatisticsList) {
        this.serviceStatisticsList = serviceStatisticsList;
    }

}
