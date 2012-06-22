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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


public class WebappSampleTestCase {
    private static final String USER_NAME = "user1";
    private static final String PASS_WORD = "pass123";
    private static final String RESOURCE_VLAUE = "kicha";
    private static final String RESOURCE_PATH = "path/to/kicha";
    private static final String CACHE_KEY = "cacheKey1";
    private static final String CACHE_VALUE = "cacheValue1";

    private static final String EXAMPLE_WEBAPP_URL = "http://localhost:9763/example/carbon";
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
}
