<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.TableDTO" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.CursorDTO" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!-- Dependencies -->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">

<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!-- Source File -->

<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.analyzer.ui.i18n.Resources">
<carbon:breadcrumb
        label="bam.database"
        resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">

    $(function () {
        $("#tabs").tabs();
    });

    $(document).ready(function () {
        var $tabs = $('#tabs > ul').tabs({ cookie:{ expires:30 } });
        $('a', $tabs).click(function () {
            if ($(this).parent().hasClass('ui-tabs-selected')) {
                $tabs.tabs('load', $('a', $tabs).index(this));
            }
        });
        <%
    String tabs = request.getParameter("tabs");
    if(tabs!=null && tabs.equals("0")) {
        %>$tabs.tabs('option', 'selected', 0);
        <%
    }else if(tabs!=null && tabs.equals("1")){
        %>$tabs.tabs('option', 'selected', 1);
        <%--<%
  } else if (tabs != null && tabs.equals("2")) {
      %>$tabs.tabs('option', 'selected', 2);--%>
        <% } %>
    });

    function deleteIndex(indexName) {
        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.index"/> " + indexName + " ?", function () {
            location.href = "index-delete.jsp?indexName=" + indexName;
        });
    }

    function deleteTable(tableName) {
        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.table"/> " + tableName + " ?", function () {
            location.href = "table-delete.jsp?tableName=" + tableName;
        });
    }

    function submitAutoForm() {
        document.autoForm.submit();
        return true;
    }

</script>

<%

    String tablePageNumberStr = request.getParameter("tablePageNumber");
    String indexPageNumberStr = request.getParameter("indexPageNumber");

    int tablePageNumber = 0;
    int indexPageNumber = 0;

    if (tablePageNumberStr != null) {
        tablePageNumber = Integer.parseInt(tablePageNumberStr);
    }

    if (indexPageNumberStr != null) {
        indexPageNumber = Integer.parseInt(indexPageNumberStr);
    }

    int numberOfTablePages = 0;
    int numberOfIndexPages = 0;

    int tableCount = 0;
    int indexCount = 0;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

    IndexDTO[] indexes = null;

    try {
        indexes = client.getIndexMetaData(indexPageNumber, IndexAdminClient.INDEXES_PER_PAGE);
        indexCount = client.getIndexCount();
    } catch (AxisFault e) {
        String credentialsNotSupplied = "Credentials invalid or not supplied yet.";

        String errorString;
        if (e.getMessage().contains(credentialsNotSupplied)) {
            errorString = "Connection parameters not provided..";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        var callback = function () {
            location.href = "../persistence/index.jsp";
        }

        CARBON.showInfoDialog('<%=errorString%>', callback, callback);
    });
