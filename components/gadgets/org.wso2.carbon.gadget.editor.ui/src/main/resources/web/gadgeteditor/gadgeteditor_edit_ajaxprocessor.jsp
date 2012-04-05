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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.clients.GadgetEditorServiceClient" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.AddServicesUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.UIGeneratorConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>

<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<fmt:bundle basename="org.wso2.carbon.gadget.editor.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.gadget.editor.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.gadget.editor.ui"/>
<%

    String path = request.getParameter("path");
    if (path.startsWith(RegistryConstants.CONFIG_REGISTRY_BASE_PATH)) {
        path = path.substring(RegistryConstants.CONFIG_REGISTRY_BASE_PATH.length());
    }

    boolean isVersionedPage = false;
    String disabledAttr = "";
    String saveButtonClass = "button registryWriteOperation";
    if (path.contains(";version")) {
        isVersionedPage = true;
        disabledAttr = " disabled=\"1\"";
        saveButtonClass = "button registryNonWriteOperation";
    }

    String content = "<test>\n<a>\n</a>\n</test>";
    try {
        GadgetEditorServiceClient client = new GadgetEditorServiceClient(config,session);
        content = client.openGadget(path);
        //content = path;
    } catch (Exception e)
    {
        %>
        <%=path%>
        <%=e.getMessage()%>
        <%
    }    
%>

<script src="../gadgeteditor/js/codemirror.js" type="text/javascript"></script>
<style type="text/css">
     .CodeMirror-line-numbers {
        width: 2.2em;
        color: #aaa;
        background-color: #eee;
        text-align: right;
        padding: .4em;
        margin: 0;
        font-family: monospace;
        font-size: 10pt;

      }
</style>
<script type="text/javascript" language="javascript">
    var editor = null;

    function saveCode() {
        var reason = "";
        var CustomUIForm=document.getElementById('CustomUIForm');
        var waitMessage = document.getElementById('waitMessage');
        var buttonRow = document.getElementById('buttonRow');
        var form = document.forms['CustomUIForm'];
        var msg = document.getElementById('editorStatus');
        if (form == undefined) {
            reason = "Custom UI form error!";
            return;
        }
        
        form.codeField.value = editor.getCode();
        if (reason != "") {
            CARBON.showWarningDialog(reason);
        }
        else {
            buttonRow.style.display = "none";
            msg.innerHTML = "Saving...";
            waitMessage.style.display = "block";
            var params = form.serialize();
            
            new Ajax.Request(form.action,
            {
                method:'post',
                parameters: params,
                onSuccess: function(transport) {
                    buttonRow.style.display = "block";
                    waitMessage.style.display = "none";
                },
                onFailure: function() {
                    //showRegistryError(org_wso2_carbon_registry_resource_ui_jsi18n["form.processing.failed"]);
                    buttonRow.style.display = "block";
                    waitMessage.style.display = "block";
                    msg.innerHTML = "Saving Failed!";
                }
            });
        }
    };
</script>


<div id="middle">
    <div id="workArea">
        <div id="activityReason" style="display: none;"></div>
        <form id="CustomUIForm" action="../gadgeteditor/save_gadgeteditor_ajaxprocessor.jsp" method="post">
            <input type="hidden" name="operation" value="Save">
            <input type="hidden" name="codeField" value="">
            <input type="hidden" name="path" value="<%=path%>"> 
            <div style="border: 1px solid black; padding: 0;">
                <textarea id="codeTextArea" cols="120" rows="30">
                    <%=content%>
                </textarea>
            </div>
            <table class="styledLeft">
                <tr id="buttonRow" style="width:100%;">
                    <td colspan="3" class="buttonRow" style="width:100%;">
                        <input class="<%=saveButtonClass%>" type="button" <%=disabledAttr%>
                               value="<fmt:message key="save.code"/>" onclick="saveCode()"/>
                    </td>
                </tr>
                <tr id="waitMessage" style="display:none;width:100%">
                    <td colspan="3" style="width:100%;">
                        <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;" id="editorStatus">
                        </div>
                    </td>
                </tr>
            </table>
        </form>
        <br/>

        <div id="AddService">
        </div>
    </div>
</div>
 <script type="text/javascript" language="javascript">
    editor = CodeMirror.fromTextArea('codeTextArea', {
        height: "600px",
        parserfile: ["parsexml.js", "parsecss.js", "tokenizejavascript.js", "parsejavascript.js", "parsehtmlmixed.js", "parsegadgetxml.js"],
        stylesheet: ["../gadgeteditor/css/xmlcolors.css", "../gadgeteditor/css/jscolors.css", "../gadgeteditor/css/csscolors.css"],
        path: "../gadgeteditor/js/",
        lineNumbers: true,
        autoMatchParens: true
      });
</script>
</fmt:bundle>
