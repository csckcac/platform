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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui"/>
<script type="text/javascript" src="../info/js/info.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">

<%
if (request.getParameter("path") != null) {
%>
    <div class="box1-head" style="height:auto;">
	    <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
	        <tr>
	
	
	            <td valign="top"><h2><fmt:message key="subscriptions"/></h2></td>
	
	
	            <td align="right" valign="top" class="expanIconCell">
	
	
	                <a onclick="showSubscription();">
	                    <img src="images/icon-expanded.gif" border="0" align="top"
	                         id="subscriptionIconExpanded" style="display:none;" />
	                    <img src="images/icon-minimized.gif" border="0" align="top"
	                         id="subscriptionIconMinimized" />
	                </a>
	
	            </td>
	
	        </tr>
	    </table>
    </div>

<%
        String subscriptionPath = "../info/subscription-ajaxprocessor.jsp?path=" + request.getParameter("path").replaceAll("&","%26");
%>
    <div class="box1-mid-fill" id="subscriptionMinimized"></div>
    <div class="box1-mid" id="subscriptionExpanded" style="display:none;">

        <div id="subscriptionDiv">
            <jsp:include page="<%=subscriptionPath%>"/>
        </div>

    </div>
<%
}
%>
</fmt:bundle>
