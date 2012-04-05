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
<%@ page import="org.wso2.carbon.mashup.jsservices.ui.Util" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mashup.jsservices.ui.MashupServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%@ page import="org.wso2.carbon.mashup.utils.MashupConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<%
    //Disabling browser caching
    response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", 0); //prevents caching at the proxy server

    String action = request.getParameter("action");
    if (action == null) {
        action = (String) session.getAttribute("mashup.action");
        session.removeAttribute("mashup.action");
    }
    String serviceName = request.getParameter("serviceName");

    //Sanity check. Immediately terminate further processing if this fails.
    boolean cannotContinue = false;
    String message = null;

    String[] nameParts = serviceName.split(MashupConstants.FORWARD_SLASH);
    String  mashupName = nameParts[1];
    if(nameParts.length != 2 || nameParts[0].equals("") || nameParts[1].equals("")) {
        message = "This service is not a JS Service. Please provide a JS Service name.";
        String returnPath = "newMashup.jsp";
        session.setAttribute("mashup.error.message", message);
        session.setAttribute("mashup.service.name", mashupName);
        %>
        <script type="text/javascript">
            location.href = "<%=returnPath%>";
        </script>
        <%
        return;

    } else {
        if (action == null) {
            message = "Mashup Editor validation failure. An action was not specified in the request.";
            cannotContinue = true;
        } else if (("edit".equalsIgnoreCase(action)) && (serviceName == null || serviceName.equals(""))) {
            message = "Mashup Editor validation failure. An edit request was made without a valid mashup being named for editing.";
            cannotContinue = true;
        } else if ("new".equalsIgnoreCase(action)) {
            if(serviceName == null || serviceName.equals("") || mashupName == null || mashupName.equals("")) {
                message = "Mashup Editor validation failure. A request was made to create a mashup without providing a valid name.";
                cannotContinue = true;
            } else {
                try {
                    Util.validateName(mashupName, "ServiceName");
                } catch (Exception e) {
                    String returnPath = "newMashup.jsp";
                    session.setAttribute("mashup.error.message", e.getMessage());
                    session.setAttribute("mashup.service.name", mashupName);
                    %>
                    <script type="text/javascript">
                        location.href = "<%=returnPath%>";
                    </script>
                    <%
                    return;
                }
            }
        }
 }

    String redirect;
    if (session.getAttribute("mashup.redirect") != null) {
        redirect = (String) session.getAttribute("mashup.redirect");
        session.removeAttribute("mashup.redirect");
    } else {
        redirect = "../service-mgt/index.jsp";
    }

    if (cannotContinue) {
        Util.handleException(request, message);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    String serviceSource = "";
    String customUISource = "";
    String gadgetUISource = "";

    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MashupServiceAdminClient client = new MashupServiceAdminClient(cookie, backendServerURL, configContext);
        if ("new".equalsIgnoreCase(action)) {
            if (client.doesServiceExists(serviceName)) {
                String returnPath = "newMashup.jsp";
                session.setAttribute("mashup.error.message", "A Mashup with the name " + mashupName + " already exists");
                session.setAttribute("mashup.service.name", mashupName);
                %>
                <script type="text/javascript">
                    location.href = "<%=returnPath%>";
                </script>
                <%
                return;
            } else {


            }
        //Inserting the initial mashup skeleton
        serviceSource = "/*\n" +
                "* Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "* you may not use this file except in compliance with the License.\n" +
                "* You may obtain a copy of the License at\n" +
                "*\n" +
                "* http://www.apache.org/licenses/LICENSE-2.0\n" +
                "*\n" +
                "* Unless required by applicable law or agreed to in writing, software\n" +
                "* distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "* See the License for the specific language governing permissions and\n" +
                "* limitations under the License.\n" +
                "*/\n" +
                "this.serviceName = \"" + mashupName + "\";\n" +
                "this.documentation = \"TODO: Add service level documentation here\" ;\n" +
                "\n" +
                "toString.documentation = \"TODO: Add operation level documentation here\" ;\n" +
                "toString.inputTypes = { /* TODO: Add input types of this operation */ };\n" +
                "toString.outputType = \"String\"; /* TODO: Add output type here */ \n" +
                "function toString()\n" +
                "{\n" +
                "   //TODO: Add function code here\n" +
                "   return \"Hi, my name is " + mashupName + "\";\n" +
                "}\n";

        customUISource = "A custom UI was not found for this mashup. You can use the 'Generate Template' button below to generate a sample.";
        gadgetUISource = "A gadget UI was not found for this mashup. You can use the 'Generate Template' button below to generate a sample.";


    } else if ("edit".equalsIgnoreCase(action)) {
        String[] contents = client.getMashupServiceContentAsString(serviceName);
        serviceSource = contents[0];
        customUISource = contents[1];
        gadgetUISource = contents[2];

        //replace html entities in order to properly display in the textarea
        serviceSource = serviceSource.replaceAll("&lt;", "&amp;lt;");
    }
} catch (Exception e) {
    Util.handleException(request, "Error occurred while trying to retrieve the contents of the JS service");
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<!-- Required Javascript -->

<script language="javascript" src="../editarea/edit_area_full.js" type="text/javascript"></script>


<script language="javascript" src="js/base64.js" type="text/javascript"></script>
<style type="text/css">
    /* --------------- inner tab styles -------------------- */

    div#editor ul.user li {
       display: inline;
    }

    div#editor ul.user li a {
       background-image: url(../js_service/images/gradient-tab.gif);
       display: inline;
       font-size: 10px;
       height: 20px;
       margin-left: 8px;
       padding: 4px 2px 2px 8px;
    }

    div#editor div.tab-right {
       background-image: url(../js_service/images/gradient-tab-right.gif);
       background-repeat: no-repeat;
       display: inline;
       font-size: 10px;
       height: 20px;
       margin-left: -7px;
       padding: 4px 0 3px;
    }

    /* --------------- iframe styles -------------------- */

    .panel {
        /*border: solid 1px black;*/
        background-color: white; /* padding: 5px;*/
        height: 95%;
        width: 100% !important;
        *width: 99% !important;
        clear: left;
        border-top: 3px solid #BDBEC0;
    }

    .panel iframe {
        height: 500px !important;
        width: 100% !important;
        overflow-x: auto;
        overflow-y: auto;
        _overflow-x: scroll;
        _overflow-y: scroll;
    }
