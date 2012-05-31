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
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;

import java.rmi.RemoteException;

public class LifeCycleAdminService {
    private static final Log log = LogFactory.getLog(LifeCycleAdminService.class);

    private final String serviceName = "CustomLifecyclesChecklistAdminService";
    private CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub;
    private String endPoint;

    public LifeCycleAdminService(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        customLifecyclesChecklistAdminServiceStub = new CustomLifecyclesChecklistAdminServiceStub(endPoint);
    }

    public void addAspect(String sessionCookie, String resourcePath, String aspectName)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        customLifecyclesChecklistAdminServiceStub.addAspect(resourcePath, aspectName);
    }

    public void invokeAspect(String sessionCookie, String resourcePath, String aspectName,
                             String action, String[] items)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        customLifecyclesChecklistAdminServiceStub.invokeAspect(resourcePath, aspectName, action, items);
    }

    public void invokeAspectWithParams(String sessionCookie, String resourcePath, String aspectName,
                                       String action, String[] items, ArrayOfString[] parameters)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        customLifecyclesChecklistAdminServiceStub.invokeAspectWithParams(resourcePath, aspectName,
                                                                         action, items, parameters);
    }

    public void removeAspect(String sessionCookie, String resourcePath, String aspectName)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        customLifecyclesChecklistAdminServiceStub.removeAspect(resourcePath, aspectName);
    }

    public LifecycleBean getLifecycleBean(String sessionCookie, String resourcePath)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        return customLifecyclesChecklistAdminServiceStub.getLifecycleBean(resourcePath);
    }

    public String[] getAllDependencies(String sessionCookie, String resourcePath)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, customLifecyclesChecklistAdminServiceStub);
        return customLifecyclesChecklistAdminServiceStub.getAllDependencies(resourcePath);
    }

}
