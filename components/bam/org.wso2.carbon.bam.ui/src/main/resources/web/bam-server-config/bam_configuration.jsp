<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="static org.wso2.carbon.bam.ui.BAMUIConstants.DATA_RETENTION_PERIOD" %>
<%@ page import="static org.wso2.carbon.bam.ui.BAMUIConstants.*" %>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<!--link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/-->
<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
    <carbon:breadcrumb label="bam.server.configuration"
                       resourceBundle="org.wso2.carbon.bam.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%
        BAMConfigAdminServiceClient client;
        String retentionPeriod;
        String archivalPeriod;
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            client = new BAMConfigAdminServiceClient(cookie, serverURL, configContext);
        } catch (Exception e) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
    <%
            return;
        }

        if (request.getParameter(ACTION_SUBMIT) != null) {

            try {
                client.updateDataArchivalPeriod(request.getParameter(DATA_ARCHIVAL_PERIOD));
                client.updateDataRetentionPeriod(request.getParameter(DATA_RETENTION_PERIOD));
    %>
    <script type="text/javascript">
        jQuery(document).init(function() {
            function handleOK() {
                window.location = '../bam-server-config/bam_configuration.jsp?region=region1&item=bam_server_configuration';
            }

            CARBON.showInfoDialog("<fmt:message key="configuration.data.successfully.updated"/>", handleOK);
        });
    </script>
    <%
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>

    <%
                return;
            }
        }

        retentionPeriod = client.getDataRetentionPeriod();
        archivalPeriod = client.getDataArchivalPeriod();
    %>

    <script type="text/javascript">
        function validate() {
            value = document.getElementsByName("<%=DATA_RETENTION_PERIOD%>")[0].value;
            if (value == '' || !(/^[0-9]+[hmd]$/.test(value))) {
                CARBON.showWarningDialog("<fmt:message key="invalid.retention.period.format"/>");
                return false;
            }
            value = document.getElementsByName("<%=DATA_ARCHIVAL_PERIOD%>")[0].value;
            if (value == '' || !(/^[0-9]+[md]$/.test(value))) {
                CARBON.showWarningDialog("<fmt:message key="invalid.archival.period.format"/>");
                return false;
            }
            document.bamConfigurationForm.submit();
            return true;
        }
    </script>


    <div id="middle">
    <h2><fmt:message key="bam.server.configuration"/></h2>

    <div id="workArea">
        <div>
            <form method="post" name="bamConfigurationForm"
                  action="../bam-server-config/bam_configuration.jsp" target="_self">
                <table width="60%" id="bamConfigTable" class="styledLeft">
                    <thead>
                    <tr>
                        <th>BAM Configurations</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <tbody>
                                <tr>
                                    <td class="leftCol-small"><fmt:message
                                            key="data.archival.period"/></td>
                                    <td><input class="text-box-big" id="<%=DATA_ARCHIVAL_PERIOD%>"
                                               name="<%=DATA_ARCHIVAL_PERIOD%>" type="text"
                                               value="<%=archivalPeriod%>"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-small"><fmt:message
                                            key="data.retention.period"/>
                                    </td>
                                    <td><input class="text-box-big" id="<%=DATA_RETENTION_PERIOD%>"
                                               name="<%=DATA_RETENTION_PERIOD%>"
                                               type="text" value="<%=retentionPeriod%>">
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="bamConfig" type="button" class="button"
                                   value="<fmt:message key="save"/>" onclick="validate();"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
              <input type="hidden" id="<%=ACTION_SUBMIT%>" name="<%=ACTION_SUBMIT%>" type="text" value="<%=ACTION_SUBMIT%>"/>

            </form>
        </div>
    </div>
</fmt:bundle>
