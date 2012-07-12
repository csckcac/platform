package org.wso2.carbon.automation.utils.registry;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.rmi.RemoteException;


public class RegistryUserCreator {
    private static final Log log = LogFactory.getLog(RegistryUserCreator.class);
    private UserManagementClient userAdminStub;
    private String sessionCookie;


    protected static String login(String userName, String password, String hostName)
            throws RemoteException, LoginAuthenticationExceptionException {
        AuthenticatorClient loginClient = new AuthenticatorClient(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void deleteUsers(int userId, String userID) throws Exception {
        setInfoRolesAndUsers(userId);
        userAdminStub.deleteUser(userID);
    }

    public void addUser(int userId, String userID, String userPassword, String roleName)
            throws Exception {
        setInfoRolesAndUsers(userId);
        try {
            String roles[] = {roleName};
            userAdminStub.addUser(userID, userPassword, roles, null);
        } catch (UserAdminException e) {
            log.error("Add user fail" + e);
            throw new UserAdminException("Add user fail" + e);
        }
    }

    public void setInfoRolesAndUsers(int userId)
            throws LoginAuthenticationExceptionException, RemoteException {
        FrameworkProperties isProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME);
        UserInfo userAdminDetails = UserListCsvReader.getUserInfo(userId);
        sessionCookie = login(userAdminDetails.getUserName(), userAdminDetails.getPassword(),
                              isProperties.getProductVariables().getBackendUrl());
        userAdminStub = new UserManagementClient(isProperties.getProductVariables().getBackendUrl(), sessionCookie);


    }

}
