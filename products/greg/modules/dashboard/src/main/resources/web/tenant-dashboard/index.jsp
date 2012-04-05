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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.governance.list.ui.clients.ListMetadataServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean" %>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../relations/relations-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../relations/js/relations.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../lcm/js/lcm.js"></script>
<script type="text/javascript" src="../tenant-dashboard/js/dashboard.js"></script>
<link href="../tenant-dashboard/css/dashboard.css" rel="stylesheet" type="text/css" media="all"/>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.governance.list.ui.i18n.JSResources"
		request="<%=request%>" />
<%  boolean canAddMetadata = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/metadata/add");
    boolean canBrowseResources = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/browse");
    String serviceBeanError = null;
    String wsdlBeanError = null;
    String policyBeanError = null;
    String schemaBeanError = null;
    ListMetadataServiceClient listservice = new ListMetadataServiceClient(config, session);
    // getting services
    ServiceBean serviceBean = null;
    try {
        serviceBean = listservice.listservices(null);
    } catch (Exception e) {
        serviceBeanError = "You don't have sufficient privileges to view Services";
    }
    // getting wsdls
    WSDLBean wsdlBean = null;
    try {
        wsdlBean = listservice.listwsdls();
    } catch (Exception e) {
        wsdlBeanError = "You don't have sufficient privileges to view WSDLs";
    }
    // getting policies
    PolicyBean policyBean = null;
    try {
        policyBean = listservice.listpolicies();
    } catch (Exception e) {
        policyBeanError = "You don't have sufficient privileges to view Policies";
    }
    // getting schemas
    SchemaBean schemaBean = null;
    try {
        schemaBean = listservice.listschemas();
    } catch (Exception e) {
        schemaBeanError = "You don't have sufficient privileges to view Schemas";
    }

    int serviceBeanCount = 0;
    if (serviceBean != null) {
        serviceBeanCount = serviceBean.getSize();
    }

    int wsdlBeanCount = 0;
    if (wsdlBean != null) {
        wsdlBeanCount = wsdlBean.getSize();
    }

    int schemaBeanCount = 0;
    if (schemaBean != null) {
        schemaBeanCount = schemaBean.getSize();
    }

    int policyBeanCount = 0;
    if (policyBean != null) {
        policyBeanCount = policyBean.getSize();
    }

%>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
<h2 class="dashboard-title">WSO2 Governance Registry quick start dashboard</h2>
<div id="workArea">

<div id="wrapper">

