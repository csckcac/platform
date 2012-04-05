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

import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;
import org.wso2.carbon.bam.core.clients.StatisticsAdminClient;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.core.util.ClientAuthHandler;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.statistics.stub.types.carbon.ServiceStatistics;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * Pulls service statistics from WSAS pull mode servers.
 */
public class ServiceStatisticsDataPuller extends AbstractDataPuller {

    public ServiceStatisticsDataPuller(ServerDO server) {
        setServer(server);
    }

    public StatisticsDO pullData(Object ctx) throws BAMException, RemoteException {
        BAMPersistenceManager pm = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
        String svcName = (String) ctx;
        String sessionCookie = ClientAuthHandler.getClientAuthHandler().getSessionString(getServer());
        StatisticsAdminClient client = new StatisticsAdminClient(getServer().getServerURL(), sessionCookie);
        ServiceStatistics svcStatistics = client.getServiceStatistics(svcName);
        ServiceStatisticsDO statisticsDO = new ServiceStatisticsDO();
        statisticsDO.setServerID(getServer().getId());
        statisticsDO.setServerURL(getServer().getServerURL());
        statisticsDO.setServiceName(svcName);

        //This should set svc.serviceID if the service is already in DB
          ServiceDO svc ;
        int serviceId = statisticsDO.getServiceID();

        if (serviceId > 0) {
            svc = pm.getService(serviceId);
        } else {
            svc = pm.getService(statisticsDO.getServerID(), statisticsDO.getServiceName());
        }


        if (svc == null) {
            svc = new ServiceDO();
            svc.setName(svcName);
            svc.setServerID(getServer().getId());
            BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).addService(svc);
            //now that the service was added, this should definitely set serviceID.
           // svc = statisticsDO.getService();
        }

        statisticsDO.setServiceID(svc.getId());

        //TODO Where should we set the timestamp?
        statisticsDO.setTimestamp(Calendar.getInstance());
        populateServiceStatisticsDO(statisticsDO, svcStatistics);
        return statisticsDO;
    }

    private static void populateServiceStatisticsDO(ServiceStatisticsDO statisticsDO, ServiceStatistics statistics) {

        statisticsDO.setAvgResTime(statistics.getAvgResponseTime());
        statisticsDO.setMaxResTime(statistics.getMaxResponseTime());
        statisticsDO.setMinResTime(statistics.getMinResponseTime());
        statisticsDO.setReqCount(statistics.getTotalRequestCount());
        statisticsDO.setResCount(statistics.getTotalResponseCount());
        statisticsDO.setFaultCount(statistics.getTotalFaultCount());
    }
}
