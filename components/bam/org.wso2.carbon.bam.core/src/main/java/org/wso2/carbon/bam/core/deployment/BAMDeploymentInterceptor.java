/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.bam.core.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.BAMConstants;

import java.util.ArrayList;

/**
 * Sets permissions for internal data services at deployment time.
 */
public class BAMDeploymentInterceptor implements AxisObserver {
    private static final Log log = LogFactory.getLog(BAMDeploymentInterceptor.class);

    private static final String[] dataDS = new String[]{
            BAMConstants.BAM_CONFIGURATION_SERVICE,
            BAMConstants.BAM_DATACOLLECTION_SERVICE,
            BAMConstants.BAM_SUMMARYGENERATION_SERVICE
    };

    private static final String[] queryDS = new String[]{
            BAMConstants.BAM_STATQUERY_SERVICE,
            BAMConstants.BAM_SUMMARYQUERY_SERVICE
    };


    public void init(AxisConfiguration axisConfiguration) {
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService service) {
        //AdminService params should be added to the service Group
    }

    // This method adds the required parameters to make the BAMDataCollectionDS and BAMStatQueryDS admin services.
    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup serviceGroup) {
        int eventType = axisEvent.getEventType();
        String sgName = serviceGroup.getServiceGroupName();
        if (eventType == AxisEvent.SERVICE_DEPLOY) {
            for (String dsName : dataDS) {
                if (sgName.compareTo(dsName) == 0) {
                    addAdminParameters(serviceGroup, "/permission/bam/dataCollection");
                }
            }

            for (String dsName : queryDS) {
                if (sgName.compareTo(dsName) == 0) {
                    addAdminParameters(serviceGroup, "/permission/bam/view");
                }
            }

        }

    }

    private void addAdminParameters(AxisServiceGroup serviceGroup, String authAction) {
//        try {
//            Parameter param1 = new Parameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true");
//            Parameter param2 = new Parameter(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME, "true");
//            Parameter param3 = new Parameter("AuthorizationAction", authAction);
//            serviceGroup.addParameter(param1);
//            serviceGroup.addParameter(param2);
//            serviceGroup.addParameter(param3);

//            Iterator<AxisService> serviceItr = serviceGroup.getServices();
//            while (serviceItr.hasNext()) {
//                AxisService service = serviceItr.next();
//                service.addExposedTransport("local");
//            }

//            if (log.isInfoEnabled()) {
//                log.info("Admin service parameters added to " + serviceGroup.getServiceGroupName()
//                        + " with AuthAction: " + authAction);
//            }
//        } catch (AxisFault axisFault) {
//            String msg = "Error adding admin service parameters to " + serviceGroup.getServiceGroupName();
//
//            if (log.isErrorEnabled()) {
//                log.error(msg, axisFault);
//            }
//        }
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
    }

    public void addParameter(Parameter parameter) throws AxisFault {
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
    }

    public void deserializeParameters(OMElement element) throws AxisFault {
    }

    public Parameter getParameter(String s) {
        return null;
    }

    public ArrayList<Parameter> getParameters() {
        return null;
    }

    public boolean isParameterLocked(String s) {
        return false;
    }

}
