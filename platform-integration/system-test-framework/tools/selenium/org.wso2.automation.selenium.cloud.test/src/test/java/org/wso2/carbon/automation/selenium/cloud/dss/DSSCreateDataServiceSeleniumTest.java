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
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertTrue;


public class DSSCreateDataServiceSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSCreateDataServiceSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "dss";
    String userName;
    String password;
    String domain;
    long sleeptime = 4000;
    long sleeptime1 = 6000;

    private String dataServiceName = "companyDataService";
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
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
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

    @Test(priority = 4, dependsOnMethods = {"exploreDatabase"})
    public void createCarbonDataSourceTest() throws InterruptedException {
        createCarbonDataSource();
    }

    @Test(priority = 5, dependsOnMethods = {"createCarbonDataSourceTest"})
    public void addDataSource() throws Exception {
        String baseUrlData = "https://" + dssProperties.getProductVariables().getHostName();
        if (dssProperties.getEnvironmentSettings().isEnablePort()) {
            baseUrlData = baseUrlData + ":" + dssProperties.getProductVariables().getHttpsPort();
        }
        if (dssProperties.getEnvironmentSettings().isEnableCarbonWebContext()) {
            baseUrlData = baseUrlData + "/" + dssProperties.getProductVariables().getWebContextRoot();
        }
        String dataSourceUrl = baseUrlData + "/t/" + domain + "/carbon/datasource/index.jsp?region=" +
                               "region1&item=datasource_menu";
        String serviceCreateUrl = baseUrlData + "/t/" + domain + "/carbon/ds/serviceDetails.jsp" +
                                  "?region=region1&item=ds_create_menu";
        String dataSourceID = getCarbonDataSourceID(dataSourceUrl);
        addDataSource(serviceCreateUrl, dataSourceID);
    }

    @Test(priority = 6, dependsOnMethods = {"addDataSource"})
    public void addQuery() throws InterruptedException {
        addNewQuery();
    }

    @Test(priority = 7, dependsOnMethods = {"addQuery"})
    public void addOperation() throws InterruptedException {
        addNewOperation();
    }

    @Test(priority = 8, dependsOnMethods = {"addOperation"})
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

    @Test(priority = 8, dependsOnMethods = {"serviceDeployment"})
    public void serviceInvocation() throws AxisFault {
        String serviceEndPoint = dssProperties.getProductVariables().getBackendUrl()
                                 + "t/" + domain + "/" + dataServiceName;
        AxisServiceClient serviceClient = new AxisServiceClient();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getEmployee", omNs);
        for (int i = 0; i < 5; i++) {
            OMElement response = serviceClient.sendReceive(payload, serviceEndPoint, "getEmployee");
            Assert.assertNotNull(response, "Response Message is null");
            Assert.assertTrue(response.toString().contains("<employeeID>"), "EmployeeID record Not Found");
            Assert.assertTrue(response.toString().contains("<lastName>"), "LastName Record Not Found");
        }

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

    /* private void deletePrivilegeGroup() throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_" + privilegeGroupName)).findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Do you want to remove privilege group?"),
                   "Privilege Group delete Pop-up Failed :");
        selenium.click("//button");
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("Privilege group has been successfully removed"),
                   "Privilege Group delete Verification Pop-up Failed :");
        selenium.click("//button");
        Thread.sleep(sleepTime);

    }*/

    /* private void deleteDatabase() throws InterruptedException {
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
        Thread.sleep(sleepTime);
        log.info("Deleted Database");
    }*/

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
                Thread.sleep(sleeptime);
                break;
            }


        }

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
        driver.findElement(By.id("sql")).sendKeys("SELECT employeeNumber, lastName FROM Employees");   //sql query
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
        driver.findElement(By.id("txtDataServiceElementNamespace")).clear();
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
        driver.findElement(By.id("serviceName")).sendKeys(dataServiceName);
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

    private String getCarbonDataSourceID(String dataSourceUrl) throws Exception {
        driver.get(dataSourceUrl);
        String dataSourceID = "";
        log.info("Carbon Data Source ID is : " + dataSourceID);
        List<WebElement> carbonDataSourceList = driver.findElement(By.id("myTable")).
                findElements(By.tagName("tr"));
        Iterator<WebElement> cdsItr = carbonDataSourceList.iterator();
        while(cdsItr.hasNext()) {
            WebElement cds =cdsItr.next();
            if(cds.getAttribute("id").contains(dataBaseName +"_" + domain.replace('.','_'))) {
               dataSourceID = cds.findElement(By.tagName("td")).getText();
                break;
            }

        }
        if(dataSourceID.equals("")){
            throw new Exception("Carbon Data Source Id not found in DataSource list");
        }
        return dataSourceID;
    }

    private void createCarbonDataSource() throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        Thread.sleep(sleeptime);
        databaseList = driver.findElement(By.id("database_table")).findElements(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr"));
        for (int j = 0; j < databaseList.size(); j++) {

            if (driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]/td[1]")).getText().contains(dataBaseName + "_" + domain.replace('.', '_'))) {
                driver.findElement(By.xpath("//tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[" + (j + 1) + "]")).findElement(By.linkText("Manage")).click();
                driver.findElement(By.linkText("Create Datasource")).click();
                Thread.sleep(sleeptime);
                assertTrue(selenium.isTextPresent("Carbon datasource has been successfully created. " +
                                                  "Please update the password field before using the " +
                                                  "Carbon datasource."), "Failed Carbon Source Creation :");
                selenium.click("//button");
                Thread.sleep(sleeptime);
                break;
            }
        }

    }

    private void exploreDatabase(String sqlScript, String dbUserPassword)
            throws InterruptedException {
        driver.findElement(By.id("dbUserTable")).findElement(By.linkText("Explore Database")).click();
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