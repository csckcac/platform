<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.mediator.payloadfactory.PayloadFactoryMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    System.out.println("in plf update");
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof PayloadFactoryMediator)) {
        CarbonUIMessage.sendCarbonUIMessage(
                "Unable to edit the mediator, expected: payloadFactoryMediator, found: " +
                        mediator.getTagLocalName(), CarbonUIMessage.ERROR, request);
        %><jsp:include page="../dialog/display_messages.jsp"/><%
        return;
    }
    PayloadFactoryMediator payloadFactoryMediator = (PayloadFactoryMediator) mediator;
    payloadFactoryMediator.getArgumentList().clear();

    payloadFactoryMediator.setFormat(request.getParameter("payloadFactory.format"));

    int maxArgCount = Integer.parseInt(request.getParameter("argCount"));
    XPathFactory xPathFactory = XPathFactory.getInstance();

    for (int i = 0; i < maxArgCount; ++i) {
        PayloadFactoryMediator.Argument arg = new PayloadFactoryMediator.Argument();
        String argType = request.getParameter("argType" + i);
        if (argType == null) {
            continue;
        }
        if ("value".equals(argType)) {
            arg.setValue(request.getParameter("payloadFactory.argValue" + i).trim());
        } else if ("expression".equals(argType)) {
            arg.setExpression(xPathFactory.createSynapseXPath("payloadFactory.argValue" + i,
                    request, session));
        } else {
            CarbonUIMessage.sendCarbonUIMessage("Invalid argument type is found for payloadFactory " +
                    "mediator", CarbonUIMessage.ERROR, request);
            %><jsp:include page="../dialog/display_messages.jsp"/><%
            return;
        }
        payloadFactoryMediator.addArgument(arg);
    }
%>
