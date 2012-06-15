<%@ page import="org.wso2.rnd.nosql.EMRClient" %>
<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.Record" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>
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
<title>CreateEditRecord</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="../js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="../js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">
	<div class="col_12">
<%
    User user = UIHelper.getUser(session);
    boolean isLoggedIn = user != null;
    Record record = null;
    boolean isEdit = false;
    String userId = null;
    String recordId = null;
    if (isLoggedIn) {
        isEdit = "edit".equals(request.getParameter("mode"));
        userId = request.getParameter("userId");
        recordId = request.getParameter("recordId");
        session.setAttribute("recordId", recordId);
        if (recordId != null && isEdit) {
            record = EMRClient.getInstance().getEmrRecordbyId(recordId);
        }
    }
    // only return one recored Record record = EMRClient.getInstance().getEmrRecordbyId(userId);
%>
<%
    if (isLoggedIn) {
%>
<script type="text/javascript" src="../js/emr-utils.js"></script>
<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="middle">
  
            <%-- h2><fmt:message key="title.create.record"/></h2 --%>
        <h3><fmt:message key="title.emr"/></h3>
    <div id="workArea">
        <form method="post" action="saveRecord.jsp" name="dataForm" onsubmit="return validateCreateEditRecord();">

            <table class="styledLeft">
                <thead>
                <tr align="left">
                    <% if (!isEdit) {%>
                    <th><fmt:message key="title.create.record"/></th>
                    <% } else {%>
                    <th><fmt:message key="title.edit.record"/></th>
                    <% } %>
                </tr>
                </thead>
                <tr>
                    <td class="formRaw">
                        <table class="styledLeft" id="createRecordTable">

                            <tbody id="createRecordBody">

                                <%--<tr>--%>

                                <%--<td>--%>
                                <%--<% if (!isEdit) {%>--%>
                                <%--<fmt:message key="field.record.id"/>--%>
                                <%--<%}%>--%>
                                <%--</td>--%>
                                <%--<td>--%>
                                <%--<input type="hidden" name="userId" id="userId"--%>
                                <%--value="<%=userId%>"/>--%>
                                <%--<% if (isEdit) {%>--%>

                                <%--<input type="hidden" name="recordId" id="recordId"--%>
                                <%--value="<%=record.getRecordID()%>"/>--%>

                                <%--<input type="text" name="recordId" id="recordId"--%>
                                <%--disabled="disabled"--%>
                                <%--value="<%=record.getRecordID()%>"/>--%>
                                <%--<% } else {%>--%>

                                <%--<input type="hidden" name="recordId" id="recordId"/>--%>
                                <%--<% } %>--%>
                                <%--</td>--%>
                                <%--</tr>--%>

                            <tr>
                                <td><fmt:message key="field.record.Type"/></td>
                                <td><input type="hidden" name="userId" id="userId"
                                           value="<%=userId%>"/>
                                    <%
                                        if (!isEdit) {
                                    %>
                                    <select name="recordType" id="recordType"
                                            onchange="populateRecordTypeData()">
                                        <option value="general" selected>General</option>
                                        <option value="wellness">Wellness</option>
                                        <option value="problem">Problem</option>
                                        <option value="medications">Medications</option>
                                        <option value="allergies">Allergies</option>
                                        <option value="testresults">Test results</option>
                                        <option value="procedures">Procedures</option>
                                        <option value="immunizations">Immunizations</option>
                                        <option value="insurance">Insurance</option>
                                    </select>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="recordType" id="recordType"
                                           value="<%=isEdit?record.getRecordType():""%>"/>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.TypeInfo"/></td>
                                <%if (!isEdit) {%>
                                <td>
                                    <select name="recordTypeData" id="recordTypeDataNew">
                                        <option value="Fine" selected>Fine</option>
                                        <option value="Critical">Critical</option>
                                        <option value="Uncomfortable">Uncomfortable</option>
                                        <option value="Usual">Usual</option>
                                    </select>
                                </td>
                                <%
                                } else {
                                %>
                                    <%--<td><fmt:message key="field.record.TypeInfo"/></td>--%>
                                <td>
                                    <input type="text"  name="recordTypeData" id="recordTypeData"
                                           value="<%=isEdit?record.getRecordTypeData():""%>"/>
                                </td>
                                <%
                                    }
                                %>
                            </tr>
                             <tr>
                                <td><fmt:message key="field.record.sickness"/></td>
                                <%if (!isEdit) {%>
                                <td>
                                    <select name="sicknessInfo" id="sicknessInfo">
                                        <option value="Fever" selected>Fever</option>
                                        <option value="Cough">Cough</option>
                                        <option value="Unusual">Unusual</option>
                                        <option value="Other">Other</option>
                                    </select>
                                </td>
                                <%
                                } else {
                                %>
                                    <%--<td><fmt:message key="field.record.TypeInfo"/></td>--%>
                                <td>
                                    <input type="text"  name="sicknessInfo" id="sicknessInfo"
                                           value="<%=record.getSickness()%>"/>
                                </td>
                                <%
                                    }
                                %>
                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.Data"/></td>
								<%if(isEdit){ %>
                                <td>
                                    <textarea rows="4" cols="30" name="recordData" id="recordData"
                                              value="<%=record.getRecordData()%>"><%=record.getRecordData()%>
                                    </textarea>
                               </td>
                               <%}else{ %>
                                <td>
                                    <textarea rows="4" cols="30" name="recordData" id="recordData"
                                              value="">
                                    </textarea>
                               </td>
                               <%} %>


                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.comment"/></td>
								<%if(isEdit){ %>
                                <td>
                                    <textarea rows="2" cols="30" name="userComment"
                                              id="userComment"
                                              value="<%=record.getUserCommnet()%>"><%=record.getUserCommnet()%>
                                    </textarea>

                                </td>
								 <%}else{ %>
								 <td>
                                    <textarea rows="2" cols="30" name="userComment"
                                              id="userComment"
                                              value="">
                                    </textarea>

                                </td>
                                <%} %>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                <tr>
                    <td class="buttonRow">
                        <% if (isEdit) {%>
                        <input class="button" type="submit"
                               value="<fmt:message key="button.update"/>"/>
                        <% } else {%>
                        <input class="button" type="submit"
                               value="<fmt:message key="button.create"/>"/>
                        <% } %>
                        <input class="button" type="button"
                               value="<fmt:message key="button.cancel"/>"
                               onclick="location.href = '../index.jsp'"/>
                    </td>
                </tr>
            </table>
        </form>
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
</html>