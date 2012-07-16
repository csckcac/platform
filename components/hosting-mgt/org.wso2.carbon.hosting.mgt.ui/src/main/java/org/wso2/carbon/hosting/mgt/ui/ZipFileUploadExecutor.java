/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.hosting.mgt.ui;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import org.wso2.carbon.hosting.mgt.stub.types.carbon.FileUploadData;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The FileUploadExecutor which handles uploading of webapps
 */
public class ZipFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".zip"};

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "Web application uploading failed. No file specified.";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request, response,
                                                "../" + webContext + "/hosting-mgt/upload.jsp");
            return false;
        }

        HostingAdminClient client =
                new HostingAdminClient(request.getLocale(), cookie, configurationContext, serverURL);
        String msg;

        List<FileItemData> tempDataList = fileItemsMap.get("warFileName");
        List<FileUploadData> fileUploadDataList = new ArrayList<FileUploadData>();
        String selectedImage = "";
        String publicIp = null;

        if(!client.isInstanceUp()){
            List<String> images = getFormFieldValue("images");
            if(images.isEmpty() || images == null){
                log.error("Image is empty, Can not start instance" );
            } else{
                if(images.size() == 1){
                    selectedImage = images.get(0);
                    log.info(selectedImage);
                    publicIp = client.startInstance(selectedImage);
                }
            }
        }
        try {
            for (FileItemData filedata : tempDataList) {
                FileUploadData tempData = new FileUploadData();
                checkServiceFileExtensionValidity(getFileName(filedata.getFileItem().getName()), ALLOWED_FILE_EXTENSIONS);
                tempData.setFileName(getFileName(filedata.getFileItem().getName()));
                tempData.setDataHandler(filedata.getDataHandler());
                fileUploadDataList.add(tempData);
            }

            client.uploadWebapp(fileUploadDataList.toArray(new FileUploadData[fileUploadDataList.size()]));

            //response.setContentType("text/html; charset=utf-8");
            if(publicIp != null){
                msg = "PHP application has been uploaded successfully. Instance is spawned and " +
                      "public ip of the instance is: " + publicIp;
            }   else{
                msg = "PHP application has been uploaded successfully.";
            }


            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request, response,
                                                "../" + webContext + "/hosting-mgt/index.jsp");

            return true;
        } catch (Exception e) {
            msg = "Web application upload failed. " + e.getMessage();
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request, response,
                                                "../" + webContext + "/hosting-mgt/upload.jsp");
        }
        return false;
    }
}
