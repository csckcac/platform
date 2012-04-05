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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServicesUtil" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.UIGeneratorConstants" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>


<%--YUI Rich Text script imports--%>
<link rel="stylesheet" type="text/css"
      href="../yui/build/editor/assets/skins/sam/simpleeditor.css"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/element/element-beta-min.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/editor/simpleeditor-min.js"></script>

<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.services.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.services.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.services.ui"/>
<script type="text/javascript" src="../services/js/services.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<%
    boolean isBrowseOnly = false;
    if(!CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/metadata/add")){
        if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")){
            isBrowseOnly=true;
        }else{
            return;
        }
    }
    String type = request.getParameter("type");
    String error = "Wrong configuration in " + RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH;
    AddServicesServiceClient client = new AddServicesServiceClient(config,session);

    String path = request.getParameter("path");
    String nextPatch = "";
    String nextMajor = "";
    String nextMinor = "";
    if (type != null) {
        if (type.equals("patch")) {
            try {
                nextPatch = Integer.toString(Integer.parseInt(
                        AddServicesUtil.getGreatestChildVersion(config, session,
                                RegistryUtils.getParentPath(path))) + 1);
            } catch (Exception ignore) {
            }
            path += "/service";
        } else if (type.equals("minor")) {
            try {
                String last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    nextMinor = Integer.toString(Integer.parseInt(
                            AddServicesUtil.getGreatestChildVersion(config, session,
                                    RegistryUtils.getParentPath(path))) + 1);
                    nextPatch = Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last + "/service";
            } catch (Exception e) {
                return;
            }
        } else if (type.equals("major")) {
            try {
                String last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    nextMajor = Integer.toString(Integer.parseInt(
                            AddServicesUtil.getGreatestChildVersion(config, session,
                                    RegistryUtils.getParentPath(path))) + 1);
                    nextMinor = Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last;
                last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    nextPatch = Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last + "/service";
            } catch (Exception e) {
                return;
            }
        } else if (type.equals("collection")) {
            try {
                String last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last;
                last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last;
                last = AddServicesUtil.getGreatestChildVersion(config, session, path);
                if (last.equals("")) {
                    return;
                }
                try {
                    Integer.toString(Integer.parseInt(last) + 1);
                } catch (Exception ignore) {
                }
                path += "/" + last + "/service";
            } catch (Exception e) {
                return;
            }
        }
    }
    String relativePath = path;
    if (relativePath.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) {
        relativePath =
                relativePath.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
    }
    boolean isVersionedPage = false;
    String disabledAttr = "";
    String saveButtonClass = "button registryWriteOperation";
    if (relativePath.contains(";version")) {
        isVersionedPage = true;
        disabledAttr = " disabled=\"1\"";
        saveButtonClass = "button registryNonWriteOperation";
    }

    String content = client.editService(relativePath);
    OMElement data = AddServicesUtil.loadAddedServiceContent(content);
    String version = CommonUtil.getServiceVersion(data);
    AddServiceUIGenerator gen = new AddServiceUIGenerator();
    OMElement uiconfig = gen.getUIConfiguration(client.getServiceConfiguration(),request,config,session);
    request.setAttribute("content",data);
    Iterator widgets = uiconfig.getChildrenWithName(new QName(null, UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuffer table = new StringBuffer();
    while(widgets.hasNext()){
        OMElement widget = (OMElement)widgets.next();
        String widgetText = gen.printWidgetWithValues(widget, data, false, true, true, request, config).replace("\n", "<!--LF-->").replace("\r", "<!--CR-->");
        if (type != null && widgetText.matches(".*<input[^>]*id=\"id_Overview_Version\"[^>]*>.*")) {
            String screenWidth = request.getParameter("screenWidth");
            String serviceLink = "&nbsp;(<a href=\"../resources/resource.jsp?region=region3&item=" +
                    "resource_browser_menu&path=" + path.replaceAll("&","%26") +
                    (screenWidth != null ? "&screenWidth=" + screenWidth : "") + "\">" +
                    CarbonUIUtil.geti18nString(
                                    "goto.service",
                                    "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                    request.getLocale()) + "</a>)";
            widgetText = AddServicesUtil.decorateVersionElement(version, widgetText, path, type,
                    serviceLink, screenWidth, config, session, request);
        }
        table.append(widgetText.replace("<!--LF-->", "\n").replace("<!--CR-->", "\r"));
    }
    String[] mandatory = gen.getMandatoryIdList(uiconfig);
    String[] name = gen.getMandatoryNameList(uiconfig);
    String servicename = AddServicesUtil.getNameFromContent(data);
    String servicenamespace = AddServicesUtil.getNamespaceFromContent(data);
    String[] addname = gen.getUnboundedNameList(uiconfig);
    String[] addwidget = gen.getUnboundedWidgetList(uiconfig);
    String[][] selectvaluelist = gen.getUnboundedValues(uiconfig, request, config);
%>

<br/>
<script type="text/javascript">

    function validateIllegalNamespace(fld){
        var error = "";
        //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
        var illegalChars = /([`()?\[\]~!@#;%^*+={}\|\\<>\"\',])/; // disallow ~!@#$;%^*+={}|\<>"',
        var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
        if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
            error = "The namespace field contains one or more illegal characters [_&`()?-\[\]~!@#;%^*+={}\|\\<>\"\',]" + "<br />";
        } else{
            fld.style.background = 'White';
        }

        return error;
    }

    function addEditService(increment){

        set_id_Overview_Description();
        set_id_Security_Comments();

        sessionAwareFunction(function() {
            getServiceName();
            var reason = "";
        <%for(int i=0;i<mandatory.length;i++){%>
            reason += validateEmpty(document.getElementById('<%=mandatory[i]%>'),
                        "<%=name[i]%>");
        <%}%>
            var CustomUIForm=document.getElementById('CustomUIForm');
            var waitMessage = document.getElementById('waitMessage');
            var buttonRow = document.getElementById('buttonRow');
            var serviceName = document.getElementById('id_Overview_Name');
            var namespaceValue = document.getElementById('id_Overview_Namespace');

            document.getElementsByName('newname')[0].value = serviceName.value;
            document.getElementsByName('newnamespace')[0].value = namespaceValue.value;

            reason += validateIllegalQName(serviceName, 'Service Name');
            if(reason!=""){
                CARBON.showWarningDialog(reason);
                return;
            }
            reason += validateIllegalQName(namespaceValue, 'Namespace');

            var versionString = (document.getElementById('id_Overview_Version')).value;
            if(versionString.length > 0){
                //                var regexp = new RegExp("^[1-9]\\d*[.]\\d+[.]\\d+$","i");
                var regexp = new RegExp("<%=CommonConstants.SERVICE_VERSION_REGEX.replace("\\","\\\\") %>","i");
                if(!versionString.match(regexp)){
                    CARBON.showWarningDialog(org_wso2_carbon_governance_services_ui_jsi18n["version.error.1"]
                            +" "+ versionString +" " +org_wso2_carbon_governance_services_ui_jsi18n["version.error.2"]);
                    return;
                }
            }
            if(reason!=""){
                CARBON.showWarningDialog(reason);
            }else{
                buttonRow.style.display = "none";
                waitMessage.style.display = "";
                if (increment > -1) {
                    var serviceVersion = document.getElementById('id_Overview_Version');
                    var serviceVersions = new Array();
                    serviceVersions = serviceVersion.value.split('.');
                    if (increment == 0 && "" != "<%=nextMajor%>") {
                        serviceVersions[increment] = "<%=nextMajor%>";
                    } else if (increment == 1 && "" != "<%=nextMinor%>") {
                        serviceVersions[increment] = "<%=nextMinor%>";
                    } else if (increment == 2 && "" != "<%=nextPatch%>") {
                        serviceVersions[increment] = "<%=nextPatch%>";
                    } else {
                        serviceVersions[increment] = (parseInt(serviceVersions[increment]) +  1);
                    }
                    if (increment < 2) {
                        serviceVersions[2] = 0;
                    }
                    if (increment < 1) {
                        serviceVersions[1] = 0;
                    }
                    serviceVersion.value = serviceVersions[0] + "." + serviceVersions[1] + "." + serviceVersions[2];
                }
                CustomUIForm.submit();
            }
        }, org_wso2_carbon_governance_services_ui_jsi18n["session.timed.out"]);
    }
    function getServiceName(){
        var serviceLoader= document.getElementById('serviceLoader');
        serviceLoader.innerHTML='<img src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message key="please.wait.saving.details.for"/> '+document.getElementById('id_Overview_Name').value+'...';
    }

    <%
       if(addname != null && addwidget != null && selectvaluelist != null){
       for(int i=0;i<addname.length;i++){%>
        <%=addname[i]%>Count = 0;
        jQuery(document).ready(function() {
            var countTracker = document.getElementById("<%=addname[i]%>CountTaker");
            if (countTracker != null && countTracker.value) {
                <%=addname[i]%>Count = parseInt(countTracker.value);
            }
        });
        function delete<%=addname[i]%>_<%=addwidget[i]%>(index) {
            var endpointMgt = document.getElementById('<%=addname[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            endpointMgt.parentNode.deleteRow(index);

            var table = endpointMgt.parentNode;
            var rows = table.getElementsByTagName("input");

            if (rows != null & rows.length == 0) {
                endpointMgt.parentNode.style.display = "none";
            }
        }
        function add<%=addname[i]%>_<%=addwidget[i]%>(inputParam){
        <%String[] valuelist = selectvaluelist[i];%>
            var epOptions = '<%for(int j=0;j<valuelist.length;j++){%><option value="<%=valuelist[j]%>"><%=valuelist[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=addname[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";

            var table = endpointMgt.parentNode;
            var rows = table.getElementsByTagName("input");

            if (rows.length > 0) {
                for (var i = 0; i < rows.length; i++) {
                    var endpoint = rows[i];
                    if (endpoint != null & endpoint.value == "") {
                        return;
                    }
                }
            }
            <%=addname[i]%>Count++;
            var epCountTaker = document.getElementById('<%=addname[i]%>CountTaker');
            epCountTaker.value = <%=addname[i]%>Count;
            var theTr = document.createElement("TR");
            var theTd1 = document.createElement("TD");
            var theTd2 = document.createElement("TD");
            var theTd3 = document.createElement("TD");
            var td1Inner = '<select name="<%=(addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-"))%>'+<%=addname[i]%>Count+'">' + epOptions + '</select>';
            var selectResource = "";
            if (inputParam == "path") {
                selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTree(\'id_<%=addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'\');"/>';
            }
            var td2Inner = '<input id="id_<%=addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'" type="text" name="<%=addwidget[i].replaceAll(" ","-") + UIGeneratorConstants.TEXT_FIELD + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'" style="width:400px"/>' + selectResource;
            var td3Inner = '<a class="icon-link" title="delete" onclick="delete<%=addname[i]%>_<%=addwidget[i]%>(this.parentNode.parentNode.rowIndex)" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

            theTd1.innerHTML = td1Inner;
            theTd2.innerHTML = td2Inner;
            <%--Setting the default width to fix alignment problems--%>
            theTd2.width="500px";

            theTd3.innerHTML = td3Inner;

            theTr.appendChild(theTd1);
            theTr.appendChild(theTd2);
            theTr.appendChild(theTd3);

            endpointMgt.appendChild(theTr);


        }
<%      }

   }%>

</script>

<div id="middle">


    <div id="workArea">

        <div id="activityReason" style="display: none;"></div>
        <form id="CustomUIForm" action="../services/add_service_ajaxprocessor.jsp" method="post">
            <input type="hidden" name="operation" value="Edit">
            <input type="hidden" name="currentname" value="<%=servicename%>">
            <input type="hidden" name="currentnamespace" value="<%=servicenamespace%>">
            <input type="hidden" name="newname" value="">
            <input type="hidden" name="newnamespace" value="">
            <input type="hidden" name="path" value="<%=path%>">
            <table class="styledLeft">
                <tr><td>
                    <%=table.toString()%>
                </td></tr>
                <% if(client.canChange(path)){%>
                <tr id="buttonRow">
                    <td colspan="3" class="buttonRow">
                        <%
                            if ((type == null || type.equals("collection"))) {
                                if(!isBrowseOnly){
                        %>
                        <input class="<%=saveButtonClass%>" type="button" <%=disabledAttr%>
                               value="<fmt:message key="save.service"/>" onclick="addEditService(-1)"/>
                        <%
                                }
                            } else {
                        %>
                        <b><fmt:message key="save.service.as"/>:&nbsp;</b>
                        <select id="saveServiceAsList" <%=disabledAttr%> >
                            <option value="-1"><fmt:message key="existing.version"/></option>

                        <%
                                int i = -1;
                                String titlePatch = "";
                                String titleMinor = "";
                                String titleMajor = "";
                                if (type.equals("patch")) {
                                    i = 2;
                                } else if (type.equals("minor")) {
                                    i = 1;
                                } else if (type.equals("major")) {
                                    i = 0;
                                }
                                switch (i) {
                                    case 0:
                                        if (!version.equals("")) {
                                            String[] versions = version.split("[.]");
                                            if (nextMajor.equals("")) {
                                                try {
                                                    nextMajor =
                                                            Integer.toString(
                                                                    Integer.parseInt(versions[0]) + 1);
                                                } catch (NumberFormatException ignore) {
                                                }
                                            }
                                            if (!nextMajor.equals("")) {
                                                titleMajor =
                                                        "title=\"" +
                                                                CarbonUIUtil.geti18nString(
                                                                        "create.new.version",
                                                                        "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                                                        request.getLocale()) + ": " +
                                                                nextMajor + ".0.0\"";
                                            }
                                        }
                        %>
                            <option value="0" <%=titleMajor%>><fmt:message key="new.major.version"/></option>
                        <%
                                    case 1:
                                        if (!version.equals("")) {
                                            String[] versions = version.split("[.]");
                                            if (nextMinor.equals("")) {
                                                try {
                                                    nextMinor =
                                                            Integer.toString(
                                                                    Integer.parseInt(versions[1]) + 1);
                                                } catch (NumberFormatException ignore) {
                                                }
                                            }
                                            if (!nextMinor.equals("")) {
                                                titleMinor =
                                                        "title=\"" +
                                                                CarbonUIUtil.geti18nString(
                                                                        "create.new.version",
                                                                        "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                                                        request.getLocale()) + ": " +
                                                                versions[0] + "." + nextMinor + ".0\"";
                                            }
                                        }
                        %>
                            <option value="1" <%=titleMinor%>><fmt:message key="new.minor.version"/></option>
                        <%
                                    case 2:
                                        if (!version.equals("")) {
                                            String[] versions = version.split("[.]");
                                            if (nextPatch.equals("")) {
                                                try {
                                                    nextPatch =
                                                            Integer.toString(
                                                                    Integer.parseInt(versions[2]) + 1);
                                                } catch (NumberFormatException ignore) {
                                                }
                                            }
                                            if (!nextPatch.equals("")) {
                                                titlePatch =
                                                        "title=\"" +
                                                                CarbonUIUtil.geti18nString(
                                                                        "create.new.version",
                                                                        "org.wso2.carbon.governance.services.ui.i18n.Resources",
                                                                        request.getLocale()) + ": " +
                                                                versions[0] + "." + versions[1] + "." +
                                                                nextPatch + "\"";
                                            }
                                        }
                        %>
                            <option value="2" <%=titlePatch%>><fmt:message key="new.patch.version"/></option>
                        <%
                                        break;
                                }
                        %>
                        </select>&nbsp;
                        <input class="<%=saveButtonClass%>" type="button" <%=disabledAttr%>
                               value="<fmt:message key="save.service"/>"
                               onclick="addEditService(document.getElementById('saveServiceAsList').value)"/>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <tr id="waitMessage" style="display:none">
                    <td colspan="3">
                        <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;" id="serviceLoader">
                        </div>
                    </td>
                </tr>
            <%}%>
            </table>
        </form>
        <br/>

        <div id="AddService">
        </div>
    </div>
</div>
</fmt:bundle>