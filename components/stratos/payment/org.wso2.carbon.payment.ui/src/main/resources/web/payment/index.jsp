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

<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.payment.ui.client.PaymentServiceClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>



<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    System.out.println("Server Url: " + serverURL);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    PaymentServiceClient paymentService = new PaymentServiceClient(configContext, serverURL, cookie);
    //String successUrl = "https://10.100.0.30:9443/carbon/payment/success.jsp";
    //String cancelUrl = "https://10.100.0.30:9443/carbon/payment/index.jsp";
    String amount = "1.75";
%>


<fmt:bundle basename="org.wso2.carbon.payment.ui.i18n.Resources">

<carbon:breadcrumb
        label="payment"
        resourceBundle="org.wso2.carbon.payment.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">

    function setExpressCheckout() {
        var successUrl = document.startPaymentForm.successUrl.value;
        var cancelUrl = document.startPaymentForm.cancelUrl.value;
        var amount = document.startPaymentForm.amount.value;

        $.ajax({
            type: 'POST',
            url: 'setEC-ajaxprocessor.jsp',
            data: 'successUrl=' + successUrl + '&cancelUrl=' + cancelUrl + '&amount=' + amount,
            success: function(msg) {
                var resp = eval('(' + msg + ')');
                if(resp.ack=='Success'){
                    location.href = '<fmt:message key="payment.paypal.sandbox"/>' + resp.token;
                }
            }
        });
    }

</script>

<form action="setEC-ajaxprocessor.jsp" name="startPaymentForm">
    <input type="hidden" name="successUrl" id="successUrl" value="<fmt:message key="payment.successUrl"/>"/>
    <input type="hidden" name="cancelUrl" id="cancelUrl" value="<fmt:message key="payment.cancelUrl"/>"/>
    <input type="hidden" name="amount" id="amount" value="<%=amount%>"/>
    <h1>Hello... Lets pay your bill</h1>

    <a href="javascript:setExpressCheckout();">Pay</a>
</form>

</fmt:bundle>