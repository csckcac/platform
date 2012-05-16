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
package org.wso2.carbon.automation.selenium.test.greg;

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregResourceURLUploader;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;

import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class GRegPolicyUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegPolicyUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String username;
    String password;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException {
        int userId = new GregUserIDEvaluator().getTenantID();
        String baseUrl = new GRegBackEndURLEvaluator().getBackEndURL();
        log.info("baseURL is " + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        UserInfo tenantDetails = UserListCsvReader.getUserInfo(userId);
        username = tenantDetails.getUserName();
        password = tenantDetails.getPassword();
    }

    @Test(groups = {"wso2.greg"}, description = "add a simple policy from Url", priority = 1)
    public void testAddPolicyFromURL() throws Exception {
        String policyURL = "https://wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/";
        String policyName = "policy.xml";
        try {
            new GregUserLogin().userLogin(driver, username, password);
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"), "G-Reg Home Page fail :");
            // goto add policy link
            driver.findElement(By.linkText("WS Policy")).click();
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("Add Policy"), "Add Policy Page Title Fail :");
            assertTrue(selenium.isTextPresent("Add New Policy"), "Add Policy Page Fail :");
            // Enter policy info
            new GregResourceURLUploader().uploadResource(driver, policyURL, policyName);
            Thread.sleep(10000L);
            assertTrue(selenium.isTextPresent("Service Policy List"), "Policy Dash Board fail :");
            assertTrue(selenium.isTextPresent("policy.xml"), "Uploaded policy does not appear on dashboard :");
            assertTrue(selenium.isTextPresent("Policy Name"), "Policy Dash Board fail :");
            //Delete policy
            driver.findElement(By.linkText("Delete")).click();
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Policy Delete Dialog Popup does not appear");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'/_system/governance/trunk/policies/policy.xml' permanently?"), "Delete Policy popup message failed :");
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");
            new GregUserLogout().userLogout(driver);
            log.info("******GRegPolicyUploaderSeleniumTest - testAddPolicyFromURL()-Passed*******");
        } catch (WebDriverException e) {
            log.info("Failed to upload policy from url - WebDriver Exception :" + e);
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GregAddPolicyfromURLSeleniumTest");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new WebDriverException("Failed to upload policy from url:" + e);
        } catch (Exception e) {
            log.info("Failed to upload policy from url - Fail :" + e);
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GregAddPolicyfromURLSeleniumTest");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new Exception("Failed to upload policy from url:" + e);
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();

    }
}
