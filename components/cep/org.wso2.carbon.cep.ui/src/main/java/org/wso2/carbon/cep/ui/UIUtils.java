package org.wso2.carbon.cep.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UIUtils {
    public static CEPAdminServiceStub getCECepAdminServiceStub(ServletConfig config, HttpSession session,
                                                               HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "CEPAdminService.CEPAdminServiceHttpsSoap12Endpoint";
        CEPAdminServiceStub stub = new CEPAdminServiceStub(configContext, serverURL);
        return stub;
    }
}

