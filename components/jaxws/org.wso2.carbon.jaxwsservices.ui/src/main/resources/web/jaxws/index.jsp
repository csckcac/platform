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

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.jaxwsservices.ui.i18n.Resources">
    <carbon:breadcrumb label="jaxwsservices.headertext"
                       resourceBundle="org.wso2.carbon.jaxwsservices.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript">

        function validate() {

            // ~!@#$;%^*()+={}[]|\<> are invalid charactors for hierarchical services
            if (document.jaxwsUpload.serviceHierarchy.value != null) {
            var serviceHierarchy = document.jaxwsUpload.serviceHierarchy.value;
                if (serviceHierarchy.lastIndexOf("~") != -1 || serviceHierarchy.lastIndexOf("!") != -1 || serviceHierarchy.lastIndexOf("@") != -1
                        || serviceHierarchy.lastIndexOf("#") != -1 || serviceHierarchy.lastIndexOf("$") != -1 || serviceHierarchy.lastIndexOf(";") != -1
                        || serviceHierarchy.lastIndexOf("%") != -1 || serviceHierarchy.lastIndexOf("^") != -1 || serviceHierarchy.lastIndexOf("*") != -1
                        || serviceHierarchy.lastIndexOf("(") != -1 || serviceHierarchy.lastIndexOf(")") != -1 || serviceHierarchy.lastIndexOf("+") != -1
                        || serviceHierarchy.lastIndexOf("=") != -1 || serviceHierarchy.lastIndexOf("{") != -1 || serviceHierarchy.lastIndexOf("}") != -1
                        || serviceHierarchy.lastIndexOf("[") != -1 || serviceHierarchy.lastIndexOf("]") != -1 || serviceHierarchy.lastIndexOf("|") != -1
                        || serviceHierarchy.lastIndexOf("\\") != -1 || serviceHierarchy.lastIndexOf("<") != -1 || serviceHierarchy.lastIndexOf(">") != -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.service.hierarchy"/>');
                    return;
                }
            } else if (document.jaxwsUpload.serviceHierarchy[0].value != null) {
                for (var i=0; i<document.jaxwsUpload.serviceHierarchy.length; i++){
                    var serviceHierarchy = document.jaxwsUpload.serviceHierarchy[i].value;
                    if (serviceHierarchy.lastIndexOf("~") != -1 || serviceHierarchy.lastIndexOf("!") != -1 || serviceHierarchy.lastIndexOf("@") != -1
                        || serviceHierarchy.lastIndexOf("#") != -1 || serviceHierarchy.lastIndexOf("$") != -1 || serviceHierarchy.lastIndexOf(";") != -1
                        || serviceHierarchy.lastIndexOf("%") != -1 || serviceHierarchy.lastIndexOf("^") != -1 || serviceHierarchy.lastIndexOf("*") != -1
                        || serviceHierarchy.lastIndexOf("(") != -1 || serviceHierarchy.lastIndexOf(")") != -1 || serviceHierarchy.lastIndexOf("+") != -1
                        || serviceHierarchy.lastIndexOf("=") != -1 || serviceHierarchy.lastIndexOf("{") != -1 || serviceHierarchy.lastIndexOf("}") != -1
                        || serviceHierarchy.lastIndexOf("[") != -1 || serviceHierarchy.lastIndexOf("]") != -1 || serviceHierarchy.lastIndexOf("|") != -1
                        || serviceHierarchy.lastIndexOf("\\") != -1 || serviceHierarchy.lastIndexOf("<") != -1 || serviceHierarchy.lastIndexOf(">") != -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.service.hierarchy"/>');
                    return;
                    }
                }
            }

            if (document.jaxwsUpload.jarfilename.value != null) {
                var fileName = document.jaxwsUpload.jarfilename.value;
                if (fileName == '') {
                    CARBON.showWarningDialog('<fmt:message key="select.annotated.jar"/>');
                } else if (fileName.lastIndexOf(".jar") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="select.annotated.jar"/>');
                } else {
                    document.jaxwsUpload.submit();
                }
            } else if (document.jaxwsUpload.jarfilename[0].value != null) {
                var validFilenames = true;
                for (var i=0; i<document.jaxwsUpload.jarfilename.length; i++) {
                    var fileName = document.jaxwsUpload.jarfilename[i].value;
                    if (fileName == '') {
                        CARBON.showWarningDialog('<fmt:message key="select.annotated.jar"/>');
                        validFilenames = false;
                        break;
                    } else if (fileName.lastIndexOf(".jar") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="select.annotated.jar"/>');
                        validFilenames = false;
                        break;
                    }
                }

                if(validFilenames) {
                    document.jaxwsUpload.submit();
                } else {
                    return;
                }
            }
        }

        var rows = 1;
        //add a new row to the table
        function addRow() {
            rows++;

            //add a row to the rows collection and get a reference to the newly added row
            var newRow = document.getElementById("jaxServiceTbl").insertRow(-1);
            newRow.id = 'file' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='file' name='jarfilename' size='50'/>";
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='text' name='serviceHierarchy' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
            oCell.className = "formRow";

            alternateTableRows('jaxServiceTbl', 'tableEvenRow', 'tableOddRow');
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('jaxServiceTbl', 'tableEvenRow', 'tableOddRow');
        }

    </script>


    <div id="middle">
        <h2><fmt:message key="jaxwsservices.headertext"/></h2>

        <div id="workArea">
            <form method="post" name="jaxwsUpload" action="../../fileupload/jaxws"
                  enctype="multipart/form-data" target="_self">

                <input type="hidden" name="errorRedirectionPage"
                                value="../carbon/jaxws/index.jsp?region=region1&item=jaxws_menu"/>
                <label style="font-weight:bold;">&nbsp;<fmt:message key="jaxwsservices.upload.jar.legend"/></label>
                <br/><br/>

                <table class="styledLeft" id="jaxServiceTbl">
                    <tr>
                        <td class="formRow">
                            <label style="font-weight:bold;"><fmt:message key="jaxwsservices.upload.jar.label"/><font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <label style="font-weight:bold;"><fmt:message key="service.hierarchy"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td class="formRow">
                            <input type="file" name="jarfilename" size="50"/>
                        </td>
                        <td class="formRow">
                            <input type="text" name="serviceHierarchy" size="50"/>&nbsp;
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>
                    
                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="jaxwsservices.upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button" onclick="javascript:location.href='../service-mgt/index.jsp'"
                                   value=" <fmt:message key="jaxwsservices.cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('jaxServiceTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
