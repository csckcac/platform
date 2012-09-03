/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.hosting.mgt.cli;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.AppsWrapper;

import java.rmi.RemoteException;

/**
 * Client which communicates with the Backend service at ADS(Artifact Deployment Server)
 */
public class HostingAdminClient {
    private static final Log log = LogFactory.getLog(HostingAdminClient.class);
    public ApplicationManagementServiceStub stub;

    public HostingAdminClient(String cookie,
                              ConfigurationContext configCtx,
                              String backendServerURL) {

        try {
        String serviceURL = backendServerURL + "/services/ApplicationManagementService";
            stub = new ApplicationManagementServiceStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            log.error("Error while connecting to Back end service ");
        }
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

    }


    /**
     * To handle
     *  stratos_cartridge_upload_apps
     *  --name=<cartridge_name>
     *      --applications=<comma separated applicatoin list with full path to the location>
     *
     * @param fileUploadDataList
     * @param cartridge
     * @throws AxisFault
     */
    public void uploadCartridgeApps(FileUploadData[] fileUploadDataList, String cartridge)
            throws AxisFault {
//        tenantId = MultitenantUtils.getTenantId()
        try {
            log.info("Going to upload apps");
            stub.uploadApp(fileUploadDataList, cartridge);
        } catch (RemoteException e) {
            handleException("Cannot upload Web application.", e);
        }
    }



    /**
     * To handle
     *  stratos_cartridge_list_types
     This command will list the cartridge types available to the tenant in Stratos
     eg output
                  PHP
                  Jetty
                  Jboss

     * @return Defined cartridge types as a list
     * @throws AxisFault
     */
    public String[] getCartridges() throws AxisFault {
        try {
            String cartridges[] =  stub.getCartridgeTitles();
            return cartridges;
        } catch (RemoteException e) {
            handleException("Error while retrieving cartridges.", e);
        }
        return null;
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


    public void register(String cartridgeType, int min, int max, String optionalName, boolean attachVolume)
            throws AxisFault {
        try {
            stub.registerCartridge(cartridgeType, min, max, optionalName, attachVolume);
        } catch (RemoteException e) {
            handleException("Cartridge regiter failure ", e);
        }
    }
}
