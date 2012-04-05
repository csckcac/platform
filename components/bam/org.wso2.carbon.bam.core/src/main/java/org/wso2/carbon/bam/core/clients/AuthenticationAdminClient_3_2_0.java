
package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.BAMConstants;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.services.stub.authenticationadmin_3_1_0.AuthenticationAdminStub;
import org.wso2.carbon.utils.NetworkUtils;

import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;
import static org.wso2.carbon.bam.core.clients.AbstractAdminClient.generateURL;

public class AuthenticationAdminClient_3_2_0 {

    private static Log log = LogFactory.getLog(AuthenticationAdminClient_3_2_0.class);
    private String sessionCookie;
    private AuthenticationAdminStub stub;

    public AuthenticationAdminClient_3_2_0(String serverURL) throws AxisFault {
        String serviceURL;
        serviceURL = generateURL(new String[]{serverURL, SERVICES_SUFFIX, BAMConstants.AUTH_ADMIN_SERVICE_3_2_0});
        stub = new AuthenticationAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public boolean authenticate(String username, String password) throws Exception {
        boolean loginResponse;

        loginResponse = stub.login(username, password, NetworkUtils.getLocalHostname());

        this.sessionCookie = (String) stub._getServiceClient().getLastOperationContext().getServiceContext()
                .getProperty(HTTPConstants.COOKIE_STRING);

        return loginResponse;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }
}
