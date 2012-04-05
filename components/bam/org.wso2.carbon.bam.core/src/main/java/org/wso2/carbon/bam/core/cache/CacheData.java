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
package org.wso2.carbon.bam.core.cache;


import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;

import java.util.Calendar;

public class CacheData {

    private Calendar timestamp;
    private ServerDO monitoringServer;
    private ServiceDO service;
    private OperationDO operation;

    public ServerDO getMonitoringServer() {
        return monitoringServer;
    }

    public void setMonitoringServer(ServerDO monitoringServer) {
        this.monitoringServer = monitoringServer;
    }

    public ServiceDO getService() {
        return service;
    }

    public void setService(ServiceDO service) {
        this.service = service;
    }

    public OperationDO getOperation() {
        return operation;
    }

    public void setOperation(OperationDO operation) {
        this.operation = operation;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

}
