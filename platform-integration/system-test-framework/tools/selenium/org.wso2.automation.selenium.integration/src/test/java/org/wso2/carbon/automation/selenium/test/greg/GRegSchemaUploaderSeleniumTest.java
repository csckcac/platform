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
import org.openqa.selenium.support.ui.Select;
//import org.wso2.carbon.admin.service.utils.ProductConstant;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregResourceURLUploader;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;

import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;


public class GRegSchemaUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegSchemaUploaderSeleniumTest.class);
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

    @Test(groups = {"wso2.greg"}, description = "add person schema from url", priority = 1)
    public void testAddSchemaFromURL() throws Exception {
        String schemaURL = "http://people.wso2.com/~evanthika/schemas/org/company/www/person/Person.xsd";
        try {
            userLogin();
            gotoSchemaPage();

            new GregResourceURLUploader().uploadResource(driver, schemaURL, null);               // enter Schema info
            Thread.sleep(10000L);
            log.info("Schema was successfully uploaded from url");
            //  Schema added successfully
            assertTrue(selenium.isTextPresent("Schema List"), "Schema Dash board Fail :");
            assertTrue(selenium.isTextPresent("Person.xsd"), "Uploaded Schema name does not display on Shema Dash board :");
            assertTrue(selenium.isTextPresent("exact:http://www.company.org"), "Uploaded Schema nameSpace does not display on Shema Dash board  :");
            // click on Delete link
            driver.findElement(By.linkText("Delete")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Schema Delete pop-up title fail :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'/_system/governance/trunk/schemas/org/company/www/Person.xsd' permanently?"), "Schema Delete pop-up message fail :");
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");

            new GregUserLogout().userLogout(driver);
            log.info("******GRegSchemaUploaderSeleniumTest - testAddSchemafromURL() -Passed******");
        } catch (WebDriverException e) {
            log.info("Failed to upload schema from url- WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegSchemaUploader_addSchemafromURL");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new WebDriverException("Failed to upload schema from url:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to upload schema from url:- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegSchemaUploader_addSchemafromURL");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new Exception("Failed to upload schema from url:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add person schema from file", priority = 2)
    public void testAddSchemaFromFile() throws InterruptedException, IOException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String schema_path = resourcePath + File.separator + "artifacts" + File.separator + "GREG"
                             + File.separator + "schema" + File.separator
                             + "Person.xsd";
        try {
            userLogin();
            gotoSchemaPage();
            //click on upload schema from file drop down
            Select select = new Select(driver.findElement(By.id("addMethodSelector")));
            select.selectByVisibleText("Upload Schema from a file");

            selenium.focus("id=uResourceFile");
            Thread.sleep(3000L);
            driver.findElement(By.id("uResourceFile")).sendKeys(schema_path);
//            selenium.type("id=uResourceFile", schema_path);
            Thread.sleep(10000L);
            //click on Add button
            driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
            Thread.sleep(15000L);
            log.info("Schema was successfully uploaded from file");
            assertTrue(selenium.isTextPresent("Schema List"), "Schema Dash board Fail :");
            assertTrue(selenium.isTextPresent("Person.xsd"), "Uploaded Schema name does not display " +
                                                             "on Schema Dashboard :");
            assertTrue(selenium.isTextPresent("exact:http://www.company.org"), "Uploaded Schema " +
                                                                               "nameSpace does not " +
                                                                               "display on Schema Dashboard  :");
            // click on Delete link
            driver.findElement(By.linkText("Delete")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Schema Delete pop-up fail :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'/_system/governance" +
                                              "/trunk/schemas/org/company/www/Person.xsd' permanently?"),
                       "Schema Delete pop-up fail :");
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");

            //logout
            new GregUserLogout().userLogout(driver);

            log.info("******GRegSchemaUploaderSeleniumTest - testAddSchemafromFile() -Passed******");

        } catch (WebDriverException e) {
            log.info("Failed to add schema from file- WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegSchemaUploader_addSchemafromFile");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(5000L);
            throw new WebDriverException("Failed to add schema from file:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add schema from file:- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegSchemaUploader_addSchemafromFile");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(5000L);
            throw new WebDriverException("Failed to upload schema from url:" + e.getMessage());
        }
    }

    private void userLogin() {
        new GregUserLogin().userLogin(driver, username, password);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"), "G-Reg Home Page fail :");
    }

    private void gotoSchemaPage() {
        driver.findElement(By.linkText("Schema")).click();             //Click on  Add Schema link
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Add Schema"), "Add Schema page title Fail :");
        assertTrue(selenium.isTextPresent("Add New Schema"), "Add Schema page description Fail :");
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();

    }


}
