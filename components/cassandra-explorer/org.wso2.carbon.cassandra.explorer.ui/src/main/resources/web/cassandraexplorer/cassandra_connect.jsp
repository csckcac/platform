
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

<%@ page import="org.wso2.carbon.cassandra.explorer.stub.CassandraExplorerAdmin" %>
<%@ page import="org.wso2.carbon.explorer.ui.CassandraExplorerAdminClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/jquery.js"></script>
<script type="text/javascript" src="../admin/js/jquery.form.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>

<form id="connect_form" method="POST">
    <table>
        <td><input type="text" name="cluster_name" id="cluster_name"/></td>
        <td><input type="text" name="connection_url" id="connection_url"/></td>
        <td><input type="text" name="user_name" id="username"/></td>
        <td><input type="password" name="password" id="password"/></td>
    </table>
    <script type="text/javascript">
        $('#connect_form').click(function () {
            location.href = "cassandra_connect_ajaxprocessor.jsp";
        });
    </script>
</form>