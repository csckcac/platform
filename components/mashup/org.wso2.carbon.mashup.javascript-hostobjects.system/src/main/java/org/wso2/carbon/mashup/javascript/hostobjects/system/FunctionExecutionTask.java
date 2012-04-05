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

import org.apache.synapse.task.Task;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngine;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngineUtils;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import java.net.URL;
import java.io.Reader;
import java.io.IOException;
import java.util.Map;

public class FunctionExecutionTask implements Task, FunctionExecutionTaskLifeCycleCallBack {

    private static final Log log = LogFactory.getLog(FunctionExecutionTask.class);

    private Map jdm;

    public void execute() {

        try {

            AxisService axisService = (AxisService) jdm.get(FunctionSchedulingJob.AXIS_SERVICE);
            Object jsFunction = jdm.get(FunctionSchedulingJob.JAVASCRIPT_FUNCTION);

            ConfigurationContext configurationContext = (ConfigurationContext) jdm.get(
                    MashupConstants.AXIS2_CONFIGURATION_CONTEXT);



            Object[] parameters = (Object[]) jdm.get(FunctionSchedulingJob.FUNCTION_PARAMETERS);


            String serviceName = axisService.getName();
            JavaScriptEngine jsEngine = new JavaScriptEngine(serviceName);

            // Rhino E4X XMLLibImpl object can be instantiated only from within a script
            // So we instantiate it in here, so that we can use it outside of the script later
            jsEngine.getCx().evaluateString(jsEngine, "new XML();", "Instantiate E4X", 0, null);


            JavaScriptEngineUtils.loadHostObjects(jsEngine, serviceName);

            // Inject the incoming MessageContext to the Rhino Context. Some
            // host objects need access to the MessageContext. Eg: FileSystem,
            // WSRequest
            Context context = jsEngine.getCx();

            /*
             * Some host objects depend on the data we obtain from the
             * AxisService & ConfigurationContext.. It is possible to get these
             * data through the MessageContext. But we face problems at the
             * deployer, where we need to instantiate host objects in order for
             * the annotations framework to work and the MessageContext is not
             * available at that time. For the consistency we inject them in
             * here too..
             */
            context.putThreadLocal(MashupConstants.AXIS2_SERVICE, axisService);
            context.putThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT,
                                   configurationContext);

            AxisConfiguration axisConfig = configurationContext.
                    getAxisConfiguration();
            JavaScriptEngineUtils.loadHostObjects(jsEngine, serviceName);

            URL repoURL = axisConfig.getRepository();
            if (repoURL != null) {
                JavaScriptEngine.axis2RepositoryLocation = repoURL.getPath();
            }

            Reader reader = MashupUtils.readJS(axisService);

            Object[] args;

            //support for importing javaScript files using services.xml or the axis2.xml
            String scripts = MashupUtils.getImportScriptsList(axisService);

            //Loading imported JavaScript files if there are any
            if (scripts != null) {
                // Generate load command out of the parameter scripts
                scripts = "load(" + ("[\"" + scripts + "\"]").replaceAll(",", "\"],[\"") + ")";
                jsEngine.getCx().evaluateString(jsEngine, scripts, "Load Included JavaScript File(s)", 0, null);
            }

            //Evaluating the JavaScript service file
            jsEngine.getCx().evaluateReader(jsEngine, reader, "Load JSService file", 1, null);

            if (jsFunction instanceof Function) {
                if (parameters != null) {
                    args = parameters;
                } else {
                    args = new Object[0];
                }

                Function function = (Function) jsFunction;
                function.call(jsEngine.getCx(), jsEngine, jsEngine, args);

            } else if (jsFunction instanceof String) {
                String jsString = (String) jsFunction;
                jsEngine.getCx()
                        .evaluateString(jsEngine, jsString, "Load JavaScriptString", 0, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(Map jdm) {
        this.jdm = jdm;
    }

    public void destroy() {
        this.jdm = null;
    }
}