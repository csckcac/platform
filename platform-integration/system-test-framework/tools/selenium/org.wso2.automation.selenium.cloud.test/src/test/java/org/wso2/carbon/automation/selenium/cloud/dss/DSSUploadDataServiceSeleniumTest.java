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

import com.thoughtworks.selenium.Selenium;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.dssProperties;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DSSUploadDataServiceSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSUploadDataServiceSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    private String productName = "dss";
    private String jdbcUrl;
    private String domain;
    long sleepTime = 4000;


    private String dataServiceName = "MySqlRSSDataServiceTest";
    private String privilegeGroupName = "testAT";
    private dssProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private String dataBaseName = dssProperties.getDataSource().getDbName();
    private String dbUserName = dssProperties.getDataSource().getRssDbUser();
    private String dbUserPassword = dssProperties.getDataSource().getRssDbPassword();

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(10);
        domain = userDetails.getDomain();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        new StratosUserLogin().userLogin(driver, selenium, userDetails.getUserName(),
                                         userDetails.getPassword(), productName);
        setPreConditions();
    }

    @Test(priority = 0)
    public void addPrivilegeGroup() throws InterruptedException {
        addPrivilegeGroup(privilegeGroupName);
    }

    @Test(priority = 1, dependsOnMethods = {"addPrivilegeGroup"})
    public void createDataBase() throws InterruptedException {
        createDatabase(dataBaseName);
    }

    @Test(priority = 2, dependsOnMethods = {"createDataBase"})
    public void addDatabaseUser() throws InterruptedException {
        createDataBaseUser(privilegeGroupName, dbUserName, dbUserPassword);
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
        String sqlScript = getDatafromFile(dbScriptCreateTableFilePath);
        sqlScript = sqlScript + ";" + getDatafromFile(dbScriptInsertFilePath);
        exploreDatabase(sqlScript, dbUserPassword);
    }

    @Test(priority = 4, dependsOnMethods = {"exploreDatabase"})
    public void uploadServiceFile() throws XMLStreamException, IOException, InterruptedException {
        String dbsFilePath = ProductConstant.getResourceLocations(ProductConstant.DSS_SERVER_NAME) + File.separator +
                             "dbs" + File.separator + "rdbms" + File.separator + "MySql" + File.separator
                             + "MySqlRSSDataServiceTest.dbs";
        String outPutFilePath;
        driver.findElement(By.linkText("Upload")).click();
        outPutFilePath = createArtifact(dbsFilePath);
        driver.findElement(By.name("dbsFilename")).sendKeys(outPutFilePath);
        driver.findElement(By.name("upload")).click();
        assertEquals(driver.findElement(By.id("messagebox-info")).getText(),
                     "Data Service configuration file uploaded successfully.", "Uploading failed.");
        driver.findElement(By.xpath("//div[2]/button")).click();
        Thread.sleep(2000);
        File tmpFile = new File(outPutFilePath);
        tmpFile.delete();

    }

    @Test(priority = 5, dependsOnMethods = {"uploadServiceFile"})
    public void serviceDeployment() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            driver.findElement(By.linkText("List")).click();
            if (driver.findElement(By.id("sgTable")).getText().contains(dataServiceName)) {
                break;
            }
            Thread.sleep(3000);

        }
        assertTrue(driver.findElement(By.id("sgTable")).getText().contains(dataServiceName), "Service Name not fount in service list");
    }

    @Test(priority = 6, dependsOnMethods = {"serviceDeployment"})
    public void serviceInvocation() throws AxisFault {
        String serviceEndPoint = dssProperties.getProductVariables().getBackendUrl()
                                 + "t/" + domain + "/" + dataServiceName;
        for (int i = 0; i < 5; i++) {
            getCustomerInBoston(serviceEndPoint);
        }
        log.info("Select Operation Success");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        try {
            userLogout();
            
        } finally {
            driver.quit();
        }

    }

    private void setPreConditions() throws InterruptedException {
        driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr/td/div/ul/li[5]/ul/li[2]/ul/li/a")).click();
        if (driver.findElements(By.id("sgTable")).size() > 0) {
            if (driver.findElement(By.id("sgTable")).getText().contains(dataServiceName)) {
                deleteDataService();
            }
        }

        driver.findElement(By.linkText("Databases")).click();
        if (driver.findElement(By.id("database_table")).getText().contains(dataBaseName)) {
            deleteDatabaseIfExist();
        }

        deletePrivilegeGroupIfExists();

    }

    private void deleteDatabaseIfExist() throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElements(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr"));
        for (int j = 0; j < databaseList.size(); j++) {

            if (driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]/td[1]")).getText().contains(dataBaseName + "_" + domain.replace('.', '_'))) {
                driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]")).findElement(By.linkText("Manage")).click();
                if (driver.findElements(By.id("dbUserTable")).size() > 0) {
                    List<WebElement> userList = driver.findElements(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr"));

                    for (int i = 0; i < userList.size(); i++) {
                        if (driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr")).
                                getText().contains("Drop")) {
                            driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr")).findElement(By.linkText("Drop")).click();
                            assertTrue(selenium.isTextPresent("exact:Do you want to drop the user?"),
                                       "Failed to Delete DB User :");
                            selenium.click("//button");
                        }


                    }
                }
                driver.findElement(By.linkText("Databases")).click();

                driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]")).findElement(By.linkText("Drop")).click();
                assertTrue(selenium.isTextPresent("exact:Do you want to drop the database?"),
                           "Failed to remove DB");
                selenium.click("//button");
                Thread.sleep(sleepTime);
                log.info("Deleted Database");
                break;
            }

        }


    }

    private void deletePrivilegeGroupIfExists() throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        if (driver.findElement(By.id("privilegeGroupTable")).getText().contains(privilegeGroupName)) {
            driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_" + privilegeGroupName)).findElement(By.linkText("Delete")).click();
            assertTrue(selenium.isTextPresent("Do you want to remove privilege group?"),
                       "Privilege Group delete Pop-up Failed :");
            selenium.click("//button");
            Thread.sleep(sleepTime);
            assertTrue(selenium.isTextPresent("Privilege group has been successfully removed"),
                       "Privilege Group delete Verification Pop-up Failed :");
            selenium.click("//button");
            Thread.sleep(sleepTime);
        }


    }


    private void deleteDataService() throws InterruptedException {
        driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr/td/div/ul/li[5]/ul/li[2]/ul/li/a")).click();
        List<WebElement> tr;
        tr = driver.findElement(By.id("sgTable")).findElements(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form[2]/table/tbody/tr"));
        Iterator<WebElement> it = tr.iterator();
        while (it.hasNext()) {
            WebElement service = it.next();
            if (service.getText().contains(dataServiceName)) {
                service.findElement(By.name("serviceGroups")).click();
                driver.findElement(By.id("delete1")).click();
                waitTimeforElement("//div[3]/div/div");
                selenium.click("//button");
                waitTimeforElement("//div[3]/div/div");
                assertTrue(selenium.isTextPresent("Successfully deleted selected service groups"),
                           "Failed to upload Data Service File :");
                selenium.click("//button");
                Thread.sleep(sleepTime);
                break;
            }


        }

    }


    private void exploreDatabase(String sqlScript, String dbUserPassword)
            throws InterruptedException {
        driver.findElement(By.id("dbUserTable")).findElement(By.linkText("Explore Database")).click();
        Thread.sleep(sleepTime);
        selenium.selectFrame("inlineframe");
        jdbcUrl = driver.findElement(By.xpath("//form/table/tbody/tr[7]/td[2]/input")).getAttribute("value");
        dbUserName = driver.findElement(By.xpath("//form/table/tbody/tr[8]/td[2]/input")).getAttribute("value");

        driver.findElement(By.name("password")).sendKeys(dbUserPassword);
        driver.findElement(By.xpath("//tr[10]/td[2]/input")).click();
        Thread.sleep(sleepTime);
        selenium.waitForPageToLoad("30000");
        selenium.selectFrame("h2query");
        waitTimeforElement("//input");

        driver.findElement(By.id("sql")).sendKeys(sqlScript);
        selenium.click("//input");
        Thread.sleep(sleepTime);
        driver.findElement(By.id("sql")).sendKeys("");

        driver.switchTo().defaultContent();
    }

    private void createDataBaseUser(String privilegeGroupName, String dbUserName,
                                    String dbUserPassword) throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElements(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr"));
        for (int j = 0; j < databaseList.size(); j++) {

            if (driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]/td[1]")).getText().contains(dataBaseName + "_" + domain.replace('.', '_'))) {
                driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]")).findElement(By.linkText("Manage")).click();


                driver.findElement(By.linkText("Add New User")).click();
                driver.findElement(By.id("username")).sendKeys(dbUserName);
                driver.findElement(By.id("password")).sendKeys(dbUserPassword);
                driver.findElement(By.id("repeatPassword")).sendKeys(dbUserPassword);
                Select selectgroup = new Select(driver.findElement(By.id("privGroupList")));
                selectgroup.selectByVisibleText(privilegeGroupName);
                driver.findElement(By.xpath("//tr[5]/td/input")).click();
                waitTimeforElement("//div[3]/div/div");
                assertTrue(selenium.isTextPresent("User has been successfully created"),
                           "Database User creation pop-up message Failed:");
                selenium.click("//button");
                String dbuserName = selenium.getText("//form/table/tbody/tr/td");
                log.info("Database User Name is : " + dbuserName);
                break;
            }
        }
    }

    private void createDatabase(String dataBaseName) throws InterruptedException {
        driver.findElement(By.linkText("Databases")).click();
        Thread.sleep(sleepTime);
        driver.findElement(By.linkText("Add Database")).click();
        waitTimeforElement("//input");
        assertTrue(driver.getPageSource().contains("New Database"),
                   "Failed to display Add New Database page :");
        Select select = new Select(driver.findElement(By.id("instances")));
        select.selectByVisibleText("WSO2_RSS");
        driver.findElement(By.id("dbName")).sendKeys(dataBaseName);
        driver.findElement(By.xpath("//form/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Database has been successfully created"),
                   "Database creation pop-up message Failed:");
        selenium.click("//button");
        waitTimeforElement("//form/table/tbody/tr/td");
        String dbName = selenium.getText("//form/table/tbody/tr/td");
        log.info("Database Name is :" + dbName);
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }

    private void addPrivilegeGroup(String groupName) throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        Thread.sleep(sleepTime);
        driver.findElement(By.linkText("Add new privilege group")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("privGroupName")).sendKeys(groupName);
        driver.findElement(By.id("selectAll")).click();
        driver.findElement(By.xpath("//td[3]/table/tbody/tr[3]/td/input")).click();
        waitTimeforElement("//div[4]/div/div");

        assertTrue(selenium.isTextPresent("Privilege group has been successfully created"),
                   "Privilege Group Pop-up message mismatched:");
        selenium.click("//button");
        waitTimeforElement("//li[4]/ul/li/a");
    }

    /*private void refreshPage(String elementName) throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        long time;
        boolean element = false;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()))
               < 120 * 1000) {
            if (selenium.isElementPresent(elementName)) {
                element = true;
                break;
            }
            Thread.sleep(sleepTime);
            driver.navigate().refresh();
            log.info("waiting for element :" + elementName);
        }
        assertTrue(element, "Element Not Found within 2 minutes :");

    }*/

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

    public String createArtifact(String dbsFilePath)
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

    private void getCustomerInBoston(String serviceEndPoint) throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "customersInBoston");
        Assert.assertTrue(result.toString().contains("<city>Boston</city>"), "Expected Result Mismatched");

    }

    private String getDatafromFile(String dbFilePath) throws IOException {
        return FileManager.readFile(dbFilePath);
    }

}