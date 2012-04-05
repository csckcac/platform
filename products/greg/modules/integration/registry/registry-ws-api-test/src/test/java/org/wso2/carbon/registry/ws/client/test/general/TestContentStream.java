/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.ws.client.test.general;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;

public class TestContentStream extends TestSetup {

    @Override
    public void runSuccessCase() {
        super.runSuccessCase();

        try {
            putResourceasStreamXML();
            contentStreaming();
            setContainStreamXML();
        } catch (Exception e) {
            e.printStackTrace();
            fail("The Content Stream Test for WS-API failed");
        }
    }

    private void putResourceasStreamXML() throws RegistryException, FileNotFoundException, Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
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

        assertEquals("File content is not matching", new String((byte[]) resource.getContent()),
                new String((byte[]) r2.getContent()));

    }

    private String getTestResourcePath() {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator +
                ".." + File.separator /*+ "src" + File.separator + "test" + File.separator + "java" +
                File.separator + "resources" + File.separator*/;
    }

    private void contentStreaming() throws Exception {


        Resource r3 = registry.newResource();
        String path = "/content/stream/content.txt";
        r3.setContent(new String("this is the content").getBytes());
        r3.setDescription("this is test desc");
        r3.setMediaType("plain/text");
        r3.setProperty("test2", "value2");
        r3.setProperty("test1", "value1");
        registry.put(path, r3);

        Resource r4 = registry.get("/content/stream/content.txt");

        assertEquals("Content is not equal.", new String((byte[]) r3.getContent()),
                new String((byte[]) r4.getContent()));

        InputStream isTest = r4.getContentStream();

        assertEquals("Content stream is not equal.", new String((byte[]) r3.getContent()),
                convertStreamToString(isTest));

        r3.discard();
    }

    private void setContainStreamXML() throws Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/contentstream/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                "pom.xml"));

        Resource resource = registry.newResource();

        resource.setContentStream(is);
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals("File content is not matching", new String((byte[]) resource.getContent()),
                new String((byte[]) r2.getContent()));

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
