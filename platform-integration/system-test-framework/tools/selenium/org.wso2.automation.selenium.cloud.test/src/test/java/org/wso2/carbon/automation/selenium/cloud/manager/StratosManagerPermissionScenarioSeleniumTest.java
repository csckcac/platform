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

import static org.testng.Assert.assertTrue;


public class StratosManagerPermissionScenarioSeleniumTest {
    private static final Log log = LogFactory.getLog(StratosManagerPermissionScenarioSeleniumTest.
            class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "manager";
    String userName;
    String password;
    String domain;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(6);
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

    @Test(groups = {"wso2.manager"}, description = "Login Permission Scenario test", priority = 1)
    public void testUserLoginPermissionScenario() throws Exception {
        String baseURL = "https://stratoslive.wso2.com";
        String userManagementURL = baseURL + "/t/" + domain + "/carbon/userstore/index.jsp?region" +
                                   "=region1&item=userstores_menu";
        String roleName = "login1";
        String newUserName = "mgrlogin";
        String newFulluserName = newUserName + "@" + domain;
        String newUserpassword = "welcome";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            gotoUserManagementPage(userManagementURL);
            createNewUser(newUserName, newUserpassword);
            gotoUserManagementPage(userManagementURL);
            addRole(newUserName, roleName);
            userLogout();
            //loging with new user credientails
            new StratosUserLogin().userLogin(driver, selenium, newFulluserName,
                                             newUserpassword, productName);
            assertTrue(driver.getPageSource().contains("Users and Roles"),
                       "New User Failed to Log in :");
            userLogout();
            //login with admin credientails
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoUserManagementPage(userManagementURL);
            deleteUser();
            gotoUserManagementPage(userManagementURL);
            deleteRole();
            userLogout();
            log.info("*******IS Stratos - Login Only Permission Scenario Test - Passed **********");
        } catch (AssertionFailedError e) {
            log.info("Login Only Permission Scenario Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Login Only Permission Scenario Test Failed " +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Login Only Permission Scenario Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Login Only Permission Scenario Test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Login Only Permission Scenario Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Login Only Permission Scenario Test Failed :" +
                                e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }


    private void gotoUserManagementPage(String userManagementURL) throws InterruptedException {
        driver.get(userManagementURL);
        waitTimeforElement("//tr[2]/td/a");
    }

    private void createNewUser(String newUserName, String newUserpassword)
            throws InterruptedException {
        driver.findElement(By.linkText("Users")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        driver.findElement(By.linkText("Add New User")).click();
        waitTimeforElement("//input");
        //enter user info
        driver.findElement(By.name("username")).sendKeys(newUserName);
        driver.findElement(By.name("password")).sendKeys(newUserpassword);
        driver.findElement(By.name("retype")).sendKeys(newUserpassword);
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        log.info("New user was created :" + newUserName);
    }

    private void addRole(String userName, String roleName) throws InterruptedException {
        driver.findElement(By.linkText("Roles")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        driver.findElement(By.linkText("Add New Role")).click();
        waitTimeforElement("//input");
        driver.findElement(By.name("roleName")).sendKeys(roleName);
        driver.findElement(By.xpath("//tr[2]/td/input")).click();
        waitTimeforElement("//input[3]");
        driver.findElement(By.xpath("//div[3]/table/tbody/tr/td[4]/div")).click();
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//input");
        driver.findElement(By.xpath("//input")).sendKeys(userName);
        driver.findElement(By.xpath("//td[3]/input")).click();
        waitTimeforElement("//td/table/tbody/tr/td/input");
        driver.findElement(By.name("selectedUsers")).click();
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
    }

    private void deleteUser() throws InterruptedException {
        driver.findElement(By.linkText("Users")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to delete the user 'mgrlogin'?"),
                   "Failed to delete user :");
        selenium.click("//button");
        waitTimeforElement("//table[2]/tbody/tr/td/a");
    }

    private void deleteRole() throws InterruptedException {
        driver.findElement(By.linkText("Roles")).click();
        waitTimeforElement("//table[2]/tbody/tr/td/a");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//div/div/span");
        assertTrue(selenium.isTextPresent("exact:Do you wish to delete the role login1?"),
                   "Failed to delete Role :");
        selenium.click("//button");
        waitTimeforElement("//table[2]/tbody/tr/td/a");
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
