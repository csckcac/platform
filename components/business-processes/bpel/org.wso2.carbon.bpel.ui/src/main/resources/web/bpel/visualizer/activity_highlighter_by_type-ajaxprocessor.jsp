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
<%
    String instanceId = request.getParameter("instanceId");
    String mainBeanId = MainBeanUtil.generateMainBeanId(instanceId);
    /* Get the MainBean from the session */
    MainBean bean = (MainBean) session.getAttribute(mainBeanId);
%>

<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
<table id="activity_highlighter_by_type_table" class="adjuster-inner">
    <thead>
    <tr>
        <th colspan="3" scope="colgroup" class="light-orange">
            <table class="inner light-orange">
                <tbody>
                <tr>
        <td class="inner left"><fmt:message key="activity_type_hightlighting"/></td>
        <td class="inner right"><a name="svg_highlighting_types_viewmode" >
            <%
                if (bean.getSvgDataModel().getTypeHighlighting().getViewMode().isMaximised()) {
            %>
            <img src="./visualizer/resources/icons/actions/minimise.png"
                 alt="<fmt:message key="tooltip_minimise" />"
                 title="<fmt:message key="tooltip_minimise"/>" class="border"/>
            <%
            } else {
            %>
            <img src="./visualizer/resources/icons/actions/maximise.png"
                 alt="<fmt:message key="tooltip_maximise" />"
                 title="<fmt:message key="tooltip_maximise"/>" class="border"/>
            <%
                }
            %>
        </a></td>
                    </tr>
                </tbody>
            </table>
        </th>
    </tr>
    </thead>
    <tbody>
    <%
        if (bean.getSvgDataModel().getTypeHighlighting().getViewMode().isMaximised()) {
    %>
    <tr>
        <td class="center">
            <select size="8" multiple="multiple"
                    name="activitytypes_highlighting_left">
                <%
                    /*
                    * Show all the items on the LEFT of the TYPE HIGHLIGHTING section
                    */
                    for (SelectItem activityType : bean.getSvgDataModel().getTypeHighlighting().getItemsLeft()) {
                %>
                <option value="<%=activityType.getValue()%>"><%=activityType.getValue()%>
                </option>
                <%
                    }
                %>
            </select>
        </td>
        <td class="center">
            <input type="button" value="<fmt:message key="add" />"
                   name="activitytypes_highlighting_add" id="activitytypes_highlighting_add" class="activitytypes_highlighting" >
            <br/>
            <input type="button" value="<fmt:message key="remove" />"
                   name="activitytypes_highlighting_remove" id="activitytypes_highlighting_remove" class="activitytypes_highlighting">
            <br/>
            <br/>
            <input type="button" value="<fmt:message key="addAll" />"
                   name="activitytypes_highlighting_addall" id="activitytypes_highlighting_addall" class="activitytypes_highlighting">
            <br/>
            <input type="button" value="<fmt:message key="removeAll" />"
                   name="activitytypes_highlighting_removeall" id="activitytypes_highlighting_removeall" class="activitytypes_highlighting">
        </td>
        <td class="center">
            <select size="8" multiple="multiple"
                    name="activitytypes_highlighting_right">
                <%
                    /*
                                                       * Show all the items on the RIGHT of the TYPE HIGHLIGHTING section
                                                       */
                    for (SelectItem activityType : bean.getSvgDataModel().getTypeHighlighting().getItemsRight()) {
                %>
                <option value="<%=activityType.getValue()%>"><%=activityType.getValue()%>
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
    $("input[class=activitytypes_highlighting]").click(function() {
        submitSettings(this.name);
        reloadTable("activity_highlighter_by_type_table");
        reloadSVG();
    });

    $("a[name=svg_highlighting_types_viewmode]").click(function() {
        minMax(this.name);
        reloadTable("activity_highlighter_by_type_table");
    });
</script>
</fmt:bundle>