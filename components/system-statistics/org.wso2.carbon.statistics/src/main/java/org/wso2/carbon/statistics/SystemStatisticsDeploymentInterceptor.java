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
package org.wso2.carbon.statistics;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.statistics.internal.ResponseTimeProcessor;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * System statistics deployment interceptor which will set Statistics related parameter to the
 * Axis operations
 *
 * @scr.component name="org.wso2.carbon.statistics.SystemStatisticsDeploymentInterceptor"
 * immediate="true"
 *
 */
public class SystemStatisticsDeploymentInterceptor implements AxisObserver {

    protected void activate(ComponentContext ctxt) {
        BundleContext bundleCtx = ctxt.getBundleContext();

        // Publish the OSGi service
        Properties props = new Properties();
        props.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
        bundleCtx.registerService(AxisObserver.class.getName(), this, props);

        PreAxisConfigurationPopulationObserver preAxisConfigObserver =
            new PreAxisConfigurationPopulationObserver() {
                public void createdAxisConfiguration(AxisConfiguration axisConfiguration) {
                    axisConfiguration.addObservers(new SystemStatisticsDeploymentInterceptor());
                }
            };
        bundleCtx.registerService(PreAxisConfigurationPopulationObserver.class.getName(),
                                  preAxisConfigObserver, null);

    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
    }

    public void init(AxisConfiguration axisConfiguration) {
        // Nothing to implement
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        if(SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
            axisService.isClientSide()) {
            return;
        }
        if (axisEvent.getEventType() == AxisEvent.SERVICE_DEPLOY) {
            for (Iterator iter = axisService.getOperations(); iter.hasNext();) {
                AxisOperation op = (AxisOperation) iter.next();

                // IN operation counter
                Parameter inOpCounter = new Parameter();
                inOpCounter.setName(StatisticsConstants.IN_OPERATION_COUNTER);
                inOpCounter.setValue(new AtomicInteger(0));
                try {
                    op.addParameter(inOpCounter);
                } catch (AxisFault ignored) { // will not occur
                }

                // OUT operation counter
                Parameter outOpCounter = new Parameter();
                outOpCounter.setName(StatisticsConstants.OUT_OPERATION_COUNTER);
                outOpCounter.setValue(new AtomicInteger(0));
                try {
                    op.addParameter(outOpCounter);
                } catch (AxisFault ignored) { // will not occur
                }

                // Operation response time processor
                Parameter responseTimeProcessor = new Parameter();
                responseTimeProcessor.setName(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
                responseTimeProcessor.setValue(new ResponseTimeProcessor());
                try {
                    op.addParameter(responseTimeProcessor);
                } catch (AxisFault axisFault) { // will not occur
                }
            }

            // Service response time processor
            Parameter responseTimeProcessor = new Parameter();
            responseTimeProcessor.setName(StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR);
            responseTimeProcessor.setValue(new ResponseTimeProcessor());
            try {
                axisService.addParameter(responseTimeProcessor);
            } catch (AxisFault axisFault) { // will not occur
            }
        }
        else if(axisEvent.getEventType() == AxisEvent.SERVICE_REMOVE) {
            //TODO remove persisted stats for service
        }
    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {
        // Nothing to implement
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
        // Nothing to implement
    }

    public void addParameter(Parameter parameter) throws AxisFault {
        // Nothing to implement
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
        // Nothing to implement
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
        // Nothing to implement
    }

    public Parameter getParameter(String s) {
        return null;  // Nothing to implement
    }

    public ArrayList<Parameter> getParameters() {
        return null;  // Nothing to implement
    }

    public boolean isParameterLocked(String s) {
        return false;  // Nothing to implement
    }
}
