/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.hosting.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.PHPappsWrapper;
//import org.wso2.carbon.hosting.mgt.stub.types.carbon.PHPappsWrapper;


import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Client which communicates with the WebappAdmin service
 */
public class HostingAdminClient {
    public static final String BUNDLE = "org.wso2.carbon.hosting.mgt.ui.i18n.Resources";
    public static final int MILLISECONDS_PER_MINUTE = 60 * 1000;
    private static final Log log = LogFactory.getLog(HostingAdminClient.class);
    private ResourceBundle bundle;
    public ApplicationManagementServiceStub stub;

    public HostingAdminClient(String cookie,
                             String backendServerURL,
                             ConfigurationContext configCtx,
                             Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ApplicationManagementService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ApplicationManagementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }


    public void uploadWebapp(FileUploadData[] fileUploadDataList) throws AxisFault {
        try {
            stub.uploadWebapp(fileUploadDataList);
        } catch (RemoteException e) {
            handleException("cannot.upload.webapps", e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public String[] listPhpApps(){
        try {
            PHPappsWrapper phPappsWrapper = getPagedPhpAppsSummary("d", 0);
            log.info(phPappsWrapper.getPhpapps().length);
            return stub.listPhpApplications();
        } catch (RemoteException e) {
            String msg = "Cannot list php apps. Backend service may be unvailable";
            log.error(msg);
        }
        return null;
    }

    public PHPappsWrapper getPagedPhpAppsSummary(String phpappSearchString, int pageNumber)
            throws AxisFault {
        try {
            return stub.getPagedPhpAppsSummary("test" , 1);
        } catch (RemoteException e) {
            handleException("cannot.get.phpapp.data", e);
        }
        return null;
    }


    public void deleteAllPhpApps() throws AxisFault {
        try {
            stub.deleteAllPhpApps();
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public void deletePhpApps(String[] phpAppFileNames) throws AxisFault {
        try {
            stub.deletePhpApps(phpAppFileNames) ;
            log.info("success");
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }



}
