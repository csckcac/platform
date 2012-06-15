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
<title>RequestDoctors</title>
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
<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <div id="middle">
            <h3><fmt:message key="title.manage.myrequets"/></h3>
        <div id="workArea">
            <form method="post" action="savebookdoctor.jsp" name="dataForm">

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="title.manage.myrequets"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRaw">
                            <table class="styledLeft" id="myRequestTable">

                                <tbody id="myRequestBody">

                                <tr>
                                    <td><fmt:message key="field.blood.pressure"/></td>
                                    <td>
                                        <input type="text" name="bloodPressure" id="bloodPressure"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.weight"/></td>
                                    <td>
                                        <input type="text" name="weight" id="weight"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="field.body.temperature"/></td>
                                    <td>
                                        <input type="text" name="temperature" id="temperature"/>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                    <tr>
                        <td class="buttonRow">
                            <input class="button" type="submit"
                                   value="<fmt:message key="button.submit"/>"/>
                            <input class="button" type="button" value="<fmt:message key="button.cancel"/>"
                                   onclick="location.href = 'index.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
</div>
</div>
</body>
</html>