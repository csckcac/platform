<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%
    String domain = getTenantDomain(request);
%>
<html>
<head>
<title>Login Page for Examples</title>
    <script type="text/javascript">
        function submitForm() {
            document.loginFrm.j_username.value = document.loginFrm.tmpUsername.value + "@<%= domain%>";
            document.loginFrm.submit();
        }
    </script>
<body bgcolor="white">
<form method="POST" name="loginFrm" action='<%= response.encodeURL("j_security_check") %>' >
  <table border="0" cellspacing="5">
    <tr>
      <th align="right">Username:</th>
      <td align="left"><input type="text" name="tmpUsername"/><input type="hidden" name="j_username">@<%= domain %></td>
    </tr>
    <tr>
      <th align="right">Password:</th>
      <td align="left"><input type="password" name="j_password"></td>
    </tr>
    <tr>
      <td align="right"><input type="button" value="Log In" onclick="submitForm()"></td>
      <td align="left"><input type="reset"></td>
    </tr>
  </table>
</form>
</body>
</html>

<%!
    private String getTenantDomain(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String temp = requestURI.substring(requestURI.indexOf("/t/") + 3);
        String tenantDomain = temp.substring(0, temp.indexOf("/"));
        return tenantDomain;
    }
%>
