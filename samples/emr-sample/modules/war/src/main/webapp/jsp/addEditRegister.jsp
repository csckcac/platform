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
<title>AddEditRegister</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="../js/emr-utils.js"></script>
<script type="text/javascript" src="../js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="../js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">
	<div class="col_12">
<%
    User user = UIHelper.getUser(session);
    boolean isLoggedIn = user != null;
    boolean isEdit = user != null;
%>
<%
    if(isLoggedIn){
%>
<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="middle">
            <% if (isEdit) {%>
             <h3><fmt:message key="title.emr"/></h3>
            <h5><fmt:message key="title.modify.user.profile"/></h5>
            <% } else {%>
            <h2><fmt:message key="title.emr"/></h2>
            <h4><fmt:message key="title.register"/></h4>
            <% }%>
        <div id="workArea">
            <form  action="saveUser.jsp" name="dataForm" onsubmit="return validatorAddEditRegister();" method="post">

                <table class="striped">
                    <thead>
                    <tr>
                        <!--th><fmt:message key="title.register"/></th -->
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="styledLeft" id="registerTable">

                                <tbody id="registerBody">

                                <tr>
                                    <td><fmt:message key="field.user.userId"/></td>
                                    <td>

                                        <% if (isEdit) { %>
                                        <input disabled="disabled" type="text" name="userId"
                                               id="userId"
                                               value="<%=user.getUserID()%>"/>
                                        <% } else {%>
                                        <input type="text" name="userId" id="userId"
                                               value=""/>
                                        <% } %>
                                    </td>
                                </tr>
                                <% if (!isEdit) {%>
                                <tr>
                                    <td><fmt:message key="field.user.password"/></td>
                                    <td>
                                        <input type="password" name="password" id="password"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.user.repassword"/></td>
                                    <td>
                                        <input type="password" name="repassword" id="repassword"/>
                                    </td>
                                </tr>
                                <% }%>
                                <tr>
                                    <td><fmt:message key="field.user.fullname"/></td>
                                    <td>
                                        <input type="text" name="fullname" id="fullname"
                                               value="<%=isEdit?user.getFullName():""%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.contact.details"/></td>
                                    <td>
                                        <input type="text" name="contactDetails" id="contactDetails"
                                               value="<%=isEdit?user.getEmail():""%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.dob"/></td>
                                    <td>
                                        <input type="text" name="dob" id="dob"
                                               value="<%=isEdit?user.getDateOfBirth():""%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.gender"/></td>
                                    <td>
                                        <select id="gender" name="gender">
                                            <% if (!isEdit) {%>
                                            <option value="select_a_value"><fmt:message
                                                    key="field.gender.select"/></option>
                                            <option value="male"><fmt:message
                                                    key="field.gender.male"/></option>
                                            <option value="female"><fmt:message
                                                    key="field.gender.female"/></option>
                                            <option value="default" selected><fmt:message
                                                    key="field.gender.default"/></option>
                                            <%
                                            } else if ("male".equals(user.getGender())) {
                                            %>
                                            <option value="male" selected="selected"><fmt:message
                                                    key="field.gender.male"/></option>
                                            <option value="female"><fmt:message
                                                    key="field.gender.female"/></option>
                                            <%
                                            } else {
                                            %>
                                            <option value="female" selected="selected"><fmt:message
                                                    key="field.gender.female"/></option>
                                            <option value="male"><fmt:message
                                                    key="field.gender.male"/></option>
                                            <%
                                                }
                                            %>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.blood.type"/></td>
                                    <td>
                                        <input type="text" name="bloodType" id="bloodType"
                                               value="<%=isEdit?user.getBloodGroup():""%>"/>
                                    </td>
                                </tr>
                                 <%--<tr>--%>
                                    <%--<td><fmt:message key="field.ethnicity"/></td>--%>
                                    <%--<td>--%>
                                        <%--<input type="text" name="ethnicity" id="ethnicity"--%>
                                               <%--value="<%=isEdit?user.getEthnicity():""%>"/>--%>
                                    <%--</td>--%>
                                <%--</tr>--%>
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
                            <%} else {%>
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
    }else {
        session.setAttribute("ERROR_MSG", "User Not Logged In.");
        response.sendRedirect("../login.jsp");

    }
%>
</div>
</div>
</body>
</html>
