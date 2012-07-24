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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.PHPAppsWrapper;


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
    private static boolean isInstanceUp = false;

    public HostingAdminClient( Locale locale, String cookie, ConfigurationContext configCtx,
                               String backendServerURL) throws AxisFault {
        String serviceURL = backendServerURL + "ApplicationManagementService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ApplicationManagementServiceStub(configCtx, serviceURL);
        stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(270000);
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, 270000);
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, 270000);
        ServiceClient client = stub._getServiceClient();
        client.getOptions().setTimeOutInMilliSeconds(270000);
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

    }

    public void uploadWebapp(FileUploadData[] fileUploadDataList) throws AxisFault {
        try {
            stub.uploadWebapp(fileUploadDataList);
        } catch (RemoteException e) {
            handleException("Cannot upload Web application.", e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public PHPAppsWrapper getPagedPhpAppsSummary(String phpappSearchString, int pageNumber)
            throws AxisFault {
        PHPAppsWrapper phpAppsWrapper = null;
        try {
            phpAppsWrapper = stub.getPagedPhpAppsSummary(phpappSearchString , pageNumber);
            String[] phpApps = phpAppsWrapper.getPhpapps(); //For testing whether the PHP apps are null
            if(phpApps != null && phpApps[0] == null){
                phpAppsWrapper.setPhpapps(null);
                phpAppsWrapper.setEndPoints(null);
            }
        } catch (RemoteException e) {
            handleException("Cannot retrieve PHP app data. Backend service may be unavailable.", e);
        }
        return phpAppsWrapper;
    }


    public void deleteAllPhpApps() throws AxisFault {
        try {
            stub.deleteAllPhpApps();
        } catch (RemoteException e) {
            handleException("Cannot delete PHP applications. Backend service may be unavailable", e);
        }
    }

    public void deletePhpApps(String[] phpAppFileNames) throws AxisFault {
        try {
            stub.deletePhpApps(phpAppFileNames) ;
        } catch (RemoteException e) {
            handleException("Cannot delete PHP applications. Backend service may be unavailable", e);
        }
    }

    public boolean isInstanceUp() throws AxisFault {
        try {
            isInstanceUp = stub.isInstanceForTenantUp();
        } catch (RemoteException e) {
            String msg = "Error while calling isInstanceUp";
            throw new AxisFault(msg);
        }
        return isInstanceUp;
    }

    /**
     * Not used for this release
     * @return
     * @throws AxisFault
     */
    public String[] getBaseImages() throws AxisFault {
        try {
            String images[] =  stub.getImages();
            return images;
        } catch (RemoteException e) {
            handleException("Error while retrieving images.", e);
        }
        return null;
    }

    public String startInstance(String image) throws AxisFault {
        String ip = "122.22.22.22";
//          try {
//            ip =  stub.startInstance(image);
//        } catch (RemoteException e) {
//            handleException("Error while starting instance" , e);
//        }
//

        return ip;
    }

}
