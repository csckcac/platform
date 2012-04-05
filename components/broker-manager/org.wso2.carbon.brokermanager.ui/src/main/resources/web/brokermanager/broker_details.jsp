<%@ page import="org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerProperty" %>
<%@ page import="org.wso2.carbon.brokermanager.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.brokermanager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="brokermanager.details"
            resourceBundle="org.wso2.carbon.brokermanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2>Broker Details</h2>

        <div id="workArea">
            <table id="brokerInputTable" class="styledLeft"
                   style="width:100%">
                <tbody>
                <%
                    String brokerName = request.getParameter("brokerName");
                    String brokerType = request.getParameter("brokerType");
                    if (brokerName != null) {
                        BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);

                        BrokerProperty[] brokerProperties = stub.getBrokerConfiguration(brokerName);

                %>
                <tr>
                    <td class="leftCol-small">Broker Name</td>
                    <td><input type="text" name="brokerName" id="brokerNameId"
                               value=" <%=brokerName%>"
                               disabled="true"
                               style="width:50%"/></td>

                    </td>
                </tr>
                <tr>
                    <td>Broker Type</td>
                    <td><select name="brokerTypeFilter"
                                disabled="true">
                        <option><%=brokerType%>
                        </option>
                    </select>
                    </td>
                </tr>
                <%
                    if (brokerProperties != null) {
                        for (BrokerProperty brokerProperty : brokerProperties) {

                %>

                <tr>
                    <td><%=brokerProperty.getDisplayName()%>
                    </td>
                    <%
                        if (!brokerProperty.getSecured()) {
                    %>
                    <td><input type="input" value="<%=brokerProperty.getValue()%>"
                               disabled="true"
                               style="width:50%"/>
                    </td>
                    <%
                    } else { %>
                    <td><input type="password" value="<%=brokerProperty.getValue()%>"
                               disabled="true"
                               style="width:50%"/>
                    </td>
                    <%
                        }
                    %>
                </tr>
                <%

                            }
                        }
                    }

                %>

                </tbody>
            </table>


        </div>
    </div>
</fmt:bundle>