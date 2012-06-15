/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.rnd.nosql;

import me.prettyprint.hector.api.exceptions.HectorPoolException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.wso2.rnd.nosql.model.Blob;
import org.wso2.rnd.nosql.model.Record;
import org.wso2.rnd.nosql.model.User;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * EMR UI helper methods.
 */
public class UIHelper {

    public static final String USER = "username";
    public static final String ERROR_MSG = "ErrMessage";
    public static final String DATE_FORMT = "EEE MMM dd HH:mm:ss zzz yyyy";


    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(USER);
    }

    /**
     * Get and clear error message
     * @param session
     * @return
     */
    public static String getAndClearErrorMsg(HttpSession session) {
        String errMsg = (String) session.getAttribute(ERROR_MSG);
        session.removeAttribute(ERROR_MSG);
        return errMsg;
    }

    /**
     * Process signing call method
     * @param session
     * @param username
     * @param config
     */
    public static void processSignIn(HttpSession session,String username,
                                     ServletConfig config) {
    	String userId = username;
        boolean isUserNameEmpty = false;
        if (userId == null) {
            userId = "";
        }
        userId = userId.trim();
        if ("".equals(userId)) {
            isUserNameEmpty = true;
        }
       /* String userPassword = (String)session.getAttribute("j_password");
        boolean isPasswordEmpty = false;
        if (userPassword == null) {
            userPassword = "";
        }
        userPassword = userPassword.trim();
        if ("".equals(userPassword)) {
            isPasswordEmpty = true;
        }
        if (!isPasswordEmpty && !isUserNameEmpty) {
            ///test

*/
//            //Auth with Carbon user base
//            String backendServerURL = config.getServletContext().getInitParameter("emrServerURL");
//            ConfigurationContext configCtx =
//                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
 /*           boolean status = false;
//            AuthenticationAdminClient authAdmin = null;
//            //System.out.println(configCtx.toString());
//            try {
//                authAdmin =
//                        new AuthenticationAdminClient(configCtx, backendServerURL, null, session, true);
//            } catch (Exception e) {
//                System.out.println("Can not get configCtx");
//            }
//            try {
//                status = authAdmin.login(userId, userPassword, request.getRemoteAddr());
//                //tobe removed. true for all users========================
                status = true;
//            } catch (Exception e) {
//                System.out.println("Carbon auth error");
//            }
*/
    		boolean status=true;
            EMRClient emrClient = null;
            User user = null;
            if (status) {
                try {
                    emrClient = EMRClient.getInstance();
                    user = emrClient.getCurrentUserInformation(userId);
                    if (user == null) {
                        user = new User();
                        user.setUserID(userId);
                        user.setFullName("<Full Name>");
                        user.setEmail("<Email>");
                        user.setDateOfBirth("<DOB>");
                        user.setGender("<Gender>");
                        user.setBloodGroup("<Blood Group>");
                        user.setEthnicity("<Ethnicity>");
                        emrClient.saveUser(user);
                        session.setAttribute(USER, user);
                    }

                } catch (HectorPoolException e) {
                    session.setAttribute(ERROR_MSG, "Cassandra Connection Error");
                    System.out.println(e.toString());
                } finally {

                }
                session.setAttribute(USER, user);
            } else {
                session.setAttribute(ERROR_MSG, "Username or Password is invalid!");
            }
            
//            if (user == null) {
//                session.setAttribute(ERROR_MSG, "Username or Password is invalid!");
//            } else {
//                session.setAttribute(USER, user);
//            }
//        }*/
        if (isUserNameEmpty) {
            session.setAttribute(ERROR_MSG, "Username is empty!");
        }
    }

    /**
     * Save user profile
     * @param request
     * @param session
     */
    public static void saveUser(HttpServletRequest request, HttpSession session) {

        User user = getUser(session);
        if (user == null) {
            user = new User();
        }
        String userId = request.getParameter("userId");
        if (userId != null) {
            user.setUserID(userId);
        }
        String password = request.getParameter("password");
        String repassword = request.getParameter("repassword");
        if (password != null) {
            user.setPassword(password);
        }
        String username = request.getParameter("fullname");
        user.setFullName(username);
        String dob = request.getParameter("dob");
        user.setDateOfBirth(dob);
        String email = request.getParameter("contactDetails");
        user.setEmail(email);
        String gender = request.getParameter("gender");
        user.setGender(gender);
        String bloodgroup = request.getParameter("bloodType");
        user.setBloodGroup(bloodgroup);
        session.setAttribute(USER, user);
        EMRClient.getInstance().saveUser(user);
    }

    /**
     * Save record
     * @param request
     * @param session
     */
    public static void saveRecord(HttpServletRequest request, HttpSession session) {

        String userId = request.getParameter("userId");
        String recordId = request.getParameter("recordId");
        String recordType = request.getParameter("recordType");
        String recordTypeData = request.getParameter("recordTypeData");
        String recordData = request.getParameter("recordData");
        String userComment = request.getParameter("userComment");
        String sickness=request.getParameter("sicknessInfo");

        if("".equals(recordId) || recordId == null)
        {
            recordId = UUID.randomUUID().toString();
        }
        if("".equals(recordType) || recordId == null)
        {
            recordType = "General";
        }
        if("".equals(recordTypeData) || recordId == null){
            recordTypeData = "Fine";
        }
        if(recordData==null)
        {
        	recordData="Not available";
        }
        if(userComment==null)
        {
        	userComment="Not available";
        }
        if(sickness==null)
        {
        	sickness="fever";
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMT);
        Record record = new Record(recordId, dateFormat.format(calendar.getTime()), recordType);
        record.setRecordTypeData(recordTypeData);
        record.setRecordData(recordData);
        record.setUserCommnet(userComment);
        record.setSickness(sickness);
        EMRClient.getInstance().saveEmrRecord(record);
        EMRClient.getInstance().saveUserRecord(userId, recordId);
    }

    /**
     * Upload image
     * @param request
     * @param session
     * @throws FileUploadException
     */
    
    public static void uploadImage(HttpServletRequest request, HttpSession session)
            throws FileUploadException {

        if (ServletFileUpload.isMultipartContent(request)) {
            ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List fileItems = servletFileUpload.parseRequest(request);
            FileItem fileItem = null;
            String recordId = null;
            String fileComment = null;
            Iterator it = fileItems.iterator();
            while (it.hasNext()) {
                FileItem fileItemTemp = (FileItem) it.next();
                if (!fileItemTemp.isFormField()) {
                    fileItem = fileItemTemp;
                } else if ("recordId".equals(fileItemTemp.getFieldName())) {
                    recordId = fileItemTemp.getString();
                } else if ("fileComment".equals(fileItemTemp.getFieldName())) {
                    fileComment = fileItemTemp.getString();
                }
            }

            if (fileItem != null && recordId != null) {
                if (fileItem.getSize() > 0) {
                    UUID blobId = UUID.randomUUID();
                    Blob blob = new Blob();
                    blob.setBlobId(blobId);
                    blob.setComment("Scanned Emr Records");
                    blob.setFileName(fileItem.getName());
                    blob.setContentType(fileItem.getContentType());
                    blob.setFileSize(fileItem.getSize());
                    blob.setFileContent(fileItem.get());
                    blob.setTimeStamp(String.valueOf(System.currentTimeMillis()));
                    //blob.setComment(fileComment);
                    blob.setComment("Scanned Emr Records");
                    //save blob and update record
                    EMRClient.getInstance().saveEmrBlob(blob);
                    //set record blob ids
                    EMRClient.getInstance().saveBlobRecord(recordId, blobId);
                    //can not get all the columns in one row :(
                    //EMRClient.getInstance().updateRecordBlob(recordId, blobId);


                }
            }

        }
    }

    /**
     * Get latest record
     * @param recordList
     * @return
     */
    public static List<Record> getLatestRecords(List<Record> recordList){
        Collections.sort(recordList,new RecordDateComparator());
        return recordList;
    }
    
    /**
     * Search user
     * @param userPattern
     * @return
     */
    public static List<User> searchUser(String userPattern){
        //get user list
        //returen user list

        return  null;
    }
}
