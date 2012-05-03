/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.autoscaler.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.IAutoscalerService;
import org.wso2.carbon.autoscaler.service.adapters.Adapter;
import org.wso2.carbon.autoscaler.service.adapters.LXCAdapter;
import org.wso2.carbon.autoscaler.service.adapters.EC2Adapter;
import org.wso2.carbon.autoscaler.service.exception.NoInstanceFoundException;
import org.wso2.carbon.autoscaler.service.util.Policy;
import org.wso2.carbon.autoscaler.service.xml.AutoscalerPolicyFileReader;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;

/**
 * AutoScaler task should communicate with AutoscalerService, when it decides to scale up
 * or down.
 * @scr.component name="org.wso2.carbon.autoscaler.service" immediate="true"
 * @scr.service value="org.wso2.carbon.autoscaler.service.IAutoscalerService"
 *
 */
public class AutoscalerServiceImpl implements IAutoscalerService{
	
	private static final Log log = LogFactory.getLog(AutoscalerServiceImpl.class);
	
	
	/**
     * Key - InstanceId, Value - Adapter Name
     */
    private Map<String, String> instanceIdToAdapterMap = new HashMap<String, String>();
    
    /**
     * Key - domain name, Value - list of instance IDs
     */
    private Map<String, ArrayList<String>> domainToInstanceIdsMap = 
            new HashMap<String, ArrayList<String>>();
    
    /**
     * Key - domain name, Value - pending instance count
     * TODO persist this too
     */
    private Map<String, Integer> domainToPendingInstanceCountMap = 
            new HashMap<String, Integer>();
    
    /**
     * Keeps a JVMAdapter instance.
     */
    private LXCAdapter containerAdapter = new LXCAdapter();

    /**
     * Keeps a EC2Adapter instance.
     */
    private EC2Adapter ec2Adapter = new EC2Adapter();

    /**
     * Specify all available adapters here.
     */
    private Adapter[] adapters = new Adapter[]{containerAdapter, ec2Adapter};
    
    /**
     * To read autoscaler-policy.xml file.
     */
    private AutoscalerPolicyFileReader policyReader;
    
    /**
     * Autoscaler policy object to be used.
     */
    private Policy autoscalerPolicy;
    

    /**
     * Within constructor, we read the policy file and loads the {@link #autoscalerPolicy} object.
     */
    public AutoscalerServiceImpl() {

        // load policy configurations
        try {
            AgentPersistenceManager agentPersistenceManager
                    = AgentPersistenceManager.getPersistenceManager();
            instanceIdToAdapterMap = agentPersistenceManager.retrieveInstanceIdToAdapterMap();
            domainToInstanceIdsMap = agentPersistenceManager.retrieveDomainToInstanceIdsMap();
            policyReader = new AutoscalerPolicyFileReader();
            autoscalerPolicy = policyReader.getPolicy();

        } catch (Exception e) {
            log.warn("Using default policy configurations....");
            // if an exception occurred when reading policy file use default policy
            autoscalerPolicy = new Policy();

        }

    }
//
//    public static void main(String[] args) {
//        AutoscalerServiceImpl autoscalerService = new AutoscalerServiceImpl();
//        try {
//            autoscalerService.startInstance("wso2.as.domain");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (SQLException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
    
    /**
     * Algorithm:
     * 
     * We follow the scale up order specified in {@link #autoscalerPolicy}.
     * If we found a valid adapter we ask that adapter to spawn an instance with a unique id.
     * If instance get successfully spawned, we add that instance id specific details in the
     * {@link #domainToInstanceIdsMap} and {@link #instanceIdToAdapterMap}.
     * If failed we try to spawn an instance in the adapter next in line of the scale up order.
     */
    public boolean startInstance(String domainName) throws ClassNotFoundException, SQLException {
  
        boolean isSuccessfullyStarted = false;

        // grabs the scale up order
        List<String> scaleUpOrder = autoscalerPolicy.getScaleUpOrderList();

        /**
         * keeps a unique id
         */
        String instanceId = generateInstanceId();

        // go in the order of scale up policy
        for (String adapter : scaleUpOrder) {

            Adapter anAdapter;

            if ((anAdapter = findAdapter(adapter)) != null) {

                // try to spawn an instance in this Adapter
                isSuccessfullyStarted = anAdapter.spawnInstance(domainName, instanceId);

                // if successful
                if (isSuccessfullyStarted) {

                    // adds to instanceId to adapter name map
                    instanceIdToAdapterMap.put(instanceId, adapter);
                    // add to domain to instance id map
                    addToDomainToInstanceIdsMap(domainName, instanceId);
                    //add instance details to database
                    AgentPersistenceManager agentPersistenceManager
                            = AgentPersistenceManager.getPersistenceManager();
                    agentPersistenceManager.addInstance(instanceId, adapter, domainName);
                    return true;
                }

                // if failed try on other adapters, added following line just for clarity!
                continue;

            } else {
                // when we found an adapter name, which we don't aware of!
                log.warn("Invalid Adapter entry in autoscaler policy's scale up order: " +adapter+
                    ".");
            }

        }

        return false;

    }
    
