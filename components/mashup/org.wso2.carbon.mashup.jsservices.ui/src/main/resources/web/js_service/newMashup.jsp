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
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%@ page import="org.wso2.carbon.mashup.utils.MashupConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources">
<carbon:breadcrumb label="new.jsservice"
		resourceBundle="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
    <%
        String username = (String) request.getSession().getAttribute(
                CarbonSecuredHttpContext.LOGGED_USER);

    %>
	<script type="text/javascript">
        function validate() {
            var mashupName = document.getElementById("mashupName").value;
            if (mashupName == '') {
                CARBON.showErrorDialog('<fmt:message key="mashupName.empty"/>');
            } else {
                document.getElementById("serviceName").value = "<%=username%>" +
                        "<%=MashupConstants.SEPARATOR_CHAR%>" + mashupName;
                document.newMashup.submit();
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="new.jsservice"/></h2>

        <div id="workArea">
            <%
                String errorMessage = (String) session.getAttribute("mashup.error.message");
                String mashupName;
                if (errorMessage != null) {
                    %>
              <p><%=errorMessage%></p><br/>
            <%      mashupName = (String) session.getAttribute("mashup.service.name");
                    session.removeAttribute("mashup.error.message");
                    session.removeAttribute("mashup.service.name");
                } else {
                        mashupName = "";
                    }
            %>

            <form name="newMashup" method="post" action="editor.jsp">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="new.jsservice"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="mashup.name"/></label>
                                    </td>
                                    <td>
                                        <input type="text" id="mashupName" name="mashupName" size="40" onkeydown="if (event.keyCode == 13) document.getElementById('nextButt').click()" value="<%=mashupName%>"/>
                                        <input type="hidden" id="serviceName" name="serviceName" size="40"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="next" id="nextButt" type="button" class="button" value=" <fmt:message key="next"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button" onclick="location.href = '../service-mgt/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
                <input type="hidden" name="action" id="action" value="new">
            </form>
        </div>
    </div>
</fmt:bundle>
