package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

/**
 * Automate carbon patch related to https://wso2.org/jira/browse/CARBON-11017
 */
public class AddWSDLTest {

    private static final Log log = LogFactory.getLog(AddWSDLTest.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    String wsdl_path = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";
    String service_path = "/_system/governance/trunk/services/com/foo/BizService";

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);
    }


    @Test(alwaysRun = true, description = "Checking carbon patch released to add WSDL issue ", priority = 1)
    public void testAddWSDL() throws RegistryException {
        try {
            String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
            WsdlManager wsdlManager = new WsdlManager(governance);
            Wsdl wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdlManager.addWsdl(wsdl);
            deleteWSDL();
        } catch (GovernanceException e) {
            log.error("Failed to add WSDL:" + e.getMessage());
            throw new GovernanceException("Failed to add WSDL :" + e.getMessage());
        }
    }

    public void deleteWSDL() throws RegistryException {
        try {
            if (registry.resourceExists(wsdl_path)) {
                registry.delete(wsdl_path);
            }

            if (registry.resourceExists(service_path)) {
                registry.delete(service_path);
            }
        } catch (RegistryException e) {
            log.error("Failed to delete WSDL :" + e.getMessage());
            throw new RegistryException("Failed to delete WSDL :" + e.getMessage());
        }
    }

}
