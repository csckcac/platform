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
<%@ page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@ page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonError"%>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

    <%
        KeyStoreData[] datas = null;
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
            datas = client.getKeyStores();
        } catch (Exception e) {
            CarbonError error = new CarbonError();
            error.addError(e.getMessage());
            request.getSession().setAttribute(CarbonError.ID, error);
        %>
            <script type="text/javascript">
                location.href = '../admin/error.jsp';
            </script>
        <%
        }

    %>
    
<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="keystore.management"
		resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
    <script type="text/javascript">
        
        function deleteKeystore(keystore) {
            function doDelete(){
                var keystoreName = keystore;
                location.href = 'delete-keystore.jsp?keyStore=' + keystoreName;
            }
            CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.the.keystore"/> ' + keystore + '?', doDelete, null);
        }
        
        
    </script>

    
    <div id="middle">
        <h2><fmt:message key="keystore.management"/></h2>
        <div id="workArea">
            <table class="styledLeft" id="keymgtTable">
                <thead>
                <tr>
                    <th width="30%"><fmt:message key="name"/></th>
                    <th width="10%"><fmt:message key="type"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (datas != null) {
                        for (KeyStoreData data : datas) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!
                %>
                <tr>
                    <td><%=data.getKeyStoreName()%>
                    </td>
                    <td><%=data.getKeyStoreType()%>
                    </td>
                    <td>
<a href="import-cert.jsp?keyStore=<%=data.getKeyStoreName()%>" class="icon-link" style="background-image:url(images/import.gif);"><fmt:message key="import.cert"/></a>
<a href="view-keystore.jsp?keyStore=<%=data.getKeyStoreName()%>" class="icon-link" style="background-image:url(images/view.gif);"><fmt:message key="view"/></a>
<a href="#" onclick="deleteKeystore('<%=data.getKeyStoreName()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a>
                    <%if(data.getPubKeyFilePath() != null){ %>
<a href="<%=data.getPubKeyFilePath()%>" class="icon-link" style="background-image:url(images/view.gif);"><fmt:message key="download.pub.key"/></a>
                        <%}%>
                    </td>
                </tr>
                <% }
                }
                %>
                </tbody>
            </table>
            <%
                }
            %>
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td>
			<a href="add-keystore-step1.jsp" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key="add.new.keystore"/></a>
                </td>
            </tr>
         </table>
    </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('keymgtTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>