<%
// We will always display the tips page... Temporarily disabled displaying the metadata boxes - Senaka.
if (/*serviceBeanCount == 0 &&
    wsdlBeanCount == 0 &&
    schemaBeanCount == 0 &&
    policyBeanCount == 0*/true) {
%>
<jsp:include page="../tenant-dashboard/tips.jsp"/>
<%
} else {
%>
    
<div class="dashboard-comp">
<fmt:bundle basename="org.wso2.carbon.governance.list.ui.i18n.Resources">
<div class="dashboard-comp-head">Services</div>
        <%
        if (serviceBeanError != null) { %>
            <div class="dashboard-comp-msg"><%=serviceBeanError%></div>
        <%} else {
            if(serviceBean.getSize()==0){%>
                    <div class="dashboard-comp-msg">There are No Services Added</div>
            <%}
            else{
            %>
            <div class="dashboard-comp-body">
            <table class="styledLeft" id="customTable1" style="margin-left:0px;">
            <tbody>
            <%
            for(int i=0;i<serviceBean.getNames().length;i++) {
                 %>
                    <tr><td><% if (canBrowseResources) { %><a href="../resources/resource.jsp?path=/_system/governance<%=serviceBean.getPath()[i]%>"><%}%><%=serviceBean.getNames()[i]%><% if (canBrowseResources) { %></a><%}%></td></tr>
                 <%
             }
             %>
             </tbody>
             </table>
                </div>
            <%}%>


        <div class="dashboard-comp-bottom">
            <% if (canAddMetadata) { %>
               <%if(serviceBean.getSize()==0){%>
                    <input type="button" class="button" onclick="javascript:window.location.href='../services/services.jsp?region=region3&item=governance_services_menu'" value="Add your first service"/>
               <%} else {%>
                    <input type="button" class="button" onclick="javascript:window.location.href='../services/services.jsp?region=region3&item=governance_services_menu'" value="Add a new service"/>
               <%}%>
            <%}%>

        </div>
        <%}%>
</fmt:bundle>
</div>



<div class="dashboard-comp">
    <div class="dashboard-comp-head">WSDLs</div>
        <%
            if (wsdlBeanError != null) { %>
                <div class="dashboard-comp-msg"><%=wsdlBeanError%></div>
            <% } else {
               if(wsdlBean.getSize()==0){%>
                <div class="dashboard-comp-msg">There are No WSDLs Added</div>
                <%}
                else{%>
                <div class="dashboard-comp-body">
            <table class="styledLeft" id="customTable2" style="margin-left:0px;">
                    <tbody>
                            <%
                      for(int i=0;i<wsdlBean.getName().length;i++) {

                         %>
                        <tr>
                            <td><% if (canBrowseResources) { %><a href="../resources/resource.jsp?path=/_system/governance<%=wsdlBean.getPath()[i]%>"><%}%><%=wsdlBean.getName()[i]%><% if (canBrowseResources) { %></a><%}%></td>
                            <td><a title="Dependency" onclick="showAssociationTree('depends','<%=wsdlBean.getPath()[i]%>')" href="#" class="icon-link" style="background-image:url(../relations/images/dep-tree.gif);"></a> </td>
                        </tr>

                            <%
                     }
                     %>
                    </tbody>
                    </table>
                    </div>
                <%}%>

            <div class="dashboard-comp-bottom">
                <% if (canAddMetadata) { %>
                   <%if(wsdlBean.getSize()==0){%>
                        <input type="button" class="button" onclick="javascript:window.location.href='../wsdl/wsdl.jsp?region=region3&item=governance_wsdl_menu'" value="Add your first wsdl"/>
                   <%} else {%>
                        <input type="button" class="button" onclick="javascript:window.location.href='../wsdl/wsdl.jsp?region=region3&item=governance_wsdl_menu'" value="Add a new wsdl"/>
                   <%}%>
                <%}%>

            </div>
        <%}%>
</div>


<div class="dashboard-comp">

    <form id="profilesEditForm">
    <div class="dashboard-comp-head">XML Schemas</div>
    <% if (schemaBeanError != null) {%>
                <div class="dashboard-comp-msg"><%=schemaBeanError%></div>
    <%} else {
                if(schemaBean.getSize()==0){%>
                <div class="dashboard-comp-msg">There are No Schemas Added</div>
                <%}
                else {%>
                <div class="dashboard-comp-body">
                    <table class="styledLeft" id="customTable4" style="margin-left:0px;">
                    <tbody>
                            <%
                      for(int i=0;i<schemaBean.getName().length;i++) {

                         %>
                        <tr>
                            <td><% if (canBrowseResources) { %><a href="../resources/resource.jsp?path=/_system/governance<%=schemaBean.getPath()[i]%>"><%}%><%=schemaBean.getName()[i]%><% if (canBrowseResources) { %></a><%}%></td>
                        </tr>

                     <%
                     }
                     %>
                    </tbody>
                    </table>
                    </div>
                <%}%>
            </form>

            <div class="dashboard-comp-bottom">
                <% if (canAddMetadata) { %>
                   <%if(schemaBean.getSize()==0){%>
                        <input type="button" class="button" onclick="javascript:window.location.href='../schema/schema.jsp?region=region3&item=governance_schema_menu'" value="Add your first schema"/>
                   <%} else {%>
                        <input type="button" class="button" onclick="javascript:window.location.href='../schema/schema.jsp?region=region3&item=governance_schema_menu'" value="Add a new schema"/>
                   <%}%>
                <%}%>

            </div>
      <%}%>
</div>


<div class="dashboard-comp">
    <div class="dashboard-comp-head">Policies</div>
        <%
        if (policyBeanError != null) {%>
            <div class="dashboard-comp-msg"><%=policyBeanError%></div>
      <% } else {
            if(policyBean.getSize()==0){%>
                    <div class="dashboard-comp-msg">There are No Policies Added</div>
            <%}
            else {%>
            <div class="dashboard-comp-body">
            <table class="styledLeft" id="customTable3" style="margin-left:0px;">
                <tbody>
                        <%
                  for(int i=0;i<policyBean.getName().length;i++) {

                     %>
                    <tr>
                        <td><% if (canBrowseResources) { %><a href="../resources/resource.jsp?path=/_system/governance<%=policyBean.getPath()[i]%>"><%}%><%=policyBean.getName()[i]%><% if (canBrowseResources) { %></a><%}%></td>
                    </tr>

                 <%
                 }
                 %>
                </tbody>
                </table>
                </div>
            <%}%>


        <div class="dashboard-comp-bottom">
            <% if (canAddMetadata) { %>
               <%if(policyBean.getSize()==0){%>
                    <input type="button" class="button" onclick="javascript:window.location.href='../policy/policy.jsp?region=region3&item=governance_policy_menu'" value="Add your first policy"/>
               <%} else {%>
                    <input type="button" class="button" onclick="javascript:window.location.href='../policy/policy.jsp?region=region3&item=governance_policy_menu'" value="Add a new policy"/>
               <%}%>
            <%}%>

        </div>
      <%}%>
</div>


<!--<div class="dashboard-comp">

</div>  -->
    <div class="dashboard-clear" style="clear:both; margin-top:5px"></div>
</div> <!-- wrapper div -->
</div>   <!--end of working area-->
<script type="text/javascript">
    alternateTableRows('customTable1','tableEvenRow','tableOddRow'); 
    alternateTableRows('customTable2','tableEvenRow','tableOddRow');
    alternateTableRows('customTable3','tableEvenRow','tableOddRow');
    alternateTableRows('customTable4','tableEvenRow','tableOddRow');
    alternateTableRows('customTable5','tableEvenRow','tableOddRow');

</script>
</div>
<%
}
%>
