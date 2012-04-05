<%@ page import="org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerProperty" %>
<%@ page import="org.wso2.carbon.brokermanager.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.brokermanager.ui.i18n.Resources">

<carbon:breadcrumb
        label="brokermanager.add"
        resourceBundle="org.wso2.carbon.brokermanager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="eventing.js"></script>
<script type="text/javascript" src="js/subscriptions.js"></script>
<script type="text/javascript" src="js/eventing_utils.js"></script>

<script language="javascript">
    // this function validate required fields if they are required fields,
    // other fields are ignored.In addition to that, the values from each
    // field is taken and appended to a string.
    // string = propertyName + $ + propertyValue + | +propertyName...
    function addBroker(form) {
        var propertyCount = 0;
        var isFieldEmpty = false;
        var parameterString = "";
        // all properties, not required and required are checked
        while (document.getElementById("property_Required_" + propertyCount) != null ||
               document.getElementById("property_" + propertyCount) != null) {
            // if required fields are empty
            if (document.getElementById("property_Required_" + propertyCount) != null) {
                if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                    // values are empty in fields
                    isFieldEmpty = true;
                    parameterString = "";
                    break;
                }
                else {
                    // values are stored in parameter string to send to backend
                    var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                    var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                    parameterString = parameterString + propertyName + "$" + propertyValue + "|";

                }
            } else if (document.getElementById("property_" + propertyCount) != null) {
                var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
                var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
                if (notRequriedPropertyValue == "") {
                    notRequriedPropertyValue = "  ";
                }
                parameterString = parameterString + notRequiredPropertyName + "$" + notRequriedPropertyValue + "|";


            }
            propertyCount++;
        }
        var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
        // Check for white space
        if (!reWhiteSpace.test(document.getElementById("brokerNameId").value)) {
            CARBON.showErrorDialog("White spaces are not allowed in broker name.");
            return;
        }
        if (isFieldEmpty || (document.getElementById("brokerNameId").value.trim() == "")) {
            // empty fields are encountered.
            CARBON.showErrorDialog("Empty inputs fields are not allowed.");
            return;
        } else {
            // create parameter string
            var selectedIndex = document.inputForm.brokerTypeFilter.selectedIndex;
            var selected_text = document.inputForm.brokerTypeFilter.options[selectedIndex].text;
            var parameters = "?brokerName=" + (document.getElementById("brokerNameId").value.trim())
                    + "&brokerType=" + selected_text;
            if (parameterString != "") {
                parameters = parameters +"&propertySet=" + parameterString;
            }

            // ajax call for creating a broker at backend, needed parameters are appended.
            $.ajax({
                type: "POST",
                url: "add_broker_ajaxprocessor.jsp" + parameters,
                data: {},
                async:false,
                success: function(msg) {
                    if (msg.trim() == "true") {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add broker, Exception: " + msg);
                    }
                }
            });

        }

    }
    function clearTextIn(obj) {
        if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
            YAHOO.util.Dom.removeClass(obj, 'initE');
            YAHOO.util.Dom.addClass(obj, 'normalE');
            textValue = obj.value;
            obj.value = "";
        }
    }
    function fillTextIn(obj) {
        if (obj.value == "") {
            obj.value = textValue;
            if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
                YAHOO.util.Dom.removeClass(obj, 'normalE');
                YAHOO.util.Dom.addClass(obj, 'initE');
            }
        }
    }

    function getTooltip(name) {
        if (name == "password") {
            return "<fmt:message key="broker.password.tooltip"/>";
        } else if (name== "password") {
            return "<fmt:message key="broker.password.tooltip"/>";
        } else if (name =="ipAddress") {
            return "<fmt:message key="broker.ipAddress.tooltip"/>";
        } else if (name =="port") {
            return "<fmt:message key="broker.port.tooltip"/>";
        } else if (name =="authenticatorURL") {
            return "<fmt:message key="broker.authenticatorURL.tooltip"/>";
        } else if (name =="receiverURL") {
            return "<fmt:message key="broker.receiverURL.tooltip"/>";
        } else if (name =="virtualHostName") {
            return "<fmt:message key="broker.virtualHostName.tooltip"/>";
        } else if (name =="uri") {
            return "<fmt:message key="broker.uri.tooltip"/>";
        } else if (name =="username") {
            return "<fmt:message key="broker.username.tooltip"/>";
        } else if (name =="jndiName") {
            return "<fmt:message key="broker.jndiName.tooltip"/>";
        } else {
            return "";
        }

    }

    // broker properties are taken from back-end and render according to fields
    function showBrokerProperties() {
        var brokerInputTable = document.getElementById("brokerInputTable");
        var selectedIndex = document.inputForm.brokerTypeFilter.selectedIndex;
        var selected_text = document.inputForm.brokerTypeFilter.options[selectedIndex].text;

        // delete all rows except first two; broker name, broker type
        for (i = brokerInputTable.rows.length - 1; i > 1; i--) {
            brokerInputTable.deleteRow(i);
        }

        // ajax call for getting backend data
        $.ajax({
            type: "POST",
            url: "get_properties_ajaxprocessor.jsp?brokerType=" + selected_text + "",
            data: {},
            async:false,
            success: function(msg) {
                if (msg != null) {
                    msg = msg.trim();
                    // properties are taken as | separated property names
                    var properties = msg.split("|");
                    var propertyCount = properties.length;
                    var tableRawArray = new Array();

                    // for each property, add a text and input field in a row
                    for (i = 1; i < propertyCount; i++) {
                        if (properties[i].trim() != "") {
                            tableRawArray[i] = brokerInputTable.insertRow(brokerInputTable.rows.length);
                            var textLabel = tableRawArray[i].insertCell(0);

                            // $ -required field
                            var propertyRequiredCheck = properties[i].match("\\\$");
                            if (propertyRequiredCheck != "") {
                                properties[i] = properties[i].replace("$", "");
                            }

                            // & - secured field
                            var propertySecuredCheck = properties[i].match("\\&");
                            if (propertySecuredCheck != "") {
                                properties[i] = properties[i].replace("&", "");
                            }

                            var propertyNameAndDisplayName = properties[i].split("=");
                            textLabel.innerHTML = propertyNameAndDisplayName[1];
                            var requiredElementId = "property_";
                            if (propertyRequiredCheck == "$") {
                                textLabel.innerHTML = propertyNameAndDisplayName[1] + '<span class="required">*</span>';
                                requiredElementId = "property_Required_";
                            }
                            var textPasswordType = "text";
                            if (propertySecuredCheck == "&") {
                                textPasswordType = "password";
                            }
                            var inputField = tableRawArray[i].insertCell(1);
                            inputField.innerHTML = '<input style="width:50%" type="' + textPasswordType + '" id="' + requiredElementId + (i - 1) + '" name="' + propertyNameAndDisplayName[0] + '" title="' + getTooltip(propertyNameAndDisplayName[0]) + '" class="initE" />';

                        }
                    }
                }
            }
        });

    }
