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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;

import static org.testng.Assert.*;

import java.net.MalformedURLException;

public class GRegServiceCreatorSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegServiceCreatorSeleniumTest.class);
    private static Selenium selenium;
    private static UserInfo tenantDetails;
    private static WebDriver driver;
    String username;
    String password;


    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException {
        int userId = new GregUserIDEvaluator().getTenantID();
        log.info("user if is :" + userId);
        String baseUrl = new GRegBackEndURLEvaluator().getBackEndURL();
        log.info("baseURL is " + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        tenantDetails = UserListCsvReader.getUserInfo(userId);
        username = tenantDetails.getUserName();
        password = tenantDetails.getPassword();
    }

    @Test(groups = {"wso2.greg"}, description = "add a simple service from UI", priority = 1)
    public void testaddService() throws Exception {
        try {
            new GregUserLogin().userLogin(driver, username, password);
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"));
            //goto Add Service link
            selenium.click("//li[6]/ul/li[4]/ul/li/a");
            selenium.waitForPageToLoad("30000");
            // verify Add Service page attributes
            assertTrue(selenium.isTextPresent("Add Service"));
            assertTrue(selenium.isTextPresent("Overview"));
            assertTrue(selenium.isTextPresent("Service Lifecycle"));
            assertTrue(selenium.isTextPresent("Interface"));
            // Enter Service details
            selenium.type("id=id_Overview_Name", "testservice1");
            selenium.type("id=id_Overview_Namespace", "testservice123");
            selenium.select("name=Service_Lifecycle_Lifecycle-Name", "label=ServiceLifeCycle");
            selenium.click("link=Add Contact");
            selenium.select("name=Contacts_Contact1", "label=Business Owner");
            selenium.type("id=id_Contacts_Contact1", "Aaaa");
            selenium.type("id=id_Interface_WSDL-URL", "aaa");
            // click on save
            selenium.click("css=input.button.registryWriteOperation");
            Thread.sleep(5000L);
            // verify service added properly
            assertEquals("/_system/governance/trunk/services/testservice123/testservice1", selenium.getValue("id=uLocationBar"));
            assertTrue(selenium.isTextPresent("Metadata"));
            assertTrue(selenium.isTextPresent("Properties"));
            assertTrue(selenium.isTextPresent("Content"));
            // click on service dash board
            selenium.click("//div[@id='menu']/ul/li[6]/ul/li[2]/ul/li/a");
            Thread.sleep(2000L);
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("Service List"));
            assertTrue(selenium.isTextPresent("testservice1"));
            assertTrue(selenium.isTextPresent("testservice123"));
            // delete service
            selenium.click("//td[4]/a");
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"));
            assertTrue(selenium.isTextPresent("Are you sure you want to delete'/_system/governance/trunk/services/testservice123/testservice1' permanently?"));
            selenium.click("css=button[type=\"button\"]");
            selenium.waitForPageToLoad("30000");
            new GregUserLogout().userLogout(driver);
            log.info("*****GRegServiceCreatorSeleniumTest - testaddService() -Passed*****");
        } catch (Exception e) {
            log.info("GRegServiceCreatorSeleniumTest - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegServiceCreatorSeleniumTest");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(3000L);
            throw new Exception("Failed to add a service:" + e.getMessage());
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }
}
