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

import static org.testng.Assert.assertTrue;

public class GRegStratosSchemaUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosSchemaUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    String schemaName = "books.xsd";
    String schema_path = "/_system/governance/trunk/schemas/books";
    long sleeptime = 4000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(ProductConstant.
                GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.stratos.greg"}, description = "add schema from url", priority = 1)
    public void testAddSchemafromURL() throws Exception {
        String schema_url = "http://people.wso2.com/~evanthika/schemas/books.xsd";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoSchemaPage();
            uploadSchemafromUrl(schema_url);
            assertTrue(driver.getPageSource().contains(schemaName), "Failed to add book schema :");
            assertTrue(driver.getPageSource().contains("urn:books"),
                       "Failed to display schemaNameSpace:");
            deleteSchema(schemaName, schema_path);
            userLogout();
            log.info("*************GReg Stratos Schema Upload from URL Test - Passed*************");
        } catch (AssertionFailedError e) {
            log.info("Schema Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Schema Upload from URL Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Schema Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Schema Upload from URL Test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("Schema Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Schema Upload from URL Test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.stratos.greg"}, description = "add schema from file", priority = 2)
    public void testAddSchemafromFile() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String file_path = resourcePath + File.separator + "artifacts" + File.separator +
                           "Selenium" + File.separator + "GREG" + File.separator + "schema" +
                           File.separator + "books.xsd";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoSchemaPage();
            addSchemaFromFile(file_path);
            deleteSchema(schemaName, schema_path);
            userLogout();
            log.info("*************GReg Stratos Schema Upload from File Test - Passed*************");
        } catch (AssertionFailedError e) {
            log.info("Schema Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Schema Upload from File Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Schema Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Schema Upload from File Test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("Schema Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Schema Upload from File Test Failed :" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void addSchemaFromFile(String file_path) throws InterruptedException {
        Select select = new Select(driver.findElement(By.id("addMethodSelector")));
        select.selectByVisibleText("Upload Schema from a file");
        waitTimeforElement("//p/input");
        selenium.focus("id=uResourceFile");
        Thread.sleep(sleeptime);
        driver.findElement(By.id("uResourceFile")).sendKeys(file_path);
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        log.info("Schema was successfully uploaded from file");
        assertTrue(driver.getPageSource().contains(schemaName), "Failed to add book schema :");
        assertTrue(driver.getPageSource().contains("urn:books"),
                   "Failed to display schemaNameSpace:");
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }


    private void uploadSchemafromUrl(String schema_url) throws InterruptedException {
        driver.findElement(By.id("irFetchURL")).sendKeys(schema_url);
        driver.findElement(By.id("irResourceName")).click();
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
    }

    private void gotoSchemaPage() throws InterruptedException {
        driver.findElement(By.linkText("Schema")).click();
        waitTimeforElement("//td[2]/input");
        assertTrue(driver.getPageSource().contains("Add Schema"),
                   "Failed to display Add Schema Page :");
        Thread.sleep(sleeptime);
    }

    private void deleteSchema(String schemaName, String path) throws InterruptedException {
        driver.findElement(By.linkText(schemaName)).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/schemas/books/books.xsd' permanently?"),
                   "Failed to delete wsdl :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
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
