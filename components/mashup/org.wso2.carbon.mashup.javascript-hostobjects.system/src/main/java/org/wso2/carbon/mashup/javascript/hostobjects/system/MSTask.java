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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngine;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.URL;

public class MSTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(MSTask.class);

	@Override
	public void execute() {

        // Inject the incoming MessageContext to the Rhino Context. Some
        // host objects need access to the MessageContext. Eg: FileSystem,
        // WSRequest
        try {

        	String serviceNameProperty = getProperties().get(MSTaskConstants.AXIS_SERVICE);
        	
    		String tidProp = this.getProperties().get(TaskInfo.TENANT_ID_PROP);
    		if (tidProp == null) {
    			throw new RuntimeException("Cannot determine the tenant id for the scheduled service: " + 
    					serviceNameProperty);
    		}
    		int tid = Integer.parseInt(tidProp);
    		
    		AxisService axisService = this.lookupAxisService(tid, serviceNameProperty);
        	if (axisService == null) {
    			throw new RuntimeException("Cannot determine the tenant id for the scheduled service: " + 
    					serviceNameProperty);
        	}

            ConfigurationContext configurationContext;
            // retrieves the ConfigurationContext object from the Rhino Engine
            Object configurationContextObject =
            		TasksDSComponent.getConfigurationContextService().getServerConfigContext();;
            if (configurationContextObject != null &&
                configurationContextObject instanceof ConfigurationContext) {
                configurationContext = (ConfigurationContext) configurationContextObject;
            } else {
                throw new CarbonException(
                        "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
            }
            
            Object jsFunction = axisService.getParameterValue(getProperties().get(MSTaskConstants.TASK_NAME));
            Object[] parameters = (Object[]) MSTaskUtils.fromString(getProperties().get(MSTaskConstants.FUNCTION_PARAMETERS));

            ContextFactory factory = (ContextFactory) axisService.getParameterValue(MSTaskConstants.CONTEXT_FACTORY);
            Context cx = RhinoEngine.enterContext(factory);
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
            RhinoEngine.putContextProperty(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configurationContext);

            AxisConfiguration axisConfig = configurationContext.
                    getAxisConfiguration();

            URL repoURL = axisConfig.getRepository();
            if (repoURL != null) {
                JavaScriptEngine.axis2RepositoryLocation = repoURL.getPath();
            }

            Object[] args;

            ScriptableObject scope = (ScriptableObject) axisService.getParameterValue(MSTaskConstants.TASK_SCOPE);
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
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CarbonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            RhinoEngine.exitContext();
        }
		
	}

	private AxisService lookupAxisService(int tid, String serviceName) {
		ConfigurationContext mainConfigCtx = TasksDSComponent.getConfigurationContextService().
				getServerConfigContext();
		AxisConfiguration tenantAxisConf;
		if (tid == MultitenantConstants.SUPER_TENANT_ID) {
			tenantAxisConf = mainConfigCtx.getAxisConfiguration();
		} else {
		    String tenantDomain = MSTaskUtils.getTenantDomainFromId(tid);
		    tenantAxisConf = TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, 
		    		mainConfigCtx);
		}		

		try {
			if (tenantAxisConf != null) {
			    return tenantAxisConf.getService(serviceName);
			} else {
				return null;
			}
		} catch (AxisFault e) {
			return null;
		}
	}
}
