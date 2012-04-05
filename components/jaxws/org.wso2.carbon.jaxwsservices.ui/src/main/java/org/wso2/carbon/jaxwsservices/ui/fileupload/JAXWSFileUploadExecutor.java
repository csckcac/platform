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
package org.wso2.carbon.jaxwsservices.ui.fileupload;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.jaxwsservices.stub.types.JAXServiceData;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The FileUploadExecutor which handles uploading of annotated jar(s) for JAX-WS service
 */

public class JAXWSFileUploadExecutor extends AbstractFileUploadExecutor {

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {
        return uploadArtifacts(request, response, "servicejars", new String[]{"jar"}, "Axis2");
    }

    protected boolean uploadArtifacts(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String uploadDirName,
                                      String[] extensions,
                                      String utilityString)
            throws IOException {

        response.setContentType("text/html; charset=utf-8");

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String msg;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/jaxws/index.jsp");
        }

        //Creating the stub to call the back-end service
        JAXWSServiceUploaderClient uploaderClient = new JAXWSServiceUploaderClient(
                configurationContext, serverURL + "JAXWSServiceUploader", cookie);
        
        // Retrieve the set of Service Hierarchies
        List<String> serviceHierarchyList = null;
        Map<String, ArrayList<java.lang.String>> formFieldsMap = getFormFieldsMap();
        if (formFieldsMap != null && formFieldsMap.size() > 0) {
            serviceHierarchyList = formFieldsMap.get("serviceHierarchy");
        }

        // Retrieve the set of FileItemData
        List<FileItemData> fileItemDataList = null;
        if (fileItemsMap != null && fileItemsMap.size() > 0) {
            fileItemDataList = fileItemsMap.get("jarfilename");
        }

        // The list that is used to hold all the artifacts before uploading 
        List<JAXServiceData> serviceDataList = new ArrayList<JAXServiceData> ();
        try {
            if (serviceHierarchyList != null) {
                for (int i = 0; i < serviceHierarchyList.size(); i++) {
                    JAXServiceData tempServiceData = new JAXServiceData();
                    tempServiceData.setFileName(getFileName(fileItemDataList.get(i).getFileItem().getName()));
                    tempServiceData.setDataHandler(fileItemDataList.get(i).getDataHandler());
                    tempServiceData.setServiceHierarchy(serviceHierarchyList.get(i));
                    serviceDataList.add(tempServiceData);
                }
            }            

            //Uploading files to back end service
            uploaderClient.uploadFileItems(serviceDataList.toArray(new JAXServiceData [serviceDataList.size()]));

            msg = "Files have been uploaded "
                    + "successfully. Please refresh this page in a while to see "
                    + "the status of the created JAXWS service";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, getContextRoot(request) + "/" + webContext + "/service-mgt/index.jsp" );
            return true;
        } catch (Exception e) {
            msg = "File upload failed.";
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/jaxws/index.jsp");
        }
        return false;

    }
}
