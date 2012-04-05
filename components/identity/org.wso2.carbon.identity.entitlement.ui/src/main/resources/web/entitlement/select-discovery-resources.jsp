
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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyAttributeFinderUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.discovery.ui.client.DiscoveryAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails" %>
<%@ page import="org.wso2.carbon.discovery.stub.types.mgt.TargetServiceDetails" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String ruleId = (String)request.getParameter("ruleId");
    Map<String, List> serviceUriAndOperationListMap = new HashMap<String, List>();

    try{
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                        CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        DiscoveryAdminClient discoveryAdminClient = new DiscoveryAdminClient(
                configContext, backendServerURL, cookie, request.getLocale());

        Map<String, DiscoveryProxyDetails> discoveryProxyDetailsMap = discoveryAdminClient.getDiscoveryProxies();

        if(discoveryProxyDetailsMap != null) {
            for (Map.Entry<String, DiscoveryProxyDetails> discoveryProxyDetailsEntry :
                    discoveryProxyDetailsMap.entrySet()) {
                DiscoveryProxyDetails discoveryProxyDetails = discoveryProxyDetailsEntry.getValue();
                if(discoveryProxyDetails.getOnline()) {
                TargetServiceDetails[] targetServiceDetailsArray = discoveryAdminClient.
                        probeDiscoveryProxy(discoveryProxyDetails.getName(), null, null);
                    if(targetServiceDetailsArray != null) {
                        for(TargetServiceDetails targetServiceDetails : targetServiceDetailsArray){
                            List<String> operationList = null;
                            String[] serviceUris = targetServiceDetails.getAddresses();
                            if(serviceUris != null) {
                                for(String serviceUri : serviceUris) {
                                    if(serviceUri != null) {
                                        if(serviceUri.startsWith("http:")) {           // only pass http url because of certificate issue. Need to fix
                                            operationList = PolicyAttributeFinderUtil.
                                                    getOperationListFromServiceUri(serviceUri);
                                        }
                                        if(operationList != null && operationList.size() > 0){
                                            serviceUriAndOperationListMap.put(serviceUri, operationList);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } 
            }
        } 
        
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
    <script type="text/javascript">
           location.href = "../admin/error.jsp";
    </script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="select.discovery.resources"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>

<script type="text/javascript">

    function removeRow(link){
        link.parentNode.parentNode.parentNode.removeChild(link.parentNode.parentNode);
    }

    function submitForm() {
        document.discoveryResourceSelectionForm.action = 'create-basic-policy.jsp?ruleId=' + '<%=ruleId%>';
        document.discoveryResourceSelectionForm.submit();
    }

    function cancelForm() {
       location.href = 'create-basic-policy.jsp?ruleId=' + '<%=ruleId%>';
    }
    var noRows = true;
    function addNewRow(){

        var comboBox = document.getElementById("serviceUri");
        var serviceUri = comboBox[comboBox.selectedIndex].value;

        comboBox = document.getElementById("operationName");
        var operationName = comboBox[comboBox.selectedIndex].value;

        var resourceName  = serviceUri + '/' + operationName;

        var mainTable = document.getElementById('mainTable');
        if(noRows){
            mainTable.deleteRow(1);
            noRows = false;
        }
        var newTr = mainTable.insertRow(mainTable.rows.length);
        var cell1 = newTr.insertCell(0);
        var rowValue = mainTable.rows.length - 1;
        cell1.innerHTML = resourceName+ '<input type="hidden" readonly="readonly"   value="' + resourceName +'" name="resourceName'+ rowValue +'" id="resourceName'+ rowValue +'"/>' ;

        var cell2 = newTr.insertCell(1);
        cell2.innerHTML = '<a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a>' ;
    }

    function getOperationList() {
        var comboBox = document.getElementById("serviceUri");
        var serviceUri = comboBox[comboBox.selectedIndex].value;
        location.href = 'select-discovery-resources.jsp?serviceUri=' + serviceUri + '&ruleId=' + '<%=ruleId%>';
    }

    YAHOO.util.Event.onDOMReady(
        function(){
            var operationNameElement = document.getElementById('operationName');
            operationNameElement.innerHTML = operationList[0].list;
        }
    );

    function selectRightList(serviceUri){
        var rightList = "";
        for(var i=0;i<operationList.length;i++){
            if(operationList[i].key == serviceUri){
                rightList = operationList[i].list;
            }
        }
        var operationNameElement = document.getElementById('operationName');
        operationNameElement.innerHTML = rightList; 
    }

    function configureWSDiscovery(){
        window.location.href = "../discovery/index.jsp";
    }

    var operationList = new Array();
    
</script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" />
    <div id="middle">
        <h2><fmt:message key='select.discovery.resources'/></h2>
            <form id="discoveryResourceSelectionForm" name="discoveryResourceSelectionForm" method="post" action="">
            <div id="workArea">
            <div class="goToAdvance">
            <a class='icon-link' href="../discovery/index.jsp?"
                       style='background-image:url(images/wsdiscovery.gif);float:none'><fmt:message key="configure.wsdiscovery"/></a>
            </div><table class="styledLeft noBorders" style="width:100%">
                    <%
                        Set keySet = serviceUriAndOperationListMap.keySet();
                        int index = 0;
                        if (keySet != null && !keySet.isEmpty()) {
                    %>
                    <tr>
                        <td class="leftCol-med" style="vertical-align:top"><fmt:message
                                key="select.resources.discovery"/></td>

                        <td>
                            <select id="serviceUri" name="serviceUri"
                                    onchange="selectRightList(this.options[this.selectedIndex].value)">

                                <%
                                    for (Object key : keySet) {
                                        index++;
                                        String serviceName = key.toString();
                                %>

                                <option <% if(index == 1){ %>selected="selected"<% } %>
                                        value="<%=serviceName%>"><%=serviceName%>
                                </option>

                                <%
                                    }
                                %>
                            </select>


                            <select id="operationName" name="operationName">
                                <%
                                    keySet = serviceUriAndOperationListMap.keySet();
                                    for (Object key : keySet) {
                                        String serviceName = key.toString();
                                        List list = serviceUriAndOperationListMap.get(serviceName);
                                        if (list != null && list.size() > 0) {
                                            Iterator iterator2 = list.iterator();
                                            String tmpOptionList = "";
                                            while (iterator2.hasNext()) {
                                                String operationName = iterator2.next().toString();
                                                tmpOptionList += "<option value=\"" + operationName + "\" >" + operationName + "</option>";

                                %>

                                <script type="text/javascript">
                                    operationList.push({key:'<%=serviceName%>',list:'<%=tmpOptionList%>'});
                                </script>

                                <%
                                            }
                                        }
                                    }
                                %>

                            </select>

                            <a onclick="addNewRow();" style='background-image:url(images/add.gif);float:none' type="button"
                               class="icon-link"></a>
                        </td>

                    </tr>

                    <%
                        }
                    %>
                    <tr>
                        <td colspan="2" style="padding:0 !important;border:none !important;margin:0 !important">
                            <table id="mainTable" class="styledLeft" style="width: 100%">
                                <thead>
                                <tr>
                                    <th><fmt:message key="resource"/></th>
                                    <th><fmt:message key="action"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr class="noRuleBox">
                                    <td colspan="3">No selected services.<br/>

                                        <div style="clear:both"></div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2" >
                            <input type="button" value="<fmt:message key="finish"/>" onclick="submitForm();"
                                   class="button"/>
                            <input type="button" value="<fmt:message key="cancel" />" onclick="cancelForm();"
                                   class="button"/>
                        </td>
                    </tr>
                </table>
        </div>
        </form>
    </div>
</fmt:bundle>
