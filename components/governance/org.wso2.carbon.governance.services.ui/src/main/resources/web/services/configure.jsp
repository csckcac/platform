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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/tinyxmlsax.js"></script>
<script type="text/javascript" src="js/tinyxmlw3cdom.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.services.ui.i18n.Resources"
		request="<%=request%>" namespace="org.wso2.carbon.governance.services.ui"/>
<%
    String content = null;
    try{
        AddServicesServiceClient client = new AddServicesServiceClient(config,session);
        content = client.getServiceConfiguration();
    } catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.governance.services.ui.i18n.Resources">
<carbon:breadcrumb
        label="services.menu.text"
        resourceBundle="org.wso2.carbon.governance.services.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript">
    function SaveConfiguration() {
        sessionAwareFunction(function() {
            var CustomUIForm = document.getElementById('services.config.form');
            var rawconfig = editAreaLoader.getValue("payloadEditor");
            if (rawconfig.indexOf("?>") > -1) {
                rawconfig = rawconfig.substring(rawconfig.indexOf("?>") + 2);
            }
            try {
                var domParser = new DOMImplementation();
                currentconfigDoc = domParser.loadXML(rawconfig);
                $('payload').value = editAreaLoader.getValue("payloadEditor");
                CustomUIForm.submit();
            }
            catch(e) {
                reason = "<fmt:message key="message1"/> !";
                CARBON.showWarningDialog(reason);
            }
        }, "<fmt:message key="session.timed.out"/>");
    }

    function cancelSequence() {
        sessionAwareFunction(function() {
            document.location.href = "configure.jsp?region=region1&item=governance_services_config_menu";
        }, "<fmt:message key="session.timed.out"/>");
    }
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
            id : "payloadEditor"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
        });
    })

</script>
<div id="middle">
    <h2><fmt:message key="configure.services"/></h2>
    <div id="workArea">
        <form id="services.config.form" method="post" action="save_service_ui_ajaxprocessor.jsp">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                <tr>
                    <th>
                        <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="message"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <textarea id="payloadEditor" style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;" name="payloadEditor" rows="30" ><%=content%></textarea>
                        <textarea style="display:none" name="payload" id="payload"><%=content%></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input class="button registryWriteOperation" type="button" onclick="SaveConfiguration()" value="<fmt:message key="save"/>"/>
                        <input class="button registryNonWriteOperation" type="button" disabled="disabled" value="<fmt:message key="save"/>"/>
                        <input class="button" type="button" value="<fmt:message key="reset"/>" onclick="javascript: cancelSequence(); return false;"/>
                    </td>
                </tr>

                <%-- <tr>
                    <td><a href="../resources/resource.jsp?path=<%=RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service-schema"%>"><img align="top" alt="" src="../resources/images/icon-download.jpg"/><fmt:message key="download.schema"/></a></td>
                 </tr>--%>

                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
    
