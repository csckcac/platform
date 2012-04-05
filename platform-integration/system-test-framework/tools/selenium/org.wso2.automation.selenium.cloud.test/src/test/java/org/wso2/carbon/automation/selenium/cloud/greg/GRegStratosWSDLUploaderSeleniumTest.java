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
package org.wso2.carbon.automation.selenium.cloud.greg;

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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertTrue;


public class GRegStratosWSDLUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosWSDLUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    long sleeptime = 3000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(ProductConstant.
                GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.stratos.greg"}, description = "add wsdl from url", priority = 1)
    public void testaddWSDLfromURL() throws Exception {
        String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/echo.wsdl";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoAddWSDLPage();
            addWsdlfromURL(wsdl_url);
            deleteWSDL();
            deleteService();
            userLogout();
            log.info("**********GReg Stratos WSDL Upload from URL Test - Passed ****************");
        } catch (AssertionFailedError e) {
            log.info("WSDL Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("WSDL Upload from URL Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("WSDL Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("WSDL Upload from URL Test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("WSDL Upload from URL Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("WSDL Upload from URL Test Failed :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.stratos.greg"}, description = "add wsdl from file", priority = 2)
    public void testaddWSDLfromFile() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;
        String file_path = resourcePath + File.separator + "artifacts" + File.separator +
                           "Selenium" + File.separator + "GREG" + File.separator + "wsdl" +
                           File.separator + "echo.wsdl";

        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            gotoAddWSDLPage();
            addWSDLfromFile(file_path);
            deleteWSDL();
            deleteService();
            userLogout();
            log.info("**********GReg Stratos WSDL Upload from File Test - Passed ****************");
        } catch (AssertionFailedError e) {
            log.info("WSDL Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("WSDL Upload from File Test Failed :" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("WSDL Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("WSDL Upload from File Test Failed :" + e.getMessage());
        } catch (Exception e) {
            log.info("WSDL Upload from File Test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("WSDL Upload from File Test Failed :" + e.getMessage());
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void addWSDLfromFile(String file_path) throws InterruptedException {
        Select select = new Select(driver.findElement(By.id("addMethodSelector")));
        select.selectByVisibleText("Upload WSDL from a file");
        waitTimeforElement("//p/input");
        selenium.focus("id=uResourceFile");
        Thread.sleep(sleeptime);
        driver.findElement(By.id("uResourceFile")).sendKeys(file_path);
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        assertTrue(driver.getPageSource().contains("echo.wsdl"), "Failed to add echo.wsdl :");
        assertTrue(driver.getPageSource().contains("http://echo.services.core.carbon.wso2.org"),
                   "Failed to display tagnameSpace :");
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }


    private void addWsdlfromURL(String wsdl_url) throws InterruptedException {
        driver.findElement(By.id("irFetchURL")).sendKeys(wsdl_url);
        driver.findElement(By.id("irResourceName")).click();
        driver.findElement(By.xpath("//div/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//form/table/tbody/tr/td/a");
        assertTrue(driver.getPageSource().contains("echo.wsdl"), "Failed to add echo.wsdl :");
        assertTrue(driver.getPageSource().contains("http://echo.services.core.carbon.wso2.org"),
                   "Failed to display tagnameSpace :");
    }


    private void deleteService() throws InterruptedException {
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys("/_system/governance/trunk/services/org");
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_system/" +
                                          "governance/trunk/services/org/wso2' permanently?"),
                   "Failed to delete service");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void deleteWSDL() throws InterruptedException {
        driver.findElement(By.linkText("echo.wsdl")).click();
        waitTimeforElement("//input");
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys("/_system/governance/trunk/wsdls/org/");
        driver.findElement(By.xpath("//input[2]")).click();
        waitTimeforElement("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/_" +
                                          "system/governance/trunk/wsdls/org/wso2' permanently?"),
                   "Failed to delete wsdl :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void gotoAddWSDLPage() throws InterruptedException {
        driver.findElement(By.linkText("WSDL")).click();
        waitTimeforElement("//td[2]/input");
        assertTrue(driver.getPageSource().contains("Add WSDL"), "Failed to display Add WSDL Page :");
        Thread.sleep(sleeptime);
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
