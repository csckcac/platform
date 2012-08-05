/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.autoscale.lbautoscale.task;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.Task;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.mediator.autoscale.lbautoscale.clients.AutoscaleServiceClient;
import org.wso2.carbon.mediator.autoscale.lbautoscale.context.LoadBalancerContext;
import org.wso2.carbon.mediator.autoscale.lbautoscale.context.AppDomainContext;
import org.wso2.carbon.mediator.autoscale.lbautoscale.replication.RequestTokenReplicationCommand;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscaleConstants;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscaleUtil;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscalerTaskDSHolder;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.ConfigHolder;
import java.util.HashMap;
import java.util.Map;

/**
 * Service request in flight autoscaler task for Stratos service level autoscaling
 */
public class ServiceRequestsInFlightAutoscaler implements Task, ManagedLifecycle {

    private static final Log log = LogFactory.getLog(ServiceRequestsInFlightAutoscaler.class);

    /**
     * This instance holds the loadbalancer configuration
     */
    private LoadBalancerConfiguration loadBalancerConfig;

    /**
     * Autoscaler service client instance
     */
    private AutoscaleServiceClient autoscalerService;

    /**
     * Autoscaler service EPR
     */
    private String autoscalerServiceEPR;

    /**
     * Server start up delay in milliseconds.
     */
    private int serverStartupDelay;

    /**
     * AppDomainContexts for each domain
     * Key - domain
     * Value - Map of key - sub domain
     *                value - {@link AppDomainContext}
     */
    private Map<String, Map<String, AppDomainContext>> appDomainContexts =
                                             new HashMap<String, Map<String, AppDomainContext>>();

    /**
     * LB Context for LB cluster
     */
    private LoadBalancerContext lbContext = new LoadBalancerContext();

    /**
     * Attribute to keep track whether this instance is the primary load balancer.
     */
    private boolean isPrimaryLoadBalancer;

    /**
     * Keeps track whether this task is still running
     */
    private boolean isTaskRunning;

