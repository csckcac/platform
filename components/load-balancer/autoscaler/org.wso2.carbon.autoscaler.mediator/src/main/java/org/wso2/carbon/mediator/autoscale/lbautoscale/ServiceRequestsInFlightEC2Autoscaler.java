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
package org.wso2.carbon.mediator.autoscale.lbautoscale;

import com.amazonaws.services.ec2.model.*;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load analyzer task for Stratos service level autoscaling
 */
@SuppressWarnings("unused")
public class ServiceRequestsInFlightEC2Autoscaler implements Task, ManagedLifecycle {

    private static final Log log = LogFactory.getLog(ServiceRequestsInFlightEC2Autoscaler.class);

    private EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();

    // **************** Task Config Parameters **************************

    public void setConfiguration(String configURL) {
        ec2LBConfig.init(configURL);
    }
    // *******************************************************************

    /**
     * AppDomainContexts for each domain
     * Key - domain
     */
    private Map<String, AppDomainContext> appDomainContexts = new HashMap<String, AppDomainContext>();

    /**
     * LB Context for LB cluster
     */
    private LoadBalancerContext lbContext = new LoadBalancerContext();

    private EC2InstanceManager ec2;

    /**
     * Attribute to keep track whether this instance is the primary load balancer.
     * <p/>
     * A primary autoscaler does not have to check more than once whether the Elastic IP has been
     * assigned to itself. However, the secondary autoscalers need to check on this, to make sure
     * that the primary autoscaler has not crashed.
     */
    private boolean isPrimaryLoadBalancer;

    /**
     * Keeps track whether this task is still running
     */
    private boolean isTaskRunning;

    public void init(SynapseEnvironment synEnv) {
        ConfigurationContext configCtx = (ConfigurationContext) synEnv.getServerContextInformation().getServerContext();
        appDomainContexts = AutoscaleUtil.getAppDomainContexts(configCtx, ec2LBConfig);
        ec2 = AutoscaleUtil.createEC2InstanceManager(ec2LBConfig.getEc2AccessKey(),
                                                     ec2LBConfig.getEc2PrivateKey(),
                                                     ec2LBConfig.getInstanceMgtEPR());
        log.info("Initialized autoscaler task");
    }

    /**
     * This is method that gets called periodically when the task runs.
     * <p/>
     * The exact sequence of execution is shown in this method.
     */
    public void execute() {
        if (isTaskRunning) {
            return;
        }
        try {
            isTaskRunning = true;
            sanityCheck();
            if (!isPrimaryLoadBalancer) {
                return;
            }
            autoscale();
        } finally {
            isTaskRunning = false;
        }
    }

    /**
     * This method makes sure that the minimum configuration of the clusters in the system is
     * maintained
     */
    private void sanityCheck() {
        nonPrimaryLBSanityCheck();
        if (!isPrimaryLoadBalancer) {
            return;
        }
        computeRunningAndPendingInstances();
        loadBalancerSanityCheck();
        appNodesSanityCheck();
    }

