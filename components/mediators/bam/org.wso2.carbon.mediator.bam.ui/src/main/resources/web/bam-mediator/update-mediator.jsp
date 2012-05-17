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

<%@ page import="org.wso2.carbon.mediator.bam.ui.BamMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String uri = "", prefix = "";
    if (!(mediator instanceof BamMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    BamMediator bamMediator = (BamMediator) mediator;



    bamMediator.setServerUrl(request.getParameter("serverUrl"));
    bamMediator.setUserName(request.getParameter("userName"));
    bamMediator.setPassword(request.getParameter("password"));
    bamMediator.setPort(request.getParameter("port"));

    bamMediator.setServerProfile(request.getParameter("serverProfile"));

    String propertyListString = request.getParameter("hfPropertyTableData");
    List<Property> properties = null;
    if (propertyListString != null) {
        properties = new ArrayList<Property>();
        Property property;
        String keyValuePair [];
        String[] propertyList = propertyListString.split(";");
        for (int i = 0; i < propertyList.length; i++) {
            property = new Property();
            keyValuePair = propertyList[i].split(":");
            property.setKey(keyValuePair[0]);
            property.setValue(keyValuePair[1]);
            property.setKey(propertyList[i].split(":")[0]);
            property.setValue(propertyList[i].split(":")[1]);
            properties.add(property);
        }
    }
    bamMediator.setProperties(properties);


/*    bamMediator.setServerProfile(request.getParameter("seq_ref"));

    String inputExpr = request.getParameter("inputExpr");
    if (inputExpr != null && !inputExpr.equals("")) {
        XPathFactory xPathFactory = XPathFactory.getInstance();
        bamMediator.setInputExpression(xPathFactory.createSynapseXPath("inputExpr", request, session));
    } else {
        bamMediator.setInputExpression(null);
    }

    String inputTYpe = request.getParameter("inputTypeSelect");
    bamMediator.setInputType(inputTYpe);

    String outputType = request.getParameter("outputTypeSelect");
    bamMediator.setOutputType(outputType);

    bamMediator.setOutputProperty(null);
    bamMediator.setOutputExpression(null);
    bamMediator.setOutputAction(null);
    String outputExprType = request.getParameter("outputExprSelect");
    if (outputExprType != null && outputExprType.equals("expression")) {
        String outputExpr = request.getParameter("outputExpr");
        if (outputExpr != null) {
            XPathFactory xPathFactory = XPathFactory.getInstance();
            bamMediator.setOutputExpression(xPathFactory.createSynapseXPath("outputExpr", request, session));
            bamMediator.setOutputProperty(null);
        }

        String outAction = request.getParameter("outputActionSelect");
        if (outAction != null) {
            bamMediator.setOutputAction(outAction);
        }
    } else {
        String outProp = request.getParameter("outputExpr");
        if (outProp != null && !outProp.equals("")) {
            bamMediator.setOutputExpression(null);
            bamMediator.setOutputProperty(outProp);
        }
    }*/
%>

