<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String remoteInterfaceName = request.getParameter("remoteInterfaceName");
    String beanJndiName = request.getParameter("beanJndiName");
    String jndiUser = request.getParameter("jndiUser");
    String jndiPassword = request.getParameter("jndiPassword");
    String providerUrl = request.getParameter("providerUrl");
    String serviceType = request.getParameter("serviceType");
    String jndiContextClass = request.getParameter("jndiContextClass");
    String serviceClass = request.getParameter("ServiceClass");
    String serviceName = request.getParameter("serviceName");

    String forwardTo = "index.jsp";
    try {
        EJBServicesAdminClient serviceAdmin = new EJBServicesAdminClient(config.getServletContext(), session);
        serviceAdmin.deleteEJBConfiguration(beanJndiName, providerUrl);

        String msg = "EJB Service Conficutation updated successfully";
        //todo delete the relevant ejb service as well.
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>