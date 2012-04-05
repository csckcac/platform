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
<%@page
	import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@page
	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator"%>
<%@page
	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationFactory"%>
<%@page
	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation"%>
<%@page
	import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Output"%>

<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp" />
<% 


	Mediator mediator1 = SequenceEditorHelper.getEditingMediator(request, session);

	if (!(mediator1 instanceof PaypalMediator)) {
	    // todo : proper error handling
	    throw new RuntimeException("Unable to edit the mediator");
	}
	PaypalMediator paypalMediator1 = (PaypalMediator) mediator1;
	Operation operation1 = paypalMediator1.getOperation();
	
  	List<Output> outputs = new ArrayList<Output>();
	
	if( null != operation1 && operation1.getName().equals(request.getParameter("operationName"))) {
		outputs = operation1.getOutputs();
	}
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    
    for(int i = 0; i < outputs.size(); i++) {
    	
    	if(null != outputs.get(i).getTargetXPath()) {
    		nameSpacesRegistrar.registerNameSpaces(outputs.get(i).getTargetXPath(), "outputValue" + i, session);

    		if(null != outputs.get(i).getSourceXPath()) {
    			nameSpacesRegistrar.registerNameSpaces(outputs.get(i).getSourceXPath(), "outputSourceExpression" + i, session);
    		}
    	}
    	
    }
	String outputTableStyle = outputs.isEmpty() ? "display:none;" : "";

%>
<fmt:bundle
	basename="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="paypalMediatorJsi18n" />
	<script type="text/javascript"
		src="../paypal-mediator/js/mediator-util.js"></script>
	<script type="text/javascript" src="../resources/js/resource_util.js"></script>
    
	<table id="outputstable" style="<%=outputTableStyle%>;" class="styledInner">
		<thead>
			<tr>
				<th width="15%" style="display: none;"><fmt:message key="mediator.paypal.outputName" /></th>
				<th width="10%"><fmt:message key="mediator.paypal.outputValue" /></th>
				<th width="15%"><fmt:message key="mediator.paypal.outputTargetExp" /></th>
				<th id="output-ns-editor-th" style="display: none;" width="15%"><fmt:message key="mediator.paypal.nsEditor" /></th>
				<th id="output-source-expression"   style="display: none;" width="15%"><fmt:message key="mediator.paypal.outputSourceExp" /></th>
				<th id="output-source-ns-editor-th" style="display: none;" width="15%"><fmt:message key="mediator.paypal.nsEditor" /></th>	
				<th><fmt:message key="mediator.paypal.action" /></th>
			</tr>	
		</thead>
		<tbody id="outputtbody">
		<%
		int i = 0;
		for (Output mp : outputs) {
        	if (mp != null) {
            	String value = mp.getTargetKey();
                SynapseXPath synapseTargetXPath = mp.getTargetXPath();
                SynapseXPath synapseSourceXPath = mp.getSourceXPath();
                boolean isLiteral = value != null && !"".equals(value);
		%>
			<tr id="outputRaw<%=i%>">
				<td style="display: none;">
					<input type="text" name="outputName<%=i%>" id="outputName<%=i%>" class="esb-edit small_textbox" value="outputName<%=i%>" />
				</td>
				<td>
					<select class="esb-edit small_textbox" name="outputTypeSelection<%=i%>" id="outputTypeSelection<%=i%>"
								onchange="onOutputTypeSelectionChange('<%=i%>','<fmt:message key="mediator.paypal.namespace"/>')">
			<%	if (isLiteral) {%>
						<option value="literal" selected="selected"><fmt:message
							key="mediator.paypal.value" /></option>
						<option value="expression"><fmt:message
							key="mediator.paypal.expression" /></option>
			<% 	} else if (synapseTargetXPath != null) {%>
						<option value="literal"><fmt:message
							key="mediator.paypal.value" /></option>
						<option value="expression" selected="selected"><fmt:message
							key="mediator.paypal.expression" /></option>
			<% 	} else { %>
						<option value="literal"><fmt:message
							key="mediator.paypal.value" /></option>
						<option value="expression"><fmt:message
							key="mediator.paypal.expression" /></option>
			<% 	}%>
					</select>
				</td>
				<td>
			<% 	if (value != null && !"".equals(value)) {%> 
					<input id="outputValue<%=i%>" name="outputValue<%=i%>" type="text" value="<%=value%>" class="esb-edit" /> 
			<% 	} else if (synapseTargetXPath != null) {%> 
					<input id="outputValue<%=i%>" name="outputValue<%=i%>" type="text" value="<%=synapseTargetXPath.toString()%>" class="esb-edit" /> 
			<% 	} else { %> 
					<input id="outputValue<%=i%>" name="outputValue<%=i%>" type="text" class="esb-edit" /> 
			<% 	}%>
				</td>
				<td id="outputnsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
			<% 	if (!isLiteral && synapseTargetXPath != null) {%> 
					<script type="text/javascript">
                    	document.getElementById("output-ns-editor-th").style.display = "";
                    </script> 
                    <a href="#nsoutputEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
						onclick="showNameSpaceEditor('outputValue<%=i%>')"> 
							<fmt:message key="mediator.paypal.namespace" />
					</a>
			<% 	}%>
				</td>
				<td id="outputSourceExpressionTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
			<% 	if(!isLiteral) { 
			   		if(synapseSourceXPath != null) {%>
					<script type="text/javascript">
        	        	document.getElementById("output-source-expression").style.display = "";
            	    </script>
					<input id="outputSourceExpression<%=i%>" name="outputSourceExpression<%=i%>" type="text" value="<%=synapseSourceXPath.toString()%>" class="esb-edit" /> 
				<%  } else { %> 
					<input id="outputSourceExpression<%=i%>" name="outputSourceExpression<%=i%>" type="text" class="esb-edit" /> 
				<%  }
				} %>
				</td>
				<td id="outputSourcensEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
			<% 	if (!isLiteral && synapseSourceXPath != null) {%> 
					<script type="text/javascript">
                    	document.getElementById("output-source-ns-editor-th").style.display = "";
                    </script> 
                    <a href="#nsoutputsourceEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
						onclick="showNameSpaceEditor('outputSourceExpression<%=i%>')">
							<fmt:message key="mediator.paypal.namespace" />
					</a>
			<%	}%>
				</td>		
				<td>
					<a href="#" class="delete-icon-link" onclick="deleteOutput(<%=i%>);return false;">
						<fmt:message key="mediator.paypal.delete" />
					</a>
				</td>
			</tr>
		<%  }
       		i++;
        } %>
            <tr style="display: none;">
				<td colspan="7"> 
					<input type="hidden" name="outputCount" id="outputCount" value="<%=i%>" />
					<script type="text/javascript">
            	        if (isRemainOutputExpressions()) {
        	                resetOutputDisplayStyle("");
                        }
                    </script>
                 </td>
            </tr>
		</tbody>
	</table>
			<div style="margin-top: 10px;"><a name="addNameLink"></a> 
				<a class="add-icon-link" href="#addNameLink"
					onclick="addOutput('<fmt:message key="mediator.paypal.namespace"/>')">
				 	<fmt:message key="mediator.paypal.addOutput" />
				</a>
			</div>

<a name="nsoutputEditorLink"></a>
<a name="nsoutputsourceEditorLink"></a>

<div id="nsoutputEditorLinkEditor" style="display:none;"></div>
<div id="nsoutputsourceEditorLinkEditor" style="display:none;"></div>
</fmt:bundle>