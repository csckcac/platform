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

package org.wso2.carbon.mashup.javascript.hostobjects.system;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.utils.MashupConstants;

import java.util.Map;

public class FunctionSchedulingJob implements Job {


    public static final String JAVASCRIPT_FUNCTION = "jsfunction";

    public static final String FUNCTION_PARAMETERS = "jsfunctionarguments";

    public static final String AXIS_SERVICE = "axisService";

    public static final String CONFIGURATION_CONTEXT = "configurationContext";

    public static final String CLASSNAME = "ClassName";

    public static final String PROPERTIES = "Properties";

    public static final String MASHUP_GROUP = "mashup.scheduler.functions";

    public static final String JS_FUNCTION_MAP = "js_sheduled_function_map";

    public static final String TASK_NAME = "taskName";

    private static final Log log = LogFactory.getLog(FunctionSchedulingJob.class);


    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {

            Task task = null;

            JobDetail jobDetail = jobExecutionContext.getJobDetail();

            if (log.isDebugEnabled()) {
                log.debug("Executing Function scheduling task : " + jobDetail.getKey().getName());
            }

            Map mjdm = jobExecutionContext.getMergedJobDataMap();
            String jobClassName = (String) mjdm.get(CLASSNAME);
            if (jobClassName == null) {
                throw new CarbonException("No " + CLASSNAME + " in JobDetails");
            }

            try {
                task = (Task) getClass().getClassLoader().loadClass(jobClassName).newInstance();
            } catch (Exception e) {
                throw new CarbonException("Cannot instantiate Function scheduling task : " + jobClassName, e);
            }

            /*Set properties = (Set) mjdm.get(PROPERTIES);
            for (Object property : properties) {
                OMElement prop = (OMElement) property;
                log.debug("Found Property : " + prop.toString());
                PropertyHelper.setStaticProperty(prop, task);
            }*/

            // 1. Initialize
            if (task instanceof FunctionExecutionTaskLifeCycleCallBack) {
                // initialize FunctionExecutionTask with JobDataMap
                ((FunctionExecutionTask) task).init(
                        jobDetail.getJobDataMap());
            }

            // 2. Execute
            task.execute();

            // 3. Destroy
            if (task instanceof FunctionExecutionTaskLifeCycleCallBack) {
                ((FunctionExecutionTaskLifeCycleCallBack) task).destroy();
            }

            ConfigurationContext configCtx = (ConfigurationContext) mjdm.get(
                    MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }

    }

}
