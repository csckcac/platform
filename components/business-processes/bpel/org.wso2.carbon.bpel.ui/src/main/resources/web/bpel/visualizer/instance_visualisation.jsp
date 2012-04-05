<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend.util.ProcessInstanceUtil" %>
<%@ page
        import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.ChangeSettingsUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend.ProcessInstanceBean" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter.AuthenticationManager" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!--
~ Copyright (c) 20011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%
    /**Storing the backend server url and session cookie to be reused during visualization tasks*/
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AuthenticationManager.init(backendServerURL, cookie);

    final Log log = LogFactory.getLog("instance_visualization.jsp");
    String instanceId = request.getParameter("iid").trim();
    if (instanceId == null) {
        log.error("instanceId is null", new NullPointerException("instanceId is null"));
    }
    String mainBeanId = MainBeanUtil.generateMainBeanId(instanceId);

    /* Get the MainBean from the session */
    MainBean bean = (MainBean) session.getAttribute(mainBeanId);
    if (bean == null) {
        bean = new ProcessInstanceBean(instanceId);
        session.setAttribute(mainBeanId, bean);
    }

    int rowIndex = ProcessInstanceUtil.getRowIndexForProcessInstanceID(bean, instanceId);
    ChangeSettingsUtil.selectProcessInstance(bean, rowIndex);
%>

<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/bpimain.css" />
<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/slider.css" />
<link rel="stylesheet" media="screen" type="text/css" href="visualizer/resources/styles/svg.css" />

<script type="text/javascript" src="visualizer/resources/scripts/main.js"></script>
<script type="text/javascript" src="visualizer/resources/scripts/slider.js"></script>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">

    <div id="bpi-body">
        <form id="bpiform" name="bpiform"
              enctype="application/x-www-form-urlencoded">
            <div id="svg-viewer">

        <table id="form:svg:table" class="bpi">
            <thead>
            <tr>
                <th class="orange" scope="colgroup">
                    <jsp:include page="activity_highlighter_by_type-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
                <th class="orange" scope="colgroup">
                    <jsp:include page="activity_highlighter_by_name-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
            </tr>
            <tr>
                <th class="orange" scope="colgroup">
                    <jsp:include page="activity_omitter_by_type-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
                <th class="orange" scope="colgroup">
                    <jsp:include page="activity_omitter_by_name-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
            </tr>
            <tr>
                <th class="orange" scope="colgroup">
                    <jsp:include page="processmodel_abstraction_level_slider-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
                <th class="orange" scope="colgroup">
                    <jsp:include page="processinstance_abstraction_level_slider-ajaxprocessor.jsp">
                        <jsp:param name="instanceId" value="<%=instanceId%>" />
                    </jsp:include>
                </th>
            </tr>
            </thead>
            <tbody>

            <tr>
                <td class="center" colspan="2">
                    <jsp:include page="svg_generator-ajaxprocessor.jsp">
                        <jsp:param name="id" value="<%=instanceId%>" />
                    </jsp:include>
                </td>
            </tr>
            </tbody>
        </table>

            </div>
        </form>
    </div>

    <div id="bpi-dummy-body">

    </div>
<script type="text/javascript">
    //To avoid js caching globally
    $(document).ready(function() {
        $.ajaxSetup({cache: false});
    });

    /**
    * This is invoked when the settings of each table need to be passed to the changesettings servlet
     * Based on the clicked button, the the params passed to the servelet vary. So name of the clicked button need to be passed as a input param
     * @param clickedButton the name of the clicked button
     */
    function submitSettings (clickedButton) {
        $.ajax({
            type: "GET",
            cache: false,
            url: "changesettings",
            data: clickedButton + "&iid=<%=instanceId%>&" + $("#bpiform").serialize(),
            async:false,
            error:function() {
                alert("An error occurred");
            }
        });
    }

    /**
    * This is called when the relevant to changes need to rectified in the pageElements.
     * As the svg table need to be reloaded at each change, it's added as a common reload to this function.
     * @param pageElementToBeReloaded the id of the element to be reloaded
     */
    function reloadTable (pageElementToBeReloaded) {
        var ajax_load = "<img src='visualizer/resources/icons/load.gif' alt='loading...' />";
        var load_activity_highlighter_by_type_url = "visualizer/activity_highlighter_by_type-ajaxprocessor.jsp?instanceId=<%=instanceId%>";
        var load_activity_highlighter_by_name_url = "visualizer/activity_highlighter_by_name-ajaxprocessor.jsp?instanceId=<%=instanceId%>";
        var load_activity_omitter_by_type_url = "visualizer/activity_omitter_by_type-ajaxprocessor.jsp?instanceId=<%=instanceId%>";
        var load_activity_omitter_by_name_url = "visualizer/activity_omitter_by_name-ajaxprocessor.jsp?instanceId=<%=instanceId%>";

        var jQueryElementIdToBeReloaded = "#" + pageElementToBeReloaded;

        if (pageElementToBeReloaded == "activity_highlighter_by_type_table") {
            $(jQueryElementIdToBeReloaded).html(ajax_load).load(load_activity_highlighter_by_type_url); //this #activity_highlighter_by_type_table resides in activity_highlighter_by_type-ajaxprocessor.jsp
        } else if (pageElementToBeReloaded == "activity_highlighter_by_name_table") {
            $(jQueryElementIdToBeReloaded).html(ajax_load).load(load_activity_highlighter_by_name_url); //this #activity_highlighter_by_name_table resides in activity_highlighter_by_name-ajaxprocessor.jsp
        } else if (pageElementToBeReloaded == "activity_omitter_by_type_table") {
            $(jQueryElementIdToBeReloaded).html(ajax_load).load(load_activity_omitter_by_type_url); //this #activity_omitter_by_type_table resides in activity_omitter_by_type-ajaxprocessor.jsp
        } else if (pageElementToBeReloaded == "activity_omitter_by_name_table") {
            $(jQueryElementIdToBeReloaded).html(ajax_load).load(load_activity_omitter_by_name_url); //this #activity_omitter_by_name_table resides in activity_omitter_by_name-ajaxprocessor.jsp
        } else {
            alert("mentioned element is not defined");
        }
    }

    /**
    * Reload the svg table defined with id svg_table
     */
    function reloadSVG () {
        var ajax_load = "<img src='visualizer/resources/icons/load.gif' alt='loading...' />";
        var load_svg_url = "visualizer/svg_generator-ajaxprocessor.jsp?id=<%=instanceId%>";

        $("#svg_table").html(ajax_load).load(load_svg_url); //this #svg_table resides in svg_generator-ajaxprocessor.jsp
    }

    /**
    * This method is used in maximize/minimize buttons
    * @param clickedButton the id of the element to be reloaded
     */
    function minMax(clickedButton) {
        $.ajax({
            type: "GET",
            cache: false,
            url: "changesettings",
            data: clickedButton + "&iid=<%=instanceId%>",
            async:false,
            error:function() {
                alert("An error occurred");
            }
        });
    }
</script>
</fmt:bundle>