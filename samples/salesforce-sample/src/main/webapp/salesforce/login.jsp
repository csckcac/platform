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

<%@include file="../includes/head.jsp" %>

<script language=”javascript” type="application/javascript" src="../js/ui-utils.js"></script>
<script>
jQuery(document).ready(
	function(){
		jQuery('#sfCredDiv').hide();
		document.getElementById('customCredentials').checked = false;
		document.getElementById('loginMethod').value = "default";		
	}
);
</script>
<%
    Object obj = session.getAttribute("index");
    if (obj == null) {
        session.setAttribute("index", 0);
    }
    int i = (Integer) session.getAttribute("index");

    i++;
    session.setAttribute("index", i);

    String errorMessage = request.getParameter("error");
    String infoMessage = request.getParameter("info");
%>
<img src="../images/logo.png" alt="Logo" />
<div class="logoutButton">
    <a href="../otauth/logout.jsp">Log out</a>
</div>

<h2 class="mainTitle">Welcome to <b>S</b>ales<b>f</b>orce <b>D</b>ata <b>E</b>xporter application</h2>
<div class="helpMessage">
In this sample application we will be looking at how you can connect to Salesforce in your domain
and export your data queried from Salesforce into a google spread sheet.

To proceed with this sample, first select your preferred mode of logging into Salesforce.
</div>
<div class="loginBox">
<form name="login" action="../SalesForceLoginServlet" method="post" onsubmit="return validateFields()">
    <input type="hidden" name="action" value="submit">
    <% if (infoMessage != null) { %>
    <%=infoMessage%>
    <% } %>
    <!-- table>
        <tr>
            <td class="textBox">Sales Force Login Method</td>
            <td>
                <select id="loginMethods" onchange="setVisibility()">
                    <option value="">----SELECT----</option>
                    <option value="default">DEFAULT</option>
                    <option value="userDefined">USER DEFINED</option>
                </select>
            </td>
        </tr>
	<tr>
		<td></td>
		<td>
			    	
		</td>
	</tr>
    </table -->
    
    
	<input type="checkbox" id="customCredentials" onclick="setVisibility()" />
	<label for="customCredentials">Applications will use the inbuilt Salesforce credentials to log in. If you want to use your own credentials, please click this checkbox.</label>
	<div id="loginBtnDiv">
	<input type="submit" value="Login to SalesForce" class="button" />
    </div>
    <br />
    
    <div id="sfCredDiv" style="display:none">
        <table>
            <tr>
                <td class="textBox"> Email :</td>
                <td class="valueBox"><input id="emailAddress" name="emailAddress" size=15 type="text"/></td>
            </tr>
            <tr>
                <td class="textBox"> Password :</td>
                <td class="valueBox">
                    <input id="password" name="password" size=15 type="password"/>
                </td>
            </tr>
		<td></td>
                <td class="valueBox"><input type="submit" value="Login to SalesForce"/></td>
                <input id="loginMethod" type="hidden" name="loginMethod"/>
            </tr>
        </table>
    </div>

    <table>
        <tr>
            <td>
                <%if (errorMessage != null) {%>
                <font color="red">Error login into system - <%=errorMessage%>
                </font>
                <%} else {%>
                &nbsp;
                <%}%>
            </td>
        </tr>
    </table>
</form>
</div>
<%@include file="../includes/footer.jsp" %>
