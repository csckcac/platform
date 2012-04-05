
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String ruleId = (String)request.getParameter("ruleId");
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="select.registry.resource"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>

<script type="text/javascript">

    function addNewRow(){

        var mainTable = document.getElementById('mainTable');
        var newTr = mainTable.insertRow(mainTable.rows.length);
        var cell1 = newTr.insertCell(0);
        cell1.innerHTML = '<input type="text" size="60" readonly="readonly"  name="resourceName'+ mainTable.rows.length +'" id="resourceName'+mainTable.rows.length+'"/>' ;

        var cell2 = newTr.insertCell(1);
        cell2.innerHTML = '<label for="policyFromRegistry"><a class="registry-picker-icon-link" style="padding-left:30px;cursor:pointer;color:#386698" onclick="showRegistryBrowser(\'resourceName'+mainTable.rows.length+'\',\'/_system/config\');"><fmt:message key="conf.registry"/></a></label>'
                +'<label for="policyFromRegistry"><a class="registry-picker-icon-link" style="padding-left:30px;cursor:pointer;color:#386698" onclick="showRegistryBrowser(\'resourceName'+mainTable.rows.length+'\',\'/_system/governance\');"><fmt:message key="gov.registry"/></a></label>'
                +'<a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a>';
    }

    function removeRow(link){
        link.parentNode.parentNode.parentNode.removeChild(link.parentNode.parentNode);

    }

    function submitForm(){
        <%--document.resourceForm.action = "create-basic-policy.jsp?cellNo=<%=cellNo%>";--%>
        document.resourceForm.action = "create-basic-policy.jsp?ruleId=" + '<%=ruleId%>';
        document.resourceForm.submit();
    }

    function cancelForm(){
       <%--location.href = 'create-basic-policy.jsp?cellNo=<%=cellNo%>';--%>
       location.href = 'create-basic-policy.jsp?ruleId=' + '<%=ruleId%>';
    }

</script>

    <div id="middle">
        <h2><fmt:message key="select.resources.registry"/></h2>

            <form id="resourceForm" name="resourceForm" method="post" action="">
            <div id="workArea">
            <a onclick="addNewRow()" style='background-image:url(images/add.gif);' type="button" class="icon-link">Add new resource</a> 
            <table class="styledLeft noBorders">
                <tr>
                <td class="nopadding">
                    <table cellspacing="0" id="mainTable" style="width:100%;border:none !important">
                        <thead>
                            <th class="leftCol-small">Resource Path</th>
                            <th>Action</th>
                        </thead>
                        <tbody>
                        <tr id="firstRow">
                            <td>
                                    <input type="text" name="resourceName1" id="resourceName1"  size="60" readonly="readonly"/>
                            </td>
                            <td>
                                <label for="resourceName1">
                                    <a class="registry-picker-icon-link"
                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                       onclick="showRegistryBrowser('resourceName1','/_system/config');"
                                            ><fmt:message key="conf.registry"/>
                                    </a>
                                </label>
                                <label for='resourceName1'>
                                    <a class="registry-picker-icon-link"
                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                       onclick="showRegistryBrowser('resourceName1','/_system/governance');">
                                        <fmt:message key="gov.registry"/>
                                    </a>
                                </label>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>

                </tr>
                <tr>
                    <td class="buttonRow">
                    <input type="button" value="<fmt:message key="finish"/>" onclick="submitForm();" class="button"/>
                    <input type="button" value="<fmt:message key="cancel" />" onclick="cancelForm();"  class="button"/>
                    </td>
                </tr>
            </table>

        </div>
        </form>
    </div>
</fmt:bundle>
