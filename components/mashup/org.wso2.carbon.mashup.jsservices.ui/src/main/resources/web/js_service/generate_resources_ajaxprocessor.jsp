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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpMethod" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils" %>
<%
    String serviceName = request.getParameter("serviceName");
    String url = request.getParameter("url");
    String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(url);
    String postFix;
    if(tenantDomain == null) {
        //non-multitenant login
        postFix = serviceName;
    } else {
        postFix = "t/" + tenantDomain + "/" + serviceName;
    }
    String type = request.getParameter("type");
    if ("gadget".equals(type)) {
        postFix = postFix + "?template&flavor=gadget";
    } else {
        postFix = postFix + "?template&flavor=html";
    }
    if (serviceName != null) {
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        HttpClient httpClient = new HttpClient();
        HttpMethod httpMethod = new GetMethod(adminConsoleURL.split(request.getContextPath()+"/carbon/")[0]+"/services/"+ postFix);
        httpClient.executeMethod(httpMethod);
        InputStream inputStream = httpMethod.getResponseBodyAsStream();
        int nextChar;
        while ((nextChar = inputStream.read()) != -1)
            out.write((char) nextChar);
        out.flush();
    }
%>