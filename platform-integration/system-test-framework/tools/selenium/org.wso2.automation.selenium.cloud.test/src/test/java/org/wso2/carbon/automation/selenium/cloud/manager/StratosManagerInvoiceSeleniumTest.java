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
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class StratosManagerInvoiceSeleniumTest {
    private static final Log log = LogFactory.getLog(StratosManagerInvoiceSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "manager";
    String userName;
    String password;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.manager"}, description = "verify Invoices page", priority = 1)
    public void testVerifyInvoice() throws Exception {
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos Manager Login Success");
            driver.findElement(By.linkText("Invoices")).click();
            waitForInvoicePage();
            assertTrue(driver.getPageSource().contains("View Invoice"),
                       "Faile to view invoices page :");
            Select select = new Select(driver.findElement(By.id("yearMonth")));
            select.selectByIndex(1);
            driver.findElement(By.xpath("//input")).click();
            assertTrue(driver.findElement(By.className("invoiceTable")).isEnabled(),
                       "Invoice not loaded");
            assertTrue(driver.getPageSource().contains("Value (USD)"),
                       "Faile to display invoce value :");
            assertTrue(driver.getPageSource().contains("Charges for Subscriptions"),
                       "Faile to display invoice :");
            userLogout();
            log.info("*******Stratos Manager Verify Invoice Test - Passed *******");
        } catch (AssertionFailedError e) {
            log.info("Verify Invoice Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Verify Invoice Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Verify Invoice Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Verify Invoice Test Failed:" + e);
        } catch (Exception e) {
            log.info("Verify Invoice Test Failed :" + e);
            userLogout();
            throw new Exception("Verify Invoice Test Failed :" + e);
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }

    private boolean waitForInvoicePage() {
        long currentTime = System.currentTimeMillis();
        long exceededTime;
        do {
            try {
                if (driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText()
                            .contains("View Invoice")) {
                    return true;
                }
            } catch (WebDriverException ignored) {
                log.info("Waiting for the element");
            }
            exceededTime = System.currentTimeMillis();
        } while (!(((exceededTime - currentTime) / 1000) > 60));
        return false;
    }
}
