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

<%@page import="org.wso2.carbon.identity.sso.saml.ui.SAMLSSOProviderConstants"%>


<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.stratos.identity.saml2.sso.mgt.ui.Util" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>WSO2 - Application Factory</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
        <link href="../carbon/appfactory/lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }
    </style>
    <link href="../carbon/appfactory/lib/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
    <link href="../carbon/appfactory/css/localstyles.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="../carbon/appfactory/lib/html5/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <!-- <link rel="shortcut icon" href="../assets/ico/favicon.ico">
  <link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">
  <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png">
  <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png">
  <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png">-->

</head>

<body>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">

            <a class="brand" href="#"></a>

            <!--/.nav-collapse -->

            <ul class="nav actions login-sign-up pull-right">
                <li>
                    <button id="logout-link" class="btn btn-danger" onClick="window.location.href='../appmgt/sign_up.jag'">Sign Up</button>
                </li>
            </ul>

        </div>
    </div>
</div>


<fmt:bundle basename="org.wso2.stratos.identity.saml2.sso.mgt.ui.i18n.Resources">
    <%
        String errorMessage = "login.fail.message";
        String tenantRegistrationPageURL = Util.getTenantRegistrationPageURL();

        if (request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE) != null &&
            (Boolean)request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE)) {
            if(request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE_MSG) != null){
                errorMessage = (String) request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE_MSG);
            }
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('<fmt:message key="<%=errorMessage%>"/>');
        });
    </script>
    <%
        }  else if (request.getSession().getAttribute(CarbonUIMessage.ID) !=null) {
            CarbonUIMessage carbonMsg = (CarbonUIMessage)request.getSession().getAttribute(CarbonUIMessage.ID);
            %>
            
                <script type="text/javascript">
                    jQuery(document).ready(function() {
                        CARBON.showErrorDialog("<%=carbonMsg.getMessage()%>");
                    });
                </script>
      <%}
    %>
    <script type="text/javascript">
        function doLogin() {
            var loginForm = document.getElementById('loginForm');
            loginForm.submit();
        }
        function doRegister() {
            document.getElementById('registrationForm').submit();
        }
    </script>

<div class="container">
    <!-- row of columns -->
    <div class="row">
        <div class="span12">
            <ul class="breadcrumb">
                <li>
                    <a href="#">&nbsp;</a>
                </li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="span12 title-back"><h1>WSO2 - Application Factory</h1></div>
    </div>
    <div class="row">
        <div class="span12 page-content">
            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span12">

                        <form class="form-horizontal well" action="../samlsso" method="post"   id="loginForm">
                            <fieldset>
                                <div class="control-group">
                                    <label class="control-label" for="username"><fmt:message key='username'/></label>

                                    <div class="controls">
                                        <input type="text" class="required input-xlarge" id="username" name="username">

                                        <p class="help-block">Enter your username or email address.</p>
                                    </div>
                                </div>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.ISSUER %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.ISSUER) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.REQ_ID %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.REQ_ID) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.SUBJECT %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.SUBJECT) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.RP_SESSION_ID %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.RP_SESSION_ID) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.ASSERTION_STR %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.ASSERTION_STR) %>"/>
                                <input type="hidden" name="<%= SAMLSSOProviderConstants.RELAY_STATE %>"
                                       value="<%= request.getAttribute(SAMLSSOProviderConstants.RELAY_STATE) %>"/>
                                <div class="control-group">
                                    <label class="control-label" for="password"><fmt:message key='password'/></label>

                                    <div class="controls">
                                        <input type="password" class="required input-xlarge" id="password" name=password>

                                        <p class="help-block"><a>Forgot your password?</a></p>
                                    </div>
                                </div>

                                <div class="form-actions">
                                    <button type="btn btn-primary" class="btn btn-primary">Sing-in</button>
                                    <button class="btn btn-primary">Cancel</button>
                                </div>
                            </fieldset>
                        </form>



                    </div>
                </div>
            </div>


        </div>
    </div>

    <hr>

    <footer>
        <p>&copy; WSO2 2012</p>
    </footer>

</div>
<!-- /container -->

<!--Elements to display popups-->
<div class="modal fade" id="messageModal"></div>
<div id="confirmation-data" style="display:none;">
    <div class="modal-header">
        <button class="close" data-dismiss="modal">×</button>
        <h3 class="modal-title">Modal header</h3>
    </div>
    <div class="modal-body">
        <p>One fine body…</p>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn btn-primary">Save changes</a>
        <a href="#" class="btn btn-other" data-dismiss="modal">Close</a>
    </div>
</div>

<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="../carbon/appfactory/lib/jquery/jquery-1.7.2.min.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-transition.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-alert.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-modal.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-dropdown.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-scrollspy.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-tab.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-tooltip.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-popover.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-button.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-collapse.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-carousel.js"></script>
<script src="../carbon/appfactory/lib/bootstrap/js/bootstrap-typeahead.js"></script>
<script src="../carbon/appfactory/lib/jquery.validate.min.js"></script>
<script src="../carbon/appfactory/js/messages.js"></script>
<script src="../carbon/appfactory/js/login.js"></script>
</fmt:bundle>
</body>
</html>