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
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%@ page import="org.wso2.carbon.upgrade.ui.utils.UpgradeUtil" %>
<%@ page import="java.awt.image.BufferedImage" %>
<%@ page import="java.io.File" %>
<%@ page import="javax.imageio.ImageIO" %>
<%@ page import="java.awt.image.RenderedImage" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.UUID" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.upgrade.stub.beans.xsd.SubscriptionInfoBean" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.upgrade.ui.i18n.JSResources"
		request="<%=request%>" />



<fmt:bundle basename="org.wso2.carbon.upgrade.ui.i18n.Resources">
<carbon:breadcrumb
            label="upgrade.subscription.menu"
            resourceBundle="org.wso2.carbon.upgrade.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/register_config.js"></script>
<%


    SubscriptionInfoBean subscriptionInfoBean = UpgradeUtil.getCurrentSubscription(config, session);
    String newPackageName = request.getParameter("packageInfo");
%>
 
<div id="middle">

    <h2><fmt:message key="upgrade.subscription"/></h2>

    <div id="workArea">

        <form id="upgradePackageForm" action="upgrade_subscription_ajaxprocessor.jsp" method="post">

        <table class="styledLeft">
		<thead>
		<tr>
		<th>
			<fmt:message key="current.subscription.info"/>
		</th>
		</tr>
		</thead>
        <tbody>
		<tr>
		<td class="nopadding">
		<table class="normal-nopadding" cellspacing="0">
		<tbody>
            <%
                if (subscriptionInfoBean == null) {
            %>
                <tr>
                    <td colspan="3">
                        You currently don't have a subscription for a paid package.
                    </td>
                </tr>
            <%
                }
                else {
                    String packageName = subscriptionInfoBean.getPackageName();
                    Date activeSince = subscriptionInfoBean.getActiveSince();
                    Date activeUntil = subscriptionInfoBean.getActiveUntil();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            %>

                <tr>
                    <td ><fmt:message key="current.package.name"/></td>
                    <td colspan="2">
                        <%=packageName%>
                    </td>
                </tr>

                <tr>
                    <td ><fmt:message key="active.since"/></td>
                    <td colspan="2">
                        <%=dateFormat.format(activeSince)%>
                    </td>
                </tr>
                <tr>
                    <td ><fmt:message key="active.util"/></td>
                    <td colspan="2">
                        <%=dateFormat.format(activeUntil)%> (If the subscription is changed, the current subscription
                                    will be automatically deactivated).
                    </td>
                </tr>
              <%
                  }
              %>
                <tr>
                    <td colspan="3" class="middle-header"><fmt:message key="new.subscription.info"/></td>
                </tr>

                <tr>
            <%
                if (subscriptionInfoBean != null && subscriptionInfoBean.getPackageName().equals(newPackageName)) {
            %>
                    <td ><fmt:message key="extending.package.name"/></td>
            <%
                } else {
            %>
                    <td ><fmt:message key="new.package.name"/></td>
            <%
                }
            %>
                    <td colspan="2">
                        <%=newPackageName%> <input type="hidden" name="packageName" value="<%=newPackageName%>"/>
                    </td>
                </tr>

                <tr>
            <%
                if (subscriptionInfoBean != null && subscriptionInfoBean.getPackageName().equals(newPackageName)) {
            %>
                    <td ><fmt:message key="extending.subscription.duration"/></td>
            <%
                } else {
            %>
                    <td ><fmt:message key="subscription.duration"/></td>
            <%
                }
            %>
                    <td colspan="2">
                        <select name="duration">
                          <option value="1">1 Month</option>
                          <option value="6">6 Months</option>
                          <option value="12">1 Year</option>
                          <option value="24">2 Years</option>
                          <option value="36">3 Years</option>
                    <%
                        if (subscriptionInfoBean != null && subscriptionInfoBean.getPackageName().equals(newPackageName)) {
                    %>
                          <option value="0">Cancel subscription</option>
                    <%
                        }
                    %>
                        </select>

                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <input type="submit" value="Change Subscription"/>
                    </td>
                </tr>

		</tbody>
		</table>
		</td>
		</tr>
		</tbody>
                </table>
            </form>
    <br/>
        </div>
    </div>
 </fmt:bundle>

