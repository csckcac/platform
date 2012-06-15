<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
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
<title>UploadImages</title>
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
    String recordId = null;
    if (isLoggedIn) {
        recordId = request.getParameter("recordId");
%>

<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <script type="text/javascript">
        function validate() {
            var fileName = document.imageUpload.imageFilename.value;
            //            if (fileName == '') {
        <%--CARBON.showErrorDialog('<fmt:message key="select.rule.service"/>');--%>
            //            } else if (fileName.lastIndexOf(".aar") == -1 && fileName.lastIndexOf(".rsl") == -1) {
        <%--CARBON.showErrorDialog('<fmt:message key="select.rule.file"/>');--%>
            //            } else {
            document.imageUpload.submit();
            //            }
        }
    </script>

    <div id="middle">
         <h3><fmt:message key="title.emr"/></h3>
        <h5><fmt:message key="th.record.upload.docs"/></h5>

        <div id="workArea">
            <form method="post" name="imageUpload" action="fileSave.jsp"
                  enctype="multipart/form-data" target="_self">
                <table class="striped" >
                    <%--<thead>--%>
                    <%--<tr>--%>
                        <%--<th colspan="2"><fmt:message key="title.upload.images"/> (.png or .gif)</th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>
                    <tr>
                        <td class="formRow">
                            <table class="striped">
                                <tr>
                                    <td>
                                        <label><fmt:message key="path.to.file"/>
                                            </label>
                                    </td>
                                    <td>
                                        <input type="file" id="imageFilename" name="imageFilename"
                                               size="40"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="hidden" name="recordId" id="recordId"
                                   value="<%=recordId%>">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="button.upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button"
                                   onclick="location.href = '../index.jsp'"
                                   value=" <fmt:message key="button.cancel"/> "/>
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
        response.sendRedirect("../login.jsp");
    }
%>
</div>
</div>
</body>
</html>