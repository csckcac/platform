<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfileUtils" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources">

<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String userName = "admin";
    String password = "admin";
    String ip = "localhost";
    String port = "7611";
    String serverProfileLocation = "";
    
    String tmpUserName = request.getParameter("txtUsername");
    if(tmpUserName != null && !tmpUserName.equals("")){
        userName = tmpUserName;
    }


    String tmpPassword = request.getParameter("txtPassword");
    if(tmpPassword != null && !tmpPassword.equals("")){
        password = tmpPassword;
    }

    String tmpIp = request.getParameter("txtIp");
    if(tmpIp != null && !tmpIp.equals("")){
        ip = tmpIp;
    }

    String tmpPort = request.getParameter("txtPort");
    if(tmpPort != null && !tmpPort.equals("")){
        port = tmpPort;
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String tmpServerProfileLocation = request.getParameter("txtServerProfileLocation");
    if(tmpServerProfileLocation != null && !tmpServerProfileLocation.equals("")){
        serverProfileLocation = tmpServerProfileLocation;

        BamServerProfileUtils bamServerProfileUtils =
                new BamServerProfileUtils(cookie, backendServerURL, configContext, request.getLocale());
        if(!bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
            bamServerProfileUtils.addResource(ip, port, userName, password, serverProfileLocation);
        }
        else {
            %>
                 <script type="text/javascript">
                     alert("Resource already exists!");
                 </script>

            <%
        }
    }


%>

<%--<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'sequences/js/registry-browser.js'"/>
<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'resources/js/resource_util.js'"/>
<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'yui/build/connection/connection-min.js'"/>--%>

<div id="middle">
    <h2>
        <fmt:message key="bam.server.profile"/>
    </h2>

    <div id="workArea">
        <form action="configure_server_profiles.jsp" method="post">
        <table>
            <tr>
                <td>
                    <fmt:message key="username"/>
                </td>
                <td>
                    <input type="text" name="txtUsername" id="txtUsername" value="<%=userName%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="password"/>
                </td>
                <td>
                    <input type="password" name="txtPassword" id="txtPassword" value="<%=password%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="ip"/>
                </td>
                <td>
                    <input type="text" name="txtIp" id="txtIp" value="<%=ip%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="port"/>
                </td>
                <td>
                    <input type="text" name="txtPort" id="txtPort" value="<%=port%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    Registry
                </td>
                <td>
                    <input type="text" name="txtServerProfileLocation" id="txtServerProfileLocation" value=""/>
                </td>
            </tr>

            <%--<tr>
                <td>
                    <fmt:message key="server.profile.location"/><span class="required">*</span>
                </td>
                <td>
                    <input class="longInput" type="text"
                           value=""
                           id="txtServerProfileLocation" name="txtServerProfileLocation" readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('txtServerProfileLocation','/_system/config')"><fmt:message key="conf.registry.browser"/>
                    </a>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('txtServerProfileLocation','/_system/governance')"><fmt:message key="gov.registry.browser"/>
                    </a>
                </td>
            </tr>--%>

            <tr>
                <td>
                    <table>
                        <thead>
                            <tr>
                                <td>Stream Name</td>
                                <td>Stream Version</td>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>

                            </tr>
                        </tbody>
                    </table>
                </td>
            </tr>

            <tr>
                <td>
                    <input type="submit" value="Save"/>
                </td>
            </tr>
        </table>
        </form>
    </div>
</div>


</fmt:bundle>

