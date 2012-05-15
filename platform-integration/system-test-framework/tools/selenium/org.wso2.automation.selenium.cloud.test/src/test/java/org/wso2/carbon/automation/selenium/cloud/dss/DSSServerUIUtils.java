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

import org.apache.axiom.om.util.Base64;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class DSSServerUIUtils {
    private WebDriver driver;
    private FrameworkProperties dssProperties;

    public DSSServerUIUtils(WebDriver driver) throws MalformedURLException {
        this.driver = driver;
        dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    }

    public void deleteDataService(String dataServiceName)
            throws InterruptedException {
        driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr/td/div/ul/li[5]/ul/li[2]/ul/li/a")).click();
        List<WebElement> tr;
        tr = driver.findElement(By.id("sgTable")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement service : tr) {
            if (service.getText().contains(dataServiceName)) {
                service.findElement(By.name("serviceGroups")).click();
                driver.findElement(By.id("delete1")).click();
                driver.findElement(By.xpath("//button")).click();
                driver.findElement(By.id("messagebox-info")).getText();
                Assert.assertEquals(driver.findElement(By.id("messagebox-info")).getText(),
                                    "Successfully deleted selected service groups",
                                    "Failed to upload Data Service File :");
                Thread.sleep(1000);
                driver.findElement(By.xpath("//button")).click();
                break;
            }


        }

    }

    public void deleteServiceIfExists(String dataServiceName)
            throws InterruptedException {
        driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr/td/div/ul/li[5]/ul/li[2]/ul/li/a")).click();
        if (driver.findElements(By.id("sgTable")).size() > 0) {
            if (driver.findElement(By.id("sgTable")).getText().contains(dataServiceName)) {
                deleteDataService(dataServiceName);
            }
        }
    }

    public void dropDatabaseIfExist(String dataBaseName) throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (int j = 0; j < databaseList.size(); j++) {
            WebElement dbElement = databaseList.get(j);
            if (dbElement.findElements(By.tagName("td")).get(0).getText().contains(dataBaseName)) {
                dbElement.findElement(By.linkText("Manage")).click();
                if (driver.findElements(By.id("dbUserTable")).size() > 0) {
                    List<WebElement> userList = driver.findElement(By.id("dbUserTable")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

                    for (int i = 0; i < userList.size(); i++) {
                        WebElement user = driver.findElement(By.id("dbUserTable")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).get(0);
                        if (user.getText().contains("Drop")) {
                            user.findElement(By.linkText("Drop")).click();
                            Assert.assertEquals(driver.findElement(By.id("messagebox-confirm")).getText(),
                                                "Do you want to drop the user?",
                                                "user deletion confirmation message mismatched");
                            driver.findElement(By.xpath("//button")).click();

                        }


                    }
                }
                driver.findElement(By.linkText("Databases")).click();

                driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).get(j).findElement(By.linkText("Drop")).click();
                Assert.assertEquals(driver.findElement(By.className("ui-dialog-container")).findElement(By.id("dialog")).getText(),
                                    "Do you want to drop the database?",
                                    "Database Dropping confirmation message mismatched");
                Thread.sleep(1000);
                driver.findElement(By.xpath("//button")).click();
                break;
            }

        }


    }

    public void deletePrivilegeGroupIfExists(String privilegeGroupName)
            throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        if (driver.findElement(By.id("privilegeGroupTable")).getText().contains(privilegeGroupName)) {
            driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_" + privilegeGroupName)).findElement(By.linkText("Delete")).click();
            Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-confirm")).getText(),
                                "Do you want to remove privilege group?",
                                "Privilege Group deletion confirmation message mismatched");
            Thread.sleep(1000);
            driver.findElement(By.xpath("//button")).click();

            Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-info")).getText(),
                                "Privilege group has been successfully removed",
                                "Verification Message mismatched");
            Thread.sleep(1000);
            driver.findElement(By.xpath("//button")).click();
        }


    }

    public void addPrivilegeGroup(String groupName) throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();

        driver.findElement(By.linkText("Add new privilege group")).click();
        driver.findElement(By.id("privGroupName")).sendKeys(groupName);
        driver.findElement(By.id("selectAll")).click();
        driver.findElement(By.xpath("//td[3]/table/tbody/tr[3]/td/input")).click();

        Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-info")).getText(),
                            "Privilege group has been successfully created",
                            "Privilege Group Pop-up message mismatched:");
        driver.findElement(By.xpath("//button")).click();
    }

    public void login(String userName, String password) {
        driver.get(new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME));
        driver.findElement(By.xpath("//a[2]/img")).click();
        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//tr[4]/td[2]/input")).click();

    }

    public void createDatabase(String dataBaseName) throws InterruptedException {
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
        Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-info")).getText(),
                            "Database has been successfully created",
                            "Database creation pop-up message mismatched");
        driver.findElement(By.xpath("//button")).click();

    }

    public void createDataBaseUser(String privilegeGroupName, String dbUserName,
                                   String dbUserPassword, String databaseName)
            throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement database : databaseList) {
            if (database.findElements(By.tagName("td")).get(0).getText().contains(databaseName)) {
                database.findElement(By.linkText("Manage")).click();

                driver.findElement(By.linkText("Add New User")).click();
                driver.findElement(By.id("username")).sendKeys(dbUserName);
                driver.findElement(By.id("password")).sendKeys(dbUserPassword);
                driver.findElement(By.id("repeatPassword")).sendKeys(dbUserPassword);
                Select selectGroup = new Select(driver.findElement(By.id("privGroupList")));
                selectGroup.selectByVisibleText(privilegeGroupName);
                List<WebElement> buttons = driver.findElement(By.id("dbGeneralInfo")).
                        findElements(By.className("button"));
                for (WebElement button : buttons) {
                    if (button.getAttribute("value").equalsIgnoreCase("Save")) {
                        button.click();
                        break;
                    }
                }
                Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-info")).getText(),
                                    "User has been successfully created",
                                    "Database User creation pop-up message mismatched");
                Thread.sleep(1000);
                driver.findElement(By.xpath("//button")).click();
                break;
            }
        }
    }

    public void exploreDatabase(String sqlScript, String dbUserPassword, String databaseName)
            throws InterruptedException {


        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement database : databaseList) {
            if (database.findElements(By.tagName("td")).get(0).getText().contains(databaseName)) {
                database.findElement(By.linkText("Manage")).click();
                Thread.sleep(1000);
                driver.findElement(By.id("dbUserTable")).findElement(By.linkText("Explore Database")).click();
                driver.switchTo().frame("inlineframe");
                driver.findElement(By.name("password")).sendKeys(dbUserPassword);
                driver.findElement(By.xpath("//tr[10]/td[2]/input")).click();
                Thread.sleep(2000);

                driver.switchTo().frame("h2query");
                driver.findElement(By.id("sql")).sendKeys(sqlScript);
                driver.findElement(By.xpath("/html/body/form/input")).click();
                //to clear text area
                driver.findElement(By.xpath("/html/body/form/input[2]")).click();

                driver.switchTo().defaultContent();
                break;
            }


        }

        driver.switchTo().defaultContent();
    }

    public void createCarbonDataSource(String databaseName) throws InterruptedException {
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement database : databaseList) {
            if (database.findElements(By.tagName("td")).get(0).getText().contains(databaseName)) {
                database.findElement(By.linkText("Manage")).click();
                driver.findElement(By.linkText("Create Datasource")).click();
                Thread.sleep(1000);
                Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-info")).getText(),
                                    "Carbon datasource has been successfully created. Please update " +
                                    "the password field before using the Carbon datasource.",
                                    "Failed Carbon Source Creation :");
                driver.findElement(By.xpath("//button")).click();
                Thread.sleep(1000);
                break;
            }
        }

    }

    public String getJdbcUrl(String databaseName) {
        String jdbcUrl = null;
        List<WebElement> databaseList;
        driver.findElement(By.linkText("Databases")).click();
        databaseList = driver.findElement(By.id("database_table")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement dbElement : databaseList) {
            if (dbElement.findElements(By.tagName("td")).get(0).getText().contains(databaseName)) {
                jdbcUrl = dbElement.findElements(By.tagName("td")).get(3).getText();
                break;
            }
        }

        return jdbcUrl;
    }

    public void goToService(String serviceName) throws InterruptedException {
        driver.findElement(By.linkText("List")).click();
        List<WebElement> tr;
        tr = driver.findElement(By.id("sgTable")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement service : tr) {
            if (service.getText().contains(serviceName)) {
                service.findElement(By.linkText(serviceName)).click();
                Thread.sleep(1000);
                Assert.assertTrue(driver.findElement(By.id("middle")).findElement(By.tagName("h2")).
                        getText().contains(serviceName), "Service Name not found in service page");
                break;
            }


        }

    }

    public List<String> getCarbonDataSourceList(String databaseName, String userDomain) {
        String baseUrlData = "https://" + dssProperties.getProductVariables().getHostName();
        if (dssProperties.getEnvironmentSettings().isEnablePort()) {
            baseUrlData = baseUrlData + ":" + dssProperties.getProductVariables().getHttpsPort();
        }
        if (dssProperties.getEnvironmentSettings().isEnableCarbonWebContext()) {
            baseUrlData = baseUrlData + "/" + dssProperties.getProductVariables().getWebContextRoot();
        }
        String dataSourceUrl = baseUrlData + "/t/" + userDomain + "/carbon/datasource/index.jsp?region=" +
                               "region1&item=datasource_menu";
        driver.get(dataSourceUrl);
        List<String> dataSourceList = new ArrayList<String>();
        List<WebElement> carbonDataSourceList = driver.findElement(By.id("myTable")).
                findElements(By.tagName("tr"));
        for (WebElement cds : carbonDataSourceList) {
            if (cds.getAttribute("id").contains(databaseName)) {
                dataSourceList.add(cds.findElement(By.tagName("td")).getText());

            }

        }

        return dataSourceList;
    }

    private void addNewQuery(String queryId, String sqlQuery, List<String> outputMappingList)
            throws InterruptedException {
        driver.findElement(By.xpath("//input[2]")).click();
        driver.findElement(By.linkText("Add New Query")).click();

        driver.findElement(By.id("queryId")).sendKeys(queryId);    //query name
        Select dataSource = new Select(driver.findElement(By.id("datasource")));
        dataSource.selectByVisibleText("test");  //datasource name
        driver.findElement(By.id("sql")).sendKeys(sqlQuery);   //sql query
        driver.findElement(By.id("addAutoResponse")).click();
        for (int i = 0; i < outputMappingList.size(); i++) {
            driver.findElement(By.id("existingOutputMappingsTable")).findElement(By.tagName("tbody"))
                    .findElements(By.tagName("tr")).get(i).findElement(By.linkText("edit"));
            driver.findElement(By.id("txtDataServiceOMElementName")).clear();
            driver.findElement(By.id("txtDataServiceOMElementName")).sendKeys(outputMappingList.get(i));
            driver.findElement(By.xpath("//tr[5]/td/input[2]")).click();
            driver.findElement(By.xpath("//tr[5]/td/input")).click();
        }
        //click on button to goto main cofiguration
        driver.findElement(By.xpath("//form/table/tbody/tr[5]/td/input")).click();
        driver.findElement(By.xpath("//tr[25]/td/input")).click();
        driver.findElement(By.xpath("//input[2]")).click(); // waiting till create operation page loads
    }

    public void addNewOperation(String operationName, String queryId) throws InterruptedException {
        driver.findElement(By.linkText("Add New Operation")).click();
        driver.findElement(By.id("operationName")).sendKeys(operationName);
        Select selectQuery = new Select(driver.findElement(By.id("queryId")));
        selectQuery.selectByVisibleText(queryId);      //query name ...??
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        driver.findElement(By.xpath("//input[3]")).click();
    }

    public List<String> getServiceList() {
        driver.findElement(By.linkText("List")).click();
        List<String> serviceList = new ArrayList<String>();
        List<WebElement> tr = driver.findElement(By.id("sgTable")).findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement service : tr) {
            serviceList.add(service.findElements(By.tagName("td")).get(1).getText());
        }
        return serviceList;
    }

    public boolean isServiceDeployed(String serviceName) {
        driver.findElement(By.linkText("List")).click();
        List<String> listItr = getServiceList();
        for (String service : listItr) {
            if (service.equalsIgnoreCase(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public void logOut() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        driver.quit();
    }


    public String getFullyQualifiedUsername(String username, String tenantDomain) {
        if (tenantDomain != null) {

            /* The maximum number of characters allowed for the username in mysql system tables is
     * 16. Thus, to adhere the aforementioned constraint as well as to give the username
     * an unique identification based on the tenant domain, we append a hash value that is
     * created based on the tenant domain */
            byte[] bytes = intToByteArray(tenantDomain.hashCode());
            return username + "_" + Base64.encode(bytes);
        }
        return username;
    }

    private static byte[] intToByteArray(int value) {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}
