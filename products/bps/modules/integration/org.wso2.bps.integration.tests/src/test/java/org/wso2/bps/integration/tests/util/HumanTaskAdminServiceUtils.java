/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.util;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagement;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Helper class to initialize admin service clients
 */
public class HumanTaskAdminServiceUtils {
    public static UserAdminStub getUserAdminStub() throws Exception {
        final String USER_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                   ":" + FrameworkSettings.HTTPS_PORT +
                                                   "/services/UserAdmin";
        UserAdminStub userAdminStub = new UserAdminStub(USER_MANAGEMENT_SERVICE_URL);
        LoginLogoutUtil util = new LoginLogoutUtil();
        String loggedInSessionCookie = util.login();

        ServiceClient serviceClient = userAdminStub._getServiceClient();
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        serviceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                         loggedInSessionCookie);
        return userAdminStub;
    }

    public static HumanTaskClientAPIAdminStub getTaskOperationServiceStub() throws Exception {
        return getTaskOperationServiceStub(HumanTaskTestConstants.CLERK1_USER,
                                           HumanTaskTestConstants.CLERK1_PASSWORD);
    }

    public static HumanTaskClientAPIAdminStub getTaskOperationServiceStub(String userName,
                                                                          String password)
            throws Exception {
        String TASK_OPERATIONS_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                             ":" + FrameworkSettings.HTTPS_PORT +
                                             "/services/HumanTaskClientAPIAdmin";
        HumanTaskClientAPIAdminStub taskOperationsStub = new HumanTaskClientAPIAdminStub(TASK_OPERATIONS_SERVICE_URL);

        ServiceClient serviceClient = taskOperationsStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(userName,
                                                  password, serviceClient);
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        return taskOperationsStub;
    }

    public static InstanceManagementServiceStub getInstanceManagementServiceStub()
            throws Exception {
        final String INSTANCE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                       ":" + FrameworkSettings.HTTPS_PORT +
                                                       "/services/InstanceManagementService";
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);

        InstanceManagementServiceStub instanceManagementServiceStub =
                new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        ServiceClient instanceManagementServiceClient = instanceManagementServiceStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", instanceManagementServiceClient);
        Options instanceManagementServiceClientOptions = instanceManagementServiceClient.getOptions();
        instanceManagementServiceClientOptions.setManageSession(true);
        return instanceManagementServiceStub;
    }

    public static HumanTaskPackageManagementStub getPackageManagementServiceStub()
            throws Exception {

        String packageManagementServiceURL = "https://" + FrameworkSettings.HOST_NAME +
                                             ":" + FrameworkSettings.HTTPS_PORT +
                                             "/services/HumanTaskPackageManagement";

        HumanTaskPackageManagementStub packageManagementStub = new HumanTaskPackageManagementStub(packageManagementServiceURL);
        ServiceClient serviceClient = packageManagementStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", serviceClient);
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);

        return packageManagementStub;
    }
}
