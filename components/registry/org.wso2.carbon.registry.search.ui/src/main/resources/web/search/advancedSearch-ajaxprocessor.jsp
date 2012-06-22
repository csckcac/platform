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

<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean" %>
<%@ page import="org.wso2.carbon.registry.search.stub.common.xsd.ResourceData" %>
<%@ page import="org.wso2.carbon.registry.search.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.search.ui.report.beans.MetaDataReportBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URLEncoder" %>


<%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        AdvancedSearchResultsBean advancedSearchBean;
        String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
        try {
            SearchServiceClient client = new SearchServiceClient(cookie, config, session);
            if (requestedPage != null && session.getAttribute("advancedSearchBean") != null) {
                advancedSearchBean = (AdvancedSearchResultsBean) session.getAttribute("advancedSearchBean");
            } else {
                advancedSearchBean = client.getAdvancedSearchResults(request);
                session.setAttribute("advancedSearchBean", advancedSearchBean);
            }
        } catch (Exception e) {
            response.setStatus(500);
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
            return;
        }
        ResourceData[] fullResourceDataList = advancedSearchBean.getResourceDataList();
        ResourceData[] resourceDataList = null;
        if (advancedSearchBean != null) {
            fullResourceDataList = advancedSearchBean.getResourceDataList();
        }
        int start = 0;
        int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);
        int pageNumber = 1;
        int numberOfPages = 1;

        if (fullResourceDataList != null && fullResourceDataList.length != 0) {

            if (requestedPage != null && requestedPage.length() > 0) {
                pageNumber = new Integer(requestedPage);
            }


            if (fullResourceDataList.length % itemsPerPage == 0) {
                numberOfPages = fullResourceDataList.length / itemsPerPage;
            } else {
                numberOfPages = fullResourceDataList.length / itemsPerPage + 1;
            }

            if (fullResourceDataList.length >= itemsPerPage) {
                start = (pageNumber - 1) * itemsPerPage;
            }
            resourceDataList = Utils.getChildren(start, itemsPerPage, fullResourceDataList);
        }


    boolean resourceExists = false;
    ResourceServiceClient client;
    try {
        client = new ResourceServiceClient(config, session);
        String loginUser = request.getSession().getAttribute("logged-user").toString();
        try {
            client.getResourceTreeEntry("/_system/config/users/" + loginUser + "/searchFilters");
        } catch (Exception ignored1) {
            try {
                client.getResourceTreeEntry("/_system/config/users/" + loginUser);
            } catch (Exception ignored2) {
                client.getResourceTreeEntry("/_system/config/users");
            }
        }
        resourceExists = true;
    } catch (Exception ignored) {

    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
    <%
        if (resourceDataList != null && resourceDataList.length > 0) {
    %>
    <carbon:report
            component="org.wso2.carbon.registry.search"
            template="MetaDataReportTemplate"
            pdfReport="true"
            htmlReport="true"
            excelReport="true"
            reportDataSession="metaDataSearchReport"
            />
    <%
        }
    %>
<%
        
        if (resourceDataList != null && resourceDataList.length != 0) {
    %>
    
    <h3 style="margin-top:20px;margin-bottom:20px;"> <fmt:message key="search.results"/> </h3>
    
    <table cellpadding="0" cellspacing="0" border="0" style="width:100%" class="styledLeft">
        <thead>
        <tr>
            <th style="padding-left:5px;text-align:left;">&nbsp;</th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="created"/></th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="author"/></th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="rating"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (int i = 0; i < resourceDataList.length; i++) {
                ResourceData resourceData = resourceDataList[i];

                if (resourceData == null) {
                    continue;
                }
                String tempPath = resourceData.getResourcePath();
                try {
                    tempPath = URLEncoder.encode(tempPath, "UTF-8");
                } catch (Exception ignore) {}
        %>
        <tr id="1">
             <% if (resourceData.getResourceType().equals("collection")) { %>
            <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                    src="images/icon-folder-small.gif" style="margin-right:5px;" align="top"/>
                    <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                    <a onclick="directToResource('../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=tempPath%>')"
                    href="#"><%=resourceData.getResourcePath()%></a>
                    <% } else { %>
                    <%=resourceData.getResourcePath()%>
                    <% } %>
            </td>
            <% } %>
            <% if (resourceData.getResourceType().equals("resource")) { %>
            <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                    src="images/resource.gif" style="margin-right:5px;" align="top"/>
                    <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                    <a onclick="directToResource('../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=tempPath%>')"
                    href="#"><%=resourceData.getResourcePath()%></a>
                    <% } else { %>
                    <%=resourceData.getResourcePath()%>
                    <% } %>
            </td>
            <% } %>

            <td style="padding-left:5px;padding-top:3px;text-align:left;"><nobr><%=resourceData.getFormattedCreatedOn()%></nobr>
            </td>
            <td style="padding-left:5px;padding-top:3px;text-align:left;"><%=resourceData.getAuthorUserName()%>
            </td>
            <td style="padding-left:5px;padding-top:3px;text-align:left;">
            <div style="width:140px;">
                <img src="images/r<%=resourceData.getAverageStars()[0]%>.gif"/>
                <img src="images/r<%=resourceData.getAverageStars()[1]%>.gif"/>
                <img src="images/r<%=resourceData.getAverageStars()[2]%>.gif"/>
                <img src="images/r<%=resourceData.getAverageStars()[3]%>.gif"/>
                <img src="images/r<%=resourceData.getAverageStars()[4]%>.gif"/>
                (<%=resourceData.getAverageRating()%>)
            </div>
            </td>
        </tr>

        <% } %>
        <!--Setting Metadata search results to metaDataSearchReportBean-->
        <%
            List<MetaDataReportBean> searchReportBeanList = new ArrayList<MetaDataReportBean>();


                for(int i=0;i<fullResourceDataList.length;i++){
                   ResourceData resourceDataFull = fullResourceDataList[i];
                   if (resourceDataFull == null) {
                       continue;
                   }
                   MetaDataReportBean metaDataSearchReportBean = new MetaDataReportBean();

                   metaDataSearchReportBean.setAuthorName(resourceDataFull.getAuthorUserName());
                   metaDataSearchReportBean.setResourcePath(resourceDataFull.getResourcePath());
                   metaDataSearchReportBean.setCreatedDate(resourceDataFull.getFormattedCreatedOn());
                   metaDataSearchReportBean.setAverageRating(Float.toString(resourceDataFull.getAverageRating()));

                   searchReportBeanList.add(metaDataSearchReportBean);
                }
            request.getSession().setAttribute("metaDataSearchReport", searchReportBeanList);

        %>
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.search.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev" tdColSpan="4"
                                  paginationFunction="submitAdvSearchForm({0})" />

                 <%
                     if (resourceExists) {
                 %>
             <tr>
                 <td colspan="4">

                     <div class="search-subtitle" style="padding-left:10px;padding-bottom:10px"><fmt:message
                             key="save.search"/></div>
                     <div style="padding-left:10px;color:#666666;font-style:italic;"><fmt:message
                             key="search.save.txt"/></div>


                     <form id="saveAdvancedSearchForm" name="saveAdvancedSearch" action=""
                           method="get">
                         <table class="normal">
                             <tr>
                                 <td class="leftCol-small"><fmt:message key="filter.name"/></td>
                                 <td>
                                     <input type="text" name="saveFilterName" id="#_saveFilterName"
                                            onkeypress="handletextBoxKeyPress(event)"/>
                                 </td>
                                 <td>
                                     <input type="button" id="#_clicked"
                                            value="<fmt:message key="save"/>" class="button"
                                            onclick="submitSaveSearchForm()"/>
                                 </td>
                             </tr>
                         </table>
                     </form>
                 </td>
             </tr>
                <%
                     }
                 %>
        </tbody>
    </table>

    <%
    } else {
    %>
    
    <strong><fmt:message key="your.search.did.not.match.any.resources"/></strong>
    <%
        }
    %>
</fmt:bundle>
