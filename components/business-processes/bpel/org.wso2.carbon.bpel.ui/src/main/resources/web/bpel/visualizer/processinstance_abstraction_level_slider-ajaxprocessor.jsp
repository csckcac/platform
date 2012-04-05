<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean" %>
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
<table id="ProcessInstanceAbstractionLevelSlider" class="adjuster-inner">
    <thead>
    <tr>
        <th colspan="1" scope="colgroup" class="light-orange">
            <table class="inner light-orange">
                <tbody>
                <tr>
                    <td class="center">
                        <span class="header">
                            <fmt:message
                                    key="slider_process_instance">
                                <fmt:param
                                        value="<%=bean.getSvgDataModel().getPiSlider().getSelection()%>"/>
                                <fmt:param
                                        value="<%=bean.getSvgDataModel().getPiSlider().getMaxRange()%>"/>
                            </fmt:message>
                        </span>
                    </td>
                </tr>
                </tbody>
            </table>
        </th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="center">
            <input type="text"
                   class="fd_range_0_3 fd_hide_input fd_callback_updateSlider"
                   value="<%=bean.getSvgDataModel().getPiSlider().getSelection()%>"
                   name="form_svg_pi_slider" id="form:svg:pi:slider"
                   style="display: none;"/>
            <input type="button" value="<fmt:message key="change" />"
                   name="pi_slider_change" id="pi_slider_change" >
        </td>
    </tr>
    </tbody>
</table>
<script type="text/javascript">
    $("input[name=pi_slider_change]").click(function() {
        submitSettings(this.name);
        reloadSVG();
    });
</script>
</fmt:bundle>