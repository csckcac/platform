<!--
~ Copyright 2009 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page
	import="org.wso2.carbon.bam.mgt.ui.client.BAMConfigAdminServiceClient"%>
<%@ page
	import="org.wso2.carbon.bam.mgt.ui.client.StatisticsEventingSubscriber"%>
<%@ page import="static org.wso2.carbon.bam.mgt.ui.BAMUIConstants.*"%>
<%@ page
	import="static org.wso2.carbon.bam.mgt.ui.BAMUIConstants.ACTION_SUBMIT"%>
<%@ page
	import="org.wso2.carbon.bam.mgt.ui.stub.bamconfig.types.carbon.MonitoredServerDTO"%>
<%@ page import="org.wso2.carbon.bam.util.BAMConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.governance.jmdns.beans.JmdnsBean"%>
<%@page import="org.wso2.carbon.governance.jmdns.JmdnsResponder"%>
<%@ page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<!--link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/-->
<%
    ServiceDiscoveryBean bean;
    if (request.getParameter(ACTION_SUBMIT) != null) {
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        String serverHttpURL = CarbonUIUtil.https2httpURL(serverURL); // TODO - need to use the https port here, once the eventing component support https

        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MonitoredServerDTO dto = new MonitoredServerDTO();
        dto.setServerURL(request.getParameter(SERVER_URL));
        dto.setUsername(request.getParameter(USERNAME));
        dto.setPassword(request.getParameter(PASSWORD));
        String serverType = request.getParameter("ServerType");
        dto.setServerType(serverType);


        try {
            if (serverType.equals(BAMConstants.SERVER_TYPE_EVENTING)) {
                String subID = StatisticsEventingSubscriber.subscribe(configContext,
                        request.getParameter(SERVER_URL) + BAMConstants.BAM_SERVICE_STATISTICS_PUBLISHER_SERVICE,
                        serverHttpURL + BAMConstants.BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE);

                dto.setSubscriptionID(subID);
            }

            BAMConfigAdminServiceClient client;
            client = new BAMConfigAdminServiceClient(cookie, serverURL, configContext);
            client.addServer(dto);

%>



<%@page import="org.wso2.carbon.dnsserverregistration.ui.bean.ServiceDiscoveryBean"%>
<%@page import="org.wso2.carbon.dnsserverregistration.ui.ServiceDiscovery"%><script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = 'list_servers.jsp?region=region3&item=monitored_server_list_menu&ordinal=0';
        }

        CARBON.showInfoDialog("Server successfully added.", handleOK);
    });
</script>
<%
    //CarbonUIMessage.sendCarbonUIMessage("Server successfully added", CarbonUIMessage.INFO, request);
} catch (Exception e) {
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('Error occurred: ' + <%=e.getMessage()%>);
    });
</script>
<%
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.bam.mgt.ui.i18n.Resources">
	<carbon:breadcrumb label="Add Server"
		resourceBundle="org.wso2.carbon.bam.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	<div id="middle">
	<h2><fmt:message key="add.monitored.server" /></h2>

	<div id="workArea"><script type="text/javascript">
                //for dns-sd
                var serverURLarray = new Array();
                <%
        bean=ServiceDiscovery.listwso2Services();
           if(bean!=null){
           List <String> maxServers=bean.getURLOfTheServer();
           for(int indexServerURL=0;indexServerURL<maxServers.size();indexServerURL++)
           	{
           	out.println("serverURLarray["+indexServerURL+"]='"+maxServers.get(indexServerURL)+"';");
           	}
           }
        %>
                function switchurl(select) {
                    var index;
                    if (index = 0)
                        document.forms[0].serverURL.value = "";
                    for (index = 1; index < select.options.length; index++)
                        if (select.options[index].selected)
                        {
                            if (select.options[index].value != "")
                                document.forms[0].serverURL.value = serverURLarray[index - 1];

                            break;
                        }
                }

                function validate() {

                    value = document.getElementsByName("<%=SERVER_URL%>")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('Server URL is required');
                        return false;
                    }
                    value = document.getElementsByName("<%=USERNAME%>")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('Username is required');
                        return false;
                    }
                    value = document.getElementsByName("<%=PASSWORD%>")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('Password is required');
                        return false;
                    }

                    document.addJMXServerForm.submit();

                    return true;
                }
            </script>

	<form method="post" name="addJMXServerForm" action="add_server.jsp"
		target="_self">
	<table style="width: 100%" class="styledLeft">
		<thead>
			<tr>
				<th colspan="2"><fmt:message key="add.server.to.monitor" /></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="formRow">
				<table class="normal" cellspacing="0">
					<tr>
						<!-- For JMDNS -->
						<td class="leftCol-small">Servers Available</td>
						<td><select id="JMDNS_SERVICES" name="JMDNS_SERVICES"
							onchange="switchurl(this);" style="width: 130px">
							<option value="0">Other</option>
							<%
						bean=JmdnsResponder.listwso2Services();
						if(bean!=null)
						{
						List <String> servicesToAdd = bean.getServicesToAdd();
				        for (int jmdnsservicesIndex = 0; jmdnsservicesIndex < servicesToAdd.size(); jmdnsservicesIndex++) {
				        	 %>
							<option value="<%=jmdnsservicesIndex+1%>"><%=servicesToAdd.get(jmdnsservicesIndex)%>
							</option>
							<%  
					}%>
							<%}
					    %>
						</select></td>
						<td class="leftCol-small"><fmt:message key="server.url" /><span
							class="required">*</span></td>
						<td><input class="text-box-big" id="<%=SERVER_URL%>"
							name="<%=SERVER_URL%>" type="text"></td>
					</tr>
					<tr>
						<td class="leftCol-small"><fmt:message key="server.type" /><span
							class="required">*</span></td>
						<td><input type="radio" name="ServerType" value="PullServer"
							checked><fmt:message key="server.type.polling" /><br>
						<input type="radio" name="ServerType" value="EventingServer"><fmt:message
							key="server.type.eventing" /><br>
						</td>
					</tr>

					<tr>
						<td class="leftCol-small"><fmt:message key="user.name" /><span
							class="required">*</span></td>
						<td><input class="text-box-big" id="<%=USERNAME%>"
							name="<%=USERNAME%>" type="text"></td>
					</tr>
					<tr>
						<td class="leftCol-small"><fmt:message key="password" /><span
							class="required">*</span></td>
						<td><input class="text-box-big" id="<%=PASSWORD%>"
							name="<%=PASSWORD%>" type="password"></td>
					</tr>
					<input type="hidden" id="<%=ACTION_SUBMIT%>"
						name="<%=ACTION_SUBMIT%>" type="text" value="<%=ACTION_SUBMIT%>">
				</table>
				</td>
			</tr>
			<tr>
				<td class="buttonRow" colspan="2"><input name="adduser"
					type="button" class="button" value="<fmt:message key="add"/>"
					onclick="validate();" /> <input type="button" class="button"
					onclick="javascript:location.href='list_servers.jsp?region=region3&item=monitored_server_list_menu&ordinal=0'"
					value="<fmt:message key="cancel"/>" /></td>
			</tr>
		</tbody>
	</table>

	</form>
	</div>
	</div>
</fmt:bundle>
