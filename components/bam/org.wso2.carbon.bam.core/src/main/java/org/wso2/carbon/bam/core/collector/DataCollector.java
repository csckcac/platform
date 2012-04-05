/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.core.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.service.*;
import org.wso2.carbon.bam.core.BAMConstants;
import org.wso2.carbon.bam.core.admin.BAMDataServiceAdmin;
import org.wso2.carbon.bam.core.admin.MonitoredServerServiceInfoAdmin;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.core.util.BackOffCounter;
import org.wso2.carbon.bam.util.BAMException;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

/**
 * Pulls dataobjects from servers added in pull mode with a exponential backoff algorithm for failed attempts.
 */
public class DataCollector extends TimerTask {

    private static Log log = LogFactory.getLog(DataCollector.class);

    private boolean running = false;        // Indicates the task is currently running
    private boolean signalled = false;      // Sever is shutting down. So do not run this task again

    public DataCollector() {
    }

    public void run() {

        // If the this Timer has been signalled let Activator know the task is not running and return
        if (signalled) {
            running = false;
            return;
        } else {
            running = true;
        }

        List<ServerDO> serverList = null;
        BAMPersistenceManager persistenceManager;
        BAMDataServiceAdmin statisticsAdmin = new BAMDataServiceAdmin();

        try {
             persistenceManager = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
            if (persistenceManager != null) {
                serverList = Arrays.asList(persistenceManager.getMonitoredServersByType(BAMConstants.SERVER_TYPE_PULL));
            } else {
                throw new BAMException("Can't initialize BAMPersistenceManager");
            }
        } catch (Exception e) {
            log.warn("Error occurred while retrieving the server list for polling data");
            if (log.isDebugEnabled()) {
                log.error("Error occurred while retrieving the server list for polling data", e);
            }
            return;
        }

        if (serverList != null && serverList.size() > 0) {
            BackOffCounter backoffcounter = BackOffCounter.getInstance();
            for (ServerDO server : serverList) {

                boolean isServerUp = isServerUpAndRunning(server);

                try {
                      Boolean state = false;
                    if(server.getActive()){
                        state =true;
                    }

                    if (state && !(backoffcounter.shouldBackoff(server))&& isServerUp) {

                        DataPuller  svrDataPuller = DataPullerFactory.getDataPuller(server, DataPullerFactory.SERVER_STATISTICS_PULLER);
                        MonitoredServerServiceInfoAdmin serviceInfoAdmin = new MonitoredServerServiceInfoAdmin();
                        String[] serviceNames = serviceInfoAdmin.getServiceNames(server);
                        ServerStatisticsDO svrStatisticsDO = (ServerStatisticsDO) svrDataPuller.pullData(server);
                        statisticsAdmin.addServerStatistics(svrStatisticsDO);

                        if (serviceNames != null && serviceNames.length > 0) {

                            DataPuller dataPuller = DataPullerFactory.getDataPuller(server,DataPullerFactory.SERVICE_STATISTICS_PULLER);
                            for (String serviceName : serviceNames) {
                                ServiceStatisticsDO svcStatisticsDO = (ServiceStatisticsDO) dataPuller.pullData(serviceName);
                                if (svcStatisticsDO != null) {
                                    statisticsAdmin.addServiceStatistics(svcStatisticsDO);
                                }

                                ServiceDO svc = persistenceManager.getService(server.getId(), serviceName);
                                String[] operationNames = serviceInfoAdmin.getOperationNames(server, serviceName);

                                if (operationNames != null && operationNames.length > 0) {
                                    DataPuller opDataPuller = DataPullerFactory.getDataPuller(server,
                                            DataPullerFactory.OPERTION_STATISTICS_PULLER);

                                    for (String operationName : operationNames) {
                                        OperationDO operationDO = new OperationDO();
                                        operationDO.setName(operationName);
                                        operationDO.setServiceID(svc.getId());
                                        OperationStatisticsDO opStatisticsDO;
                                        opStatisticsDO = (OperationStatisticsDO) opDataPuller.pullData(operationDO);
                                        statisticsAdmin.addOperationStatistics(opStatisticsDO);
                                    }
                                }
                            }
                        }
                        // Successfully collected data from this server... hence no more backing off.
                        backoffcounter.resetFailCount(server);
                    }

                } catch (Exception e) {
                    backoffcounter.incrementFailCount(server);
                    log.warn("Error occurred while polling data from server " + server.getServerURL());
                    if (log.isDebugEnabled()) {
                        log.error("Error occurred while polling data from server " + server.getServerURL(), e);
                    }
                }
            }
        }

    }

    private boolean isServerUpAndRunning(ServerDO server) {
        boolean isServerUp;
        int firstElement = 0;
        int secondElement = 1;
        String[] urlAndPort = server.getServerURL().split("://")[secondElement].split(":");
        String url = urlAndPort[firstElement];
        String port;
        if(urlAndPort.length > 1){
            if (urlAndPort[secondElement].contains("/")) {
                port = urlAndPort[secondElement].split("/")[firstElement];
            } else {
                port = urlAndPort[secondElement];
            }
        } else {
            //port is not given. We have to find the port based on protocol
            String protocol = server.getServerURL().split("://")[firstElement];
            if("http".equals(protocol)){
                port = "80";
            } else {
                //Assumption is, URL can be only http or https
                port = "443";
            }
        }

        try {
            Socket socket = new Socket(url, Integer.parseInt(port));
            isServerUp = true;
        } catch (IOException e) {
            isServerUp = false;
        }
        return isServerUp;
    }

    public boolean getRunningState() {
        return running;
    }

    public void setSignalledState(boolean signalled) {
        this.signalled = signalled;
    }


}
