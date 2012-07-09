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
package org.wso2.carbon.automation.api.clients.registry;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.registry.activities.stub.ActivityAdminServiceStub;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;

import java.rmi.RemoteException;

public class ActivityAdminServiceClient {
    private static final Log log = LogFactory.getLog(ActivityAdminServiceClient.class);

    private final String serviceName = "ActivityAdminService";
    private ActivityAdminServiceStub activityAdminServiceStub;
    private String endPoint;

    public final static String FILTER_ALL = "all";
    public final static String FILTER_ASSOCIATE_ASPECT = "associateAspect";
    public final static String FILTER_RESOURCE_ADDED = "resourceAdd";
    public final static String FILTER_RESOURCE_UPDATE = "resourceUpdate";

    public ActivityAdminServiceClient(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        activityAdminServiceStub = new ActivityAdminServiceStub(endPoint);
    }

    public ActivityBean getActivities(String sessionCookie, String userName, String resourcePath
            , String fromDate, String toDate, String filter, int page)
            throws RegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, activityAdminServiceStub);
        return activityAdminServiceStub.getActivities(userName, resourcePath, fromDate, toDate
                , filter, page + "", sessionCookie);
    }
}
