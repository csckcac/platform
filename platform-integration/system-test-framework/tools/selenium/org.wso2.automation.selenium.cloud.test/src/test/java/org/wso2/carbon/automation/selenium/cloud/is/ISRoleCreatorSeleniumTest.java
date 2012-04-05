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

package org.wso2.carbon.automation.selenium.cloud.is;

import com.thoughtworks.selenium.Selenium;
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
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
import java.util.Calendar;

import static org.testng.Assert.assertTrue;


public class ISRoleCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(ISRoleCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;
    String domain;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.IS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.greg"}, description = "add a new role with login permission", priority = 1)
    public void testAddNewLoginRole() throws Exception {
        String baseURL = "https://identity.stratoslive.wso2.com";
        String roleName = "login";
        String userManagementURL = baseURL + "/t/" + domain + "/carbon/userstore/index.jsp?region" +
                                   "=region1&item=userstores_menu";
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            driver.get(userManagementURL);
            waitTimeforElement("//tr[2]/td/a");
            addRole(roleName);
            deleteRole();
            userLogout();
            log.info("*******IS Stratos - Add New Role Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Failed to create  new role :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Failed to create  new role :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Failed to create  new role :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Failed to create  new role :" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to create  new role :" + e.getMessage());
            userLogout();
            throw new Exception("Failed to create  new role :" + e.getMessage());
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteRole() throws InterruptedException {
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div/div/span");
        assertTrue(selenium.isTextPresent("exact:Do you wish to delete the role login?"),
                   "Failed to delete Role :");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");
    }

    private void addRole(String roleName) throws InterruptedException {
        driver.findElement(By.linkText("Roles")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        driver.findElement(By.linkText("Add New Role")).click();
        waitTimeforElement("//input");
        driver.findElement(By.name("roleName")).sendKeys(roleName);
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        waitTimeforElement("//input[3]");
        driver.findElement(By.xpath("//div[3]/table/tbody/tr/td[4]/div")).click();
        driver.findElement(By.xpath("//input[3]")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }


    private void waitTimeforElement(String elementName) throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        long time;
        boolean element = false;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()))
               < 120 * 1000) {
            if (selenium.isElementPresent(elementName)) {
                element = true;
                break;
            }
            Thread.sleep(1000);
            log.info("waiting for element :" + elementName);
        }
        assertTrue(element, "Element Not Found within 2 minutes :");
    }
}
