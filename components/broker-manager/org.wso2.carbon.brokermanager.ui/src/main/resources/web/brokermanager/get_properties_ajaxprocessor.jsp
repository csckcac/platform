<%@ page import="org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerProperty" %>
<%@ page import="org.wso2.carbon.brokermanager.ui.UIUtils" %>
<%
    // get broker properties
    BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);
    String brokerType = request.getParameter("brokerType");
    if (brokerType != null) {
        BrokerProperty[] properties = stub.getBrokerProperties(brokerType);
        if (properties != null) {
            // prepare property string separated by | and if it is not requried field, $ sign is added too.
            // eg. propertieString = userName|url|password|$description here description is not required field.
            // for secured fields, & mark is added.
            String propertiesString = "";
            for (BrokerProperty property : properties) {
                if (property.getRequired()) {
                    if (property.getSecured()) {
                        propertiesString = propertiesString + "|$&" + property.getKey() + "=" + property.getDisplayName();
                    } else {
                        propertiesString = propertiesString + "|$" + property.getKey() + "=" + property.getDisplayName();
                    }
                } else {
                    if (property.getSecured()) {
                        propertiesString = propertiesString + "|&" + property.getKey() + "=" + property.getDisplayName();
                    } else {
                        propertiesString = propertiesString + "|" + property.getKey() + "=" + property.getDisplayName();
                    }

                }
            }
%>
<%=propertiesString%>
<%
    }
%>
<%
    }

%>
