/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.multiple.instance.endpoint.mgt.autoscale;

import org.wso2.carbon.utils.CarbonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.SynapseException;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.startup.Task;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.clustering.ClusteringFault;

import java.util.*;

/*
  This class is the Task Class which runs time to time to keep the required
  number of instance as a constant
*/
public class LocalLoadAnalyzerTask implements Task, ManagedLifecycle {

    private static final Log log = LogFactory.getLog(LocalInstanceManager.class);
    private static LocalInstanceManager imanager;
    private static boolean initialized = false;

    /**
     * Task initialization parameters
     */

    private String pLoadBalancerGroup = "default";
    private String pAvailabilityZone = "us-east-1b";
    private long pMessageExpiryTime = -1;
    private int pMinLoadBalancerInstances = 2;
    private int pMinAppInstances = 0;
    private int pMaxAppInstances = 1;
    private int pQueueLengthPerNode = 10; // concurrentProcessingCountPerNode
    private int pRoundsToAverage = 10; // computationWindowSize
    private int pInstancesPerScaleUp = 1;
    Map runInstanceData = new HashMap();

    public boolean doSanityCheck() {
        // Check the min number of load balancer instances
        int currentLoadBalancerInstances = getRunningInstances();
        if (currentLoadBalancerInstances < getPMinAppInstances()) {
            log.warn("Sanity check failed. Min Application Instances is: " + currentLoadBalancerInstances +
                    ". Specified Min Application Instances is: " + getPMinAppInstances());
            int diff = getPMinAppInstances() - currentLoadBalancerInstances;

            // Launch diff number of LB instances
            log.info("Launching " + diff + " Application instances");
            try {
                for (int i = 0; i < diff; i++) {
                    imanager.start();
                }
            } catch (Exception e) {
                log.error("Failed to start the required number of instances", e);
            }
            return false;
        }
        return true;
    }

    public void execute() {
        /* Overwriting the execute method to tell not to call for the instances we are
            by checking the system property
         */
        if (CarbonUtils.isMultipleInstanceCase()) {
            /* since we do not expect the normal auto-scaling with multiple-instance case we just use to start n
            number of instances and keep it as a constant  so we do not use super.execute here*/
            //            super.execute();
            doSanityCheck();
        }
    }

    private synchronized int getRunningInstances() {
        Map instanceList = imanager.getInstances();
        Iterator it = instanceList.keySet().iterator();
        int noOfProcesses = 0;
        while (it.hasNext()) {
            String key = (String) it.next();
            Process process = ((LocalInstance) instanceList.get(key)).getJavaprocess();
            try {
                // Test whether this process has been killed or not///
                int exit = process.exitValue();
                // If exception not thrown then process has been killed so remove the process from the Map
                if (exit != 1) {
                    /* Exit value = 1 means there's an exception during startup and this doesn't cause startup to be interrupted */
                    log.info("1 Instance has been killed due to some reason and another Instance will start soon. Exit Value :" + exit);
                    it.remove();
                } else {
                    noOfProcesses++;
                }
            } catch (IllegalThreadStateException e) {
                /* IllegalThreadStateException thrown when the started process is already running,
                 so we detect nothing went wrong with this process*/
                noOfProcesses++;
            }
        }
        return noOfProcesses;
    }

    public List<LocalInstance> getRunningApplicationInstances() throws Exception {
        // By calling this method it leads to remove the processes which has been killed and always get  the latest state
        getRunningInstances();
        return new ArrayList<LocalInstance>(imanager.getInstances().values());
    }

    public List<LocalInstance> getPendingApplicationInstances() throws Exception {
        // Since there are not pending instances in this scenario we are return empty List
        return new ArrayList<LocalInstance>();
    }

    public void init(SynapseEnvironment synEnv) {
        /*Only if the instance.value system property has set we do the Initializing */
        if (CarbonUtils.isMultipleInstanceCase()) {
            log.debug("Initializing LocalLoadAnalyzer Task ...");
            setMinAppInstances(System.getProperty("instances.value"));
            if (!initialized) {
                imanager = new LocalInstanceManager();
                imanager.setInstances(new HashMap());
                // During the initialization process H2 Server is starting
                initialized = true;
            }
        }
    }

