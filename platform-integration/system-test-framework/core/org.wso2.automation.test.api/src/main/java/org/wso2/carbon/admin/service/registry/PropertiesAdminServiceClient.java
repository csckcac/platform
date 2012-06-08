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

package org.wso2.carbon.admin.service.registry;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.jdbc.meta.strats.StringFieldStrategy;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;

import java.rmi.RemoteException;

public class PropertiesAdminServiceClient {

    private static final Log log = LogFactory.getLog(PropertiesAdminServiceClient.class);

    private PropertiesAdminServiceStub propertiesAdminServiceStub;

    public PropertiesAdminServiceClient(String backendURL, String sessionCookie) throws AxisFault {
        String serviceName = "PropertiesAdminService";
        String endPoint = backendURL + serviceName;
        propertiesAdminServiceStub = new PropertiesAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, propertiesAdminServiceStub);        
    }
    
    public void setRetentionProperties(String path, String mode, String fromDate, String toDate)
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        RetentionBean retentionBean = new RetentionBean();
        retentionBean.setWriteLocked(mode.contains("write"));
        retentionBean.setDeleteLocked(mode.contains("delete"));
        retentionBean.setFromDate(fromDate);
        retentionBean.setToDate(toDate);

        try {
            propertiesAdminServiceStub.setRetentionProperties(path, retentionBean);
        } catch (RemoteException e) {
            log.error("Set retention properties failed " , e);
            throw new RemoteException("Set retention properties failed " , e);
        } catch (PropertiesAdminServiceRegistryExceptionException e) {
            log.error("Set retention lock failed " , e);
            throw new PropertiesAdminServiceRegistryExceptionException("Set retention properties failed " , e);
        }
    }

    public RetentionBean getRetentionProperties(String path)
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        RetentionBean retentionBean = new RetentionBean();
        try {
            retentionBean = propertiesAdminServiceStub.getRetentionProperties(path);
        } catch (RemoteException e) {
            log.error("get retention properties failed " , e);
            throw new RemoteException("get retention properties failed " , e);
        } catch (PropertiesAdminServiceRegistryExceptionException e) {
            log.error("get retention properties failed " , e);
            throw new PropertiesAdminServiceRegistryExceptionException("get retention properties failed " , e);
        }
        return retentionBean;
    }

}
