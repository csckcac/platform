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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.hosting.mgt.ui.HostingAdminClient" %>
<%@ page import="org.wso2.carbon.hosting.mgt.stub.types.carbon.PHPappsWrapper" %>

<%
    response.setHeader("Cache-Control", "no-cache");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    HostingAdminClient client;
    int numberOfPages;

    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    PHPappsWrapper phpAppsWrapper;
    String[] phpApps;

    String phpappSearchString = request.getParameter("phpappSearchString");

    if (phpappSearchString == null) {
        phpappSearchString = "";
    }
    try {
         client = new HostingAdminClient(cookie, serverURL, configContext , request.getLocale());
         phpAppsWrapper = client.getPagedPhpAppsSummary(phpappSearchString,
                                                        Integer.parseInt(pageNumber));
         numberOfPages = phpAppsWrapper.getNumberOfPages();
         phpApps = phpAppsWrapper.getPhpapps();
    } catch (Exception e) {
         response.setStatus(500);
         CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
         session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
        return;
    }

        int numOfPhpapps = phpAppsWrapper.getNumberOfPhpapps();

            ResourceBundle bundle = ResourceBundle.getBundle(HostingAdminClient.BUNDLE, request.getLocale());
    %>

<fmt:bundle basename="org.wso2.carbon.hosting.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="webapp"
                       resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <jsp:include page="javascript_include.jsp"/>

<script type="text/javascript">

    var allPhpAppsSelected = false;
    function isPHPappSelected() {
        var selected = false;
        if (document.phpappsForm.phpappFileName[0] != null) { // there is more than 1
            for (var j = 0; j < document.phpappsForm.phpappFileName.length; j++) {
                selected = document.phpappsForm.phpappFileName[j].checked;
                if (selected) break;
            }
        } else if (document.phpappsForm.name != null) { // only 1
            selected = document.phpappsForm.phpappFileName.checked;
        }
        return selected;
    }

    function selectAllInThisPage(isSelected) {
        allPhpAppsSelected = false;
        if (document.phpappsForm.phpappFileName != null &&
            document.phpappsForm.phpappFileName[0] != null) { // there is more than 1
            if (isSelected) {
                for (var j = 0; j < document.phpappsForm.phpappFileName.length; j++) {
                    document.phpappsForm.phpappFileName[j].checked = true;
                }
            } else {
                for (j = 0; j < document.phpappsForm.phpappFileName.length; j++) {
                    document.phpappsForm.phpappFileName[j].checked = false;
                }
            }
        } else if (document.phpappsForm.phpappFileName != null) { // only 1
            document.phpappsForm.phpappFileName.checked = isSelected;
        }
        return false;
    }

    function deletePHPapps() {
        var selected = isPHPappSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.phpapps.to.be.deleted"/>');
            return;
        }
        if (allPhpAppsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.phpapps.prompt"><fmt:param value="<%= numOfPhpapps%>"/></fmt:message>",
                              function() {
                                  location.href = 'delete_phpapps.jsp?deleteAllPhpapps=true';
                              }
        );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.phpapps.on.page.prompt"/>",
                function() {
                  document.phpappsForm.action = 'delete_phpapps.jsp';
                  document.phpappsForm.submit();
                }
            );
        }
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allPhpAppsSelected = true;
        return false;
    }


    function resetVars() {
        allPhpAppsSelected = false;

        var isSelected = false;
        if (document.phpappsForm.phpappFileName[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.phpappsForm.phpappFileName.length; j++) {
                if (document.phpappsForm.phpappFileName[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.phpappsForm.phpappFileName != null) { // only 1 sg
            if (document.phpappsForm.phpappFileName.checked) {
                isSelected = true;
            }
        }
        return false;
    }
</script>


<script type="text/javascript">
    function searchPhpapps() {
        document.searchForm.submit();
    }
</script>

<div id="middle">
<h2><fmt:message key="phpapps"/></h2>

<div id="workArea">

<form action="index.jsp" name="searchForm">
    <table class="styledLeft">
        <tr>
            <td style="border:0; !important">
                <nobr>
                    <%= numOfPhpapps%> <fmt:message key="phpapps"/>.&nbsp;
                </nobr>
            </td>
        </tr>
        <tr>
            <td style="border:0; !important">&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table style="border:0; !important">
                    <tbody>
                    <tr style="border:0; !important">
                        <td style="border:0; !important">
                            <nobr>
                                <fmt:message key="search.phpapps"/>
                                <input type="text" name="phpappSearchString"
                                       value="<%= phpappSearchString != null? phpappSearchString : ""%>"/>&nbsp;
                            </nobr>
                        </td>
                        <td style="border:0; !important">
                            <a class="icon-link" href="#"
                               style="background-image: url(images/search.gif);"
                               onclick="searchPhpapps(); return false;"
                               alt="<fmt:message key="search"/>">
                            </a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</form>

<p>&nbsp;</p>
   <%
       if (phpApps != null) {
           String parameters = "phpappSearchString=" + phpappSearchString;


   %>

   <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                     page="index.jsp" pageNumberParameterName="pageNumber"
                     resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                     prevKey="prev" nextKey="next"
                     parameters="<%=parameters%>"/>

   <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                             selectAllFunction="selectAllInAllPages()"
                             selectNoneFunction="selectAllInThisPage(false)"
                             resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                             selectAllInPageKey="selectAllInPage"
                             selectAllKey="selectAll"
                             selectNoneKey="selectNone"
                             addRemoveFunction="deletePHPapps()"
                             addRemoveButtonId="delete1"
                             addRemoveKey="delete"
                             numberOfPages="<%=numberOfPages%>"/>
   <p>&nbsp;</p>
<form action="delete_phpapps.jsp" name="phpappsForm" method="post">
<input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
<table class="styledLeft" id="webappsTable" width="100%">
    <thead>
    <tr>
        <th>&nbsp;</th>
        <th width="15%"><fmt:message key="name"/></th>
    </tr>
    </thead>
    <tbody>
    <%
        for (String phpApp : phpApps) {
    %>
    <tr>
        <td width="10px" style="text-align:center; !important">
                        <input type="checkbox" name="phpappFileName"
                               value="<%=phpApp%>"
                               onclick="resetVars()" class="chkBox"/>
        </td>
        <td>
            <%=phpApp.substring(0, phpApp.indexOf(".zip"))%>
        </td>
    </tr>
    <% } %>
    </tbody>

</table>
</form>

<p>&nbsp;</p>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveFunction="deleteWebapps()"
                          addRemoveButtonId="delete2"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"/>
<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%= parameters%>"/>
<%
} else {
%>
<b><fmt:message key="no.phpapps.found"/></b>
<%
    }
%>
</div>
</div>
</fmt:bundle>