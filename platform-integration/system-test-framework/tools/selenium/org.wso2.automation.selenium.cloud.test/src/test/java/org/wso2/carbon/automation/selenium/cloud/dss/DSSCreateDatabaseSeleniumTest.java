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
package org.wso2.carbon.automation.selenium.cloud.dss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.selenium.cloud.dss.utils.DSSServerUIUtils;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;

import java.net.MalformedURLException;
import java.util.List;

/*https://wso2.org/jira/browse/STRATOS-2045*/
public class DSSCreateDatabaseSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSCreateDatabaseSeleniumTest.class);
    private WebDriver driver;
    private DSSServerUIUtils dssServerUI;
    private UserInfo userDetails;
    private String dataBaseName = "tmp_database";

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUI = new DSSServerUIUtils(driver);
        dssServerUI.login(userDetails.getUserName(), userDetails.getPassword());
        dssServerUI.dropDatabaseIfExist(dataBaseName + "_" + userDetails.getDomain().replace('.', '_'));
        dssServerUI.dropDatabaseIfExist(dataBaseName + "_" + userDetails.getDomain().replace('.', '_'));
        dssServerUI.dropDatabaseIfExist(dataBaseName + "_" + userDetails.getDomain().replace('.', '_'));

    }


    @Test(priority = 0)
    public void createDataBase() throws InterruptedException {
        dssServerUI.createDatabase(dataBaseName);

    }

    @Test(priority = 1, dependsOnMethods = {"createDataBase"}, invocationCount = 3)
    public void createDatabaseNameAlreadyExistTest() throws InterruptedException {
        driver.findElement(By.linkText("Databases")).click();
        driver.findElement(By.linkText("Add Database")).click();
        Assert.assertEquals(driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText(),
                            "New Database",
                            "Failed to display Add New Database page :");
        Select select = new Select(driver.findElement(By.id("instances")));
        select.selectByVisibleText("WSO2_RSS");
        driver.findElement(By.id("dbName")).sendKeys(dataBaseName);
        List<WebElement> buttonList = driver.findElement(By.id("addDatabaseForm")).
                findElements(By.tagName("input"));
        for (WebElement button : buttonList) {
            if (button.getAttribute("value").equalsIgnoreCase("Create")) {
                button.click();
                break;
            }
        }
        Assert.assertTrue((driver.findElement(By.id("dialog")).findElements(By.id("messagebox-info")).size() == 0),
                          "Database creation error message box not found. Database created with same name. Message : "
                          + driver.findElement(By.id("dialog")).getText());

        Assert.assertTrue((driver.findElement(By.id("dialog")).findElements(By.id("messagebox-error")).size() > 0),
                          "Database creation error message box not found Message :" + driver.findElement(By.id("dialog")).getText());
        Assert.assertTrue((driver.findElement(By.id("dialog")).findElement(By.id("messagebox-error")).getText()
                                   .contains("Failed to create database")),
                          "Database creation error pop up message mismatched. message : "
                          + driver.findElement(By.id("dialog")).findElement(By.id("messagebox-error")).getText());
        driver.findElement(By.xpath("//button")).click();
    }

    @Test(priority = 3, dependsOnMethods = {"createDatabaseNameAlreadyExistTest"})
    public void deleteDatabase() throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement dbElement : databaseList) {
            if (dbElement.findElements(By.tagName("td")).get(0).getText().contains(dataBaseName)) {

                dbElement.findElement(By.linkText("Drop")).click();
                Assert.assertEquals(driver.findElement(By.className("ui-dialog-container")).findElement(By.id("dialog")).getText(),
                                    "Do you want to drop the database?",
                                    "Database Dropping confirmation message mismatched");
                Thread.sleep(1000);
                driver.findElement(By.xpath("//button")).click();
                break;
            }

        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        try {
            dssServerUI.logOut();
        } finally {
            driver.quit();
        }

    }
}
