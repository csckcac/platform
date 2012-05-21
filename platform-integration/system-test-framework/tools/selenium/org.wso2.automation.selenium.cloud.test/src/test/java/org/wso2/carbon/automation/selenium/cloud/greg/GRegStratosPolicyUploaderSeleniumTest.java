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

import static org.testng.Assert.*;


public class GRegStratosPolicyUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosPolicyUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(ProductConstant
                                                                                 .GREG_SERVER_NAME);
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
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoAddPolicyPage();
            addPolicyURLDetails(policyURL, policyName);
            findPolicy(policyPath);
            deletePolicy();
            userLogout();
            log.info("********** GReg Stratos Policy Uploader from URL test - Passed ************");
        } catch (AssertionError e) {
            log.info("Policy Uploader from URL test Failed :" + e);
            userLogout();
            throw new AssertionError("Policy Uploader from URL test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Policy Uploader from URL test Failed :" + e);
            userLogout();
            throw new WebDriverException("Policy Uploader from URL test Failed :" + e);
        } catch (Exception e) {
            log.info("Policy Uploader from URL test Failed :" + e);
            userLogout();
            throw new Exception("Policy Uploader from URL test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a policy from File", priority = 2)
    public void testAddPolicyFromFile() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String file_path = resourcePath + File.separator + "artifacts" + File.separator +
                            File.separator + "GREG" + File.separator + "policy" +
                           File.separator + "policy.xml";
        String policyPath = "/_system/governance/trunk/policies/";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoAddPolicyPage();
            addPolicyFromFile(file_path);
            findPolicy(policyPath);
            deletePolicy();
            userLogout();
            log.info("********** GReg Stratos Policy Uploader from File test - Passed ************");
        } catch (AssertionError e) {
            log.info("Policy Uploader from File test  Failed :" + e);
            userLogout();
            throw new AssertionError("Policy Uploader from File test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Policy Uploader from File test  Failed :" + e);
            userLogout();
            throw new WebDriverException("Policy Uploader from File test  Failed :" + e);
        } catch (Exception e) {
            log.info("Policy Uploader from File test  Failed :" + e);
            userLogout();
            throw new Exception("Policy Uploader from File test  Failed :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void addPolicyFromFile(String file_path) throws InterruptedException {
        Select select = new Select(driver.findElement(By.id("addMethodSelector")));
        select.selectByVisibleText("Upload Policy from a file");
        driver.findElement(By.id("uResourceFile")).sendKeys(file_path);
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitForPolicyListPage();
        assertTrue(driver.getPageSource().contains("policy.xml"), "Policy Has not been uploaded");
    }


    private void deletePolicy() throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        driver.findElement(By.linkText("Delete")).click();
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/policies/policy.xml' permanently?"),
                   "Delete Policy Message Failed :");
        selenium.click("//button");
    }

    private void findPolicy(String policyPath) throws InterruptedException {
        driver.findElement(By.linkText("policy.xml")).click();
        driver.findElement(By.xpath("//input")).clear();
        driver.findElement(By.xpath("//input")).sendKeys(policyPath);
        driver.findElement(By.xpath("//input[2]")).click();
    }

    private void addPolicyURLDetails(String policyURL, String policyName)
            throws InterruptedException {
        driver.findElement(By.id("irFetchURL")).sendKeys(policyURL);
        driver.findElement(By.id("irResourceName")).sendKeys(policyName);
        driver.findElement(By.cssSelector("input.button.registryWriteOperation")).click();
        selenium.waitForPageToLoad("40000");
        waitForPolicyListPage();
        assertTrue(driver.getPageSource().contains("policy.xml"), "Policy Has not been uploaded");
    }

    private void gotoAddPolicyPage() throws InterruptedException {
        driver.findElement(By.linkText("WS Policy")).click();
        assertTrue(selenium.isTextPresent("Add Policy"), "Add Policy Page Title Fail :");
        assertTrue(selenium.isTextPresent("Add New Policy"), "Add Policy Page Fail :");
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }

    private boolean waitForPolicyListPage() {
        long currentTime = System.currentTimeMillis();
        long exceededTime;
        do {
            try {
                if (driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText()
                            .contains("Service Policy List")) {
                    return true;
                }
            } catch (WebDriverException ignored) {
                log.info("Waiting for the element");
            }
            exceededTime = System.currentTimeMillis();
        } while (!(((exceededTime - currentTime) / 1000) > 60));
        return false;
    }
}
