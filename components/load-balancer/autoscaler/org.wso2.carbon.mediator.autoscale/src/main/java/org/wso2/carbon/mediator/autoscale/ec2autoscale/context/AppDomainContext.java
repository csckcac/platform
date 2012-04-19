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
package org.wso2.carbon.mediator.autoscale.ec2autoscale.context;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;


/**
 * Contextual information related to autoscaling for a particular clustering domain
 */
public class AppDomainContext extends LoadBalancerContext{

//    private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;
//    private static final int IDLE_INSTANCE_RUNNING_TIME_IN_MILLIS = 58 * 60 * 1000; // 58 minutes

    /**
     * Request tokens of requests in flight
     * <p/>
     * Key - request token ID, Value - message received time
     */
    private Map<String, Long> requestTokens = new ConcurrentHashMap<String, Long>();
    private List<Integer> requestTokenListLengths;
    // private List<Instance> runningInstanceList = new ArrayList<Instance>();
//    private int pendingInstances;
//    private int runningInstanceCount;
    private LoadBalancerConfiguration.ServiceConfiguration serviceConfig;

    public AppDomainContext(LoadBalancerConfiguration.ServiceConfiguration serviceConfig) {
        this.serviceConfig = serviceConfig;
        requestTokenListLengths = new Vector<Integer>(serviceConfig.getRoundsToAverage());
    }

    public LoadBalancerConfiguration.ServiceConfiguration getServiceConfig() {
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

//    public int getRunningInstanceCount() {
//        return super.getRunningInstanceCount();
//    }

    /**
     * This will set the running instance count for this app domain
     * and also will return the difference of current running instance count and previous count.
     * @param runningInstanceCount current running instance count
     * @return difference of current running instance count and previous count.
     */
//    public int setRunningInstanceCount(int runningInstanceCount) {
//        int diff = 0;
//        
//        if(this.runningInstanceCount < runningInstanceCount){
//            diff = runningInstanceCount - this.runningInstanceCount;
//        }
//        
//        this.runningInstanceCount = runningInstanceCount;
//        
//        return diff;
//    }

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


//    public synchronized int getPendingInstances() {
//        return pendingInstances;
//    }

//    public synchronized void incrementPendingInstances() {
//        this.pendingInstances++;
//    }

//    public synchronized void decrementPendingInstancesIfNotZero(int diff) {
//        
//        while (diff > 0 && this.pendingInstances > 0 ){
//            this.pendingInstances--;
//            diff--;
//        }
//        
//    }
    
//    public synchronized int getInstances() {
//        return runningInstanceCount + pendingInstances;
//    }

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


//    public synchronized void resetRunningPendingInstances() {
//        pendingInstances = 0;
//        runningInstanceCount = 0;
//    }
}
