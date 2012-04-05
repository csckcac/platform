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

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources">
<carbon:breadcrumb label="upload.jsservice"
		resourceBundle="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript">
        function validate() {
            if (document.jsUpload.jsFilename.value != null) {
                var jarinput = document.jsUpload.jsFilename.value;
                if (jarinput == '') {
                    CARBON.showErrorDialog('<fmt:message key="select.js.service"/>');
                } else if (jarinput.lastIndexOf(".zip") == -1) {
                    CARBON.showErrorDialog('<fmt:message key="select.js.file"/>');
                } else {
                    document.jsUpload.submit();
                }
            } else if (document.jsUpload.jsFilename[0].value != null) {
                var isValidFileName = true;
                for (var i=0; i<document.jsUpload.jsFilename.length; i++) {
                    var jarinput = document.jsUpload.jsFilename[i].value;
                    if (jarinput == '') {
                        CARBON.showErrorDialog('<fmt:message key="select.js.service"/>');
                        isValidFileName = false; break;
                    } else if (jarinput.lastIndexOf(".zip") == -1) {
                        CARBON.showErrorDialog('<fmt:message key="select.js.file"/>');
                        isValidFileName = false; break;
                    }
                }
                if (isValidFileName) {
                    document.jsUpload.submit();
                }
            }

        }

        var rows = 1;
        //add a new row to the table
        function addRow() {
            rows++;

            //add a row to the rows collection and get a reference to the newly added row
            var newRow = document.getElementById("jsServiceTbl").insertRow(-1);
            newRow.id = 'file' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = '<label><fmt:message key="path.to.jsservice.config"/> (.zip)<font color="red">*</font></label>';
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='file' name='jsFilename' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
            oCell.className = "formRow";

            alternateTableRows('jsServiceTbl', 'tableEvenRow', 'tableOddRow');
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('jsServiceTbl', 'tableEvenRow', 'tableOddRow');
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="upload.jsservice"/></h2>

        <div id="workArea">
            <form method="post" name="jsUpload" action="../../fileupload/zip"
                  enctype="multipart/form-data" target="_self">
                <input type="hidden" name="errorRedirectionPage"
                            value="../carbon/js_service/upload.jsp?region=region1&item=js_upload_menu"/>
                                   
                <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.jsservice"/> (.zip)</label>
                <br/><br/>       

                <table class="styledLeft" id="jsServiceTbl">
                    <tr>
                        <td>
                            <label><fmt:message key="path.to.jsservice.config"/> (.zip)<font color="red">*</font></label>
                        </td>
                        <td>
                            <input type="file" name="jsFilename" size="50"/>&nbsp;
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>

                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button" onclick="location.href = '../service-mgt/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>