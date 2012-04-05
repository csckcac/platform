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
package org.wso2.carbon.bam.core.collector;


import org.wso2.carbon.bam.core.clients.StatisticsAdminClient;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.core.util.ClientAuthHandler;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.statistics.stub.types.carbon.OperationStatistics;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * Pulls operations statistics from WSAS pull mode servers.
 */
public class OperationStatisticsDataPuller extends AbstractDataPuller {

    public OperationStatisticsDataPuller(ServerDO server) {
        setServer(server);
    }

    public StatisticsDO pullData(Object ctx) throws BAMException, RemoteException {
       BAMPersistenceManager persistenceManager = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
        OperationDO tmpOp = (OperationDO) ctx;
        String sessionCookie = ClientAuthHandler.getClientAuthHandler().getSessionString(getServer());
        StatisticsAdminClient client = new StatisticsAdminClient(getServer().getServerURL(), sessionCookie);
        ServiceDO svc = persistenceManager.getService(tmpOp.getServiceID());
        OperationStatistics opStatistics = client.getOperationStatistics(svc.getName(), tmpOp.getName());
        OperationStatisticsDO statisticsDO = new OperationStatisticsDO();
        statisticsDO.setServiceID(tmpOp.getServiceID());
        statisticsDO.setOperationName(tmpOp.getName());
        //This should set op.id if the service is already in DB
        OperationDO op = null;
        int operationId = statisticsDO.getOperationID();
        if(operationId >0){
           op = persistenceManager.getOperation(operationId);
        }else {
          op = persistenceManager.getOperation(statisticsDO.getServiceID() ,statisticsDO.getOperationName());
        }

        //if it is not, add it to DB
        if (op == null) {
            BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).addOperation(tmpOp);
        }
        //now that the op is added, op.id should definitely be set
        if (op != null) {
            statisticsDO.setOperationID(op.getOperationID());
        }

        //TODO Where should we set the timestamp?
        statisticsDO.setTimestamp(Calendar.getInstance());
        populateOperationStatisticsDO(statisticsDO, opStatistics);
        return statisticsDO;
    }

    private static void populateOperationStatisticsDO(OperationStatisticsDO statisticsDO, OperationStatistics statistics) {

        statisticsDO.setAvgResTime(statistics.getAvgResponseTime());
        statisticsDO.setMaxResTime(statistics.getMaxResponseTime());
        statisticsDO.setMinResTime(statistics.getMinResponseTime());
        statisticsDO.setReqCount(statistics.getTotalRequestCount());
        statisticsDO.setResCount(statistics.getTotalResponseCount());
        statisticsDO.setFaultCount(statistics.getTotalFaultCount());
    }

}
