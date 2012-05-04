/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/


package org.wso2.carbon.automation.selenium.cloud.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;
import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;

/**
 * Verity redirection from a Stratos Service to Manager.
 */
public class ServiceToManagerRedirectionTest {

    private static final Log log = LogFactory.getLog(ServiceToManagerRedirectionTest.class);
    private static WebDriver driver;
    private String userName;
    private String password;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseURL = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.APP_SERVER_NAME);
        log.info("baseURL is :" + baseURL);
        driver = BrowserManager.getWebDriver();
        driver.get(baseURL);
    }

    @Test(groups = {"wso2.manager"}, description = "Login to app server as tenant admin", priority = 1)
    public void testLoginToAppServer() throws Exception {
        String productName = "appsServer";
        new StratosUserLogin().userLogin(driver, null, userName, password, productName);
        log.info("Stratos AppServer Login Success");
    }

    @Test(groups = {"wso2.manager"}, description = "Login to manager from app server", priority = 2)
    public void testLoginToManager() throws Exception {
        driver.findElement(By.linkText("Stratos Manager")).click();
        assertTrue(driver.getPageSource().contains("Application Server"),
                   "Manager Home page Failed");
        assertTrue(driver.getPageSource().contains("Mashup Server"), "Manager Home page Failed");
        assertTrue(driver.getPageSource().contains("Identity Server"), "Manager Home page Failed");
        assertTrue(driver.getPageSource().contains("Message Broker"), "Manager Home page Failed");
        assertTrue(driver.getPageSource().contains("Enterprise Service Bus"),
                   "Manager Home page Failed");
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {
        driver.quit();
    }
}
