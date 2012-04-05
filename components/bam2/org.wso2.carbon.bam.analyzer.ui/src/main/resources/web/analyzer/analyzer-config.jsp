<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.AnalyzerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript" src="js-lib/jstree/_lib/jquery.js"></script>
<script type="text/javascript" src="js-lib/jstree/_lib/jquery-ui.min.js"></script>
<script type="text/javascript" src="js-lib/jstree/_lib/jquery.hotkeys.js"></script>
<script type="text/javascript" src="js-lib/jstree/_lib/jquery.cookie.js"></script>
<script type="text/javascript" src="js-lib/jstree/jquery.jstree.js"></script>
<script type="text/javascript" src="js-lib/jquery-validation/jquery.validate.min.js"></script>

<script type="text/javascript" src="js/overrides_ui.js"></script>
<script type="text/javascript" src="js/overrides_save.js"></script>
<script type="text/javascript" src="js/const.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/Config.js"></script>
<script type="text/javascript" src="js/Tree.js"></script>
<script type="text/javascript" src="js/Toolbar.js"></script>
<script type="text/javascript" src="js/TreeOperations.js"></script>


<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" rel="stylesheet" href="css/analizer.css">
<%
 String mode = null;
 String analyzerXML_from_source = null;
    if(request.getParameter("mode") != null){
        mode = request.getParameter("mode");
    }
    if(request.getParameter("analyzerXML_from_source") != null){
        analyzerXML_from_source = request.getParameter("analyzerXML_from_source");
    }

    String analyzerXML;
    String xmlName = "";
    String analyzerSeqName = "";

    if (mode.equals("edit")) {
        if(request.getParameter("xmlName") != null){
            xmlName = request.getParameter("xmlName");
        }


        analyzerSeqName = request.getParameter("seqname");



        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);

        analyzerXML = client.getAnalyzerXML(analyzerSeqName);


    } else {
        /*analyzerXML =
                "<analyzerSequence name=\"workflowSummarizer\">\n" +
                        "<trigger cron=\"1 * * * * ? *\"/>" +
                "<analyzers>" +
                "</analyzers>" +
                "</analyzerSequence>" ;*/
        analyzerXML = "";
    }
    if(analyzerXML_from_source!=null){
            analyzerXML = analyzerXML_from_source;
    }
    if(request.getParameter("analyzerXML")!=null){//This is when the design tab is clicked on source view
        analyzerXML = request.getParameter("analyzerXML");
    }
    StringBuffer escapedBuffer = new StringBuffer();
    for (int i = 0; i < analyzerXML.length(); i++) {
        if ((analyzerXML.charAt(i) != '\n') && (analyzerXML.charAt(i) != '\r') && (analyzerXML.charAt(i) != '\t')) {
            escapedBuffer.append(analyzerXML.charAt(i));
        }
    }
    String s = escapedBuffer.toString();
    s=s.replace("'", "\"");


%>
<script type="text/javascript">

    $(document).ready(function() {
        $.ajax({
            url:'analizerUIConfig.xml',
            success:function(dataConfigXML) {
                var stringXML = '<%=s%>';
                var data_XML;
               if(stringXML == ""){
                    var config = new Config(dataConfigXML);
                    data_XML = config.createInitialXML(); // If the data xml is empty create the initial data xml from the config xml
                }else{
                   try
                      {
                        data_XML=$.parseXML(stringXML);
                      }
                    catch(err)
                      {
                          err = err.replace(/</g,'&lt;');
                          err = err.replace(/>/g,'&gt;');
                          CARBON.showErrorDialog("You have something wrong with the XML !!! Please correct it fist to view the design view. <br><br>"+err,function(){loadSourceOnError('<%=s%>');},function(){loadSourceOnError('<%=s%>');});
                          return;
                      }

               }

                init(data_XML, dataConfigXML);

            }
        }
      );
    });

</script>
<fmt:bundle basename="org.wso2.carbon.analyzer.ui.i18n.Resources">
    <carbon:breadcrumb label="main.analyzer"
                       resourceBundle="org.wso2.carbon.analyzer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
<div id="middle">
    <h2>Analyzer Sequences</h2>

    <div id="tabs">
        <ul>
            <li class="ui-tabs-selected"><a id="designTab">Design</a></li>
            <li><a id="sourceTab">Source</a></li>
        </ul>
        <div style="clear:both"></div>
    </div>
    <div id="workArea">
        <div id="dialog-overlay"></div>
        <div id="dialog-box">
            <div class="dialog-content">
            	<form id="attributeForm" onsubmit="return false">
	                <a class="closeButton"></a>
	                <div id="dialog-message" class="xmlTreeAttributes"></div>
	                <div class="dialogButtons">
	                    	<input type="submit" class="button" id="saveButton" value="Save"/>
                        	<input type="button" class="button" id="cancelButton" value="Cancel"/>
	                </div>
                </form>
            </div>
        </div>

        <div class="analizer_background">
            <table class="analizerMainTable">
                <tr>
                    <td>
                        <div id="analizer_nav">
                            <div class="title toolBar-header">Filters</div>
                            <div class="analizer_toolbar">
                                <div id="analizer_toolbar"></div>
                                <div id="toolbar_help_design">Drag and drop filters to data flow.</div>
                                <div id="toolbar_help_source" style="display:none">Click to add code block at cursor.</div>
                            </div>
                            <%--<div class="analizer_toolbar bottomSection">
                                <div class="title">Click and Add</div>
                                <select class="" id="nodeTypeSelect">
                                </select>
                                <input id="addButton" class="button" value="Add" />
                            </div>--%>
                        </div>
                    </td>
                    <td style="width:100%">
                        <div id="designView">
                            <div id="analizer_content">
                                <div class="sectionSeperator togglebleTitle">Main Configuration Options</div>
                                <div id="analizer_header">
                                    <div class="xmlTreeAttributes borderClass" id="mainAttributes"></div>
                                </div>

                                <div style="clear:both;"></div>
                                <div class="sectionSeperator togglebleTitle" style="margin-top:20px;">Tree</div>
                                <div class="treeContainer">
                                    <%--<div class="init-droppable" style="clear:both; border:5px solid green; background:lime; color:green; height:40px; line-height:40px; text-align:center; font-size:20px;">I have the jstree-drop class</div>--%>
                                    <div id="analizerTreeContainer"></div>
                                </div>
                            </div>
                        </div>

                        <div id="sourceView">
                            <textarea class="source" id="sourceTextArea"><%=analyzerXML%></textarea>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </div>

</div>
<div class="buttonRow">
    <input class="button" type="button" value="Save All" id="saveAllButton"/>
    <input class="button" type="button" value="Cancel" id="cancelAllButton"/>
</div>
<form id="designSourceForm" action="analyzer-config.jsp" method="post">
    <input type="hidden" name="analyzerXML_from_source" id="analyzerXML" />
    <input type="hidden" name="seqname" value="<%if(analyzerSeqName!=null){%><%=analyzerSeqName%><% } %>" />
    <input type="hidden" name="mode" id="mode" value="<%=mode%>" />
</form>
</fmt:bundle>