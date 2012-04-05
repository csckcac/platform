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
<%@ page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="java.util.ResourceBundle" %>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
    <carbon:breadcrumb label="view.keystore"
                       resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%

        CertData[] certData = new CertData[0];
        KeyStoreData keyStoreData = null;
        String keyStore = request.getParameter("keyStore");
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ServletContext servletContext = session.getServletContext();
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
            keyStoreData = client.getKeystoreInfo(keyStore);
            certData = keyStoreData.getCerts();
        } catch (Exception e) {

    %>
    <strong>An error occurred!</strong>

    <p>Error message is : <%=e.getMessage()%>
    </p>
    <%

        }

    %>
    
    <script type="text/javascript">
        
        function deleteCert(alias, keystore) {

            function doDelete(){
                var certAlias = alias;
                var keystoreName = keystore;		
                location.href = 'delete-cert.jsp?alias=' + certAlias+'&keystore='+keystoreName;
            }		
            CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.the.certificate"/> ' + alias + '?', doDelete, null);		
        }
        
        
    </script>

    <div id="middle">
        <h2><fmt:message key="view.keystore"/></h2>

        <div id="workArea">
            <h3><fmt:message key="available.certificates"/></h3>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="alias"/></th>
                    <th><fmt:message key="issuerdn"/></th>
                    <th><fmt:message key="notafter"/></th>
                    <th><fmt:message key="notbefore"/></th>
                    <th><fmt:message key="serialnumber"/></th>
                    <th><fmt:message key="subjectdn"/></th>
                    <th><fmt:message key="version"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (certData != null && certData.length > 0) {
                        for (CertData cert : certData) {
                            if (cert != null) {
                %>
                <tr>
                    <td><%=cert.getAlias()%>
                    </td>
                    <td><%=cert.getIssuerDN()%>
                    </td>
                    <td><%=cert.getNotAfter()%>
                    </td>
                    <td><%=cert.getNotBefore()%>
                    </td>
                    <td><%=cert.getSerialNumber()%>
                    </td>
                    <td><%=cert.getSubjectDN()%>
                    </td>
                    <td><%=cert.getVersion()%>
                    </td>
                    <td><a href="#" onclick="deleteCert('<%=cert.getAlias()%>', '<%=keyStoreData.getKeyStoreName()%>')" class="icon-link" style="background-image:url(images/delete.gif);">Delete</a>
                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
                <tr>
                    <td style="border-left: 0px !important; border-right: 0px !important; padding-left: 0px !important;"
                        colspan="7">
                        <h3><fmt:message key="certificate.of.the.private.key"/></h3></td>
                </tr>
                <tr>
                    <td class="sub-header"><fmt:message key="alias"/></td>
                    <td class="sub-header"><fmt:message key="issuerdn"/></td>
                    <td class="sub-header"><fmt:message key="notafter"/></td>
                    <td class="sub-header"><fmt:message key="notbefore"/></td>
                    <td class="sub-header"><fmt:message key="serialnumber"/></td>
                    <td class="sub-header"><fmt:message key="subjectdn"/></td>
                    <td colspan="2" class="sub-header"><fmt:message key="version"/></td>
                </tr>


                <%
                    if (keyStoreData.getKey() != null) {
                        CertData cdata = keyStoreData.getKey();
                %>
                <tr>
                    <td><%=cdata.getAlias()%>
                    </td>
                    <td><%=cdata.getIssuerDN()%>
                    </td>
                    <td><%=cdata.getNotAfter()%>
                    </td>
                    <td><%=cdata.getNotBefore()%>
                    </td>
                    <td><%=cdata.getSerialNumber()%>
                    </td>
                    <td><%=cdata.getSubjectDN()%>
                    </td>
                    <td colspan="2"><%=cdata.getVersion()%>
                    </td>
                </tr>
                <%              
                    }
                %>
                <tr>
	                <td class="buttonRow" colspan="8">
		                 <form>
		                 	   <input value="<fmt:message key="import.cert"/>" type="button" class="button" onclick="location.href ='import-cert.jsp?keyStore=<%=keyStore%>'"/>
		                       <input value="<fmt:message key="finish"/>" type="button" class="button" onclick="location.href ='keystore-mgt.jsp?region=region1&item=keystores_menu'"/>
		                 </form>
	                </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>