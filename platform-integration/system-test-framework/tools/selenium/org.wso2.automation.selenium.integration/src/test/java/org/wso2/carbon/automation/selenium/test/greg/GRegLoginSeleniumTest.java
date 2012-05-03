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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.automation.selenium.page.HomePage;
import org.wso2.automation.selenium.page.LoginPage;
import org.wso2.automation.selenium.page.util.UIElementMapper;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.testng.annotations.*;

import static org.testng.Assert.*;

import java.io.IOException;

/**
 * Login  and logout from G-Reg management console
 */
public class GRegLoginSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegLoginSeleniumTest.class);
    private static WebDriver driver;
    private static LoginPage loginPage;
    String username;
    String password;
    private UIElementMapper uiElementMapper;


    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {
        int userId = new GregUserIDEvaluator().getTenantID();
        String baseUrl = new GRegBackEndURLEvaluator().getBackEndURL();
        driver = BrowserManager.getWebDriver();
        driver.get(baseUrl);
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        username = userInfo.getUserName();
        password = userInfo.getPassword();
        loginPage = new LoginPage(driver);
        uiElementMapper = UIElementMapper.getInstance();
    }

    @Test(groups = {"wso2.greg"}, description = "G-Reg user login to system")
    public void testGRegUserLogin() throws InterruptedException, IOException {
        HomePage home = loginPage.loginAs(username, password);
        assertTrue(driver.findElement(By.id(uiElementMapper.getElement("home.logged.user.dev")))
                           .getText().contains(username), "User name not found in the page");
        home.logout();
        assertTrue(driver.findElement(By.id(uiElementMapper.getElement("login.header.div")))
                           .getText().contains("Sign-in"), "Cannot find sign-in text");
        log.info("Login test was successful");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }
}
