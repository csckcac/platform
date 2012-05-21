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
package org.wso2.carbon.automation.selenium.cloud.greg;

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.*;


public class GRegServiceCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegServiceCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    String schemaName = "books.xsd";
    String schema_path = "/_system/governance/trunk/schemas/books";
    long sleepTime = 5000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.stratos.greg"}, description = "add a simple service", priority = 1)
    public void testAddService() throws Exception {
        String serviceName = "testservice1";
        String nameSpace = "service123";
        String servicePath = "/_system/governance/trunk/services/service123/testservice1";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoAddServicePage();
            createService(serviceName, nameSpace, servicePath);
            findLocation("/_system/governance/trunk/services/service123");
            deleteService();
            userLogout();
            log.info("*********GReg Stratos - Add Simple Service Test - Passed****************");
        } catch (AssertionError e) {
            log.info("Add Simple Service Test Failed :" + e);
            userLogout();
            throw new AssertionError("Add Simple Service Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Add Simple Service Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add Simple Service Test Failed :" + e);
        } catch (Exception e) {
            log.info("Add Simple Service Test Failed:" + e);
            userLogout();
            throw new Exception("Add Simple Service Test Failed :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }

    private void deleteService() throws InterruptedException {
        waitTimeforElement("//td/table/tbody/tr/td[2]/table/tbody/tr/td[2]/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/services/service123/testservice1' " +
                                          "permanently?"), "Failed to delete service :");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");

    }

    private void createService(String serviceName, String nameSpace, String servicePath)
            throws InterruptedException {
        driver.findElement(By.id("id_Overview_Name")).sendKeys(serviceName);
        driver.findElement(By.id("id_Overview_Namespace")).sendKeys(nameSpace);
        driver.findElement(By.xpath("//form/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//div[3]/div/div/div/form/table/tbody/tr[2]/td/input");
        assertEquals(servicePath, selenium.getValue("//input"), "Failed to create a service :");
    }


    private void gotoAddServicePage() throws InterruptedException {
        driver.findElement(By.linkText("Service")).click();
        waitTimeforElement("//td[2]/input");
        assertTrue(driver.getPageSource().contains("Add Service"),
                   "Failed to display Add Service Page :");
        waitTimeforElement("//td[2]/input");
    }

    private void findLocation(String path) throws Exception {
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        driver.findElement(By.xpath("//input[2]")).click();
        Thread.sleep(5000L);
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
}