</style>

<script language="JavaScript" type="text/javascript">
    var panels = new Array('mashup_code', 'ui_code', 'gadget_code');
    var selectedTab = null;
     function showPanel(tab, name)
    {
        if (selectedTab)
        {
            selectedTab.style.color = '';
            // 		selectedTab.style.fontWeight = '';
            selectedTab.style.textDecoration = '';

        }
        selectedTab = tab;
        selectedTab.style.color = 'black';
        //    selectedTab.style.fontWeight = 'bold';
        selectedTab.style.textDecoration = 'none';

        for (var i = 0; i < panels.length; i++)
        {
            document.getElementById(panels[i]).style.display = (name == panels[i]) ? 'block' : 'none';
            var syntaxArray = new Array("js","html","xml");

            if (name == panels[i]) {
                $(document).ready(function() {
                    editAreaLoader.init({
                                            id:panels[i] + "_text"
                                            ,syntax:syntaxArray[i]
                                            ,start_highlight:true
                                        });
                });
            }

        }
        return false;
    }

    function init() {
        try {
            showPanel(document.getElementById('tab1'), 'mashup_code');
        } catch(e) {
        }
    }

    function discardChanges() {
        window.location = "<%=redirect%>";
    }

    function saveSource(code, formName, contentsField, status) {
        /*var codeForm = document.getElementById(formName);
        var contents = document.getElementById(contentsField);
        contents.value = Base64.encode(code);
        codeForm.submit();*/
        var url = "save_mashup_ajaxprocessor.jsp";

        var request = createXmlHttpRequest();
        var data = "action=edit&serviceName=<%=serviceName%>&type=" + contentsField + "&source=" + Base64.encode(code);

        //Make sure the XMLHttpRequest object was instantiated
        if (request) {
            //open connection
            request.open("POST", url, true);

            //Send the proper header information along with the request
            request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            request.setRequestHeader("Content-length", data.length);
            request.setRequestHeader("Connection", "close");

            request.onreadystatechange = function () {
                 if (request.readyState == 4) {

                    try {
                        if (request.status == 200 && request.responseText.indexOf("true") > -1) {
                            if (status == "save") {
                                CARBON.showInfoDialog('Your changes were successfully saved and the'+
                                'service will be deployed.', function() {
                                    window.location.href = "../service-mgt/index.jsp";
                                });

                            } else {
                                CARBON.showInfoDialog('Your changes were successfully applied to the service.');
                            }
                        } else {
                            CARBON.showErrorDialog('Error occurred while saving the JS service', function() {
                                window.location.href = "../admin/error.jsp";
                            });
                        }
                    } catch(ex) {

                    }
                }
            }
            request.send(data);
        }
    }

    function generateNewUi(uiType) {
        var url = "generate_resources_ajaxprocessor.jsp?serviceName=<%=serviceName%>&type=" +
                  uiType + "&url=" + document.URL;

        var request = createXmlHttpRequest();

        //Make sure the XMLHttpRequest object was instantiated
        if (request)
        {
            //Check for requested web-accesible artifact (e.g. index.html, gadget.xml)
            request.open("GET", url, true);

            request.onreadystatechange = function () {
                if (request.readyState == 4) {

                    try {
                        if (request.status == 200) {
                             if (uiType == 'gadget') {
                                editAreaLoader.setValue('gadget_code_text',request.responseText);
                            } else {
                                editAreaLoader.setValue('ui_code_text',request.responseText);
                            }
                        } else {
                            var response = request.responseText;
                            if (response.indexOf("Service cannot be found.") > -1) {
                                CARBON.showErrorDialog("You have not yet saved the service. In order to generate a UI, please go to the 'Mashup Code' tab and save your service. ");
                            } else {
                                CARBON.showErrorDialog(response);
                            }

                        }
                    } catch(ex) {

                    }
                }
            }

            request.send(null);
        }
    }

    function createXmlHttpRequest() {

        var request;

        // Lets try using ActiveX to instantiate the XMLHttpRequest object
        try {
            request = new ActiveXObject("Microsoft.XMLHTTP");
        } catch(ex1) {
            try {
                request = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(ex2) {
                request = null;
            }
        }

        // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
        if (!request && typeof XMLHttpRequest != "undefined") {
            //The browser does, so lets instantiate the object
            request = new XMLHttpRequest();
        }

        return request;
    }

    if (window.attachEvent) window.attachEvent('onload', init);
    else window.addEventListener('DOMContentLoaded', init, false);

</script>
<fmt:bundle basename="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources">

    <carbon:breadcrumb
            label="jsservice.editor"
            resourceBundle="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<div id="middle">
    <h2>Editing the Mashup <%=mashupName%></h2>
    <div id="page">
        <div id="simple-content">
            <div id="mashup_editor" style="height: 80%;">
                <div id="editor">
                    <ul class="user">
                        <li><a href="#"
                                           onmousedown="return event.returnValue = showPanel(document.getElementById('tab1'), 'mashup_code');"
                                           id="tab1"
                                           onclick="return false;">Mashup Code</a></li>
                        <div class="tab-right"><img src="images/1px.gif" height="10" width="8"/></div>
                        <li><a href="#"
                                           onmousedown="return event.returnValue = showPanel(document.getElementById('tab2'), 'ui_code');"
                                           id="tab2"
                                           onclick="return false;">Custom UI Code</a></li>
                        <div class="tab-right"><img src="images/1px.gif" height="10" width="8"/></div>
                        <li><a href="#"
                                           onmousedown="return event.returnValue = showPanel(document.getElementById('tab3'), 'gadget_code');"
                                           id="tab3"
                                           onclick="return false;">Gadget UI Code</a></li>
                        <div class="tab-right"><img src="images/1px.gif" height="10" width="8"/></div>
                    </ul>
                </div>
                <div class="panel" id="mashup_code" style="display: block">
                    <form name="mashupCodeForm" id="mashupCodeForm">
                        <textarea id="mashup_code_text" rows="40" cols="100"
                                  style="width: 100%; height: 94%; *height:628px; display:block;"><%=serviceSource%>
                        </textarea>
                        <br>

                        <div style="margin-top: 10px;"><label id="source_save_status" style="float: left;">Tip: You can
                            use the buttons on the right to save or discard changes.</label>
                <span style="float:right">
                    <input type="button" class="button" value="Discard changes" onclick="discardChanges();"/>
                    <input type="button" class="button" value="Save changes"
                           onclick="saveSource(editAreaLoader.getValue('mashup_code_text'), 'mashupCodeForm', 'js', 'save');"/>
                    <input type="button" class="button" value="Apply changes"
                           onclick="saveSource(editAreaLoader.getValue('mashup_code_text'), 'mashupCodeForm', 'js', 'apply');"/>
                    <input type="hidden" name="mashupContents" id="mashupContents"/>
                </span>
                        </div>
                    </form>
                </div>
                <div class="panel" id="ui_code" style="display: block">
                    <form name="customUICodeForm" id="customUICodeForm">
                        <textarea id="ui_code_text" rows="40" cols="100"
                                  style="width: 100%; height: 94%; *height:628px; display:block;"><%=customUISource%>
                        </textarea>
                        <br>

                        <div style="margin-top: 10px;"><label id="ui_save_status" style="float: left;">Tip: You can
                            use the button on
                            the right to save changes.</label>
            <span style="float:right">
                <input type="button" class="button" value="Generate Template" onclick="generateNewUi('custom_ui');"/>
                <input type="button" class="button" value="Discard changes" onclick="discardChanges();"/>
                <%--<input type="button" class="button" value="Apply changes" onclick="saveUiSource(ui_code_text.getCode(),false, 'html');" />--%>
                <input type="button" class="button" value="Save changes"
                       onclick="saveSource(editAreaLoader.getValue('ui_code_text'), 'customUICodeForm', 'html', 'save');"/>
                <input type="button" class="button" value="Apply changes"
                       onclick="saveSource(editAreaLoader.getValue('ui_code_text'), 'customUICodeForm', 'html', 'apply');"/>
                <input type="hidden" name="customUIContents" id="customUIContents"/>
            </span>
                        </div>
                    </form>
                </div>
                <div class="panel" id="gadget_code" style="display: block">
                    <form name="gadgetUICodeForm" id="gadgetUICodeForm">
                        <textarea id="gadget_code_text" rows="40" cols="100"
                                  style="width: 100%; height: 94%; *height:628px; display:block;"><%=gadgetUISource%>
                        </textarea>
                        <br>

                        <div style="margin-top: 10px;"><label id="gadget_save_status" style="float: left;">Tip: You can
                            use the button on
                            the right to save changes.</label>
            <span style="float:right">
                <input type="button" class="button" value="Generate Template" onclick="generateNewUi('gadget');"/>
                <input type="button" class="button" value="Discard changes" onclick="discardChanges();"/>
                <%--<input type="button" class="button" value="Apply changes" onclick="saveUiSource(gadget_code_text.getCode(),false, 'gadget');" />--%>
                <input type="button" class="button" value="Save changes"
                       onclick="saveSource(editAreaLoader.getValue('gadget_code_text'), 'gadgetUICodeForm', 'gadget', 'save');"/>
                <input type="button" class="button" value="Apply changes"
                       onclick="saveSource(editAreaLoader.getValue('gadget_code_text'), 'gadgetUICodeForm', 'gadget', 'apply');"/>
                <input type="hidden" name="gadgetUIContents" id="gadgetUIContents"/>
            </span>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</fmt:bundle>