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
package org.wso2.carbon.url.mapper.clustermessage.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.url.mapper.clustermessage.commands.VirtualHostActivityRequest;
import org.wso2.carbon.url.mapper.clustermessage.commands.VirtualHostActivityResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualHostActivityUtil {
    private static final Log log = LogFactory.getLog(VirtualHostActivityUtil.class);

    public static Map<String, String> getActiveVirtualHostsInCluster() throws AxisFault {
        Map<String, String> urlMappings = new HashMap<String, String>();
        try {
            ClusteringAgent agent = getClusteringAgent();
            List<ClusteringCommand> list = agent.sendMessage(new VirtualHostActivityRequest(), true);
            if (log.isDebugEnabled()) {
                log.debug("sent cluster command to to get Active tenants on cluster");
            }
            for (ClusteringCommand command : list) {
                if (command instanceof VirtualHostActivityResponse) {
                    VirtualHostActivityResponse response = (VirtualHostActivityResponse) command;
                    for (Map.Entry<String, String> entry : response.getMappings().entrySet()) {
                        urlMappings.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (AxisFault f) {
            String msg = "Error in getting active tenant by cluster commands";
            log.error(msg, f);
            throw new AxisFault(msg);
        }
        return urlMappings;
    }

    private static ClusteringAgent getClusteringAgent() throws AxisFault {

        AxisConfiguration axisConfig =
                DataHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        return axisConfig.getClusteringAgent();
    }



}
