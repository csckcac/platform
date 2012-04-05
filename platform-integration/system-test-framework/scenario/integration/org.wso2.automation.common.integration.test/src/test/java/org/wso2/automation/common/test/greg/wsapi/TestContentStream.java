package org.wso2.automation.common.test.greg.wsapi;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import java.io.*;

public class TestContentStream {
    private static final Log log = LogFactory.getLog(TestContentStream.class);
    private static WSRegistryServiceClient registry = null;
    String resourcePath;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Put Resource as StreamXML test", priority = 1)
    private void testPutResourceasStreamXML() throws FileNotFoundException, RegistryException {
        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";
        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/conf/pom.xml";

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                    "pom.xml"));
            String st = null;
            try {
                st = slurp(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Resource resource = registry.newResource();
            resource.setContent(st.getBytes());
            resource.setDescription(description);
            resource.setMediaType(mediaType);
            registry.put(registryPath, resource);

            Resource r2 = registry.get(registryPath);
            assertEquals(new String((byte[]) resource.getContent()),
                    new String((byte[]) r2.getContent()), "File content is not matching");

            deleteResources("/wso2registry");
            log.info("**************WS-API Put Resource as StreamXML test - Passed**************");
        } catch (FileNotFoundException e) {
            log.error("WS-API Put Resource as StreamXML test - Fail :" + e.getMessage());
            throw new FileNotFoundException("WS-API Put Resource as StreamXML test- Fail:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("WS-API Put Resource as StreamXML test - Fail :" + e.getMessage());
            throw new RegistryException("WS-API Put Resource as StreamXML test - Fail:" + e.getMessage());
        }
    }

    private String getTestResourcePath() {
        String repo_path = resourcePath + File.separator + "artifacts" + File.separator + "GREG" + File.separator;
        return repo_path;
    }

    @Test(groups = {"wso2.greg"}, description = "Content StreamXML test", priority = 2)
    private void testContentStreaming() throws RegistryException {
        Resource r3 = registry.newResource();
        String path = "/content/stream/content.txt";

        try {
            r3.setContent(new String("this is the content").getBytes());
            r3.setDescription("this is test desc");
            r3.setMediaType("plain/text");
            r3.setProperty("test2", "value2");
            r3.setProperty("test1", "value1");
            registry.put(path, r3);

            Resource r4 = registry.get("/content/stream/content.txt");
            assertEquals(new String((byte[]) r3.getContent()),
                    new String((byte[]) r4.getContent()), "Content is not equal.");

            InputStream isTest = r4.getContentStream();
            assertEquals(new String((byte[]) r3.getContent()),
                    convertStreamToString(isTest), "Content stream is not equal.");
            r3.discard();
            deleteResources("/content");
            log.info("*************WS-API Content Streaming test - Passed******************");
        } catch (RegistryException e) {
            log.error("contentStreaming RegistryException thrown :" + e.getMessage());
            throw new RegistryException("WS-API Put Resource as StreamXML test - Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Content StreamXML test", priority = 3)
    private void testSetContainStreamXML() throws FileNotFoundException, RegistryException {
        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";
        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/contentstream/conf/pom.xml";
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                    "pom.xml"));
            Resource resource = registry.newResource();
            resource.setContentStream(is);
            resource.setDescription(description);
            resource.setMediaType(mediaType);
            registry.put(registryPath, resource);

            Resource r2 = registry.get(registryPath);
            assertEquals(new String((byte[]) resource.getContent()),
                    new String((byte[]) r2.getContent()), "File content is not matching");
            deleteResources("/wso2registry");
            log.info("**************WS-API set Contain StreamXML test - Passed ***************");
        } catch (FileNotFoundException e) {
            log.error("WS-API set Contain StreamXML test - FAil :" + e.getMessage());
            throw new FileNotFoundException("WS-API set Contain StreamXML test- Fail:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("WS-API set Contain StreamXML test-Fail :" + e.getMessage());
            throw new RegistryException("WS-API set Contain StreamXML test- Fail:" + e.getMessage());
        }
    }


    private void removeResource() throws RegistryException {
        deleteResources("/wso2registry");
        deleteResources("/content");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e.getMessage());
            throw new RegistryException("deleteResources RegistryException thrown:" + e.getMessage());
        }
    }


    //    The below methods are used in the above tests. No need to add them to the test suit.
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
