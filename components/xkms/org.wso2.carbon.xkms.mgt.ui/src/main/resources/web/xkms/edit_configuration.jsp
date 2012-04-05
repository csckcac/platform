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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.xkms.mgt.ui.XKMSMgtConstants" %>
<%@ page import="org.wso2.carbon.xkms.mgt.ui.Utils" %>
<%@ page import="org.wso2.carbon.xkms.mgt.ui.client.XKMSMgtClient" %>
<%@ page import="org.wso2.carbon.xkms.mgt.stub.types.XKMSConfigData" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    XKMSConfigData configData = null;


    try {
        XKMSMgtClient client = new XKMSMgtClient(cookie, serverURL, configContext);
        configData = client.getXKMSConfig();

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        response.sendRedirect("../admin/error.jsp");
    }

%>

<fmt:bundle basename="org.wso2.carbon.xkms.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="xkms.menu"
            resourceBundle="org.wso2.carbon.xkms.mgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <script type="text/javascript">
        function validate() {
          var value = document.getElementsByName("<%=XKMSMgtConstants.SERVER_AUTHENTICATION_CODE%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="server.authentication.code.is.required"/>');
             return false;
           }

          var value = document.getElementsByName("<%=XKMSMgtConstants.KEY_STORE_LOCATION%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="keystore.location.is.required"/>');
             return false;
           }

          var value = document.getElementsByName("<%=XKMSMgtConstants.KEY_STORE_PASSWORD%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="keystore.password.is.required"/>');
             return false;
           }

          var value = document.getElementsByName("<%=XKMSMgtConstants.SERVER_CERT_ALIACE%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="server.certificate.alias.is.required"/>');
             return false;
           }

            var value = document.getElementsByName("<%=XKMSMgtConstants.SERVER_KEY_PASSWORD%>")[0].value;
            if (value == '') {
                CARBON.showWarningDialog('<fmt:message key="server.key.password.is.required"/>');
                return false;
            }

          var value = document.getElementsByName("<%=XKMSMgtConstants.ISSUER_CERT_ALIACE%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="issuer.certificate.alias.is.required"/>');
             return false;
           }

          var value = document.getElementsByName("<%=XKMSMgtConstants.ISSUER_KEY_PASSWORD%>")[0].value;
          if (value == '') {
             CARBON.showWarningDialog('<fmt:message key="issuer.key.password.is.required"/>');
             return false;
           }          
          document.xkms_params.submit();
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='xkms.configuration'/></h2>

        <div id="workArea">
            <form method="post" name="xkms_params" action="update_configuration.jsp" target="_self">
                <table style="width: 100%" class="styledLeft">
            		<thead>
	             		<tr>
				         <th colspan="2"><fmt:message key='view.update.xkms.configuration'/></th>
			        </tr>
                    	</thead>
                    	<tbody>
				<tr>
					<td class="formRow">
						<table class="normal" cellspacing="0">
				                    <tr>
				                        <td><fmt:message key='server.authentication.code'/><span class="required">*</span></td>
				                        <td><input type="password" class="text-box-big" id="<%=XKMSMgtConstants.SERVER_AUTHENTICATION_CODE%>"
				                                   name="<%=XKMSMgtConstants.SERVER_AUTHENTICATION_CODE%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getAuthenCode())%>"></td>
				                    </tr>
				                    <tr >
				                        <td><fmt:message key='keystore.location'/><span class="required">*</span></td>
				                        <td><input class="text-box-big" id="<%=XKMSMgtConstants.KEY_STORE_LOCATION%>"
				                                   name="<%=XKMSMgtConstants.KEY_STORE_LOCATION%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getKeystoreLocation())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='keystore.password'/><span class="required">*</span></td>
				                        <td><input type="password" class="text-box-big" id="<%=XKMSMgtConstants.KEY_STORE_PASSWORD%>"
				                                   name="<%=XKMSMgtConstants.KEY_STORE_PASSWORD%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getKeystorePassword())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='server.certificate.alias'/><span class="required">*</span></td>
				                        <td><input class="text-box-big" id="<%=XKMSMgtConstants.SERVER_CERT_ALIACE%>"
				                                   name="<%=XKMSMgtConstants.SERVER_CERT_ALIACE%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getServerCertAlias())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='server.key.password'/><span class="required">*</span></td>
				                        <td><input type="password" class="text-box-big" id="<%=XKMSMgtConstants.SERVER_KEY_PASSWORD%>"
				                                   name="<%=XKMSMgtConstants.SERVER_KEY_PASSWORD%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getServerKeyPassword())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='issuer.certificate.alias'/><span class="required">*</span></td>
				                        <td><input class="text-box-big" id="<%=XKMSMgtConstants.ISSUER_CERT_ALIACE%>"
				                                   name="<%=XKMSMgtConstants.ISSUER_CERT_ALIACE%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getIssuerCertAlias())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='issuer.key.password'/><span class="required">*</span></td>
				                        <td><input type="password" class="text-box-big" id="<%=XKMSMgtConstants.ISSUER_KEY_PASSWORD%>"
				                                   name="<%=XKMSMgtConstants.ISSUER_KEY_PASSWORD%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getIssuerKeyPassword())%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='default.expiration.interval'/></td>
				                        <td><input class="text-box-big" id="<%=XKMSMgtConstants.DEFAULT_EXPIRY_INTERVAL%>"
				                                   name="<%=XKMSMgtConstants.DEFAULT_EXPIRY_INTERVAL%>" type="text"
				                                   value="<%=configData.getDefaultExpriyInterval()%>"></td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='default.private.key.password'/></td>
				                       <td><input type="password" class="text-box-big" id="<%=XKMSMgtConstants.DEFAULT_PRIVATE_KEY_PASSWORD%>"
				                                   name="<%=XKMSMgtConstants.DEFAULT_PRIVATE_KEY_PASSWORD%>" type="text"
				                                   value="<%=Utils.getInputFieldValue(configData.getDefaultPrivateKeyPassword())%>">
				                        </td>
				                    </tr>
				                    <tr>
				                        <td><fmt:message key='enable.persistence'/><span class="required">*</span></td>
				                        <td>
				                            <% boolean persistenceEnabled = configData.getPersistenceEnabled(); %>
				                            <select id="<%=XKMSMgtConstants.ENABLE_PERSISTENCE%>"
				                                    name="<%=XKMSMgtConstants.ENABLE_PERSISTENCE%>">
				                                <option value="true" <% if (persistenceEnabled) { %>
				                                        selected="selected" <% } %>>true
				                                </option>
				                                <option value="false" <% if (!persistenceEnabled) { %>
				                                        selected="selected" <% } %>>false
				                                </option>
				                            </select>
				
				                        </td>
				                    </tr>
						</table>
					</td>
				</tr>
	                    <tr>
	                        <td colspan="2" class="buttonRow">
	                            <input name="Submit" type="button" class="button"
	                                   value="<fmt:message key='update'/>"
	                                    onclick="validate();"     />
	                            <input type="reset" class="button" value="<fmt:message key='reset'/>"/>
	                        </td>
	                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
