/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.notifications.ui.worklist;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.notifications.worklist.stub.ClaimService;
import org.wso2.carbon.governance.notifications.worklist.stub.ClaimServiceStub;
import org.wso2.carbon.governance.notifications.worklist.stub.xsd.Cust_type0;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.TaskOperationsStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.GetAllRolesNamesUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.GetUserStoreInfoUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class HumanTaskClient {

    private TaskOperationsStub htStub;
    private UserAdminStub umStub;
    private ClaimServiceStub wlStub;

    //TODO: Create static initializer for these
    private static final String SERVER_URL = "https://localhost:9443/services/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public HumanTaskClient(ServletConfig config, HttpSession session) throws AxisFault {
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String backendServerURL = SERVER_URL != null ? SERVER_URL :
                CarbonUIUtil.getServerURL(config.getServletContext(), session);

        htStub = new TaskOperationsStub(configContext, backendServerURL + "taskOperations");
        configureServiceClient(htStub, session);

        umStub = new UserAdminStub(configContext, backendServerURL + "UserAdmin");
        configureServiceClient(umStub, session);

        wlStub = new ClaimServiceStub(configContext, backendServerURL + "ClaimService");
        configureServiceClient(wlStub, session);
    }

    private void configureServiceClient(Stub stub, HttpSession session) {
        ServiceClient client;Options options;
        client = stub._getServiceClient();
        options = client.getOptions();
        if (USERNAME != null && PASSWORD != null) {
            CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", client);
        } else {
            options.setProperty(HTTPConstants.COOKIE_STRING,
                    session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE));
        }
        options.setManageSession(true);
    }

    public TTaskSimpleQueryResultRow[] getWorkItems()
            throws IllegalArgumentFault, IllegalStateFault, RemoteException {
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);

        TTaskSimpleQueryResultSet resultSet = htStub.simpleQuery(queryInput);
        if (resultSet == null || resultSet.getRow() == null || resultSet.getRow().length == 0) {
            return new TTaskSimpleQueryResultRow[0];
        }
        return resultSet.getRow();
    }

    public String[] getRoles() throws RemoteException, GetAllRolesNamesUserAdminExceptionException,
            GetUserStoreInfoUserAdminExceptionException {
        //TODO: This operations needs to make use of the cache for good reasons.
        FlaggedName[] allRolesNames = umStub.getAllRolesNames();
        String everyOneRole = umStub.getUserStoreInfo().getEveryOneRole();
        List<String> roles = new LinkedList<String>();
        for (FlaggedName role : allRolesNames) {
            String name = role.getItemName();
            if (!name.equals(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME) && !name.equals(
                    everyOneRole)) {
                roles.add(name);
            }
        }
        return roles.toArray(new String[roles.size()]);
    }

    public void createTask(String role, String description, String priority)
            throws RemoteException {
        //TODO: Make the method body meaningful
        Cust_type0 custom = new Cust_type0();
        custom.setFirstname("sanjaya");
        custom.setLastname("vithanagama");
        custom.setId("235235");
        int rand = Integer.parseInt(priority);
        wlStub.approve(custom, 2600 + rand, "LK", rand, new GregorianCalendar(2012, 2, 9, 1, 1, 1));
    }

}
