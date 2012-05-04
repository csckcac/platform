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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ISCreatePolicySetSeleniumTest {
    private static final Log log = LogFactory.getLog(ISCreatePolicySetSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;
    String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;


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

    @Test(groups = {"wso2.greg"}, description = "Apply policy algorithm to a set", priority = 1)
    public void testAddSetOfPolicy() throws Exception {
        String filePath1 = resourcePath + File.separator + "artifacts" + File.separator +
                           "IS" + File.separator + "IIA001Policy.xml";
        String filePath2 = resourcePath + File.separator + "artifacts" + File.separator +
                           "IS" + File.separator +"IIA003Policy.xml";
        String policySetName = "testpolicygroup";
        String policyDescription = "test a set of policies";
        String resourceName = "groupfoo";
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            gotoAdministrationPage();
            addPolicyfromFile(filePath1);
            assertTrue(selenium.isTextPresent("exact:urn:oasis:names:tc:xacml:2.0:" +
                                              "conformance-test:IIA1:policy"),
                       "Failed to upload IIA001Policy.xml policy file ");
            driver.findElement(By.linkText("Enable")).click();
            waitTimeforElement("//td[3]/a");
            addPolicyfromFile(filePath2);
            assertTrue(selenium.isTextPresent("exact:urn:oasis:names:tc:xacml:2.0:" +
                                              "conformance-test:IIA003:policy"),
                       "Failed to upload IIA003Policy.xml policy file ");
            driver.findElement(By.linkText("Enable")).click();
            waitTimeforElement("//td[3]/a");
            addPolicySet(policySetName, policyDescription, resourceName);
            driver.findElement(By.linkText("Enable")).click();
            waitTimeforElement("//td[3]/a");
            gotoTryItPage(resourceName);
            gotoAdministrationPage();
            deleteArtifacts();
            userLogout();
            log.info("********** IS Stratos Create Policy Set Test - passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Create Policy Set Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Create Policy Set Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Create Policy Set Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Create Policy Set Test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("Create Policy Set Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Create Policy Set Test Failed :" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteArtifacts() throws InterruptedException {
        driver.findElement(By.xpath("//td[2]/input")).click();
        driver.findElement(By.xpath("//tr[2]/td[2]/input")).click();
        driver.findElement(By.xpath("//tr[3]/td[2]/input")).click();
        driver.findElement(By.id("delete1")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to delete the selected polices?"),
                   "Failed to delete created Policy Group :");
        selenium.click("//button");
        waitTimeforElement("//td[4]/a");
        log.info("Artifacts were deleted successfully !");
    }

    private void gotoTryItPage(String resourceName) throws InterruptedException {
        driver.findElement(By.linkText("TryIt")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("resourceNames")).sendKeys(resourceName);
        driver.findElement(By.id("subjectNames")).sendKeys("admin");
        driver.findElement(By.id("actionNames")).sendKeys("read");
        driver.findElement(By.xpath("//tr[7]/td/input")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Deny"), "Failed to invoke created Policy Group :");
        selenium.click("//button");
    }

    private void addPolicySet(String policySetName, String policyDescription, String resourceName)
            throws InterruptedException {
        driver.findElement(By.linkText("Add New Policy Set")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("policySetName")).sendKeys(policySetName);
        Select select = new Select(driver.findElement(By.id("policyAlgorithmName")));
        select.selectByVisibleText("first-applicable");
        driver.findElement(By.id("policySetDescription")).sendKeys(policyDescription);
        driver.findElement(By.xpath("//td/h2")).click();
        waitTimeforElement("//td[2]/table/tbody/tr/td[2]/input");
        driver.findElement(By.id("resourceNamesTarget")).sendKeys(resourceName);
        driver.findElement(By.id("subjectNamesTarget")).sendKeys("admin");
        driver.findElement(By.id("actionNamesTarget")).sendKeys("read");
        driver.findElement(By.linkText("Add to Policy Set")).click();
//        Thread.sleep(10000L);
        Select selectPolicyType = new Select(driver.findElement(By.id("policyIds")));
        selectPolicyType.selectByVisibleText("urn:oasis:names:tc:xacml:2.0:conformance-test:" +
                                             "IIA1:policy");
        driver.findElement(By.linkText("Add to Policy Set")).click();
        driver.findElement(By.xpath("//tr[7]/td/input")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Entitlement policy added successfully. Policy is " +
                                          "disabled by default."),
                   "Failed to create a set of Policies :");
        selenium.click("//button");
        waitTimeforElement("//td[4]/a");
    }


    private void addPolicyfromFile(String filePath) throws InterruptedException {
        driver.findElement(By.linkText("Import New Entitlement Policy")).click();
        waitTimeforElement("//select");
        driver.findElement(By.id("policyFromFileSystem")).sendKeys(filePath);
        driver.findElement(By.xpath("//tr[4]/td/input")).click();
        Thread.sleep(10000L);
        assertEquals("Entitlement policy imported successfully", selenium.getText("//p"),
                     "Entitlement Policy Import Message Failed:");
        selenium.click("//button");
        waitTimeforElement("//td[3]/a");
    }

    private void gotoAdministrationPage() throws InterruptedException {
        driver.findElement(By.linkText("Administration")).click();
        waitTimeforElement("//td[3]/div/a");
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
