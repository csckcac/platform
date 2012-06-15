<%@ page import="org.wso2.rnd.nosql.EMRClient" %>
<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.UUID" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!--
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
~ under the License.
-->

<!DOCTYPE html>
<html><head>
<title>ShowBlob</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="../js/emr-utils.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<script type="text/javascript" src="../js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="../js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a>
<div id="wrap" class="clearfix">
	<div class="col_12">
	
<%
    User user = UIHelper.getUser(session);
    boolean isLoggedIn = user != null;
    String recordId = null;
    boolean blobState = false;
    List<UUID> blobList = null;
    EMRClient emrClient = null;
    if (isLoggedIn) {
        recordId = request.getParameter("recordId");
        emrClient = EMRClient.getInstance();
        if (recordId != null && !recordId.equals("")) {
            blobList = emrClient.getRecordBlobIds(recordId);
            //blobState = blobList != null;
            if(blobList.size() >= 0){
                blobState = true;
            }
        }

%>
               
<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="middle">
            <h3><fmt:message key="title.emr"/></h3>
            <h5><fmt:message key="title.emr.scanned"/></h5>

        <div id="workArea">
            <%
                if (blobState) {
            %>
            <%--<p><fmt:message key="title.record.id"/><%=recordId%>--%>
            </p>
            <table class="styledLeft" border="1" align="left">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="th.blob.file.name"/></th>
                    <th width="15%"><fmt:message key="th.blob.file.size"/></th>
                    <th width="15%"><fmt:message key="th.blob.file.timestamp"/></th>
                    <th width="15%"><fmt:message key="th.blob.file.comment"/></th>
                    <th width="15%"><fmt:message key="th.blob.file.action"/> </th>
                </tr>
                
                </thead>
                <%
                    for (UUID blobId : blobList) {
                        if (blobId == null || emrClient.getEmrBlob(blobId) == null) {
                            continue;
                        }
                %>
              	   
              
                <tr>
                    <td align="center"><a href="getEmrBlob.jsp?blobId=<%=blobId.toString()%>"
                           target="_blank"><%=emrClient.getEmrBlob(blobId).getFileName()%>
                           
                    </a></td>
                    <td align="center"><%=emrClient.getEmrBlob(blobId).getFileSize()%>
                    </td>
                    <td align="center"><%=emrClient.getEmrBlob(blobId).getTimeStamp()%>
                    </td>
                    <td align="center"><%=emrClient.getEmrBlob(blobId).getComment()%>
                    </td>
                   <td align="center"><a class="delete-icon-link"
                           onclick="deleteblob('<%=blobId.toString()%>','<%=recordId%>');"
                           href="#"><fmt:message
                            key="action.delete"/></a></td>
                </tr>
                <%}%>
            </table>
            <br>
            <br><br>
            <table><tr><td align="left"><input class="button" type="button"
                               value="<fmt:message key="button.back"/>"
                               onclick="location.href = '../index.jsp'"/></td></tr></table>
            <%} else {%>
            <p align="left">No Scanned Images for this Record</p>
            <%}%>
        </div>
    </div>
</fmt:bundle>
<%
    } else {
        session.setAttribute("ERROR_MSG", "User Not Logged In.");
        response.sendRedirect("../index.jsp");
    }
%>
</div>
</div>
</body>
</html>/