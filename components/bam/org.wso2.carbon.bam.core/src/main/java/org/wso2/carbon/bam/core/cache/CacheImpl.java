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
import org.wso2.carbon.bam.core.admin.BAMDataServiceAdmin;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMException;

import java.util.Calendar;
import java.util.HashMap;

public class CacheImpl {


    private HashMap<String, CacheData> cacheDataHashMap;

    public CacheImpl() {
        cacheDataHashMap = BAMUtil.getBAMCache();
    }

    public ServerDO getServer(BAMDataServiceAdmin dsAdmin, String serverName, int tenantID,
                              String serverTypeEventing,
                              int serviceStatType) throws BAMException {
        ServerDO monitoringServer = null;
        String cacheKey = serverName + CacheConstant.CACHE_SEPARATOR + tenantID +
                          CacheConstant.CACHE_SEPARATOR + serverTypeEventing +
                          CacheConstant.CACHE_SEPARATOR + serviceStatType;
        CacheData cacheData = cacheDataHashMap.get(cacheKey);
        if (cacheData != null) {
            monitoringServer = cacheData.getMonitoringServer();
            cacheData.setTimestamp(Calendar.getInstance());
        } else {
            monitoringServer = dsAdmin.getServer(serverName, tenantID,
                                                 serverTypeEventing,
                                                 serviceStatType);
            if (monitoringServer != null) {
                cacheData = new CacheData();
                cacheData.setMonitoringServer(monitoringServer);
                cacheData.setTimestamp(Calendar.getInstance());
                cacheDataHashMap.put(cacheKey, cacheData);
            }
        }
        return monitoringServer;
    }

    public ServiceDO getService(BAMDataServiceAdmin dsAdmin, String serverName, int tenantID,
                                int serverID, String serviceName)
            throws BAMException {

        ServiceDO service = null;
        String cacheKey = serverName + CacheConstant.CACHE_SEPARATOR + tenantID + CacheConstant.CACHE_SEPARATOR +
                          serverID + CacheConstant.CACHE_SEPARATOR + serviceName;
        CacheData cacheData = cacheDataHashMap.get(cacheKey);
        if (cacheData != null) {
            service = cacheData.getService();
            cacheData.setTimestamp(Calendar.getInstance());
        } else {
            service = dsAdmin.getService(serverID, serviceName);
            if (service != null) {
                cacheData = new CacheData();
                cacheData.setService(service);
                cacheData.setTimestamp(Calendar.getInstance());
                cacheDataHashMap.put(cacheKey, cacheData);
            }
        }
        return service;
    }

    public OperationDO getOperation(BAMDataServiceAdmin dsAdmin, String serverName, int tenantId,
                                    int serviceID, String operationName) throws BAMException {
        OperationDO operation = null;
        String cacheKey = serverName + CacheConstant.CACHE_SEPARATOR + tenantId +
                          CacheConstant.CACHE_SEPARATOR + serviceID + CacheConstant.CACHE_SEPARATOR +
                          operationName;
        CacheData cacheData = cacheDataHashMap.get(cacheKey);
        if (cacheData != null) {
            operation = cacheData.getOperation();
            cacheData.setTimestamp(Calendar.getInstance());
        } else {
            operation = dsAdmin.getOperation(serviceID, operationName);
            if (operation != null) {
                cacheData = new CacheData();
                cacheData.setOperation(operation);
                cacheData.setTimestamp(Calendar.getInstance());
                cacheDataHashMap.put(cacheKey, cacheData);
            }
        }
        return operation;
    }
}
