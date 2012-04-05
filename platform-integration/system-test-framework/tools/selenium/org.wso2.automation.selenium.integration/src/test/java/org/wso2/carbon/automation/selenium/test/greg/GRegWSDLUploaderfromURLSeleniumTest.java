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
package org.wso2.carbon.automation.selenium.test.greg;

import com.thoughtworks.selenium.Selenium;
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregResourceURLUploader;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;
import org.testng.annotations.*;

import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class GRegWSDLUploaderfromURLSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegWSDLUploaderfromURLSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String username;
    String password;


    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException {
        int userId = new GregUserIDEvaluator().getTenantID();
        String baseUrl = new GRegBackEndURLEvaluator().getBackEndURL();
        log.info("baseURL is " + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        UserInfo tenantDetails = UserListCsvReader.getUserInfo(userId);
        username = tenantDetails.getUserName();
        password = tenantDetails.getPassword();
    }


    @Test(groups = {"wso2.greg"}, description = "add echo wsdl from url",priority = 1)
    public void testAddWSDLfromURL() throws Exception {
        String wsdlURL = "http://people.wso2.com/~evanthika/wsdls/echo.wsdl";

        try {
            new GregUserLogin().userLogin(driver, username, password);
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"), "G-Reg Home Page fail :");
            // Click on add wsdl link
            driver.findElement(By.linkText("WSDL")).click();
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("Add WSDL"), "Add WSDL Dash Board - Add WSDL Text does not appear::");
            assertTrue(selenium.isTextPresent("Add New WSDL"), "Add WSDL Dash Board - Add New WSDL Text does not appear::");
            // Add WSDL Dashboard
            new GregResourceURLUploader().uploadResource(driver, wsdlURL, null);
            Thread.sleep(15000L);
            // wsdl dash board
            assertTrue(selenium.isTextPresent("WSDL List"), "WSDL Dashboard Does not appear ::");
            assertTrue(selenium.isTextPresent("echo.wsdl"), "Uploaded WSDL name does appear on WSDL Dashboard :");
            // click on delete wsdl link
            driver.findElement(By.linkText("Delete")).click();
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Pop-up fail:");
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");
            //goto service dashboard
            selenium.click("css=input.button.registryWriteOperation");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("Service List"), "Service List DashBoard fail : ");
            // delete service
            driver.findElement(By.linkText("Delete")).click();
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Service Delete Popup fail :");
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");
            //logout
            new GregUserLogout().userLogout(driver);
            log.info("GRegWSDLUploaderfromURLSeleniumTest -testAddWSDLfromURL()- Passed");
        } catch (AssertionFailedError e) {
            log.info("GregAddWsdlfromURLSeleniumTest - Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GregAddWsdlfromURLSeleniumTest");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to upload echo wsdl from url:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GregAddWsdlfromURLSeleniumTest - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GregAddWsdlfromURLSeleniumTest");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to upload echo wsdl from url:" + e.getMessage());
        } catch (Exception e) {
            log.info("GregAddWsdlfromURLSeleniumTest - Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GregAddWsdlfromURLSeleniumTest");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to upload echo wsdl from url:" + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();

    }
}
