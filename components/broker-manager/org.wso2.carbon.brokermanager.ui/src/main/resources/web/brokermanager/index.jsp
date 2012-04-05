<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerConfigurationDetails" %>
<%@ page import="org.wso2.carbon.brokermanager.ui.UIUtils" %>

<fmt:bundle basename="org.wso2.carbon.brokermanager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="brokermanager.list"
            resourceBundle="org.wso2.carbon.brokermanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">
        function doDelete(brokerName) {
            var theform = document.getElementById('deleteForm');
            theform.brokername.value = brokerName;
            theform.submit();
        }
    </script>
    <%
        String brokerName = request.getParameter("brokername");
        if (brokerName != null) {
            BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);
            stub.removeBrokerConfiguration(brokerName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Broker successfully deleted.');</script>
    <%
        }
    %>

    <div id="middle">
        <div id="workArea">
            <h3>Available Brokers</h3>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>Broker Name</th>
                    <th>Broker Type</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);
                    BrokerConfigurationDetails[] brokerDetailsArray = stub.getAllBrokerConfigurationNamesAndTypes();
                    if (brokerDetailsArray != null) {
                        for (BrokerConfigurationDetails brokerDetails : brokerDetailsArray) {

                %>
                <tr>
                    <td>
                        <a href="broker_details.jsp?brokerName=<%=brokerDetails.getBrokerName()%>&brokerType=<%=brokerDetails.getBrokerType()%>"><%=brokerDetails.getBrokerName()%>
                        </a>

                    </td>
                    <td><%=brokerDetails.getBrokerType()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=brokerDetails.getBrokerName()%>')"><font color="#4682b4">Delete</font></a>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            
        <div>
            <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                             name="brokername"
                                                                             value=""/></form>
        </div>
    </div>


    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
