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


public class ISClaimManagementSeleniumTest {
    private static final Log log = LogFactory.getLog(ISClaimManagementSeleniumTest.class);
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


    @Test(groups = {"wso2.greg"}, description = "Apply wso2.org claim Nick Name", priority = 1)
    public void testAddNewClaimManagement() throws Exception {
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            addClaim();
            log.info("Stratos IS Claim was updated");
            verifyUserProfile();
            addClaim();
            log.info("Stratos IS Claim was removed");
            userLogout();
            log.info("*******IS Stratos - Claim Management Test - Passed **********");
        } catch (AssertionError e) {
            log.info("Claim Management Test Failed :" + e);
            userLogout();
            throw new AssertionError("Claim Management Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Claim Management Test Failed:" + e);
            userLogout();
            throw new WebDriverException("Claim Management Test Failed :" + e);
        } catch (Exception e) {
            log.info("Claim Management Test Failed :" + e);
            userLogout();
            throw new Exception("Claim Management Test Failed:" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void verifyUserProfile() throws InterruptedException {
        driver.findElement(By.id("menu-panel-button1")).click();
        driver.findElement(By.linkText("My Profiles")).click();
        driver.findElement(By.linkText("Add New Profile")).click();
        assertTrue(selenium.isTextPresent("Other Phone"),
                   "Failed to Update newly added Claim to user profile :");
    }

    private void addClaim() throws InterruptedException {
        driver.findElement(By.id("menu-panel-button3")).click();
        driver.findElement(By.linkText("Claim Management")).click();
        driver.findElement(By.linkText("http://wso2.org/claims")).click();
        log.info("Claim was selected successfully ");
        driver.findElement(By.linkText("Edit")).click();
        driver.findElement(By.id("supported")).click();
        driver.findElement(By.xpath("//form/table/tbody/tr[2]/td/input")).click();
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }
}
