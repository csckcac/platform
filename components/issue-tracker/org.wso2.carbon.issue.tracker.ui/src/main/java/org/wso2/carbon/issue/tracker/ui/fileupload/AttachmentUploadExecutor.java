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
import org.wso2.carbon.issue.tracker.stub.GenericIssue;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * this class is responsible for obtaining form field data and attached files in order to create issues
 * exceute() method will be called from ../issue/newIssue.jsp
 */
public class AttachmentUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".txt", ".info", ".xml", ".doc", ".png", ".jpg", ".jpeg", ".gif"};

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
        List<String> account = formFieldsMap.get("accountNames");
        List<String> project = formFieldsMap.get("projectList");
        List<String> summary = formFieldsMap.get("summary");
        List<String> priority = formFieldsMap.get("priority");
        List<String> description = formFieldsMap.get("description");
        List<String> type = formFieldsMap.get("type");
        List<String> dueDate = formFieldsMap.get("due");
        List<String> issueKeys = formFieldsMap.get("issueKey");
        List<String> bundleInfoFile = formFieldsMap.get("bundleInfo");
        List<String> threadDump = formFieldsMap.get("threadDump");
        List<String> logFile = formFieldsMap.get("log");
        ArrayList <String>  unUploadedFiles=new ArrayList<String>();
        ArrayList <String>  uploadedFiles=new ArrayList<String>();
        // get credentials from the registry


        AccountInfo accountInfo;
        if (issueTrackerClient.isStratosService()) {

            accountInfo = issueTrackerClient.getAccount();

        } else {
            accountInfo = issueTrackerClient.getAccount(account.get(0));

        }

        GenericCredentials credentials;

        credentials = accountInfo.getCredentials();


        String token = null;

        //login
        try {
            token = issueTrackerClient.login(credentials);
        } catch (RemoteException e) {
            String msg = "Error loging to JIRA from account " + accountInfo.getKey();
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");
            return false;
        }

        String issueKey = null;

        //if issue key is available,attach files to the same issue. else create a new issue and attach files.

        // report issue
        GenericIssue issue = new GenericIssue();

        if (null != project && project.size() > 0) {
            issue.setProjectKey(project.get(0));
        }
        if (!"--Select--".equals(type.get(0))) {
            issue.setType(type.get(0));

        }

        issue.setSummary(summary.get(0).trim());


        if (null != priority && priority.size() > 0) {
            if (!"--Select--".equals(priority.get(0))) {
                issue.setPriority(priority.get(0));
            }
        }

        issue.setDescription(description.get(0).trim());

        // setting due date

        if (null != dueDate) {
            String due = dueDate.get(0);

            if (null != due && !("".equals(due))) {
                DateFormat formatter;
                Date date;
                formatter = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    date = (Date) formatter.parse(due);
                } catch (ParseException e) {
                    String msg = "Incorrect due date " + due;
                    log.error(msg);

                    CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                            response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");
                    return false;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                issue.setDueDate(cal);

            }
        }

        try {
            issueKey = issueTrackerClient.createIssue(issue, token, credentials.getUrl());
        } catch (Exception e) {
            log.error(e.getMessage() + "Error creating the issue.Please refer backend logs for more details.");
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage()
                    , CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/issue/newIssue.jsp");

            return false;

        }
        issue.setIssueKey(issueKey);


        httpSession.setAttribute("selectedAccount", accountInfo.getKey());

        httpSession.setAttribute("issue", issue);

        String msg = "Issue " + issueKey + " is successfully created";


        // attach bundle.info file

        if (null != bundleInfoFile && "on".equals(bundleInfoFile.get(0))) {

            issueTrackerClient.uploadBundleInfo(token, issueKey, credentials.getUrl());

        }


        //attach thread dump

        if (null != threadDump && "on".equals(threadDump.get(0))) {
            issueTrackerClient.uploadThreadDump(token, issueKey, credentials.getUrl());
        }

        //attach log file

        if (null != logFile && "on".equals(logFile.get(0))) {
            issueTrackerClient.uploadLogFile(token, issueKey, credentials.getUrl());
        }


        // attach files

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
                    response, getContextRoot(request) + "/" + webContext + "/issue/updateIssue.jsp");
            return false;
        }
        response.setContentType("text/html; charset=utf-8");

        String returnUrl = getContextRoot(request) + "/" + webContext + "/issue/viewSupportIssues.jsp?viewAccount=" + accountInfo.getKey() + "&issueKey=" + issueKey;

        response.sendRedirect(returnUrl);

        httpSession.removeAttribute("selectedAccount");

        httpSession.removeAttribute("issue");

        return true;

    }
}
