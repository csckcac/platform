<%@ page import="org.wso2.carbon.hosting.mgt.ui.HostingAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<jsp:include page="../dialog/display_messages.jsp"/>
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
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    HostingAdminClient client;
    try {
             client = new HostingAdminClient(request.getLocale(),cookie, configContext, serverURL);
    }catch (Exception e) {
             response.setStatus(500);
             CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
        return;
    }

        boolean instancesUpForTenant = true;
%>

<fmt:bundle basename="org.wso2.carbon.hosting.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.webapp"
                       resourceBundle="org.wso2.carbon.hosting.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript">
        function validate() {
            var cartridgeSelect = document.getElementById("cartridges");
            if(cartridgeSelect .options[cartridgeSelect .selectedIndex].text == '<fmt:message key="select.cartridge"/>'){
                CARBON.showWarningDialog('<fmt:message key="no.selected.cartridge"/>');
                return;
            }

            if (document.webappUploadForm.warFileName.value != null) {
                var jarinput = document.webappUploadForm.warFileName.value;
                if (jarinput == '') {
                    CARBON.showWarningDialog('<fmt:message key="select.webapp.file"/>');
                } else if (jarinput.lastIndexOf(".zip") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                } else {
                    document.webappUploadForm.submit();
                }
            } else if (document.webappUploadForm.warFileName[0].value != null) {
                var validFileNames = true;

                for (var i=0; i<document.webappUploadForm.warFileName.length; i++) {
                    var jarinput = document.webappUploadForm.warFileName[i].value;
                    if (jarinput == '') {
                        CARBON.showWarningDialog('<fmt:message key="select.webapp.file"/>');
                        validFileNames = false; break;
                    } else if (jarinput.lastIndexOf(".zip") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                        validFileNames = false; break;
                    }
                }
                if(validFileNames) {
                    document.webappUploadForm.submit();
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
            var newRow = document.getElementById("webappTbl").insertRow(-1);
            newRow.id = 'file' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = '<label><fmt:message key="app.archive"/> (.zip)<font color="red">*</font></label>';
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='file' name='warFileName' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
            oCell.className = "formRow";

            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="upload.application"/></h2>

        <div id="workArea">
            <form method="post" name="webappUploadForm" action="../../fileupload/app"
                  enctype="multipart/form-data" target="_self">
                <input type="hidden" name="errorRedirectionPage"
                            value="../carbon/hosting-mgt/upload.jsp?region=region1&item=webapps_add_menu"/>
                <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.new.app"/> (.zip)</label>
                <br/><br/>

                <table class="styledLeft" id="webappTbl">
                    <tr>
                        <td class="formRow">
                            <label><fmt:message key="app.archive"/> (.zip)<font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <input type="file" name="warFileName" size="50"/>&nbsp;
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>
                 <%
                    String cartridges[] = client.getCartridges();
                 %>
                <table class="styledLeft">
                    <tr>
                        <td class="cartridgeRow">
                            <nobr>
                                <fmt:message key="cartridge"/>
                                <label><font color="red">*</font></label>
                            </nobr>
                        </td>
                        <td class="cartridgeRow">
                            <nobr>
                                <select name="cartridges" id="cartridges">
                                    <option value="selectACartridge" selected="selected">
                                       <fmt:message key="select.cartridge"/>
                                    </option>
                                        <%
                                        for (String cartridge : cartridges) {
                                         %>
                                            <option value="<%= cartridge%>"> <%= cartridge%>  </option>
                                        <%
                                        }
                                         %>
                                </select>
                            </nobr>
                        </td>
                    </tr>
                </table>
                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button"
                                   onclick="location.href='../hosting-mgt/index.jsp'"
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
