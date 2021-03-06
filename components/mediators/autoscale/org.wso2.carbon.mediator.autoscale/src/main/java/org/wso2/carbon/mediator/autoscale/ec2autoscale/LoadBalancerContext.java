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

/**
 * Contextual information related to the load balancer cluster
 */
public class LoadBalancerContext {
    private int runningInstances;
    private int pendingInstances;

    public synchronized void incrementRunningInstances() {
        this.runningInstances++;
    }

    public synchronized void incrementPendingInstances() {
        this.pendingInstances++;
    }

    public synchronized int getInstances() {
        return runningInstances + pendingInstances;
    }

    public synchronized void resetRunningPendingInstances() {
        runningInstances = 0;
        pendingInstances = 0;
    }
}
