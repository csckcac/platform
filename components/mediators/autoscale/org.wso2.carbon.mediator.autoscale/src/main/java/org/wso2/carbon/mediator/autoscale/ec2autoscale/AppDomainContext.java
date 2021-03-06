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
package org.wso2.carbon.mediator.autoscale.ec2autoscale;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contextual information related to autoscaling for a particular clustering domain
 */
public class AppDomainContext {

    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;
    private static final int IDLE_INSTANCE_RUNNING_TIME_IN_MILLIS = 58 * 60 * 1000; // 58 minutes

    /**
     * Request tokens of requests in flight
     * <p/>
     * Key - request token ID, Value - message received time
     */
    private Map<String, Long> requestTokens = new ConcurrentHashMap<String, Long>();
    private List<Integer> requestTokenListLengths;
    private List<Instance> runningInstanceList = new ArrayList<Instance>();
    private int pendingInstances;

    private EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig;

    public AppDomainContext(EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig) {
        this.serviceConfig = serviceConfig;
        requestTokenListLengths = new Vector<Integer>(serviceConfig.getRoundsToAverage());
    }

    public EC2LoadBalancerConfiguration.ServiceConfiguration getServiceConfig() {
        return serviceConfig;
    }

    /**
     * If there is insufficient number of messages we cannot make a scaling decision.
     *
     * @return true - if a scaling decision can be made
     */
    public boolean canMakeScalingDecision() {
        return requestTokenListLengths.size() >= serviceConfig.getRoundsToAverage();
    }

    public void addRequestToken(String tokenId) {
        requestTokens.put(tokenId, System.currentTimeMillis());
    }

    public void removeRequestToken(String tokenId) {
        requestTokens.remove(tokenId);
    }

    public void expireRequestTokens() {
        for (Map.Entry<String, Long> entry : requestTokens.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() >= serviceConfig.getMessageExpiryTime()) {
                requestTokens.remove(entry.getKey());
            }
        }
    }

    public void recordRequestTokenListLength() {
        if (requestTokenListLengths.size() >= serviceConfig.getRoundsToAverage()) {
            requestTokenListLengths.remove(0);
        }
        requestTokenListLengths.add(requestTokens.size());
    }

    public synchronized int getRunningInstances() {
        return runningInstanceList.size();
    }

    public synchronized void addRunningInstance(Instance instance) {
        runningInstanceList.add(instance);
    }

    public synchronized int getPendingInstances() {
        return pendingInstances;
    }

    public synchronized void incrementPendingInstances() {
        this.pendingInstances++;
    }

    public synchronized int getInstances() {
        return runningInstanceList.size() + pendingInstances;
    }

    public List<Instance> getRunningInstanceList() {
        return Collections.unmodifiableList(runningInstanceList);
    }

    /**
     * Get the average requests in flight, averaged over the number of  of observations rounds
     *
     * @return number of average requests in flight. -1 if there no requests were received
     */
    public int getAverageRequestsInFlight() {
        long total = 0;
        for (Integer messageQueueLength : requestTokenListLengths) {
            total += messageQueueLength;
        }
        int size = requestTokenListLengths.size();
        if (size == 0) {
            return -1; // -1 means that no requests have been received
        }
        return (int) total / size;
    }

    public Instance getRunningInstanceForTermination() {
        for (Instance instance : runningInstanceList) {
            if (canTerminateInstance(instance)) {
                return instance;
            }
        }
        return null;
    }

    public synchronized void resetRunningPendingInstances() {
        pendingInstances = 0;
        runningInstanceList.clear();
    }

    // TODO If a KILL_INSTANCE decision has been made, check whether there are pending requests from the instance to be killed
    // We should not simply terminate instances. If they have run into an hour,
    // we let it run until that hour ends, bcoz we have already been charged for that hr
    //TODO: Kill one of the newer instances

    private boolean canTerminateInstance(Instance instance) {
        boolean canTerminate = true;
        List<Tag> tags = instance.getTags();
        for (Tag tag : tags) {
            if (tag.getKey().equals(AutoscaleConstants.AVOID_TERMINATION)) {
                if (tag.getValue() == null || tag.getValue().equalsIgnoreCase("true")) {
                    canTerminate = false;
                    break;
                }
            }
        }
        if (canTerminate) {
            Date launchTime = instance.getLaunchTime();
            double runningTimeMillis = System.currentTimeMillis() - launchTime.getTime();
            canTerminate = ((runningTimeMillis % ONE_HOUR_IN_MILLIS) >= IDLE_INSTANCE_RUNNING_TIME_IN_MILLIS);
        }
        return canTerminate;
    }
}
