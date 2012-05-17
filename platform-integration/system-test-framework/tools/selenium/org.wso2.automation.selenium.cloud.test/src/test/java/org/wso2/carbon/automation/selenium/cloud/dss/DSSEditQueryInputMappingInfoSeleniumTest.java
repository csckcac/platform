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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
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
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/*https://wso2.org/jira/browse/CARBON-10972*/
public class DSSEditQueryInputMappingInfoSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSEditQueryInputMappingInfoSeleniumTest.class);
    private WebDriver driver;
    private DSSServerUIUtils dssServerUi;
    private UserInfo userDetails;

    private String dataServiceName = "MySqlRSSDataServiceTest";
    private String privilegeGroupName = "testAT";
    private FrameworkProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private String dataBaseName = dssProperties.getDataSource().getDbName();
    private String dbUserName = dssProperties.getDataSource().getRssDbUser();
    private String dbUserPassword = dssProperties.getDataSource().getRssDbPassword();

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUi = new DSSServerUIUtils(driver);
        dssServerUi.login(userDetails.getUserName(), userDetails.getPassword());
        dssServerUi.deleteServiceIfExists(dataServiceName);
        dssServerUi.dropDatabaseIfExist(dataBaseName + "_" + userDetails.getDomain().replace('.', '_'));
        dssServerUi.deletePrivilegeGroupIfExists(privilegeGroupName);
    }

    @Test(priority = 0)
    public void addPrivilegeGroup() throws InterruptedException {
        dssServerUi.addPrivilegeGroup(privilegeGroupName);
    }

    @Test(priority = 1, dependsOnMethods = {"addPrivilegeGroup"})
    public void createDataBase() throws InterruptedException {
        dssServerUi.createDatabase(dataBaseName);
        dataBaseName = dataBaseName + "_" + userDetails.getDomain().replace('.', '_');
    }

    @Test(priority = 2, dependsOnMethods = {"createDataBase"})
    public void addDatabaseUser() throws InterruptedException {
        dssServerUi.createDataBaseUser(privilegeGroupName, dbUserName, dbUserPassword, dataBaseName);
        dbUserName = dssServerUi.getFullyQualifiedUsername(dbUserName, userDetails.getDomain());
    }

    @Test(priority = 3, dependsOnMethods = {"addDatabaseUser"})
    public void exploreDatabase() throws IOException, InterruptedException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String dbScriptCreateTableFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                                             "DSS" + File.separator + "sql" + File.separator + "MySql" + File.separator +
                                             "CreateTables.sql";
        String dbScriptInsertFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                                        "DSS" + File.separator + "sql" + File.separator + "MySql" + File.separator +
                                        "Customers.sql";
        String sqlScript = FileManager.readFile(dbScriptCreateTableFilePath);
        sqlScript = sqlScript + ";" + FileManager.readFile(dbScriptInsertFilePath);
        dssServerUi.exploreDatabase(sqlScript, dbUserPassword, dataBaseName);
    }

    @Test(priority = 4, dependsOnMethods = {"exploreDatabase"})
    public void uploadServiceFile() throws XMLStreamException, IOException, InterruptedException {
        String dbsFilePath = ProductConstant.getResourceLocations(ProductConstant.DSS_SERVER_NAME) + File.separator +
                             "dbs" + File.separator + "rdbms" + File.separator + "MySql" + File.separator
                             + "MySqlRSSDataServiceTest.dbs";
        String outPutFilePath;
        outPutFilePath = createArtifact(dbsFilePath, dssServerUi.getJdbcUrl(dataBaseName));
        driver.findElement(By.linkText("Upload")).click();
        driver.findElement(By.name("dbsFilename")).sendKeys(outPutFilePath);
        driver.findElement(By.name("upload")).click();
        assertEquals(driver.findElement(By.id("messagebox-info")).getText(),
                     "Data Service configuration file uploaded successfully.", "Uploading failed.");
        driver.findElement(By.xpath("//div[2]/button")).click();
        Thread.sleep(2000);
        File tmpFile = new File(outPutFilePath);
        tmpFile.delete();

    }

    @Test(priority = 5, dependsOnMethods = {"uploadServiceFile"}, timeOut = 1000 * 60 * 2)
    public void serviceDeployment() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            if(dssServerUi.isServiceDeployed(dataServiceName)){
                break;
            }
            Thread.sleep(3000);

        }
        assertTrue(driver.findElement(By.id("sgTable")).getText().contains(dataServiceName), "Service Name not fount in service list");
    }

    @Test(priority = 6, dependsOnMethods = {"serviceDeployment"})
    public void editQueryInputMappingInfo() throws InterruptedException {

        dssServerUi.goToServiceInfo(dataServiceName);
        driver.findElement(By.linkText("Edit Data Service (Wizard)")).click();
        Iterator<WebElement> buttons = driver.findElement(By.id("dataSources")).findElement(By.tagName("tbody")).findElements(By.className("button")).iterator();
        while (buttons.hasNext()) {
            WebElement button = buttons.next();
            if (button.getAttribute("value").equalsIgnoreCase("Next >")) {
                button.click();
                break;
            }
        }
        Assert.assertTrue(driver.findElement(By.id("middle")).findElement(By.tagName("h2")).
                getText().contains("Data Sources"), "Edit DataSource page not found");
        buttons = driver.findElement(By.id("datasource-table")).findElement(By.tagName("tbody")).findElements(By.className("button")).iterator();
        while (buttons.hasNext()) {
            WebElement button = buttons.next();
            if (button.getAttribute("value").equalsIgnoreCase("Next >")) {
                button.click();
                break;
            }
        }
        Iterator<WebElement> queryItr = driver.findElement(By.id("query-table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).iterator();
        while (queryItr.hasNext()) {
            WebElement tableRow = queryItr.next();
            if (tableRow.findElements(By.tagName("td")).get(0).getText().equalsIgnoreCase("customersInBostonSQL")) {
                tableRow.findElements(By.tagName("td")).get(2).findElement(By.linkText("Edit Query")).click();
                break;
            }
        }
        Thread.sleep(2000);
        Assert.assertEquals(driver.findElement(By.id("existingInputMappingsTable")).
                findElement(By.tagName("label")).getText(),
                            "Currently there are no input mappings present for this query",
                            "Input Mapping message mismatched when there is no Input values");

        buttons = driver.findElement(By.id("addQuery")).findElement(By.tagName("tbody")).findElements(By.className("button")).iterator();
        while (buttons.hasNext()) {
            WebElement button = buttons.next();
            if (button.getAttribute("value").equalsIgnoreCase("Cancel")) {
                button.click();
                break;
            }
        }

        queryItr = driver.findElement(By.id("query-table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).iterator();
        while (queryItr.hasNext()) {
            WebElement tableRow = queryItr.next();
            if (tableRow.findElements(By.tagName("td")).get(0).getText().equalsIgnoreCase("employeesByNumberSQL")) {
                tableRow.findElements(By.tagName("td")).get(2).findElement(By.linkText("Edit Query")).click();
                break;
            }
        }

        Thread.sleep(2000);
        List<WebElement> mappingHeaders = driver.findElement(By.id("existingInputMappingsTable")).
                findElements(By.tagName("tr")).get(0).findElements(By.tagName("td"));
        Assert.assertEquals("Mapping Name", mappingHeaders.get(0).getText(), "Header Mapping Name Not found in Input mapping Table");
        Assert.assertEquals("Parameter Type", mappingHeaders.get(1).getText(), "Header Parameter Type Not found in Input mapping Table");
        Assert.assertEquals("Type", mappingHeaders.get(2).getText(), "Header Type Not found in Input mapping Table");
        Assert.assertEquals("Action", mappingHeaders.get(3).getText(), "Header Action Not found in Input mapping Table");

        buttons = driver.findElement(By.id("addQuery")).findElement(By.tagName("tbody")).findElements(By.className("button")).iterator();
        while (buttons.hasNext()) {
            WebElement button = buttons.next();
            if (button.getAttribute("value").equalsIgnoreCase("Cancel")) {
                button.click();
                break;
            }
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        try {
            dssServerUi.logOut();

        } finally {
            driver.quit();
        }

    }

    private String createArtifact(String dbsFilePath, String jdbcUrl)
            throws XMLStreamException, IOException {
        OMElement dbsFile = AXIOMUtil.stringToOM(FileManager.readFile(dbsFilePath));
        OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
        Iterator configElement1 = dbsConfig.getChildElements();
        while (configElement1.hasNext()) {
            OMElement property = (OMElement) configElement1.next();
            String value = property.getAttributeValue(new QName("name"));
            if ("org.wso2.ws.dataservice.protocol".equals(value)) {
                property.setText(jdbcUrl);

            } else if ("org.wso2.ws.dataservice.user".equals(value)) {
                property.setText(dbUserName);

            } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                property.setText(dbUserPassword);
            }
        }
        log.debug(dbsFile);
        String outputFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                                File.separator + dataServiceName + ".dbs";
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false));
        writer.write(dbsFile.toString());
        writer.newLine();
        writer.flush();
        writer.close();
        return outputFilePath;

    }
}
