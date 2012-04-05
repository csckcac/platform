<%@ page import="org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerConfigurationDetails" %>
<%@ page import="org.wso2.carbon.brokermanager.stub.types.BrokerProperty" %>
<%@ page import="org.wso2.carbon.brokermanager.ui.UIUtils" %>
<%
    // get requrired parameters to add a broker to back end.
    BrokerManagerAdminServiceStub stub = UIUtils.getBrokerManagerAdminService(config, session, request);
    String brokerName = request.getParameter("brokerName");
    String msg = null;

    BrokerConfigurationDetails[] brokerConfigurationDetailsArray = null;
     if(stub != null){
       try{
           brokerConfigurationDetailsArray =  stub.getAllBrokerConfigurationNamesAndTypes();
       }catch ( Exception e){
            %>
            <script type="text/javascript">
                location.href = 'index.jsp';</script>
            <%
           return;
       }
     }

    if (brokerConfigurationDetailsArray != null) {
        for (BrokerConfigurationDetails brokerConfigurationDetails : brokerConfigurationDetailsArray) {
            if (brokerConfigurationDetails.getBrokerName().equals(brokerName)) {
                msg = brokerName + " already exists.";
                break;
            }
        }
    }
    if (msg == null) {
        String brokerType = request.getParameter("brokerType");
        // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
        String propertySet = request.getParameter("propertySet");
        BrokerProperty[] brokerProperties = null;

        if (propertySet != null) {
            String[] properties = propertySet.split("\\|");
            if (properties != null) {
                // construct broker property array for each broker property
                brokerProperties = new BrokerProperty[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$");
                    if (propertyNameAndValue != null) {
                        brokerProperties[index] = new BrokerProperty();
                        brokerProperties[index].setKey(propertyNameAndValue[0].trim());
                        brokerProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }
        try {
            // add broker via admin service
            stub.addBrokerConfiguration(brokerName, brokerType, brokerProperties);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }
%>  <%=msg%>   <%

%>
