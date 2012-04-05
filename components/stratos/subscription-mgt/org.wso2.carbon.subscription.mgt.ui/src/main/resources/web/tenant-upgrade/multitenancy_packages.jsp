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
<%@ page import="org.wso2.carbon.upgrade.stub.beans.xsd.PackageInfoBean" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.upgrade.ui.utils.UpgradeUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.upgrade.ui.i18n.JSResources"
		request="<%=request%>" />



<fmt:bundle basename="org.wso2.carbon.upgrade.ui.i18n.Resources">
<carbon:breadcrumb
            label="packages.info"
            resourceBundle="org.wso2.carbon.upgrade.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/register_config.js"></script>
<link href="../tenant-upgrade/css/upgrades.css" rel="stylesheet" type="text/css" media="all"/>

<%
    PackageInfoBean[] packageInfoBeans = UpgradeUtil.getPackageInfo(config, session);
%>
 
<div id="middle">

    <h2><fmt:message key="packages.info"/></h2>

    <div id="workArea">

        <form id="choosePackage" action="upgrade_subscription.jsp" method="post">

        <table class="styledLeft">
		<thead>
		<tr>
		<th>
			<fmt:message key="available.packages"/>
		</th>
		</tr>
		</thead>
        <tbody>
		<tr>
		<td class="nopadding">
		<table class="normal-nopadding" cellspacing="0">
		<tbody>
             <%
                 for (PackageInfoBean packageInfoBean: packageInfoBeans) {
                    String name = packageInfoBean.getName();
                    boolean currentPackage = packageInfoBean.getCurrentPackage();
                    String subscriptionPerUserFee = packageInfoBean.getSubscriptionPerUserFee();
                    int userLimit  = packageInfoBean.getUserLimit();
                    long resourceVolumeLimit = packageInfoBean.getResourceVolumeLimit();
                    long bandwidthLimit = packageInfoBean.getBandwidthLimit();
                    String overuseCharge = packageInfoBean.getOveruseCharge();

                    String checkedString = currentPackage ? " checked='true'":"";

             %>
                <tr class="packagerow">
                    <td style="border-bottom:1px solid #ccc !important" class="packageCol">
                        <input <%=checkedString%> type="radio" name="packageInfo" value="<%=name%>"/><%=name%> 
                    </td>
                    <td style="border-bottom:1px solid #ccc !important" class="packageCol" colspan="2">
                        <ul>
                        <li>
                            User Limit: <%=userLimit==-1?"unlimited":userLimit%>
                        </li>
                        <li>
                            Resource Volume Limit (Per User): <%=resourceVolumeLimit==-1?"unlimited":resourceVolumeLimit%> Mb
                        </li>
                        <li>
                            Bandwidth Limit (Per User): <%=bandwidthLimit==-1?"unlimited":bandwidthLimit%> Gb
                        </li>
                        <li>
                            Subscription Fee (Per User): <%=subscriptionPerUserFee%>
                        </li>
                        <li>
                            Overuse Charge (Per User): <%=overuseCharge%>
                        </li>
                        </ul>
                    </td>
                </tr>

        <%
            }
        %>

                <tr>
                    <td colspan="3">
                        <input type="submit" value="Select Package"/>
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

