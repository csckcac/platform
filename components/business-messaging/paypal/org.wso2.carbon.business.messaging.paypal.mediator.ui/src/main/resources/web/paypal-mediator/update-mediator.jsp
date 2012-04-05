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

<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationInputFactory"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationFactory"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Output"%><%--
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

<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.io.Writer"%>
<%@page import="java.io.File"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Input"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.RequestCredential"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
	
    String param = null;
	String groupParam = null;
	String groupParamVal = null;
    String groupParamExp = null;
   // SynapseXPath xpath = null;
    XPathFactory xPathFactory = XPathFactory.getInstance();
    
    if (!(mediator instanceof PaypalMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PaypalMediator paypalMediator = (PaypalMediator) mediator;
    RequestCredential requestCredentials = new RequestCredential();
    Operation operation = OperationFactory.getInstance().create(request.getParameter("mediator.paypal.operation_name"));
    paypalMediator.setOperation(operation);
	paypalMediator.setRequestCredential(requestCredentials);  
    
	param = request.getParameter("mediator.paypal.repo");
    if (param != null && !param.equals("")) {
    	paypalMediator.setClientRepository(param);
    }
    param = request.getParameter("mediator.paypal.axis2XML");
    if (param != null && !param.equals("")) {
    	paypalMediator.setAxis2xml(param);
    }

    groupParam = request.getParameter("usernamegroup");
    if (groupParam != null && !groupParam.equals("")){
    	if (groupParam.equals("Key")) {
        	groupParamVal = request.getParameter("mediator.paypal.username.key_val");
        	if (groupParamVal != null && !groupParamVal.equals("")) {
            	requestCredentials.setUsernameValue(groupParamVal);
            }
        } else if (groupParam.equals("XPath")) {
        	groupParamExp = request.getParameter("mediator.paypal.username.xpath_val");
            if (groupParamExp != null && !groupParamExp.equals("")) {
            	requestCredentials.setUsernameXPath(xPathFactory.createSynapseXPath("mediator.paypal.username.xpath_val", request, session));
            }
        }
    }

    groupParam = request.getParameter("passwordgroup");
    if (groupParam != null && !groupParam.equals("")){
        if (groupParam.equals("Key")) {
        	groupParamVal = request.getParameter("mediator.paypal.password.key_val");
            if (groupParamVal != null && !groupParamVal.equals("")) {
            	requestCredentials.setPasswordValue(groupParamVal);
            }
        } else if (groupParam.equals("XPath")) {
        	groupParamExp = request.getParameter("mediator.paypal.password.xpath_val");
            if (groupParamExp != null && !groupParamExp.equals("")) {
            	requestCredentials.setPasswordXPath(xPathFactory.createSynapseXPath("mediator.paypal.password.xpath_val", request, session));
            }
        }
    }
    
    groupParam = request.getParameter("signaturegroup");
    if (groupParam != null && !groupParam.equals("")){
        if (groupParam.equals("Key")) {
        	groupParamVal = request.getParameter("mediator.paypal.signature.key_val");
            if (groupParamVal != null && !groupParamVal.equals("")) {
            	requestCredentials.setSignatureValue(groupParamVal);
            }
        } else if (groupParam.equals("XPath")) {
        	groupParamExp = request.getParameter("mediator.paypal.signature.xpath_val");
            if (groupParamExp != null && !groupParamExp.equals("")) {
            	requestCredentials.setSignatureXPath(xPathFactory.createSynapseXPath("mediator.paypal.signature.xpath_val", request, session));
            }
        }
    }
    
%>

<%
	
	String inputCountParameter = request.getParameter("inputCount");
    List<Input> inputList = new ArrayList<Input>();
	if (inputCountParameter != null && !"".equals(inputCountParameter)) {
    	int inputCount = 0;
    	try {
    		inputCount = Integer.parseInt(inputCountParameter.trim());
    		for (int i = 0; i < inputCount; i++) {
            	String name = request.getParameter("inputName_hidden" + i);
  
	          	if (name != null && !"".equals(name)) {
                	String type =  request.getParameter("inputType" + i);
                	String value = request.getParameter("inputValue" + i);
                	String expression = request.getParameter("inputTypeSelection" + i);
                	boolean isExpression = expression != null && "expression".equals(expression.trim());
                	
                	Input input = new Input();
                	input.setName(name.trim());
                	if(type != null && !"".equals(type)) {
                		input.setType(type);
                	}
                	
                	if (value != null) {
                		if("".equals(value)) {
                			value = "?";
                		}
                    	if (isExpression) {
                    		input.setSourceXPath(xPathFactory.createSynapseXPath("inputValue" + i, value.trim(), session));
                    	} else {
                    		input.setSourceValue(value.trim());
                    	}
                	}
                	inputList.add(input);
           	 	}
        	}
        	OperationInputFactory.getInstance().populateInputs(operation, inputList);
    	} catch (NumberFormatException ignored) {
        	throw new RuntimeException("Invalid number format");
    	}
	}
%>

<%	
	 String outputCountParameter = request.getParameter("outputCount");
	    if (outputCountParameter != null && !"".equals(outputCountParameter)) {
	        int outputCount = 0;
	        try {
	        	outputCount = Integer.parseInt(outputCountParameter.trim());
	            for (int i = 0; i < outputCount; i++) {
	                String name = request.getParameter("outputName" + i);

	                if (name != null && !"".equals(name)) {
	                    String targetValueId = "outputValue" + i;
	                    String targetValue = request.getParameter(targetValueId);
	                    
	                    String sourceValueId = "outputSourceExpression" + i;
	                    String sourceExpressionValue = request.getParameter(sourceValueId);
	                    
	                    String expression = request.getParameter("outputTypeSelection" + i);
	                    boolean isExpression = expression != null && "expression".equals(expression.trim());
	                   
						Output output = new Output();
	                    
						if (targetValue != null) {
	                        if (isExpression) {
	                        	output.setTargetXPath(xPathFactory.createSynapseXPath(targetValueId, targetValue.trim(), session));
	                        	output.setSourceXPath(xPathFactory.createSynapseXPath(sourceValueId, sourceExpressionValue.trim(), session));
	                        } else {
	                        	output.setTargetKey(targetValue.trim());
	                        }
	                    }
						
						operation.addOutput(output);
	                }
	            }
	        } catch (NumberFormatException ignored) {
	            throw new RuntimeException("Invalid number format");
	        }
	    }
%>
