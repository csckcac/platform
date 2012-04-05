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
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean" %>
<%@ page import="org.wso2.carbon.governance.list.ui.clients.ListMetadataServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../list/list-i18n-ajaxprocessor.jsp"/>

<carbon:breadcrumb
            label="list.services.menu.text"
            resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<script type="text/javascript" src="js/list.js"></script>
<%
    String criteria = null;
    if (request.getParameter("filter") != null) {
        criteria = (String)session.getAttribute("criteria");
    }
    ServiceBean bean = null;
    try {
        ListMetadataServiceClient listservice = new ListMetadataServiceClient(config, session);
        bean = listservice.listservices(criteria);
    } catch (Exception e) {
        if (request.getParameter("filter") != null) {
%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../list/service.jsp?region=region3&item=governance_list_services_menu";
          return;
      });

</script>
<%
        } else {
%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../admin/index.jsp";
          return;
      });

</script>
<%
        }
        return;
    }
%>

<script type="text/javascript">
    function submitFilterByNameForm() {

        sessionAwareFunction(function() {
            var advancedSearchForm = $('filterByNameForm');
            advancedSearchForm.submit();
        }, org_wso2_carbon_governance_list_ui_jsi18n["session.timed.out"]);
    }
</script>

<fmt:bundle basename="org.wso2.carbon.governance.list.ui.i18n.Resources">
<br/>
<div id="middle">
<h2><fmt:message key="service.list"/></h2>
<div id="workArea">
 <%if(bean.getSize() != 0 || request.getParameter("filter") != null){%>
    <p style="padding:5px">
    <form id="filterByNameForm" action="service_name_filter_ajaxprocessor.jsp"
          onsubmit="return submitFilterByNameForm();" method="post">

        <table id="#_innerTable" style="width:100%">
            <tr id="buttonRow">
                <td nowrap="nowrap" style="line-height:25px;padding-right:10px;width:150px;">Filter by Service Name:</td>
                <td nowrap="nowrap" style="width:200px">
                    <input id="id_Service_Name" onkeypress="if (event.keyCode == 13) {submitFilterByNameForm(); }"
                           type="text" name="Service_Name" style="width:200px;margin-bottom:10px;">
                </td>
                <td>
                    <table style="*width:430px !important;">
        		<tbody>
                          <tr>
        		    <td>
       				<a class="icon-link" href="#" style="background-image: url(../component-mgt/images/search.gif);" onclick="submitFilterByNameForm(); return false;" alt="Search"></a>
        		   </td>
        		   <td style="vertical-align:middle;padding-left:10px;padding-right:5px;">|</td>
                           <td>
            			<a href="../list/service_filter.jsp" class="icon-link" style="background-image:url(../search/images/search-top.png);"><fmt:message key="filter.service.message"/></a>
        		  </td>
                         </tr>
                       </tbody>
                    </table>
                </td>
            </tr>
            <tr id="waitMessage" style="display:none">
                <td>
                    <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important"
                         id="serviceLoader" class="ajax-loading-message">
                    </div>
                </td>
            </tr>

        </table>
    </form>
    </p>
    <br>
 <%
     }
 %>
<form id="profilesEditForm">
<table class="styledLeft" id="customTable">
           <%if(bean.getSize()==0){%>
                <thead>
                    <tr>
                        <%
                        if (request.getParameter("filter") != null) {
                        %>
                        <th><fmt:message key="no.services.matches.filter"/></th>
                        <% } else { %>
                        <th><fmt:message key="no.services"/></th>
                        <% } %>
                    </tr>
                </thead>
        <%}
        else{%>
        <thead>
        <tr>
                <th><fmt:message key="service.name"/></th>
                <th><fmt:message key="service.version"/></th>
                <th><fmt:message key="service.namespace"/></th>
                <% if (CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/browse")) {%><th><fmt:message key="actions"/></th><%} %>
            </tr>
        </thead>
        <tbody>
                <%
          for(int i=0;i<bean.getNames().length;i++) {
              String tempPath = bean.getPath()[i];
                  String completePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
              try {
                  tempPath = tempPath.replace("&", "%26");
              } catch (Exception ignore) {}
                  String urlCompletePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
              String urlCompletePathNoVersion = RegistryUtils.getParentPath(
                      RegistryUtils.getParentPath(urlCompletePath));
              String version = RegistryUtils.getResourceName(RegistryUtils.getParentPath(tempPath));
              if (!version.matches(CommonConstants.SERVICE_VERSION_REGEX)) {
                  urlCompletePathNoVersion = RegistryUtils.getParentPath(urlCompletePath);
              } else {
                  urlCompletePathNoVersion = RegistryUtils.getParentPath(
                      RegistryUtils.getParentPath(urlCompletePath));
              }
              if(tempPath.startsWith(bean.getDefaultServicePath())){
                  completePath = completePath.substring(0, completePath.indexOf(version) - 1);
                  urlCompletePath = urlCompletePath.substring(0, urlCompletePath.indexOf(version) - 1);

                  if (urlCompletePathNoVersion.contains(version)) {
                      urlCompletePathNoVersion = urlCompletePathNoVersion.substring(0, urlCompletePathNoVersion.indexOf(version) - 1);
                  }
              }
                %>
            <tr>
                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) { %>
                <td><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=urlCompletePathNoVersion%>"><%=bean.getNames()[i]%></a></td>
                <td><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=urlCompletePath%>"><%=version%></a></td>
                <td><%=bean.getNamespace()[i]%></td>
                <td>
                    <%if (bean.getCanDelete()[i])  { %>
                        <a title="<fmt:message key="delete"/>" onclick="deleteService('<%=completePath%>','/','../list/service.jsp?region=region3&item=governance_list_services_menu')" href="#" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                    <%} else { %>
                        <a class="icon-link registryWriteOperation" style="background-image:url(./images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message key="delete"/></a>
                    <%} %>
            </td>
		<% } else { %>
                <td><%=bean.getNames()[i]%></td>
                <td><%=version%></td>
                <td><%=bean.getNamespace()[i]%></td>
                <% } %>
            </tr>

                <%
         }
         %>
        </tbody>
    <%}%>
    </table>
</form>
</div>    
</div>
    <script type="text/javascript">
    alternateTableRows('customTable','tableEvenRow','tableOddRow');
</script>
</fmt:bundle>
