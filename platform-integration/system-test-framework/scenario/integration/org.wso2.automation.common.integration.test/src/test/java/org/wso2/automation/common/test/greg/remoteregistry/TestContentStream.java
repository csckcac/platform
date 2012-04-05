/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.automation.common.test.greg.remoteregistry;

import java.io.*;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;


public class TestContentStream {
    private static final Log log = LogFactory.getLog(TestTagging.class);
    public RemoteRegistry registry;
    String resourcePath;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        removeResource();
    }


    @Test(groups = {"wso2.greg"}, description = "test resource stream xml ", priority = 1)
    public void testPutResourceasStreamXML() throws FileNotFoundException, RegistryException {
        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";
        final String registryPath = "/wso2registry/conf/pom.xml";

        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                    "pom.xml"));
            String st = null;
            try {
                st = slurp(is);
            }
            catch (IOException e) {
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
            log.info("***************Registry API Put Resource as StreamXML test - Passed**************");
        } catch (FileNotFoundException e) {
            log.error("Registry API Put Resource as StreamXML test - Failed :" + e.getMessage());
            throw new FileNotFoundException("Registry API Put Resource as StreamXML test - Failed:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Registry API Put Resource as StreamXML test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Put Resource as StreamXML test - Failed:" + e.getMessage());
        }
    }

    private String getTestResourcePath() {
        String repo_path = resourcePath + File.separator + "artifacts" + File.separator + "GREG" + File.separator;
        return repo_path;
    }

    @Test(groups = {"wso2.greg"}, description = "test content stream xml ", priority = 2)
    public void testContentStreaming() throws RegistryException {
        Resource r3;
        try {
            r3 = registry.newResource();
            String path = "/content/stream/content.txt";
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
            log.info("***************Registry API Content Streaming test- Passed *****************");
        } catch (RegistryException e) {
            log.error("Registry API Content Streaming test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Content Streaming test - Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test setcontent stream xml ", priority = 3)
    public void setContainStreamXML() throws FileNotFoundException, RegistryException {
        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";
        final String registryPath = "/wso2registry/contentstream/conf/pom.xml";

        InputStream is;
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
            log.info("***************Registry API Set Contain StreamXML test - Passed ****************");
        } catch (FileNotFoundException e) {
            log.error("Registry API Set Contain StreamXML test - Failed :" + e.getMessage());
            throw new FileNotFoundException("Registry API Set Contain StreamXML test - Failed:" + e.getMessage());
        } catch (RegistryException e) {
            log.error("Registry API Set Contain StreamXML test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Set Contain StreamXML test - Failed:" + e.getMessage());
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

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
