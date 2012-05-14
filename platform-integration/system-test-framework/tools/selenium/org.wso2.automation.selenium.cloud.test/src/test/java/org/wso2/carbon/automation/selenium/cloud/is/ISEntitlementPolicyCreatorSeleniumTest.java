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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
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
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ISEntitlementPolicyCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(ISEntitlementPolicyCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;
    long sleeptime = 5 * 1000; // 5 seconds
    

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.IS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.greg"}, description = "create new policy from UI", priority = 1)
    public void testCreatePolicyFromUI() throws Exception {
        String policyName = "stratostestpolicy1";
        String policyDescription = "test stratos policy";
        String resourceName = "foo";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            gotoAdministrationPage();
            deleteEntitlementPolicy();
            createEntitlement(policyName, policyDescription, resourceName);
            defineRule1();
            defineRule2();
            saveEntitlement();
            driver.findElement(By.linkText("Enable")).click();          //enable policy
            gotoTryLinkPage(resourceName);
            deleteEntitlement();
            userLogout();
            log.info("*******IS Stratos - Create a new Policy from UI Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Create a new Policy from UI Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Create a new Policy from UI Test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Create a new Policy from UI Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Create a new Policy from UI Test Failed:" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Create a new Policy from UI Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Create a new Policy from UI Test Failed :" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteEntitlement() throws InterruptedException {
        driver.findElement(By.linkText("Administration")).click();
        driver.findElement(By.name("policies")).click();
        driver.findElement(By.id("delete1")).click();
        assertTrue(selenium.isTextPresent("exact:Do you want to delete the selected polices?"),
                   "Failed to Delete Policy :");
        selenium.click("//button");
    }

    private void gotoTryLinkPage(String resourceName) throws InterruptedException {
        driver.findElement(By.linkText("TryIt")).click();
        driver.findElement(By.id("resourceNames")).sendKeys(resourceName);
        driver.findElement(By.id("subjectNames")).sendKeys("admin");
        driver.findElement(By.xpath("//tr[7]/td/input")).click();
        Thread.sleep(5000);
        assertTrue(selenium.isTextPresent("Permit"), "admin Policy has not been applied :");
        selenium.click("//button");

        driver.findElement(By.id("subjectNames")).clear();
        driver.findElement(By.id("subjectNames")).sendKeys("admin123");
        driver.findElement(By.id("actionNames")).sendKeys("read");
        driver.findElement(By.xpath("//tr[7]/td/input")).click();
        Thread.sleep(5000);
        assertTrue(selenium.isTextPresent("Deny"), "admin Policy has not been applied :");
        selenium.click("//button");
    }

    private void saveEntitlement() throws InterruptedException {
        driver.findElement(By.xpath("//tr[7]/td/input")).click();
        assertEquals("Entitlement policy added successfully. Policy is disabled by default.",
                     selenium.getText("//p"), "Failed to pop up message when policy is created");
    }

    private void defineRule2() throws InterruptedException {
        driver.findElement(By.xpath("//tr[5]/td/h2")).click();
        driver.findElement(By.id("ruleId")).sendKeys("rule2");
        Select selectrule2 = new Select(driver.findElement(By.id("ruleEffect")));
        selectrule2.selectByVisibleText("Deny");
        driver.findElement(By.id("subjectNames")).sendKeys("admin123");
        driver.findElement(By.id("actionNames")).sendKeys("read");
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
    }

    private void defineRule1() throws InterruptedException {
        driver.findElement(By.xpath("//tr[5]/td/h2")).click();
        driver.findElement(By.id("ruleId")).sendKeys("rule1");
        Select select1 = new Select(driver.findElement(By.id("ruleEffect")));
        select1.selectByVisibleText("Permit");
        driver.findElement(By.id("subjectNames")).sendKeys("admin");
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
    }

    private void createEntitlement(String policyName, String policyDescription, String resourceName)
            throws InterruptedException {
        driver.findElement(By.linkText("Add New Entitlement Policy")).click();
        //enter policy details
        driver.findElement(By.id("policyName")).sendKeys(policyName);
        Select select = new Select(driver.findElement(By.id("algorithmName")));
        select.selectByVisibleText("first-applicable");
        driver.findElement(By.id("policyDescription")).sendKeys(policyDescription);
        //enter policy applies to details
        driver.findElement(By.xpath("//td/h2")).click();
        driver.findElement(By.id("resourceNamesTarget")).sendKeys(resourceName);
    }

    private void gotoAdministrationPage() throws InterruptedException {
        driver.findElement(By.linkText("Administration")).click();
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }
    
    private void deleteEntitlementPolicy(){
        WebElement webElementList = driver.findElement(By.className("chkBox"));
        if (webElementList.getAttribute("value").contains("stratostestpolicy1")){
            webElementList.click();
            driver.findElement(By.id("delete1")).click();
            driver.findElement(By.xpath("//div[3]/div[2]/button")).click();
        }
    }
}
