<%@ page import="org.*" %>
<%@ page import="org.wso2.rnd.nosql.EMRClient" %>
<%@ page import="org.wso2.rnd.nosql.model.*" %>
<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.RecordDateComparator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
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
<title>Home</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="js/emr-utils.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">

	<div class="col_12">
	<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
 <%
	UIHelper.processSignIn(session,request.getUserPrincipal().getName(),config);
	String errMsg = UIHelper.getAndClearErrorMsg(session);
    EMRClient emrClient = null;
        try {
            emrClient = EMRClient.getInstance();
        } catch (NullPointerException e) {
            session.setAttribute("ERROR_MSG", "Cassandra Connection Error");
            System.out.println("Cassandra Connection Error");
        }
        User user=(User)session.getAttribute(UIHelper.USER);
%>
<div>
<h3><fmt:message key="title.emrLogged"/></h3>
		<p align="right">  <fmt:message key="field.emr.registration"/>&nbsp;<%=user.getUserID()%>
            <a class="edit-icon-link"
               href="jsp/addEditRegister.jsp"><fmt:message
                    key="field.update.profile"/></a> &nbsp;
            <a class="edit-icon-link"
               href="jsp/logout.jsp"><fmt:message
                    key="field.logout"/></a>
		</p>
</div> 
<% if(user!=null){%>
<ul class="menu">
	<li class="current"><a href="">Categories</a>
	<ul>
	<li>
            <a class="add-icon-link"
               href="jsp/createEditRecord.jsp?mode=add&userId=<%=user.getUserID()%>"><fmt:message
                    key="title.create.record"/></a></li>
    <li>
            <a class="edit-icon-link"
               href="jsp/trackHealth.jsp?userId=<%=user.getUserID()%>"><fmt:message
                    key="title.tract.myhealth"/></a></li>
	</ul>	
	</li>
	<li><a href="">Features</a></li>
</ul>
<div>
<h6><fmt:message key="field.user.profile"/></h6>
</div>
<div id="middle">
<div id="workArea">
            <table class="tight" id="createRecordTable">
            <col width="25" />
  			<col width="25" />
                <tbody id="createRecordBody">
                 <tr>
                    <td><fmt:message key="field.contact.fullname"/></td>
                    <td>
                        <%=user.getFullName()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="field.contact.details"/></td>
                    <td>
                        <%=user.getEmail()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="field.dob"/></td>
                    <td>
                        <%=user.getDateOfBirth()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="field.gender"/></td>
                    <td>
                        <%=user.getGender()%>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="field.blood.type"/></td>
                    <td>
                        <%=user.getBloodGroup()%>
                    </td>
                </tr>
                </tbody>
            </table>
				 <%
                    List<String> userRecordIds = emrClient.getCurrentUserRecordIds(user.getUserID());

                    if (!userRecordIds.isEmpty()) {
                %>
            <h6><fmt:message key="field.user.recordInfo"/></h6>
            <table class="striped" id="recordTable" border="1" align="left">
               
                <thead>
                <tr>
                    <th width="10%"><font size='2'><fmt:message key="th.record.number"/></font></th>
                    <th width="20%"><font size='2'><fmt:message key="th.record.timestamp"/></font></th>
                    <th width="10%"><font size='2'><fmt:message key="th.record.type"/></font></th>
                    <th width="10%"><font size='2'><fmt:message key="th.record.sickness"/></font></th>
                    <th width="10%"><font size='2'><fmt:message key="th.record.blob"/></font></th>
                    <th width="10%"><font size='2'><fmt:message key="th.record.upload.docs"/></font></th>
                    <th width="20%"><font size='2' ><fmt:message key="th.actions"/></font></th>
                </tr>
                </thead>
                <tbody id="recordBody">
                <%
                	String userRecordId=null;
                    List<Record> dashboardRecordList = new ArrayList<Record>();
                    int j = 0;
                    for (; j < userRecordIds.size(); j++) {
                        userRecordId = userRecordIds.get(j);
                        Record tmpEmr = emrClient.getEmrRecordbyId(userRecordId);
                        if (tmpEmr != null) {
                            dashboardRecordList.add(tmpEmr);
                        }

                    }
                    Collections.sort(dashboardRecordList, new RecordDateComparator());
                    int recordCount = 0;
                    for (Record currentEmr : dashboardRecordList) {
                        recordCount++;
                %>
                <tr>
                    <td align="center"><%=recordCount%>
                    </td>
                    <td align="center"><%=currentEmr.getTimeStamp()%>
                    </td>
                    <td align="center"><%=currentEmr.getRecordType()%>
                    </td>
                    <td align="center"><%=currentEmr.getSickness()%>
                    </td>
                    <td align="center"><a class="edit-icon-link"
                           onclick="showBlobs('<%=currentEmr.getRecordID()%>');"
                           href="#"><fmt:message
                            key="action.show.blob"/></a></td>
                    <td align="center"><a class="upload-icon-link"
                           onclick="uploadimage('<%=currentEmr.getRecordID()%>');"
                           href="#"><fmt:message
                            key="action.upload"/></a></td>
                    <td align="center">
                            <input type="hidden" name="recordName<%=j%>"
                            id="recordName<%=j%>"
                            value="<%=userRecordId%>"/>
                        <a class="edit-icon-link"
                           onclick="showrecordEditor('<%=currentEmr.getRecordID()%>','<%=user.getUserID()%>');"
                           href="#"><fmt:message
                                key="action.edit"/></a>

                        <a class="delete-icon-link"
                           onclick="deleterecord('<%=user.getUserID()%>','<%=currentEmr.getRecordID()%>');"
                           href="#"><fmt:message
                                key="action.delete"/></a>
                    </td>
                </tr>
                <% }%>
                <% }%>
                </tbody>
            </table>
<%}%>
</div>
</div>
</fmt:bundle>
	</div>
</div><!-- END WRAP -->
</body></html>
