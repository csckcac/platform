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

<%@ page import="org.wso2.carbon.mediator.filter.FilterMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.mediator.filter.ui.i18n.Resources">

<%
    try {
        String regEx;
        String param;
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof FilterMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        FilterMediator filterMediator = (FilterMediator) mediator;
        param = request.getParameter("mediator.filter.type");
        if (param != null && !param.equals("")) {
            if (param.equals("xpathRegx")) {
                filterMediator.setXpath(null);
                regEx = request.getParameter("mediator.filter.regex_val");
                if (regEx != null && !regEx.equals("")) {
                    filterMediator.setRegex(Pattern.compile(regEx));
                }
                XPathFactory xPathFactory = XPathFactory.getInstance();
                filterMediator.setSource(xPathFactory.createSynapseXPath("mediator.filter.source_val", request, session));
            } else if (param.equals("xpath")) {
                filterMediator.setRegex(null);
                filterMediator.setSource(null);
                XPathFactory xPathFactory = XPathFactory.getInstance();
                filterMediator.setXpath(xPathFactory.createSynapseXPath("mediator.filter.xpath_val", request, session));
            }
        }
    } catch (Exception e) {
%>
    CARBON.showErrorDialog("<fmt:message key="mediator.filter.update.error"/>");
    <%
        }
    %>
</fmt:bundle>