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

import org.apache.axis2.AxisFault;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;
import org.wso2.carbon.bam.core.clients.StatisticsAdminClient;
import org.wso2.carbon.bam.core.util.ClientAuthHandler;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * Pulls server statistics from WSAS pull mode servers.
 */
public class ServerStatisticsDataPuller extends AbstractDataPuller {

    public ServerStatisticsDataPuller(ServerDO server) {
        setServer(server);
    }

    public StatisticsDO pullDataInternal(ServerDO server) throws BAMException, RemoteException {
        String sessionCookie = ClientAuthHandler.getClientAuthHandler().getSessionString(server);
        String serverUrl = server.getServerURL();
        StatisticsAdminClient client = new StatisticsAdminClient(serverUrl, sessionCookie);
        SystemStatistics sysStatistics = client.getSystemStatistics();
        ServerStatisticsDO statisticsDO = new ServerStatisticsDO();
        statisticsDO.setServerID(server.getId());
        statisticsDO.setServerURL(serverUrl);

        //TODO Where should we set the timestamp?
        statisticsDO.setTimestamp(Calendar.getInstance());
        populateServerStatisticsDO(statisticsDO, sysStatistics);
        return statisticsDO;

    }

    public StatisticsDO pullData(Object serverObj) throws BAMException, RemoteException {
        StatisticsDO statisticsDO = null;
        ServerDO server=(ServerDO)serverObj;

        try {
            statisticsDO = pullDataInternal(server);
        } catch (AxisFault axisFault) {
            if (ClientAuthHandler.checkAuthException(axisFault)) {
                ClientAuthHandler.getClientAuthHandler().authenticateForcefully(getServer());
                statisticsDO = pullDataInternal(server);
            }
        }
        return statisticsDO;
    }

    private static void populateServerStatisticsDO(ServerStatisticsDO statisticsDO, SystemStatistics statistics) {
        statisticsDO.setAvgResTime(statistics.getAvgResponseTime());
        statisticsDO.setMaxResTime(statistics.getMaxResponseTime());
        statisticsDO.setMinResTime(statistics.getMinResponseTime());
        statisticsDO.setReqCount(statistics.getTotalRequestCount());
        statisticsDO.setResCount(statistics.getTotalResponseCount());
        statisticsDO.setFaultCount(statistics.getTotalFaultCount());
    }
}
