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
package org.wso2.carbon.automation.selenium.cloud.greg;

import com.thoughtworks.selenium.Selenium;
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.*;


public class GRegStratosPolicyUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosPolicyUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    long sleeptime = 4000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.greg"}, description = "add a policy from URL", priority = 1)
    public void testAddPolicyfromURL() throws Exception {
        String policyURL = "https://wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/policies/";
        String policyName = "policy.xml";
        String policyPath = "/_system/governance/trunk/policies/";
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoAddPolicyPage();
            addPolicyURLDetails(policyURL, policyName);
            findPolicy(policyPath);
            deletePolicy();
            userLogout();
            log.info("********** GReg Stratos Policy Uploader from URL test - Passed ************");
        } catch (AssertionFailedError e) {
            log.info("Policy Uploader from URL test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Policy Uploader from URL test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Policy Uploader from URL test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Policy Uploader from URL test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("Policy Uploader from URL test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Policy Uploader from URL test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a policy from File", priority = 2)
    public void testAddPolicyfromFile() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String file_path = resourcePath + File.separator + "artifacts" + File.separator +
                           "Selenium" + File.separator + "GREG" + File.separator + "policy" +
                           File.separator + "policy.xml";
        String policyPath = "/_system/governance/trunk/policies/";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoAddPolicyPage();
            addPolicyfromFile(file_path);
            findPolicy(policyPath);
            deletePolicy();
            userLogout();
            log.info("********** GReg Stratos Policy Uploader from File test - Passed ************");
        } catch (AssertionFailedError e) {
            log.info("Policy Uploader from File test  Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Policy Uploader from File test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Policy Uploader from File test  Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Policy Uploader from File test  Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("Policy Uploader from File test  Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Policy Uploader from File test  Failed :" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void addPolicyfromFile(String file_path) throws InterruptedException {
        Select select = new Select(driver.findElement(By.id("addMethodSelector")));
        select.selectByVisibleText("Upload Policy from a file");
        waitTimeforElement("//p/input");
        selenium.focus("id=uResourceFile");
        Thread.sleep(sleeptime);
        driver.findElement(By.id("uResourceFile")).sendKeys(file_path);
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
        waitTimeforElement("//form/table/tbody/tr/td/a");
        assertTrue(driver.getPageSource().contains("Service Policy List"), "Failed to display " +
                                                                           "Policy Dash board ");
        assertTrue(driver.getPageSource().contains("policy.xml"), "Policy Has not been uploaded");
        Thread.sleep(sleeptime);
    }


    private void deletePolicy() throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/policies/policy.xml' permanently?"),
                   "Delete Policy Message Failed :");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");
    }

    private void findPolicy(String policyPath) throws InterruptedException {
        driver.findElement(By.linkText("policy.xml")).click();
        waitTimeforElement("//input");
        driver.findElement(By.xpath("//input")).clear();
        driver.findElement(By.xpath("//input")).sendKeys(policyPath);
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
    }

    private void addPolicyURLDetails(String policyURL, String policyName)
            throws InterruptedException {
        driver.findElement(By.id("irFetchURL")).sendKeys(policyURL);
        driver.findElement(By.id("irResourceName")).sendKeys(policyName);
        driver.findElement(By.cssSelector("input.button.registryWriteOperation")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        assertTrue(driver.getPageSource().contains("Service Policy List"), "Failed to display " +
                                                                           "Policy Dash board ");
        assertTrue(driver.getPageSource().contains("policy.xml"), "Policy Has not been uploaded");
    }

    private void gotoAddPolicyPage() throws InterruptedException {
        driver.findElement(By.linkText("WS Policy")).click();
        waitTimeforElement("//select");
        assertTrue(selenium.isTextPresent("Add Policy"), "Add Policy Page Title Fail :");
        assertTrue(selenium.isTextPresent("Add New Policy"), "Add Policy Page Fail :");
        Thread.sleep(sleeptime);
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
