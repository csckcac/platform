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
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.UIGeneratorConstants" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page
        import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactsBean" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactBean" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../list/list-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../generic/js/genericpagi.js"/>

<carbon:breadcrumb
            label="<%=request.getParameter("breadcrumb")%>"
            topPage="true"
            request="<%=request%>" />
<script type="text/javascript" src="../list/js/list.js"></script>
<%
    String key = request.getParameter("key");
    String breadcrumb = request.getParameter("breadcrumb");
    String queryTrailer = "&key=" + key + "&breadcrumb=" + breadcrumb;
    String dataName = request.getParameter("dataName");
    if (dataName == null) {
        dataName = "metadata";
    } else {
        queryTrailer += "&dataName=" + dataName;
    }
    String dataNamespace = request.getParameter("dataNamespace");
    if (dataNamespace == null) {
        dataNamespace = UIGeneratorConstants.DATA_NAMESPACE;
    } else {
        queryTrailer += "&dataNamespace=" + dataNamespace;
    }
    String singularLabel = request.getParameter("singularLabel");
    if (singularLabel == null) {
        singularLabel = "Artifact";
    } else {
        queryTrailer += "&singularLabel=" + singularLabel;
    }
    String pluralLabel = request.getParameter("pluralLabel");
    if (pluralLabel == null) {
        pluralLabel = "Artifacts";
    } else {
        queryTrailer += "&pluralLabel=" + pluralLabel;
    }
    String criteria = null;
    boolean filter = request.getParameter("filter") != null;
    if (filter) {
        criteria = (String)session.getAttribute("criteria");
    }
    ArtifactsBean bean = null;
    String region = request.getParameter("region");
    String item = request.getParameter("item");
    try {
        ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
        bean = client.listArtifacts(key, criteria);
    } catch (Exception e) {
        if (filter) {
%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../generic/list.jsp?region=<%=region%>&item=<%=item%><%=queryTrailer%>";
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
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
<br/>
<div id="middle">
<h2><fmt:message key="artifact.list"><fmt:param value="<%=singularLabel%>"/><</fmt:message></h2>
<div id="workArea">
 <%if(bean.getArtifacts() != null && bean.getArtifacts().length != 0){%>
 <p style="padding:5px">
 <a href="../generic/filter.jsp?list_region=<%=region%>&list_item=<%=item%>&dataNamespace=<%=dataNamespace%>&dataName=<%=dataName%>&singularLabel=<%=singularLabel%>&pluralLabel=<%=pluralLabel%>&key=<%=key%>&list_breadcrumb=<%=breadcrumb%>"><fmt:message key="filter.artifact.message"><fmt:param value="<%=singularLabel.toLowerCase()%>"/></fmt:message></a>
 </p>
 <%}%>
<form id="profilesEditForm">
<table class="styledLeft" id="customTable">
           <%if(bean.getArtifacts() == null || bean.getArtifacts().length==0){%>
                <thead>
                    <tr>
                        <%
                        if (filter) {
                        %>
                        <th><fmt:message key="no.artifact.matches.filter"><fmt:param value="<%=singularLabel.toLowerCase()%>"/></fmt:message></th>
                        <% } else { %>
                        <th><fmt:message key="no.artifacts"><fmt:param value="<%=pluralLabel.toLowerCase()%>"/></fmt:message></th>
                        <% } %>
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
            if (bean.getArtifacts().length % itemsPerPage == 0) {
                numberOfPages = bean.getArtifacts().length / itemsPerPage;
            } else {
                numberOfPages = bean.getArtifacts().length / itemsPerPage + 1;
            }
        %>
        <thead>
        <tr>
                <%
                    for (String name : bean.getNames()) {
                %>
                <th><%=name%></th>
                <%
                    }

                    if (CarbonUIUtil.isUserAuthorized(request,
                        "/permission/admin/manage/resources/browse")) {%><th><fmt:message key="actions"/></th><%} %>
            </tr>
        </thead>
        <tbody>
                <%
                    for (int j = (pageNumber - 1) * itemsPerPage;
                         j < pageNumber * itemsPerPage && j < bean.getArtifacts().length; j++) {
                        ArtifactBean artifact = bean.getArtifacts()[j];

                %>
            <tr>
                <%
                    if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {
                        for (int i = 0; i < bean.getNames().length; i++) {
                            if (bean.getTypes()[i].equals("path")) {
                                %><td><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=artifact.getValuesB()[i].replace("&", "%26")%>"><%= artifact.getValuesA()[i] != null ? artifact.getValuesA()[i] : "" %></a></td><%
                            } else {
                                %><td><%= artifact.getValuesA()[i] != null ? artifact.getValuesA()[i] : "" %></td><%
                            }
                        }
                %>


                <td><% if (artifact.getCanDelete()) { %><a title="<fmt:message key="delete"/>" onclick="deleteService('<%=artifact.getPath()%>','/','../generic/list.jsp?region=<%=region%>&item=<%=item%><%=queryTrailer%>')" href="#" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a><% } %></td>
                <%
                    } else {
                        for (int i = 0; i < bean.getNames().length; i++) {
                            %><td><%=artifact.getValuesA()[i]%></td><%
                        }
                    }
                %>
            </tr>

                <%
                }
                %>
        </tbody>
    </table>
    <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="loadPagedList({0})" />
    <%}%>
    </table>
</form>
</div>
</div>
    <script type="text/javascript">
    alternateTableRows('customTable','tableEvenRow','tableOddRow');

    function loadPagedList(page) {
        window.location = '<%="../generic/list.jsp?region=" + request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" + request.getParameter("dataName") + "&singularLabel=" + request.getParameter("singularLabel") + "&pluralLabel=" + request.getParameter("pluralLabel") + "&dataNamespace=" + request.getParameter("dataNamespace") + "&key=" + request.getParameter("key") + "&breadcrumb=" + request.getParameter("breadcrumb") + (filter ? "&filter=filter" : "")%>';
    }
</script>
</fmt:bundle>
