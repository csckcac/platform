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
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.TestExceptionHandler;
import org.wso2.platform.test.core.utils.webapputils.WebAppUtil;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Deploy multiple webapps by multiple uses and send HTTP GET request to invoke webapps..
 */
public class MultipleWebAppUploaderClient {

    private static final Log log = LogFactory.getLog(MultipleWebAppUploaderClient.class);
    private static final String CALENDAR_WAR = "Calendar.war";
    private static final String MY_SERVLET_WAR_WAR = "myServletWAR.war";
    private static final String SAMPLE_WAR = "sample.war";
    ManageEnvironment environment3;
    ManageEnvironment environment2;
    ManageEnvironment environment1;
    WebAppWorker worker1;
    WebAppWorker worker2;
    WebAppWorker worker3;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        String resourcePath = ProductConstant.getResourceLocations(ProductConstant.APP_SERVER_NAME);
        EnvironmentBuilder builder3 = new EnvironmentBuilder().as(3);
        environment3 = builder3.build();

        EnvironmentBuilder builder1 = new EnvironmentBuilder().as(1);
        environment1 = builder1.build();

        EnvironmentBuilder builder2 = new EnvironmentBuilder().as(2);
        environment2 = builder2.build();

        worker1 = new WebAppWorker(environment1.getAs().getSessionCookie(),
                                         environment1.getAs().getBackEndUrl(),
                                         resourcePath + File.separator + "war"
                                         + File.separator + CALENDAR_WAR);

        worker2 = new WebAppWorker(environment1.getAs().getSessionCookie(),
                                         environment1.getAs().getBackEndUrl(),
                                         resourcePath + File.separator + "war"
                                         + File.separator + MY_SERVLET_WAR_WAR);

        worker3 = new WebAppWorker(environment3.getAs().getSessionCookie(),
                                         environment3.getAs().getBackEndUrl(),
                                         resourcePath + File.separator +
                                         "war" + File.separator + SAMPLE_WAR);
    }


    @Test (groups = "wso2.as", description = "Deploy three different webapp files by three users" +
                                             " concurrently on same app server", priority = 1)
    public void testConcurrentWebappDeployment() throws Exception {
        Thread t1 = new Thread(worker1);
        TestExceptionHandler exHandler = new TestExceptionHandler();
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
            log.error("Thread interruptted : " + ignored.getMessage());

        }

        if (exHandler.throwable != null) {
            exHandler.throwable.printStackTrace();
            fail(exHandler.throwable.getMessage());
        }

        WebAppUtil.waitForWebAppDeployment(environment1.getAs().getWebAppURL() +
                                           "/Calendar/Calendar.html", "GWT Calendar");
        WebAppUtil.waitForWebAppDeployment(environment2.getAs().getWebAppURL() +
                                           "/myServletWAR/hello", "from HelloServlet");
        WebAppUtil.waitForWebAppDeployment(environment3.getAs().getWebAppURL() +
                                           "/sample/hello", "Sample Application Servlet");

    }

    @Test (groups = "wso2.as", description = "Undeploy all the webapps deployed" , priority = 2)
    public void testCleanDeployedApps() throws Exception {
        worker1.deleteWebApp(CALENDAR_WAR);
        worker2.deleteWebApp(MY_SERVLET_WAR_WAR);
        worker3.deleteWebApp(SAMPLE_WAR);

        WebAppUtil.waitForWebAppUnDeployment(environment1.getAs().getWebAppURL() +
                                             "/Calendar/Calendar.html", "GWT Calendar");
        WebAppUtil.waitForWebAppUnDeployment(environment2.getAs().getWebAppURL() +
                                             "/myServletWAR/hello", "from HelloServlet");
        WebAppUtil.waitForWebAppUnDeployment(environment3.getAs().getWebAppURL() +
                                             "/sample/hello", "Sample Application Servlet");
    }

}
