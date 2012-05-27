package org.wso2.automation.common.test.greg.wsapi;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;


public class TestCopy {
    private static final Log log = LogFactory.getLog(TestCopy.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Resource Copy", priority = 1)
    private void testResourceCopy() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        try {
            r1.setContent("c");

            registry.put("/test1/copy/c1/copy1", r1);
            Collection c1 = registry.newCollection();
            registry.put("/test1/move", c1);
            registry.copy("/test1/copy/c1/copy1", "/test1/copy/c2/copy2");
            Resource newR1 = registry.get("/test1/copy/c2/copy2");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/c1/copy1");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");

            String newContent = new String((byte[]) newR1.getContent());
            String oldContent = new String((byte[]) oldR1.getContent());
            assertEquals(newContent, oldContent, "Contents are not equal in copied resources");
            deleteResources("/test1");
            log.info("****************WS-API Resource Copy test - Passed**********************");
        } catch (RegistryException e) {
            log.error("WS-API Resource Copy test-Fail :" + e);
            throw new RegistryException("WS-API Resource Copy test-Fail:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Resource Copy", priority = 1)
    private void testCopyResourceToRoot() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        try {
            r1.setContent("c");

            registry.put("/test1/copy/c1/copy1", r1);
            Collection c1 = registry.newCollection();
            registry.put("/test1/move", c1);
            registry.copy("/test1/copy/c1/copy1", "/copy1");
            Resource newR1 = registry.get("/copy1");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/c1/copy1");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");

            String newContent = new String((byte[]) newR1.getContent());
            String oldContent = new String((byte[]) oldR1.getContent());
            assertEquals(newContent, oldContent, "Contents are not equal in copied resources");
            deleteResources("/test1");
            deleteResources("/copy1");
        } catch (RegistryException e) {
            log.error("WS-API Resource Copy test-Fail :" + e);
            throw new RegistryException("WS-API Resource Copy test-Fail:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Resource Copy", priority = 1)
    private void testCopyResourceFromRoot() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        try {
            r1.setContent("c2");

            registry.put("/copy3", r1);
            registry.copy("copy3","/test1/copy/c1/copy3");
            Resource newR1 = registry.get("/copy3");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/c1/copy3");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");

            String newContent = new String((byte[]) newR1.getContent());
            String oldContent = new String((byte[]) oldR1.getContent());
            assertEquals(newContent, oldContent, "Contents are not equal in copied resources");
            deleteResources("/test1");
            deleteResources("/copy3");
        } catch (RegistryException e) {
            log.error("WS-API Resource Copy test-Fail :" + e);
            throw new RegistryException("WS-API Resource Copy test-Fail:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Resource Copy", priority = 1)
    private void testCopyResourceFromRootToRoot() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        try {
            r1.setContent("c2");

            registry.put("/copy3", r1);
            registry.copy("copy3","copy4");
            Resource newR1 = registry.get("/copy3");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/copy4");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");

            String newContent = new String((byte[]) newR1.getContent());
            String oldContent = new String((byte[]) oldR1.getContent());
            assertEquals(newContent, oldContent, "Contents are not equal in copied resources");
            deleteResources("/copy4");
            deleteResources("/copy3");
        } catch (RegistryException e) {
            log.error("WS-API Resource Copy test-Fail :" + e);
            throw new RegistryException("WS-API Resource Copy test-Fail:" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Collection Copy", priority = 2)
    private void testCollectionCopy() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");

        try {
            r1.setContent("c");
            registry.put("/test1/copy/copy3/c3/resource1", r1);
            Collection c1 = registry.newCollection();
            registry.put("/test1/move", c1);
            registry.copy("/test1/copy/copy3", "/test1/newc/copy3");

            Resource newR1 = registry.get("/test1/newc/copy3/c3/resource1");
            assertEquals(newR1.getProperty("test"), "copy", "Copied resource should have a property named 'test' with value 'copy'.");

            Resource oldR1 = registry.get("/test1/copy/copy3/c3/resource1");
            assertEquals(oldR1.getProperty("test"), "copy", "Original resource should have a property named 'test' with value 'copy'.");
            deleteResources("/test1");
            log.info("*******************WS-API Collection Copy test - Passed ********************");
        } catch (RegistryException e) {
            log.error("WS-API Collection Copy test-Fail:" + e);
            throw new RegistryException("WS-API Collection Copy test-Fail:" + e);
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/test1");
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
