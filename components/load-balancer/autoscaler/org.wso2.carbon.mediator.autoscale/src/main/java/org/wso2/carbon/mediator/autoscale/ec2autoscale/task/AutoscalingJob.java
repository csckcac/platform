/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mediator.autoscale.ec2autoscale.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.Task;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.CarbonException;
import java.util.Map;

public class AutoscalingJob implements Job {


    public static final String JAVASCRIPT_FUNCTION = "jsfunction";

    public static final String FUNCTION_PARAMETERS = "jsfunctionarguments";

    public static final String AXIS_SERVICE = "axisService";

    public static final String CONFIGURATION_CONTEXT = "configurationContext";

    public static final String CLASSNAME = "ClassName";

    public static final String PROPERTIES = "Properties";

    public static final String MASHUP_GROUP = "mashup.scheduler.functions";

    public static final String JS_FUNCTION_MAP = "js_sheduled_function_map";

    public static final String TASK_NAME = "taskName";
    
    public static final String AUTOSCALER_TASK ="autoscalerTask";
    
    public static final String SYNAPSE_ENVI ="synapseEnv";

    private static final Log log = LogFactory.getLog(AutoscalingJob.class);


    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {

            Task task = null;

            JobDetail jobDetail = jobExecutionContext.getJobDetail();

            if (log.isDebugEnabled()) {
                log.debug("Executing Function scheduling task : " + jobDetail.getFullName());
            }

            Map mjdm = jobExecutionContext.getMergedJobDataMap();
            log.info("***** AUTOSCALER JOB **************" + String.valueOf(mjdm.get(AUTOSCALER_TASK)));
           
            String jobClassName = (String) mjdm.get(AUTOSCALER_TASK);
            log.info("***** AUTOSCALER JOB ************** class: "+jobClassName );
            if (jobClassName == null) {
                throw new CarbonException("No " + AUTOSCALER_TASK + " in JobDetails");
            }

            try {
                task = (Task) getClass().getClassLoader().loadClass(jobClassName).newInstance();
                log.info("***** AUTOSCALER JOB ************** "+task.getClass().getName());
            } catch (Exception e) {
                throw new CarbonException("Cannot instantiate Function scheduling task : " + jobClassName, e);
            }

            SynapseEnvironment sysEnv = (SynapseEnvironment) mjdm.get(SYNAPSE_ENVI);
            /*Set properties = (Set) mjdm.get(PROPERTIES);
            for (Object property : properties) {
                OMElement prop = (OMElement) property;
                log.debug("Found Property : " + prop.toString());
                PropertyHelper.setStaticProperty(prop, task);
            }*/

            // 1. Initialize
            if (task instanceof ManagedLifecycle) {
                // initialize FunctionExecutionTask with JobDataMap
                ((ServiceRequestsInFlightAutoscaler) task).init(sysEnv);
            }

            // 2. Execute
            task.execute();

//            // 3. Destroy
//            if (task instanceof ManagedLifecycle) {
//                ((AutoscalerTaskLifeCycleCallBack) task).destroy();
//            }

//            ConfigurationContext configCtx = (ConfigurationContext) mjdm.get(
//                    MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }

    }

}