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

<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<%
    String forwardTo = null;
    String attributeDesignatorElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeDesignatorElementId"));
    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));

    if(attributeDesignatorElementId != null && !attributeDesignatorElementId.trim().equals("") &&
            !attributeDesignatorElementId.trim().equals("null")){
        entitlementPolicyBean.removeAttributeDesignatorElement(Integer.
                parseInt(attributeDesignatorElementId));
    }
    forwardTo = "add-apply.jsp?applyElementId=" + applyElementId;
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
