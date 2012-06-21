<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
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

<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../schema/js/schema_util.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.schema.ui.i18n.Resources">
<%
    boolean isUploadError = false;
    if (request.getParameter("errorUpload") != null) {
        isUploadError = true;
        String error = request.getParameter("msg");
%>
        <script type="text/javascript">
            CARBON.showErrorDialog("<fmt:message key="unable.to.upload.schema"/> " + "<%=(error != null) ? error : ""%>");
        </script>
<%
    }
%>    
<script type="text/javascript">

    var callback =
    {
        success:handleSuccess,
        failure:handleFailure
    };

    function handleSuccess(o) {
        window.location = "../listSchema/schema.jsp?region=region3&item=governance_list_schema_menu";
    }

    function handleFailure(o) {
        var buttonRow = document.getElementById('buttonRow');
        var waitMessage = document.getElementById('waitMessage');

        buttonRow.style.display = "";
        waitMessage.style.display = "none";
        if (o.responseText) {
            CARBON.showErrorDialog("<fmt:message key="unable.to.upload.schema"/> "+o.responseText);
        } else {
            CARBON.showErrorDialog("<fmt:message key="unable.to.upload.schema"/>");
        }
    }

    function submitImportFormAsync() {

        var form = document.getElementById("schemaImportForm");
        YAHOO.util.Connect.setForm(form, false, false);
        YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
    }

    function submitUploadForm() {
        var form = document.getElementById("schemaUploadForm");
        form.submit();
    }

    function clearAll() {
        document.getElementById('uResourceFile').value = "";
        document.getElementById('uResourceName').value = "";
        document.getElementById('irFetchURL').value = "";
        document.getElementById('irResourceName').value = "";
    }

    function addSchema() {
        var reason = "";
        var addSelector = document.getElementById('addMethodSelector');
        var selectedValue = addSelector.options[addSelector.selectedIndex].value;

        if (selectedValue == "upload") {
            var uResourceFile = document.getElementById('uResourceFile');
            var uResourceName = document.getElementById('uResourceName');

            //reason += validateEmpty(uResourceFile, "<fmt:message key="schema.zip.file"/>");
            if (uResourceFile.value == null || uResourceFile.value == "") {
                reason += org_wso2_carbon_registry_common_ui_jsi18n["the.required.field"] + " "+
                          "<fmt:message key="schema.zip.file"/>"+
                          " " + org_wso2_carbon_registry_common_ui_jsi18n["not.filled"] + "<br />";
            }

            if (reason == "") {
                reason += validateEmpty(uResourceName, "<fmt:message key="name"/>");
            }

            if (reason != "") {
                CARBON.showWarningDialog(reason);
                return;
            }

            submitUploadForm();

        } else if (selectedValue == "import") {
            var irFetchURL = document.getElementById('irFetchURL');
            var irResourceName = document.getElementById('irResourceName');

            reason += validateEmpty(irFetchURL, "<fmt:message key="schema.url"/>");
            if (reason == "") {
                reason += validateUrl(irFetchURL, "<fmt:message key="schema.url"/>");
            }

            if (reason == "") {
                reason += validateEmpty(irResourceName, "<fmt:message key="name"/>");
            }

            if (reason != "") {
                CARBON.showWarningDialog(reason);
                return;
            }


            var buttonRow = document.getElementById('buttonRow');
            var waitMessage = document.getElementById('waitMessage');

            buttonRow.style.display = "none";
            waitMessage.style.display = "";

            submitImportFormAsync();
        }
    }

    function fillResourceImportDetailsForSchemas() {
        var filePath = document.getElementById('irFetchURL').value;
        var filename = resolveSchemaName(filePath);

        document.getElementById('irResourceName').value = filename;
    }

    function fillResourceUploadDetails() {
        var filePath = document.getElementById('uResourceFile').value;
        var filename = resolveSchemaName(filePath);

        // deriving the media type.
        if (filename.search(/\.xsd$/i) >= 0) {
            // so it is a single schema.
            document.getElementById('uMediaType').value = "application/x-xsd+xml";
            document.getElementById('uploadName').style.display = "";
        } else if (filename.search(/\.(zip|gar)$/i) >= 0) {
            // so it is a zip
            document.getElementById('uMediaType').value = "application/vnd.wso2.governance-archive";
            document.getElementById('uploadName').style.display = "none";
        } else {
            document.getElementById('uResourceFile').value = "";
            CARBON.showWarningDialog("<fmt:message key="only.filetypes.allowed"/>");
        }

        document.getElementById('uResourceName').value = filename;
    }

    function resolveSchemaName(filepath) {
        var filename = "";
        if (filepath.indexOf("\\") != -1) {
            filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
        } else {
            filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
        }
        if (filename.search(/\.[^?]*$/i) < 0) {
            filename = filename.replace("?", ".");
            var suffix = ".xsd";
            if (filename.indexOf(".") > 0) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + suffix;
            } else {
                filename = filename + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            filename = filename.replace(c, "_");
        }
        return filename;
    }

