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
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "ApplicationManagementService";
        stub = new ApplicationManagementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(90000);

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

    public PHPAppsWrapper getPagedPhpAppsSummary(String phpappSearchString, int pageNumber)
            throws AxisFault {
        try {
            return stub.getPagedPhpAppsSummary(phpappSearchString , pageNumber);
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
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public boolean isInstanceUp(){
         return isInstanceUp;
    }


    public boolean getIsInstanceUpFromLb(){
        //TODO get state from LB service  and set
        return isInstanceUp;
    }

    public String[] getBaseImages() throws AxisFault {
        //TODO get base images from autoscaler
        try {
            return stub.getImages();
        } catch (RemoteException e) {
            handleException("cannot.get.images", e);
        }
//        String images[] = new String[5];
//        images[0] = "image1";
//        images[1] = "image2";
//        images[2] = "image3";
//        images[3] = "image4";
//        images[4] = "image5";
//        return images;
        return null;
    }
    
    public void startInstance(String image, String tenant) throws AxisFault {
        try {
            stub.startInstance(image, tenant);
        } catch (RemoteException e) {
            handleException("cannot.start.instance" , e);
        }
    }

}
