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

import static org.testng.Assert.assertTrue;


public class DSSPriviledgeGroupCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSPriviledgeGroupCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "dss";
    String userName;
    String password;
    String domain;
    long sleeptime = 4000;

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
        StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
        deletePrivilegeGroupIfExists();
    }


    @Test(groups = {"wso2.manager"}, description = "add a new privilege group", priority = 1)
    public void testAddPrivilegeGroup() throws Exception {
        try {

            log.info("Stratos Data Login Success");
            addPrivilegeGroup();
            deletePrivilegeGroup();
            userLogout();
            log.info("*******Data Stratos - Add Privilege Group  Test - Passed **********");
        } catch (AssertionFailedError e) {
            log.info("Add Privilege Group  Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add Privilege Group  Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Add Privilege Group  Test Failed:" + e);
            userLogout();
            throw new WebDriverException("Add Privilege Group  Test Failed :" + e);
        } catch (Exception e) {
            log.info("Add Privilege Group  Test Failed :" + e);
            userLogout();
            throw new Exception("Add Privilege Group  Test Failed:" + e);
        }
    }

    private void addPrivilegeGroup() throws InterruptedException {
        waitTimeforElement("//li[4]/ul/li/a");
        driver.findElement(By.linkText("Privilege Groups")).click();
        Thread.sleep(sleeptime);
        driver.findElement(By.linkText("Add new privilege group")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("privGroupName")).sendKeys("qatest");
        driver.findElement(By.id("selectAll")).click();
        driver.findElement(By.xpath("//td[3]/table/tbody/tr[3]/td/input")).click();
        waitTimeforElement("//div[4]/div/div");
        assertTrue(selenium.isTextPresent("Privilege group has been successfully created"),
                   "Privilege Group Pop-up Failed :");
        selenium.click("//button");
        waitTimeforElement("//li[4]/ul/li/a");
    }

    private void deletePrivilegeGroup() throws InterruptedException {
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to remove privilege group?"),
                   "Privilege Group delete Pop-up Failed :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("Privilege group has been successfully removed"),
                   "Privilege Group delete Verification Pop-up Failed :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void deletePrivilegeGroupIfExists() throws InterruptedException {
        waitTimeforElement("//li[4]/ul/li/a");
        driver.findElement(By.linkText("Privilege Groups")).click();
        if (driver.findElement(By.id("privilegeGroupTable")).getText().contains("qatest")) {
            driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_qatest")).findElement(By.linkText("Delete")).click();
            waitTimeforElement("//div[3]/div/div");
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

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
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