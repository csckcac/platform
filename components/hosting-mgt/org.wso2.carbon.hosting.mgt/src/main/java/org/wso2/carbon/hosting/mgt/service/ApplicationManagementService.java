/**
 * 
 */
package org.wso2.carbon.hosting.mgt.service;


import org.apache.axis2.AxisFault;
import org.wso2.carbon.hosting.mgt.internal.Store;
import org.wso2.carbon.hosting.mgt.openstack.db.OpenstackDAO;
import org.wso2.carbon.hosting.mgt.utils.FileUploadData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hosting.mgt.clients.AutoscaleServiceClient;
import org.wso2.carbon.hosting.mgt.utils.PHPAppsWrapper;
import org.wso2.carbon.hosting.mgt.utils.PHPCartridgeConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


/**
 * @author wso2
 *
 */
public class ApplicationManagementService extends AbstractAdmin{

    private static final Log log = LogFactory.getLog(ApplicationManagementService.class);
    public static final String FILE_DEPLOYMENT_FOLDER = "phpapps";
    AutoscaleServiceClient client;
//    HashMap<String, String> imageIdtoNameMap;  to active for next release
    OpenstackDAO openstackDAO;

    public ApplicationManagementService() throws AxisFault{
//        imageIdtoNameMap = new HashMap<String, String>(); to active for next release
        initAutoscaler();
    }

