package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import static org.testng.Assert.*;


public class OnDemandContentTest {
    private static final Log log = LogFactory.getLog(OnDemandContentTest.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Delete on Demand Content")
    private void testOnDemandContent() throws Exception {
        try {
            String testPath = "/ondemand/test";
            Resource r1 = registry.newResource();
            r1.setContent("This is test content. It should not be loaded unless getContent() is called.".getBytes());
            registry.put(testPath, r1);

            OnDemandContentResourceImpl r1_get = (OnDemandContentResourceImpl) registry.get(testPath);
            r1_get.setClient(null);
            Object content;
            try {
                content = r1_get.getContent();
                assertNull(content, "Resource content should not exist");
                fail("Content has not been pre-fetched, not on demand");
            } catch (Exception ignored) {

            }

            Resource r1_get2 = registry.get(testPath);
            content = r1_get2.getContent();
            assertNotNull(content, "Resource content should be fetched on demand");
            deleteResources("/ondemand");
            log.info("***********WS-API on Demand Content - Passed************");
        } catch (Exception e) {
            log.error("Delete on Demand Content Fail:" + e);
            throw new Exception("Delete on Demand Content Fail:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/ondemand");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }

    }


}