    /**
     * holding service domains and sub domains
     */
    private String[] serviceDomains, serviceSubDomains;

    
    public void init(SynapseEnvironment synEnv) {

        String msg = "Autoscaler Service initialization failed and cannot proceed.";

        loadBalancerConfig = AutoscalerTaskDSHolder.getInstance().getLoadBalancerConfig();

        if (loadBalancerConfig == null) {
            log.error(msg + "Reason: Load balancer configuration is null.");
            throw new RuntimeException(msg);
        }

        serverStartupDelay = loadBalancerConfig.getLoadBalancerConfig().getServerStartupDelay();
        serviceDomains = loadBalancerConfig.getServiceDomains();

        ConfigurationContext configCtx =
                                         (ConfigurationContext) synEnv.getServerContextInformation()
                                                                      .getServerContext();
        appDomainContexts = AutoscaleUtil.getAppDomainContexts(configCtx, loadBalancerConfig);
        ConfigHolder.setAgent(synEnv.getSynapseConfiguration().getAxisConfiguration()
                                    .getClusteringAgent());

        autoscalerServiceEPR = loadBalancerConfig.getLoadBalancerConfig().getAutoscalerServiceEpr();

        try {

            autoscalerService = new AutoscaleServiceClient(autoscalerServiceEPR);
            // let's initialize the autoscaler service
            autoscalerService.init(false);

        } catch (AxisFault e) {
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        if (log.isDebugEnabled()) {

            log.debug("Autoscaler task is initialized.");

        }
    }

    /**
     * This is method that gets called periodically when the task runs.
     * <p/>
     * The exact sequence of execution is shown in this method.
     */
    public void execute() {

        if (isTaskRunning) {
            log.debug("Task is already running!");
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
            // if there are any changes in the request length
            if (Boolean.parseBoolean(System.getProperty(AutoscaleConstants.IS_TOUCHED))) {
                // primary LB will send out replication message to all load balancers
                sendReplicationMessage();
            }
            isTaskRunning = false;
            log.debug("Task finished a cycle.");
        }
    }

    /**
     * Replicate information needed to take autoscaling decision for other ELBs
     * in the cluster.
     */
    private void sendReplicationMessage() {

        ClusteringAgent clusteringAgent = ConfigHolder.getAgent();
        if (clusteringAgent != null) {
            RequestTokenReplicationCommand msg = new RequestTokenReplicationCommand();
            msg.setAppDomainContexts(appDomainContexts);
            try {
                clusteringAgent.sendMessage(msg, true);
                System.setProperty(AutoscaleConstants.IS_TOUCHED, "false");
                log.debug("Request token replication messages sent out successfully!!");

            } catch (ClusteringFault e) {
                log.error("Failed to send the request token replication message.", e);
            }
        }
        log.debug("Clustering Agent is null. Hence, unable to send out the replication message.");
    }

    /**
     * This method makes sure that the minimum configuration of the clusters in the system is
     * maintained
     */
    private void sanityCheck() {

        setIsPrimaryLB();

        if (!isPrimaryLoadBalancer) {
            log.debug("This is not the primary load balancer, hence will not " +
            		"perform any sanity check.");
            return;
        }
        
        log.debug("This is the primary load balancer, starting to perform sanity checks.");
        
        computeRunningAndPendingInstances();
        loadBalancerSanityCheck();
        appNodesSanityCheck();
    }

    /**
     * We compute the number of running instances of a particular domain using clustering agent.
     */
    private void computeRunningAndPendingInstances() {

        int runningInstances, pendingInstanceCount = 0;

        for (String serviceDomain : serviceDomains) {

            // get the list of service sub_domains specified in loadbalancer config
            serviceSubDomains = loadBalancerConfig.getServiceSubDomains(serviceDomain);

            for (String serviceSubDomain : serviceSubDomains) {
                
                log.debug("Computation of instance counts started for domain: "+serviceDomain+
                          " and sub domain: "+serviceSubDomain);

                /* Calculate running instances of each service domain, sub domain combination */

                AppDomainContext appCtxt;

                // for each sub domain, get the clustering group management agent
                GroupManagementAgent agent =
                                             ConfigHolder.getAgent()
                                                         .getGroupManagementAgent(serviceDomain,
                                                                                  serviceSubDomain);

                // if it isn't null
                if (agent != null) {
                    // we calculate running instance count for this service domain
                    runningInstances = agent.getMembers().size();
                } else {
                    // if agent is null, we assume no service instances are running
                    runningInstances = 0;
                }
                
                log.debug("Running instance count : "+runningInstances);

                /* Calculate pending instances of each service domain */

                try {
                    pendingInstanceCount =
                                           autoscalerService.getPendingInstanceCount(serviceDomain,
                                                                                     serviceSubDomain);

                } catch (Exception e) {
                    log.error("Failed to retrieve pending instance count for domain: " +
                              serviceDomain + " and sub domain: " + serviceSubDomain, e);

                }
                
                log.debug("Pending instance count : "+pendingInstanceCount);

                int previousPendingCount = 0;
                int previousRunningCount = 0;

                if (appDomainContexts.get(serviceDomain) != null) {
                    appCtxt = appDomainContexts.get(serviceDomain).get(serviceSubDomain);

                    previousPendingCount = appCtxt.getPendingInstanceCount();
                    previousRunningCount = appCtxt.getRunningInstanceCount();
                }

                int newRunningInstanceCount = runningInstances;

                if (appDomainContexts.get(serviceDomain) != null) {

                    // if we need to wait for sometime to cover the server startup delay
                    if (previousPendingCount > 0 && pendingInstanceCount == 0 &&
                        runningInstances < (previousPendingCount + previousRunningCount)) {
                        
                        log.debug("There's an instance/s whose state changed from pending to " +
                        		"running (but still not joined ELB), hence we should wait till " +
                        		"it really started up.");
                        
                        int totalWaitedTime = 0;
                        
                        log.debug("Task will wait maximum of (milliseconds) : "+serverStartupDelay+
                                  ", to let server starts up.");
                        
                        // we give some time for the server to be started, we'll check time to time
                        // whether server has actually started up.
                        while(agent.getMembers().size() == runningInstances && 
                                totalWaitedTime < serverStartupDelay){
                            
                            try {
                                Thread.sleep(AutoscaleConstants.SERVER_START_UP_CHECK_TIME);
                            } catch (InterruptedException ignore) {
                            }
                            
                            totalWaitedTime += AutoscaleConstants.SERVER_START_UP_CHECK_TIME;
                        }
                        
                        log.debug("Task waited for (milliseconds) : "+totalWaitedTime);

                        // we recalculate number of agents, to check whether an instance spawned up
                        newRunningInstanceCount = agent.getMembers().size();
                        
                        log.debug("New running instance count: "+newRunningInstanceCount);

                        // if server hasn't yet started up, we gonna kill it.
                        if (newRunningInstanceCount == runningInstances) {
                            
                            log.debug("Running instance count hasn't been increased, hence we " +
                            		"gonna terminate it.");
                            // terminate the lastly spawned instance
                            try {
                                autoscalerService.terminateLastlySpawnedInstance(serviceDomain,
                                                                                 serviceSubDomain);
                            } catch (Exception e) {
                                log.error("Failed to terminate lastly spawned instance of domain: " +
                                                  serviceDomain +
                                                  " and sub domain: " +
                                                  serviceSubDomain + "! ", e);
                            }
                        }
                    }

                    appCtxt = appDomainContexts.get(serviceDomain).get(serviceSubDomain);
                    appCtxt.setRunningInstanceCount(newRunningInstanceCount);
                    appCtxt.setPendingInstanceCount(pendingInstanceCount);
                    
                    log.debug("Finished counting for domain: "+serviceDomain+" and sub domain: "+
                    serviceSubDomain);

                }
            }
        }

        /* Calculate running load balancer instances */

        // count this LB instance in.
        runningInstances = 1;

        runningInstances += ConfigHolder.getAgent().getAliveMemberCount();

        lbContext.setRunningInstanceCount(runningInstances);

        if(ConfigHolder.getAgent().getParameter("domain") == null){
            String msg = "Clustering Agent's domain parameter is null. Please specify a domain" +
            		" name in axis2.xml";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        
        String lbDomain = ConfigHolder.getAgent().getParameter("domain").getValue().toString();
        
        String lbSubDomain = null;
        
        if (ConfigHolder.getAgent().getParameter("subDomain") != null) {
            lbSubDomain =
                                 ConfigHolder.getAgent().getParameter("subDomain").getValue()
                                             .toString();
        }

        pendingInstanceCount = 0;

        try {
            pendingInstanceCount = autoscalerService.getPendingInstanceCount(lbDomain, lbSubDomain);

        } catch (Exception e) {
            log.error("Failed to set pending instance count for domain: " + lbDomain +
                      " and sub domain: " + lbSubDomain, e);
        }

        lbContext.setPendingInstanceCount(pendingInstanceCount);
        
        log.debug("Load Balancer members of domain: "+lbDomain+" and sub domain: "+lbSubDomain+
                  " (including this): " + runningInstances+" - pending instances: "
                + pendingInstanceCount);

    }

    /**
     * Sanity check to see whether the number of LBs is the number specified in the LB config
     */
    private void loadBalancerSanityCheck() {
        
        log.debug("Load balancer sanity check has started.");
        
        // get current LB instance count
        int currentLBInstances = lbContext.getInstances();

        LoadBalancerConfiguration.LBConfiguration lbConfig =
                                                             loadBalancerConfig.getLoadBalancerConfig();

        // get minimum requirement of LB instances
        int requiredInstances = lbConfig.getInstances();

        if (currentLBInstances < requiredInstances) {
            log.debug("LB Sanity check failed. Current LB instances: " + currentLBInstances +
                     ". Required LB instances: " + requiredInstances);
            int diff = requiredInstances - currentLBInstances;

            // gets the domain of the LB
            String lbDomain = ConfigHolder.getAgent().getParameter("domain").getValue().toString();
            String lbSubDomain =
                                 ConfigHolder.getAgent().getParameter("subDomain").getValue()
                                             .toString();

            // Launch diff number of LB instances
            log.debug("Launching " + diff + " LB instances.");
            
            runInstances(lbContext, lbDomain, lbSubDomain, diff);
            // lbContext.incrementPendingInstances(diff);
            // lbContext.resetRunningPendingInstances();
        }
    }

    private int runInstances(LoadBalancerContext context, String domain, String subDomain, 
        int diff) {

        int successfullyStartedInstanceCount = diff;

        while (diff > 0) {
            // call autoscaler service and ask to spawn an instance
            // and increment pending instance count only if autoscaler service returns
            // true.
            try {
                boolean isSuccessful = autoscalerService.startInstance(domain, subDomain);
                
                if (!isSuccessful) {
                    log.debug("Instance start up failed. domain: "+domain+", sub domain: "+subDomain);
                    successfullyStartedInstanceCount--;
                } else {
                    log.debug("An instance of domain: "+domain+" and sub domain: "+subDomain+" is started up.");
                    if (context != null) {
                        context.incrementPendingInstances(1);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to start an instance of sub domain: " + subDomain +
                          " of domain : " + domain + ".\n", e);
                successfullyStartedInstanceCount--;
            }

            diff--;
        }

        return successfullyStartedInstanceCount;
    }

    /**
     * This method will check whether this LB is the primary LB or not and set
     * attribute accordingly.
     */
    private void setIsPrimaryLB() {

        ClusteringAgent clusteringAgent = ConfigHolder.getAgent();
        if (clusteringAgent != null) {

            isPrimaryLoadBalancer = clusteringAgent.isCoordinator();

        }

    }

    /**
     * Check that all app nodes in all clusters meet the minimum configuration
     */
    private void appNodesSanityCheck() {
        for (String serviceDomain : serviceDomains) {
            // get the list of service sub_domains specified in loadbalancer config
            serviceSubDomains = loadBalancerConfig.getServiceSubDomains(serviceDomain);

            for (String serviceSubDomain : serviceSubDomains) {
                log.debug("Sanity check has started for domain: "+serviceDomain+
                          " and sub domain: "+serviceSubDomain);
                appNodesSanityCheck(serviceDomain, serviceSubDomain);
            }
        }
    }

    private void appNodesSanityCheck(String serviceDomain, String serviceSubDomain) {

        String msg =
                     "Sanity check is failed to run. No Appdomain context is generated for the" +
                             " domain " + serviceDomain;

        if (appDomainContexts.get(serviceDomain) == null) {
            log.error(msg);

        } else {
            AppDomainContext appDomainContext =
                                                appDomainContexts.get(serviceDomain)
                                                                 .get(serviceSubDomain);

            int currentInstances = 0;
            if (appDomainContext != null) {
                // we're considering both running and pending instance count
                currentInstances = appDomainContext.getInstances();
            } else {
                log.error(msg + " and sub domain " + serviceSubDomain + " combination.");
            }

            LoadBalancerConfiguration.ServiceConfiguration serviceConfig =
                                                                           loadBalancerConfig.getServiceConfig(serviceDomain,
                                                                                                               serviceSubDomain);
            int requiredInstances = serviceConfig.getMinAppInstances();

            // we try to maintain the minimum number of instances required
            if (currentInstances < requiredInstances) {
                log.debug("App domain Sanity check failed for [" + serviceDomain + " : " +
                         serviceSubDomain + "] . Current instances: " + currentInstances +
                         ". Required instances: " + requiredInstances);

                int diff = requiredInstances - currentInstances;

                // Launch diff number of App instances
                log.debug("Launching " + diff + " App instances for sub domain " + serviceSubDomain +
                         " of domain " + serviceDomain);

                // FIXME: should we need to consider serviceConfig.getInstancesPerScaleUp()?
                runInstances(appDomainContext, serviceDomain, serviceSubDomain, diff);
            }
        }
    }

    /**
     * Autoscale the entire system, analyzing the requests in flight of each domain - sub domain
     */
    private void autoscale() {
        for (String serviceDomain : serviceDomains) {

            // get the list of service sub_domains specified in loadbalancer config
            serviceSubDomains = loadBalancerConfig.getServiceSubDomains(serviceDomain);

            for (String serviceSubDomain : serviceSubDomains) {
                
                log.debug("Autoscaling analysis is starting to run for domain: "+serviceDomain+
                          " and sub domain: "+serviceSubDomain);

                expireRequestTokens(serviceDomain, serviceSubDomain);
                autoscale(serviceDomain, serviceSubDomain);
            }
        }
    }

    /**
     * This method contains the autoscaling algorithm for requests in flight based autoscaling
     * 
     * @param serviceDomain
     *            service clustering domain
     * @param serviceSubDomain
     */
    private void autoscale(String serviceDomain, String serviceSubDomain) {

        String msg =
                     "Failed to autoscale. No Appdomain context is generated for the" + " domain " +
                             serviceDomain;

        if (appDomainContexts.get(serviceDomain) == null) {
            log.error(msg);

        } else {

            AppDomainContext appDomainContext =
                                                appDomainContexts.get(serviceDomain)
                                                                 .get(serviceSubDomain);
            LoadBalancerConfiguration.ServiceConfiguration serviceConfig =
                                                                           appDomainContext.getServiceConfig();

            appDomainContext.recordRequestTokenListLength();
            if (!appDomainContext.canMakeScalingDecision()) {
                return;
            }

            long average = appDomainContext.getAverageRequestsInFlight();
            int runningAppInstances = appDomainContext.getRunningInstanceCount();

            int queueLengthPerNode = serviceConfig.getQueueLengthPerNode();
            if (log.isDebugEnabled()) {
                log.debug("Average requests in flight: " + average + " **** Handleable requests: " +
                          (runningAppInstances * queueLengthPerNode));
            }
            if (average > (runningAppInstances * queueLengthPerNode)) {
                // current average is high than that can be handled by current nodes.
                scaleUp(serviceDomain, serviceSubDomain);
            } else if (average < ((runningAppInstances - 1) * queueLengthPerNode)) {
                // current average is less than that can be handled by (current nodes - 1).
                scaleDown(serviceDomain, serviceSubDomain);
            }
        }
    }

    /**
     * Scales up the system when the request count is high in the queue
     * Handle scale-up, if the number of running applications is less than the allowed maximum and
     * if there are no instances pending startup
     * 
     * @param domain
     *            The service clustering domain
     */
    private void scaleUp(String domain, String subDomain) {

        LoadBalancerConfiguration.ServiceConfiguration serviceConfig =
                                                                       loadBalancerConfig.getServiceConfig(domain,
                                                                                                           subDomain);
        int maxAppInstances = serviceConfig.getMaxAppInstances();

        String msg =
                     "Failed to scale up. No Appdomain context is generated for the" + " domain " +
                             domain;

        if (appDomainContexts.get(domain) == null) {
            log.error(msg);

        } else {

            AppDomainContext appDomainContext = appDomainContexts.get(domain).get(subDomain);
            
            if (appDomainContext != null) {

                int runningInstances = appDomainContext.getRunningInstanceCount();
                int pendingInstances = appDomainContext.getPendingInstanceCount();

                int failedInstances = 0;
                if (runningInstances < maxAppInstances && pendingInstances == 0) {

                    int instancesPerScaleUp = serviceConfig.getInstancesPerScaleUp();
                    log.debug("Domain: " + domain + " Going to start instance " +
                             instancesPerScaleUp + ". Running instances:" + runningInstances);

                    int successfullyStarted =
                                              runInstances(appDomainContext, domain, subDomain,
                                                           instancesPerScaleUp);

                    if (successfullyStarted != instancesPerScaleUp) {
                        failedInstances = instancesPerScaleUp - successfullyStarted;
                        if (log.isDebugEnabled()) {
                            log.debug(successfullyStarted +
                                      " instances successfully started and\n" + failedInstances +
                                      " instances failed to start for domain " + domain);
                        }
                    }

                    // we increment the pending instance count
                    // appDomainContext.incrementPendingInstances(instancesPerScaleUp);
                    else {
                        log.debug("Successfully started " + successfullyStarted +
                                 " instances of domain " + domain+", sub domain: "+subDomain);
                    }

                } else if (runningInstances > maxAppInstances) {
                    log.fatal("Number of running instances has over reached the maximum limit of " +
                             maxAppInstances + " in domain " + domain);
                }
            } else {
                log.error(msg + " and sub domain: " + subDomain + " combination.");
            }
        }
    }

    /**
     * Scale down the number of nodes in a domain, if the load has dropped
     * 
     * @param domain
     *            The service clustering domain
     */
    private void scaleDown(String domain, String subDomain) {

        LoadBalancerConfiguration.ServiceConfiguration serviceConfig =
                                                                       loadBalancerConfig.getServiceConfig(domain,
                                                                                                           subDomain);

        String msg =
                     "Failed to scale down. No Appdomain context is generated for the" +
                             " domain " + domain;

        if (appDomainContexts.get(domain) == null) {
            log.error(msg);

        } else {
            AppDomainContext appDomainContext = appDomainContexts.get(domain).get(subDomain);

            if (appDomainContext != null) {
                int runningInstances = appDomainContext.getRunningInstanceCount();
                int minAppInstances = serviceConfig.getMinAppInstances();
                if (runningInstances > minAppInstances) {

                    if (log.isDebugEnabled()) {
                        log.debug("Domain: " + domain + ". Running instances:" + runningInstances +
                                  ". Min instances:" + minAppInstances);
                    }
                    // ask to scale down
                    try {
                        if (autoscalerService.terminateInstance(domain, subDomain)) {

                            runningInstances--;
                            appDomainContext.setRunningInstanceCount(runningInstances);
                        }

                    } catch (Exception e) {
                        log.error("Instance termination failed for domain " + domain);
                    }

                }
            } else {
                log.error(msg + " and sub domain: " + subDomain + " combination.");
            }
        }
    }

    private void expireRequestTokens(String domain, String subDomain) {
        if (appDomainContexts.get(domain) != null) {
            appDomainContexts.get(domain).get(subDomain).expireRequestTokens();
            return;
        }
        log.error("No Appdomain context is generated for the" + " domain " + domain);
    }

    public void destroy() {
        appDomainContexts.clear();
        log.debug("Cleared AppDomainContext Map.");
    }
}
