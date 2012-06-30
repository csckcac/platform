<%@ page import="org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext" %>
<%@ page import="org.wso2.carbon.registry.core.service.RegistryService" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page
        import="org.wso2.carbon.bam.toolbox.deployer.config.*" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.util.JasperTabDTO" %>

<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>

<script type="text/javascript" src="../../carbon/dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../../carbon/dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../../carbon/dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<script>
    $(function() {
        $("#tabs").tabs();
    });
</script>

<html>
<body>

<%

    int tenantId = CarbonContext.getCurrentContext().getTenantId();

    ToolBoxConfigurationManager mgr = ToolBoxConfigurationManager.getInstance();
    List<String> toolBoxNames = mgr.getAllToolBoxNames(tenantId);

%>

<div id="tabs">
    <%

        for (String toolBoxName : toolBoxNames) {
            ToolBoxDTO toolBoxDTO = mgr.getToolBox(toolBoxName, tenantId);

            List<JasperTabDTO> tabs = toolBoxDTO.getJasperTabs();

    %>
    <ul>
        <%
            for (int i = 0; i < tabs.size(); i++) {
                JasperTabDTO tab = tabs.get(i);
                String tabName = tab.getTabName();
                String tabId = "#tab-" + i;
/*                String registryPath = jasperPath + RegistryConstants.PATH_SEPARATOR + jrxml;

                Resource jrxmlResource = null;
                try {
                    jrxmlResource = registry.get(registryPath);
                } catch (RegistryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }*/

        %>
        <li><a href='<%= tabId %>'></a><%= tabName %>
        </li>
        <%
            }
        %>
    </ul>
    <%

        String renderJsp = "../render.jsp";

        for (int i = 0; i < tabs.size(); i++) {
            JasperTabDTO tab = tabs.get(i);
            String jrxml = tab.getJrxmlFileName();
            String renderUrl = renderJsp + "?jrxml=" + jrxml;

            String tabId = "tab-" + i;

    %>

    <div id='<%= tabId %>'>
        <iframe src='<%= renderUrl %>'></iframe>
    </div>

    <%
            }
        }
    %>

</div>
</body>
</html>
