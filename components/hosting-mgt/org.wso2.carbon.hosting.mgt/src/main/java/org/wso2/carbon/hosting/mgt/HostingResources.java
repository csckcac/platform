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
import org.wso2.carbon.hosting.mgt.dto.Container;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.util.*;

/**
 * This class handle the registry related functionality for the Stratos Hosting deployments.
 */

public class HostingResources {
	
    private static final Log log = LogFactory.getLog(HostingResources.class);


    /**
     * This method will retrieve resource allocation plan specific to the zone from the registry
     * @param registry
     * @param zone zone in which machine is physically created
     * @return
     * @throws Exception
     */
    private Map<Object, String> getZoneResourcePlan(WSRegistryServiceClient registry, String zone)
            throws Exception {
        Map<Object, String> resMap = new HashMap<Object, String>();
        Resource containerResource = null;

        try {
            if(registry == null) {
                registry = ResourcesUtils.initializeRegistry();
            }
            String zonePath = ResourcesConstants.ZONE_PATH.concat(File.separator).concat(zone).concat(File.separator).
                    concat(ResourcesConstants.ZONE_PLAN);
            Resource zonePlan = registry.get(zonePath);
            Properties plan = zonePlan.getProperties();
            Set keySet = plan.keySet();
            Iterator itr = keySet.iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if(key != null) {
                    String value = zonePlan.getProperty(key);
                    resMap.put(key, value);
                }
            }

        }catch (Exception e) {
            String msg = "Resource allocation plan for zone " + zone + " could not be retrieved from the Registry";
            throw new ResourcesException( msg + e.getMessage());
        }

