<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bam.analyzer.ui.i18n.Resources">
    <carbon:breadcrumb label="bam.database"
                       resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">
        function submitIndexData(indexName) {
            document.indexForm.action = 'index-save.jsp?indexName=' + indexName + '&mode=edit';
            document.indexForm.submit();
            return true;
        }

        function cancelIndexData() {
            location.href = "data-config.jsp";
        }

    </script>
    <%

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

        String indexName = request.getParameter("indexName");

        IndexDTO index;

        try {
            index = client.getIndex(indexName);
        } catch (AxisFault e) {
            String errorString = "Failed to get index data.";
    %>
    <script type="text/javascript">
        jQuery(document).init(function () {
            CARBON.showErrorDialog('<%=errorString%>', function () {
                location.href = "data-config.jsp";
            });
        });
    </script>
    <%
            return;
        }

    %>

    <div id="middle">

        <h2>Configure Index</h2>

        <div id="workArea">

            <form id="indexForm" name="indexForm" action="" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="index.configuration"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td width="180px"><fmt:message key="cron"/></td>
                                    <td><input name="cron" id="cron"
                                               value="<%= index.getCron() != null ? index.getCron() : "" %>"/>
                                    </td>
                                </tr>
                            </table>

                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input type="button" value="<fmt:message key="save"/>"
                                               class="button" name="save"
                                               onclick="javascript:submitIndexData('<%= index.getIndexName() %>');"/>
                                        <input type="button" value="<fmt:message key="cancel"/>"
                                               name="cancel" class="button"
                                               onclick="javascript:cancelIndexData();"/>
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

