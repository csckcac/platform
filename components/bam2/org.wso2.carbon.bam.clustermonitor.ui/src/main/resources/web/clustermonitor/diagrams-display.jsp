<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>

<!--[if lt IE 9]><script language="javascript" type="text/javascript" src="js/dist/excanvas.min.js"></script><![endif]-->
<script language="javascript" type="text/javascript" src="js/dist/jquery.min.js"></script>
<script language="javascript" type="text/javascript" src="js/dist/jquery.jqplot.min.js"></script>
<link rel="stylesheet" type="text/css" href="js/dist/jquery.jqplot.min.css" />
<link rel="stylesheet" type="text/css" href="css/clustermonitor.css" />

<script type="text/javascript" src="js/dist/plugins/jqplot.barRenderer.min.js"></script>
<script type="text/javascript" src="js/dist/plugins/jqplot.highlighter.min.js"></script>
<script type="text/javascript" src="js/dist/plugins/jqplot.cursor.min.js"></script>
<script type="text/javascript" src="js/dist/plugins/jqplot.pointLabels.min.js"></script>
<script type="text/javascript" src="js/dist/plugins/jqplot.categoryAxisRenderer.min.js"></script>
<script type="text/javascript" src="js/diagram-display.js"></script>
<script type="text/javascript">
    var t;
    var timer_is_on = 0;

    function timedCount() {
        t = setTimeout(timedCount, 60000);
        generateGraphs(true);
    }

    function getDataCluster(){
        var dc = $('#dcCombo').val();
        $('#clusterCombo').load("clusters_ajaxprocessor.jsp",{dc:dc},function(data){
            generateGraphs(false);
            getDataServices();
        });
    }
    function getDataServices(){
        var cluster = $('#clusterCombo').val();
        $('#serviceCombo').load("services_ajaxprocessor.jsp",{cluster:cluster},function(data){
			getDataOperations();
            generateGraphs(false);
        });
    }
    function getDataOperations(){
        var service = $('#serviceCombo').val();
        $('#operationCombo').load("operations_ajaxprocessor.jsp",{service:service},function(data){
            generateGraphs(false);
        });
    }
    function generateGraphs(replot){
        var dataCenter = $('#dcCombo').val();
        var cluster = $('#clusterCombo').val();
        var service = $('#serviceCombo').val();
        var operation = $('#operationCombo').val();

        $("#dcName_label").html(dataCenter);
        $("#clusterName_label").html(cluster);
        if(service != 'all'){
            $("#serviceName_label").html(" - " + service);
        }else{
            $("operationCombo").val("all");
            operation = "all";
            $("#serviceName_label").html("");
        }
        if(operation != 'all') {
            $("#operationName_label").html(" - " + operation);
        } else {
            $("#operationName_label").html("");
        }

        $.ajax(
        {
            async:false,
            data:{dataCenter:dataCenter,cluster:cluster,service:service,operation:operation},
            url:"get_data_ajaxprocessor.jsp",
            success:function(data){
				var requestCount = [];
				var responseCount = [];
				var faultCount = [];
				var responseTime = [];
                data = data.replace(/(\r\n|\n|\r)/gm," ").replace(/\s+/g,"");
                var dataRequestCount = data.split("<data>")[1];
                var dataResponseCount = data.split("<data>")[2];
                var dataFaultCount = data.split("<data>")[3];
                var dataResponseTime = data.split("<data>")[4];

                var requestCount = jQuery.parseJSON(dataRequestCount);
                var responseCount = jQuery.parseJSON(dataResponseCount);
                var faultCount = jQuery.parseJSON(dataFaultCount);
                var responseTime = jQuery.parseJSON(dataResponseTime);

                var allData = [];
                var allData_responseTime = [];
                allData.push(requestCount);
                allData.push(responseCount);
                allData.push(faultCount);

                allData_responseTime.push(responseTime);
                //allData is something like [[9.0,3.0],[72.0,21.0],[79.0,54.0]];
                if(!replot){
                    var plot1 = $.jqplot('placeholder1', allData,getConfig());
                    var plot2 = $.jqplot('placeholder2', allData_responseTime,getConfigRT());
                }else{
                    $.jqplot('placeholder1', allData, getConfig()).replot();
                    $.jqplot('placeholder2', allData_responseTime,getConfigRT()).replot();
                }
				allData_responseTime = null;responseCount = null;faultCount = null;responseTime = null;allData = null;requestCount = null;
                 if (!timer_is_on) {
                    timer_is_on = 1;
                    t = setTimeout(timedCount, 60000);
                }
            }
        });


    }
    $(document).ready(
            function(){
                getDataCluster();
            }
    );
</script>
<%--<script type="text/javascript" src="js/diagram-display.js"></script>--%>


<%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

        String[] dataCenter = client.getDataCenters();

%>
<div id="middle">
    <h2>Statistics - for <span id="clusterName_label"></span> in <span id="dcName_label"></span><span id="serviceName_label"></span><span id="operationName_label"></span></h2>

    <div id="workArea">
        <table width="100%" class="styledLeft">
            <thead>
                <tr>
                    <th colspan="2">Select Data Center and Cluster to generate Graphs.</th>
                </tr>
            </thead>
            <tbody>
            <tr>
                <td class="leftCol-med">Data Center</td>
                <td>
                    <select onchange="getDataCluster()" id="dcCombo">
                        <% for(int i=0;i<dataCenter.length;i++){%>
                        <option value="<%=dataCenter[i]%>"><%=dataCenter[i]%></option>
                        <% } %>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med">Clusters for the Data Center</td>
                <td>
                    <select onchange="generateGraphs(this);getDataServices()" id="clusterCombo"></select>
                </td>
            </tr>
			
			<tr>
                <td class="leftCol-med">Services</td>
                <td>
                    <select onchange="generateGraphs(this);getDataOperations()" id="serviceCombo"></select>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med">Operations</td>
                <td>
                    <select onchange="generateGraphs(this)" id="operationCombo"></select>
                </td>
            </tr>
            </tbody>
        </table>
        <table style="width:100%;">
    <tr>
        <td style="width:50%;padding:10px 10px 10px 0 !important;">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>Request/Response/Fault Counts</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <div id="placeholder1" style="height:300px"></div>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
        <td style="width:50%;padding:10px 0px 10px 0 !important;">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>Response Time</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <div id="placeholder2" style="height:300px"></div>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
</table>

    </div>
</div>


