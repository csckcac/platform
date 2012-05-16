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
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.net.MalformedURLException;

/*This class tests DSS> Tools > DB Explorer page*/
public class DSSDatabaseExplorerSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSDatabaseExplorerSeleniumTest.class);
    private WebDriver driver;
    private DSSServerUIUtils dssServerUI;
    private UserInfo userDetails;

    private FrameworkProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private final String driverClass = "com.mysql.jdbc.Driver";
    private String dataBaseName;
    private String dbUserName;
    private String dbUserPassword;

    private String jdbcUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUI = new DSSServerUIUtils(driver);
        dssServerUI.login(userDetails.getUserName(), userDetails.getPassword());
        dataBaseName = dssProperties.getDataSource().getDbName() + "_" + userDetails.getDomain().replace('.', '_');
        dbUserName = dssServerUI.getFullyQualifiedUsername(dssProperties.getDataSource().getRssDbUser(), userDetails.getDomain());
        dbUserPassword = dssProperties.getDataSource().getRssDbPassword();
        jdbcUrl = dssServerUI.getJdbcUrl(dataBaseName);
        if (jdbcUrl == null) {
            throw new RuntimeException(dataBaseName +
                                       " Database Not found to connect");
        }


    }

    @Test(priority = 1)
    public void testConnectionUnSuccessful() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");

        log.info("Driver Class : " + driverClass);
        log.info("JDBC URL : " + jdbcUrl);
        log.info("User : " + dbUserName);

        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).
                sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys("12345");
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Test Connection".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(2000);
        Assert.assertNotEquals(driver.findElement(By.id("login")).findElement(By.className("error")).getText(), "Test successful",
                               "Connection failed message mismatched");
    }

    @Test(priority = 2)
    public void testConnectionSuccessful() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");


        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).
                sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys(dbUserPassword);
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Test Connection".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(2000);

        Assert.assertEquals(driver.findElement(By.id("login")).findElement(By.className("error")).getText(), "Test successful",
                            "Test Connection Failed");
    }

    @Test(priority = 3)
    public void explorerDatabaseByTool() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");

        log.info("Driver Class : " + driverClass);
        log.info("JDBC URL : " + jdbcUrl);
        log.info("User : " + dbUserName);

        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys(dbUserPassword);
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Connect".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(3000);
        driver.switchTo().frame("h2query");
        Assert.assertEquals(driver.findElement(By.xpath("/html/body/form/input")).getAttribute("value"),
                            "Run (Ctrl+Enter)", "Database Console page not found. Run (Ctrl+Enter) " +
                                                "button not found in Database Console.");
//        driver.findElement(By.xpath("/html/body/form/table/tbody/tr/td/a/img")).click();
        driver.switchTo().defaultContent();

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
