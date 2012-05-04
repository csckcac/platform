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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ISRelyingPartiesSeleniumTest {
    private static final Log log = LogFactory.getLog(ISOpenIDSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;
    String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;


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

    @Test(groups = {"wso2.greg"}, description = "test add new relying party", priority = 1)
    public void testAddTrustedRelyingParty() throws Exception {
        String filePath = resourcePath + File.separator + "artifacts" + File.separator + "IS" + File.separator + "javarp.cer";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            importRelyingCERT(filePath);
            deleteRelyingCERT();
            userLogout();
            log.info("********** IS Stratos Add new Relying party Test - passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Failed to add new Relying Party :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Failed to add new Relying Party :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Failed to add new Relying Party :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Failed to add new Relying Party :" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add new Relying Party :" + e.getMessage());
            userLogout();
            throw new Exception("Failed to add new Relying Party :" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteRelyingCERT() throws InterruptedException {
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:You are about to remove localhost. " +
                                          "Do you want to proceed?"),
                   "Delete pop-up message Failed: ");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");
    }

    private void importRelyingCERT(String filePath) throws InterruptedException {
        driver.findElement(By.linkText("Trusted Relying Parties")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("rpcert")).sendKeys(filePath);
        driver.findElement(By.name("upload")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("Trusted relying party added successfully"),
                   "Failed to add trusted relying party :");
        selenium.click("//button");
        waitTimeforElement("//form[2]/table/tbody/tr/td");
        assertEquals("localhost", selenium.getText("//form[2]/table/tbody/tr/td"),
                     "Relying certificate has not been uploaded :");
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
