package org.wso2.automation.common.test.greg.wsapi;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import org.testng.annotations.*;


public class ContinuousOperations {
    private static final Log log = LogFactory.getLog(ContinuousOperations.class);
    private static WSRegistryServiceClient registry = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Continuous Delete")
    private void testcontinousDelete() throws RegistryException, InterruptedException {
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            Resource res1 = registry.newResource();
            byte[] r1content = "R2 content".getBytes();

            try {
                res1.setContent(r1content);
                String path = "/con-delete/test/" + i + 1;
                registry.put(path, res1);
                Resource resource1 = registry.get(path);

                assertEquals(new String((byte[]) resource1.getContent()),
                        new String((byte[]) res1.getContent()), "File content is not matching");

                registry.delete(path);

                boolean value = false;

                if (registry.resourceExists(path)) {
                    value = true;
                }
                assertFalse(value, "Resoruce not found at the path");
                res1.discard();
                resource1.discard();
                Thread.sleep(100);
                deleteResources("/con-delete");
            } catch (RegistryException e) {
                log.error("WS-API Continuous Delete Operations Fail:" + e);
                throw new RegistryException("WS-API Continuous Delete Operations Fail:" + e);
            } catch (InterruptedException e) {
                log.error("WS-API Continuous Delete Operations Fail:" + e);
                throw new InterruptedException("WS-API Continuous Delete Operations Fail:" + e);
            }
        }
        log.info("***********WS-API Continous Delete - Passed**********");
    }

     @Test(groups = {"wso2.greg"}, description = "Continuous Update")
    private void testContinuousUpdate() throws RegistryException, InterruptedException {
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            Resource res1 = registry.newResource();
            byte[] r1content = "R2 content".getBytes();

            try {
                res1.setContent(r1content);
                String path = "/con-delete/test-update/" + i + 1;
                registry.put(path, res1);

                Resource resource1 = registry.get(path);

                assertEquals(new String((byte[]) resource1.getContent()),
                        new String((byte[]) res1.getContent()),"File content is not matching");

                Resource resource = new ResourceImpl();
                byte[] r1content1 = "R2 content updated".getBytes();
                resource.setContent(r1content1);
                resource.setProperty("abc", "abc");

                registry.put(path, resource);
                Resource resource2 = registry.get(path);
                assertEquals(new String((byte[]) resource.getContent()),
                        new String((byte[]) resource2.getContent()),"File content is not matching");

                resource.discard();
                res1.discard();
                resource1.discard();
                resource2.discard();
                Thread.sleep(100);
                deleteResources("/con-delete");
            } catch (RegistryException e) {
                log.error("WS-API Continuous Update Operations Fail:" + e);
                throw new RegistryException("WS-API Continuous Update Operations Fail:" + e);
            } catch (InterruptedException e) {
                log.error("WS-API Continuous Update Operations Fail:" + e);
                throw new InterruptedException("WS-API Continuous Update Operations Fail:" + e);
            }
        }
        log.info("**********WS-API Continuous Update - Passed ***********");
    }



    private void removeResource() throws RegistryException {
        deleteResources("/con-delete");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("ContinuousOperations deleteResources RegistryException thrown:" + e);
            throw new RegistryException("ContinuousOperations deleteResources RegistryException thrown:" + e);
        }

    }
}




