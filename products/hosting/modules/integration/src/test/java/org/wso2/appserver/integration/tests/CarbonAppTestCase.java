/*
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.appserver.integration.tests;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertTrue;

public class CarbonAppTestCase {

    private static final Log log = LogFactory.getLog(CarbonAppTestCase.class);
    private List<UploadedFileItem> uploadServiceTypeList;

    String SERVICE_URL = null;

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private ApplicationAdminStub applicationAdminStub;

    @BeforeMethod(groups = {"wso2.as"})
    public void login() throws java.lang.Exception {
        log.info("****Inside Login Service in Carbon Apps Test*****");
        String loggedInSessionCookie = util.login();

        SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/ApplicationAdmin";

        applicationAdminStub =
                new ApplicationAdminStub(SERVICE_URL);
        ServiceClient client = applicationAdminStub._getServiceClient();
        Options options = client.getOptions();

        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
        options.setManageSession(true);
        //Increase the time out when sending large attachments
        options.setTimeOutInMilliSeconds(10000);
        uploadServiceTypeList = new ArrayList<UploadedFileItem>();
    }

    @AfterClass(groups = {"wso2.as"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("****Inside Log Out Service in Carbon Apps Test*****");
    }

    @Test(groups = {"wso2.as"})
    public void init() {
        System.out.println("Initializing the test");
    }

    @Test(groups = {"wso2.as"}, dependsOnMethods = {"init"}, enabled = true)
    public void testAppDeployments() throws RemoteException, XMLStreamException, ApplicationAdminExceptionException {
        Set<String> apps = new HashSet<String>();

        if (applicationAdminStub.listAllApplications() != null) {

            for (String app : applicationAdminStub.listAllApplications()) {
                log.info("Found application - " + app);
                apps.add(app);
            }

            assertTrue(apps.contains("ShoppingCartSample"));
        }
    }

    @Test(groups = {"wso2.as"}, dependsOnMethods = {"testAppDeployments"}, enabled = true)
    public void testDeployedApps() throws RemoteException, XMLStreamException, ApplicationAdminExceptionException {
        //Do the testing of ShoppingCartSample

    }

}
