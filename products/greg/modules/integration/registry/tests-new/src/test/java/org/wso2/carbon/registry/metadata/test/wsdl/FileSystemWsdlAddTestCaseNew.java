package org.wso2.carbon.registry.metadata.test.wsdl;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.governance.utils.FileReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class FileSystemWsdlAddTestCaseNew {
    private Registry governanceRegistry;
    private ResourceAdminServiceClient resourceAdminServiceClient;


    @BeforeClass
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                                                                    ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
    }


    @Test(groups = "wso2.greg", description = "Add WSDL from file system")
    public void testAddWSDLFromFileSystem() throws IOException,
                                                   ResourceAdminServiceExceptionException,
                                                   RegistryException {
        String wsdlPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                          File.separator + "wsdl" + File.separator + "info.wsdl";

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
        Wsdl wsdl = wsdlManager.newWsdl(FileReader.readFile(wsdlPath).getBytes());
        wsdlManager.addWsdl(wsdl);
        assertTrue(wsdl.getQName().toString().contains("http://footballpool.dataaccess.eu"));
    }
}
