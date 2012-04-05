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
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.AddServicesUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.utils.UIGeneratorConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.clients.AddServicesServiceClient" %>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<%
    String error = "Wrong configuration in " + RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH;
    AddServiceUIGenerator uigen = new AddServiceUIGenerator();
    AddServicesServiceClient client = new AddServicesServiceClient(config,session);
    OMElement head = uigen.getUIconfiguration(client.getServiceConfiguration());
    Iterator widgets = head.getChildrenWithName(new QName(null,UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuffer table = new StringBuffer();
    while(widgets.hasNext()){
        OMElement widget = (OMElement)widgets.next();
        table.append(uigen.printwidgetwithvalues(widget, null, true));
    }
    String[] mandatory = uigen.getmandatoryidlist(head);
    String[] name = uigen.getmandatorynamelist(head);
    String[] addname = uigen.getunboundednamelist(head);
    String[] addwidget = uigen.getunboundedwidgetlist(head);
    String[][] selectvaluelist = uigen.getunboundedvalues(head);
//    String configuration;
//    try {
//        configuration = AddServicesUtil.getServiceConfiguration(session,config);
//    } catch (Exception e) {
//        configuration = null;
//    }
//    if(configuration == null){
//        request.setAttribute(CarbonUIMessage.ID,new CarbonUIMessage(error,error,null));
%>
<%--<jsp:include page="../admin/error.jsp?<%=error%>"/>--%>
<%
    //        return;
//    }
//    String[] splittedconfig = configuration.split(":");
//    String[] endpoints = RegistryConstants.GOVERNANCE_ENDPOINTS.split(",");
%>
<fmt:bundle basename="org.wso2.carbon.gadget.editor.ui.i18n.Resources">
    <carbon:breadcrumb
            label="services.menu.text"
            resourceBundle="org.wso2.carbon.gadget.editor.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
    <br/>

    <script type="text/javascript">
        <% if (request.getParameter("wsdlError") != null) { %>
            CARBON.showErrorDialog("<fmt:message key="unable.to.import.wsdl"/>");    
        <% } %>
        function addEditService(){
            getServiceName();
            var reason = "";
        <%for(int i=0;i<mandatory.length;i++){%>
            var <%=mandatory[i]%>=document.getElementById('<%=mandatory[i]%>');
            reason += validateEmpty(<%=mandatory[i]%>,"<%=name[i]%>");
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
        }
        function getServiceName(){
            var serviceLoader= document.getElementById('serviceLoader');

            serviceLoader.innerHTML='<img src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message key="please.wait.saving.details.for"/> '+document.getElementById('id_Overview_Name').value+'...';
        }





        <%
       if(addname != null && addwidget != null && selectvaluelist != null){
       for(int i=0;i<addname.length;i++){%>
        <%=addname[i]%>Count =0;
        jQuery(document).ready(function() {
            for(var i=0;i<0;i++){
                add<%=addname[i]%>_<%=addwidget[i]%>();
            }
        });

        function add<%=addname[i]%>_<%=addwidget[i]%>(){
        <%String[] valuelist = selectvaluelist[i];%>
            var epOptions = '<%for(int j=0;j<valuelist.length;j++){%><option value="<%=j%>"><%=valuelist[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=addname[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            if(<%=addname[i]%>Count >0){
                for(var i=1;i<=<%=addname[i]%>Count;i++){
                    var endpoint = document.getElementById(i);
                    if(endpoint.value == ""){
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
            var td1Inner = '<select name="<%=(addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-"))%>'+<%=addname[i]%>Count+'">' + epOptions + '</select>';
            var td2Inner = '<input id="'+<%=addname[i]%>Count+'" type="text" name="<%=addwidget[i].replaceAll(" ","_") + UIGeneratorConstants.TEXT_FIELD + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'" style="width:400px"/>';

            theTd1.innerHTML = td1Inner;
            theTd2.innerHTML = td2Inner;

            theTr.appendChild(theTd1);
            theTr.appendChild(theTd2);

            endpointMgt.appendChild(theTr);


        }
<%      }

   }%>

    </script>
    <%
        if(table != null){
    %>

    <div id="middle">

        <h2><fmt:message key="service.operations"/></h2>

        <div id="workArea">

            <div id="activityReason" style="display: none;"></div>
            <form id="CustomUIForm" action="../services/add_service_ajaxprocessor.jsp" method="post">
                <input type="hidden" name="operation" value="Add"/>
                <input type="hidden" name="currentname" value="">
                <input type="hidden" name="currentnamespace" value="">
                <table class="styledLeft">
                    <tr><td>
                        <%=table.toString()%>
                    </td></tr>
                    <tr id="buttonRow">
                        <td class="buttonRow">
                            <input class="button registryWriteOperation" type="button"
                                   value="<fmt:message key="save"/>" onclick="addEditService()" />
                            <input class="button registryNonWriteOperation" type="button"
                                   value="<fmt:message key="save"/>" disabled="disabled" />
                        </td>
                    </tr>
                    <tr id="waitMessage" style="display:none">
                        <td>
                            <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important" id="serviceLoader" class="ajax-loading-message">
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
    <%}%>
</fmt:bundle>
