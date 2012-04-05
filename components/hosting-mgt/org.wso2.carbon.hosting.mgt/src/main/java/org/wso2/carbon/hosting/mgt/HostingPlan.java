/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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


package org.wso2.carbon.hosting.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

/**
 * This class will handle the Resource plan related stuff
 */

public class HostingPlan {

    private static final Log log = LogFactory.getLog(HostingPlan.class);

   /**
    * This method will add a physical machine to the registry
    * @param registry
    * @param zone zone in which machine is physically created
    * @param machineName
    * @return
    * @throws Exception
    */
    public void addMachine (WSRegistryServiceClient registry,String zone, String machineName)
            throws ResourcesException {
    }

    /**
     * This method will add a bridge to a machine
     * @param registry
     * @param zone zone in which machine is physically created
     * @param machineName
     * @param bridge
     * @return
     * @throws Exception
     */
    public void addBridge (WSRegistryServiceClient registry,String zone, String machineName, String bridge)
            throws ResourcesException {
    }

    /**
     * This method will add an ip to a bridge
     * @param registry
     * @param zone zone in which machine is physically created
     * @param machineName
     * @param bridge
     * @return
     * @throws Exception
     */
    public void addIp (WSRegistryServiceClient registry, String zone, String machineName, String bridge, String ip)
            throws ResourcesException {
    }

    /**
     * This method will add resource allocation plan specific to the zone, in to the registry
     * @param registry
     * @param zone zone in which machine is physically created
     * @return
     * @throws Exception
     */
    public void addZoneResourcePlan (WSRegistryServiceClient registry,String zone,
        String memory, String swap, String storage, String cpuShares, String cpuSetShares)
            throws ResourcesException {
    }

}

