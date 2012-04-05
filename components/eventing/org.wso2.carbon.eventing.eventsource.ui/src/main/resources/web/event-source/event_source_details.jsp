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
<%@ page import="org.wso2.carbon.eventing.eventsource.ui.EventingSourceAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.eventing.eventsource.stub.types.carbon.EventSourceDTO" %>
<link href="../styles/main.css" rel="stylesheet" type="text/css" media="all"/>

<fmt:bundle basename="org.wso2.carbon.eventing.eventsource.ui.i18n.Resources">
<carbon:breadcrumb 
		label="view.details"
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
	    EventSourceDTO eventSource = null;
		String sourceName = null;

		sourceName = request.getParameter("eventsource");

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingSourceAdminClient(cookie, backendServerURL,configContext);
			eventSource = client.getEventSource(sourceName);			
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
	<h2 id="eventsources"><fmt:message key="event.source.details"/></h2>
	<div id="workArea">
	
<script type="text/javascript">


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
			error='<fmt:message key="invalid.characters"/>' + ":" + '<';
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

	function essave(regmsg, usermsg, nsmsg, headermsg, passwordmsg, form) {

		var eventsourcetype = document.getElementById("eventsourcetype");
		var errorMsg = "";

		if (document.getElementById('headerName').value == '') {
			CARBON.showInfoDialog(headermsg);
			return false;
		}

		errorMsg = validateName('headerName');
			
	    if (errorMsg != "") {
			CARBON.showInfoDialog(errorMsg + '<fmt:message key="topic.error"/>');
			return false;
		}

		if (document.getElementById('namespace').value == '') {
			CARBON.showInfoDialog(nsmsg);
			return false;
		}

		errorMsg = validateName('namespace');
		
	    if (errorMsg != "") {
			CARBON.showInfoDialog(errorMsg + '<fmt:message key="topic.namespace.error"/>');
			return false;
		}

		if (eventsourcetype != null && eventsourcetype.value == "Registry") {

			if (document.getElementById('registryUrl').value == '') {
				CARBON.showInfoDialog(regmsg);
				return false;
			}

			if (document.getElementById('user').value == '') {
				CARBON.showInfoDialog(usermsg);
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
	
	
	   <%if(eventSource==null) { %>	
	   <fmt:message key="no.event.source.details"/>
	   <%} else { %>
	    <form action="update_eventsource.jsp" id="escreationform" name="escreationform">
	    <input type="hidden" name="name" id="name" value="<%=eventSource.getName()%>"/>
		<table class="styledLeft" width="100%" id="eventsources">
		    <thead>
            	<tr>
        			<th colspan="2"><fmt:message key="event.source.details"/> :<strong><%=eventSource.getName()%></th>
    			</tr>
			</thead>
	    	
			<%if(eventSource.getType()!=null) {%>
			<tr>			
				<td><fmt:message key="source.type"/></td>
				<td><%=eventSource.getType()%></td>			 
			</tr>
			<%}%>
			
			<%if(eventSource.getClassName()!=null) {%>
			<tr>			
				<td><fmt:message key="class.name"/></td>
				<td><%=eventSource.getClassName()%></td>			 
			</tr>
			<%}%>	
			
			<%if(eventSource.getTopicHeaderName()!=null) {%>
			<tr>			
				<td><fmt:message key="topic.header.name"/></td>
				<td><input type="text" name="headerName" id="headerName" style="width : 100%" value="<%=eventSource.getTopicHeaderName()%>"/></td>		 
			</tr>
			<%}%>	
			
			<%if(eventSource.getTopicHeaderNS()!=null) {%>
			<tr>			
				<td><fmt:message key="topic.header.namespace"/></td>
				<td><input type="text" name="namespace" id="namespace" style="width : 100%" value="<%=eventSource.getTopicHeaderNS()%>"/></td>			 
			</tr>
			<%}%>
			
			<%if(eventSource.getRegistryUrl()!=null) {%>
			<input type="hidden" name="eventsourcetype" id="eventsourcetype" value="Registry"/>
			<tr>			
				<td><fmt:message key="event.registry.url"/></td>
				<td><input type="text" name="registryUrl" id="registryUrl" style="width : 100%" value="<%=eventSource.getRegistryUrl()%>"/></td>			 
			</tr>
			<%}%>
			
			<%if(eventSource.getUsername()!=null) {%>
			<tr>			
				<td><fmt:message key="event.username"/></td>
				<td><input type="text" name="user" id="user" style="width : 100%" value="<%=eventSource.getUsername()%>" /></td>			 
			</tr>
			<%}%>
			
			<%if(eventSource.getPassword()!=null) {%>
			<tr>			
				<td><fmt:message key="event.password"/></td>
				<td><input type="password" name="pwd" id="pwd" style="width : 100%" value="<%=eventSource.getPassword()%>"/></td>			 
			</tr>
			<%}%>
			
			<tr>
                <td class="buttonRow" colspan="2">        
        			<input class="button" type="submit" value="<fmt:message key="save"/>" onclick="javascript: essave('<fmt:message key="regisry.url.empty"/>','<fmt:message key="user.name.empty"/>','<fmt:message key="topic.header.namespace.empty"/>','<fmt:message key="topic.header.name.empty"/>','<fmt:message key="password.empty"/>',document.escreationform); return false;"/> 
        			<input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="javascript:document.location.href='index.jsp'"/>
               </td>
            </tr>			
					
	    </table>
	    </form>
	    <%}%>
	</div>
	</div>
	
   <script type="text/javascript">
        alternateTableRows('eventsources', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>