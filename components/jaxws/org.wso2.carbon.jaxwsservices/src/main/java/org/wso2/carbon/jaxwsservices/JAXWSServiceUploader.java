/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jaxwsservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The class that handles the deployment deployment of JAX-WS
 * service jar(s) that were uploaded via the management console
 */

public class JAXWSServiceUploader extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(JAXWSServiceUploader.class);

    public void uploadService(JAXServiceData[] serviceDataList) throws AxisFault {
        try {

            AxisConfiguration axisConfig = getAxisConfig();
            String repo = axisConfig.getRepository().getPath();

            StringBuilder destDir;

            for(JAXServiceData serviceData : serviceDataList) {
                String serviceHierarchy = serviceData.getServiceHierarchy();
                destDir = new StringBuilder();
                //Writting the artifacts to the proper location
                destDir.append(repo + File.separator + "servicejars");

                // create the hierarchical folders before deploying
                if(serviceHierarchy != null) {
                    String [] hierarchyParts = serviceHierarchy.split("/");
                    for(String part : hierarchyParts) {
                        destDir.append(File.separator + part);
                        File hierarchyFolder = new File(destDir.toString());
                        if (!hierarchyFolder.exists()) {
                            boolean status = hierarchyFolder.mkdir();
                            if (!status) {
                                throw new AxisFault("Error while creating directory structure");        
                            }
                        }
                    }
                }

                if (serviceData.getFileName() == null || serviceData.getFileName().equals("")) {
                    throw new AxisFault("Invalid file name");
                } else if (serviceData.getFileName().endsWith(".aar")) {
                    throw new AxisFault("File with extension " + serviceData.getFileName() + " is not supported!");
                } else {
                    writeResource(serviceData.getDataHandler(), destDir.toString(), serviceData.getFileName());
                }

            }
      
        } catch (IOException e) {
            throw new AxisFault("Error occurred while uploading service artifacts", e);
        }
    }

    private void writeResource(DataHandler dataHandler,
                               String destPath,
                               String fileName) throws IOException {
        File destFile = new File(destPath, fileName);
        FileOutputStream fos = null;

        try{
            fos = new FileOutputStream(destFile);
            dataHandler.writeTo(fos);
            fos.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Failed to close FileOutputStream for file " + fileName, e);
                }
            }
        }
    }

}
