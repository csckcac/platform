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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to create three types of dataobjects pullers service/ server/ operation from pull mode servers.
 */
public class DataPullerFactory {

    public static final String SERVICE_STATISTICS_PULLER = "SVC_STATS_PULLER";
    public static final String SERVER_STATISTICS_PULLER = "SVR_STATS_PULLER";
    public static final String OPERTION_STATISTICS_PULLER = "OP_STATS_PULLER";
    private static Map<Integer, Map<String, DataPuller>> pullers = new ConcurrentHashMap<Integer, Map<String, DataPuller>>();

    public static DataPuller getDataPuller(ServerDO server, String type) {
        DataPuller puller;
        Map<String, DataPuller> serverPullers = pullers.get(server.getId());

        //if a puller of the particular type is available for the particular server then return it.
        //else create a new serverPullers map and add the new puller to it.
        if (serverPullers != null) {
            puller = serverPullers.get(type);
            if (puller != null) return puller;
        } else {
            serverPullers = new ConcurrentHashMap<String, DataPuller>();
            pullers.put(server.getId(), serverPullers);
        }

        if (type.compareTo(SERVICE_STATISTICS_PULLER) == 0) {
            puller = new ServiceStatisticsDataPuller(server);
        } else if (type.compareTo(SERVER_STATISTICS_PULLER) == 0) {
            puller = new ServerStatisticsDataPuller(server);
        } else if (type.compareTo(OPERTION_STATISTICS_PULLER) == 0) {
            //no backoff at op level
            puller = new OperationStatisticsDataPuller(server);
        } else {
            throw new RuntimeException("unexpected puller type");
        }


        //the creation of a serverPuller Map for a particular server is ensured above. so just have to add it.
        serverPullers.put(type, puller);

        return puller;
    }

}
