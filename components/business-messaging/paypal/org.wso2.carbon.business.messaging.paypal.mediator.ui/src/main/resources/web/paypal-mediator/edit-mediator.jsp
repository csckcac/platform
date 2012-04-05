<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@page import="org.apache.synapse.util.xpath.SynapseXPath"%>

<%@page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator" %>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.factory.OperationFactory"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation"%>
<%@page import="org.wso2.carbon.business.messaging.paypal.mediator.ui.Input"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Queue"%>
<%@page import="java.util.LinkedList"%>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<script type="">
YAHOO.util.Event.onDOMReady(function() {
 	var selectElem = document.getElementById("mediator.paypal.operation_name");
	loadConfigedInputs(selectElem[selectElem.selectedIndex].value); 
	loadConfigedOutputs(selectElem[selectElem.selectedIndex].value);  
});
</script>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
    String 	usernameVal = "",
    		passwordVal = "",
    		signatureVal = "";
    boolean isUsernameXpath = false,
    		isPasswordXpath = false,
    		isSignatureXpath = false;
    String 	repo = "", 
    		axis2XML = "";
    if (!(mediator instanceof PaypalMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PaypalMediator paypalMediator = (PaypalMediator) mediator;

    if (paypalMediator.getClientRepository() != null) {
        repo = paypalMediator.getClientRepository();
    }
    if (paypalMediator.getAxis2xml() != null) {
        axis2XML = paypalMediator.getAxis2xml();
    }

	if(null != paypalMediator.getRequestCredential()) {
    	if (paypalMediator.getRequestCredential().getUsernameXPath() != null) {
	 		isUsernameXpath = true;
		 	usernameVal = paypalMediator.getRequestCredential().getUsernameXPath().toString();
	        nmspRegistrar.registerNameSpaces(paypalMediator.getRequestCredential().getUsernameXPath(), "mediator.paypal.username.xpath_val", session);
	    } else if (paypalMediator.getRequestCredential().getUsernameValue() != null) {
	    	isUsernameXpath = false;
		 	usernameVal = paypalMediator.getRequestCredential().getUsernameValue();
	    }
	    
	    if (paypalMediator.getRequestCredential().getPasswordXPath() != null) {
		 	isPasswordXpath = true;
		 	passwordVal = paypalMediator.getRequestCredential().getPasswordXPath().toString();
	        nmspRegistrar.registerNameSpaces(paypalMediator.getRequestCredential().getPasswordXPath(), "mediator.paypal.password.xpath_val", session);
	    } else if (paypalMediator.getRequestCredential().getPasswordValue() != null) {
	    	isPasswordXpath = false;
	    	passwordVal = paypalMediator.getRequestCredential().getPasswordValue();
	    }
	    
	    if (paypalMediator.getRequestCredential().getSignatureXPath() != null) {
		 	isSignatureXpath = true;
		 	signatureVal = paypalMediator.getRequestCredential().getSignatureXPath().toString();
	        nmspRegistrar.registerNameSpaces(paypalMediator.getRequestCredential().getSignatureXPath(), "mediator.paypal.signature.xpath_val", session);
	    } else if (paypalMediator.getRequestCredential().getSignatureValue() != null) {
	    	isSignatureXpath = false;
	    	signatureVal = paypalMediator.getRequestCredential().getSignatureValue();
	    }
    }
/*
    if (calloutMediator.getTargetKey() != null) {
        isTargetXpath = false;
        targetVal = calloutMediator.getTargetKey();
    } else if (calloutMediator.getTargetXPath() != null){
        isTargetXpath = true;
        targetVal = calloutMediator.getTargetXPath().toString();
        nmspRegistrar.registerNameSpaces(calloutMediator.getTargetXPath(), "mediator.paypal.target.xpath_val", session);
    }
*/
%>

<fmt:bundle basename="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.business.messaging.paypal.mediator.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="paypalMediatorJsi18n"/>
<div>
    <script type="text/javascript" src="../paypal-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    
    <table class="normal" width="100%">
    <tr>
        <td>
            <h2><fmt:message key="mediator.paypal.header"/></h2>
        </td>
    </tr>
    
    <tr>
        <td>
        <h3 class="mediator"><fmt:message key="mediator.paypal.credentials"/></h3>
       <table border="0" class="normal">
        				<tr>
                			<td>
                			<fmt:message key="mediator.paypal.username"/> <span class="required">*</span>
        					<table class="normal">
                				<tr>
                    				<td class="leftCol-small">
                        				<fmt:message key="mediator.paypal.specifyas"/> :
                    				</td>
                    				<td>
                    					<input type="radio" id="usernameGroupXPath"
                               				onclick="javascript:displayElement('mediator.paypal.username.xpath', true); displayElement('mediator.paypal.username.namespace.editor', true); displayElement('mediator.paypal.username.key', false);"
                               				name="usernamegroup" <%=isUsernameXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>"/>
                        				<fmt:message key="mediator.paypal.xpath"/>
                        				<input type="radio"
                              				onclick="javascript:displayElement('mediator.paypal.username.xpath', false); javascript:displayElement('mediator.paypal.username.key', true); displayElement('mediator.paypal.username.namespace.editor', false);"
                               				name="usernamegroup" <%=!isUsernameXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        				<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td></td>
                				</tr>
                				<tr id="mediator.paypal.username.xpath" <%=!isUsernameXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.xpath"/>
                    				</td>
                    				<td>
                    					<input type="text" id="username_xpath" name="mediator.paypal.username.xpath_val" style="width:300px"
                               				id="mediator.paypal.username.xpath_val" value="<%=usernameVal%>"/>
                               		</td>
                    				<td>
                    					<a id="mediator.paypal.username.xpath_nmsp_button" href="#"
                           					onclick="showNameSpaceEditor('mediator.paypal.username.xpath_val')" class="nseditor-icon-link"
                                   			style="padding-left:40px">
                        					<fmt:message key="mediator.paypal.namespace"/>
                        				</a>
                    				</td>
                				</tr>
                				<tr id="mediator.paypal.username.key" <%=isUsernameXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td>
                    					<input type="text" name="mediator.paypal.username.key_val" style="width:300px"
                               					id="mediator.paypal.username.key_val" value="<%=usernameVal%>" />
                        				<input type="hidden" name="mediator.paypal.username.key_val_hidden" style="width:300px"
                               					id="mediator.paypal.username.key_val_hidden" value="<%=usernameVal%>"/>
                               		</td>
                    				<td>
                    				</td>
                				</tr>
        					</table>
                    		</td>
            			</tr>
            			
            			<tr>
                			<td>
                			<fmt:message key="mediator.paypal.password"/> <span class="required">*</span>
        					<table class="normal">
                				<tr>
                    				<td class="leftCol-small">
                        				<fmt:message key="mediator.paypal.specifyas"/> :
                    				</td>
                    				<td>
                        				<input type="radio" id="passwordGroupXPath"
                               				onclick="javascript:displayElement('mediator.paypal.password.xpath', true); displayElement('mediator.paypal.password.namespace.editor', true); displayElement('mediator.paypal.password.key', false);"
                               				name="passwordgroup" <%=isPasswordXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>"/>
                        				<fmt:message key="mediator.paypal.xpath"/>
                        				<input type="radio"
                              				onclick="javascript:displayElement('mediator.paypal.password.xpath', false); javascript:displayElement('mediator.paypal.password.key', true); displayElement('mediator.paypal.password.namespace.editor', false);"
                               				name="passwordgroup" <%=!isPasswordXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        				<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td></td>
                				</tr>
                				<tr id="mediator.paypal.password.xpath" <%=!isPasswordXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.xpath"/>
                    				</td>
                    				<td>
                    					<input type="text" id="password_xpath" name="mediator.paypal.password.xpath_val" style="width:300px"
                               				id="mediator.paypal.password.xpath_val" value="<%=passwordVal%>"/>
                               		</td>
                    				<td>
                    					<a id="mediator.paypal.password.xpath_nmsp_button" href="#"
                           					onclick="showNameSpaceEditor('mediator.paypal.password.xpath_val')" class="nseditor-icon-link"
                                   			style="padding-left:40px">
                        					<fmt:message key="mediator.paypal.namespace"/>
                        				</a>
                    				</td>
                				</tr>
                				<tr id="mediator.paypal.password.key" <%=isPasswordXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td>
                    					<input type="text" name="mediator.paypal.password.key_val" style="width:300px"
                               					id="mediator.paypal.password.key_val" value="<%=passwordVal%>" />
                        				<input type="hidden" name="mediator.paypal.password.key_val_hidden" style="width:300px"
                               					id="mediator.paypal.password.key_val_hidden" value="<%=passwordVal%>"/>
                               		</td>
                    				<td>
                    				</td>
                				</tr>
        					</table>
                    		</td>
            			</tr>
            			
            			<tr>
                			<td>
                			<fmt:message key="mediator.paypal.signature"/>
        					<table class="normal">
                				<tr>
                    				<td class="leftCol-small">
                        				<fmt:message key="mediator.paypal.specifyas"/> :
                    				</td>
                    				<td>
                        				<input type="radio" id="signatureGroupXPath"
                               				onclick="javascript:displayElement('mediator.paypal.signature.xpath', true); displayElement('mediator.paypal.signature.namespace.editor', true); displayElement('mediator.paypal.signature.key', false);"
                               				name="signaturegroup" <%=isSignatureXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>"/>
                        				<fmt:message key="mediator.paypal.xpath"/>
                        				<input type="radio"
                              				onclick="javascript:displayElement('mediator.paypal.signature.xpath', false); javascript:displayElement('mediator.paypal.signature.key', true); displayElement('mediator.paypal.signature.namespace.editor', false);"
                               				name="signaturegroup" <%=!isSignatureXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        				<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td></td>
                				</tr>
                				<tr id="mediator.paypal.signature.xpath" <%=!isSignatureXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.xpath"/>
                    				</td>
                    				<td>
                    					<input type="text" id="signature_xpath" name="mediator.paypal.signature.xpath_val" style="width:300px"
                               				id="mediator.paypal.signature.xpath_val" value="<%=signatureVal%>"/>
                               		</td>
                    				<td>
                    					<a id="mediator.paypal.signature.xpath_nmsp_button" href="#"
                           					onclick="showNameSpaceEditor('mediator.paypal.signature.xpath_val')" class="nseditor-icon-link"
                                   			style="padding-left:40px">
                        					<fmt:message key="mediator.paypal.namespace"/>
                        				</a>
                    				</td>
                				</tr>
                				<tr id="mediator.paypal.signature.key" <%=isSignatureXpath ? "style=\"display:none\";" : ""%>>
                    				<td>
                    					<fmt:message key="mediator.paypal.key"/>
                    				</td>
                    				<td>
                    					<input type="text" name="mediator.paypal.signature.key_val" style="width:300px"
                               					id="mediator.paypal.signature.key_val" value="<%=signatureVal%>" />
                        				<input type="hidden" name="mediator.paypal.signature.key_val_hidden" style="width:300px"
                               					id="mediator.paypal.signature.key_val_hidden" value="<%=signatureVal%>"/>
                               		</td>
                    				<td>
                    				</td>
                				</tr>
        					</table>
                    		</td>
            			</tr>
        			</table>
    	</td>
    </tr>
    <tr>
    	<td>
	    	<h3 class="mediator">
	                		<fmt:message key="mediator.paypal.operationDetails"/>
	                	</h3>
    		
               <%
               		Operation operation = paypalMediator.getOperation();
               		String selectedOperation = null;
               		if( null == operation ) {
               			selectedOperation = "GetBalance";
               		}else {
               			selectedOperation = operation.getName();
               		}
              %>
           	<table border="0" class="normal" width="100%">
			<tr>
           			<td class="leftCol-small"><fmt:message key="mediator.paypal.operationName"/></td>
	                <td>
       		            <select id="mediator.paypal.operation_name" name="mediator.paypal.operation_name" onchange="loadConfigedInputs(this[this.selectedIndex].value)" >
               		        <% 
               		        if("AddressVerify".equals(selectedOperation)) { 
               		        %>
                       		<option value="AddressVerify" selected="selected">Address Verify</option>
                       		<option value="BillUser">Bill User</option>
               		        <option value="GetBalance">Get Balance</option>
                       		<option value="GetPalDetails">Get PalDetails</option>
                       		<%
                       		} else if("BillUser".equals(selectedOperation)) { 
                       		%>
                       		<option value="AddressVerify">Address Verify</option>
                       		<option value="BillUser" selected="selected">Bill User</option>
               		        <option value="GetBalance">Get Balance</option>
                       		<option value="GetPalDetails">Get PalDetails</option>
                       		<%
                       		} else if("GetBalance".equals(selectedOperation)) { 
                           	%>
                       		<option value="AddressVerify">Address Verify</option>
                       		<option value="BillUser">Bill User</option>
               		        <option value="GetBalance" selected="selected">Get Balance</option>
                       		<option value="GetPalDetails">Get PalDetails</option>
                           	<%
                           	} else if("GetPalDetails".equals(selectedOperation)) { 
                       		%>
                       		<option value="AddressVerify">Address Verify</option>
                       		<option value="BillUser">Bill User</option>
               		        <option value="GetBalance">Get Balance</option>
                       		<option value="GetPalDetails"  selected="selected">Get PalDetails</option>
                       		<%
                       		} 
                       		%>
                   		</select>
               		</td>
               		<td>
               			<input type="hidden" name="mediator.paypal.version.val_hidden"
                               					id="mediator.paypal.version.val_hidden" value="61"/>
                        <input type="hidden" name="mediator.paypal.currency.val_hidden"
                               					id="mediator.paypal.currency.val_hidden" value="USD"/>    					
                    </td>
           	</tr>
           	</table>
           	<div id="configInputs"></div>
            <br>
            <div id="configOutputs"></div> 
            
           
    	</td>
    </tr>
    <tr>
    <td>
    <h3 class="mediator"><fmt:message key="mediator.paypal.optionalAxis2Config"/></h3>
    <table border="0" class="normal" >
            <tr>
                <td class="leftCol-small">
                    <fmt:message key="mediator.paypal.repo"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.paypal.repo" name="mediator.paypal.repo" value="<%=repo%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.paypal.axis2XML"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.paypal.axis2XML" name="mediator.paypal.axis2XML" value="<%=axis2XML%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                   <span class="required">*</span> <fmt:message key="mediator.paypal.defaultconfig"/>
                </td>
            </tr>
    </table>
    </td>
    </tr>
   </table>
</div>
</fmt:bundle>