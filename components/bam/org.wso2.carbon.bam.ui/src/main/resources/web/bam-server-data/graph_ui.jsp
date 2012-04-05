<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<link rel="stylesheet" type="text/css" href="../bam-server-data/css/reporting.css"/>

<script type="text/javascript">
    function setType(reportType) {

        var reportTypeObj     = document.getElementById("reportType");
        var taskObj = document.getElementById("task");
        reportTypeObj.value = reportType;
        taskObj.value = selectedTask;
        document.graphGenerateUi.submit();
    }
</script>


<form action="../graph" id="graphForm" method="post" name="graphGenerateUi" target="_blank">
    <input type="hidden" name="reportType" id="reportType" value="">
    <input type="hidden" name="task" id="task" value="">
    <input type="hidden" name="component" id="component" value="<%=request.getParameter("component")%>">
    <input type="hidden" name="template" id="template" value="<%=request.getParameter("template")%>">
    <input type="hidden" name="reportDataSession" id="reportDataSession" value="<%=request.getParameter("reportDataSession")%>">

    <table class="reportingType">
                <tr> <td class="reporting-title"></td>
                    <td><a onclick="setType('pdf')"><img src="../bam-server-data/images/chart-report.gif"/>Generate PDF Graph</a></td>
                    <td class="reporting-sep">|</td>
                    <td><a onclick="setType('html')"><img src="../bam-server-data/images/chart-report.gif"/>Generate Html Graph</a></td>
                    <td class="reporting-sep">|</td>
                    <td><a onclick="setType('excel')"><img src="../bam-server-data/images/chart-report.gif"/>Generate Excel Graph</a></td>
                </tr>
            </table>
    </form>