        return resMap;
    }
    /**
     * This method will be called from the front end apps to create a container for the tenant user. Note that call to
     * this method does not create the container physically. It will only register the necessary information to
     * create a container for the tenant, into the registry.
     * When the tenant first upload his applications into the container, this information is retrieved from the registry
     * and the container will actually be created.
     *
     * Container will be stored as a collection. We store container resources
     * such as memory, cpu as properties of this collection resource.
     * @param containerName name of the container. This actually the user id of the tenant user
     * @param zone zone in which machine is physically created
     * @param template Template from which the machine is created
     * @throws Exception
     */
    public void registerContainer(String containerName, String zone, String template)
            throws ResourcesException {
        WSRegistryServiceClient registry = null;
        Resource containerResource = null;
        String ip = null;
        try {
            String containerPath = ResourcesConstants.CONTAINER_PATH + File.separator + containerName;
            Map<Object,String> regMap = null;
            Map<Object,String> resMap = null;
            if(registry == null)
            {
                registry = ResourcesUtils.initializeRegistry();
            }

            if(registry.resourceExists(containerPath))
            {
                log.debug("Container Resource " + containerPath + " already exists");
                containerResource = registry.get(containerPath);
                if(containerResource == null) {
                    String msg = "Container path " + containerPath +
                        " exists, but could not retrieve container";
                    throw new ResourcesException(msg);
                }
                String statusProperty = containerResource.getProperty(ResourcesConstants.AVAILABLE);
                if(statusProperty == null || statusProperty.isEmpty() || statusProperty.equals("false")) {
                    unregisterContainer(registry, containerName);
                    containerResource = null;
                }
                else {
                    String msg = "Container already exist and active !!!";
                    log.debug(msg);
                    throw new ResourcesException(msg);
                }
            }
            if(containerResource == null) {
                regMap = retrieveAvailableIp(registry, zone);
                ip = regMap.get("ip");
                resMap = getZoneResourcePlan(registry, zone);
                containerResource = registry.newCollection();
                /* Get machine information */
                containerResource.addProperty(ResourcesConstants.CONTAINER_NAME, containerName);
                containerResource.addProperty(ResourcesConstants.CONTAINER_TEMPLATE, template);
                containerResource.addProperty(ResourcesConstants.CONTAINER_ZONE, zone);
                containerResource.addProperty(ResourcesConstants.CONTAINER_WORKER_NODE,
                        regMap.get(ResourcesConstants.CONTAINER_WORKER_NODE));
                containerResource.addProperty(ResourcesConstants.CONTAINER_IP,
                        regMap.get(ResourcesConstants.CONTAINER_IP));

                containerResource.addProperty(ResourcesConstants.CONTAINER_BRIDGE,
                        regMap.get(ResourcesConstants.CONTAINER_BRIDGE));
                containerResource.addProperty(ResourcesConstants.CONTAINER_GATEWAY,
                        resMap.get(ResourcesConstants.CONTAINER_GATEWAY));
                containerResource.addProperty(ResourcesConstants.CONTAINER_NET_MASK,
                        resMap.get(ResourcesConstants.CONTAINER_NET_MASK));
                containerResource.addProperty(ResourcesConstants.CONTAINER_ROOT,
                        resMap.get(ResourcesConstants.CONTAINER_ROOT));

                /* Get resource information */
                containerResource.addProperty(ResourcesConstants.CONTAINER_MEMORY,
                        resMap.get(ResourcesConstants.CONTAINER_MEMORY));
                containerResource.addProperty(ResourcesConstants.CONTAINER_SWAP,
                        resMap.get(ResourcesConstants.CONTAINER_SWAP));
                containerResource.addProperty(ResourcesConstants.CONTAINER_STORAGE,
                        resMap.get(ResourcesConstants.CONTAINER_STORAGE));
                containerResource.addProperty(ResourcesConstants.CONTAINER_CPU_SHARES,
                        resMap.get(ResourcesConstants.CONTAINER_CPU_SHARES));
                containerResource.addProperty(ResourcesConstants.CONTAINER_CPUSET_CPUS,
                        resMap.get(ResourcesConstants.CONTAINER_CPUSET_CPUS));
                log.debug("Resource " + containerPath + " does not exist. Creating... ");
                // Create the container as a collection resource
                containerResource.addProperty(ResourcesConstants.AVAILABLE, "true");
                registry.put(containerPath, containerResource);
                log.debug("Container" + containerPath + "is published to registry");
            }
        } catch (Exception e) {
            String msg = "Container resource " + containerName + " could not be added to the Registry. ";
            throw new ResourcesException( msg + e.getMessage());
        }
    }

    /**
     * This method will remove a container resource from registry if it exists. When the container is physically removed
     * this method will be called to clear the registry entry corresponding to the container.
     * @param containerName name of the container
     * @param registry
     * @throws Exception
     */
    public void unregisterContainer(WSRegistryServiceClient registry, String containerName)
            throws ResourcesException {
		String containerPath = ResourcesConstants.CONTAINER_PATH.concat(File.separator).concat(containerName);
        
	    try {
            if(registry == null)
            {
                registry = ResourcesUtils.initializeRegistry();
            }


            if(!registry.resourceExists(containerPath))
            {
                log.debug("Resource" + containerPath + "does not exists");
            }
            else
            {
                Resource container = registry.get(containerPath);
                String ip = container.getProperty("ip");
                String bridge = container.getProperty("bridge");
                String hostName = container.getProperty("host_name");
                registry.delete(containerPath);
                String hostPath = ResourcesConstants.ZONE_PATH + File.separator + hostName;
                Resource availableIPList = registry.get(hostPath + File.separator + "bridges" + File.separator +
                    bridge + File.separator + ResourcesConstants.AVAILABLE);
                Resource usedIPList = registry.get(hostPath + File.separator + "bridges" + File.separator +
                        bridge + File.separator + ResourcesConstants.USED);
                usedIPList.removeProperty(ip);
                availableIPList.addProperty(ip, ip);

                log.debug("Container" + containerPath + "is removed from registry");
            }
        }catch(Exception e) {
            String msg = "Removing container from registry failed";
            throw new ResourcesException(msg + e.getMessage());
        }
    }
 
    /**
     * This method will retrieve container resource from registry
     * @param containerName
     * @return
     * @throws Exception
     */
    public Container retrieveContainer(String containerName) throws
    	ResourcesException {
        WSRegistryServiceClient registry = null;
    	Resource containerRes = null;
        Container container = null;
		String containerPath = ResourcesConstants.CONTAINER_PATH.concat(File.separator).concat(containerName);
        try {
            registry = ResourcesUtils.initializeRegistry();
            containerRes =  registry.get(containerPath);
            if(containerRes != null) {
                container = new Container();
                container.setContainerName(containerName);
                container.setTemplate(containerRes.getProperty(ResourcesConstants.CONTAINER_TEMPLATE));
                container.setBridge(containerRes.getProperty(ResourcesConstants.CONTAINER_BRIDGE));
                container.setIp(containerRes.getProperty(ResourcesConstants.CONTAINER_IP));

            }
        } catch(Exception e) {
            String msg = "Retrieving container from registry failed";
            throw new ResourcesException(msg + e.getMessage());
        }
			
		return container;
    }


    /**
     * This method will retrieve a container ip. It will iterate through the available machines and bridges of those
     * machines until an available ip is found. Once such ip found return the ip, the bridge that ip belong and the
     * machine that bridge belong.
     * @param registry
     * @param zone
     * @return Map map contain ip, the bridge that ip belongs, the machine that bridge belong.
     * @throws Exception
     */
    public Map<Object, String> retrieveAvailableIp (WSRegistryServiceClient registry, String zone) throws Exception {
        Map<Object,String> regMap = new HashMap<Object, String>();
    	Collection physicalMachines = null;
        String availablePhysicalMachine = null;
        String availableBridge = null;
        String availableIp = null;
    	Resource machine = null;
		String zonePath = ResourcesConstants.ZONE_PATH.concat(File.separator).concat(zone);
        String machinesPath = zonePath.concat(File.separator).concat(ResourcesConstants.MACHINES);
		
		physicalMachines = (Collection) registry.get(machinesPath);
        if(physicalMachines == null) {
            String msg = "Zone path " + zonePath +
                    " exists, but coult not retrieve machines resource";
            log.debug(msg);
            throw new ResourcesException(msg);
        }
        String[] machines = physicalMachines.getChildren();
        for(int i = 0; i < machines.length; i++) {
            String machinePath = machines[i];
            machine = registry.get(machinePath);
            if(machine == null) {
                String msg = "Machine path " + machinePath +
                        " exists, but coult not retrieve machine resource";
                log.debug(msg);
                throw new ResourcesException(msg);
            }
            String machineAvailable = machine.getProperty(ResourcesConstants.AVAILABLE);
            if(machineAvailable.equalsIgnoreCase("true")) {
                Resource bridge = null;
                availablePhysicalMachine = machinePath;
                String bridgesPath = machinePath.concat(File.separator).concat(ResourcesConstants.BRIDGES);

                Collection bridges = (Collection) registry.get(bridgesPath);
                if(bridges == null) {
                    String msg = "Could not retrieve bridges resource";
                    log.debug(msg);
                    throw new ResourcesException(msg);
                }
                String[] bridgesChildren = bridges.getChildren();
                for(int j = 0; i < bridgesChildren.length; i++) {
                    String bridgePath = bridgesChildren[j];
                    bridge = registry.get(bridgePath);
                    if(bridge == null) {
                        String msg = "Bridge path " + bridgePath +
                                " exists, but could not retrieve bridge resource";
                        log.debug(msg);
                        machine.setProperty(ResourcesConstants.AVAILABLE, "false");
                        throw new ResourcesException(msg);
                    }
                    String isBridgeAvailable = bridge.getProperty(ResourcesConstants.AVAILABLE);
                    if(isBridgeAvailable.equalsIgnoreCase("true")) {
                        /* Now look for an available ip */
                        availableBridge = bridgePath;
                        Resource availableIpList;
                        Properties ipList;

                        String availableIpListPath = bridgePath.concat(File.separator).concat(ResourcesConstants.AVAILABLE);
                        availableIpList = registry.get(availableIpListPath);
                        ipList = availableIpList.getProperties();
                        Set keySet = ipList.keySet();
                        Iterator itr = keySet.iterator();
                        while(itr.hasNext()) {
                            String key = (String) itr.next();
                            if(key != null) {
                                availableIp = availableIpList.getProperty(key);
                                regMap.put(ResourcesConstants.MACHINE_IP, availableIp);
                                /* Remove the ip from the available ip list of the bridge */
                                Resource availableIPListResource = registry.get(availableIpListPath);
                                availableIPListResource.removeProperty(key);
                                break; // Found an ip
                            }
                        }
                        if(availableIp == null) {
                            availableBridge = null;
                            bridge.setProperty(ResourcesConstants.AVAILABLE, "false");
                            continue; // Look in the next bridge
                        }
                        regMap.put(ResourcesConstants.MACHINE_BRIDGE, availableBridge);
                        break; // Found an available ip, so no more iteration of bridges
                    } else {
                        continue; // Look in the next bridge
                    }
                } // End of bridge loop
                if(availableBridge == null) {
                    availablePhysicalMachine = null;
                    machine.setProperty(ResourcesConstants.AVAILABLE, "false");
                    continue; // Look in the next machine
                }
                regMap.put(ResourcesConstants.MACHINE_NAME, availablePhysicalMachine);
                break; // Found an available ip, so no more iteration of machines
            }
        } // End of machine loop
        if(availableIp == null) {
            String msg = "No ip is available in the all the machines in the zone";
            throw new ResourcesException(msg);
        }

        return regMap;
    }

    public void changeContainerStatus (String containerName, String status) throws ResourcesException {
        WSRegistryServiceClient registry = null;
        String containerPath = ResourcesConstants.CONTAINER_PATH + containerName;
        Resource container = null;
        try {
            if(registry == null)
            {
                registry = ResourcesUtils.initializeRegistry();
            }
            container =  registry.get(containerPath);
            container.setProperty(ResourcesConstants.AVAILABLE, status);
        } catch(Exception e) {
            String msg = "Retrieving container from registry failed";
            throw new ResourcesException(msg + e.getMessage());
        }
    }
}

