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
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.UIGeneratorConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page
        import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>

<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../services/js/collapsible_menu_util.js"></script>

<%
    String dataName = request.getParameter("dataName");
    if (dataName == null) {
        dataName = "metadata";
    }
    String dataNamespace = request.getParameter("dataNamespace");
    if (dataNamespace == null) {
        dataNamespace = UIGeneratorConstants.DATA_NAMESPACE;
    }
    String breadcrumb = request.getParameter("breadcrumb");
    if (breadcrumb == null) {
        breadcrumb = "Artifact";
    }
    AddServiceUIGenerator uigen = new AddServiceUIGenerator(dataName, dataNamespace);
    ManageGenericArtifactServiceClient
            client = new ManageGenericArtifactServiceClient(config,session);
    OMElement head = uigen.getUIConfiguration(client.getArtifactUIConfiguration(request.getParameter("key")),request,config,session);
    Iterator widgets = head.getChildrenWithName(new QName(null,UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuilder table = new StringBuilder();
    while (widgets.hasNext()) {
        OMElement widget = (OMElement) widgets.next();
        table.append(uigen.printWidgetWithValues(widget, null, false, false, true, request, config));
    }
    String[] mandatory = uigen.getMandatoryIdList(head);
    String[] name = uigen.getMandatoryNameList(head);
    String[] addName = uigen.getUnboundedNameList(head);
    String[] addWidget = uigen.getUnboundedWidgetList(head);
    String[][] selectValueList = uigen.getUnboundedValues(head, request, config);
%>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
<carbon:breadcrumb
            label="<%=breadcrumb%>"
            topPage="true"
            request="<%=request%>" />
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
    <br/>

<script type="text/javascript">

    jQuery(document).ready(
            function() {
                initCollapseMenu("#_addTable");
            }
    );
</script>

    <script type="text/javascript">
        <% if (request.getParameter("wsdlError") != null) { %>
            CARBON.showErrorDialog(decodeURIComponent("<%=request.getParameter("wsdlError")%>"));
        <% } %>
        function addEditArtifact(){
        sessionAwareFunction(function() {
            var versionElement = document.getElementById('id_Overview_Version');
            if (versionElement) {
                var versionString = versionElement.value;
                if(versionString.length > 0){
    //                var regexp = new RegExp("^[1-9]\\d*[.]\\d+[.]\\d+$","i");
                    var regexp = new RegExp("<%=CommonConstants.SERVICE_VERSION_REGEX.replace("\\","\\\\") %>","i");
                    if(!versionString.match(regexp)){
                        CARBON.showWarningDialog(org_wso2_carbon_governance_generic_ui_jsi18n["version.error.1"]
                                +" "+ versionString +" " +org_wso2_carbon_governance_generic_ui_jsi18n["version.error.2"]);
                        return;
                    }
                }
            }
                getArtifactName();
                var reason = "";
            <%for(int i=0;i<mandatory.length;i++){%>
                reason += validateEmpty(document.getElementById('<%=mandatory[i]%>'),
                        "<%=name[i]%>");
            <%}%>
                var CustomUIForm=document.getElementById('CustomUIForm');
                var waitMessage = document.getElementById('waitMessage');
                var buttonRow = document.getElementById('buttonRow');
                if(reason!=""){
                    CARBON.showWarningDialog(reason);
                    return;
                }else{
                    buttonRow.style.display = "none";
                    waitMessage.style.display = "";
                    CustomUIForm.submit();
                }
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function getArtifactName(){
            var artifactLoader= document.getElementById('artifactLoader');

            artifactLoader.innerHTML='<img src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message key="please.wait.saving.details.for"/> '+'<%=breadcrumb%>'+'...';
        }

        function clearAll() {
            var table = $('#_addTable');
            var Inputrows = table.getElementsByTagName('input');

            for (var i = 0; i < Inputrows.length; i++) {
                if (Inputrows[i].type == "text") {
                    Inputrows[i].value = "";
                } else if (Inputrows[i].type == "checkbox") {
                    Inputrows[i].checked = false;
                }
            }

            var TextAreas = table.getElementsByTagName('textarea');
            for (var i = 0; i < TextAreas.length; i++) {
                TextAreas[i].value = "";
            }
            var SelectAreas = table.getElementsByTagName('select');
            for (var i = 0; i < SelectAreas.length; i++) {
                SelectAreas[i].selectedIndex = 0;
            }
        }


       <%
        if (addName != null && addWidget != null && selectValueList != null) {
            for (int i = 0; i < addName.length; i++) {
       %>
        <%=addName[i]%>Count = 0;
        jQuery(document).ready(function() {
            for (var i = 0; i < 0; i++) {
                add<%=addName[i]%>_<%=addWidget[i]%>("");
            }
        });
        function delete<%=addName[i]%>_<%=addWidget[i]%>(index) {
            var endpointMgt = document.getElementById('<%=addName[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            endpointMgt.parentNode.deleteRow(index);

            var table = endpointMgt.parentNode;
            var rows = table.getElementsByTagName("input");

            if (rows != null & rows.length == 0) {
                endpointMgt.parentNode.style.display = "none";
            }
        }
        function add<%=addName[i]%>_<%=addWidget[i]%>(inputParam) {
        <%String[] valuelist = selectValueList[i];%>
            var epOptions = '<%for(int j=0;j<valuelist.length;j++){%><option value="<%=valuelist[j]%>"><%=valuelist[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=addName[i]%>Mgt');
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
            <%=addName[i]%>Count++;
            var epCountTaker = document.getElementById('<%=addName[i]%>CountTaker');
            epCountTaker.value = <%=addName[i]%>Count;
            var theTr = document.createElement("TR");
            var theTd1 = document.createElement("TD");
            var theTd2 = document.createElement("TD");
            var theTd3 = document.createElement("TD");
            var td1Inner = '<select name="<%=(addWidget[i].replaceAll(" ","_") + "_" + addName[i].replaceAll(" ","-"))%>' + <%=addName[i]%>Count + '">' + epOptions + '</select>';
            var selectResource = "";
            if (inputParam == "path") {
                selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTree(\'id_<%=addWidget[i].replaceAll(" ","_") + "_" + addName[i].replaceAll(" ","-")%>' + <%=addName[i]%>Count + '\');"/>';
            }
            var td2Inner = '<input id="id_<%=addWidget[i].replaceAll(" ","_") + "_" + addName[i].replaceAll(" ","-")%>' + <%=addName[i]%>Count + '" type="text" name="<%=addWidget[i].replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD + "_" + addName[i].replaceAll(" ","-")%>' + <%=addName[i]%>Count + '" style="width:400px"/>' + selectResource;
            var td3Inner = '<a class="icon-link" title="delete" onclick="delete<%=addName[i]%>_<%=addWidget[i]%>(this.parentNode.parentNode.rowIndex)" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

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
        <%
              }

        }
        %>

    </script>
    <%
        if(table != null){
    %>

    <div id="middle">

        <h2><fmt:message key="add.artifact"><fmt:param value="<%=breadcrumb%>"/></fmt:message></h2>

        <div id="workArea">

            <div id="activityReason" style="display: none;"></div>
            <form id="CustomUIForm" action="../generic/add_ajaxprocessor.jsp" method="post">
                <input type="hidden" name="add_edit_operation" value="add"/>
                <input type="hidden" name="dataName" value="<%=dataName%>"/>
                <input type="hidden" name="dataNamespace" value="<%=dataNamespace%>"/>
                <input type="hidden" name="breadcrumb" value="<%=breadcrumb%>%>"/>
                <input type="hidden" name="region" value="<%=request.getParameter("region")%>"/>
                <input type="hidden" name="item" value="<%=request.getParameter("item")%>"/>
                <input type="hidden" name="key" value="<%=request.getParameter("key")%>"/>
                <input type="hidden" name="lifecycleAttribute" value="<%=request.getParameter("lifecycleAttribute")%>"/>
                <table class="styledLeft" id="#_addTable">
                    <tr><td>
                        <%=table.toString()%>
                    </td></tr>
                    <tr id="buttonRow">
                        <td class="buttonRow">
                            <input class="button registryWriteOperation" type="button"
                                   value="<fmt:message key="save"/>" onclick="addEditArtifact()" />
                            <input class="button registryNonWriteOperation" type="button"
                                   value="<fmt:message key="save"/>" disabled="disabled" />
                            <input type="button" id="#_1" value="<fmt:message key="clear"/>" class="button"
                   onclick="clearAll()"/>
                        </td>
                    </tr>
                    <tr id="waitMessage" style="display:none">
                        <td>
                            <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important" id="artifactLoader" class="ajax-loading-message">
                            </div>
                        </td>
                    </tr>

                </table>
            </form>
            <br/>

            <div id="AddArtifact">
            </div>
        </div>
    </div>
    <%}%>
</fmt:bundle>