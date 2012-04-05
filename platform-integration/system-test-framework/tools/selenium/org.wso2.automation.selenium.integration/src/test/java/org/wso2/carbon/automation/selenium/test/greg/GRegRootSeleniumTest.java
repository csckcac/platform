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
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;

import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;

import java.net.MalformedURLException;


public class GRegRootSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegRootSeleniumTest.class);
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

    @Test(groups = {"wso2.greg"}, description = "add a collection to root",priority = 1)
    public void testAddCollectionToRoot() throws Exception, AssertionFailedError {
        String collectionPath = "/selenium_root";
        try {
            userLogin();
            gotoDetailViewTab();
            addCollection(collectionPath);
            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest addCollectionToRoot() - Passed ");
        } catch (AssertionFailedError e) {
            log.info("Failed to add a collection to root- Assertion Fail:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollectionToRoot");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to add a collection to root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Failed to add a collection to root- WebDriver Error: :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollectionToRoot");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to add collection to root:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add a collection to root:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollectionToRoot");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add collection to root:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "add a resource to root",priority = 2)
    public void testAddResourceToRoot() throws Exception {
        String resourceName = "root_resource";
        try {
            userLogin();
            gotoDetailViewTab();
            addResource(resourceName);
            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest addResourceToRoot() - Passed ");
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest -addResourceToRoot() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResourceToRoot");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to a add resource to root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest addResourceToRoot() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResourceToRoot");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to a add resource to root:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest addResourceToRoot()- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResourceToRoot");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a resource to root:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "delete a resource from root",priority = 3)
    public void testDeleteResourceFromRoot() throws Exception {
        try {
            userLogin();
            gotoDetailViewTab();

            driver.findElement(By.id("actionLink2")).click();
            Thread.sleep(2000L);
            driver.findElement(By.xpath("//tr[9]/td/div/a[3]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete root resource pop-up dialog title fail:");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/root_resource' permanently?"), "Delete root resource pop-up message fail :");
            selenium.click("//button");
            Thread.sleep(2000L);
            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest deleteResourceFromRoot() - Passed ");
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest -deleteResource() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_deleteResource");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to delete a resource from root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest deleteResource() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_deleteResource");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to delete a resource from root:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest deleteResource()- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_deleteResource");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to delete a resource from root:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a comment to root",priority = 4)
    public void testAddCommentToRoot() throws Exception, AssertionFailedError {
        String comment = "rootcomment";
        try {
            userLogin();
            gotoDetailViewTab();

            //Add Comment
            driver.findElement(By.id("commentsIconMinimized")).click();
            Thread.sleep(2000L);
            driver.findElement(By.linkText("Add Comment")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("Add New Comment"), "Add comment window pop -up failed :");
            assertEquals("Add", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input"), "Add comment window  pop -up Add button failed :");
            assertEquals("Cancel", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input[2]"), "Add comment window  pop -up Cancel Button failed :");
            driver.findElement(By.id("comment")).sendKeys(comment);
            driver.findElement(By.xpath("//div[3]/div[3]/form/table/tbody/tr[2]/td/input")).click();
            Thread.sleep(5000L);
            assertTrue(selenium.isTextPresent("rootcomment \n posted on 0m ago by admin"), "root comment was  not added properly :");

            //Delete comment
            driver.findElement(By.id("closeC0")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "root Comment Delete pop-up  title failed :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this comment?"), "root Comment Delete pop-up  message failed :");
            selenium.click("//button");
            Thread.sleep(2000L);

            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest addCommentToRoot() - Passed");
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest -addCommentToRoot() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCommentToRoot");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to add a comment to root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest addCommentToRoot() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCommentToRoot");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to add a comment to root:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest addCommentToRoot()- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCommentToRoot");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a comment to root:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a tag to root",priority = 5)
    public void addTagToRoot() throws Exception {
        String tagName = "roottag";
        try {
            userLogin();
            gotoDetailViewTab();

            driver.findElement(By.id("tagsIconMinimized")).click();        //Apply Tag
            Thread.sleep(2000L);
            driver.findElement(By.linkText("Add New Tag")).click();          //click on Add New Tag
            Thread.sleep(2000L);
            driver.findElement(By.id("tfTag")).sendKeys(tagName);
            Thread.sleep(3000L);
            driver.findElement(By.xpath("//div[2]/input[3]")).click();
            Thread.sleep(5000L);
            selenium.mouseOver("//div[12]/div[3]/a");
            Thread.sleep(2000L);

            driver.findElement(By.xpath("//a[2]/img")).click();           //Delete Tag
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Tag Pop-up Title fail :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this tag?"), "Delete Tag Pop-up Message fail :");

            selenium.click("//button");                //click on "yes" button
            selenium.waitForPageToLoad("30000");
            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest addTagToRoot() - Passed ");
        } catch (AssertionFailedError e) {
            log.info("Failed to add a tag to root -Assertion Error:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addTagToRoot");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to add a tag to root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("Failed to add a tag to root - Webdriver Error:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addTagToRoot");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to add a tag to root:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add a tag to root - Exception Error:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addTagToRoot");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a tag to root:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a rating to root",priority = 6)
    public void addRatingToRoot() throws Exception, AssertionFailedError {
        try {
            userLogin();
            gotoDetailViewTab();

            // Add rating 1
            driver.findElement(By.xpath("//span/img[3]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("(1.0)"), "Rating 1 has failed :");

            // Add rating 2
            driver.findElement(By.xpath("//span/img[5]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("(2.0)"), "Rating 2 has failed :");

            // Add rating 3
            driver.findElement(By.xpath("//img[7]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("(3.0)"), "Rating 3 has failed :");

            // Add rating 4
            driver.findElement(By.xpath("//img[9]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("(4.0)"), "Rating 4 has failed :");

            // Add rating 5
            driver.findElement(By.xpath("//img[11]")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("(5.0)"), "Rating 5 has failed :");

            new GregUserLogout().userLogout(driver);
            log.info("GRegRootSeleniumTest addRatingToRoot() - Passed ");
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest -addRatingToRoot() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addRatingToRoot");
            new GregUserLogout().userLogout(driver);
            throw new AssertionFailedError("Failed to apply rating to root:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest addRatingToRoot() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addRatingToRoot");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to apply rating to root:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest addRatingToRoot()- Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addRatingToRoot");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to apply rating to root:" + e.getMessage());
        }
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();

    }


    private void gotoDetailViewTab() throws Exception, AssertionFailedError {
        try {
            driver.findElement(By.linkText("Browse")).click();    //Click on Browse link
            Thread.sleep(5000L);
            driver.findElement(By.id("stdView")).click();        //Go to Detail view Tab
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("Browse"), "Browse Detail View Page fail :");
            assertTrue(selenium.isTextPresent("Metadata"), "Browse Detail View Page fail Metadata:");
            assertTrue(selenium.isTextPresent("Entries"), "Browse Detail View Page Entries fail :");
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest - gotoDetailViewTab() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_gotoDetailViewTab");
            throw new AssertionFailedError("Failed to goto Detail View Tab:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest-gotoDetailViewTab() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_gotoDetailViewTab");
            throw new WebDriverException("Failed to goto Detail View Tab:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest-gotoDetailViewTab() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_gotoDetailViewTab");
            throw new Exception("Failed to goto Detail View Tab:" + e.getMessage());
        }
    }


    private void addCollection(String collectionPath) throws Exception {
        try {
            driver.findElement(By.linkText("Add Collection")).click();
            Thread.sleep(3000L);
            driver.findElement(By.id("collectionName")).sendKeys(collectionPath);
            driver.findElement(By.id("colDesc")).sendKeys("Selenium Test");
            driver.findElement(By.xpath("//div[7]/form/table/tbody/tr[5]/td/input")).click();          //Click on Add button
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add new Collection pop -up failed :");
            assertTrue(selenium.isTextPresent("Successfully added new collection."), "Add new Collection pop -up failed :");
            selenium.click("//button");                           //click on OK button
        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest - Failed to create collection Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollection");
            throw new AssertionFailedError("Failed to create collection:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest-Failed to create collection:- WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollection");
            throw new WebDriverException("Failed to create collection:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest-Failed to create collection:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addCollection");
            throw new Exception("Failed to create collection:" + e.getMessage());
        }
    }


    private void addResource(String resourceName) throws Exception {
        try {
            driver.findElement(By.linkText("Add Resource")).click();
            Thread.sleep(2000L);
            assertTrue(selenium.isTextPresent("Add Resource"), "Add new resource page failed :");
            //select create text content
            selenium.select("id=addMethodSelector", "label=Create Text content");
            selenium.click("css=option[value=\"text\"]");
            Thread.sleep(3000L);
            // Enter name
            driver.findElement(By.id("trFileName")).sendKeys(resourceName);
            driver.findElement(By.id("trMediaType")).sendKeys("txt");
            driver.findElement(By.id("trDescription")).sendKeys("selenium test resource");
            driver.findElement(By.id("trPlainContent")).sendKeys("selenium test123");
            // Click on Add button
            driver.findElement(By.xpath("//tr[4]/td/form/table/tbody/tr[6]/td/input")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add Resource pop-up message title fail :");
            assertTrue(selenium.isTextPresent("Successfully added Text content."), "Add Resource pop-up message fail :");
            //Click on OK button
            driver.findElement(By.xpath("//button")).click();
            Thread.sleep(2000L);

        } catch (AssertionFailedError e) {
            log.info("GRegRootSeleniumTest- addResource() Assertion Failure ::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResource");
            throw new AssertionFailedError("Failed to create resource:" + e.getMessage());
        } catch (WebDriverException e) {
            log.info("GRegRootSeleniumTest-addResource() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResource");
            throw new WebDriverException("Failed to create resource:" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegRootSeleniumTest-addResource() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegRootSeleniumTest_addResource");
            throw new Exception("Failed to create resource:" + e.getMessage());
        }
    }

    private void userLogin() {
        new GregUserLogin().userLogin(driver, username, password);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"), "GReg Home page not present :");
    }


}
