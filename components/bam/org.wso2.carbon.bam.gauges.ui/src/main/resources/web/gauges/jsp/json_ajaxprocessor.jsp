<%--
 * Copyright 2008 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>
        <%@ page import="org.apache.axiom.om.OMElement" %>
        <%@ page import="org.apache.axiom.om.util.Base64" %>
        <%@ page import="org.apache.axiom.soap.SOAPEnvelope"%>
        <%@ page import="org.apache.axis2.AxisFault"%>
        <%@ page import="org.apache.axis2.Constants"%>
        <%@ page import="org.apache.axis2.addressing.EndpointReference"%>
        <%@ page import="org.apache.axis2.client.Options"%>
        <%@ page import="org.apache.axis2.client.ServiceClient"%>
        <%@ page import="org.apache.axis2.context.ConfigurationContext"%>
        <%@ page import="org.apache.axis2.context.MessageContext"%>
        <%@ page import="org.apache.axis2.context.OperationContext"%>
        <%@ page import="org.apache.axis2.description.WSDL2Constants"%>
        <%@ page import="org.wso2.carbon.CarbonConstants" %>
        <%@ page import="org.wso2.carbon.utils.ServerConstants"%>
        <%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
        <%@ page import="java.io.IOException"%>
        <%@ page import="org.apache.axiom.om.util.AXIOMUtil"%>
        <%@ page import="javax.xml.stream.XMLStreamException"%>
        <%@ page contentType="application/javascript" language="java" %>
        <%!

            String decode(String s) throws Exception {
                if ("~".equals(s)) return null;
                return new String(Base64.decode(s), "UTF-8");
            }

            /*OMElement json2OM(String json) throws IOException {
                String localName = "";
                String prefix = "";
                OMFactory factory = OMAbstractFactory.getOMFactory();
                OMNamespace ns = factory.createOMNamespace("", "");
                Reader reader = new StringReader(json);
                *//*
                   Now we have to read the localname and prefix from the input stream
                   if there is not prefix, message starts like {"foo":
                   if there is a prefix, message starts like {"prefix:foo":
                *//*

                //read the stream until we find a : symbol
                char temp = (char) reader.read();
                while (temp != ':') {
                    if (temp != ' ' && temp != '{') {
                        localName += temp;
                    }
                    temp = (char) reader.read();
                }

                //if the part we read ends with ", there is no prefix, otherwise it has a prefix
                if (localName.charAt(0) == '"') {
                    if (localName.charAt(localName.length() - 1) == '"') {
                        localName = localName.substring(1, localName.length() - 1);
                    } else {
                        prefix = localName.substring(1, localName.length()) + ":";
                        localName = "";
                        //so far we have read only the prefix, now lets read the localname
                        temp = (char) reader.read();
                        while (temp != ':') {
                            if (temp != ' ') {
                                localName += temp;
                            }
                            temp = (char) reader.read();
                        }
                        localName = localName.substring(0, localName.length() - 1);
                    }
                }
                AbstractJSONDataSource jsonDataSource = new JSONBadgerfishDataSource(reader, "\"" + prefix + localName + "\"");
                return new OMSourcedElementImpl(localName, ns, factory, jsonDataSource);
            }*/

            String getErrorXML(String msg, String exception) {
                msg = msg == null ? "" : msg;
                exception = exception == null ? "" : exception;
                return "<error xmlns=\"http://bam.carbon.wso2.org/errors/json_ajaxprocessor\">" +
                     "<message>" + msg + "</message>" +
                     "<exception>" + exception + "</exception>" +
                   "</error>";
            }
        %>
        <%
            String body;
            String service = request.getParameter("service");
            String action = request.getParameter("action");
            String payload = request.getParameter("payload");

            if(payload != null && !"".equals(payload)) {
                try {
                    payload = decode(payload);
                } catch(Exception e) {
                    body = getErrorXML("Error decoding base64 encoded payload", e.getMessage());
                    out.print(body);
                    return;
                }
            }

            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            Options opts = new Options();
            opts.setTo(new EndpointReference(backendServerURL + service));
            opts.setAction(action);
            opts.setProperty(Constants.Configuration.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_POST);
            opts.setManageSession(true);
            opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            /*opts.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json/badgerfish");*/

            // Parse
            OMElement payloadElement;
            try {
                payloadElement = payload != null ? AXIOMUtil.stringToOM(payload) : null;
                //creating service client
                ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                ServiceClient sc = new ServiceClient(configContext, null);
                sc.setOptions(opts);

                try {
                        sc.sendReceive(payloadElement);
                        OMElement responseElement = sc.getLastOperationContext().
                            getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN).getEnvelope().getBody().getFirstElement();
                        body = responseElement.toString();
                } catch (AxisFault axisFault) {
                    OperationContext operationContext = sc.getLastOperationContext();
                    if (operationContext != null) {
                        MessageContext messageContext =
                                operationContext.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                        if (messageContext != null) {
                            SOAPEnvelope envelope = messageContext.getEnvelope();
                            if (envelope != null) {
                                OMElement bodyElement = envelope.getBody().getFirstElement();
                                body = getErrorXML(null, bodyElement.toString());
                            } else body = getErrorXML("Error in payload", axisFault.toString());
                        }  else body = getErrorXML("MessageContext error", axisFault.toString());
                    } else body = getErrorXML("AxisFault", axisFault.toString());
                } finally {
                    sc.cleanupTransport();
                }
            } catch (XMLStreamException e) {
                body = getErrorXML("Invalid input : " + payload, e.getMessage());
            } catch (IOException e) {
                body = getErrorXML("Error parsing payload : '" + payload, e.getMessage());
            }
            out.print(body);
        %>