/*
 * Copyright 2011-2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.appserver.integration.tests;

import org.apache.catalina.util.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;


public class WebappSampleTestCase {
    private static final String USER_NAME = "user1";
    private static final String PASS_WORD = "pass123";
    private static final String RESOURCE_VLAUE = "kicha";
    private static final String RESOURCE_PATH = "path/to/kicha";
    private static final String CACHE_KEY = "cacheKey1";
    private static final String CACHE_VALUE = "cacheValue1";

    private static final String EXAMPLE_WEBAPP_URL = "http://localhost:9763/example/carbon";
    private static String CLIENT_AUTH_HEADER = "authorization";
    private HttpClient httpClient = new HttpClient();

    private static final Log log = LogFactory.getLog(WebappSampleTestCase.class);


    @Test(groups = {"wso2.as"})
    public void testUserManagerAndAuthenticationDemo() throws IOException {
        log.info("Running carbon example webapp test case");
        ClientConnectionUtil.waitForPort(9763);

        String url1 = EXAMPLE_WEBAPP_URL + "/usermgt/index.jsp?username=" +
                      USER_NAME + "&password=" + PASS_WORD;
        GetMethod getMethod1 = new GetMethod(url1);
        try {
            log.info("Adding test user to User Realm");
            int statusCode = httpClient.executeMethod(getMethod1);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod1.getStatusLine());
            }

        } finally {
            getMethod1.releaseConnection();
        }

        String url2 = EXAMPLE_WEBAPP_URL + "/authentication/login.jsp?username=" +
                      USER_NAME + "&password=" + PASS_WORD;
        GetMethod getMethod2 = new GetMethod(url2);

        try {
            log.info("Authenticating test user with carbon user realm");
            int statusCode = httpClient.executeMethod(getMethod2);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod2.getStatusLine());
            } else {
                boolean success = Boolean.
                        parseBoolean(getMethod2.getResponseHeader("logged-in").getValue());
                if (success) {
                    String username = getMethod2.getResponseHeader("username").getValue();
                    assertEquals(username, USER_NAME);
                } else {
                    fail("Authentication failed for test user");
                }
            }

        }  finally {
            getMethod2.releaseConnection();
        }
    }

    @Test(groups = {"wso2.as"}, dependsOnMethods = "testUserManagerAndAuthenticationDemo")
    public void testRegistryUsageDemo() throws IOException {
        log.info("Running registry usage demo test case");
        ClientConnectionUtil.waitForPort(9763);
        String url1 = EXAMPLE_WEBAPP_URL + "/registry/index.jsp?add=Add&resourcePath=" +
                      RESOURCE_PATH + "&value=" + RESOURCE_VLAUE;
        GetMethod getMethod1 = new GetMethod(url1);

        try {
            log.info("Adding test resource to registry");
            int statusCode = httpClient.executeMethod(getMethod1);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod1.getStatusLine());
            }
        }  finally {
            getMethod1.releaseConnection();
        }

        String url2 = EXAMPLE_WEBAPP_URL + "/registry/index.jsp?view=View&resourcePath=" +
                      RESOURCE_PATH;
        GetMethod getMethod2 = new GetMethod(url2);
        try {
            log.info("Getting test resource content from registry");
            int statusCode = httpClient.executeMethod(getMethod2);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod2.getStatusLine());
            } else {
                String resourceContent = getMethod2.
                        getResponseHeader("resource-content").getValue();
                assertEquals(resourceContent, RESOURCE_VLAUE);
            }
        } finally {
            getMethod2.releaseConnection();
        }
    }

    @Test(groups = {"wso2.as"}, dependsOnMethods = "testRegistryUsageDemo")
    public void testCarbonCachingDemo() throws IOException {
        log.info("Running carbon caching demo test case");
        ClientConnectionUtil.waitForPort(9763);
        String url1 = EXAMPLE_WEBAPP_URL + "/caching/index.jsp?add=Add&key=" + CACHE_KEY +
                      "&value=" + CACHE_VALUE;
        GetMethod getMethod1 = new GetMethod(url1);

        try {
            log.info("Adding test cache value to carbon context cache");
            int statusCode = httpClient.executeMethod(getMethod1);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod1.getStatusLine());
            }
        } finally {
            getMethod1.releaseConnection();
        }

        String url2 = EXAMPLE_WEBAPP_URL + "/caching/index.jsp?view=View&key=" + CACHE_KEY;
        GetMethod getMethod2 = new GetMethod(url2);
        try {
            log.info("Getting test cache value from carbon context");
            int statusCode = httpClient.executeMethod(getMethod2);
            if (statusCode != HttpStatus.SC_OK) {
                fail("Method failed: " + getMethod2.getStatusLine());
            } else {
                String cacheValue = getMethod2.
                        getResponseHeader("cache-value").getValue();
                assertEquals(cacheValue, CACHE_VALUE);
            }
        } finally {
            getMethod2.releaseConnection();
        }
    }

    @Test(groups = {"wso2.as"})
    public void testBasicAuth() throws Exception {
        log.info("Running Basic Authentication test case for example webapp ...");
        String userName = "admin";
        String pwd = "admin";
        String resourceURL = "http://localhost:9763/example/jsp/security/protected/index.jsp";

        // the first access attempt should be challenged
        Map<String, List<String>> reqHeaders1 =
                new HashMap<String, List<String>>();
        Map<String, List<String>> respHeaders1 =
                new HashMap<String, List<String>>();

        ByteChunk bc = new ByteChunk();
        int rc = getResponseCode(resourceURL, bc, 1000000, reqHeaders1,
                                 respHeaders1);

        assertEquals(401, rc);
        assertNull(bc.toString());

        // the second access attempt should be sucessful
        String credentials = userName + ":" + pwd;
        byte[] credentialsBytes = ByteChunk.convertToBytes(credentials);
        String base64auth = Base64.encode(credentialsBytes);
        String authLine = "Basic " + base64auth;

        List<String> auth = new ArrayList<String>();
        auth.add(authLine);
        Map<String, List<String>> reqHeaders2 = new HashMap<String, List<String>>();
        reqHeaders2.put(CLIENT_AUTH_HEADER, auth);

        Map<String, List<String>> respHeaders2 =
                new HashMap<String, List<String>>();

        bc.reset();
        rc = getResponseCode(resourceURL, bc, 1000000, reqHeaders2,
                             respHeaders2);


        assertEquals(200, rc);
//            assertEquals("OK", bc.toString());
    }

    private static int getResponseCode(String path, ByteChunk out, int readTimeout,
                                       Map<String, List<String>> reqHead,
                                       Map<String, List<String>> resHead) throws IOException {

        URL url = new URL(path);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setReadTimeout(readTimeout);
        if (reqHead != null) {
            for (Map.Entry<String, List<String>> entry : reqHead.entrySet()) {
                StringBuilder valueList = new StringBuilder();
                for (String value : entry.getValue()) {
                    if (valueList.length() > 0) {
                        valueList.append(',');
                    }
                    valueList.append(value);
                }
                connection.setRequestProperty(entry.getKey(),
                                              valueList.toString());
            }
        }
        connection.connect();
        int rc = connection.getResponseCode();
        if (resHead != null) {
            Map<String, List<String>> head = connection.getHeaderFields();
            resHead.putAll(head);
        }
        if (rc == HttpServletResponse.SC_OK) {
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(is);
                byte[] buf = new byte[2048];
                int rd = 0;
                while ((rd = bis.read(buf)) > 0) {
                    out.append(buf, 0, rd);
                }
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
        return rc;
    }
}
