<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources">
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

 <%
     String success = request.getParameter("success");
     String message = "";
     if(null != success && success.equalsIgnoreCase("false")){
         message = request.getParameter("message");
     }
 %>
<script type="text/javascript">
    function deployToolBox(){
        var toolbox = document.getElementById('toolbox').value;
        if('' == toolbox ){
              CARBON.showErrorDialog('No ToolBox has been selected!');
        }   else if(toolbox.indexOf('.bar') == -1){
             CARBON.showErrorDialog('The ToolBox should be \'bar\' artifact');
        }else{
             document.getElementById('uploadBar').submit();
        }

    }

    function cancelDeploy(){
        location.href = "../bam-toolbox/list.bar";
    }
</script>

  <%--<script type="text/javascript">--%>
        <%--jQuery(document).ready(function() {--%>
            <%--var message = '<%=message%>';--%>
            <%--if(message != ''){--%>
                <%--CARBON.showErrorDialog(message);--%>
            <%--}--%>
          <%--});--%>
    <%--</script>--%>

<div id="middle">
    <h2>Add Tool Box</h2>

    <div id="workArea">

        <form id="uploadBar" name="uploadBar" enctype="multipart/form-data" action="../../fileupload/bamToolboxDeploy" method="POST">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="upload.bar"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>


                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>

                            <tr>
                                <td width="180px"><fmt:message key="bar.artifact"/> <span
                                        class="required">*</span></td>
                                <td><input type="file" name="toolbox"
                                           id="toolbox" size="100px"/>
                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </td>
                </tr>

                <table class="normal-nopadding">
                    <tbody>

                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" value="<fmt:message key="deploy"/>"
                                   class="button" name="deploy"
                                   onclick="javascript:deployToolBox();"/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   name="cancel" class="button"
                                   onclick="javascript:cancelDeploy();"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                </tbody>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>
