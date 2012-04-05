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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.CallTemplateMediator" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.util.Value" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.util.CallUtil" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%
    try {
//        System.out.println("calltemplate update");

        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof CallTemplateMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        CallTemplateMediator callTemplateMediator = (CallTemplateMediator) mediator;

        XPathFactory xPathFactory = XPathFactory.getInstance();


        callTemplateMediator.setTargetTemplate(request.getParameter("mediator.call.target"));

        String propertyCountParameter = request.getParameter("propertyCount");
//        System.out.println("count : " + propertyCountParameter);
        if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(propertyCountParameter.trim());
                callTemplateMediator.getpName2ExpressionMap().clear();
                for (int i = 0; i <= propertyCount; i++) {
                    String name = request.getParameter("propertyName" + i);
//                    System.out.println("propertyName : " + name);
                    if (name != null && !"".equals(name)) {
                        String valueId = "propertyValue" + i;
                        String dynamicXpath = "dynamicCheckbox" + i;
                        String value = request.getParameter(valueId);
                        String[] isDynamic = request.getParameterValues(dynamicXpath);
                        String isDynamicXpathEnabled = request.getParameter(dynamicXpath);
//                        System.out.println("xpath dynamic : " + isDynamic);
                        String expression = request.getParameter("propertyTypeSelection" + i);
                        boolean isExpression = expression != null && "expression".equals(expression.trim());
                        Value paramValue = null;
                        if (value != null) {
                            if (isExpression) {
//                                System.out.println("is Expression");
                                SynapseXPath xpath = xPathFactory.createSynapseXPath(valueId, value.trim(), session);
//                                if(isDynamic != null && isDynamic.length > 0 && "true".equals(isDynamic[0])){
                                if(isDynamicXpathEnabled != null && "true".equals(isDynamicXpathEnabled)){
                                    paramValue = new Value("{" + value + "}");
                                    CallUtil.addNamespacesTo(paramValue, xpath);
//                                    System.out.println("dynamic xpath : value --> " + paramValue);
                                }else{
//                                    System.out.println("static xpath : value --> " + paramValue);
                                    paramValue = new Value(xpath);
                                }
                            } else if (value != null) {
//                                System.out.println("is plain Value");
                                paramValue = new Value(value);
                            }
                        }
                        if (paramValue != null) {
//                            System.out.println("adding paramValue ");
                            callTemplateMediator.addExpressionForParamName(name.trim(), paramValue);
                        }
                    }
//                    System.out.println("----------------");
                }

            } catch (NumberFormatException ignored) {
            }
        }

%>
<%
} catch (Exception e) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : " + '<%=e.getMessage()%>');
    });
</script>
<%
        return;
    }
%>
