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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;
/* This Class tests data service generation wizard */
public class GenerateDataServiceSeleniumTest {

    private static final Log log = LogFactory.getLog(DSSEditQueryInputMappingInfoSeleniumTest.class);
    private WebDriver driver;
    private DSSServerUIUtils dssServerUI;
    private UserInfo userDetails;

    private String dataServiceName = "generatedDataService";
    private String privilegeGroupName = "testAT";
    private FrameworkProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private String dataBaseName = dssProperties.getDataSource().getDbName();
    private String dbUserName = dssProperties.getDataSource().getRssDbUser();
    private String dbUserPassword = dssProperties.getDataSource().getRssDbPassword();
    private String carbonDataSourceName;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUI = new DSSServerUIUtils(driver);
        dssServerUI.login(userDetails.getUserName(), userDetails.getPassword());
        dssServerUI.deleteServiceIfExists(dataServiceName);
        dssServerUI.dropDatabaseIfExist(dataBaseName + "_" + userDetails.getDomain().replace('.', '_'));
        dssServerUI.deletePrivilegeGroupIfExists(privilegeGroupName);
    }

    @Test(priority = 0)
    public void addPrivilegeGroup() throws InterruptedException {
        dssServerUI.addPrivilegeGroup(privilegeGroupName);
    }

    @Test(priority = 1, dependsOnMethods = {"addPrivilegeGroup"})
    public void createDataBase() throws InterruptedException {
        dssServerUI.createDatabase(dataBaseName);
        dataBaseName = dataBaseName + "_" + userDetails.getDomain().replace('.', '_');
    }

    @Test(priority = 2, dependsOnMethods = {"createDataBase"})
    public void addDatabaseUser() throws InterruptedException {
        dssServerUI.createDataBaseUser(privilegeGroupName, dbUserName, dbUserPassword, dataBaseName);
        dbUserName = dssServerUI.getFullyQualifiedUsername(dbUserName, userDetails.getDomain());
    }

    @Test(priority = 3, dependsOnMethods = {"addDatabaseUser"})
    public void exploreDatabase() throws IOException, InterruptedException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String dbScriptCreateTableFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                                             "DSS" + File.separator + "sql" + File.separator + "MySql" + File.separator +
                                             "CreateTables.sql";
        String sqlScript = FileManager.readFile(dbScriptCreateTableFilePath);

        dssServerUI.exploreDatabase(sqlScript, dbUserPassword, dataBaseName);
    }

    @Test(priority = 4, dependsOnMethods = {"exploreDatabase"})
    public void generateCarbonDataSource() throws InterruptedException {
        dssServerUI.createCarbonDataSource(dataBaseName);
        List<String> cdsList = dssServerUI.getCarbonDataSourceList(dataBaseName);
        if (cdsList.size() > 0) {
            Collections.sort(cdsList);
            carbonDataSourceName = cdsList.get(cdsList.size() - 1);
            dssServerUI.editCarbonDataSourcePassword(carbonDataSourceName, dbUserPassword);
        } else {
            throw new RuntimeException("Carbon Data Source Not Found. framework error");
        }
    }

    @Test(priority = 5, dependsOnMethods = {"generateCarbonDataSource"})
    public void generateDataService() throws MalformedURLException, InterruptedException {

        dssServerUI.clickOnMenu();
        driver.findElement(By.linkText("Generate")).click();
        driver.findElement(By.id("dbName")).sendKeys(dataBaseName);
        Select dataSourceId = new Select(driver.findElement(By.id("datasource")));
        dataSourceId.selectByVisibleText(carbonDataSourceName);
        for (WebElement button : driver.findElement(By.id("workArea")).findElements(By.className("button"))) {
            if (button.getAttribute("value").equalsIgnoreCase("Next >")) {
                button.click();
                break;
            }
        }
        driver.findElement(By.id("content-table")).findElement(By.linkText("Select none")).click();
        Thread.sleep(1000);
        driver.findElement(By.id("content-table")).findElement(By.id("Employees")).click();
        for (WebElement button : driver.findElement(By.id("content-table")).findElements(By.className("button"))) {
            if (button.getAttribute("value").equalsIgnoreCase("Next >")) {
                button.click();
                Thread.sleep(1000);
                break;
            }
        }

        for (WebElement radio : driver.findElement(By.id("workArea")).findElements(By.id("mode"))) {
            if (radio.getAttribute("value").equalsIgnoreCase("Single")) {
                radio.click();
                Thread.sleep(1000);
                break;
            }
        }
        driver.findElement(By.id("workArea")).findElement(By.id("txtServiceName")).sendKeys(dataServiceName);
        Thread.sleep(1000);
        for (WebElement button : driver.findElements(By.className("button"))) {
            if (button.getAttribute("value").equalsIgnoreCase("Next >")) {
                button.click();
                Thread.sleep(1000);
                break;
            }
        }

        for (WebElement button : driver.findElements(By.className("button"))) {
            if (button.getAttribute("value").equalsIgnoreCase("Finish")) {
                button.click();
                break;
            }
        }


    }

    @Test(priority = 6, dependsOnMethods = {"generateDataService"})
    public void serviceDeployment() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            if (dssServerUI.isServiceDeployed(dataServiceName)) {
                break;
            }
            Thread.sleep(3000);

        }
        assertTrue(driver.findElement(By.id("sgTable")).getText().contains(dataServiceName), "Service Name not fount in service list");
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
