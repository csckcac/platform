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
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class DSSExploreDatabaseSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSExploreDatabaseSeleniumTest.class);
    private WebDriver driver;
    private Selenium selenium;
    String userName;
    String password;
    String domain;
    long sleeptime = 4000;
    long sleeptime1 = 6000;

    private String privilegeGroupName = "testautomation";
    private FrameworkProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private String dataBaseName = dssProperties.getDataSource().getDbName();
    private String dbUserName = dssProperties.getDataSource().getRssDbUser();
    private String dbUserPassword = dssProperties.getDataSource().getRssDbPassword();

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
        driver.get(baseUrl);
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        new StratosUserLogin().userLogin(driver, selenium, userName, password, "dss");
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
        String dbScriptFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                                  "DSS" + File.separator + "sql" + File.separator + "MySql" + File.separator +
                                  "selenium_Company.sql";
        String sqlScript = getDatafromFile(dbScriptFilePath);
        exploreDatabase(sqlScript, dbUserPassword);
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
                Thread.sleep(sleeptime);
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
            Thread.sleep(sleeptime);
            assertTrue(selenium.isTextPresent("Privilege group has been successfully removed"),
                       "Privilege Group delete Verification Pop-up Failed :");
            selenium.click("//button");
            Thread.sleep(sleeptime);
        }

    }


    private void exploreDatabase(String sqlScript, String dbUserPassword)
            throws InterruptedException {
        driver.findElement(By.id("dbUserTable")).findElement(By.linkText("Explore Database")).click();
        Thread.sleep(sleeptime);
        Assert.assertTrue(driver.getPageSource().contains("Database Console"), "Database Console page not found");
        selenium.selectFrame("inlineframe");
        driver.findElement(By.name("password")).sendKeys(dbUserPassword);
        driver.findElement(By.xpath("//tr[10]/td[2]/input")).click();
        Thread.sleep(sleeptime);

        driver.switchTo().frame("h2query");
        waitTimeforElement("//input");
        driver.findElement(By.id("sql")).sendKeys(sqlScript);
        driver.findElement(By.xpath("/html/body/form/input")).click();
        Thread.sleep(sleeptime1);
        driver.findElement(By.xpath("/html/body/form/input[2]")).click();
        driver.findElement(By.xpath("/html/body/form/input")).click();
        driver.findElement(By.id("sql")).sendKeys("SELECT * FROM Employee");
        driver.findElement(By.xpath("/html/body/form/input")).click();

        Thread.sleep(sleeptime);
        driver.switchTo().defaultContent();
        driver.switchTo().frame("page1").switchTo().frame("h2result");
        Assert.assertTrue(driver.findElement(By.id("output")).getText().contains("Perera"), "SELECT Query Failed. Expected out put not found");
        Assert.assertTrue(driver.findElement(By.id("output")).getText().contains("Liyanage"), "SELECT Query Failed. Expected out put not found");
        Assert.assertTrue(driver.findElement(By.id("output")).getText().contains("Amarasiri"), "SELECT Query Failed. Expected out put not found");


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
        driver.findElement(By.linkText("Privilege Groups")).click();
        Thread.sleep(sleeptime);
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
        return xmlString;
    }
}
