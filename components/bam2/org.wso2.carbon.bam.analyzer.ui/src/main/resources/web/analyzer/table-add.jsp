<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.analyzer.ui.i18n.Resources">
    <carbon:breadcrumb label="bam.database"
                       resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">

        function addColumn() {

        }

        function submitTableData() {
            document.tableForm.action = 'table-save.jsp';
            document.tableForm.submit();
            return true;
        }

        function cancelTableData() {
            location.href = "data-config.jsp";
        }

    </script>

    <%

        String mode = "";
        if (request.getParameter("mode") != null) {
            mode = request.getParameter("mode");
        }

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

        // Handle 'new' and 'exit' modes

        String[] dataSourceTypes = new String[0];
        try {
            dataSourceTypes = client.getDataSourceTypes();
        } catch (AxisFault e) {
            String errorString = "Unable to fetch data source meta data.";
    %>
    <script type="text/javascript">
        jQuery(document).init(function () {
            CARBON.showErrorDialog('<%=errorString%>');
        });
    </script>
    <%
        }
    %>

    <div id="middle">

        <h2>Add Table</h2>

        <div id="workArea">

            <form id="tableForm" name="tableForm" action="" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="table.configuration"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td width="180px"><fmt:message key="name"/> <span
                                            class="required">*</span></td>
                                    <td>
                                        <input name="tableName" id="tableName" value=""/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message
                                            key="dataSource.type"/><span class="required"> *</span>
                                    </td>
                                    <td>
                                        <select id="dataSourceType" name="dataSourceType">
                                            <%
                                                for (int i = 0; i < dataSourceTypes.length; i++) {
                                                    String type = dataSourceTypes[i].split(":")[0];

                                            %>
                                            <option value="<%=type%>" <%=i == 0 ? "selected=\"selected\"" : ""%>>
                                                <%=type%>
                                            </option>
                                            <% }%>
                                        </select>

                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message
                                            key="table.columns"/></td>
                                    <td><input id="columns" name="columns" type="text"
                                               value=""/>

                                        <div style="height:25px;">
                                            <a onclick="javaScript:addColumn()"
                                               style="background-image: url(../admin/images/add.gif);"
                                               class="icon-link">Add
                                                                 Column</a>
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>

                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input type="button" value="<fmt:message key="save"/>"
                                               class="button" name="save"
                                               onclick="javascript:submitTableData();"/>
                                        <input type="button" value="<fmt:message key="cancel"/>"
                                               name="cancel" class="button"
                                               onclick="javascript:cancelTableData();"/>
                                    </td>
                                </tr>
                                </tbody>

                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>

        </div>
    </div>


</fmt:bundle>