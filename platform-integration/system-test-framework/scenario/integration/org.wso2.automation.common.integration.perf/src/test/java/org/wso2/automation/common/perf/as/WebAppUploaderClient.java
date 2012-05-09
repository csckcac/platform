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

package org.wso2.automation.common.perf.as;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.TestExceptionHandler;
import org.wso2.platform.test.core.utils.webapputils.WebAppUtil;

import java.io.*;
import java.rmi.RemoteException;

/**
 * Deploy same webapps by multiple uses and send HTTP GET request to invoke them.
 */
public class WebAppUploaderClient {

    private static final Log log = LogFactory.getLog(WebAppUploaderClient.class);

    private static final String SAMPLE_WAR_FILE_NAME = "SimpleServlet.war";
    WebAppWorker worker1;
    WebAppWorker worker2;
    WebAppWorker worker3;
    ManageEnvironment environment3;
    ManageEnvironment environment2;
    ManageEnvironment environment1;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        String resourcePath = ProductConstant.getResourceLocations(ProductConstant.APP_SERVER_NAME);
        EnvironmentBuilder builder1 = new EnvironmentBuilder().as(1);
        environment1 = builder1.build();

        EnvironmentBuilder builder2 = new EnvironmentBuilder().as(2);
        environment2 = builder2.build();

        EnvironmentBuilder builder3 = new EnvironmentBuilder().as(3);
        environment3 = builder3.build();

        worker1 = new WebAppWorker(environment1.getAs().getSessionCookie(),
                                         environment1.getAs().getBackEndUrl(),
                                         resourcePath + File.separator + "war"
                                         + File.separator + SAMPLE_WAR_FILE_NAME);

        worker2 = new WebAppWorker(environment2.getAs().getSessionCookie(),
                                         environment2.getAs().getBackEndUrl(),
                                         resourcePath + File.separator + "war"
                                         + File.separator + SAMPLE_WAR_FILE_NAME);

        worker3 = new WebAppWorker(environment3.getAs().getSessionCookie(),
                                         environment3.getAs().getBackEndUrl(),
                                         resourcePath + File.separator +
                                         "war" + File.separator + SAMPLE_WAR_FILE_NAME);
    }

    @Test(groups = {"wso2.as"}, description = "Upload the same webapp by multiple users", priority = 1)
    public void runSuccessCase() throws Exception {
        TestExceptionHandler exHandler = new TestExceptionHandler();

        Thread t1 = new Thread(worker1);
        t1.setUncaughtExceptionHandler(exHandler);
        t1.start();

        Thread t2 = new Thread(worker2);
        t2.setUncaughtExceptionHandler(exHandler);
        t2.start();

        Thread t3 = new Thread(worker3);
        t3.setUncaughtExceptionHandler(exHandler);
        t3.start();

        //wait for thread to finish
        try {
            t1.join();
            t2.join();
            t3.join();

        } catch (InterruptedException ignored) {

        }

        if (exHandler.throwable != null) {
            exHandler.throwable.printStackTrace();
            exHandler.throwable.getMessage();
        }


        WebAppUtil.waitForWebAppDeployment(environment1.getAs().getWebAppURL() +
                                           "/SimpleServlet/simple-servlet", "Hello");
        WebAppUtil.waitForWebAppDeployment(environment2.getAs().getWebAppURL() +
                                           "/SimpleServlet/simple-servlet", "Hello");
        WebAppUtil.waitForWebAppDeployment(environment3.getAs().getWebAppURL() +
                                           "/SimpleServlet/simple-servlet", "Hello");
    }

    @Test(groups = "wso2.as", description = "Undeploy the webapp", priority = 2)
    public void testCleanDeployedApps() throws Exception {
        worker1.deleteWebApp(SAMPLE_WAR_FILE_NAME);
        worker2.deleteWebApp(SAMPLE_WAR_FILE_NAME);
        worker3.deleteWebApp(SAMPLE_WAR_FILE_NAME);

        WebAppUtil.waitForWebAppUnDeployment(environment1.getAs().getWebAppURL() +
                                             "/SimpleServlet/simple-servlet", "Hello");

       WebAppUtil.waitForWebAppUnDeployment(environment2.getAs().getWebAppURL() +
                                             "/SimpleServlet/simple-servlet", "Hello");

        WebAppUtil.waitForWebAppUnDeployment(environment3.getAs().getWebAppURL() +
                                             "/SimpleServlet/simple-servlet", "Hello");
    }


}
