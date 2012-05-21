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

import static org.testng.Assert.assertTrue;

public class GRegStratosSchemaUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosSchemaUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    private static String productName = "greg";
    private static String userName;
    private static String password;
    private static String schemaName = "books.xsd";
    private static String schema_path = "/_system/governance/trunk/schemas/books";

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
        String schema_url = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                            "platform-integration/system-test-framework/core/org.wso2.automation.platform.core/" +
                            "src/main/resources/artifacts/GREG/schema/books.xsd";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoSchemaPage();
            uploadSchemaFromURL(schema_url);
            waitForSchemaListPage();
            assertTrue(driver.getPageSource().contains(schemaName), "Failed to add book schema :");
            assertTrue(driver.getPageSource().contains("urn:books"),
                       "Failed to display schemaNameSpace:");
            deleteSchema(schemaName, schema_path);
            userLogout();
            log.info("*************GReg Stratos Schema Upload from URL Test - Passed*************");
        } catch (AssertionError e) {
            log.info("Schema Upload from URL Test Failed :" + e);
            userLogout();
            throw new AssertionError("Schema Upload from URL Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Schema Upload from URL Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Schema Upload from URL Test Failed :" + e);
        } catch (Exception e) {
            log.info("Schema Upload from URL Test Failed :" + e);
            userLogout();
            throw new Exception("Schema Upload from URL Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.stratos.greg"}, description = "add schema from file", priority = 2)
    public void testAddSchemafromFile() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String file_path = resourcePath + File.separator + "artifacts" + File.separator +
                           File.separator + "GREG" + File.separator + "schema" +
                           File.separator + "books.xsd";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoSchemaPage();
            addSchemaFromFile(file_path);
            deleteSchema(schemaName, schema_path);
            userLogout();
            log.info("*************GReg Stratos Schema Upload from File Test - Passed*************");
        } catch (AssertionError e) {
            log.info("Schema Upload from File Test Failed :" + e);
            userLogout();
            throw new AssertionError("Schema Upload from File Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Schema Upload from File Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Schema Upload from File Test Failed :" + e);
        } catch (Exception e) {
            log.info("Schema Upload from File Test Failed :" + e);
            userLogout();
            throw new Exception("Schema Upload from File Test Failed :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void addSchemaFromFile(String file_path) throws InterruptedException {
        Select select = new Select(driver.findElement(By.id("addMethodSelector")));
        select.selectByVisibleText("Upload Schema from a file");
        selenium.focus("id=uResourceFile");
        driver.findElement(By.id("uResourceFile")).sendKeys(file_path);
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitForSchemaListPage();
        log.info("Schema was successfully uploaded from file");
        assertTrue(driver.getPageSource().contains(schemaName), "Failed to add book schema :");
        assertTrue(driver.getPageSource().contains("urn:books"),
                   "Failed to display schemaNameSpace:");
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }

    private void uploadSchemaFromURL(String schema_url) throws InterruptedException {
        driver.findElement(By.id("irFetchURL")).sendKeys(schema_url);
        driver.findElement(By.id("irResourceName")).click();
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
    }

    private void gotoSchemaPage() throws InterruptedException {
        driver.findElement(By.linkText("Schema")).click();
        assertTrue(driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText()
                           .contains("Add Schema"), "Add schema page not found");
    }

    private void deleteSchema(String schemaName, String path) throws InterruptedException {
        driver.findElement(By.linkText(schemaName)).click();
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        driver.findElement(By.xpath("//input[2]")).click();
        driver.findElement(By.id("actionLink1")).click();
        driver.findElement(By.linkText("Delete")).click();
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/schemas/books/books.xsd' permanently?"),
                   "Failed to delete wsdl :");
        selenium.click("//button");
    }

    private boolean waitForSchemaListPage() {
        long currentTime = System.currentTimeMillis();
        long exceededTime;
        do {
            try {
                if (driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText()
                            .contains("Schema List")) {
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