</script>
    <carbon:breadcrumb
            label="schema.menu.text"
            resourceBundle="org.wso2.carbon.governance.schema.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <script type="text/javascript">
    </script>
    <div id="middle">

        <h2><fmt:message key="add.schema"/></h2>
        <div id="workArea">

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="add.schema.table.heading"/></th>
                    </tr>
                    </thead>

                    <tr>
                        <td class="formRow">
                            <table width="100%">
                                <tr>
                                    <td>
                                        <div>
                                        <select id="addMethodSelector" onchange="viewAddSchemaUI()">
                                            <option value="import" <%= !isUploadError? "selected=\"selected\"" : "" %> ><fmt:message key="import.schema.from.url"/></option>
                                            <option value="upload" <%= isUploadError? "selected=\"selected\"" : "" %> ><fmt:message key="upload.schema.from.file"/></option>
                                        </select>
                                        </div>
                                        <br/>
                                        <div id ="importUI" <%= isUploadError? "style=\"display:none;\"" : "" %> >
                                            <form action="../resources/import_resource_ajaxprocessor.jsp" method="post"
                                                  id="schemaImportForm">
                                                <input type="hidden" name="printerror" value="true"/>
                                                <input type="hidden" name="parentPath" value="/"/>
                                                <input type="hidden" name="mediaType" value="application/x-xsd+xml"/>
                                                <%--<input type="hidden" name="isAsync" value="true"/>--%>
                                                <table class="normal">
                                                    <tr>
                                                        <td><fmt:message key="schema.url"/> <span class="required">*</span></td>
                                                        <td><input type="text"
                                                                   onchange="fillResourceImportDetailsForSchemas()"
                                                                   name="fetchURL" style="width:400px" id="irFetchURL"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td><fmt:message key="name"/> <span class="required">*</span></td>
                                                        <td><input type="text" name="resourceName" style="width:400px"
                                                                   id="irResourceName"/></td>
                                                    </tr>
                                                </table>
                                            </form>
                                        </div>
                                        <div id="uploadUI" <%= !isUploadError? "style=\"display:none;\"" : "" %> >
                                            <form method="post"
                                                  name="schemaUploadForm"
                                                  id="schemaUploadForm"
                                                  action="../../fileupload/resource"
                                                  enctype="multipart/form-data" target="_self">
                                                 <input type="hidden" id="uPath" name="path" value="/"/>
                                                 <input type="hidden" id="uMediaType" name="mediaType"/>
                                                 <input type="hidden" id="uDescription" name="description" value=""/>
                                                 <input type="hidden" id="uRedirect" name="redirect" value="listSchema/schema.jsp?region=region3&item=governance_list_schema_menu"/>
                                                 <input type="hidden" id="uErrorRedirect" name="errorRedirect" value="schema/schema.jsp?errorUpload=errorUpload"/>

                                                 <table class="normal">
                                                    <tr>
                                                        <td><fmt:message key="schema.zip.file"/> <span class="required">*</span></td>
                                                        <td> <p>
                                                             <input id="uResourceFile" type="file" name="upload" size="50"
                                                                    style="background-color:#cccccc"
                                                                    onchange="fillResourceUploadDetails()"
                                                                    onkeypress="return blockManual(event)"/>
                                                            </p>
                                                            <p>
                                                                <fmt:message key="possible.uploadable.formats"/>
                                                            </p>
                                                        </td>
                                                    </tr>
                                                    <tr id="uploadName" style="display:none;">
                                                        <td><fmt:message key="name"/> <span class="required">*</span></td>
                                                        <td><input type="text" name="filename" style="width:400px"
                                                                   id="uResourceName"/></td>
                                                    </tr>
                                                </table>

                                             </form>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr id="buttonRow">
                        <td class="buttonRow">
                            <input class="button registryWriteOperation" type="button" onClick="addSchema();"
                                   value='<fmt:message key="add"/>'/>
                            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                                   value='<fmt:message key="add"/>'/>
                            <input type="button" id="#_1" value="<fmt:message key="clear"/>" class="button"
                   onclick="clearAll()"/>
                        </td>
                    </tr>
                    <tr id="waitMessage" style="display:none">
                        <td>
                            <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important" class="ajax-loading-message"><img
                                    src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message key="please.wait.until.schema.is.added"/>...
                            </div>
                        </td>
                    </tr>
                </table>
        </div>
    </div>
<script type="text/javascript">
    jQuery(document).ready(function() {
        var addSelector = document.getElementById('addMethodSelector');
        addSelector.selectedIndex = <%= isUploadError? "1" : "0" %>;
    });
</script>
</fmt:bundle>
