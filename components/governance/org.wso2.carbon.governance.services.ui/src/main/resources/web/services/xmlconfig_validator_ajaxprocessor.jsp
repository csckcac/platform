<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.wso2.carbon.governance.services.stub.AddServicesServiceStub" %>
<%@ page import="org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient" %>

<%
    String schema = request.getParameter("schema");
    String xml = request.getParameter("target_xml");
    AddServicesServiceClient client = new AddServicesServiceClient(config,session);
    boolean validated = client.validateXMLConfigOnSchema(xml,schema);
    if(validated) {
        %>---XMLSchemaValidated----<%
    } else {
        %>XML Schema Violated. XML should follow the schema definition..!!!<%
    }
%>