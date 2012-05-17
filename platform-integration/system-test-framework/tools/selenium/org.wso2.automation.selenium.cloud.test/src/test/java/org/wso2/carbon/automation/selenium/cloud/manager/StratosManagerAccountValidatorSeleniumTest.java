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
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.*;


public class StratosManagerAccountValidatorSeleniumTest {
    private static final Log log = LogFactory.getLog(StratosManagerAccountValidatorSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "manager";
    String userName;
    String password;
    String domain;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                                                                                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.manager"}, description = "verify Account Manager", priority = 1)
    public void testVerifyAccountManager() throws Exception {
        String baseurl = "https://stratoslive.wso2.com";
        String accountManagerUrl = baseurl + "/t/" + domain + "/carbon/account-mgt/" +
                                   "account_mgt.jsp?region=region1&item=gaas_account_mgt_menu";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos Manager Login Success");
            gotoAccountManagerPage(accountManagerUrl);
            userLogout();
            log.info("*******Stratos Manager Account Validation Page Viewer Test - Passed *******");
        } catch (AssertionFailedError e) {
            log.info("Manager Account Validation Page Viewer Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Manager Account Validation Page Viewer Test Failed :" +
                                           e);
        } catch (WebDriverException e) {
            log.info("Manager Account Validation Page Viewer Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Manager Account Validation Page Viewer Test Failed :" +
                                         e);
        } catch (Exception e) {
            log.info("Manager Account Validation Page Viewer Test Failed :" + e);
            userLogout();
            throw new Exception("Manager Account Validation Page Viewer Test Failed :" +
                                e);
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void gotoAccountManagerPage(String accountManagerUrl) throws InterruptedException {
        driver.get(accountManagerUrl);
        assertTrue(driver.getPageSource().contains("Contact Information"),
                   "Faile to display Contact Information :");
        assertTrue(driver.getPageSource().contains("Administrator Profile"),
                   "Faile to display Administrator Profile :");
        assertTrue(driver.getPageSource().contains("Validate Domain Ownership"),
                   "Faile to display Validate Domain Ownership :");
        assertTrue(driver.getPageSource().contains("Usage Plan Information"),
                   "Faile to display Usage Plan Information :");
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
    }

}
