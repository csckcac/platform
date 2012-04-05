package org.wso2.carbon.brokermanager.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UIUtils {
   public static BrokerManagerAdminServiceStub getBrokerManagerAdminService(ServletConfig config, HttpSession session,
                                                         HttpServletRequest request)
           throws AxisFault {
       ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                      .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
              //Server URL which is defined in the server.xml
              String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                      session) + "BrokerManagerAdminService.BrokerManagerAdminServiceHttpsSoap12Endpoint";
              BrokerManagerAdminServiceStub stub = new BrokerManagerAdminServiceStub(configContext, serverURL);

              String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

              ServiceClient client = stub._getServiceClient();
              Options option = client.getOptions();
              option.setManageSession(true);
              option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

//        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
//        backendServerURL = backendServerURL + "BrokerManagerAdminService";
//        BrokerManagerAdminServiceStub stub = new BrokerManagerAdminServiceStub(backendServerURL);
        return stub;
   }

}
