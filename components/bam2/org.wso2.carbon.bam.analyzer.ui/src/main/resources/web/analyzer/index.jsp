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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.AnalyzerAdminClient" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript" src="js-lib/jstree/_lib/jquery.js"></script>
<script type="text/javascript" src="js/xmlOperations.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript">
    jQuery(function() {

        jQuery.ajax({
            url:"bam.xml",
            dataType:"xml",
            complete:function(xml) {

                if (xml.status == 200) {
                    _xmlObj = xml.responseXML;
                    CreateHtml(xml.responseXML);
                }
                else {
                    xmlDoc.createNewDoc();
                }
            }
        });
        jQuery("#addColumnFamily").click(function() {
            var _colFamilyName = $("#columnFamily").val(),
                    _defaultCF = $("#defaultCF").is(":checked"),
                    _granularity = $("#granularity").val(),
                    _indexRowKey = $("#indexRowKey").val();
            xmlDoc.creteNode(_colFamilyName, _defaultCF, _granularity, _indexRowKey, _xmlObj);
        });
    });
    function changeState(active) {
        jQuery.ajax({
            url:'change_state_ajaxprocessor.jsp?isActive=' + active,
            dataType:"xml",
            complete:function(xml) {

                if (xml.status == 200) {

                    if (active) {
                        jQuery('#activeDiv').show();
                        jQuery('#inactiveDiv').hide();
                    } else {
                        jQuery('#activeDiv').hide();
                        jQuery('#inactiveDiv').show();
                    }

                } else if (xml.status == 404) {
                    alert('page not found');
                }
            }
        });

    }
    function deleteXML(analyzerName) {
        CARBON.showConfirmationDialog('The analyzer "' + analyzerName + '" will be deleted immediately.Are you sure you want to continue?',
                function() {
                    window.location.href = 'deleteAnalyzer.jsp?seqname=' + analyzerName;
                },
                function() {

                }
                );
    }
</script>
<style type="text/css">
    #configXml_Cont ul {
        list-style: none;
        padding: 2px;
    }

    #configXml_Cont ul.secondLevel {
        padding: 10px 20px;
        background: #FFFFD0;
        display: none;
    }

    #configXml_Cont li {
        clear: both;
        overflow: hidden;
    }

    #configXml_Cont a.showHideParts {
        float: right;
        margin: 0 20px 0 0;
    }

    #configXml_Cont a.addPartsLink {
        float: right;
        padding: 10px 20px 10px 0;
    }

    #configXml_Cont #accordion {
        padding: 10px;
    }

    #configXml_Cont .deleteNodeLink {
        padding: 0 10px;
    }

    #configXml_Cont input[type="text"] {
        width: 100px;
    }

    #configXml_Cont div.textBlock {
        padding: 0 10px;
        float: left;
    }

    #configXml_Cont div.addNewCFBtn, div.saveXmlBtn {
        clear: both;
        width: 100%;
        text-align: right;
    }

    #configXml_Cont #addNewColumnFamily {
        clear: both;
        background: #e8e8e8;
        padding: 10px 6px;
    }
</style>
<!--link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/-->
<fmt:bundle basename="org.wso2.carbon.bam.analyzer.ui.i18n.Resources">
    <carbon:breadcrumb label="main.analyzer"
                       resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <%

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        IndexAdminClient indexAdminClient = new IndexAdminClient(cookie, serverURL, configContext);

        int indexCount = 0;
        IndexDTO[] indexes = null;

        int indexPageNumber = 0;
        try {
            indexes = indexAdminClient.getIndexMetaData(indexPageNumber, IndexAdminClient.INDEXES_PER_PAGE);
            indexCount = indexAdminClient.getIndexCount();
        } catch (AxisFault e) {
            String credentialsNotSupplied = "Credentials invalid or not supplied yet.";

            String errorString;
//            System.out.println(e.getMessage());
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

        AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);

        String[] analyzers = client.getAnalyzerXMLs();
        String[] analyzerNames = null;
        if (analyzers != null) {
            analyzerNames = new String[analyzers.length];
            for (int i = 0; i < analyzers.length; i++) {
                String analyzer = analyzers[i];
                OMElement analyzerSeq = AXIOMUtil.stringToOM(analyzer);
                analyzerNames[i] = analyzerSeq.getAttribute(new QName("name")).getAttributeValue();
            }
        }
    %>

    <div id="middle">
        <h2>Analyzer Sequences & Indexes</h2>

        <div id="workArea">

            <div>
                <h3>Sequences</h3>
            </div>

            <div style="height:25px;">
                <a href="analyzer-config.jsp?mode=new"
                   style="background-image: url(../admin/images/add.gif);" class="icon-link">Add
                                                                                             Sequence</a>
            </div>
            <% if (analyzerNames != null) { %>
            <table width="100%" id="sgTable" class="styledLeft">
                <thead>
                <tr>

                    <th>Sequences</th>
                    <th>Actions</th>

                </tr>
                </thead>
                <tbody>

                <%

                    for (int i = 0; i < analyzerNames.length; i++) {
                %>
                <tr>
                    <td><%=analyzerNames[i]%>
                    </td>
                    <td style="width:320px">


<%--                        <nobr>

                            <div id="activeDiv">


                                <span style="background-image:url(images/activate.gif);" class="icon-text">Active&nbsp; &nbsp;[</span>
                                <a onclick="changeState(false); return false;" title="Deactivate this service"
                                   style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
                                   class="icon-link" href="#">Deactivate</a>
                                <span style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
                                      class="icon-text">]</span>


                            </div>
                            <div id="inactiveDiv" style="display:none">
                                <span style="background-image:url(images/deactivate.gif);"
                                      class="icon-text">Inactive&nbsp; &nbsp;[</span><a
                                                    onclick="changeState(true); return false;" title="Activate this service"
                                                    style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
                                                    class="icon-link" href="#">Activate</a>
                                <span style="background-image:none !important; margin-left: 2px !important; padding-left: 2px !important;"
                                      class="icon-text">]</span>
                            </div>
                        </nobr>--%>


                        <a style="background-image:url(../admin/images/edit.gif);" class="icon-link"
                           onclick=""
                           href="analyzer-config.jsp?seqname=<%=analyzerNames[i]%>&mode=edit">Edit</a>
                        <a style="background-image:url(../admin/images/delete.gif);" class="icon-link"
                           onclick=""
                           href="deleteAnalyzer.jsp?seqname=<%=analyzerNames[i]%>">Delete</a>


                    </td>
                </tr>

                <%
                }
                %>
                </tbody>
            </table>
            <% } %>

        </div>
    </div>
</fmt:bundle>