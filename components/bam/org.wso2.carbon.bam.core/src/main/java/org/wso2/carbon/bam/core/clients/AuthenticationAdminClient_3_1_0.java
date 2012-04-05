package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.bam.core.BAMConstants;
import org.wso2.carbon.bam.services.stub.authenticationadmin_3_1_0.AuthenticationAdminStub;
import org.wso2.carbon.bam.services.stub.authenticationadminservice203.AuthenticationExceptionException;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.rmi.RemoteException;

import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;

/**
 * This class used to authenticate with the carbon 3.1.0 AuthenticationAdmin
 */
public class AuthenticationAdminClient_3_1_0 extends AbstractAdminClient<AuthenticationAdminStub> {
    private String sessionCookie;

    public AuthenticationAdminClient_3_1_0(String serverURL) throws AxisFault {

        String serviceURL = generateURL(new String[]{serverURL, SERVICES_SUFFIX, BAMConstants.AUTH_ADMIN_SERVICE_3_1_0});
        stub = new AuthenticationAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public boolean authenticate(String userName, String password) throws RemoteException, SocketException,
            AuthenticationExceptionException {
        boolean logInResponse = false;

        try {
            logInResponse = stub.login(userName, password, NetworkUtils.getLocalHostname());
            this.sessionCookie = (String) stub._getServiceClient().getLastOperationContext().getServiceContext()
                    .getProperty(HTTPConstants.COOKIE_STRING);

        } catch (org.wso2.carbon.bam.services.stub.authenticationadmin_3_1_0.AuthenticationExceptionException e) {
            e.printStackTrace();
        }
        return logInResponse;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }
}
