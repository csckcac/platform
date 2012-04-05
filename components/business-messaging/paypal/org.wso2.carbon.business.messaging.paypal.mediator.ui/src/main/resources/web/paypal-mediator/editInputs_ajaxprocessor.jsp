<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@page import="org.apache.synapse.util.xpath.SynapseXPath"%>

<%@page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@page	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator"%>
<%@page	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationFactory"%>
<%@page	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation"%>
<%@page	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Input"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Queue"%>
<%@page import="java.util.LinkedList"%>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp" />
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<% 


	Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

	if (!(mediator instanceof PaypalMediator)) {
	    // todo : proper error handling
	    throw new RuntimeException("Unable to edit the mediator");
	}
	PaypalMediator paypalMediator = (PaypalMediator) mediator;
	Operation operation = paypalMediator.getOperation();
		
	if( null == operation || !request.getParameter("operationName").equals(operation.getName())) {
	 	OperationFactory factory = OperationFactory.getInstance();
   	   	operation = factory.create(request.getParameter("operationName"));
    }
  	List<Input> inputs = operation.getInputs();
  	
  	 NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
     
     for(int i = 0; i < inputs.size(); i++) {
     	
     	if(null != inputs.get(i).getSourceXPath()) {
     		nameSpacesRegistrar.registerNameSpaces(inputs.get(i).getSourceXPath(), "inputValue" + i, session);
     	}
     	
     }
     
	String inputTableStyle = inputs.isEmpty() ? "display:none;" : "";

%>
<fmt:bundle
	basename="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="paypalMediatorJsi18n" />
<div>
	<script type="text/javascript"
		src="../paypal-mediator/js/mediator-util.js"></script>
	<script type="text/javascript" src="../resources/js/resource_util.js"></script>
	<script type="text/javascript" src="../ajax/js/prototype.js"></script>

	<table id="inputstable" style="<%=inputTableStyle%>;"
		class="styledInner">
		<thead>
			<tr>
				<th width="25%"><fmt:message key="mediator.paypal.inputName" /></th>
				<th width="25%"><fmt:message key="mediator.paypal.inputValue" /></th>
				<th width="25%"><fmt:message key="mediator.paypal.inputExp" /></th>
				<th id="ns-edior-th" style="display: none;"><fmt:message
					key="mediator.paypal.nsEditor" /></th>
			</tr>
		</thead>
		<tbody id="inputtbody">
		<%
     	int i = 0;
        Queue<Input> inputStack = new LinkedList<Input>();
                        	
        for (Input input : inputs) {
                                	
		if (input != null) {
        	inputStack.offer(input);
                                		
            while(!inputStack.isEmpty()) {
                                			
            	Input currentInput = inputStack.poll();

                if(currentInput.getType() != null) {
                    //for complex types with values required
                    /*if(currentInput.getName() != null){
                        inputStack.offer(currentInput);        
                    }*/
                	for(Input subInput: currentInput.getSubInputs()) {
                    	subInput.setType(currentInput.getType());
                        inputStack.offer(subInput);
                    }
                }
                if(currentInput.getName() != null){
                	String inputName = currentInput.getName() ;
                    String inputType = currentInput.getType();
                    boolean isRequired = currentInput.isRequired();
                    String value = currentInput.getSourceValue();
                    SynapseXPath synapseXPath = currentInput.getSourceXPath();
                  //  boolean isLiteral = value == null || value != null && !"".equals(value); 
                    boolean isLiteral = value != null && !"".equals(value); 
		%>
			<tr id="inputRaw<%=i%>">
				<td>
					<%=inputName%> 
				<%	if(isRequired) { %>
					<span class="required">*</span>
				<%  } %>
					<input type="hidden" name="inputName_hidden<%=i%>" id="inputName_hidden<%=i%>" value="<%=inputName%>" />
					<input type="hidden" name="inputRequired_hidden<%=i%>" id="inputRequired_hidden<%=i%>" value="<%=isRequired%>" />
					<input type="hidden" name="inputType<%=i%>" id="inputType<%=i%>" value="<%= null == inputType ? ""  : inputType %>" />
				</td>
				<td>
					<select class="esb-edit small_textbox"
						name="inputTypeSelection<%=i%>" id="inputTypeSelection<%=i%>"
						onchange="onInputTypeSelectionChange('<%=i%>','<fmt:message key="mediator.paypal.namespace"/>')">
				<%  if (isLiteral) {%>
						<option value="literal"><fmt:message
							key="mediator.paypal.value" /></option>
						<option value="expression"><fmt:message
							key="mediator.paypal.expression" /></option>
				<%} else if (synapseXPath != null) {%>
						<option value="expression"><fmt:message
							key="mediator.paypal.expression" /></option>
						<option value="literal"><fmt:message
							key="mediator.paypal.value" /></option>
				<%} else { %>
						<option value="literal"><fmt:message
							key="mediator.paypal.value" /></option>
						<option value="expression"><fmt:message
							key="mediator.paypal.expression" /></option>	
				<%  }%>
					</select>
				</td>
				<td>
				<%  if (isLiteral) {%> 
					<input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
						value="<%=value%>" class="esb-edit" /> 
				<%  }else if (synapseXPath != null) {%>
					<input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
						value="<%=synapseXPath.toString()%>" class="esb-edit" /> 
				<%  }else { %>
					<input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
						class="esb-edit" /> 
				<%  }%>
				</td>
				<td id="nsEditorButtonTD<%=i%>"
					style="<%=synapseXPath == null?"display:none;":""%>">
				<%  if (!isLiteral && synapseXPath != null) {
					System.out.println("Executed inside...");
				%> 
					<script type="text/javascript">
                       	document.getElementById("ns-edior-th").style.display = "";
					</script> 
                    <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
					   onclick="showNameSpaceEditor('inputValue<%=i%>')"> 
						<fmt:message key="mediator.paypal.namespace" /> 
					</a> 	
				<%  }%>
				</td>
			</tr>
			<% 	
                    i++;
				}// end else
             }//end while
           }//end if
        } // end for
		inputStack.clear();
		%>
			<tr style="display: none;">
				<td colspan="4">
					<input type="hidden" name="inputCount" id="inputCount" value="<%=i%>" />
					<script type="text/javascript">
						if (isRemainPropertyExpressions()) {
                           	resetDisplayStyle("");
                        }  
                    </script>
				</td>
			</tr>
		</tbody>
	</table>
	
	<a name="nsEditorLink"></a>
	<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>