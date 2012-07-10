/**
 * 
 */
package org.wso2.carbon.hosting.mgt.service;


import org.apache.axis2.AxisFault;
import org.wso2.carbon.hosting.mgt.utils.FileUploadData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hosting.mgt.clients.AutoscaleServiceClient;
import org.wso2.carbon.hosting.mgt.utils.PHPAppsWrapper;
import org.wso2.carbon.hosting.mgt.utils.PHPCartridgeConstants;


/**
 * @author wso2
 *
 */
public class ApplicationManagementService extends AbstractAdmin{

    private static final Log log = LogFactory.getLog(ApplicationManagementService.class);
    public static final String FILE_DEPLOYMENT_FOLDER = "phpapps";
    AutoscaleServiceClient client;
    HashMap<String, String> imageIdtoNameMap;

    public ApplicationManagementService() throws Exception {
        client = new AutoscaleServiceClient(System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
        imageIdtoNameMap = new HashMap<String, String>();
        log.info("Initialized Autoscaler service");
        client.init(true);
        log.info("Called INIT of Autoscaler service and Image id is");
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

            log.info("Uploaded files" );
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

        log.info("Listing php apps");
        String phpAppPath = getWebappDeploymentDirPath();
        File phpAppDirectory = new File(phpAppPath);
        String[] children;

        // This return any files that ends with '.zip'.
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        };
        children = phpAppDirectory.list(filter);

        return children;

    }

    public PHPAppsWrapper getPagedPhpAppsSummary(String phpAppSearchString, int pageNumber){
         PHPAppsWrapper phpAppsWrapper = new PHPAppsWrapper();
        phpAppsWrapper.setPhpapps(listPhpApplications());
        String[] phpApps= listPhpApplications();
        phpAppsWrapper.setPhpapps(phpApps);
        phpAppsWrapper.setEndPoints(getEndPoints(phpApps));
        phpAppsWrapper.setNumberOfPages(1);
        return phpAppsWrapper;
    }

    private String[] getEndPoints(String[] phpApps) {
        String[] endPoints = new String[phpApps.length];
        for(int i = 0; i < endPoints.length; i++){
            endPoints[i] = "https://" + "<tenant_ip>" + "/t/" + "tenant_name/" + phpApps[i];
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
            phpAppFile = new File(getWebappDeploymentDirPath() + File.separator + phpApp);
            phpAppFile.delete();
        }
    }

    public void startInstance(String image, String tenant){
//                                public String startInstance(String image, String tenant)  {
//        String ip = null;
        try{
            String ip = client.startInstance(System.getProperty("php.domain"), imageIdtoNameMap.get(image));
            log.info("Started Instance ip is " + ip);
        }catch (Exception e){
            String msg = "Error while calling auto scaler to start instance";
            log.error(msg);
        }
//        return ip;
    }

    private void addPHPClusterDomain(){


    }

    private boolean isInstanceUp(String tenant){
        return true;
    }

    public String[] getImages(){
        String imageIdsString = System.getProperty(PHPCartridgeConstants.OPENSTACK_INSTANCE_IMAGE_IDS);
        String[] images = imageIdsString.split(",");
        for (String image : images) {
            String imageArray[] = image.split(":");
            imageIdtoNameMap.put(imageArray[0], imageArray[1]);
        }
        return imageIdtoNameMap.keySet().toArray(new String[imageIdtoNameMap.size()]);
    }

}
