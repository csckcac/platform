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
import java.io.IOException;


import org.wso2.carbon.core.AbstractAdmin;

/**
 * @author wso2
 *
 */
public class ApplicationManagementService extends AbstractAdmin{

    private static final Log log = LogFactory.getLog(ApplicationManagementService.class);
    public static final String FILE_DEPLOYMENT_FOLDER = "phpapps";

    /**
     * Upload the applications that will be deployed in the container
     * @param tenantName
     * @param password
     * @param appName
     */

	public void  uploadApplication(String tenantName, String password, String appName) {
		
//		try {
//            //Container container =  hostingRes.retrieveContainer(tenantName);
//            // TODO call the agent to create the container
//		} catch (ResourcesException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

    /**
         * Upload a File
         *
         * @param fileUploadDataList Array of data representing the webapps that are to be uploaded
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
                handleException("Error occured while uploading the webapp " + fileName, e);
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
     * Retrieve and display the applications deployed in the container
     * @param username
     * @param containerRoot
     */
	public void listApplications(String username, String containerRoot) {
        // TODO list applications
	}

}
