<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>

<script language="javascript" type="text/javascript" src="js/dist/jquery.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/clustermonitor.css" />

<script type="text/javascript" src="js/diagram-display.js"></script>
<script type="text/javascript">
    var t;
    var timer_is_on = 0;

    function timedCount() {
        //t = setTimeout(timedCount, 4000);
        generateTables();
    }

    function getDataCluster(){
        var dc = $('#dcCombo').val();
        $('#clusterCombo').load("clusters_ajaxprocessor.jsp",{dc:dc},function(data){
            generateTables();
        });
    }
    function getDataServices(){
        var cluster = $('#clusterCombo').val();
        $('#serviceCombo').load("services_ajaxprocessor.jsp",{cluster:cluster},function(data){
            generateTables();
        });
    }

    function generateTables(){
        var dc = $('#dcCombo').val();
        var cluster = $('#clusterCombo').val();

        $("#dcName_label").html(dc);
        $("#clusterName_label").html(cluster);

        $.ajax(
        {
            async:false,
            data:{cluster:cluster,dc:dc},
            url:"cluster_data_display_ajaxprocessor.jsp",
            success:function(data){
                  $('#clusterStatContainer').html(data);
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
                    <select onchange="generateTables(this);getDataServices()" id="clusterCombo"></select>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">

                </td>
            </tr>

            </tbody>
        </table>
        <div id="clusterStatContainer"></div>

    </div>
</div>


