<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<%@ page import="org.wso2.carbon.mediator.callout.CalloutMediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String targetVal = null, sourceVal = null;
    String targetExp = null, sourceExp = null;
    SynapseXPath xpath = null;
    String uri = null, prefix = null;
    XPathFactory xPathFactory = XPathFactory.getInstance();
    String param = null;
    if (!(mediator instanceof CalloutMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CalloutMediator calloutMediator = (CalloutMediator) mediator;
    calloutMediator.setServiceURL(request.getParameter("mediator.callout.svcURL"));

    calloutMediator.setAction(null);
    calloutMediator.setClientRepository(null);
    calloutMediator.setAxis2xml(null);

    param = request.getParameter("mediator.callout.action");
    if (param != null && !param.equals("")) {
        calloutMediator.setAction(param);
    }    
    param = request.getParameter("mediator.callout.repo");
    if (param != null && !param.equals("")) {
        calloutMediator.setClientRepository(param);
    }
    param = request.getParameter("mediator.callout.axis2XML");
    if (param != null && !param.equals("")) {
        calloutMediator.setAxis2xml(param);
    }

    param = request.getParameter("mediator.callout.initAxis2ClientOptions");

    if(param != null && !param.equals("")) {
        calloutMediator.setInitAxis2ClientOptions(param);
    }

    calloutMediator.setRequestKey(null);
    calloutMediator.setRequestXPath(null);
    calloutMediator.setTargetKey(null);
    calloutMediator.setTargetXPath(null);

    String sourceGroup = request.getParameter("sourcegroup");
    if (sourceGroup != null && !sourceGroup.equals("")){
        if (sourceGroup.equals("Key")) {
            sourceVal = request.getParameter("mediator.callout.source.key_val");
            if (sourceVal != null && !sourceVal.equals("")) {
                calloutMediator.setRequestKey(sourceVal);
            }
        } else if (sourceGroup.equals("XPath")) {
            sourceExp = request.getParameter("mediator.callout.source.xpath_val");
            if (sourceExp != null && !sourceExp.equals("")) {
                calloutMediator.setRequestXPath(xPathFactory.createSynapseXPath("mediator.callout.source.xpath_val", request, session));
            }
        }
    }

    String targetGroup = request.getParameter("targetgroup");
    if (targetGroup != null && !targetGroup.equals("")) {
        if (targetGroup.equals("Key")) {
            targetVal = request.getParameter("mediator.callout.target.key_val");
            if (targetVal != null && !targetVal.equals("")) {
                calloutMediator.setTargetKey(targetVal);
            }
        } else if (targetGroup.equals("XPath")) {
            targetExp = request.getParameter("mediator.callout.target.xpath_val");
            if (targetExp != null && !targetExp.equals("")) {
                calloutMediator.setTargetXPath(xPathFactory.createSynapseXPath("mediator.callout.target.xpath_val", request, session));
            }
        }
    }
%>

