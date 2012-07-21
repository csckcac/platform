package org.wso2.carbon.registry.metadata.test.wsdl;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class LargeWSDLAddTestCase {
    private Registry governanceRegistry;

    @BeforeClass
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                                                                    ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
    }

    /**
     * adding a large wsdl
     *
     * @throws MalformedURLException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add Large WSDL")
    public void testAddLargeWSDL() throws RemoteException,
                                          ResourceAdminServiceExceptionException,
                                          GovernanceException,
                                          MalformedURLException {

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
        Wsdl wsdl = wsdlManager.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                        "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo" +
                                        "/src/main/resources/artifacts/GREG/wsdl/info.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added large wsdl using url");
        wsdlManager.addWsdl(wsdl);
        assertTrue(wsdl.getAttribute("description").contentEquals("added large wsdl using url"));
    }
}
