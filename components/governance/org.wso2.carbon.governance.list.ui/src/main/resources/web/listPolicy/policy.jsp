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
<%@ page import="org.wso2.carbon.governance.list.ui.clients.ListMetadataServiceClient" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="java.net.URLEncoder" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../list/list-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../list/js/list.js"></script>
<%

    PolicyBean bean;
    try {
        ListMetadataServiceClient listservice = new ListMetadataServiceClient(config, session);
        bean = listservice.listpolicies();
    } catch (Exception e) {

%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../admin/index.jsp";
          return;
      });

</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.governance.list.ui.i18n.Resources">
<carbon:breadcrumb
            label="list.policies.menu.text"
            resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<br/>
<div id="middle">
    <h2><fmt:message key="policy.list"/></h2>
    <div id="workArea">
    <form id="profilesEditForm">
    <table class="styledLeft" id="customTable">
                                 <%if(bean.getSize()==0){%>
                <thead>
                    <tr>
                        <th><fmt:message key="no.policies"/></th>
                    </tr>
                </thead>
        <%} else{
            int pageNumber;
            String pageStr = request.getParameter("page");
            if (pageStr != null) {
                pageNumber = Integer.parseInt(pageStr);
            } else {
                pageNumber = 1;
            }
            int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);
            int numberOfPages;
            if (bean.getName().length % itemsPerPage == 0) {
                numberOfPages = bean.getName().length / itemsPerPage;
            } else {
                numberOfPages = bean.getName().length / itemsPerPage + 1;
            }
        %>
            <thead>
            <tr>
                    <th><fmt:message key="policy.name"/></th>
                    <th><fmt:message key="version"/></th>
                    <th><fmt:message key="policy.LC.info"/></th>
                    <% if (CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/browse")) {%><th><fmt:message key="actions"/></th><%} %>
                </tr>
            </thead>
            <tbody>
                    <%
              for(int i=(pageNumber - 1) * itemsPerPage;i<pageNumber * itemsPerPage && i<bean.getName().length;i++) {
                  String tempPath = bean.getPath()[i];
                  String completePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                  try {
                      tempPath = URLEncoder.encode(tempPath, "UTF-8");
                  } catch (Exception ignore) {}
                  String urlCompletePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                 %>
                <tr>
                    <%
                        String policyName = bean.getName()[i];
                        String LCState = "";
                        if(!bean.getLCName()[i].equals("")){
                            LCState = bean.getLCName()[i] + " / " + bean.getLCState()[i];
                        }
                        String version = "";
                        if (RegistryUtils.getResourceName(RegistryUtils.getParentPath(completePath)).replace(
                                "-SNAPSHOT", "").matches(CommonConstants.SERVICE_VERSION_REGEX)) {
                            version = RegistryUtils.getResourceName(RegistryUtils.getParentPath(completePath));
                        }
                        if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) { %>
                    <td><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=urlCompletePath%>"><%=policyName%></a></td>
                    <td><%=version%></td>
                    <td><%=LCState%></td>
                    <td>
                        <%if (bean.getCanDelete()[i])  { %>
                            <a title="<fmt:message key="delete"/>" onclick="deleteService('<%=completePath%>','/','../listPolicy/policy.jsp?region=region3&item=governance_list_policy_menu')" href="#" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                         <%} else { %>
                            <a class="icon-link registryWriteOperation" style="background-image:url(./images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message key="delete"/></a>
                         <%} %>
                    </td>
                    <% } else { %>
                    <td><%=policyName%></td>
                    <td><%=version%></td>
                    <td><%=LCState%></td>
                    <% } %>
                </tr>

                    <%
             }
             %>
            </tbody>
        </table>
        <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
            <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                      resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
                                      nextKey="next" prevKey="prev"
                                      paginationFunction="loadPagedList({0}, false, 'listPolicy', 'policy', 'policy')" />
        <%}%>
        </table>
    </form>
    </div>
    </div>
        <script type="text/javascript">
        alternateTableRows('customTable','tableEvenRow','tableOddRow');
</script>
</fmt:bundle>    