    /**
     * Algorithm: 
     * 
     * First we get the list of instanceIds which are spawned for this domain by querying
     * {@link #domainToInstanceIdsMap}. 
     * Next we iterate through adapters in scale down order of the {@link #autoscalerPolicy}.
     * Then for each adapter we try to find an instance id which had spawned through it using
     * {@link #instanceIdToAdapterMap}.
     * After we found an instanceId-adapter pair, we checks whether this adapter has more than
     * minimum number of required instances specified in the {@link #autoscalerPolicy}. 
     * If so we ask that particular adapter to terminate the found instance. 
     * If instance is successfully get terminated, we remove details corresponds to that
     * instanceId from {@link #domainToInstanceIdsMap} and {@link #instanceIdToAdapterMap}.  
     */
    public boolean terminateInstance(String domainName) throws NoInstanceFoundException {

        List<String> scaleDownOrder = autoscalerPolicy.getScaleDownOrderList();

        Map<Integer, Integer> scaleDownOrderIdToMinInstanceCountMap =
            autoscalerPolicy.getScaleDownOrderIdToMinInstanceCountMap();

        ArrayList<String> instanceIdsList;
        boolean isSuccessfullyTerminated = false;

        // gets the list of instance ids which are spawned for this domain
        if (domainToInstanceIdsMap.containsKey(domainName) &&
            ((instanceIdsList = domainToInstanceIdsMap.get(domainName)).size() > 0)) {

            // follow the scaleDown order
            for (String adapter : scaleDownOrder) {

                // for each instance id get the Adaptor name
                for (String instanceId : instanceIdsList) {

                    String adapterName = instanceIdToAdapterMap.get(instanceId);

                    // is accordance to the order?
                    if (adapterName.equalsIgnoreCase(adapter)) {

                        Adapter anAdapter;
                        int minInstanceCount =
                            scaleDownOrderIdToMinInstanceCountMap.get(
                                                       scaleDownOrder.indexOf(adapter));

                        // to be safe
                        if ((anAdapter = findAdapter(adapterName)) != null &&
                            anAdapter.getRunningInstanceCount() > minInstanceCount) {

                            // terminate it!
                            isSuccessfullyTerminated = anAdapter.terminateInstance(instanceId);

                            if (isSuccessfullyTerminated) {
                                // remove this instance id from Maps
                                instanceIdToAdapterMap.remove(instanceId);
                                removeFromDomainToInstanceIdsMap(domainName, instanceId);

                                return true;
                            }
                        }
                        else{
                            log.debug(
                                "Termination of an instance belongs to '"+domainName+"' failed.\n"+
                                "Adapter according to the policy: "+adapterName+"" +
                                "Minimum instance count specified: "+minInstanceCount+".");
                        }

                    }
                }
            }

        } else {
            String msg =
                "No instance of the domain '" + domainName + "' had spawned." +
                    " Thus failed to terminate.";
            log.error(msg);
            throw new NoInstanceFoundException(msg);
        }

        return false;
    }

    /**
     * Searches for the given adapter name, in the configured {@link #adapters} list.
     * @param adapterName name of the Adapter
     * @return found Adapter object or null if not found.
     */
    private Adapter findAdapter(String adapterName) {

        for (Adapter adapterX : adapters) {
            if (adapterX.getName().equalsIgnoreCase(adapterName)) {
                return adapterX;
            }
        }

        log.error("No Adapter called '"+adapterName+"' can be found!");
        return null;
    }

    /**
     * Add to {@link #domainToInstanceIdsMap}.
     * @param domainName 
     * @param instanceId
     */
    private void addToDomainToInstanceIdsMap(String domainName, String instanceId) {

        ArrayList<String> instances;

        if (domainToInstanceIdsMap.containsKey(domainName)) {
            instances = domainToInstanceIdsMap.get(domainName);
        } else {
            instances = new ArrayList<String>();
        }

        instances.add(instanceId);
        domainToInstanceIdsMap.put(domainName, instances);
    }

    /**
     * Remove an instanceId from {@link #domainToInstanceIdsMap}, if and only if domainName is
     * already there in the map.
     * @param domainName
     * @param instanceId
     */
    private void removeFromDomainToInstanceIdsMap(String domainName, String instanceId) {

        ArrayList<String> instances;

        if (domainToInstanceIdsMap.containsKey(domainName)) {
            instances = domainToInstanceIdsMap.get(domainName);
            instances.remove(instanceId);
            domainToInstanceIdsMap.put(domainName, instances);
        }

    }
    
    /**
     * This will generate a unique ID
     * @return unique ID
     */
    private String generateInstanceId() {
        UUID uniqueId = UUID.randomUUID();
        return uniqueId.toString();
    }

    public int getPendingInstanceCount(String domainName) {
        
        if(domainToPendingInstanceCountMap.containsKey(domainName)){
            
            if(domainToPendingInstanceCountMap.get(domainName) > 0){
                return domainToPendingInstanceCountMap.get(domainName);
            }
            
        }
        
        return 0;
    }

    public void addPendingInstanceCount(String domainName, int count) {

        int currentVal = 0;
        
        if(domainToPendingInstanceCountMap.containsKey(domainName)){
            
            currentVal = domainToPendingInstanceCountMap.get(domainName);
             
        }
        
        domainToPendingInstanceCountMap.put(domainName, currentVal+count);
    }

    
}
