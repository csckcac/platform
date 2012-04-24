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
package org.wso2.carbon.mediator.autoscale.ec2autoscale.internal;

import java.util.Date;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskScheduler;
import org.apache.synapse.task.service.TaskManagementService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
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
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 *
 */
public class AutoscalerTaskServiceComponent {
    
    private static final Log log = LogFactory.getLog(AutoscalerTaskServiceComponent.class);
    private AutoscalerTaskMgmtAdminService taskMgtService;
    private ConfigurationContext configurationContext = null;
    
    protected void activate(ComponentContext context) {

        String configURL = System.getProperty("loadbalancer.conf");
        LoadBalancerConfiguration lbConfig = new LoadBalancerConfiguration();
        lbConfig.init(configURL);

        // check whether autoscaling is enabled
        if (lbConfig.getLoadBalancerConfig().isAutoscaleEnabled()) {

            AutoscalerTaskDSHolder.getInstance().setLoadBalancerConfig(lbConfig);
            BundleContext bundleContext = context.getBundleContext();
            if (log.isDebugEnabled()) {
                log.debug("Initiating SystemHostObject service components");
            }
            bundleContext.registerService(TaskManagementService.class.getName(),
                                          new AutoscalerTaskMgmtAdminService(), null);
            // bundleContext.registerService(JobMetaDataProviderService.class.getName(),
            // new FunctionSchedulingJobMetaDataProviderService(), null);
            // bundleContext.registerService(SystemHostObjectService.class.getName(),
            // new SystemHostObjectService(), null);

            AutoscalerTaskInitializer listener = new AutoscalerTaskInitializer();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          listener, null);
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

            Parameter synEnv = configurationContext.getAxisConfiguration()
                    .getParameter(SynapseConstants.SYNAPSE_ENV);
            
            if (synEnv == null || synEnv.getValue() == null 
                    || !(synEnv.getValue() instanceof SynapseEnvironment)) {
                
                String message="Unable to initialize the Synapse Configuration : Can not find the ";
                log.fatal(message + "Synapse Environment");
                throw new SynapseException(message + "Synapse Environment");
            }
            
            ServiceRequestsInFlightAutoscaler autoscalerTask =
                new ServiceRequestsInFlightAutoscaler();

            // specify your sceduler task details
            JobDetail job = new JobDetail();
            job.setName("autoscalerJob");
            job.setJobClass(AutoscalingJob.class);
            

            Map<String, Object> dataMap = job.getJobDataMap();
            dataMap.put(AutoscalingJob.AUTOSCALER_TASK, autoscalerTask.getClass().getName());
            dataMap.put(AutoscalingJob.SYNAPSE_ENVI, (SynapseEnvironment) synEnv.getValue());

            final TaskDescription taskDescription = new TaskDescription();
            taskDescription.setTaskClass(ServiceRequestsInFlightAutoscaler.class.getName());
            taskDescription.setName("autoscaler");
            taskDescription.setCount(SimpleTrigger.REPEAT_INDEFINITELY);
            taskDescription.setInterval(10000);
            taskDescription.setStartTime(new Date(System.currentTimeMillis() + 1000));

            TaskSchedulingManager scheduler = new TaskSchedulingManager();
            scheduler.scheduleTask(taskDescription, dataMap, configurationContext);

        }
        else{
            log.info("Autoscaling is disabled.");
        }
    }
    
//    protected void activate(ComponentContext ctx) {
//        try {
//
//
//            String configURL = System.getProperty("loadbalancer.conf");
//            LoadBalancerConfiguration lbConfig = new LoadBalancerConfiguration();
//            lbConfig.init(configURL);
//            
//            // check whether autoscaling is enabled
//            if (lbConfig.getLoadBalancerConfig().isAutoscaleEnabled()) {
//
//                AutoscalerTaskDSHolder.getInstance().setLoadBalancerConfig(lbConfig);
//
//                BundleContext bundleContext = ctx.getBundleContext();
//
//                bundleContext.registerService(TaskManagementService.class.getName(),
//                                              new AutoscalerTaskMgmtAdminService(), null);
//                
//                taskMgtService =
//                    new AutoscalerTaskMgmtAdminService(
//                                                  AutoscalerTaskDSHolder.getInstance()
//                                                                        .getConfigurationContextServiceService()
//                                                                        .getServerConfigContext());
//                bundleContext.registerService(TaskManagementService.class.getName(),
//                                              taskMgtService, null);
//
//                TaskDescription taskDes = new TaskDescription();
//                taskDes.setTaskClass(ServiceRequestsInFlightAutoscaler.class.getName());
//                taskDes.setCount(SimpleTrigger.REPEAT_INDEFINITELY);
//                taskDes.setInterval(5);
//                taskDes.setName("autoscaler");
//                taskDes.setStartTime(new Date(System.currentTimeMillis() + 1000));
//                //taskDes.addProperty(AXIOMUtil.stringToOM("<property name=\"configuration\" value=\"$system:loadbalancer.conf\"/>"));
//
//                taskMgtService.addTaskDescription(taskDes);
//                
//                
//
//                if (log.isDebugEnabled()) {
//                    log.debug("Autoscaler task bundle is activated");
//                }
//            }
//            else{
//                //if not we deactivate this bundle
//                deactivate(ctx);
//            }
//        } catch (Throwable e) {
//            log.error("Autoscaler task bundle cannot be started", e);
//        }
//
//    }

    protected void deactivate(ComponentContext ctx) {
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(null);
        if (log.isDebugEnabled()) {
            log.debug("Autoscaler task bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService context) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the Autoscaler task initialization process");
        }
        this.configurationContext = context.getServerConfigContext();
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(context);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the Autoscaler task");
        }
        this.configurationContext = null;
        AutoscalerTaskDSHolder.getInstance().setConfigurationContextService(null);
    }

}
