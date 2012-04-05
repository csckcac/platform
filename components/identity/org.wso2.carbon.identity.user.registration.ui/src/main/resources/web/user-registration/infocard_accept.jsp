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
<%@page import="org.wso2.carbon.identity.user.registration.ui.client.UserRegistrationClient" %>
<%@page import="org.wso2.carbon.identity.user.registration.ui.util.TokenDecrypter" %>
<%@page import="java.util.ArrayList" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="org.apache.xml.serialize.XMLSerializer" %>
<%@ page import="org.apache.xml.serialize.OutputFormat" %>

<%
    String cssLocation = request.getParameter("css");
    if ("null".equals(cssLocation)) {
        cssLocation = null;
    }

    if (cssLocation != null) {
        cssLocation = URLDecoder.decode(cssLocation, "UTF-8");
    }

    String pageTitle = request.getParameter("title");

    String forwardPage = request.getParameter("forwardPage");
    if (forwardPage != null) {
        forwardPage = URLDecoder.decode(forwardPage, "UTF-8");
    }

    String xmlToken = request.getParameter("xmlToken");
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.user.registration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    if (xmlToken == null) {
        String message = resourceBundle.getString("information.card.not.accepted");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?region=region1&item=user_registration_menu&ordinal=0";

        if (forwardPage != null) {
            forwardTo = forwardPage;
        }
    } else {

        Element element = TokenDecrypter.decryptToken(xmlToken);

        OutputFormat format = new OutputFormat();
        StringWriter sw = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(sw, format);
        serializer.serialize(element);

        String stringToken = sw.toString();

        if (xmlToken != null) {
            String serverURL = CarbonUIUtil.getServerURL(config
                    .getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session
                    .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            UserRegistrationClient client = null;
            InfoCarDTO infoCard = null;

            try {
                client = new UserRegistrationClient(cookie, serverURL, configContext);
                infoCard = new InfoCarDTO();
                infoCard.setXmlToken(stringToken);
                client.addUserWithInfoCard(infoCard);
                String message = resourceBundle.getString("information.card.added");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
                forwardTo = "index.jsp?region=region1&item=user_registration_menu&ordinal=0";

                if (forwardPage != null) {
                    forwardTo = forwardPage;
                }
            } catch (Exception e) {
                String message = resourceBundle.getString("information.card.not.accepted");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                forwardTo = "index.jsp?region=region1&item=user_registration_menu&ordinal=0";

                if (forwardPage != null) {
                    forwardTo = forwardPage;
                }
            }
        }
    }
%>


<%@page import="org.apache.axis2.util.XMLUtils" %>
<%@page import="java.util.ResourceBundle" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.wso2.carbon.identity.user.registration.stub.dto.InfoCarDTO" %>
<script
        type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
