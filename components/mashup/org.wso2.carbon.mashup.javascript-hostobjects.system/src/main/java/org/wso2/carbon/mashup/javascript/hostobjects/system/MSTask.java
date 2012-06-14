/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.net.URL;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngine;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngineUtils;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupReader;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

public class MSTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(MSTask.class);

	@Override
	public void execute() {

		log.error("EXECUTE CALLED >>>>>>>>>>>>>>>>> " + getProperties().get(MSTaskConstants.TASK_NAME));

		MSTaskInfo taskInfo = null;
		try {
			taskInfo = MSTaskUtils.getTaskInfo
					(MSTaskConstants.MS_TASK_TYPE, getProperties().get(MSTaskConstants.TASK_NAME));
			
			log.error("Elements " + taskInfo.getProperties());
		} catch (TaskException e) {
			e.printStackTrace();
			log.error("Error occurred while running task : " + e.getMessage());
		}
		
		if(taskInfo == null) {
			log.error("Error occurred while running task : Task info is null");
			return;
		}
		
        try {

            AxisService axisService = (AxisService) taskInfo.getRuntimeProperties().get(MSTaskConstants.AXIS_SERVICE);
            Object jsFunction = getProperties().get(MSTaskConstants.JAVASCRIPT_FUNCTION);

            ConfigurationContext configurationContext = (ConfigurationContext) taskInfo.getRuntimeProperties().get(
                    MashupConstants.AXIS2_CONFIGURATION_CONTEXT);



            Object[] parameters = (Object[]) taskInfo.getRuntimeProperties().get(MSTaskConstants.FUNCTION_PARAMETERS);


            String serviceName = axisService.getName();
            JavaScriptEngine jsEngine = new JavaScriptEngine(serviceName);

            // Inject the incoming MessageContext to the Rhino Context. Some
            // host objects need access to the MessageContext. Eg: FileSystem,
            // WSRequest
            Context context = RhinoEngine.enterContext();

            /*
             * Some host objects depend on the data we obtain from the
             * AxisService & ConfigurationContext.. It is possible to get these
             * data through the MessageContext. But we face problems at the
             * deployer, where we need to instantiate host objects in order for
             * the annotations framework to work and the MessageContext is not
             * available at that time. For the consistency we inject them in
             * here too..
             */
            RhinoEngine.putContextProperty(MashupConstants.AXIS2_SERVICE, axisService);
            RhinoEngine.putContextProperty(MashupConstants.AXIS2_CONFIGURATION_CONTEXT,
                    configurationContext);

            AxisConfiguration axisConfig = configurationContext.
                    getAxisConfiguration();

            URL repoURL = axisConfig.getRepository();
            if (repoURL != null) {
                JavaScriptEngine.axis2RepositoryLocation = repoURL.getPath();
            }

            Object[] args;

            RhinoEngine engine = JavaScriptEngineUtils.getEngine();
            ScriptableObject scope = engine.getRuntimeScope();
            //Evaluating the JavaScript service file
            engine.exec(new MashupReader(axisService), scope, MashupUtils.getScriptCachingContext(configurationContext, axisService));
            Context cx = RhinoEngine.enterContext();
            if (jsFunction instanceof Function) {
                if (parameters != null) {
                    args = parameters;
                } else {
                    args = new Object[0];
                }
                Function function = (Function) jsFunction;
                function.call(cx, scope, scope, args);
            } else if (jsFunction instanceof String) {
                String jsString = (String) jsFunction;
                cx.evaluateString(scope, jsString, "Load JavaScriptString", 0, null);
            }
        } catch (ScriptException e) {
            log.error(e.getMessage(), e);
        } finally {
            RhinoEngine.exitContext();
        }
		
	}

}
