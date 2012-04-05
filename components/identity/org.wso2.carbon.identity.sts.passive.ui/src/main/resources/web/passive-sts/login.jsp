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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>


<link
        media="all" type="text/css" rel="stylesheet"
        href="css/registration.css"/>

<fmt:bundle basename="org.wso2.carbon.identity.sts.passive.ui.i18n.Resources">

    <script type="text/javascript">
        function doLogin() {
            var loginForm = document.getElementById('loginForm');
            loginForm.submit();
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='passive.sts'/></h2>

        <div id="workArea">
            <table style="width:100%">
                <tr>
                    <td style="width:50%" id="loginTable">
                        <form action="../../passivests" method="post" id="loginForm">

                            <div id="loginbox" class="identity-box">
                                <strong id="loginDisplayText"><fmt:message
                                        key='enter.password.to.signin'/></strong>

                                <h2></h2>
                                <table id="loginTable">
                                    <tr height="22">
                                        <td colspan="2"></td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key='username'/></td>
                                        <td>
                                            <input type="text" id='username' name="username"
                                                   size='30'/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key='password'/></td>
                                        <td>
                                            <input type="password" id='password' name="password"
                                                   size='30'/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                            <input type="submit" value="<fmt:message key='login'/>"
                                                   class="button">
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </form>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>