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
<title>Login</title>
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
    boolean isDelete = false;
    String userId = null;
    String recordId = null;
    Record record = null;
    if (isLoggedIn) {
        isDelete = "delete".equals(request.getParameter("mode"));
        userId = request.getParameter("userId");
        recordId = request.getParameter("recordId");
        if (recordId != null && isDelete) {
            record = EMRClient.getInstance().getEmrRecordbyId(recordId);
        }
    }
%>

<%
    if (isLoggedIn) {
%>
<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="middle">
    <h3><fmt:message key="title.emr"/></h3>

    <div id="workArea">
        <form method="post" action="deleteRecord.jsp" name="dataForm">

            <table class="styledLeft">
                <thead>
                <tr>
                    <th align="left"><fmt:message key="title.delete.record"/></th>
                </tr>
                </thead>
                <tr>
                    <td class="formRaw">
                        <table class="styledLeft" id="createRecordTable">

                            <tbody id="createRecordBody">

                            <tr>
                                <td><fmt:message key="field.record.id"/></td>
                                <td>
                                    <input type="hidden" name="userId" id="userId"
                                           value="<%=userId%>"/>
                                    <input type="hidden" name="recordId" id="recordId"
                                           value="<%=recordId%>"/>
                                    <%=recordId%>
                                </td>
                            </tr>

                            <tr>
                                <td><fmt:message key="field.record.Type"/></td>
                                <td>

                                    <input type="hidden" name="recordType" id="recordType"
                                           value="<%=record.getRecordType()%>"/>
                                    <%=record.getRecordType()%>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.TypeInfo"/></td>
                                <td>

                                    <input type="hidden" name="healthStatus" id="healthStatus"
                                           value="<%=record.getRecordTypeData()%>"/>
                                    <%=record.getRecordTypeData()%>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.Data"/></td>
                                <td><%=record.getRecordData()%></td>
                            </tr>
                            <tr>
                                <td><fmt:message key="field.record.comment"/></td>
                                <td><%=record.getUserCommnet()%> </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                <tr>
                    <td class="buttonRow">

                        <input class="button" type="submit"
                               value="<fmt:message key="button.confirm.delete"/>"/>

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