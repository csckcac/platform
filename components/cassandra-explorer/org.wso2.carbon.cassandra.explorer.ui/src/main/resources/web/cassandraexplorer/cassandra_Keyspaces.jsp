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

<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>

<%
    CassandraExplorerAdminClient adminClient
            = new CassandraExplorerAdminClient(config.getServletContext(), session);
    String[] keyspaces = adminClient.getKeyspaces();
%>

<script language="text/javascript" src="../admin/js/customControls.js"></script>
<script>
    jQuery(document).ready(function () {
        initSections("");
    });
</script>
<style>
    .sectionSeperator {
        margin-bottom: 0;
    }

    .sectionSub {
        padding: 0;
        margin: 0 0 10px 0;
    }
</style>

<div id="middle">
    <h2>User Management</h2>

    <div id="workArea">
        <!-- Section 1 -->
        <%
            for (String keyspace : keyspaces) {%>
        <div class="sectionSeperator togglebleTitle"><%=keyspace%>
        </div>
        <div class="sectionSub">
            <table width="100%" id="internal" class="styledLeft">
                <tbody>
                <% String[] columnFamilies = adminClient.getColumnFamilies(keyspace);
                    for (int i = 0; i < columnFamilies.length; i++) {
                        String rowType;
                        if (i + 1 % 2 == 0) {
                            rowType = "tableEvenRow";
                        } else {
                            rowType = "tableOddRow";
                        }
                        %>
                <tr class=<%=rowType%>>
                    <td>
                        <a href="#" onclick="viewExplorer('<%=keyspace%>','<%=columnFamilies[i]%>')"
                           style="background-image:url(images/explorer.png);" class="icon-link"><%=columnFamilies[i]%></a>
                    </td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
        <%}%>
    </div>
</div>