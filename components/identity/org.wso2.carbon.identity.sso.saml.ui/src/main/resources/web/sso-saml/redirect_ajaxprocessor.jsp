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
<%@page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants"%>
<%@page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils"%>
<%@page import="java.net.URLDecoder" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="java.net.URL" %>
<%@page import="java.net.HttpURLConnection" %>
<%@page import="org.wso2.carbon.identity.sso.saml.ui.SAMLSSOProviderConstants" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Enumeration" %>
<%@page import="org.apache.commons.codec.binary.Base64" %>
<%@page import="org.wso2.carbon.identity.sso.saml.ui.session.mgt.FESessionManager" %>
<%@page import="org.wso2.carbon.identity.sso.saml.ui.session.mgt.FESessionBean" %>
<%@page import="org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOReqValidationResponseDTO" %>
<%@page import="org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSORespDTO" %>
<html>
<head></head>
<body>
<%
    String assertionConsumerURL = (String) request.getAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL);
    String assertion = (String) request.getAttribute(SAMLSSOProviderConstants.ASSERTION_STR);
    String relayState = (String) request.getAttribute(SAMLSSOProviderConstants.RELAY_STATE);
    String subject = (String) request.getAttribute(SAMLSSOProviderConstants.SUBJECT);
    String domain = null;
	if(subject != null && MultitenantUtils.getTenantDomain(subject) != null){
    	   domain = MultitenantUtils.getTenantDomain(subject);
	}
    relayState = URLDecoder.decode(relayState, "UTF-8");
    relayState = relayState.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
            replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");
    
    
   /*  try {
    	URL url = new URL(assertionConsumerURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("tenantDomain", "wso2.lk");
        conn.setRequestProperty("something", "something");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		String content = "SAMLResponse="+assertion+"&RelayState="+relayState;
		out.writeBytes(content);
		out.flush();
		out.close();
		conn.disconnect();
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (ProtocolException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } */
    
    
%>
<p>You are now redirected back to <%=assertionConsumerURL%>. If the
 redirection fails, please click the post button.</p>
<form method="post" action="<%=assertionConsumerURL%>">
    <p><input type="hidden" name="SAMLResponse" value="<%=assertion%>"/>
        <input type="hidden" name="RelayState" value="<%=relayState%>"/>
        <button type="submit">POST</button>
    </p>
</form>

<script type="text/javascript">
    document.forms[0].submit();
</script>

</body>
</html>