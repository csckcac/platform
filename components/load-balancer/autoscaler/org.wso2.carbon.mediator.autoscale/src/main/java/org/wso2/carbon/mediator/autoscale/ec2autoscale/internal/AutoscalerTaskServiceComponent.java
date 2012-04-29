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
package org.wso2.carbon.mediator.autoscale.ec2autoscale.internal;

import java.util.Date;
import java.util.Map;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.filters.InMediator;
import org.apache.synapse.mediators.filters.OutMediator;
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskScheduler;
import org.apache.synapse.task.service.TaskManagementService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.util.AutoscaleConstants;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.mediators.AutoscaleInMediator;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.mediators.AutoscaleOutMediator;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.task.AutoscalerTaskInitializer;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.task.AutoscalerTaskMgmtAdminService;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.task.AutoscalingJob;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.task.ServiceRequestsInFlightAutoscaler;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.task.TaskSchedulingManager;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.util.AutoscalerTaskDSHolder;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="autoscaler.task.component" immediate="true"
 * 
 * @scr.reference name="carbon.core.configurationContextService"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * 
 */
public class AutoscalerTaskServiceComponent {

    private static final Log log = LogFactory.getLog(AutoscalerTaskServiceComponent.class);
    private ConfigurationContext configurationContext = null;

    protected void activate(ComponentContext context) {

        // read config file
        String configURL = System.getProperty(AutoscaleConstants.LOAD_BALANCER_CONFIG);
        LoadBalancerConfiguration lbConfig = new LoadBalancerConfiguration();
        lbConfig.init(configURL);

        // load synapse environment
        Parameter synEnv =
            configurationContext.getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_ENV);

        if (synEnv == null || synEnv.getValue() == null ||
            !(synEnv.getValue() instanceof SynapseEnvironment)) {

            String message = "Unable to initialize the Synapse Configuration : Can not find the ";
            log.fatal(message + "Synapse Environment");
            throw new SynapseException(message + "Synapse Environment");
        }

        SynapseEnvironment synapseEnv = (SynapseEnvironment) synEnv.getValue();

        /** Initializing autoscaleIn and autoscaleOut Mediators.**/
        
        // check whether autoscaling is enabled
        if (lbConfig.getLoadBalancerConfig().isAutoscaleEnabled()) {

            // set the lb configuration 
            AutoscalerTaskDSHolder.getInstance().setLoadBalancerConfig(lbConfig);

            // get the main sequence mediator
            SequenceMediator mainSequence =
                (SequenceMediator) synapseEnv.getSynapseConfiguration().getSequence("main");

            // iterate through its child mediators
            for (Mediator child : mainSequence.getList()) {

                // find the InMediator
                if (child instanceof InMediator) {
                    InMediator inSequence = (InMediator) child;

                    // if the first child of InMediator isn't an AutoscaleInMediator
                    if (!(inSequence.getList().get(0) instanceof AutoscaleInMediator) ) {
           
                        // we gonna add it!
                        inSequence.getList().add(0, new AutoscaleInMediator(lbConfig));
                        
                        log.debug("Added Mediator: " + inSequence.getChild(0) + "" +
                            " to InMediator. Number of child mediators in InMediator" +
                            " is " + inSequence.getList().size()+".");
                    }

                }

                // find the OutMediator
                if (child instanceof OutMediator) {

                    OutMediator outSequence = (OutMediator) child;

                    // if the first child of OutMediator isn't an AutoscaleOutMediator
                    if (!(outSequence.getList().get(0) instanceof AutoscaleOutMediator)) {
                        
                        // we gonna add it!
                        outSequence.getList().add(0, new AutoscaleOutMediator());
                        
                        log.debug("Added Mediator: " + outSequence.getChild(0) + "" +
                                " to OutMediator. Number of child mediators in OutMediator" +
                                " is " + outSequence.getList().size()+".");

                    }
                }
            }
            
            /** Initializing Autoscaler Task **/

            BundleContext bundleContext = context.getBundleContext();
            if (log.isDebugEnabled()) {
                log.debug("Initiating Autoscaler task service component");
            }

            if (bundleContext.getServiceReference(TaskManagementService.class.getName()) != null) {
                bundleContext.registerService(TaskManagementService.class.getName(),
                                              new AutoscalerTaskMgmtAdminService(), null);
            }
            

            AutoscalerTaskInitializer listener = new AutoscalerTaskInitializer();

            if (bundleContext.getServiceReference(Axis2ConfigurationContextObserver.class.getName()) != null) {
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                              listener, null);
            }
            
            if (configurationContext != null) {
                TaskScheduler scheduler =
                    (TaskScheduler) configurationContext.getProperty(AutoscalerTaskInitializer.CARBON_TASK_SCHEDULER);
                if (scheduler == null) {
                    scheduler = new TaskScheduler(TaskConstants.TASK_SCHEDULER);
                    scheduler.init(null);
                    configurationContext.setProperty(AutoscalerTaskInitializer.CARBON_TASK_SCHEDULER,
                                                     scheduler);
                } else if (!scheduler.isInitialized()) {
                    scheduler.init(null);
                }
            }

            ServiceRequestsInFlightAutoscaler autoscalerTask =
                new ServiceRequestsInFlightAutoscaler();

            autoscalerTask.init(synapseEnv);

            // specify sceduler task details
            JobDetail job = new JobDetail();
            job.setName("autoscalerJob");
            job.setJobClass(AutoscalingJob.class);

            Map<String, Object> dataMap = job.getJobDataMap();
            dataMap.put(AutoscalingJob.AUTOSCALER_TASK, autoscalerTask);
            dataMap.put(AutoscalingJob.SYNAPSE_ENVI, synapseEnv);

            final TaskDescription taskDescription = new TaskDescription();
            taskDescription.setTaskClass(ServiceRequestsInFlightAutoscaler.class.getName());
            taskDescription.setName("autoscaler");
            taskDescription.setCount(SimpleTrigger.REPEAT_INDEFINITELY);
            
            int interval = lbConfig.getLoadBalancerConfig().getAutoscalerTaskInterval();
            taskDescription.setInterval(interval);
            taskDescription.setStartTime(new Date(System.currentTimeMillis() + interval));

            TaskSchedulingManager scheduler = new TaskSchedulingManager();
            scheduler.scheduleTask(taskDescription, dataMap, configurationContext);


        } else {

            log.info("Autoscaling is disabled.");
        }
    }


    protected void deactivate(ComponentContext ctx) {
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(null);
        if (log.isDebugEnabled()) {
            log.debug("Autoscaler task bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService context) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the Autoscaler task initialization process");
        }
        this.configurationContext = context.getServerConfigContext();
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(context);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the Autoscaler task");
        }
        this.configurationContext = null;
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(null);
    }

}
