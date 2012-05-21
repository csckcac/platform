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
package org.wso2.carbon.automation.selenium.cloud.manager;

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

public class StratosManagerRoleCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(StratosManagerRoleCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    private String userName;
    private String password;
    private String roleName;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(6);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                                                                                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        roleName = "login";

    }

    @Test(groups = {"wso2.manager"}, description = "add new role with login permission", priority = 1)
    public void testAddNewLoginRole() throws Exception {

        try {
            String productName = "manager";
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos Manager Login Success");

            driver.findElement(By.id("menu-panel-button3")).click();
            driver.findElement(By.linkText("Users and Roles")).click();
            driver.findElement(By.linkText("Roles")).click();
            deleteRoleByName(roleName);
            addRole(roleName);

            log.info("*******Stratos Manager - Add New Role Test - Passed ***********");
        } catch (AssertionError e) {
            log.info("Failed to create  new role :" + e);
            userLogout();
            throw new AssertionError("Failed to create  new role :" + e);
        } catch (WebDriverException e) {
            log.info("Failed to create  new role :" + e);
            userLogout();
            throw new WebDriverException("Failed to create  new role :" + e);
        } catch (Exception e) {
            log.info("Failed to create  new role :" + e);
            userLogout();
            throw new Exception("Failed to create  new role :" + e);
        }

    }

    private void deleteRoleByName(String roleName) {
        WebElement table =
                driver.findElement(By.id("roleTable"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        Iterator<WebElement> i = rows.iterator();
        boolean status = false;
        boolean outStatus = false;
        int counter = 0;
        while (i.hasNext()) {
            WebElement row = i.next();
            List<WebElement> columns = row.findElements(By.tagName("td"));
            for (WebElement column : columns) {
                System.out.print(column.getText());

                if (column.getText().equals(roleName)) {
                    status = true;
                }
                if (status && column.getText().contains("Delete")) {
                    outStatus = true;
                    driver.findElement(By.xpath
                                                  ("/html/body/table/tbody/tr[2]/td[3]/table/tbody/" +
                                                   "tr[2]/td/div/div/table/tbody/tr[" +
                                                   counter + "]/td[2]/a[4]")).click();
                    driver.findElement(By.xpath("//button")).click();
                    break;
                }
            }
            if(outStatus){
                break;
            }
            counter++;
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        deleteRoleByName(roleName);
        Thread.sleep(2000);
        userLogout();
        driver.quit();
    }

    private void addRole(String roleName) throws InterruptedException {
        driver.findElement(By.linkText("Add New Role")).click();
        driver.findElement(By.name("roleName")).sendKeys(roleName);
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        driver.findElement(By.xpath("//div[3]/table/tbody/tr/td[4]/div")).click();
        driver.findElement(By.xpath("//input[3]")).click();
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }
}
