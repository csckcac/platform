<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!--
~ Copyright (c) 20012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%
    String id = request.getParameter("id");
    String mainBeanId = MainBeanUtil.generateMainBeanId(id);
    /* Get the MainBean from the session */
    MainBean bean = (MainBean) session.getAttribute(mainBeanId);
%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<span id="svg_table">
<%
    if (bean.getSvgDataModel().getSvg() != null) {
%>
        <%=java.text.MessageFormat.format(bean.getSvgDataModel().getSvg().getHtml(), id)%>
<%
    }
%>
</span>
</fmt:bundle>