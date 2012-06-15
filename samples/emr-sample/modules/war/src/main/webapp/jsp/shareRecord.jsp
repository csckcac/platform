<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>

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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html><head>
<title>ShareRecord</title>
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
    if (isLoggedIn) {
%>

<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="header">Header</div>
    <div id="leftcolumn">Left Column</div>
    <div id="content">
        <form method="post" action="confirmShareRecord.jsp" name="shareForm">

            <div><p>Owner : <%=user.getUserID()%> (You) </p></div>
            <div id="search"><p>Share with : <input id="sbox" name="share" type="text">
                <select id="accesstype" name="acesslevel">
                    <option value="view">Can View</option>
                    <option value="edit">Can Edit</option>
                </select></p>
            </div>
            <div>
                <input class="button" type="submit"
                       value="<fmt:message key="button.confirm.share"/>"/>
                <input class="button" type="button"
                       value="<fmt:message key="button.cancel"/>"
                       onclick="location.href = '../index.jsp'"/>
            </div>
        </form>
    </div>
    <div id="footer">Footer</div>
</fmt:bundle>
<%
    } else {
        session.setAttribute("ERROR_MSG", "User Not Logged In.");
        response.sendRedirect("../index.jsp");
    }
%>


<%--//search user--%>
<%--//select user--%>
<%--//select access leve--%>
<%--//grant acess--%>
</div>
</div>
</body>
</html>
