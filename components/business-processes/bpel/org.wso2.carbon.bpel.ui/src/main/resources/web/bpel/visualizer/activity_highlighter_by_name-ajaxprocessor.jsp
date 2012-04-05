<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean" %>
<%@ page
        import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg.SelectItem" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!--
~ Copyright (c) 20012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="visualizer/resources/scripts/main.js"></script>
<%
    String instanceId = request.getParameter("instanceId");
    String mainBeanId = MainBeanUtil.generateMainBeanId(instanceId);
    /* Get the MainBean from the session */
    MainBean bean = (MainBean) session.getAttribute(mainBeanId);
%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<table id="activity_highlighter_by_name_table" class="adjuster-inner">
    <thead>
    <tr>
        <th colspan="3" scope="colgroup" class="light-orange">
            <table class="inner light-orange">
                <tbody>
                <tr>
                    <td class="inner left"><fmt:message
                            key="activity_name_hightlighting"/></td>
                    <td class="inner right">
                        <a name="svg_highlighting_names_viewmode" >
                            <%
                                if (bean.getSvgDataModel().getNameHighlighting().getViewMode().isMaximised()) {
                            %>
                            <img src="./visualizer/resources/icons/actions/minimise.png"
                                 alt="<fmt:message key="tooltip_minimise" />"
                                 title="<fmt:message key="tooltip_minimise" />"
                                 class="border"/>
                            <%
                            } else {
                            %>
                            <img src="./visualizer/resources/icons/actions/maximise.png"
                                 alt="<fmt:message key="tooltip_maximise" />"
                                 title="<fmt:message key="tooltip_maximise" />"
                                 class="border"/>
                            <%
                                }
                            %>
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>
        </th>
    </tr>
    </thead>
    <tbody>
    <%
        if (bean.getSvgDataModel().getNameHighlighting().getViewMode().isMaximised()) {
    %>
    <tr>
        <td class="center">
            <select size="8" multiple="multiple"
                    name="activitynames_highlighting_left">
                <%
                    /*
                                                       * Show all the items on the LEFT of the NAME HIGHLIGHTING section
                                                       */
                    for (SelectItem activityName : bean.getSvgDataModel().getNameHighlighting().getItemsLeft()) {
                %>
                <option value="<%=activityName.getValue()%>"><%=activityName.getValue()%>
                </option>
                <%
                    }
                %>
            </select>
        </td>
        <td class="center">
            <input type="button" value="<fmt:message key="add" />"
                   name="activitynames_highlighting_add" id="activitynames_highlighting_add" class="activitynames_highlighting">
            <br/>
            <input type="button" value="<fmt:message key="remove" />"
                   name="activitynames_highlighting_remove" id="activitynames_highlighting_remove" class="activitynames_highlighting">
            <br/>
            <br/>
            <input type="button" value="<fmt:message key="addAll" />"
                   name="activitynames_highlighting_addall" id="activitynames_highlighting_addall" class="activitynames_highlighting">
            <br/>
            <input type="button" value="<fmt:message key="removeAll" />"
                   name="activitynames_highlighting_removeall" id="activitynames_highlighting_removeall" class="activitynames_highlighting">
        </td>
        <td class="center">
            <select size="8" multiple="multiple"
                    name="activitynames_highlighting_right">
                <%
                    /*
                                                       * Show all the items on the RIGHT of the NAME HIGHLIGHTING section
                                                       */
                    for (SelectItem activityName : bean.getSvgDataModel().getNameHighlighting().getItemsRight()) {
                %>
                <option value="<%=activityName.getValue()%>"><%=activityName.getValue()%>
                </option>
                <%
                    }
                %>
            </select>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<script type="text/javascript">
    $("input[class=activitynames_highlighting]").click(function() {
        submitSettings(this.name);
        reloadTable("activity_highlighter_by_name_table");
        reloadSVG();
    });

    $("a[name=svg_highlighting_names_viewmode]").click(function() {
        minMax(this.name);
        reloadTable("activity_highlighter_by_name_table");
    });
</script>
</fmt:bundle>