    public void destroy() {
        /*Only if the instance.value system property has set we do the Initializing */
        if (CarbonUtils.isMultipleInstanceCase()) {
            /* do nothing in destroy method */
        }
    }
    public int getPMinAppInstances() {
        return pMinAppInstances;
    }

    public static LocalInstanceManager getImanager() {
        return imanager;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public String getPLoadBalancerGroup() {
        return pLoadBalancerGroup;
    }

    public String getPAvailabilityZone() {
        return pAvailabilityZone;
    }

    public long getPMessageExpiryTime() {
        return pMessageExpiryTime;
    }

    public int getPMinLoadBalancerInstances() {
        return pMinLoadBalancerInstances;
    }

    public int getPMaxAppInstances() {
        return pMaxAppInstances;
    }

    public int getPQueueLengthPerNode() {
        return pQueueLengthPerNode;
    }

    public int getPRoundsToAverage() {
        return pRoundsToAverage;
    }

    public int getPInstancesPerScaleUp() {
        return pInstancesPerScaleUp;
    }

    public Map getRunInstanceData() {
        return runInstanceData;
    }

    public static void setImanager(LocalInstanceManager imanager) {
        LocalLoadAnalyzerTask.imanager = imanager;
    }

    public static void setInitialized(boolean initialized) {
        LocalLoadAnalyzerTask.initialized = initialized;
    }

    public void setPLoadBalancerGroup(String pLoadBalancerGroup) {
        this.pLoadBalancerGroup = pLoadBalancerGroup;
    }

    public void setPAvailabilityZone(String pAvailabilityZone) {
        this.pAvailabilityZone = pAvailabilityZone;
    }

    public void setPMessageExpiryTime(long pMessageExpiryTime) {
        this.pMessageExpiryTime = pMessageExpiryTime;
    }

    public void setPMinLoadBalancerInstances(int pMinLoadBalancerInstances) {
        this.pMinLoadBalancerInstances = pMinLoadBalancerInstances;
    }

    public void setPMinAppInstances(int pMinAppInstances) {
        this.pMinAppInstances = pMinAppInstances;
    }

    public void setPMaxAppInstances(int pMaxAppInstances) {
        this.pMaxAppInstances = pMaxAppInstances;
    }

    public void setPQueueLengthPerNode(int pQueueLengthPerNode) {
        this.pQueueLengthPerNode = pQueueLengthPerNode;
    }

    public void setPRoundsToAverage(int pRoundsToAverage) {
        this.pRoundsToAverage = pRoundsToAverage;
    }

    public void setPInstancesPerScaleUp(int pInstancesPerScaleUp) {
        this.pInstancesPerScaleUp = pInstancesPerScaleUp;
    }

    public void setRunInstanceData(Map runInstanceData) {
        this.runInstanceData = runInstanceData;
    }
    public void setMinLoadBalancerInstances(String minInstances) {
        int iMinInstances = Integer.parseInt(minInstances);
        if (iMinInstances < 1) {
            handleException("minLoadBalancerInstances in the LoadAnalyzerTask configuration should be at least 1");
        }
        this.pMinLoadBalancerInstances = iMinInstances;
    }

    public void setMinAppInstances(String minInstances) {
        int iMinInstances = Integer.parseInt(minInstances);
        if (iMinInstances < 1) {
            handleException("minAppInstances in the LoadAnalyzerTask configuration should be at least 1");
        }
        this.pMinAppInstances = iMinInstances;
    }

    public void setMaxAppInstances(String maxInstances) {
        int iMaxInstances = Integer.parseInt(maxInstances);
        if (iMaxInstances < 1) {
            handleException("maxAppInstances in the LoadAnalyzerTask configuration should be at least 1");
        }
        this.pMaxAppInstances = iMaxInstances;
    }
    public void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

}
