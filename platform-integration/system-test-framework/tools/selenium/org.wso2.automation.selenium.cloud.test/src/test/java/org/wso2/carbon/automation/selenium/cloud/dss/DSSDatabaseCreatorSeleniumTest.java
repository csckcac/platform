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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertTrue;


public class DSSDatabaseCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSDatabaseCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "dss";
    String userName;
    String password;
    String domain;
    long sleeptime = 4000;
    long sleeptime1 = 6000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(10);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.manager"}, description = "add a new privilege group", priority = 1)
    public void testAddPrivilegeGroup() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String dbScriptFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                                  "DSS"+File.separator+"sql"+File.separator+"MySql"+File.separator+
                                  "selenium_Company.sql";
        String sqlScript = getDatafromFile(dbScriptFilePath);
        String privilegeGroupName = "testautomation";
        String dataBaseName = "company";
        String dbUserName = "dbuser";
        String dbUserPassword = "test";
        String baseUrlData = "https://data.stratoslive.wso2.com";
        String dataSourceUrl = baseUrlData + "/t/" + domain + "/carbon/datasource/index.jsp?region=" +
                               "region1&item=datasource_menu";

        String serviceCreateUrl = baseUrlData + "/t/" + domain + "/carbon/ds/serviceDetails.jsp" +
                                  "?region=region1&item=ds_create_menu";
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos Data Login Success");
            addPrivilegeGroup(privilegeGroupName);
            createDatabase(dataBaseName);
            createDataBaseUser(privilegeGroupName, dbUserName, dbUserPassword);
            exploreDatabase(sqlScript, dbUserPassword);
            createCarbonDataSource();
            String dataSourceID = getCarbonDataSourceID(dataSourceUrl);
            addDataSource(serviceCreateUrl, dataSourceID);
            addNewQuery();
            addNewOperation();
            log.info("Service was Created Successfully Refreshing page .....");
            refreshPage("//td[2]/nobr/a");
            waitTimeforElement("//td[2]/nobr/a");  // till list dashboard appears...
            assertTrue(driver.getPageSource().contains("company"), "Failed to Create Data Service :");
            deleteDataService();
            deleteDatabase();
            deletePrivilegeGroup();
            userLogout();
            log.info("**************Stratos DSS - Create Data Service Test - Passed  ************");
        } catch (AssertionFailedError e) {
            log.info("Create Data Service Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Create Data Service Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Create Data Service Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Create Data Service Test Failed:" + e.getMessage());
        } catch (Exception e) {
            log.info("Create Data Service Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Create Data Service Test Failed :" + e.getMessage());
        }
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }


    private void deletePrivilegeGroup() throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to remove privilege group?"),
                   "Failed to Display Warning message going to delete Privilege group  :");
        selenium.click("//button");
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Privilege group has been successfully removed"),
                   "Failed to Delete Priviledge group :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void deleteDatabase() throws InterruptedException {
        // delete database & user
        driver.findElement(By.linkText("Databases")).click();
        waitTimeforElement("//td[5]/a");
        driver.findElement(By.linkText("Manage")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.linkText("Drop")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to drop the user?"),
                   "Failed to Delete DB User :");
        selenium.click("//button");
        log.info("Deleted Database User");
        // go back to delete db now
        waitTimeforElement("//input");
        driver.findElement(By.xpath("//input")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.linkText("Drop")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to drop the database?"),
                   "Failed to remove DB");
        selenium.click("//button");
        Thread.sleep(sleeptime);
        log.info("Deleted Database");
    }

    private void deleteDataService() throws InterruptedException {
        waitTimeforElement("//td/input");
        driver.findElement(By.name("serviceGroups")).click();
        driver.findElement(By.id("delete1")).click();
        waitTimeforElement("//div[3]/div/div");
        selenium.click("//button");
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Successfully deleted selected service groups"),
                   "Failed to upload Data Service File :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
        log.info("Deleted Data Service");
    }

    private void addNewOperation() throws InterruptedException {
        driver.findElement(By.linkText("Add New Operation")).click();
        waitTimeforElement("//td[2]/input");
        driver.findElement(By.id("operationName")).sendKeys("getEmployee");
        Select selectQuery = new Select(driver.findElement(By.id("queryId")));
        selectQuery.selectByVisibleText("get_employee");      //query name ...??
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        waitTimeforElement("//input[3]");
        driver.findElement(By.xpath("//input[3]")).click();
    }

    private void addNewQuery() throws InterruptedException {
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        driver.findElement(By.linkText("Add New Query")).click();
        waitTimeforElement("//td[2]/input");
        driver.findElement(By.id("queryId")).sendKeys("get_employee");    //query name
        Select dataSource = new Select(driver.findElement(By.id("datasource")));
        dataSource.selectByVisibleText("test");  //datasource name
        waitTimeforElement("//textarea");
        driver.findElement(By.id("sql")).sendKeys("SELECT ID, LName FROM Employee");   //sql query
        driver.findElement(By.id("addAutoResponse")).click();
        waitTimeforElement("//tr[20]/td/table/tbody/tr/td");
        driver.findElement(By.linkText("Edit")).click();
        waitTimeforElement("//div[3]/table/tbody/tr/td/table/tbody/tr[2]/td[2]/input");
        driver.findElement(By.id("txtDataServiceOMElementName")).clear();
        driver.findElement(By.id("txtDataServiceOMElementName")).sendKeys("employeeID");
        driver.findElement(By.xpath("//tr[5]/td/input[2]")).click();
        waitTimeforElement("//tr[2]/td[7]/a");
        driver.findElement(By.xpath("//tr[2]/td[7]/a")).click();
        waitTimeforElement("//div[3]/table/tbody/tr/td/table/tbody/tr[2]/td[2]/input");
        driver.findElement(By.id("txtDataServiceOMElementName")).clear();
        driver.findElement(By.id("txtDataServiceOMElementName")).sendKeys("lastName");
        driver.findElement(By.xpath("//tr[5]/td/input[2]")).click();
        //click on button to goto main cofiguration
        waitTimeforElement("//form/table/tbody/tr[5]/td/input");
        driver.findElement(By.xpath("//form/table/tbody/tr[5]/td/input")).click();
        waitTimeforElement("//tr[25]/td/input");
        driver.findElement(By.xpath("//tr[25]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.xpath("//input[2]")).click(); // waiting till create operation page loads
        waitTimeforElement("//tr[2]/td/a");
    }

    private void addDataSource(String serviceCreateUrl, String dataSourceID)
            throws InterruptedException {
        driver.get(serviceCreateUrl);
        waitTimeforElement("//input");
        driver.findElement(By.id("serviceName")).sendKeys("company");
        driver.findElement(By.xpath("//tr[5]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        driver.findElement(By.linkText("Add New Data Source")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("datasourceId")).sendKeys("test");  //datasource name
        Select selectDataSource = new Select(driver.findElement(By.id("datasourceType")));
        selectDataSource.selectByVisibleText("Carbon Data Source");
        waitTimeforElement("//tr[3]/td[2]/select");
        Select selectCarbonSource = new Select(driver.findElement(By.id("carbon_datasource_name")));
        selectCarbonSource.selectByVisibleText(dataSourceID);
        driver.findElement(By.name("save_button")).click();
        waitTimeforElement("//input[2]");
    }

    private String getCarbonDataSourceID(String dataSourceUrl) throws InterruptedException {
        driver.get(dataSourceUrl);
        waitTimeforElement("//div/div/table/tbody/tr/td");
        String dataSourceID = selenium.getText("//div/div/table/tbody/tr/td");
        log.info("Carbon Data Source ID is : " + dataSourceID);
        return dataSourceID;
    }

    private void createCarbonDataSource() throws InterruptedException {
        driver.findElement(By.linkText("Databases")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.linkText("Manage")).click();
        waitTimeforElement("//form/table/tbody/tr/td");
        driver.findElement(By.linkText("Create Datasource")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Carbon datasource has been successfully created. " +
                                          "Please update the password field before using the " +
                                          "Carbon datasource."), "Failed Carbon Source Creation :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void exploreDatabase(String sqlScript, String dbUserPassword)
            throws InterruptedException {
        driver.findElement(By.linkText("Explore Database")).click();
        Thread.sleep(sleeptime);
        selenium.selectFrame("inlineframe");
        driver.findElement(By.name("password")).sendKeys(dbUserPassword);
        driver.findElement(By.xpath("//tr[10]/td[2]/input")).click();
        Thread.sleep(sleeptime);
        selenium.waitForPageToLoad("30000");
        selenium.selectFrame("h2query");
        waitTimeforElement("//input");
        driver.findElement(By.id("sql")).sendKeys(sqlScript);
        selenium.click("//input");
        Thread.sleep(sleeptime1);
        driver.switchTo().defaultContent();
    }

    private void createDataBaseUser(String privilegeGroupName, String dbUserName,
                                    String dbUserPassword) throws InterruptedException {
        driver.findElement(By.linkText("Manage")).click();
        //wait for add new user page
        waitTimeforElement("//form/table/tbody/tr/td/a");
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
        waitTimeforElement("//form/table/tbody/tr/td");
        String dbuserName = selenium.getText("//form/table/tbody/tr/td");
        log.info("Database User Name is : " + dbuserName);
    }

    private void createDatabase(String dataBaseName) throws InterruptedException {
        driver.findElement(By.linkText("Databases")).click();
        Thread.sleep(sleeptime);
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
        waitTimeforElement("//li[4]/ul/li/a");
        driver.findElement(By.linkText("Privilege Groups")).click();
        Thread.sleep(sleeptime);
        driver.findElement(By.linkText("Add new privilege group")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("privGroupName")).sendKeys(groupName);
        driver.findElement(By.id("selectAll")).click();
        driver.findElement(By.xpath("//td[3]/table/tbody/tr[3]/td/input")).click();
        waitTimeforElement("//div[4]/div/div");
        assertTrue(selenium.isTextPresent("Privilege group has been successfully created"),
                   "Privilege Group Pop-up Failed :");
        selenium.click("//button");
        waitTimeforElement("//li[4]/ul/li/a");
    }

    private void refreshPage(String elementName) throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        long time;
        boolean element = false;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()))
               < 120 * 1000) {
            if (selenium.isElementPresent(elementName)) {
                element = true;
                break;
            }
            Thread.sleep(sleeptime);
            driver.navigate().refresh();
            log.info("waiting for element :" + elementName);
        }
        assertTrue(element, "Element Not Found within 2 minutes :");

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


    private String getDatafromFile(String dbFilePath) throws IOException {
        File xmlFile = new File(dbFilePath);
        BufferedReader bufferedReader = null;
        String xmlString = "";

        try {
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader(xmlFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                //Process the dss, here we just print it out
                xmlString += line + "\n";
            }

        } catch (IOException ex) {
            throw new IOException(ex);
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                throw new IOException(ex);
            }
        }
        log.info("Request xml file content is :" + "\n" + xmlString);

        return xmlString;

    }
}