</script>

<div id="middle">
    <div id="workArea">
        <h3>Create a New Broker</h3>

        <form name="inputForm" action="index.jsp" method="get" id="addBroker">
            <table style="width:100%" id="brokerAdd" class="styledLeft">
                <thead>
                <tr>
                    <th>Enter Broker Details</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <table id="brokerInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>
                            <tr>
                                <td class="leftCol-med">Broker Name<span
                                        class="required">*</span>
                                </td>
                                <td><input type="text" name="brokerName" id="brokerNameId"
                                           class="initE"
                                           onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                                           value=""
                                           title="<fmt:message key="broker.name.tooltip"/>"
                                           style="width:50%"/></td>
                            </tr>
                            <tr>
                                <td>Broker Type<span class="required">*</span></td>
                                <td><select name="brokerTypeFilter"
                                            onchange="showBrokerProperties()"
                                            title="<fmt:message key="broker.type.tooltip"/>">
                                    <%
                                        BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);
                                        String[] brokerNames = stub.getBrokerNames();
                                        String firstBrokerName = null;
                                        if (brokerNames != null) {
                                            firstBrokerName = brokerNames[0];
                                            for (String type : brokerNames) {
                                    %>
                                    <option><%=type%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>

                                </td>

                            </tr>
                            <%
                                if (firstBrokerName != null) {
                                    BrokerProperty[] properties = stub.getBrokerProperties(firstBrokerName);
                                    if (properties != null) {
                                        for (int index = 0; index < properties.length; index++) {
                            %>
                            <tr>
                                <td class="leftCol-med"><%=properties[index].getDisplayName()%>
                                    <%
                                        String propertyId = "property_";
                                        if (properties[index].getRequired()) {
                                            propertyId = "property_Required_";

                                    %>
                                    <span class="required">*</span>
                                    <%
                                        }
                                    %>
                                </td>
                                <%
                                    String type = "text";
                                    if (properties[index].getSecured()) {
                                        type = "password";
                                    }
                                %>
                                <td><input type="<%=type%>" name="<%=properties[index].getKey()%>"
                                           id="<%=propertyId%><%=index%>" class="initE"
                                           style="width:50%"
                                           title="<fmt:message key="broker.<%=properties[index].getKey()%>.tooltip"/>"
                                           value=""/>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>

                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="Add Broker"
                               onclick="addBroker(document.getElementById('addBroker'))"/>
                    </td>
                </tr>
                </tbody>
            </table>


        </form>
    </div>
</div>
</fmt:bundle>
