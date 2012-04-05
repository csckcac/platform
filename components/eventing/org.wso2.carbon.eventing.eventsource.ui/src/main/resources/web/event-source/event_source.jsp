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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.eventing.eventsource.ui.EventingSourceAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<link href="../styles/main.css" rel="stylesheet" type="text/css" media="all"/>

<fmt:bundle basename="org.wso2.carbon.eventing.eventsource.ui.i18n.Resources">
<carbon:breadcrumb 
		label="source.add"
		resourceBundle="org.wso2.carbon.eventing.eventsource.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />

	<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../admin/js/cookies.js"></script>
	<script type="text/javascript" src="../admin/js/main.js"></script>
	<script type="text/javascript" src="global-params.js"></script>
	
	<%
		EventingSourceAdminClient client = null;
		String backendServerURL = null;
		ConfigurationContext configContext = null;
		String cookie = null;
	    String names = null;

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingSourceAdminClient(cookie, backendServerURL,configContext);
			names = client.getEventSourceNames();		
		} catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <jsp:include page="../admin/error.jsp"/>
<%
            return;
		}
	%>
	

	<div id="middle">
	<h2 id="eventsources"><fmt:message key="new.event.source"/></h2>
	<div id="workArea">
	    <script type="text/javascript">
	     function setType() {
    		var regUrl = document.getElementById("regUrl");  
    		var user = document.getElementById("username"); 
    		var password = document.getElementById("password"); 
    		var eventsourcetype = document.getElementById("eventsourcetype");
    		var type = null;
    			if (regUrl != undefined && eventsourcetype != null && user!=null && password!=null) {
    				type = eventsourcetype.value;
        			if ('DefaultInMemory' == type) {
            			regUrl.style.display = "none";  
            			user.style.display = "none"; 
            			password.style.display = "none";             			         			     
        			} else if ("Registry" == type) {
            			eventsourcetype.value="Registry";  
            			regUrl.style.display = "";  
            			user.style.display = ""; 
            			password.style.display = "";    
            		} else if ("EmbRegistry" == type){
                        eventsourcetype.value="EmbRegistry"; 
            			regUrl.style.display = "none";
            			user.style.display = "none";
            			password.style.display = "none";
                    }
        		}
        		return true;
   		 }


	 	function validateName(fldname) {
			var invalid = " "; // Invalid character is a space
			var fld = document.getElementsByName(fldname)[0];
			var error = "";
			var value = fld.value;
			
			if (value.indexOf(invalid) > -1)
			{
				error='<fmt:message key="invalid.characters"/>' + ":" + '<fmt:message key="empty.space"/>';
			}

			if ( value.indexOf("&") > -1 )
			{
				error='<fmt:message key="invalid.characters"/>' + ":" + '&';
			}

			if( value.indexOf(">") > -1 )
			{
				error='<fmt:message key="invalid.characters"/>' + ":" + '>';
			}

			if(value.indexOf("%") > -1)
			{
				error='<fmt:message key="invalid.characters"/>' + ":" + '%';
			}

			if( value.indexOf("<") > -1) {
				error='<fmt:message key="invalid.characters"/>' + ":" + '&lt;';
			}
             
		    if( value.indexOf("$") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '$';
		    }

            if( value.indexOf("@") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '@';
		    }

            if( value.indexOf("~") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '~';
		    }

            if( value.indexOf("!") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '!';
		    }

            if( value.indexOf("#") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '#';
		    }

            if( value.indexOf(";") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + ';';
		    }

            if( value.indexOf("^") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '^';
		    }

            if( value.indexOf("*") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '*';
		    }

            if( value.indexOf("+") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '+';
		    }

            if( value.indexOf("=") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + "=" + '=';
		    }

            if( value.indexOf("{") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '{';
		    }

            if( value.indexOf("}") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '}';
		    }

            if( value.indexOf("|") > -1) {
			    error='<fmt:message key="invalid.characters"/>' + ":" + '|';
		    }
			return error;
		}
  		 
   		 function essave(namemsg,regmsg,usermsg,nsmsg,headermsg,passwordmsg, form,names) {
   		 
   		 var eventsourcetype = document.getElementById("eventsourcetype");
   		 var errorMsg="";

    		if (document.getElementById('eventsourcename').value == '') {
        		CARBON.showWarningDialog(namemsg);
        		return false;
    		}

    		errorMsg = validateName('eventsourcename');
			
    	    if (errorMsg != "") {
    			CARBON.showInfoDialog(errorMsg + '<fmt:message key="event.source.name.error"/>');
    			return false;
    		}

    	    var i=0;

    	    var temp = new Array();

			if (names!=null)
			{    	    
    	      temp = names.split('%');
			}

			if(temp!=null) {    	    
    	    	for (i=0;i<temp.length;i++)
    	    	{
    	    		if (document.getElementById('eventsourcename').value == temp[i]) {
            			CARBON.showInfoDialog('<fmt:message key="event.source.existing"/>');
            			return false;
        			}
    	    	}
			}
    	        		
    
    		if (document.getElementById('headerName').value == '') {
        		CARBON.showWarningDialog(headermsg);
        		return false;
    		}

	        errorMsg = validateName('headerName');
			
    	    if (errorMsg != "") {
    			CARBON.showInfoDialog(errorMsg + '<fmt:message key="topic.error"/>');
    			return false;
    		}
    
    		if (document.getElementById('namespace').value == '') {
        		CARBON.showWarningDialog(nsmsg);
        		return false;
    		}

    		errorMsg = validateName('namespace');
  			
      	    if (errorMsg != "") {
      			CARBON.showInfoDialog(errorMsg + '<fmt:message key="topic.namespace.error"/>');
      			return false;
      		}
    		
    		if (eventsourcetype.value=="Registry")
    		{
    		
    			if (document.getElementById('registryUrl').value == '') {
        			CARBON.showWarningDialog(regmsg);
        			return false;
    			}
    
    			if (document.getElementById('user').value == '') {
        			CARBON.showWarningDialog(usermsg);
        			return false;
    			}

    			errorMsg = validateName('user');
    			
    		    if (errorMsg != "") {
    				CARBON.showInfoDialog(errorMsg + '<fmt:message key="user.error"/>');
    				return false;
    			}
    
    			if (document.getElementById('pwd').value == '') {
        			CARBON.showInfoDialog(passwordmsg);
        			return false;
    			}
    		}
    
    		form.submit();
		}

	    </script>
	    
	    <form action="add_eventsource.jsp" id="escreationform" name="escreationform">
		    
		<table class="styledLeft" width="100%" id="eventsources">
		
		   <thead>
            	<tr>
        			<th colspan="2"><fmt:message key="new.event.source"/></th>
    			</tr>
			</thead>

			<tr>			  
				<td width="25%"><fmt:message key="source.type"/></td>
				<td>				
					<select id="eventsourcetype" name="eventsourcetype" onchange="setType()">
            			<option value="DefaultInMemory">DefaultInMemory (topic filter)</option>
            			<option value="Registry">RemoteRegistry (topic filter)</option>
                        <option value="EmbRegistry">EmbeddedRegistry (topic filter)</option>
        			</select>        	
    			</td>							 
			</tr>
			
			<tr>			
				<td width="25%"><fmt:message key="source.name"/><span class='required'>*</span></td>
				<td><input type="text" name="eventsourcename" id="eventsourcename" style="width : 80%"/></td>			 
			</tr>
			
			<tr>			
				<td width="25%"><fmt:message key="topic.header.name"/><span class='required'>*</span></td>
				<td><input type="text" name="headerName" id="headerName" style="width : 80%"/></td>			 
			</tr>
			
			<tr>			
				<td width="25%"><fmt:message key="topic.header.namespace"/><span class='required'>*</span></td>
				<td><input type="text" name="namespace" id="namespace" style="width : 80%"/></td>			 
			</tr>
			
			<tr id="regUrl" style="display:none;">			
				<td width="25%"><fmt:message key="event.registry.url"/><span class='required'>*</span></td>
				<td><input type="text" name="registryUrl" id="registryUrl" style="width : 80%"/></td>			 
			</tr>
			
			<tr id="username" style="display:none;">			
				<td width="25%"><fmt:message key="event.username"/><span class='required'>*</span></td>
				<td><input type="text" name="user" id="user" style="width : 80%"/></td>			 
			</tr>
			
			<tr id="password" style="display:none;">			
				<td width="25%"><fmt:message key="event.password"/><span class='required'>*</span></td>
				<td><input type="password" name="pwd" id="pwd" style="width : 80%"/></td>			 
			</tr>	
			
			<tr>
			  <td colspan="2"  class="buttonRow">
                  <input class="button" type="submit" value="<fmt:message key="source.add"/>" onclick="javascript: essave('<fmt:message key="event.source.name.empty"/>','<fmt:message key="regisry.url.empty"/>','<fmt:message key="user.name.empty"/>','<fmt:message key="topic.header.namespace.empty"/>','<fmt:message key="topic.header.name.empty"/>','<fmt:message key="password.empty"/>',document.escreationform,'<%=names%>'); return false;"/>  
                  <input class="button" type="reset" value="<fmt:message key="cancel"/>"  onclick="javascript:document.location.href='index.jsp'"/ >                        
              </td>
			</tr>	
			
	    </table>
	    </form>	    
	</div>
	</div>
</fmt:bundle>