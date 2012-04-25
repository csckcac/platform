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
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.testng.annotations.*;

import static org.testng.Assert.*;

import java.net.MalformedURLException;


public class GregLoginSeleniumTest {
    private static final Log log = LogFactory.getLog(GregLoginSeleniumTest.class);
    private static Selenium selenium;
    private static UserInfo tenantDetails;
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
        tenantDetails = UserListCsvReader.getUserInfo(userId);
        username = tenantDetails.getUserName();
        password = tenantDetails.getPassword();
    }

    @Test(groups = {"wso2.greg"}, description = "G-Reg user login to system")
    public void testGRegUserLogin() throws InterruptedException {
        new GregUserLogin().userLogin(driver, username, password);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"));
        new GregUserLogout().userLogout(driver);
        log.info("******GregLoginSeleniumTest -testGRegUserLogin()-Passed****** ");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }
}
