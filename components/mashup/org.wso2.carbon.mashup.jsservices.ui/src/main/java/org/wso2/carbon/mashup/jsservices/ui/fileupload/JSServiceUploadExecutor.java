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
package org.wso2.carbon.mashup.jsservices.ui.fileupload;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.jsservices.ui.MashupServiceAdminClient;
import org.wso2.carbon.mashup.jsservices.stub.client.types.JSServiceUploadData;
import org.wso2.carbon.ui.CarbonSecuredHttpContext;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JSServiceUploadExecutor extends AbstractFileUploadExecutor {
    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".zip"};

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String username =
                (String) request.getSession().getAttribute(CarbonSecuredHttpContext.LOGGED_USER);

        List<FileItemData> tempDataList = null;
        String message = null;

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            message = "File uploading failed. No files are specified";
            log.error(message);
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, response,
                                                getContextRoot(request) + "/" + webContext +
                                                "/js_service/upload.jsp");
            return false;
        } else {
            tempDataList = fileItemsMap.get("jsFilename");
        }

        JSServiceUploadClient serviceUploaderClient =
                new JSServiceUploadClient(configurationContext, serverURL, cookie);
        MashupServiceAdminClient client =
                new MashupServiceAdminClient(cookie, serverURL, configurationContext);
        List<JSServiceUploadData> jsServiceDataList = new ArrayList<JSServiceUploadData>();
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        String[] serviceNames = new String[tempDataList.size()];
        ArrayList<File> tempFiles = new ArrayList<File>();
        try {
            for (int i = 0; i < tempDataList.size(); i++) {
                FileItemData fileData = tempDataList.get(i);
                String fileName = getFileName(fileData.getFileItem().getName());
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);
                JSServiceUploadData tempData = new JSServiceUploadData();
                tempData.setFileName(fileName);
                tempData.setDataHandler(fileData.getDataHandler());

                if (fileName.endsWith(".zip")) {
                    //validating the content of the zip file.
                    File zipFile = ((DeferredFileOutputStream) fileData.getFileItem().
                            getOutputStream()).getFile();
                    fileData.getFileItem().write(zipFile);
                    tempFiles.add(zipFile);
                    String[] files = archiveManipulator.check(zipFile.getAbsolutePath());
                    boolean jsFile = false;
                    boolean resourceFolder = false;
                    //validates the structure of zip file. i.e. zip file shoud contains only one
                    //.js file in the root folder and no other file types, but directories.
                    for (String file : files) {
                        if (file.indexOf("/") == -1) {
                            String extension = file.substring(file.indexOf(".") + 1);
                            String jsName = file.substring(0, (file.indexOf(".")));
                            if ("js".equals(extension)) {
                                if (jsFile) {
                                    message = getFileName(fileData.getFileItem().getName()) +
                                              " file should contain only " +
                                              "one '.js' file in the root folder";
                                    log.error(message);
                                    CarbonUIMessage
                                            .sendCarbonUIMessage(message, CarbonUIMessage.ERROR,
                                                                 request, response,
                                                                 getContextRoot(request) + "/" +
                                                                 webContext +"/js_service/upload.jsp");
                                    return false;
                                }
                                serviceNames[i] = username + "/" + jsName;
                                jsFile = true;
                            } else if ("resources".equals(extension)) {
                                if (resourceFolder) {
                                    message = getFileName(fileData.getFileItem().getName()) +
                                              " file contains an invalid file in the root folder : " +
                                              file;
                                    log.error(message);
                                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR,
                                                                        request, response,
                                                                        getContextRoot(request) +
                                                                        "/" + webContext +
                                                                        "/js_service/upload.jsp");
                                    return false;
                                }
                                resourceFolder = true;
                            } else {
                                message = getFileName(fileData.getFileItem().getName()) +
                                          " file contains an invalid file in the root folder : " +
                                          file;
                                log.error(message);
                                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR,
                                                                    request, response,
                                                                    getContextRoot(request) + "/" +
                                                                    webContext +"/js_service/upload.jsp");
                                return false;
                            }
                        }
                    }
                    if (!jsFile) {
                        message =
                                getFileName(fileData.getFileItem().getName()) +
                                " file doesn't contain a '.js'" +" file in the root folder";

                        log.error(message);
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request,
                                                            response, getContextRoot(request) + "/"
                                                                      + webContext +
                                                                      "/js_service/upload.jsp");
                        return false;
                    }

                    jsServiceDataList.add(tempData);
                } else {
                    message = "File with extension " + fileName + " is not supported!";
                    CarbonUIMessage
                            .sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, response,
                                                 getContextRoot(request) + "/" + webContext +
                                                 "/js_service/upload.jsp");
                    log.error(message);
                    return false;
                }
            }

            String[] existingServices = client.doesServicesExists(serviceNames);
            String zipCode = null;

            if (existingServices != null && existingServices.length != 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < existingServices.length; i++) {
                    String existingService =
                            existingServices[i].substring(existingServices[i].indexOf("/") + 1);

                    for (int j = 0; j < jsServiceDataList.size(); j++) {
                        String fileName = jsServiceDataList.get(j).getFileName();

                        if (fileName.replaceAll(zipCode, "").equals(existingService)) {
                            jsServiceDataList.remove(j);
                        }
                    }
                    if (i == 0) {
                        stringBuilder.append(existingService);
                    } else {
                        stringBuilder.append(", " + existingService);
                    }
                }
                if (jsServiceDataList.size() > 0) {
                    StringBuilder serviceList = new StringBuilder();

                    for (int j = 0; j < jsServiceDataList.size(); j++) {
                        if (j == 0) {
                            serviceList.append(jsServiceDataList.get(j).getFileName()
                                                       .replaceAll(zipCode, ""));
                        } else {
                            serviceList.append("," + jsServiceDataList.get(j).getFileName()
                                    .replaceAll(zipCode, ""));
                        }
                    }

                    JSServiceUploadData[] services =
                            jsServiceDataList.toArray(new JSServiceUploadData
                                                              [jsServiceDataList.size()]);
                    serviceUploaderClient.uploadService(username, services);

                    message = "Javascript services with the names-" + serviceList.toString() +
                              " uploaded successfully,except services with the names- " +
                              stringBuilder.toString() + " since they are already exist.";

                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request,
                                                        response, getContextRoot(request) + "/" +
                                                                  webContext + "/service-mgt/index.jsp");
                    log.error(message);
                    return false;
                } else {
                    message = "Services with the names " + stringBuilder.toString() +
                              " already exist";
                    CarbonUIMessage
                            .sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, response,
                                                 getContextRoot(request) + "/" +
                                                 webContext + "/js_service/upload.jsp");
                    log.error(message);
                    return false;
                }
            }

            JSServiceUploadData[] services =
                    jsServiceDataList.toArray(new JSServiceUploadData[jsServiceDataList.size()]);
            serviceUploaderClient.uploadService(username, services);

            message = "Javascript Services uploaded successfully.";
            log.info(message);
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request, response,
                                                getContextRoot(request) + "/" + webContext +
                                                "/service-mgt/index.jsp");
            return true;
        } catch (java.lang.Exception e) {
            message = "File upload failed. " + e.getMessage();
            log.error(message, e);
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, response,
                                                getContextRoot(request) + "/" + webContext +
                                                "/js_service/upload.jsp");
        } finally {
            //deleting temp files
            for (File file : tempFiles) {
                if (!file.delete()) {
                    log.warn("Error deleting temp file " + file.getName());
                }
            }
        }
        return false;
    }

}
