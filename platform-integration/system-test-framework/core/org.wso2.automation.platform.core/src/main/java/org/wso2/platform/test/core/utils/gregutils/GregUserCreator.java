package org.wso2.platform.test.core.utils.gregutils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.rmi.RemoteException;


public class GregUserCreator {
    private static final Log log = LogFactory.getLog(GregUserCreator.class);
    private static WSRegistryServiceClient registry = null;
    private AdminServiceUserMgtService userAdminStub;
    private UserInfo userAdminDetails;
    private String sessionCookie;


    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void deleteUsers(int userId, String userID) {
        setInfoRolesAndUsers(userId);
        userAdminStub.deleteUser(sessionCookie, userID);
    }

    public void addUser(int userId, String userID, String userPassword, String roleName) throws UserAdminException {
        setInfoRolesAndUsers(userId);
        try {
            String roles[] = {roleName};
            userAdminStub.addUser(sessionCookie, userID, userPassword, roles, null);
        } catch (UserAdminException e) {
            log.error("Add user fail" + e.getMessage());
            throw new UserAdminException("Add user fail" + e.getMessage());
        }
    }

    public void setInfoRolesAndUsers(int userId) {
        FrameworkProperties isProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME);
        userAdminStub = new AdminServiceUserMgtService(isProperties.getProductVariables().getBackendUrl());
        userAdminDetails = UserListCsvReader.getUserInfo(userId);
        sessionCookie = login(userAdminDetails.getUserName(), userAdminDetails.getPassword(),
                isProperties.getProductVariables().getBackendUrl());
    }

    public WSRegistryServiceClient getRegistry(int userId, String userID, String userPassword) throws RegistryException, RemoteException, LoginAuthenticationExceptionException {
        setInfoRolesAndUsers(userId);
        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String userName = userID + "@" + userDetails.getDomain();
        FrameworkProperties gregProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        String serverURL = "https://" + gregProperties.getProductVariables().getHostName() + "/t/" + userDetails.getDomain() + File.separator + "services" + File.separator;
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String axis2Repo = ProductConstant.getModuleClientPath();
        String axis2Conf = resourcePath + File.separator + "axis2config" + File.separator + "axis2_client.xml";

        ConfigurationContext configContext = null;
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);

            String serviceEPR = serverURL + "AuthenticationAdmin";
            AuthenticationAdminStub stub = new AuthenticationAdminStub(configContext, serviceEPR);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            String cookie = null;

            boolean result = stub.login(userName, userPassword, serviceEPR);
            if (result) {
                cookie = ((String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING));
            }

            registry = new WSRegistryServiceClient(serverURL, cookie);

        } catch (AxisFault axisFault) {
            log.error("Axis2 fault thrown :" + axisFault.getMessage());
            throw new AxisFault("Axis2 fault thrown :" + axisFault.getMessage());
        } catch (RegistryException e) {
            log.error("Registry Exception thrown:" + e.getMessage());
            throw new RegistryException("Registry Exception thrown:" + e.getMessage());
        }
        log.info("GReg Registry -Login Success");
        return registry;
    }


}
