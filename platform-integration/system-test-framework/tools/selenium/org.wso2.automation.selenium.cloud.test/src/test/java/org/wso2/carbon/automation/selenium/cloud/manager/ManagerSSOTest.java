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

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.assertTrue;

/**
 * Class to test SSO feature on Stratos
 */
public class ManagerSSOTest {
    private static final Log log = LogFactory.getLog(StratosManagerAccountValidatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "manager";
    private static UserInfo userDetails;
    private static String baseURL;
    String userName;
    String password;
    String domain;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        baseURL = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseURL);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseURL);
        driver.get(baseURL);
    }

    @Test(groups = {"wso2.manager"}, description = "Login to manager", priority = 1)
    public void testLoginToManager() throws Exception {
        new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
        log.info("Stratos Manager Login Success");
    }

    @Test(groups = {"wso2.manager"}, description = "Login to each service", priority = 2)
    public void testLoginToServices() throws Exception {
        String originalWindowId = driver.getWindowHandle();
        int numberOfServices = 12;
        for (int i = 1; i <= numberOfServices; i++) {
            driver.findElement(By.xpath("//tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/div/table/tbody/tr[" + i + "]/td/a")).click();
            Set<String> handlers = driver.getWindowHandles();

            Iterator itr = handlers.iterator();
            while (itr.hasNext()) {
                String windowId = itr.next().toString();
                driver.switchTo().window(windowId);

                //True if current url is not manger and windows id is the last one.
                if (!driver.getCurrentUrl().contains(baseURL) && !itr.hasNext()) {
                    log.info("Current URL is : " + driver.getCurrentUrl());
                    System.out.println(driver.findElement(By.id("logged-user")).getText());
                    System.out.println(userName + "@" + userDetails.getDomain());
                    assertTrue(driver.findElement(By.id("logged-user")).getText().contains(userName), "user not log in");
                    assertTrue(driver.getPageSource().toLowerCase().contains("quick start dashboard"), "Dashboard text not found");
                    driver.switchTo().window(originalWindowId);
                }
            }
        }
        driver.quit();
    }
}

