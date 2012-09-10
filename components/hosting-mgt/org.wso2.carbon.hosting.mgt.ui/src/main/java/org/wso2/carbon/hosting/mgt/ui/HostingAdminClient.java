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
import org.wso2.carbon.hosting.mgt.stub.types.carbon.AppsWrapper;


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

    public void uploadCartridgeApps(FileUploadData[] fileUploadDataList, String cartridge) throws AxisFault {
        try {
            stub.uploadApp(fileUploadDataList, cartridge);
        } catch (RemoteException e) {
            handleException("Cannot upload Web application.", e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public AppsWrapper getPagedAppsSummary(String cartridge)
            throws AxisFault {
        AppsWrapper appsWrapper = null;
        try {
            appsWrapper = stub.getPagedAppsSummary(cartridge);
            String[] phpApps = appsWrapper.getApps(); //For testing whether the apps are null
            if((phpApps != null && phpApps[0] == null) || phpApps == null){
                appsWrapper.setApps(null);
            }
        } catch (RemoteException e) {
            handleException("Cannot retrieve application data. Backend service may be unavailable.", e);
        }
        return appsWrapper;
    }


    public void deleteAllApps(String cartridge) throws AxisFault {
        try {
            stub.deleteAllApps(cartridge);
        } catch (RemoteException e) {
            handleException("Cannot delete applications. Backend service may be unavailable", e);
        }
    }

    public void deleteApps(String[] appFileNames,String cartridge) throws AxisFault {
        try {
            stub.deleteApps(appFileNames, cartridge) ;
        } catch (RemoteException e) {
            handleException("Cannot delete applications. Backend service may be unavailable", e);
        }
    }
    public String[] getCartridges() throws AxisFault {
        try {
            String cartridges[] =  stub.getCartridgeTitles();
            return cartridges;
        } catch (RemoteException e) {
            handleException("Error while retrieving cartridges.", e);
        }
        return null;
    }


}
