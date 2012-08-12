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
<%@ page import="org.wso2.carbon.hosting.mgt.stub.types.carbon.AppsWrapper" %>
<jsp:include page="../dialog/display_messages.jsp"/>

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

    AppsWrapper appsWrapper;
    String[] apps;
    String[] endPoints;

    String cartridgeTitle = request.getParameter("cartridges");
    if (cartridgeTitle == null) {
        cartridgeTitle = "";
    }
    try {
         client = new HostingAdminClient(request.getLocale(),cookie, configContext, serverURL );
         appsWrapper = client.getPagedAppsSummary(cartridgeTitle);
         numberOfPages = appsWrapper.getNumberOfPages();
        //get the app list and endpoints list
         apps = appsWrapper.getApps();
         endPoints = appsWrapper.getEndPoints();
    } catch (Exception e) {
         response.setStatus(500);
         CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
         session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
<jsp:include page="../admin/error.jsp"/>
    <%
        return;
    }   int numOfapps = 0;
        if(apps != null){
            numOfapps = apps.length;
        }
            ResourceBundle bundle = ResourceBundle.getBundle(HostingAdminClient.BUNDLE, request.getLocale());
    %>

<fmt:bundle basename="org.wso2.carbon.hosting.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="webapp"
                       resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <jsp:include page="javascript_include.jsp"/>

<script type="text/javascript">

    var allPhpAppsSelected = false;
    function isAppSelected() {
        var selected = false;
        if (document.appsForm.appFileName[0] != null) { // there is more than 1
            for (var j = 0; j < document.appsForm.appFileName.length; j++) {
                selected = document.appsForm.appFileName[j].checked;
                if (selected) break;
            }
        } else if (document.appsForm.name != null) { // only 1
            selected = document.appsForm.appFileName.checked;
        }
        return selected;
    }

    function selectAllInThisPage(isSelected) {
        allPhpAppsSelected = false;
        if (document.appsForm.appFileName != null &&
            document.appsForm.appFileName[0] != null) { // there is more than 1
            if (isSelected) {
                for (var j = 0; j < document.appsForm.appFileName.length; j++) {
                    document.appsForm.appFileName[j].checked = true;
                }
            } else {
                for (j = 0; j < document.appsForm.appFileName.length; j++) {
                    document.appsForm.appFileName[j].checked = false;
                }
            }
        } else if (document.appsForm.appFileName != null) { // only 1
            document.appsForm.appFileName.checked = isSelected;
        }
        return false;
    }

    function deleteApps() {
        var selected = isAppSelected();
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.apps.to.be.deleted"/>');
            return;
        }
        if (allPhpAppsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.apps.prompt"><fmt:param value="<%= numOfapps%>"/></fmt:message>",
              function() {
                  location.href = 'delete_apps.jsp?deleteAllapps=true&cartridge=<%=cartridgeTitle%>';
              }
        );
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.apps.on.page.prompt"/>",
                function() {
                  document.appsForm.action = 'delete_apps.jsp?cartridge=<%=cartridgeTitle%>';
                  document.appsForm.submit();
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
        if (document.appsForm.appFileName[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.appsForm.appFileName.length; j++) {
                if (document.appsForm.appFileName[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.appsForm.appFileName != null) { // only 1 sg
            if (document.appsForm.appFileName.checked) {
                isSelected = true;
            }
        }
        return false;
    }
</script>


<script type="text/javascript">
    function listApps() {
        document.listForm.submit();
    }
</script>

<div id="middle">



<h2><fmt:message key="apps"/></h2>

<div id="workArea">
    <%
        String cartridges[] = client.getCartridges();
     %>
    <form action="index.jsp" name="listForm">
        <table class="styledLeft">
            <tr>
                <td width="30px" class="cartridgeRow">
                    <nobr>
                        <fmt:message key="cartridge"/>
                        <label><font color="red">*</font></label>
                    </nobr>
                </td>
                <td class="cartridgeRow">
                    <nobr>
                        <select name="cartridges" id="cartridges">
                            <option value="selectACartridge" selected="selected">
                               <fmt:message key="select.cartridge"/>
                            </option>
                                <%
                                for (String cartridge : cartridges) {
                                 %>
                                    <option value="<%= cartridge%>"> <%= cartridge%>  </option>
                                <%
                                }
                                 %>
                        </select>
                    </nobr>
                </td>
                <td class="buttonRow">
                    <input type="button" class="button"
                           onclick="listApps(); return false;"
                           value=" <fmt:message key="list"/> "/>
                </td>
            </tr>
        </table>
    </form>


    <%
       if (apps != null && cartridgeTitle != null && cartridgeTitle != "") {
   %>

<p>&nbsp;</p>
    <p><strong><%=cartridgeTitle%> Applications</strong></p>
    <p>&nbsp;</p>

   <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                     page="index.jsp" pageNumberParameterName="pageNumber"
                     resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                     prevKey="prev" nextKey="next"/>

   <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                             selectAllFunction="selectAllInAllPages()"
                             selectNoneFunction="selectAllInThisPage(false)"
                             resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                             selectAllInPageKey="selectAllInPage"
                             selectAllKey="selectAll"
                             selectNoneKey="selectNone"
                             addRemoveFunction="deleteApps()"
                             addRemoveButtonId="delete1"
                             addRemoveKey="delete"
                             numberOfPages="<%=numberOfPages%>"/>
   <p>&nbsp;</p>



<form action="delete_apps.jsp" name="appsForm" method="post">
<input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
<table class="styledLeft" id="webappsTable" width="100%">
    <thead>
    <tr>
        <th width="10px"></th>
        <th><fmt:message key="name"/></th>
        <!--th--><!--fmt:message key="endpoint"/--><!--/th-->
    </tr>
    </thead>
    <tbody>
    <%
        for (int i=0; i< apps.length; i++) {
    %>
    <tr>
        <td width="10px" style="text-align:center; !important">
                        <input type="checkbox" name="appFileName"
                               value="<%=apps[i]%>"
                               onclick="resetVars()" class="chkBox"/>
        </td>
        <td>
            <%=apps[i]%>
        </td>
        <!--td-->
            <!--%=endPoints[i]%-->
        <!--/td-->
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
                          addRemoveFunction="deleteApps()"
                          addRemoveButtonId="delete2"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"/>
<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"/>
<%
}
%>

</div>



</div>
</fmt:bundle>