<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%
String backendServerURL =
            CarbonUIUtil.getServerURL(config.getServletContext(), session);
%>
<%--
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>
<fmt:bundle basename="org.wso2.carbon.mashup.javascript.stubgenerator.ui.i18n.Resources">
<!-- Required CSS -->
<script type="text/javascript" src="../carbon/global-params.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>

<script language="JavaScript" type="text/javascript">

    wso2.wsf.Util.initURLs();

    var frontendURL = wso2.wsf.Util.getServerURL() + "/";

    function clearText() {
        document.getElementById("js-stub").value = "";
    }

    var callback =
    {
        success:handleSuccess,
        failure:handleFailure,
        upload:handleSuccess
    };

    function handleSuccess(o) {
        var browser = WSRequest.util._getBrowser();

        if (browser == "ie" || browser == "ie7") {
            document.getElementById("js-stub").value =
            o.responseXML.documentElement.getElementsByTagName("ns:return")[0].firstChild.nodeValue;
        } else {
            document.getElementById("js-stub").value =
            o.responseXML.documentElement.firstChild.textContent;
        }

    }

    function handleFailure(o) {
        document.getElementById("js-stub").value = o.responseText;
    }

    function submitFormAsync(formId, isFileUpload) {
        clearText();
        var form = document.getElementById(formId);

        if ((isFileUpload) && (document.getElementById("wsdl").value == "")) {
            CARBON.showWarningDialog("Please browse your file system for a valid wsdl document");
        } else if ((!isFileUpload) && (document.getElementById("url").value == "")) {
            CARBON.showWarningDialog("Please enter the url of a valid wsdl document");
        } else {
            if (isFileUpload) {
                YAHOO.util.Connect.setForm(form, true, true);
                YAHOO.util.Connect.asyncRequest("POST", frontendURL + "JavaScriptStubGeneratorService/genarateStub", callback, null);
            } else {
                YAHOO.util.Connect.setForm(form);
                YAHOO.util.Connect.asyncRequest("POST", frontendURL + "JavaScriptStubGeneratorService/genarateStubFromURL", callback, null);
            }
        }
    }

    function noEnter(e) {
        var keynum = "";
        if (window.event) // IE
        {
            keynum = e.keyCode;

            if (keynum == 13) {
                e.cancelBubble = true;
                e.returnValue = false;
            }
        }
        else if (e.which) // Netscape/Firefox/Opera
        {
            keynum = e.which;
            if (keynum == 13) {
                e.preventDefault();
            }
        }

    }

    jQuery(document).ready(clearText);

</script>

    <carbon:breadcrumb
            label="js_stub_generator.headertext"
            resourceBundle="org.wso2.carbon.mashup.javascript.stubgenerator.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
    <h2><fmt:message key="js_stub_generator.headertext"/></h2>

    <div id="page">
        <div id="welcome">
        </div>
        <div id="simple-content">
            <form id="form" method="post" enctype="multipart/form-data" target="uploadFrame">

                <table>
                    <tr>
                        <th class="mashup_label" align="left">DOM</th>
                        <td><input type="radio" name="type" value="dom" checked="checked"/></td>
                    </tr>
                    <tr>
                        <th class="mashup_label" align="left">E4X</th>
                        <td><input type="radio" name="type" value="e4x"/></td>
                    </tr>
                    <tr>
                        <th class="mashup_label" align="left">Upload WSDL File</th>
                        <td><input size="100" type="file" id="wsdl" name="wsdl" onkeydown="noEnter(event)"/></td>
                    </tr>
                </table>

                <br>
                <input type="button" class="button" id="doUpload" name="doUpload" value="Generate from File"
                       onclick="submitFormAsync('form',true)"/><br/><br/><br/></form>

            <form id="form2" method="post" enctype="multipart/form-data"
                  target="uploadFrame">

                <table>
                    <tr>
                        <th class="mashup_label" align="left">DOM</th>
                        <td><input type="radio" name="type" value="dom" checked="checked"/></td>
                    </tr>
                    <tr>
                        <th class="mashup_label" align="left">E4X</th>
                        <td><input type="radio" name="type" value="e4x"/></td>
                    </tr>
                    <tr>
                        <th class="mashup_label" align="left">Read WSDL URL&nbsp;&nbsp;&nbsp;</th>
                        <td><input size="100" type="text" id="url" name="url" onkeydown="noEnter(event)"/></td>
                    </tr>
                </table>

                <br>
                <input type="button" class="button" value="Generate from URL"
                       onclick="submitFormAsync('form2',false)"/><br/><br/></form>

            <div>
                <textarea id="js-stub" rows="25" cols="" class="codepress javascript"
                          style="width: 100%; *width: 96%; height: 60%;"> </textarea>
            </div>
        </div>

        <div id="testdiv"></div>
    </div>
</fmt:bundle> 