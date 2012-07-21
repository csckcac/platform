package org.wso2.carbon.registry.metadata.test.wsdl;

import static org.testng.Assert.assertTrue;

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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class AddSameWsdlAgainTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private Registry governance;
    private Wsdl wsdl, wsdlCopy;
    private WsdlManager wsdlManager;


    @BeforeClass
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(1); // user
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(1);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient registry = provider.getWSRegistry(userId,
                                                                  ProductConstant.GREG_SERVER_NAME);
        governance = provider.getGovernanceRegistry(registry, userId);
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                                    userInfo.getUserName(), userInfo.getPassword());

    }

    /**
     * WSDL addition from URL
     *
     * @throws GovernanceException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL")
    public void testAddWSDL() throws RemoteException,
                                     ResourceAdminServiceExceptionException, GovernanceException {

        wsdlManager = new WsdlManager(governance);
        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration" +
                         "/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts" +
                         "/GREG/wsdl/Automated.wsdl");
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "kana");
        wsdl.addAttribute("description", "added wsdl via URL");
        wsdlManager.addWsdl(wsdl);

    }

    /**
     * WSDL addition from URL: verification
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL", dependsOnMethods = "testAddWSDL")
    public void testWsdlVerification() throws RemoteException,
                                              ResourceAdminServiceExceptionException,
                                              GovernanceException {
        assertTrue(wsdl.getAttribute("description").contentEquals("added wsdl via URL"));
    }


    /**
     * adding the same wsdl
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add same WSDL via URL", dependsOnMethods = "testWsdlVerification")
    public void testAddAlreadyAddedWSDL() throws RemoteException,
                                                 ResourceAdminServiceExceptionException,
                                                 GovernanceException {

        wsdlManager = new WsdlManager(governance);
        wsdlCopy = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                         "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                         "GREG/wsdl/Automated.wsdl");
        wsdlCopy.addAttribute("description", "added wsdl via URL");
        wsdlManager.addWsdl(wsdlCopy);

    }


    /**
     * Second WSDL addition from URL: verification
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL", dependsOnMethods = "testAddAlreadyAddedWSDL")
    public void testSecondWsdlVerification() throws RemoteException,
                                                    ResourceAdminServiceExceptionException,
                                                    GovernanceException {
        assertTrue(wsdlCopy.getAttribute("description").contentEquals("added wsdl via URL"));
    }

    /**
     * compare both WSDLs additions from URL: verification
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL", dependsOnMethods = "testAddAlreadyAddedWSDL")
    public void testCompareWSDLs() throws RemoteException,
                                          ResourceAdminServiceExceptionException,
                                          GovernanceException {

        assertTrue(wsdlCopy.getId().matches(wsdl.getId()));

    }


    /**
     * This method is not enabled
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Verify Added WSDL via URL", dependsOnMethods = "testAddWSDL")
    public void testVerifyWSDL() throws RemoteException,
                                        ResourceAdminServiceExceptionException {

        assertTrue(resourceAdminServiceClient
                           .getMetadata(
                                   "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts" +
                                   "/_2008/_01/MyFirstTestWSDL.wsdl")
                           .getDescription().contentEquals("for the wsdl addition test"));

    }

}
