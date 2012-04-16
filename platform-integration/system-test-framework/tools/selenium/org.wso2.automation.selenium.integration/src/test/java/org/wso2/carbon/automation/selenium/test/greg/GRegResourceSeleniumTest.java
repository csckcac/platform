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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebDriverException;


import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;


import java.net.MalformedURLException;
import java.util.Calendar;


public class GRegResourceSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegResourceSeleniumTest.class);
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

    @Test(groups = {"wso2.greg"}, description = "add a resource to a collection", priority = 1)
    public void testAddResourceToCollection() throws Exception {
        String collectionPath = "/selenium_root/resource_root/resource/a1";
        String resourceName = "res1";


        try {
            userLogin();
            gotoDetailViewTab();
            int resourceId = getResourceId("selenium_root");
            if (resourceId != 0) {
                deleteResourceFromBrowser(resourceId);
            }
            selenium.waitForPageToLoad("30000");
            addCollection(collectionPath);   //Create Collection  1
            Thread.sleep(2000L);
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection1 does not Exists :");

            addResource(resourceName);          //add Resource

            findLocation(collectionPath);
            assertTrue(selenium.isTextPresent("res1"), "Resource res1 does not Exists:");
            new GregUserLogout().userLogout(driver);
            log.info("********GRegResourceSeleniumTest addResourceToCollection() - Passed ***********");
        } catch (WebDriverException e) {
            log.info("Failed to add a resource to collection:" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg",
                                                      "GRegResourceSeleniumTest_addResourceToCollection");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new WebDriverException("Failed to add a resource to collection:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add a resource to collection :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg",
                                                      "GRegResourceSeleniumTest_addResourceToCollection");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(4000L);
            throw new Exception("Failed to add a resource to collection:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a comment to a resource", priority = 2)
    public void testAddCommentToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/comment/a1";
        String resourceName = "res1";
        String comment = "resourceComment";

        try {
            userLogin();
            gotoDetailViewTab();

            addCollection(collectionPath);           //Create Collection  1
            Thread.sleep(2000L);
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");

            //add Resource
            addResource(resourceName);

            findLocation(collectionPath + "/" + resourceName);

            if (waitForElement("//*[@id=\"commentsIconMinimized\"]")) {
                driver.findElement(By.id("commentsIconMinimized")).click();
            } else {
                driver.findElement(By.id("commentsIconExpanded")).click();
            }

            Thread.sleep(2000L);
            driver.findElement(By.linkText("Add Comment")).click();
            Thread.sleep(5000L);
            assertTrue(selenium.isTextPresent("Add New Comment"),
                       "Add comment window pop -up title failed :");
            //div[15]/div/div[3]/div[3]/form/table/tbody/tr[2]/td/input
            assertEquals("Add", selenium.getValue("//div[15]/div/div[3]/div[3]/form/table/tbody/" +
                                                  "tr[2]/td/input"),
                         "Add comment window  button add not present failed :");
            assertEquals("Cancel", selenium.getValue("//div[15]/div/div[3]/div[3]/form/table/" +
                                                     "tbody/tr[2]/td/input[2]"),
                         "Add comment window  pop -up failed :");
            driver.findElement(By.id("comment")).sendKeys(comment);
            Thread.sleep(3000L);
            driver.findElement(By.xpath("//div[15]/div/div[3]/div[3]/form/table/tbody/tr[2]/td/input")).click();
            Thread.sleep(5000L);
            assertEquals("resourceComment \n posted on 0m ago by admin",
                         selenium.getText("//div[4]/div/div[2]/table/tbody/tr/td/div"),
                         "Added comment failed :");

            //Delete Comment
            driver.findElement(By.id("closeC0")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Comment Delete pop-up  failed :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this comment?"),
                       "Comment Delete pop-up  failed :");
            selenium.click("//button");
            Thread.sleep(2000L);

            new GregUserLogout().userLogout(driver);         //signout
            log.info("********GRegResourceSeleniumTest addCommentToResource() - Passed ***********");
        } catch (WebDriverException e) {
            log.info("Failed to add a comment to resource :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg",
                                                      "GRegResourceSeleniumTest_addCommentToResource");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(5000L);
            throw new WebDriverException("Failed to add a comment to resource:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add a comment to resource :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg",
                                                      "GRegResourceSeleniumTest_addCommentToResource");
            new GregUserLogout().userLogout(driver);
            Thread.sleep(5000L);
            throw new Exception("Failed to add a comment to resource:" + e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "add a tag to resource", priority = 3)
    public void testAddTagToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/tag/a1";
        String resourceName = "res1";
        String tagName = "resource";

        try {
            userLogin();
            gotoDetailViewTab();

            //Create Collection  1
            addCollection(collectionPath);
            Thread.sleep(2000L);
            assertEquals(collectionPath, selenium.getValue("//input"), "New Created Collection does not Exists :");

            //add Resource
            addResource(resourceName);

            findLocation(collectionPath + "/" + resourceName);

            //Apply Tag
            driver.findElement(By.id("tagsIconMinimized")).click();
            Thread.sleep(2000L);
            //click on Add New Tag
            driver.findElement(By.linkText("Add New Tag")).click();
            Thread.sleep(2000L);
            driver.findElement(By.id("tfTag")).sendKeys(tagName);
            Thread.sleep(3000L);
            driver.findElement(By.xpath("//div[2]/input[3]")).click();
            Thread.sleep(5000L);
            selenium.mouseOver("//div[3]/a");
            Thread.sleep(2000L);
            //Delete Tag
            driver.findElement(By.xpath("//a[2]/img")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Tag Pop-up Title fail :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this tag?"), "Delete Tag Pop-up Message fail :");
            //click on "yes" button
            selenium.click("//button");
            selenium.waitForPageToLoad("30000");
            //signout
            new GregUserLogout().userLogout(driver);
            log.info("********GRegResourceSeleniumTest addTagToResource() - Passed ***********");

        } catch (WebDriverException e) {
            log.info("Failed to add a tag to resource: :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addTagToResource");
            new GregUserLogout().userLogout(driver);
            throw new WebDriverException("Failed to add a tag to resource:" + e.getMessage());
        } catch (Exception e) {
            log.info("Failed to add a tag to resource::" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addTagToResource");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a tag to resource:" + e.getMessage());
        }

    }

    private void userLogin() {
        new GregUserLogin().userLogin(driver, username, password);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"),
                   "GReg Home page not present :");
    }


    private void gotoDetailViewTab() throws Exception {
        try {
            //Click on Browse link
            driver.findElement(By.linkText("Browse")).click();
            Thread.sleep(5000L);
            //Go to Detail view Tab
            driver.findElement(By.id("stdView")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("Browse"), "Browse Detail View Page fail :");
            assertTrue(selenium.isTextPresent("Metadata"), "Browse Detail View Page Metadata fail  :");
            assertTrue(selenium.isTextPresent("Entries"), "Browse Detail View Page fail :");

        } catch (WebDriverException e) {
            log.info("GRegResourceSeleniumTestt-gotoDetailViewTab() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_gotoDetailViewTab");
            throw new WebDriverException("GRegResourceSeleniumTestt-gotoDetailViewTab():" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegResourceSeleniumTest-gotoDetailViewTab() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_gotoDetailViewTab");
            throw new Exception("GRegResourceSeleniumTestt-gotoDetailViewTab():" + e.getMessage());
        }
    }

    private void findLocation(String path) throws Exception {
        try {
            driver.findElement(By.id("uLocationBar")).clear();
            driver.findElement(By.id("uLocationBar")).sendKeys(path);
            driver.findElement(By.xpath("//input[2]")).click();
            Thread.sleep(3000L);
        } catch (WebDriverException e) {
            log.info("GRegResourceSeleniumTest-findLocation() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_findLocation");
            throw new WebDriverException("GRegResourceSeleniumTest-findLocation() :" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegResourceSeleniumTest-findLocation() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_findLocation");
            throw new Exception("GRegResourceSeleniumTest-findLocation() :" + e.getMessage());
        }
    }

    private void addCollection(String collectionPath) throws Exception {

        try {
            driver.findElement(By.linkText("Add Collection")).click();
            Thread.sleep(3000L);
            driver.findElement(By.id("collectionName")).sendKeys(collectionPath);
            driver.findElement(By.id("colDesc")).sendKeys("Selenium Test");
            //Click on Add button
            driver.findElement(By.xpath("//div[7]/form/table/tbody/tr[5]/td/input")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add new Collection pop -up Title failed:");
            assertTrue(selenium.isTextPresent("Successfully added new collection."), "Add new Collection pop -up Message failed:");
            //click on OK button
            selenium.click("//button");
        } catch (WebDriverException e) {
            log.info("GRegCollectionSeleniumTest-addCollection() Fail  :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegCollectionSeleniumTest_addCollection");
            throw new WebDriverException("GRegCollectionSeleniumTest-addCollection() Fail  :" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegCollectionSeleniumTest-addCollection() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegCollectionSeleniumTest_addCollection");
            throw new Exception("GRegCollectionSeleniumTest-addCollection() Fail  :" + e.getMessage());
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
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add Resource pop-up title fail :");
            assertTrue(selenium.isTextPresent("Successfully added Text content."), "Add Resource pop-up message fail :");
            //Click on OK button
            driver.findElement(By.xpath("//button")).click();
            Thread.sleep(2000L);
        } catch (WebDriverException e) {
            log.info("GRegResourceSeleniumTest-addResource() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addResource");
            throw new WebDriverException("GRegResourceSeleniumTest-addResource()  :" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegResourceSeleniumTest-addResource() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addResource");
            throw new Exception("GRegResourceSeleniumTest-addResource()  :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a life cycle to resource", priority = 4)
    public void testAddLifeCycleToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/lifecycle/a1";
        String resourceName = "res1";

        try {
            userLogin();
            gotoDetailViewTab();

            //Create Collection  1
            addCollection(collectionPath);
            Thread.sleep(2000L);
            assertEquals(collectionPath, selenium.getValue("//input"), "New Created Collection does not Exists :");

            //add Resource
            addResource(resourceName);

            findLocation(collectionPath + "/" + resourceName);

            //Add LifeCycle
            selenium.click("id=lifecycleIconMinimized");
            Thread.sleep(3000L);
            selenium.click("link=Add Lifecycle");
            Thread.sleep(4000L);
            assertTrue(selenium.isTextPresent("Enable Lifecycle"), "Enable LifeCycle Text does not appear :");
            assertEquals("Add", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input"),
                         "Lifecycle Add Button does not appear :");
            assertEquals("Cancel", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input[2]"), "Lifecycle Cancel button does not appear");
            // select ServiceLifeCycle
            selenium.click("//div[3]/div[3]/form/table/tbody/tr[2]/td/input");
            Thread.sleep(5000L);
            assertTrue(selenium.isTextPresent("Development"), "Service Life cycle default state does not exists:");

            promoteState();
            assertTrue(selenium.isTextPresent("Testing"), "Service Life Cycle Testing state fail:");

            promoteState();
            assertTrue(selenium.isTextPresent("Production"), "Service Life Cycle Production State fail:");

            //Delete LifeCycle
            driver.findElement(By.xpath("//div[3]/div[2]/table/tbody/tr/td/div/a")).click();
            Thread.sleep(3000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Lifecycle pop-up Title fail:");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'ServiceLifeCycle' permanently?"), "Delete Lifecycle pop-up message fail :");
            driver.findElement(By.xpath("//button")).click();
            //signout
            new GregUserLogout().userLogout(driver);
            log.info("********GRegResourceSeleniumTest addLifeCycleToResource() - Passed ***********");
        } catch (WebDriverException e) {
            log.info("Failed to add a life cycle to resource :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addLifeCycleToResource");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a life cycle to resource :" + e.getMessage());

        } catch (Exception e) {
            log.info("Failed to add a life cycle to resource :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_addLifeCycleToResource");
            new GregUserLogout().userLogout(driver);
            throw new Exception("Failed to add a life cycle to resource  :" + e.getMessage());
        }

    }

    //
    private void promoteState() throws Exception {
        try {
            driver.findElement(By.id("option0")).click();
            Thread.sleep(2000L);
            driver.findElement(By.id("option1")).click();
            Thread.sleep(2000L);
            driver.findElement(By.id("option2")).click();
            Thread.sleep(2000L);
            // promote
            driver.findElement(By.xpath("//tr[2]/td/div/input")).click();
            Thread.sleep(4000L);
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Life Cycle Promote pop-up Title fail :");
            assertTrue(selenium.isTextPresent("Successfully Promoted"), "Life Cycle promote pop-up message fail :");
            // click on OK button
            selenium.click("css=button[type=\"button\"]");
            Thread.sleep(3000L);

        } catch (WebDriverException e) {
            log.info("GRegResourceSeleniumTest-promoteState() - WebDriver Exception :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_promoteState");
            throw new Exception("GRegResourceSeleniumTest-addResource()  :" + e.getMessage());
        } catch (Exception e) {
            log.info("GRegResourceSeleniumTest-promoteState() Fail :" + e.getMessage());
            new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegResourceSeleniumTest_promoteState");
            throw new Exception("GRegResourceSeleniumTest-addResource()  :" + e.getMessage());
        }
    }

    private boolean waitForElement(String elementName) throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        while (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()))
               < (120 * 1000)) {
            if (selenium.isElementPresent(elementName)) {
                return true;
            }
            Thread.sleep(1000);
            log.info("waiting for element :" + elementName);
        }
        return false;
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        userLogin();
        gotoDetailViewTab();
        int resourceId = getResourceId("selenium_root");
        if (resourceId != 0) {
            deleteResourceFromBrowser(resourceId);
        }
        driver.quit();

    }

    private int getResourceId(String resourceName) {
        int pageCount = 10;
        int id = 0;
        for (int i = 1; i <= pageCount; i++) {
            if (driver.getPageSource().contains(resourceName)) {
                if (driver.findElement(By.xpath("//*[@id=\"resourceView" + i + "\"]")).getText().equals(resourceName)) {
                    id = i;
                    break;
                }
            }
        }
        return id;
    }

    private void deleteResourceFromBrowser(int resourceRowId) {
        if (resourceRowId != 0) {
            driver.findElement(By.id("actionLink" + resourceRowId)).click();
            selenium.waitForPageToLoad("30000");
            resourceRowId = ((resourceRowId - 1) * 7) + 2;
            driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                                        "table/tbody/tr/td/div[2]/div[3]/div[3]/div[9]/table/tbody/tr[" + resourceRowId + "]" +
                                        "/td/div/a[3]")).click();
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Resource Delete pop-up  failed :");
            assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete"), "Resource Delete pop-up  failed :");
            selenium.click("//button");
            selenium.waitForPageToLoad("30000");
        }
    }
}