    public void initAutoscaler() throws AxisFault {
        client = new AutoscaleServiceClient(System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
        try {
			client.init(true);
		} catch (Exception e) {
			throw new AxisFault(e.getMessage(), e);
		}
    }
    /**
         * Upload a File
         *
         * @param fileUploadDataList Array of data representing the PHP apps(.zip) that are to be uploaded
         * @return true - if upload was successful
         * @throws org.apache.axis2.AxisFault If an error occurs while uploading
         */
    public boolean uploadWebapp(FileUploadData[] fileUploadDataList ) throws AxisFault {
        File phpAppsDir = new File(getWebappDeploymentDirPath());

        if (!phpAppsDir.exists() && !phpAppsDir.mkdirs()) {
            log.warn("Could not create directory " + phpAppsDir.getAbsolutePath());
        }

        for (FileUploadData uploadData : fileUploadDataList) {
            String fileName = uploadData.getFileName();
            File destFile = new File(phpAppsDir, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destFile);
                uploadData.getDataHandler().writeTo(fos);
            } catch (IOException e) {
                handleException("Error occurred while uploading the PHP application " + fileName, e);
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    log.warn("Could not close file " + destFile.getAbsolutePath());
                }
            }
            log.info("Files are successfully uploaded !" );
        }
        return true;
    }


    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }


    protected String getWebappDeploymentDirPath() {
        return getAxisConfig().getRepository().getPath() + File.separator + FILE_DEPLOYMENT_FOLDER;
    }

    /**
     * Retrieve and display the applications
     */
    public String[] listPhpApplications() {
        String phpAppPath = getWebappDeploymentDirPath();
        File phpAppDirectory = new File(phpAppPath);
        String[] children;
        if(phpAppDirectory.exists()){
            // This return any files that ends with '.zip'.
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            };
            children = phpAppDirectory.list(filter);
            if(children.length == 0){
                children = null;
            }else {
                for(int i = 0; i < children.length; i++){
                    children[i] = children[i].substring(0, children[i].indexOf(".zip"));
                }
            }
        }else {
            children = null;
        }   
        return children;

    }

    public PHPAppsWrapper getPagedPhpAppsSummary(String phpAppSearchString, int pageNumber){
         PHPAppsWrapper phpAppsWrapper = new PHPAppsWrapper();
        String[] phpApps= listPhpApplications();
        phpAppsWrapper.setPhpapps(phpApps);
        phpAppsWrapper.setEndPoints(getEndPoints(phpApps));
        phpAppsWrapper.setNumberOfPages(1);
        return phpAppsWrapper;
    }

    private String[] getEndPoints(String[] phpApps) {
        String[] endPoints;
        if(phpApps != null){
            endPoints = new String[phpApps.length];
            int tenantId = MultitenantUtils.getTenantId(getConfigContext());
            String tenantIdentityForUrl = "";
            if(tenantId != -1234) {
               tenantIdentityForUrl = "/t/" + tenantId ;
            }
            for(int i = 0; i < endPoints.length; i++){
                if(isInstanceForTenantUp()){
                    String publicIp = Store.tenantToPublicIpMap.get(tenantId);
                    endPoints[i] = "https://" + publicIp + tenantIdentityForUrl + "/" + phpApps[i]
                                   + "/" + phpApps[i] + ".php";
                }    else{
                    endPoints[i] = "Endpoint could not be allocated. Please contact your administrator";
                }
            }
        }   else {
            endPoints = null;
        }
        return endPoints;
    }


    public void deleteAllPhpApps(){
        deleteApps(listPhpApplications());
    }

    public void deletePhpApps(String[] phpAppFileNames){
        deleteApps(phpAppFileNames);
    }

    private void deleteApps(String phpApps[]){
        File phpAppFile;
        for (String phpApp : phpApps) {
            phpAppFile = new File(getWebappDeploymentDirPath() +  File.separator + phpApp);
            phpAppFile.delete();
        }
        if(listPhpApplications() == null){
            Integer tenantId = MultitenantUtils.getTenantId(getConfigContext());
            try {
                client.terminateSpiInstance(Store.tenantToPublicIpMap.get(tenantId));
            } catch (Exception e) {
                log.error("Error while terminating instance");
            }
        }
    }

    public String startInstance(String image){
        //passed value will be ignored for this release
        String imageId = "";
        String imageIdsString = System.getProperty(PHPCartridgeConstants.OPENSTACK_INSTANCE_IMAGE_IDS);
        String[] imagesToNameArray = imageIdsString.split(",");
        if (imagesToNameArray != null) {
            String imageArray[] = imageIdsString.split(":");
            imageId = imageArray[1];
        }
        //Above code will get first one from images of configuration for this release
        Integer tenantId = MultitenantUtils.getTenantId(getConfigContext());
        log.info("An instance is spawning for tenant " + tenantId);
        try {
			if (client == null) {
				initAutoscaler();
			}
        } catch (Exception e) {
            String msg = "Error while initializing Autoscaler";
            log.error(msg, e);
        }
        String publicIp = "";
        openstackDAO = new OpenstackDAO();
        try{
            String modifiedDomainWithTenantId = System.getProperty("php.domain") + "/t/" + tenantId;
            String privateIp = client.startInstance( modifiedDomainWithTenantId , imageId);
            publicIp = openstackDAO.getPublicIp(privateIp);
            Store.publicIpToTenantMap.put(publicIp, tenantId);
            Store.tenantToPublicIpMap.put(tenantId, publicIp);
            log.info("Started Instance public ip is " + publicIp);
        }catch (Exception e){
            String msg = "Error while calling auto scaler to start instance";
            log.error(msg);
        }

        return publicIp;
    }

    public boolean isInstanceForTenantUp(){
        Integer tenantId = MultitenantUtils.getTenantId(getConfigContext());
        if(Store.publicIpToTenantMap.containsValue(tenantId)){
            return true;
        }
        return false;
    }

    /**
     * Not used for this release
     * @return
     */
//    public String[] getImages(){
//        String imageNames[];
//        String[] apps = listPhpApplications();
//        if(!isInstanceForTenantUp()){
//            String imageIdsString = System.getProperty(PHPCartridgeConstants.OPENSTACK_INSTANCE_IMAGE_IDS);
//            String[] imagesToNameArray = imageIdsString.split(",");
//            for (String image : imagesToNameArray) {
//                String imageArray[] = image.split(":");
//                imageIdtoNameMap.put(imageArray[0], imageArray[1]);
//            }
//            imageNames = imageIdtoNameMap.keySet().toArray(new String[imageIdtoNameMap.size()]);
//        } else {
//            imageNames = null;
//        }
//        return imageNames;
//    }

}
