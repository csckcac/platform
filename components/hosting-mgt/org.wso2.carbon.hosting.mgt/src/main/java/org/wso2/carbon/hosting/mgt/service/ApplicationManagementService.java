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
import java.util.HashMap;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hosting.mgt.clients.AutoscaleServiceClient;
import org.wso2.carbon.hosting.mgt.utils.PHPAppsWrapper;


/**
 * @author wso2
 *
 */
public class ApplicationManagementService extends AbstractAdmin{

    private static final Log log = LogFactory.getLog(ApplicationManagementService.class);
    public static final String FILE_DEPLOYMENT_FOLDER = "phpapps";
    public static final String PHP_APP_DOMAIN = "phpdomain";
    AutoscaleServiceClient client;
    HashMap<String, String> imageIdtoNameMap;

    public ApplicationManagementService() throws AxisFault {
        client = new AutoscaleServiceClient("https://123.231.125.177:9444/services/AutoscalerService/");
        try {
            client.init(true);
        } catch (Exception e) {
            String msg = "Error while initiating autoscaler stub";
            log.error(msg);
        }
        imageIdtoNameMap = new HashMap<String, String>();
    }
    /**
         * Upload a File
         *
         * @param fileUploadDataList Array of data representing the PHP apps(.zip) that are to be uploaded
         * @return true - if upload was successful
         * @throws org.apache.axis2.AxisFault If an error occurs while uploading
         */
    public boolean uploadWebapp(FileUploadData[] fileUploadDataList
    ) throws AxisFault {
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
        phpAppsWrapper.setNumberOfPages(1);
        return phpAppsWrapper;
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

    public void startInstance(String image)  {
        try{
        client.startInstance(PHP_APP_DOMAIN, image);
        }catch (Exception e){
            String msg = "Error while calling auto scaler to start instance";
            log.error(msg);
        }
    }

    private void addPHPClusterDomain(){


    }

    private boolean isInstanceUp(String tenant){
        return true;
    }

    public String[] getImages(){
        String images[] = new String[1];
        images[0] = "nova/d1e1c317-5ed2-45ed-aa73-fec6d05b7fdc";
//        images[0] = "us-east-1/ami-52409a3b";
        return images;
    }

}
