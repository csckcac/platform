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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="java.util.ResourceBundle" %>
<%@page import="java.lang.Exception"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.AttributeValueTreeNodeDTO" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="java.util.Set" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%!
    public void printChildrenTree(AttributeValueTreeNodeDTO node, JspWriter out) throws IOException {
        if(node != null){
            AttributeValueTreeNodeDTO[] children = node.getChildNodes();
            if(children != null  && children.length > 0){
                out.write("<li><a class='plus' onclick='treeColapse(this)'>&nbsp;</a> " +
                          "<a class='treeNode' onclick='selectMe(this)'>" + node.getName() + "</a>");
                out.write("<ul style='display:none'>");
                for(AttributeValueTreeNodeDTO child : children){
                    printChildrenTree(child, out);
                }
                out.write("</ul>");
            } else {
                out.write("<li><a class='minus' onclick='treeColapse(this)'>&nbsp;</a> " +
                          "<a class='treeNode' onclick='selectMe(this)'>" + node.getName() + "</a>");                
                out.write("</li>");
            }
        }
    }

    public void printChildren(AttributeValueTreeNodeDTO node, String parentNodeName, JspWriter out) throws IOException {
        if(node != null){
            String nodeName;
            if(parentNodeName != null && parentNodeName.trim().length() > 0){
                nodeName = parentNodeName + "/" + node.getName();
            } else {
               nodeName = node.getName();                
            }

            out.write("<li><a class='treeNode' onclick='selectMe(this)'>" + nodeName + "</a></li>") ;
            AttributeValueTreeNodeDTO[] children = node.getChildNodes();
            if(children != null  && children.length > 0){
                for(AttributeValueTreeNodeDTO child : children){
                    printChildren(child, nodeName, out);
                }
            }
        }
    }

%>

<%
    String forwardTo;
    Set<AttributeValueTreeNodeDTO> nodeDTO = null;
    AttributeValueTreeNodeDTO selectedTree = null;
    String selectedFinderModule;
    String attributeType;

//    BasicTargetElementDTO basicTargetElementDTO = entitlementPolicyBean.getBasicTargetElementDTO();
//    String resourceList  = basicTargetElementDTO.getResourceList();
//    String[] resourceNames = resourceList.split(",");

    String ruleId = (String)request.getParameter("ruleId");
    selectedFinderModule = (String) request.getParameter("finderModule");
    if(selectedFinderModule == null || selectedFinderModule.trim().length() < 1){
        selectedFinderModule = EntitlementPolicyConstants.DEFAULT_META_DATA_MODULE_NAME;
    }

    attributeType = (String) request.getParameter("attributeType");

    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        nodeDTO = entitlementPolicyBean.getAttributeValueNodeMap(attributeType);
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.retrieving.attribute.values");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="advance.search"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

    
     <!--Yahoo includes for dom event handling-->
    <script src="http://yui.yahooapis.com/2.8.1/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

    <!--Yahoo includes for animations-->
    <script src="http://yui.yahooapis.com/2.8.1/build/animation/animation-min.js" type="text/javascript"></script>

    <!--Local js includes-->
    <script type="text/javascript" src="js/treecontrol.js"></script>
    <script type="text/javascript" src="js/popup.js"></script>

    <link href="css/tree-styles.css" media="all" rel="stylesheet" />
    <link href="css/dsxmleditor.css" media="all" rel="stylesheet" />
<script type="text/javascript">

    function getFinderModule() {
        var comboBox = document.getElementById("finderModule");
        var finderModule = comboBox[comboBox.selectedIndex].value;
        location.href = 'select_attribute_values.jsp?finderModule=' + finderModule
                        + "&attributeType=" + '<%=attributeType%>' + "&ruleId=" + '<%=ruleId%>';
    }

    function createInputs(value){
        var mainTable = document.getElementById('mainTable');
        var newTr = mainTable.insertRow(mainTable.rows.length);
        var cell1 = newTr.insertCell(0);
        cell1.innerHTML = '<input type="hidden" name="resourceName'+ mainTable.rows.length
                +'" id="resourceName'+ mainTable.rows.length +'" value="' + value + '"/>';
    }

    function submitForm(fullPathSupported){
        for(var i in paths){
            if(fullPathSupported){
                createInputs(paths[i].path);
            } else {
                createInputs(paths[i].name);
            }
        }
        document.attributeValueForm.action = "policy-editor.jsp?attributeType="
                + '<%=attributeType%>' +"&ruleId=" + '<%=ruleId%>' ;
        document.attributeValueForm.submit();
    }

    function doCancel(){
        document.attributeValueForm.action = "policy-editor.jsp?ruleId=" + '<%=ruleId%>';
        document.attributeValueForm.submit();
    }

    <%--var selectedPaths = new Array();--%>
    <%--<%--%>
        <%--for(int i=0;i<resourceNames.length;i++){--%>
            <%--%>--%>
                <%--selectedPaths.push('<%=resourceNames[i]%>');--%>
            <%--<%--%>
        <%--}--%>
    <%--%>--%>
    <%--if(selectedPaths.length > 0){--%>
        
    <%--}--%>
    <%--jQuery(document).ready(--%>
            <%--function(){--%>
                <%--pickNames();           --%>
            <%--}--%>
    <%--);--%>



