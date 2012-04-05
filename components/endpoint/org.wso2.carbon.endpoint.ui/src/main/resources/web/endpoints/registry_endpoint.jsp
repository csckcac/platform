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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="javax.xml.namespace.QName" %>
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);

    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    boolean isTemplateRegMode = false;
    if (isFromTemplateEditor) {
        //to identify we are saving a reg template
        isTemplateRegMode = session.getAttribute("templateEdittingMode") != null ? true : false;
    }


    String anonEpAction = request.getParameter("anonEpAction");
    String forwardTo = "";
    if (request.getParameter("cancelled") != null && "true".equals(request.getParameter("cancelled"))) {
        if (!isFromTemplateEditor) {
            forwardTo = "index.jsp?header=" + session.getAttribute("header");
        } else{
            forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-4";
        }
        // removes common attributes
        removeCommonSessionAttributes(session);
    } else if (isTemplateRegMode) {
        //We are in the path of saving reg endpoint templates edit-->reg Endpoint Templates --> save
        String configuration = (String) session.getAttribute("anonEpXML");
        String key = (String) session.getAttribute("templateRegKey");
        // the user may have cancelled the operation and therefore the anonEpXML may be null as well
        if (configuration != null && !"".equals(configuration) && key != null) {
            //editing a endpoint template
            EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
            templateClient.saveDynamicTemplate(key, configuration);
        }
        forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-4";
        session.removeAttribute("templateRegKey");
        session.removeAttribute("templateEdittingMode");
        removeCommonSessionAttributes(session);
    } else {
        if (anonEpAction != null && !"".equals(anonEpAction) && "edit".equals(anonEpAction)) {
            // send path
            // sets the anonOriginator as anonEpHandler.jsp. This will be the page to which result should be returned
            session.setAttribute("anonOriginator", "registry_endpoint.jsp");
            session.setAttribute("header", request.getParameter("header"));
            session.setAttribute("epMode", "anon");
//            ProxyData pd = (ProxyData) session.getAttribute("proxy");
//            session.setAttribute("proxy", pd);
            // going to modify the existing EP
            String key = request.getParameter("key");
            String anonEpXML = client.getDynamicEndpoint(key);
            session.setAttribute("dynamicEprKey", key);
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                try {
                    StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(anonEpXML.getBytes()));
                    OMElement confElem = builder.getDocumentElement();
                    OMElement elem = null;
                    if ((elem = confElem.getFirstElement()) != null) {
                        String localName = elem.getLocalName();
                        if ("address".equals(localName)) {
                            // current one is an address EP
                            forwardTo = "../endpoints/addressEndpoint.jsp";
                        } else if ("wsdl".equals(localName)) {
                            // current one is an wsdl EP
                            forwardTo = "../endpoints/WSDLEndpoint.jsp";
                        } else if ("failover".equals(localName)) {
                            // current one is an failover EP
                            forwardTo = "../endpoints/failOverEndpoint.jsp";
                        } else if ("loadbalance".equals(localName)) {
                            // current one is an loadBalance EP
                            forwardTo = "../endpoints/loadBalanceEndpoint.jsp";
                        } else if ("default".equals(localName)) {
                            forwardTo = "../endpoints/defaultEndpoint.jsp";
                            session.setAttribute("regEpName", key);
                        } else if (confElem.getAttribute(new QName("template")) != null) {
                            forwardTo = "../endpoints/templateEndpoint.jsp";
                        }
                    }
                } catch (XMLStreamException e) {
                    // todo - handle error
                }
                forwardTo = forwardTo + "?toppage=false";
                session.setAttribute("anonEpXML", anonEpXML);
            }
        } else {
            // return path
            String anonEpXML = (String) session.getAttribute("anonEpXML");
            String key = (String) session.getAttribute("dynamicEprKey");
            // the user may have cancelled the operation and therefore the anonEpXML may be null as well
            if (anonEpXML != null && !"".equals(anonEpXML) && key != null) {
                client.updateDynamicEndpoint(key, anonEpXML);
            }
            forwardTo = "index.jsp?header=" + session.getAttribute("header") + "&fromdesign=true";
            removeCommonSessionAttributes(session);
        }
    }
%>

<%!
    void removeCommonSessionAttributes(HttpSession session) {
        session.removeAttribute("dynamicEprKey");
        session.removeAttribute("anonOriginator");
        session.removeAttribute("epMode");
        session.removeAttribute("anonEpXML");
        session.removeAttribute("header");
    }
%>

<script type="text/javascript">
    window.location.href = '<%=forwardTo%>';
</script>
