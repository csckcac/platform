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



package org.wso2.carbon.issue.tracker.ui.fileupload;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.issue.tracker.stub.AccountInfo;
import org.wso2.carbon.issue.tracker.stub.AttachmentData;
import org.wso2.carbon.issue.tracker.stub.GenericCredentials;

import org.wso2.carbon.issue.tracker.ui.IssueTrackerClient;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateAttachmentsExecutor extends AbstractFileUploadExecutor {
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        Map<String, ArrayList<java.lang.String>> formFieldsMap = getFormFieldsMap();

        HttpSession httpSession = request.getSession();


        IssueTrackerClient issueTrackerClient = new IssueTrackerClient(cookie, serverURL, configurationContext);
        AttachmentData[] attachmentDataArray = new AttachmentData[0];

        // Retrieve the set of FileItems
        List<FileItemData> fileItems = fileItemsMap.get("attachmentName");


        List<AttachmentData> attachmentDataList = new ArrayList<AttachmentData>();

        if (null != fileItems) {
            for (FileItemData fileItemData : fileItems) {

                String filename = getFileName(fileItemData.getFileItem().getName());

//                try {
//                    checkServiceFileExtensionValidity(filename, ALLOWED_FILE_EXTENSIONS);
                AttachmentData attachmentData = new AttachmentData();
                attachmentData.setFileName(filename);
                attachmentData.setDataHandler(fileItemData.getDataHandler());
                attachmentDataList.add(attachmentData);
//                } catch (FileUploadException e) {
//                    log.error("File upload failed. " + e.getMessage());
//                    CarbonUIMessage.sendCarbonUIMessage("File upload failed. " + e.getMessage(), CarbonUIMessage.ERROR, request,
//                            response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");
//                    return true;
//                }


            }

            attachmentDataArray = attachmentDataList.toArray(new AttachmentData[attachmentDataList.size()]);
        }

        //get form fields
        List<String> account = formFieldsMap.get("accountName");
        List<String> issueKeys = formFieldsMap.get("issueKey");

        // get credentials from the registry

        AccountInfo accountInfo;
        if(issueTrackerClient.isStratosService()){

             accountInfo = issueTrackerClient.getAccount() ;

         }else{
             accountInfo = issueTrackerClient.getAccount(account.get(0));
        }
             GenericCredentials credentials = accountInfo.getCredentials();
        String token = null;

        //login
        try {
            token = issueTrackerClient.login(credentials);
        } catch (RemoteException e) {
            String msg = "Error loging to JIRA from account " + account.get(0);
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");
            return false;
        }

        String issueKey = null;
        ArrayList <String>  unUploadedFiles=new ArrayList<String>();
        ArrayList <String>  uploadedFiles=new ArrayList<String>();
        if (null != issueKeys && !"".equals(issueKeys.get(0))) {
            issueKey = issueKeys.get(0);

            if (attachmentDataArray.length > 0) {
                AttachmentData tempArray[]=new AttachmentData[1];
                unUploadedFiles.clear();


                for(AttachmentData data :attachmentDataArray)   {
                    tempArray[0]=data;
                    if (null != issueKey && !"".equals(issueKey)) {

                        try {
                            issueTrackerClient.uploadAttachments(token, issueKey, tempArray, credentials.getUrl());
                            uploadedFiles.add(data.getFileName());


                        } catch (Exception e) {
                            unUploadedFiles.add(data.getFileName());
                            log.error(e.getMessage() + " Uploading file  "+data.getFileName() +"failed");


                            //return false;

                        }
                    }
                }
            }
            if(!unUploadedFiles.isEmpty()) {
                CarbonUIMessage.sendCarbonUIMessage( "Following files are attached sucessfully "+uploadedFiles.toString()+". Re attach following files "+unUploadedFiles.toString()
                        , CarbonUIMessage.ERROR, request,
                        response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");
                return false;
            }


        }


        response.setContentType("text/html; charset=utf-8");

        String returnUrl = getContextRoot(request) + "/" + webContext + "/issue/viewSupportIssues.jsp?viewAccount=" + account.get(0) + "&issueKey=" + issueKey;

        response.sendRedirect(returnUrl);

        httpSession.removeAttribute("selectedAccount");

        httpSession.removeAttribute("issue");

        return true;
    }
}
