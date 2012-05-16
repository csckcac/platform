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
import org.testng.annotations.*;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class GRegStratosCollectionSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosCollectionSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    long sleeptime = 5000;
    long sleeptime1 = 3000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(ProductConstant.GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

    @Test(groups = {"wso2.greg"}, description = "add a collection tree ", priority = 1)
    public void testAddCollectionTree() throws Exception {
        String collectionPath = "/selenium_root/collection_root/c1/c2";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            userLogout();
            log.info("********GReg Stratos Add New Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add New Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add New Collection Test Failed :" + e);
        } catch (WebDriverException e) {
            log.info("Add New Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add New Collection Test Failed :" + e);
        } catch (Exception e) {
            log.info("Add New Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Add New Collection Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a comment to collection ", priority = 2)
    public void testAddComment() throws Exception {
        String collectionPath = "/selenium_root/collection_root/comment/a1";
        String comment = "Collection Comment1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");

            addComment(comment);
            deleteComment();
            userLogout();
            log.info("********GReg Stratos Add a Comment to Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add a Comment to Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add a Comment to Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add a Comment to Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add a Comment to Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add a Comment to Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Add a Comment to Collection Test Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "apply a tag to a collection", priority = 3)
    public void testAddTagToCollection() throws Exception {
        String collectionPath = "/selenium_root/collection_root/tag/a1";
        String tag = "Collection_tag1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);      //Create Collection  1
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            addTag(tag);
            selenium.mouseOver("//div[12]/div[3]/a");
            Thread.sleep(sleeptime);
            deleteTag();
            userLogout();
            log.info("********GReg Stratos Apply Tag to Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Apply Tag to Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Apply Tag to Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Apply Tag to Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Apply Tag to Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Apply Tag to Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Apply Tag to Collection Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a lifecycle to a collection", priority = 4)
    public void testAddLifeCycleToCollection() throws Exception {
        String collectionPath = "/selenium_root/collection_root/lifecycle/a1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            addServiceLifeCycle();
            promoteState();
            assertTrue(selenium.isTextPresent("Testing"),
                    "Service Life cycle Testing State fail:");
            promoteState();
            assertTrue(selenium.isTextPresent("Production"),
                    "Service Life Cycle Production State fail:");
            //Delete LifeCycle
            deleteServiceLifeCycle();
            userLogout();            //Sign out
            log.info("********GReg Stratos Add Life Cycle to Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add Life Cycle to Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add Life Cycle to Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add Life Cycle to Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add Life Cycle to Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add Life Cycle to Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Add Life Cycle to Collection Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a rating to a collection", priority = 5)
    public void testAddRatingToCollection() throws Exception {
        String collectionPath = "/selenium_root/collection_root/rating/a1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            addRating();
            userLogout();
            log.info("********GReg Stratos Add Rating To Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add Rating To Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add Rating To Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add Rating To Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add Rating To Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add Rating To Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Add Rating To Collection Test Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "rename a collection collection", priority = 6)
    public void testRenameCollection() throws Exception {
        String collectionPath = "/selenium_root/collection_root/rename/a1";
        String rename = "renameda1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTime("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            findLocation("/selenium_root/collection_root/rename");
            assertTrue(selenium.isTextPresent("a1"), "Collection a1 does not Exists :");
            renameCollection(rename);
            userLogout();
            log.info("********GReg Stratos Rename Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Rename Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Rename Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Rename Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Rename Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Rename Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Rename Collection Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "move a collection", priority = 7)
    public void testMoveCollection() throws Exception {
        String collectionPath1 = "/selenium_root/collection_root/move/collection1/a1";
        String collectionPath2 = "/selenium_root/collection_root/move/collection2/b1";
        String movePath = "/selenium_root/collection_root/move/collection2/b1";
        String collection3 = "abc";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);   //Create Collection  1
            waitTime("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                    "New Created Collection1 does not Exists :");
            driver.findElement(By.xpath("//input")).clear();
            findLocation("/");
            addCollection(collectionPath2);          //Create Collection  2
            waitTime("//input");
            assertEquals(collectionPath2, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            findLocation("/selenium_root/collection_root/move/collection1");
            assertTrue(selenium.isTextPresent("a1"), "a1 Collection does not Exists :");
            moveCollection(movePath);
            Thread.sleep(sleeptime);
            findPath(movePath);
            Thread.sleep(sleeptime);
            addCollection(collection3);
            findPath(movePath);
            assertTrue(selenium.isTextPresent("a1"), "Moved Collection a1 does not Exists :");
            findPath("/selenium_root/collection_root/move/collection1");
            assertFalse(selenium.isTextPresent("a1"), "a1 Collection has not been moved :");
            userLogout();
            log.info("********GReg Stratos Move a Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Move a Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Move a Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Move a Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Move a Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Move a Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Move a Collection Test Failed :" + e);
        }
    }


    //    @Test(groups = {"wso2.greg"}, description = "copy a collection", priority = 8)
    public void testcopyCollection() throws Exception {
        String collectionPath1 = "/selenium_root/collection_root/copy/collection1/a1";
        String collectionPath2 = "/selenium_root/collection_root/copy/collection2/b1";
        String collectionPath3 = "/abc";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);            //Create Collection  1
            waitTime("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                    "New Created Collection1 does not Exists :");
            findLocation("/");
            addCollection(collectionPath2);             //Create Collection  2
            waitTime("//input");
            assertEquals(collectionPath2, selenium.getValue("//input"),
                    "New Created Collection2 does not Exists :");
            findLocation("/selenium_root/collection_root/copy/collection1");
            copyCollection(collectionPath2);
            findPath(collectionPath2);
            addCollection(collectionPath3); //this is add becoz carbon 3.2.2 has a cashing issue
            findPath(collectionPath2);
            assertTrue(selenium.isTextPresent("a1"), "Copied a1 collection does not Exists :");
            findLocation("/selenium_root/collection_root/copy/collection1");
            assertTrue(selenium.isTextPresent("a1"), "Copied a1 collection does not Exists :");
            userLogout();
            log.info("********GReg Stratos Copy Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Copy Collection Test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Copy Collection Test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Copy Collection Test Failed :" + e);
            userLogout();
            throw new WebDriverException("Copy Collection Test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Copy Collection Test Failed :" + e);
            userLogout();
            throw new Exception("Copy Collection Test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "copy a collection", priority = 9)
    public void testDeleteCollection() throws Exception {
        String collectionPath1 = "/selenium_root/collection_root/delete/collection1/a1";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);               //Create Collection  1
            waitTime("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                    "New Created Collection does not Exists :");
            findLocation("/selenium_root/collection_root/delete/");
            deleteCollection();
            userLogout();
            log.info("********GReg Stratos Delete Collection Test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Delete Collection Test  Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Delete Collection Test  Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Delete Collection Test  Failed :" + e);
            userLogout();
            throw new WebDriverException("Delete Collection Test  Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Delete Collection Test  Failed :" + e);
            userLogout();
            throw new Exception("Delete Collection Test  Failed :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteCollection() throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        waitTime("//a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "Delete Collection pop-up Title does not appear :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete " +
                "'/selenium_root/collection_root/delete/collection1'" +
                " permanently?"), "Delete Collection pop-up message" +
                " fail:");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void copyCollection(String collectionPath2) throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        waitTime("//a[4]");
        driver.findElement(By.linkText("Copy")).click();
        waitTime("//td/table/tbody/tr/td[2]/input");
        driver.findElement(By.id("copy_destination_path1")).sendKeys(collectionPath2);
        //click on copy button
        driver.findElement(By.xpath("//td/table/tbody/tr[2]/td/input")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(driver.getPageSource().contains("WSO2 Carbon"),
                "Copy Collection pop-up Title Failed:");
        assertTrue(driver.getPageSource().contains("Successfully copied collection."),
                "Copy Collection pop-up Message Failed :");
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleeptime);
    }

    private void moveCollection(String movePath) throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        waitTime("//tr[2]/td/div/a[2]");
        driver.findElement(By.linkText("Move")).click();
        waitTime("//tr[5]/td/table/tbody/tr/td[2]/input");
        driver.findElement(By.id("move_destination_path1")).sendKeys(movePath);
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//tr[5]/td/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
        waitTime("//body/div[3]/div/div");
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Move pop-up window Title fail");
        assertTrue(selenium.isTextPresent("Successfully moved collection."),
                "Move pop-up window message fail");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void renameCollection(String rename) throws InterruptedException {
        driver.findElement(By.id("actionLink1")).click();
        waitTime("//tr[2]/td/div/a");
        driver.findElement(By.linkText("Rename")).click();
        waitTime("//tr[6]/td/table/tbody/tr/td/input");
        driver.findElement(By.id("resourceEdit1")).clear();
        driver.findElement(By.id("resourceEdit1")).sendKeys(rename);
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//tr[6]/td/table/tbody/tr[2]/td/input")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "rename pop-up title fail :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent(rename), "Renamed Collection does not Exists :");
    }

    private void findPath(String path) throws InterruptedException {
        driver.findElement(By.id("uLocationBar")).clear();
        Thread.sleep(sleeptime);
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//input[2]")).click();
        Thread.sleep(sleeptime);
    }


    private void findLocation(String path) throws Exception {
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        driver.findElement(By.xpath("//input[2]")).click();
        waitTime("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
    }

    private void addRating() throws InterruptedException {
        // Add rating 1
        driver.findElement(By.xpath("//img[3]")).click();
        Thread.sleep(sleeptime1);
        assertTrue(selenium.isTextPresent("(1.0)"), "Rating 1 has failed :");
        // Add rating 2
        driver.findElement(By.xpath("//img[5]")).click();
        Thread.sleep(sleeptime1);
        assertTrue(selenium.isTextPresent("(2.0)"), "Rating 2 has failed :");
        // Add rating 3
        driver.findElement(By.xpath("//img[7]")).click();
        Thread.sleep(sleeptime1);
        assertTrue(selenium.isTextPresent("(3.0)"), "Rating 3 has failed :");
        // Add rating 4
        driver.findElement(By.xpath("//img[9]")).click();
        Thread.sleep(sleeptime1);
        assertTrue(selenium.isTextPresent("(4.0)"), "Rating 4 has failed :");
        // Add rating 5
        driver.findElement(By.xpath("//img[11]")).click();
        Thread.sleep(sleeptime1);
        assertTrue(selenium.isTextPresent("(5.0)"), "Rating 5 has failed :");
    }

    private void deleteServiceLifeCycle() throws InterruptedException {
        driver.findElement(By.xpath("//div[3]/div[2]/table/tbody/tr/td/div/a")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Lifecycle pop-up Title fail:");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'ServiceLifeCycle'" +
                " permanently?"),
                "Delete Lifecycle pop-up message fail :");
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleeptime);
    }

    private void promoteState() throws Exception {
        driver.findElement(By.id("option0")).click();
        Thread.sleep(sleeptime1);
        driver.findElement(By.id("option1")).click();
        Thread.sleep(sleeptime1);
        driver.findElement(By.id("option2")).click();
        Thread.sleep(sleeptime1);
        // promote
        driver.findElement(By.xpath("//tr[2]/td/div/input")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "Life Cycle Promote pop-up Title fail :");
        assertTrue(selenium.isTextPresent("Successfully Promoted"),
                "Life Cycle promote pop-up message fail :");
        // click on OK button
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void addServiceLifeCycle() throws InterruptedException {
        driver.findElement(By.id("lifecycleIconMinimized")).click();
        waitTime("//div[11]/div[3]/div[2]/a");
        driver.findElement(By.linkText("Add Lifecycle")).click();
        waitTime("//div[3]/div[3]/form/table/tbody/tr[2]/td/input");
        assertTrue(selenium.isTextPresent("Enable Lifecycle"),
                "Enable LifeCycle Text does not appear :");
        assertEquals("Add", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input"),
                "Lifecycle Add Button does not appear :");
        assertEquals("Cancel", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input[2]"),
                "Lifecycle Cancel button does not appear");
        selenium.click("//div[3]/div[3]/form/table/tbody/tr[2]/td/input");
        waitTime("//tr[2]/td/div/input");
        assertTrue(selenium.isTextPresent("Development"),
                "Service Life cycle default state does not exists:");
    }


    private void deleteTag() throws InterruptedException {
        driver.findElement(By.xpath("//div[12]/div[3]/a[2]/img")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Tag Pop-up Title fail :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this tag?"),
                "Delete Tag Pop-up Message fail :");
        //click on "yes" button
        selenium.click("//button");
        Thread.sleep(sleeptime1);
    }

    private void addTag(String tag) throws InterruptedException {
        driver.findElement(By.id("tagsIconMinimized")).click();
        waitTime("//div[12]/div/a");
        //click on Add New Tag
        driver.findElement(By.linkText("Add New Tag")).click();
        waitTime("//div[2]/input[2]");
        driver.findElement(By.id("tfTag")).sendKeys(tag);
        driver.findElement(By.xpath("//div[2]/input[3]")).click();
        waitTime("//div[12]/div[3]/a");
    }

    private void deleteComment() throws InterruptedException {
        driver.findElement(By.id("closeC0")).click();
        waitTime("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Comment Delete pop-up title failed :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this comment?"),
                "Comment Delete pop-up  message failed :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }

    private void addComment(String comment) throws InterruptedException {
        driver.findElement(By.id("commentsIconMinimized")).click();
        waitTime("//div[12]/div/div[3]/div[2]/a");
        driver.findElement(By.linkText("Add Comment")).click();
        waitTime("//div[3]/form/table/tbody/tr/td/textarea");
        assertTrue(selenium.isTextPresent("Add New Comment"),
                "Add comment window pop -up failed :");
        assertEquals("Add", selenium.getValue("//div[12]/div/div[3]/div[3]/form/table/tbody/" +
                "tr[2]/td/input"), "Add comment window  pop -up failed :");
        assertEquals("Cancel", selenium.getValue("//div[12]/div/div[3]/div[3]" +
                "/form/table/tbody/tr[2]/td/input[2]"),
                "Add comment window  pop -up failed :");
        driver.findElement(By.id("comment")).sendKeys(comment);
        driver.findElement(By.xpath("//div[12]/div/div[3]/div[3]/form/table/tbody/" +
                "tr[2]/td/input")).click();
        waitTime("//div[4]/div/div[2]/table/tbody/tr/td/div");
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTime("//a[2]/img");
    }

    private void gotoDetailViewTab() throws Exception, AssertionFailedError {
        driver.findElement(By.linkText("Browse")).click();    //Click on Browse link
        waitTime("//a[2]");
        driver.findElement(By.id("stdView")).click();        //Go to Detail view Tab
        waitTime("//div[3]/div[2]/a");
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("Browse"), "Browse Detail View Page fail :");
        assertTrue(selenium.isTextPresent("Metadata"), "Browse Detail View Page fail Metadata:");
        assertTrue(selenium.isTextPresent("Entries"), "Browse Detail View Page Entries fail :");

    }

    private void addCollection(String collectionPath) throws Exception {
        driver.findElement(By.linkText("Add Collection")).click();
        waitTime("//div[7]/form/table/tbody/tr[2]/td[2]/input");
        driver.findElement(By.id("collectionName")).sendKeys(collectionPath);
        driver.findElement(By.id("colDesc")).sendKeys("Selenium Test");
        //Click on Add button
        driver.findElement(By.xpath("//div[7]/form/table/tbody/tr[5]/td/input")).click();
        waitTime("//button");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "Add new Collection pop -up title failed :");
        assertTrue(selenium.isTextPresent("Successfully added new collection."),
                "Add new Collection pop -up message failed :");
        //click on OK button
        selenium.click("//button");
        Thread.sleep(sleeptime);

    }


    private void waitTime(String elementName) throws InterruptedException {
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
