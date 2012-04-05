/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mashup.jsservices.admin.fileupload;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mashup.jsservices.admin.utils.MashupArchiveManupulator;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;

public class JSServiceUploader extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(JSServiceUploader.class);

    public String uploadService(String username, JSServiceUploadData[] jsServiceDataList) throws Exception {
        String fileName = null;
        try {
            // First lets filter for jar resources
            AxisConfiguration axisConfig = getAxisConfig();
            String repo = axisConfig.getRepository().getPath();

            if (CarbonUtils.isURL(repo)) {
                throw new AxisFault("Uploading services to URL repo is not supported ");
            }

            // Remove '@' charactor from the username, if it contains
            String userdir = MashupUtils.filterUsername(username);
            // Composing the proper location to copy the artifact
            MashupArchiveManupulator archiveManupulator = new MashupArchiveManupulator();

            for (JSServiceUploadData tempData : jsServiceDataList) {
                fileName = tempData.getFileName();
                String shortFileName = fileName.substring(0, fileName.lastIndexOf('.'));
                archiveManupulator.deploySharedService(userdir, shortFileName, tempData.getDataHandler(), axisConfig);
            }

        } catch (IOException e) {
            String msg = "Error occured while uploading the Javascript Service "
                         + fileName;
            log.error(msg, e);
            throw new Exception("Failed to upload the Javascript Service "
                                + fileName, e);
        }
        return "successful";
    }
}
