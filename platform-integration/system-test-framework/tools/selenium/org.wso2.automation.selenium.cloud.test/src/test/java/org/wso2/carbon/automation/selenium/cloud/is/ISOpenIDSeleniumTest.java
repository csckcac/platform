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
package org.wso2.carbon.automation.selenium.cloud.is;

import com.thoughtworks.selenium.Selenium;
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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


public class ISOpenIDSeleniumTest {
    private static final Log log = LogFactory.getLog(ISOpenIDSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;


    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.IS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.greg"}, description = "test open id policy ", priority = 1)
    public void testOpenIDPolicy() throws Exception {
        String openID;
        String liveJournelURL = "http://www.livejournal.com/identity/login.bml?type=openid";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            openID = getOpenID();
            logintoLiveJournal(openID, liveJournelURL);
            log.info("**********IS Stratos - Open ID Test -Passed *************");
        } catch (AssertionFailedError e) {
            log.info("Open ID Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Open ID Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Open ID Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Open ID Test Failed :" + e);
        } catch (Exception e) {
            log.info("Open ID Test Failed :" + e);
            userLogout();
            throw new Exception("Open ID Test Failed :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void logintoLiveJournal(String openID, String liveJournelURL)
            throws InterruptedException {
        driver.get(liveJournelURL);
        driver.findElement(By.id("openid_url")).sendKeys(openID);
        driver.findElement(By.xpath("//button")).click();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//td[2]/input")).click();
        driver.findElement(By.id("approve")).click();
        assertTrue(driver.getPageSource().contains("Welcome back to LiveJournal!"),
                   "Failed to Login to Live Journel");
    }

    private String getOpenID() throws InterruptedException {
        driver.findElement(By.linkText("InfoCard/OpenID")).click();
        WebElement openIDValue = driver.findElement(By.xpath("//div/div/table/tbody/tr/td/a"));
        String openID = openIDValue.getText();
        log.info("value is :" + openID);
        return openID;
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }
}
