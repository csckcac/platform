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
import java.rmi.RemoteException;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hosting.mgt.utils.PHPappsWrapper;

/**
 * @author wso2
 *
 */
public class ApplicationManagementService extends AbstractAdmin{

    private static final Log log = LogFactory.getLog(ApplicationManagementService.class);
    public static final String FILE_DEPLOYMENT_FOLDER = "phpapps";
    public static final String PHP_APP_DOMAIN = "phpdomain";
    /**
     * Upload the applications that will be deployed in the container
     * @param tenantName
     * @param password
     * @param appName
     */

    /**
         * Upload a File
         *
         * @param fileUploadDataList Array of data representing the PHP apps(.zip) that are to be uploaded
         * @return true - if upload was successful
         * @throws org.apache.axis2.AxisFault If an error occurrs while uploading
         */
    public boolean uploadWebapp(FileUploadData[] fileUploadDataList) throws AxisFault {
        File webappsDir = new File(getWebappDeploymentDirPath());
        if (!webappsDir.exists() && !webappsDir.mkdirs()) {
            log.warn("Could not create directory " + webappsDir.getAbsolutePath());
        }

        for (FileUploadData uploadData : fileUploadDataList) {
            String fileName = uploadData.getFileName();
            File destFile = new File(webappsDir, fileName);
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

    public PHPappsWrapper getPagedPhpAppsSummary(String phpAppSearchString, int pageNumber){
         PHPappsWrapper phpAppsWrapper = new PHPappsWrapper();
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
}
