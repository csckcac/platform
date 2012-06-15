<%@ page import="org.wso2.rnd.nosql.EMRClient" %>
<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.Blob" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.util.UUID" %>
<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.  --%>
<%
    User user = UIHelper.getUser(session);
    boolean isLoggedIn = user != null;
    if (isLoggedIn) {
        try {
            //String recordId = request.getParameter("recordId");
            String blobId = request.getParameter("blobId");
            EMRClient emrClient = EMRClient.getInstance();
            //blobId = "94c7d7bf-1388-4239-9b3f-26cd0a4212ce";
            Blob blob = null;
            if (blobId != null) {
                blob = emrClient.getEmrBlob(UUID.fromString(blobId));
                if (blob != null) {
                    // get the image from the database
                    byte[] imgData = blob.getFileContent();
                    // display the image
                    response.setContentType(blob.getContentType());
                    // response.setContentType("image/png");
                    OutputStream o = response.getOutputStream();
                    o.write(imgData);
                    o.flush();
                    o.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;


        }
    }
%>