    /**
     * We compute the running & pending instances for the entire system using a single EC2 API
     * call since we want to reduce the number of EC2 API calls. This is because it seems that
     * AWS throttles the number of requests you can make in a given time
     */
    private void computeRunningAndPendingInstances() {
        EC2LoadBalancerConfiguration.LBConfiguration lbConfig = ec2LBConfig.getLoadBalancerConfig();
        String[] serviceDomains = ec2LBConfig.getServiceDomains();
        List<Reservation> reservations = ec2.describeInstances();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                List<GroupIdentifier> securityGroups = instance.getSecurityGroups();
                String instanceState = instance.getState().getName();
                if (AutoscaleUtil.areEqual(securityGroups, lbConfig.getSecurityGroups())) {
                    if (instanceState.equals(AutoscaleConstants.InstanceState.RUNNING.getState())) {
                        lbContext.incrementRunningInstances();
                    } else if (instanceState.equals(AutoscaleConstants.InstanceState.PENDING.getState())) {
                        lbContext.incrementPendingInstances();
                    }
                    continue;
                }
                for (String serviceDomain : serviceDomains) {
                    EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig =
                            ec2LBConfig.getServiceConfig(serviceDomain);
                    if (AutoscaleUtil.areEqual(securityGroups, serviceConfig.getSecurityGroups())) {
                        if (instanceState.equals(AutoscaleConstants.InstanceState.RUNNING.getState())) {
                            appDomainContexts.get(serviceDomain).addRunningInstance(instance);
                        } else if (instanceState.equals(AutoscaleConstants.InstanceState.PENDING.getState())) {
                            appDomainContexts.get(serviceDomain).incrementPendingInstances();
                        }
                    }
                }
            }
        }
    }

    /**
     * Sanity check to see whether the number of LBs is the number specified in the LB config
     */
    private void loadBalancerSanityCheck() {
        int currentLBInstances = lbContext.getInstances();
        EC2LoadBalancerConfiguration.LBConfiguration lbConfig = ec2LBConfig.getLoadBalancerConfig();
        int requiredInstances = lbConfig.getInstances();
        if (currentLBInstances < requiredInstances) {
            log.warn("LB Sanity check failed. Current LB instances: " + currentLBInstances +
                     ". Required LB instances is: " + requiredInstances);
            int diff = requiredInstances - currentLBInstances;

            // Launch diff number of LB instances
            log.info("Launching " + diff + " LB instances");
            runInstances(lbConfig, diff);
            lbContext.resetRunningPendingInstances();
        }
    }

    /**
     * This sanity check is run only by non-primary LBs.
     * This method assigns the elastic IP to this instance, if not already assigned.
     * The primary LB will do this once. The secondary LBs will check this from time to time, to see
     * whether the primary LB is still running
     */
    private void nonPrimaryLBSanityCheck() {
        if (!isPrimaryLoadBalancer) {
            String elasticIP = ec2LBConfig.getLoadBalancerConfig().getElasticIP();
            Address address = ec2.describeAddress(elasticIP);
            if (address == null) {
                AutoscaleUtil.handleException("Elastic IP address " + elasticIP +
                                              " has  not been reserved");
                return;
            }
            String localInstanceId = System.getenv("instance_id");
            String elasticIPInstanceId = address.getInstanceId();
            if (elasticIPInstanceId == null || elasticIPInstanceId.isEmpty()) {
                ec2.associateAddress(localInstanceId, elasticIP);
                isPrimaryLoadBalancer = true;
                log.info("Associated Elastic IP " + elasticIP + " with local instance " + localInstanceId);
            } else if (elasticIPInstanceId.equals(localInstanceId)) {
                isPrimaryLoadBalancer = true;  // If the Elastic IP is assigned to this instance, it is the primary LB
            }
        }
    }

    private boolean isInstanceRunningOrPending(Instance instance) {
        return instance.getState().getName().equals(AutoscaleConstants.InstanceState.RUNNING.getState()) ||
               instance.getState().getName().equals(AutoscaleConstants.InstanceState.PENDING.getState());
    }

    /**
     * Check that all app nodes in all clusters meet the minimum configuration
     */
    private void appNodesSanityCheck() {
        String[] serviceDomains = ec2LBConfig.getServiceDomains();
        for (String serviceDomain : serviceDomains) {
            appNodesSanityCheck(serviceDomain);
        }
    }

    private void appNodesSanityCheck(String serviceDomain) {
        AppDomainContext appDomainContext = appDomainContexts.get(serviceDomain);
        int currentInstances = appDomainContext.getInstances();
        EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig = ec2LBConfig.getServiceConfig(serviceDomain);
        int requiredInstances = serviceConfig.getMinAppInstances();
        if (currentInstances < requiredInstances) {
            log.warn("App domain Sanity check failed for [" + serviceDomain + "] . Current instances: " +
                     currentInstances + ". Require instances is: " + requiredInstances);
            int diff = requiredInstances - currentInstances;

            // Launch diff number of App instances
            log.info("Launching " + diff + " App instances for domain " + serviceDomain);
            runInstances(serviceConfig, serviceConfig.getInstancesPerScaleUp());
            appDomainContext.resetRunningPendingInstances();
        }
    }

    /**
     * Autoscale the entire system, analyzing the load of each domain
     */
    private void autoscale() {
        String[] serviceDomains = ec2LBConfig.getServiceDomains();
        for (String serviceDomain : serviceDomains) {
            expireRequestTokens(serviceDomain);
            autoscale(serviceDomain);
        }
    }

    /**
     * This method contains the autoscaling algorithm for requests in flight based autoscaling
     *
     * @param serviceDomain service clustering domain
     */
    private void autoscale(String serviceDomain) {
        AppDomainContext appDomainContext = appDomainContexts.get(serviceDomain);
        EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig = appDomainContext.getServiceConfig();

        appDomainContext.recordRequestTokenListLength();
        if (!appDomainContext.canMakeScalingDecision()) {
            return;
        }

        long average = appDomainContext.getAverageRequestsInFlight();
        int runningAppInstances = appDomainContext.getRunningInstances();

        int queueLengthPerNode = serviceConfig.getQueueLengthPerNode();
        if (average > (runningAppInstances * queueLengthPerNode)) {
            // current average is high than that can be handled by current nodes.
            scaleUp(serviceDomain);
        } else if (average < ((runningAppInstances - 1) * queueLengthPerNode)) {
            // current average is less than that can be handled by (current nodes - 1).
            scaleDown(serviceDomain);
        }
        appDomainContext.resetRunningPendingInstances();
    }

    /**
     * Scales up the system when the request count is high in the queue
     * Handle scale-up, if the number of running applications is less than the allowed maximum and
     * if there are no instances pending startup
     *
     * @param serviceDomain The service clustering domain
     */
    private void scaleUp(String serviceDomain) {
        EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig = ec2LBConfig.getServiceConfig(serviceDomain);
        int maxAppInstances = serviceConfig.getMaxAppInstances();
        AppDomainContext appDomainContext = appDomainContexts.get(serviceDomain);
        int runningInstances = appDomainContext.getRunningInstances();
        int pendingInstances = appDomainContext.getPendingInstances();
        if (runningInstances < maxAppInstances && pendingInstances == 0) {
            try {
                int instancesPerScaleUp = serviceConfig.getInstancesPerScaleUp();
                log.info("Domain: " + serviceDomain + " Going to start instance " + instancesPerScaleUp +
                         ". Running instances:" + runningInstances);
                runInstances(serviceConfig, instancesPerScaleUp);
                log.info("Started " + instancesPerScaleUp + " new app instances in domain" +
                         serviceDomain);
            } catch (Exception e) {
                log.error("Could not start new app instances for domain " + serviceDomain, e);
            }
        } else if (runningInstances > maxAppInstances) {
            log.warn("Number of running EC2 instances has reached the maximum limit of " +
                     maxAppInstances + " in domain " + serviceDomain);
        }
    }

    /**
     * Scale down the number of nodes in a domain, if the load has dropped
     *
     * @param serviceDomain The service clustering domain
     */
    private void scaleDown(String serviceDomain) {
        EC2LoadBalancerConfiguration.ServiceConfiguration serviceConfig = ec2LBConfig.getServiceConfig(serviceDomain);
        AppDomainContext appDomainContext = appDomainContexts.get(serviceDomain);
        int runningInstances = appDomainContext.getRunningInstances();
        int minAppInstances = serviceConfig.getMinAppInstances();
        if (runningInstances > minAppInstances) {
            Instance appInstance = appDomainContext.getRunningInstanceForTermination();
            if (appInstance == null) {
                return;
            }
            String instanceId = appInstance.getInstanceId();
            if (ec2LBConfig.getDisableApiTermination()) {
                ec2.enableApiTermination(instanceId);
            }
            log.info("Domain: " + serviceDomain + " Going to terminate instance " + instanceId +
                     ". Running instances:" + runningInstances + ". Min instances:" + minAppInstances);
            ec2.terminateInstances(Arrays.asList(instanceId));
            log.info("Terminated instance: " + instanceId + " in domain " +
                     serviceDomain);
        }
    }

    private void runInstances(EC2LoadBalancerConfiguration.Configuration configuration, int diff) {
        RunInstancesRequest request = new RunInstancesRequest(configuration.getImageId(),
                                                              diff, diff);
        request.setInstanceType(configuration.getEc2InstanceType());
        request.setKeyName(ec2LBConfig.getSshKey());
        request.setSecurityGroups(Arrays.asList(configuration.getSecurityGroups()));
        request.setAdditionalInfo(configuration.getAdditionalInfo());
        request.setUserData(configuration.getUserData());
        request.setPlacement(new Placement(configuration.getAvailabilityZone()));
        request.setDisableApiTermination(ec2LBConfig.getDisableApiTermination());
        ec2.runInstances(request);
    }

    private void expireRequestTokens(String serviceDomain) {
        appDomainContexts.get(serviceDomain).expireRequestTokens();
    }

    public void destroy() {
        appDomainContexts.clear();
    }
}
