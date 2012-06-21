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
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.notifications.worklist.stub.WorkListServiceStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.*;
import org.wso2.carbon.registry.common.eventing.WorkListConfig;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.*;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public class HumanTaskClient {

    private static final Log log = LogFactory.getLog(HumanTaskClient.class);

    private HumanTaskClientAPIAdminStub htStub;
    private UserAdminStub umStub;
    private WorkListServiceStub wlStub;

    private static WorkListConfig workListConfig = new WorkListConfig();

    public HumanTaskClient(ServletConfig config, HttpSession session) throws AxisFault {
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String backendServerURL =
                workListConfig.getServerURL() != null ? workListConfig.getServerURL() :
                CarbonUIUtil.getServerURL(config.getServletContext(), session);

        htStub = new HumanTaskClientAPIAdminStub(configContext, backendServerURL + "HumanTaskClientAPIAdmin");
        configureServiceClient(htStub, session);

        umStub = new UserAdminStub(configContext, backendServerURL + "UserAdmin");
        configureServiceClient(umStub, session);

        wlStub = new WorkListServiceStub(configContext, backendServerURL + "WorkListService");
        configureServiceClient(wlStub, session);
    }

    private void configureServiceClient(Stub stub, HttpSession session) {
        ServiceClient client;Options options;
        client = stub._getServiceClient();
        options = client.getOptions();
        if (workListConfig.getUsername() != null && workListConfig.getPassword() != null) {
            CarbonUtils.setBasicAccessSecurityHeaders(workListConfig.getUsername(),
                    workListConfig.getPassword(), client);
        } else {
            options.setProperty(HTTPConstants.COOKIE_STRING,
                    session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE));
        }
        options.setManageSession(true);
    }

    public WorkItem[] getWorkItems()
            throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault, RemoteException {
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);

        TTaskSimpleQueryResultSet resultSet = htStub.simpleQuery(queryInput);
        if (resultSet == null || resultSet.getRow() == null || resultSet.getRow().length == 0) {
            return new WorkItem[0];
        }
        List<WorkItem> workItems = new LinkedList<WorkItem>();
        for (TTaskSimpleQueryResultRow row : resultSet.getRow()) {
            URI id = row.getId();
            workItems.add(new WorkItem(id, row.getPresentationSubject(),
                    row.getPresentationName(), row.getPriority(), row.getStatus(),
                    row.getCreatedTime(), htStub.loadTask(id).getActualOwner().getTUser()));
        }
        return workItems.toArray(new WorkItem[workItems.size()]);
    }

    public String[] getRoles(HttpSession session) throws RemoteException,
            GetAllRolesNamesUserAdminExceptionException,
            GetUserStoreInfoUserAdminExceptionException,
            GetRolesOfCurrentUserUserAdminExceptionException {
        //TODO: This operations needs to make use of the cache for good reasons.

        FlaggedName[] allRolesNames = umStub.getRolesOfCurrentUser();
        String adminRole = umStub.getUserStoreInfo().getAdminRole();

        for (FlaggedName role : allRolesNames) {
            String name = role.getItemName();
            if (name.equals(adminRole)) {
                allRolesNames = umStub.getAllRolesNames();
                break;
            }
        }

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
        wlStub.addTask(role, description, Integer.parseInt(priority));
    }

    public void completeTask(String id)
            throws RemoteException, IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            IllegalStateFault {
        try {
            htStub.complete(new URI(id), "<WorkResponse>true</WorkResponse>");
        } catch (URI.MalformedURIException e) {
            log.error("Invalid task identifier", e);
        }
    }

}
