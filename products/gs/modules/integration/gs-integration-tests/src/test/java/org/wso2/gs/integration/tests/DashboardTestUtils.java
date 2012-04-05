/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.gs.integration.tests;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.SystemOutLogger;
import org.wso2.carbon.dashboard.stub.DashboardServiceStub;
import org.wso2.carbon.dashboard.stub.DashboardUtilServiceStub;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.apache.axis2.AxisFault;

import java.io.File;

/*
GS- DashboardTestUtils - Dashboard
*/
public class DashboardTestUtils {
    private static final Log log = LogFactory.getLog(DashboardTestUtils.class);

    // get the gadget resource path
    public static String getGadgetResourcePath(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + "HelloWorldGadget.xml";
    }

     // get the DashboardServiceStub
    public static DashboardServiceStub getDashboardServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "DashboardService";
        DashboardServiceStub dashboardServiceStub = null;
        try {
            dashboardServiceStub = new DashboardServiceStub(serviceURL);
            ServiceClient client = dashboardServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            dashboardServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            log.error("Unexpected exception thrown in DashboardServiceStub creation");
            axisFault.printStackTrace();
        }
        log.info("DashboardServiceStub created");
        return dashboardServiceStub;
    }

    // This method returns the stub without setting the session Cookie
    public static DashboardUtilServiceStub getDashboardUtilServiceStub() {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "DashboardUtilService";
        DashboardUtilServiceStub dashboardUtilServiceStub = null;
        try {
            dashboardUtilServiceStub = new DashboardUtilServiceStub(serviceURL);
            ServiceClient client = dashboardUtilServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
        } catch (AxisFault axisFault) {
            log.error("Unexpected exception thrown in DashboardUtilServiceStub creation");
            axisFault.printStackTrace();
        }
        log.info("DashboardUtilServiceStub created");
        return dashboardUtilServiceStub;
    }
}