</script>
<%
        } else {
            errorString = "Unable to fetch index meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }
    }

    String includeAuto = request.getParameter("autoGenerated");

    if (includeAuto == null) {
        includeAuto = "false";
    }

    TableDTO[] tables = null;

    try {
        if (includeAuto != null && Boolean.parseBoolean(includeAuto)) {
            tables = client.getTableMetaData(tablePageNumber, IndexAdminClient.TABLES_PER_PAGE, true);
            tableCount = client.getTableCount(true);
        } else {
            tables = client.getTableMetaData(tablePageNumber, IndexAdminClient.TABLES_PER_PAGE, false);
            tableCount = client.getTableCount(false);
        }
    } catch (AxisFault e) {
        String credentialsNotSupplied = "Credentials invalid or not supplied yet.";

        String errorString;
        if (e.getMessage().contains(credentialsNotSupplied)) {
            errorString = "Connection parameters not provided..";
%>
<script type="text/javascript">
    jQuery(document).init(function () {

        var callback = function () {
            location.href = "../persistence/index.jsp";
        }

        CARBON.showInfoDialog('<%=errorString%>', callback, callback);
    });
</script>
<%
        } else {
            errorString = "Unable to fetch table meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }
    }

    CursorDTO[] cursors = null;

    try {
        cursors = client.getAllCursors();
    } catch (AxisFault e) {
        String credentialsNotSupplied = "Credentials invalid or not supplied yet.";

        String errorString;
        if (e.getMessage().contains(credentialsNotSupplied)) {
            errorString = "Connection parameters not provided..";
%>
<script type="text/javascript">
    jQuery(document).init(function () {

        var callback = function () {
            location.href = "../persistence/index.jsp";
        }

        CARBON.showInfoDialog('<%=errorString%>', callback, callback);
    });
</script>
<%
        } else {
            errorString = "Unable to fetch cursor meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }
    }

    if (tableCount % IndexAdminClient.INDEXES_PER_PAGE == 0) {
        numberOfTablePages = tableCount / IndexAdminClient.INDEXES_PER_PAGE;
    } else {
        numberOfTablePages = tableCount / IndexAdminClient.INDEXES_PER_PAGE + 1;
    }

    if (indexCount % IndexAdminClient.TABLES_PER_PAGE == 0) {
        numberOfIndexPages = indexCount / IndexAdminClient.TABLES_PER_PAGE;
    } else {
        numberOfIndexPages = indexCount / IndexAdminClient.TABLES_PER_PAGE + 1;
    }

%>

<div id="middle">
<h2>
    BAM Database
</h2>

<div id="workArea" style="background-color:#F4F4F4;">
<div id="tabs">
<ul>
    <li><a href="#tabs-1"><fmt:message key="defined.indexes"/></a></li>
    <li><a href="#tabs-2"><fmt:message key="defined.tables"/></a></li>
        <%--<li><a href="#tabs-3"><fmt:message key="created.cursors"/></a></li>--%>
</ul>
<div id="tabs-1">
    <div id="noEpDiv" style="<%=indexes!=null ?"display:none":""%>">
        <fmt:message
                key="no.indexes.defined"></fmt:message>

    </div>

    <div style="height:25px;">
        <a href="index-config.jsp?mode=new"
           style="background-image: url(../admin/images/add.gif);" class="icon-link">Add
                                                                                     Index</a>
    </div>
    <br/>

    <% if (indexes != null) {%>
    <p><fmt:message key="available.indexes"/></p>
    <br/>


    <carbon:paginator pageNumber="<%=indexPageNumber%>" numberOfPages="<%=numberOfIndexPages%>"
                      page="data-config.jsp" pageNumberParameterName="indexPageNumber"
                      resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=""%>"/>
    <br/>
    <table class="styledLeft" cellpadding="1" id="indexListTable">
        <thead>
        <tr>
            <th style="width:15%"><fmt:message key="index.name"/></th>
            <th style="width:15%"><fmt:message key="indexed.table"/></th>
            <th style="width:15%"><fmt:message key="dataSource.type"/></th>
            <th style="width:15%"><fmt:message key="auto.generated"/></th>
            <th style="width:30%"><fmt:message key="indexed.columns"/></th>
            <th colspan="2"><fmt:message key="action"/></th>
        </tr>
        </thead>
        <tbody>
        <%for (IndexDTO index : indexes) {%>
        <tr>
            <td>
                <%=index.getIndexName()%>
            </td>
            <td>
                <%=index.getIndexedTable()%>
            </td>
            <td>
                <%=index.getDataSourceType()%>
            </td>
            <td>
                <%=index.getAutoGenerated()%>
            </td>
            <td>
                <%
                    String[] columns = index.getIndexedColumns();
                    StringBuffer sb = new StringBuffer("");
                    String result = "";

                    if (columns != null) {
                        for (String column : columns) {
                            if (column != null) {
                                sb.append(column);
                                sb.append(", ");
                            }
                        }
                    }

                    if (!sb.toString().equals("")) {
                        result = sb.toString().substring(0, (sb.lastIndexOf(",")));
                    }

                %>
                <%=result%>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="deleteIndex('<%= index.getIndexName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </div>
                <% if (index.getManuallyIndexed()) { %>
                <div class="inlineDiv">
                    <a href='<%="index-edit.jsp?mode=edit&indexName=" +  index.getIndexName() %>'
                       class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="configure"/></a>
                </div>
                <% } %>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <br/>
    <carbon:paginator pageNumber="<%=indexPageNumber%>" numberOfPages="<%=numberOfIndexPages%>"
                      page="data-config.jsp" pageNumberParameterName="indexPageNumber"
                      resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=""%>"/>
    <% } %>
</div>
<div id="tabs-2">

    <div style="height:25px;">
        <a href="table-add.jsp"
           style="background-image: url(../admin/images/add.gif);" class="icon-link">Add
                                                                                     Table</a>
    </div>
    <br/>

    <div id="noTableDiv" style="<%=tables!=null ?"display:none":""%>">
        <fmt:message
                key="no.tables.defined"></fmt:message>

    </div>

    <% if (tables != null) {%>

    <form id="autoForm" name="autoForm" action="data-config.jsp" method="POST">
        <input type="checkbox" name="autoGenerated" value="true"
               onchange=submitAutoForm()

                <% if (includeAuto.equals("true")) { %>
               checked="true"
                <% } %>
                />
        Include auto generated tables.
        <br/>
    </form>
    <br/>

    <p><fmt:message key="available.tables"/></p>
    <br/>


    <carbon:paginator pageNumber="<%=tablePageNumber%>" numberOfPages="<%=numberOfTablePages%>"
                      page="data-config.jsp" pageNumberParameterName="tablePageNumber"
                      resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%="autoGenerated=" + includeAuto %>"/>
    <br/>
    <table class="styledLeft" cellpadding="1" id="tableListTable">
        <thead>
        <tr>
            <th style="width:15%"><fmt:message key="table.name"/></th>
            <th style="width:15%"><fmt:message key="dataSource.type"/></th>
            <th style="width:30%"><fmt:message key="table.columns"/></th>
            <th colspan="1"><fmt:message key="action"/></th>
        </tr>
        </thead>
        <tbody>
        <%for (TableDTO table : tables) {%>
        <tr>
            <td>
                <%=table.getTableName()%>
            </td>
            <td>
                <%=table.getDataSourceType()%>
            </td>
            <td>
                <%
                    String[] columns = table.getColumns();
                    StringBuffer sb = new StringBuffer("");

                    String result = "";

                    if (columns != null) {
                        for (String column : columns) {
                            if (column != null) {
                                sb.append(column);
                                sb.append(", ");
                            }
                        }

                        if (!sb.toString().equals("")) {
                            result = sb.toString().substring(0, (sb.lastIndexOf(",")));
                        }
                    }
                %>
                <%=result.equals("") ? "Not Available" : result %>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="deleteTable('<%= table.getTableName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </div>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <br/>
    <carbon:paginator pageNumber="<%=tablePageNumber%>" numberOfPages="<%=numberOfTablePages%>"
                      page="data-config.jsp" pageNumberParameterName="tablePageNumber"
                      resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%="autoGenerated=" + includeAuto %>"/>
    <% } %>
</div>
    <%--<div id="tabs-3">
        <div id="noCursorDiv" style="<%=cursors!=null ?"display:none":""%>">
            <fmt:message
                    key="no.cursors.present"></fmt:message>

        </div>

        <br/>

        <% if (cursors != null) {%>
        <p><fmt:message key="available.indexes"/></p>
        <br/>


        <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                          page="data-config.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>" />
        <br/>
        <table class="styledLeft" cellpadding="1" id="indexListTable">
            <thead>
            <tr>
                <th style="width:15%"><fmt:message key="index.name"/></th>
                <th style="width:15%"><fmt:message key="indexed.table"/></th>
                <th style="width:15%"><fmt:message key="dataSource.type"/></th>
                <th style="width:15%"><fmt:message key="auto.generated"/></th>
                <th style="width:30%"><fmt:message key="indexed.columns"/></th>
                <th colspan="1"><fmt:message key="action"/></th>
            </tr>
            </thead>
            <tbody>
            <%for (IndexDTO index : indexes) {%>
            <tr>
                <td>
                    <%=index.getIndexName()%>
                </td>
                <td>
                    <%=index.getIndexedTable()%>
                </td>
                <td>
                    <%=index.getDataSourceType()%>
                </td>
                <td>
                    <%=index.getAutoGenerated()%>
                </td>
                <td>
                    <%
                        String[] columns = index.getIndexedColumns();
                        StringBuffer sb = new StringBuffer("");

                        if (columns != null)
                            for (String column : columns) {
                                sb.append(column);
                                sb.append(", ");
                            }

                        sb.toString().substring(0, (sb.lastIndexOf(",")));

                    %>
                    <%=sb.toString()%>
                </td>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteIndex('<%= index.getIndexName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </div>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                          page="data-config.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>" />
        <% } %>
    </div>--%>
</div>
</div>
</div>

<script type="text/javascript">
    alternateTableRows('indexListTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('tableListTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>