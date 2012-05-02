<!--
~ Copyright WSO2, Inc. (http://wso2.com)
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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>


<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript">


    $(document).ready(function () {

        if ($("#page").val() == "01") {
            $("#Back").attr("disabled", "disabled");
        }

        $("#Finish").hide();

        $("#Finish").click(function() {
            sendAjaxRequest("generate_gadget_ajaxprocessor.jsp", null);
        })

        $("#Back").click(function() {
            var backURL = "";
            if ($("#page").val() == "02") {
                backURL = "datasource_ajaxprocessor.jsp";
            } else if ($("#page").val() == "03") {
                backURL = "sqlinput_ajaxprocessor.jsp";
            } else if ($("#page").val() == "04") {
                backURL = "pickuielement_ajaxprocessor.jsp";
            }

            var data = $("form").serialize();
            sendAjaxRequest(backURL, data);

        })


        function sendAjaxRequest(url, data) {
            //start the ajax
            $.ajax({
                //this is the php file that processes the data and send mail
                url: url,

                //GET method is used
                type: "POST",

                //pass the data
                data: data,

                //Do not cache the page
                cache: false,

                //success
                success: function (html) {
                    //if process.php returned 1/true (send mail success)
                    //hide the form
                    $('#div-form').fadeOut('fast', function() {

                        $('#div-form').html(html);

                        //show the success message
                        $('#div-form').fadeIn('fast');

                        if ($("#page").val() == "01") {
                            $("#Back").attr("disabled", "disabled");
                        } else {
                            $("#Back").removeAttr('disabled');
                        }

                        if ($("#page").val() == "04") {
                            $("#Finish").show();
                        } else {
                            $("#Finish").hide();
                        }

                    });

                    //if process.php returned 0/false (send mail failed)
                }
            });
        }

        $("#Next").click(function() {
            var jdbcurl = $("[name=jdbcurl]").val();
            var username = $("[name=username]").val();
            var password = $("[name=password]").val();
            var driver = $("[name=driver]").val();

            var nextURL = "";
            if ($("#page").val() == "01") {
               nextURL = "sqlinput_ajaxprocessor.jsp";
            } else if ($("#page").val() == "02") {
               nextURL = "pickuielement_ajaxprocessor.jsp";
            } else if ($("#page").val() == "03") {
               nextURL = "preview_ajaxprocessor.jsp";
            }

            var data = $("form").serialize();

           sendAjaxRequest(nextURL, data);

        });
    });
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

%>

    <div id="middle">
        <h2>Gadget Generator Wizard</h2>

        <div id="workArea">

            <div>
                <h3>Pick Data Source</h3>
            </div>

            <div style="height:130px;" id="div-form">
                <form>
                <p>JDBC URL : <input type="text" size="50%" name="jdbcurl" value="jdbc:h2:/Users/mackie/tmp/jaggery-1.0.0-SNAPSHOT_M4/repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE"/></p>
                <p>Driver Class Name : <input type="text" size="50%" name="driver" value="org.h2.Driver"/></p>
                <p>Username : <input type="text" size="50%" name="username" value="wso2carbon"/></p>
                <p>Password : <input type="text" size="50%" name="password" value="wso2carbon"/></p>
                    <input type="hidden" name="page" id="page" value="01">
                </form>
            </div>
            <div>
                <p>
                    <input type="button" id="Back" value="Back">

                    <input type="button" id="Next" value="Next">

                    <input type="button" id="Finish" value="Finish">

                    </p>
            </div>




        </div>
    </div>
</fmt:bundle>