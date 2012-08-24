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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hosting.mgt.stub.ApplicationManagementServiceStub;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import org.wso2.carbon.hosting.mgt.stub.types.carbon.AppsWrapper;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Client which communicates with the WebappAdmin service
 */
public class HostingAdminClient {
    public static final String BUNDLE = "org.wso2.carbon.hosting.mgt.ui.i18n.Resources";
    private static final Log log = LogFactory.getLog(HostingAdminClient.class);
    private ResourceBundle bundle;
    public ApplicationManagementServiceStub stub;
//    int tenantId = 0;
    //key: tenant,  values: list of cartridges that have instances running
    HashMap<Integer, ArrayList<String>> tenantToCartridges;
//    AutoscaleServiceClient autoscaleServiceClient;
    public HostingAdminClient(String cookie,
                              ConfigurationContext configCtx,
                              String backendServerURL
            //Locale locale,
                              ) throws AxisFault {

//        autoscaleServiceClient = new AutoscaleServiceClient(System.getProperty(CartridgeUIConstants.AUTOSCALER_SERVICE_URL));
//        autoscaleServiceClit.init(true);
        String serviceURL = backendServerURL + "/services/ApplicationManagementService";
//        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ApplicationManagementServiceStub(configCtx, serviceURL);
//        stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(270000);
//        stub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, 270000);
//        stub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, 270000);
        ServiceClient client = stub._getServiceClient();
//        client.getOptions().setTimeOutInMilliSeconds(270000);


        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
//        tenantId = MultitenantUtils.getTenantId(configCtx);
        tenantToCartridges = new HashMap<Integer, ArrayList<String>>();

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
     * stratos_cartridge_create --cartridge-type=<cartridge type>
     *     --cartridge-name=<optional name for the cartridge. default name is the same as cartridge type>
     *         --applications=<optional:comma separated applications with full path to the location>
     *     --min-instances=<optional:minimum number of instances>
     *         --max_instances=<optional:maximum number of instances>
     *
     *
     * @param fileUploadDataList
     * @param cartridgeName
     * @param minInstances
     * @param maxInstances
     * @return
     * @throws AxisFault
     */
   public boolean createCartridge(FileUploadData[] fileUploadDataList, String cartridgeName,
                                  int minInstances, int maxInstances)
           throws AxisFault {
       //to keep whether creation succeeded
       boolean isSuccessful = false;

       if(fileUploadDataList != null){
           try {
               stub.uploadApp(fileUploadDataList, cartridgeName);
           } catch (RemoteException e) {
               handleException("Cannot upload Web application.", e);
           }
       }
       manageSPICartridges(cartridgeName, minInstances, maxInstances);
       return isSuccessful;

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
                appsWrapper.setEndPoints(null);
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



    /**
     * For non wso2 (SPI) cartridges
     * @param cartridge
     * @param minInstances
     * @param maxInstances
     */
    private void manageSPICartridges(String cartridge, int minInstances, int maxInstances){

//        if(tenantToCartridges.containsKey(tenantId)){
//            if(!tenantToCartridges.get(tenantId).contains(cartridge)){
//                startInstance(cartridge);
//                ArrayList<String> cartridgeList = tenantToCartridges.get(tenantId);
//                cartridgeList.add(cartridge);
//                tenantToCartridges.put(tenantId, cartridgeList);
//            }
//        }else{
//            startInstance(cartridge);
//            ArrayList<String> cartridgeList = new ArrayList<String>();
//            cartridgeList.add(cartridge);
//            tenantToCartridges.put(tenantId, cartridgeList);
//        }
    }

     private String startInstance(String cartridge){

//         String cartridgeDomain = cartridge  + "." +  tenantId ;
//         try{
////             autoscaleServiceClient.startInstance("wso2.php.domain");
//         }catch (Exception e){
//             String msg = "Error while calling auto scaler to start instance";
//             log.error(msg);
//         }
//         log.info("Starting instance for cartridge : "  + cartridge + ", tenant : "  + tenantId);
         String publicIp = "";

         return publicIp;
     }

    private boolean isCartridgeForTenantAvailable(String cartridge){

//        if(tenantToCartridges.containsKey(tenantId)){
//            if(!tenantToCartridges.get(tenantId).contains(cartridge)){
//                return  true;
//            }
//        }
        return false;
    }

}
