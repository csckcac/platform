/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.deployer.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.UploadedFileItem;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.*;
import java.nio.channels.FileChannel;

/**
 * HumanTask package uploader admin service service
 */
public class HumanTaskUploader extends AbstractAdmin {

    final private static Log log = LogFactory.getLog(HumanTaskUploader.class);

    public void uploadHumanTask(UploadedFileItem[] fileItems) throws AxisFault {

        //First lets filter for jar resources
        ConfigurationContext configurationContext = getConfigContext();
        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();


        if (CarbonUtils.isURL(repo)) {
            throw new AxisFault("URL Repositories are not supported: " + repo);
        }

        //Writting the artifacts to the proper location
        String humantaskDirectory = getHumanTaskLocation(repo);

        String humantaskTemp = CarbonUtils.getCarbonHome() + File.separator +
                               HumanTaskConstants.HUMANTASK_PACKAGE_TEMP_DIRECTORY;
        File humantaskTempDir = new File(humantaskTemp);
        if (!humantaskTempDir.exists() && !humantaskTempDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + humantaskTempDir.getAbsolutePath());
        }

        File humantaskDir = new File(humantaskDirectory);
        if (!humantaskDir.exists() && !humantaskDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + humantaskDir.getAbsolutePath());
        }

        for (UploadedFileItem uploadedFile : fileItems) {
            String fileName = uploadedFile.getFileName();

            if (fileName == null || fileName.equals("")) {
                throw new AxisFault("Invalid file name. File name is not available");
            }

            if (uploadedFile.getFileType().equals(HumanTaskConstants.HUMANTASK_PACKAGE_EXTENSION)) {
                try {
                    writeResource(uploadedFile.getDataHandler(), humantaskTemp, fileName, humantaskDir);
                } catch (IOException e) {
                    throw new AxisFault("IOError: Writing resource failed.");
                }
            } else {
                throw new AxisFault("Invalid file type : " + uploadedFile.getFileType() + " ." +
                                    HumanTaskConstants.HUMANTASK_PACKAGE_EXTENSION +
                                    " file type is expected");
            }
        }

    }

    //TODO common function see BpelUploader
    private void writeResource(DataHandler dataHandler, String destPath, String fileName,
                               File bpelDest) throws IOException {
        File destFile = new File(destPath, fileName);
        FileOutputStream fos = null;
        FileChannel out = null;
        FileChannel in = null;
        try {
            fos = new FileOutputStream(destFile);
            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            dataHandler.writeTo(fos);
            out = new FileOutputStream(bpelDest + File.separator + fileName).getChannel();
            in = new FileInputStream(destFile).getChannel();
            out.write(in.map(FileChannel.MapMode.READ_ONLY, 0, in.size()));
        } catch (FileNotFoundException e) {
            log.error("Cannot find the file", e);
            throw e;
        } catch (IOException e) {
            log.error("IO error.");
            throw e;
        } finally {
            close(in);
            close(out);
            close(fos);
        }

        boolean isDeleted = destFile.delete();
        if (!isDeleted) {
            log.warn("temp file: " + destFile.getAbsolutePath() +
                     " deletion failed, scheduled deletion on server exit.");
            destFile.deleteOnExit();
        }
    }

    public static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            log.warn("Can't close file streams.", e);
        }
    }

    // Get the human task repository location.
    private String getHumanTaskLocation(String repoLocation) {
        if (repoLocation.endsWith(File.separator)){
            return  repoLocation +    HumanTaskConstants.HUMANTASK_REPO_DIRECTORY;
        }   else {
            return repoLocation + File.separator + HumanTaskConstants.HUMANTASK_REPO_DIRECTORY;
        }
    }
}