</script>

<div id="middle">
    <h2><fmt:message key="select.attribute.values"/></h2>
    <div id="workArea">
        <form id="attributeValueForm" name="attributeValueForm" method="post" action="policy-editor.jsp">


        <table width="60%" id="userAdd" class="styledLeft">
        <%--<thead>--%>
        <%--<tr>--%>
            <%--<th>Attribute Finder Module</th>--%>
        <%--</tr>--%>
        <%--</thead>--%>
        <tbody>
        <tr>
            <td class="formRaw">
            <table class="normal" cellpadding="0" cellspacing="0" class="treeTable" style="width:100%">
            <tr>
                <td>
                <table>
                <tr>
                    <td class="leftCel-med">
                        <fmt:message key="attribute.finder.module"/>
                    </td>
                    <td>
                        <select onchange="getFinderModule();" id="finderModule" name="finderModule" class="text-box-big">
                            <option value="<%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%>" selected="selected">
                                <%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%></option>
                        <%
                            if (nodeDTO != null && nodeDTO.size() > 0) {
                                for (AttributeValueTreeNodeDTO node : nodeDTO) {
                                    if(selectedFinderModule.equals(node.getModuleName())){
                                        selectedTree = node;
                        %>
                              <option value="<%=selectedFinderModule%>" selected="selected"><%=selectedFinderModule%></option>
                        <%
                                    } else {
                        %>
                              <option value="<%=node.getModuleName()%>"><%=node.getModuleName()%></option>
                        <%
                                    }
                                }
                            }
                        %>
                        </select>
                    </td>
                </tr>

                <%
                    if(selectedTree != null && selectedTree.getAttributeDataTypes() != null){
                %>
                <tr>
                    <td>
                        <fmt:message key="select.attribute.dataType"/>
                    </td>

                    <td>
                        <select type="hidden" id="selectedAttributeDataType" name="selectedAttributeDataType" class="text-box-big">
                        <%
                            for (String attributeDataType : selectedTree.getAttributeDataTypes()) {
                        %>
                              <option value="<%=attributeDataType%>"><%=attributeDataType%></option>
                        <%
                            }
                        %>
                        </select>
                    </td>
                </tr>
                <%
                    }
                %>

                <%
                    if(selectedTree != null && selectedTree.getSupportedAttributeIds() != null){
                %>
                <tr>
                    <td>
                        <fmt:message key="select.attribute.id"/>
                    </td>

                    <td>
                        <select id="selectedAttributeId" name="selectedAttributeId" class="text-box-big">
                        <%
                            for (String attributeId : selectedTree.getSupportedAttributeIds()) {
                        %>
                              <option value="<%=attributeId%>"><%=attributeId%></option>
                        <%
                            }
                        %>
                        </select>
                    </td>
                </tr>
                <%
                    }
                %>
                </table>
                </td>
                </tr>

                <tr>
                    <td>
                        <table id="mainTable" class="styledLeft noBorders" style="display:none">
                        </table>
                    </td>
                </tr>
                <tr>
                <td colspan="2">
                    <table cellpadding="0" cellspacing="0" class="treeTable" style="width:100%">
                    <thead>
                        <tr>
                            <th ><fmt:message key="attribute.values"/></th>
                            <th  style="background-image:none;border:none"></th>
                            <th><fmt:message key="selected.attribute.values"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                             <%
                                if(selectedTree != null){
                             %>
                            <td style="width: 500px;border:solid 1px #ccc">
                                <div class="treeControl">
                                <ul>
                            <%
                                if(selectedTree.getHierarchicalTree()){
                                    AttributeValueTreeNodeDTO[] childNodes = selectedTree.getChildNodes();
                                    if(childNodes != null && childNodes.length > 0){
                                        for(AttributeValueTreeNodeDTO childNode : childNodes){
                                            printChildrenTree(childNode , out);
                                        }
                                    }
                                } else {
                                    AttributeValueTreeNodeDTO[] childNodes = selectedTree.getChildNodes();
                                    if(childNodes != null && childNodes.length > 0){
                                        for(AttributeValueTreeNodeDTO childNode : childNodes){
                                            printChildren(childNode, selectedTree.getName(), out);
                                        }
                                    }
                                }
                            %>
                                </ul>
                                </div>
                            </td>
                            <td style="width:50px;vertical-align: middle;border-bottom:solid 1px #ccc">
                                <input class="button" value=">>" onclick="pickNames(<%=selectedTree.getFullPathSupported()%>)" style="width:30px;margin:10px;" />
                            </td>
                            <td style="border:solid 1px #ccc"><div style="overflow: auto;height:300px" id="listView"></div>
                            </td>
                            <%
                                }
                            %>
                        </tr>
                    </tbody>
                    </table>
                </td>
                </tr>

                <tr>
                    <td class="buttonRow" >
                         <%
                        if(selectedTree != null){
                        %>
                            <input type="button" onclick="submitForm('<%=selectedTree.getFullPathSupported()%>')" value="<fmt:message key="add"/>"  class="button"/>
                        <%
                            }
                        %>
                        <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />" class="button"/>
                    </td>
                </tr>
                </table>
                </td>                
            </tr>
            </tbody>
        </table>
        </form>
    </div>
</div>
</fmt:bundle>

