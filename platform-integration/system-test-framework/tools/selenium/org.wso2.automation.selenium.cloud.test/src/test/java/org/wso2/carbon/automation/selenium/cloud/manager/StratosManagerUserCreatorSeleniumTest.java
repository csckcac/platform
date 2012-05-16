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
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.seleniumutils.UserManagementSeleniumUtils;

import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;


public class StratosManagerUserCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(StratosManagerUserCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "manager";
    String userName;
    String password;
    String domain;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(7);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.manager"}, description = "create a new user with admin role", priority = 1)
    public void testAddNewUser() throws Exception {
        String newUserName = "manager123";
        String newUserPassword = "manager123";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos Manager Login Success");
            gotoUserManagementPage();
            UserManagementSeleniumUtils.deleteUserByName(driver, newUserName);
            createNewUser(newUserName, newUserPassword);
            findUser(newUserName);
            UserManagementSeleniumUtils.deleteUserByName(driver, newUserName);
            userLogout();
            log.info("*******Manager Stratos - Add New User Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Failed to create  new User :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Failed to create  new User :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Failed to create  new User :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Failed to create  new User :" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to create  new User :" + e.getMessage());
            userLogout();
            throw new Exception("Failed to create  new User :" + e.getMessage());
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void createNewUser(String newUserName, String newUserpassword)
            throws InterruptedException {
        driver.findElement(By.linkText("Add New User")).click();
        //enter user info
        driver.findElement(By.name("username")).sendKeys(newUserName);
        driver.findElement(By.name("password")).sendKeys(newUserpassword);
        driver.findElement(By.name("retype")).sendKeys(newUserpassword);
        driver.findElement(By.xpath("//form/table/tbody/tr[2]/td/input")).click();
        //assign roles
        driver.findElement(By.name("userRoles")).click();
        driver.findElement(By.xpath("//input[2]")).click();
    }

    private void gotoUserManagementPage() throws InterruptedException {
        driver.findElement(By.id("menu-panel-button3")).click();
        driver.findElement(By.linkText("Users and Roles")).click();
        driver.findElement(By.linkText("Users")).click();
    }

    private void findUser(String newUserName) {
        driver.findElement(By.xpath("//input")).clear();
        driver.findElement(By.xpath("//input")).sendKeys(newUserName);
        driver.findElement(By.xpath("//td[3]/input")).click();
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }
}
