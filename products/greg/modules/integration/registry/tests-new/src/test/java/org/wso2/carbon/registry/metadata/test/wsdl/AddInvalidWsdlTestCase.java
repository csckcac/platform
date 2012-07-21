package org.wso2.carbon.registry.metadata.test.wsdl;

import java.rmi.RemoteException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class AddInvalidWsdlTestCase {

    private Registry governanceRegistry;
    private Wsdl wsdl;


    @BeforeClass
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
    }

    /**
     * Add invalid wsdl file
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Add Invalid WSDL")
    public void testAddInvalidWSDL() throws GovernanceException {

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
        wsdl = wsdlManager.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                                   "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                                   "GREG/lifecycle/lcWithScript.xml");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added invalid wsdl using url");
        wsdlManager.addWsdl(wsdl);

    }
}
