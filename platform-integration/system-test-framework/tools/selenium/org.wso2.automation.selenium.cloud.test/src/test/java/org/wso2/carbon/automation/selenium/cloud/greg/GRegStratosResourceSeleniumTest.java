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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.*;



public class GRegStratosResourceSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosResourceSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    long sleeptime = 4000;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.GREG_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }


    @Test(groups = {"wso2.greg"}, description = "add a resource to a collection", priority = 1)
    public void testaddResourceToCollection() throws Exception {
        String collectionPath = "/selenium_root/resource_root/resource/a1";
        String resourceName = "res1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);   //Create Collection  1
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection1 does not Exists :");
            addResource(resourceName);          //add Resource
            findLocation(collectionPath);
            assertTrue(selenium.isTextPresent("res1"), "Resource res1 does not Exists:");
            userLogout();
            log.info("********GReg Stratos - Add Resource To a Collection test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Add Resource To a Collection test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Add Resource To a Collection test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Add Resource To a Collection test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Add Resource To a Collection test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Add Resource To a Collection test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Add Resource To a Collection test Failed :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "add a comment to a resource", priority = 2)
    public void testaddCommentToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/comment/a1";
        String resourceName = "res1";
        String comment = "resourceComment";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName);
            findLocation(collectionPath + "/" + resourceName);
            addComment(comment);
            deleteComment();
            userLogout();
            log.info("********GReg Stratos - Add Comment To a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add Comment To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Add Comment To a Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Add Comment To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Add Comment To a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Add Comment To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Add Comment To a Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a tag to resource", priority = 3)
    public void testAddTagToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/tag/a1";
        String resourceName = "res1";
        String tagName = "resource";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName);
            findLocation(collectionPath + "/" + resourceName);
            applyTag(tagName);
            selenium.mouseOver("//div[3]/a");
            waitTimeforElement("//a[2]/img");
            deleteTag();
            userLogout();
            log.info("********GReg Stratos- Add Tag To a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add Tag To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Add Tag To a Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Add Tag To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Add Tag To a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Add Tag To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Add Tag To a Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a life cycle to resource", priority = 4)
    public void testAddLifeCycleToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/lifecycle/a1";
        String resourceName = "res1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName);
            findLocation(collectionPath + "/" + resourceName);
            addServiceLifeCycle();
            promoteState();
            assertTrue(selenium.isTextPresent("Testing"),
                       "Service Life Cycle Testing state fail:");
            promoteState();
            assertTrue(selenium.isTextPresent("Production"),
                       "ServiceLifeCycle Production State fail:");
            deleteServiceLifeCycle();
            userLogout();
            log.info("********GReg Stratos -Add Life Cycle To a Resource test - Passed **********");
        } catch (AssertionFailedError e) {
            log.info("Add Life Cycle To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Add Life Cycle To a Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Add Life Cycle To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Add Life Cycle To a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Add Life Cycle To a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Add Life Cycle To a Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a rating to resource", priority = 5)
    public void testaddRatingToResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/rating/a1";
        String resourceName = "res1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName);
            findLocation(collectionPath + "/" + resourceName);
            addRating();
            userLogout();
            log.info("********GReg Stratos - Add a Rating To a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Add a Rating To a Resource Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Add a Rating To a Resource Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Add a Rating To a Resource Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Add a Rating To a Resource Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Add a Rating To a Resource Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Add a Rating To a Resource Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add a rating to resource", priority = 6)
    public void testRenameResource() throws Exception {
        String collectionPath = "/selenium_root/resource_root/rename/a1";
        String resourceName = "res1";
        String rename = "rename_res1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            waitTimeforElement("//input");
            assertEquals(collectionPath, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName);
            findLocation(collectionPath);
            renameResource(rename);
            assertTrue(selenium.isTextPresent(rename), "Renamed Resource does not Exists :");
            userLogout();
            log.info("********GReg Stratos - Rename a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Rename a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Rename a Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Rename a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Rename a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Rename a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Rename a Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "move a resource", priority = 7)
    public void testMoveResource() throws Exception {
        String collectionPath1 = "/selenium_root/resource_root/move1/a1";
        String collectionPath2 = "/selenium_root/resource_root/move2/b1";
        String resourceName1 = "res1";
        String resourceName2 = "res2";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);
            waitTimeforElement("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName1);
            findLocation("/");
            addCollection(collectionPath2);
            waitTimeforElement("//input");
            assertEquals(collectionPath2, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            findLocation("/selenium_root/resource_root/move1/a1/");
            moveResource(collectionPath2);
            findLocation(collectionPath2);
            addResource(resourceName2);
            findLocation(collectionPath2);
            assertTrue(selenium.isTextPresent("res1"), "Moved res1 resource does not Exists :");
            findLocation(collectionPath1);
            assertFalse(selenium.isTextPresent("res1"), "res1 resource has not been moved :");
            userLogout();
            log.info("********GReg Stratos - Move a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Move a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Move a Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Move a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Move a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Move a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Move a Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "delete a resource", priority = 8)
    public void testdeleteResource() throws Exception {
        String collectionPath1 = "/selenium_root/resource_root/delete/a1";
        String resourceName1 = "res1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);
            waitTimeforElement("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                         "New Created Collection does not Exists :");
            addResource(resourceName1);
            findLocation(collectionPath1);
            deleteResource();
            findLocation(collectionPath1);
            assertFalse(selenium.isTextPresent("res1"), "Resource res1 does Exists:");
            userLogout();
            log.info("********GReg Stratos - Delete Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Delete Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Delete Resource test Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Delete Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Delete Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Delete Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Delete Resource test Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "copy a resource", priority = 9)
    public void testcopyResource() throws Exception {
        String collectionPath1 = "/selenium_root/resource_root/copy1/a1";
        String collectionPath2 = "/selenium_root/resource_root/copy2/b1";
        String resourceName1 = "res1";
        String resourceName2 = "res2";
        String copyPath = "/selenium_root/resource_root/copy2/b1";

        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath1);
            waitTimeforElement("//input");
            assertEquals(collectionPath1, selenium.getValue("//input"),
                         "New Created Collection1 does not Exists :");
            addResource(resourceName1);
            findLocation("/");
            addCollection(collectionPath2);
            waitTimeforElement("//input");
            assertEquals(collectionPath2, selenium.getValue("//input"),
                         "New Created Collection2 does not Exists :");
            findLocation("/selenium_root/resource_root/copy1/a1/");
            copyResource(copyPath);
            findLocation(collectionPath2);
            addResource(resourceName2);
            findLocation(collectionPath2);
            assertTrue(selenium.isTextPresent("res1"),
                       "copied res1 resource does not Exists in copied to location :");
            findLocation(collectionPath1);
            assertTrue(selenium.isTextPresent("res1"), "copied resource does not exists : :");
            userLogout();
            log.info("********GReg Stratos - Copy a Resource test - Passed ***********");
        } catch (AssertionFailedError e) {
            log.info("Copy a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("Copy a Resource tes Failed :" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("Copy a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("Copy a Resource test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("Copy a Resource test Failed :" + e.getMessage());
            userLogout();
            throw new Exception("Copy a Resource test Failed :" + e.getMessage());
        }
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void deleteResource() throws InterruptedException {
        waitTimeforElement("//td/table/tbody/tr/td[2]/table/tbody/tr/td[2]/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//div/a[3]");
        driver.findElement(By.linkText("Delete")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Resource pop-up Title fail :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete '/selenium_root/" +
                                          "resource_root/delete/a1/res1' permanently?"),
                   "Delete Resource pop-up message fail :");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }


    private void copyResource(String copyPath) throws InterruptedException {
        waitTimeforElement("//td/table/tbody/tr/td[2]/table/tbody/tr/td[2]/a");
        Thread.sleep(sleeptime);
        driver.findElement(By.id("actionLink1")).click();
        Thread.sleep(sleeptime);
        waitTimeforElement("//div/a[4]");
        driver.findElement(By.linkText("Copy")).click();
        Thread.sleep(sleeptime);
        waitTimeforElement("//td/table/tbody/tr/td[2]/input");
        driver.findElement(By.id("copy_destination_path1")).sendKeys(copyPath);
        Thread.sleep(sleeptime);
        waitTimeforElement("//td/table/tbody/tr[2]/td/input");
        driver.findElement(By.xpath("//td/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "copy pop-up title fail :");
        assertTrue(selenium.isTextPresent("Successfully copied resource."),
                   "copy pop-up message fail :");
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleeptime);
    }

    private void moveResource(String collectionPath2) throws InterruptedException {
        waitTimeforElement("//td/table/tbody/tr/td[2]/table/tbody/tr/td[2]/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//tr[2]/td/div/a[2]");
        driver.findElement(By.linkText("Move")).click();
        waitTimeforElement("//tr[5]/td/table/tbody/tr/td[2]/input");
        driver.findElement(By.id("move_destination_path1")).sendKeys(collectionPath2);
        waitTimeforElement("//tr[5]/td/table/tbody/tr[2]/td/input");
        driver.findElement(By.xpath("//tr[5]/td/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Move pop-up window Title fail");
        assertTrue(selenium.isTextPresent("Successfully moved resource."),
                   "Move pop-up window message fail");
        selenium.click("//button");
        waitTimeforElement("//input");
    }


    private void renameResource(String rename) throws InterruptedException {
        waitTimeforElement("//td/table/tbody/tr/td[2]/table/tbody/tr/td[2]/a");
        driver.findElement(By.id("actionLink1")).click();
        waitTimeforElement("//tr[2]/td/div/a");
        driver.findElement(By.linkText("Rename")).click();
        waitTimeforElement("//tr[6]/td/table/tbody/tr/td/input");
        driver.findElement(By.id("resourceEdit1")).clear();
        driver.findElement(By.id("resourceEdit1")).sendKeys(rename);
        waitTimeforElement("//tr[6]/td/table/tbody/tr[2]/td/input");
        driver.findElement(By.xpath("//tr[6]/td/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Rename pop-up Title fail :");
        assertTrue(selenium.isTextPresent("Successfully renamed resource."),
                   "Rename pop-up message fail :");
        selenium.click("//button");
        waitTimeforElement("//div[9]/table/tbody/tr/td/table/tbody/tr/td/a");
    }

    private void addRating() throws InterruptedException {
        driver.findElement(By.xpath("//span/img[3]")).click();
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("(1.0)"), "Rating 1 has failed :");
        driver.findElement(By.xpath("//span/img[5]")).click();         // Add rating 2
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("(2.0)"), "Rating 2 has failed :");
        driver.findElement(By.xpath("//img[7]")).click();              // Add rating 3
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("(3.0)"), "Rating 3 has failed :");
        driver.findElement(By.xpath("//img[9]")).click();                 // Add rating 4
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("(4.0)"), "Rating 4 has failed :");
        driver.findElement(By.xpath("//img[11]")).click();                // Add rating 5
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("(5.0)"), "Rating 5 has failed :");
    }


    private void deleteServiceLifeCycle() throws InterruptedException {
        driver.findElement(By.xpath("//div[3]/div[2]/table/tbody/tr/td/div/a")).click();
        waitTimeforElement("//body/div[4]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Lifecycle pop-up Title fail:");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete'ServiceLifeCycle'" +
                                          " permanently?"), "Delete Lifecycle pop-up message fail :");
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleeptime);
        waitTimeforElement("//li[3]/a");
    }

    private void addServiceLifeCycle() throws InterruptedException {
        driver.findElement(By.id("lifecycleIconMinimized")).click();
        waitTimeforElement("//div[3]/div[2]/a");
        driver.findElement(By.linkText("Add Lifecycle")).click();
        waitTimeforElement("//div[3]/div[3]/form/table/tbody/tr[2]/td/input");
        assertTrue(selenium.isTextPresent("Enable Lifecycle"), "Enable LifeCycle Text does not appear :");
        assertEquals("Add", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input"),
                     "Lifecycle Add Button does not appear :");
        assertEquals("Cancel", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input[2]"),
                     "Lifecycle Cancel button does not appear");
        driver.findElement(By.xpath("//div[3]/div[3]/form/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//tr[2]/td/div/input");
        assertTrue(selenium.isTextPresent("Development"),
                   "Service Life cycle default state does not exists:");
    }

    private void promoteState() throws Exception {
        waitTimeforElement("//td/div[3]/table/tbody/tr/td/input");
        driver.findElement(By.id("option0")).click();
        Thread.sleep(sleeptime);
        driver.findElement(By.id("option1")).click();
        Thread.sleep(sleeptime);
        driver.findElement(By.id("option2")).click();
        Thread.sleep(sleeptime);
        driver.findElement(By.xpath("//tr[2]/td/div/input")).click();
        waitTimeforElement("//body/div[4]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Life Cycle Promote pop-up Title fail :");
        assertTrue(selenium.isTextPresent("Successfully Promoted"), "Life Cycle promote pop-up message fail :");
        // click on OK button
        selenium.click("css=button[type=\"button\"]");
        Thread.sleep(sleeptime);
    }

    private void deleteTag() throws InterruptedException {
        driver.findElement(By.xpath("//a[2]/img")).click();
        waitTimeforElement("//body/div[4]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Tag Pop-up Title fail :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this tag?"),
                   "Delete Tag Pop-up Message fail :");
        waitTimeforElement("//button");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");
    }

    private void applyTag(String tagName) throws InterruptedException {
        driver.findElement(By.id("tagsIconMinimized")).click();
        waitTimeforElement("//div[12]/div/a");
        driver.findElement(By.linkText("Add New Tag")).click();
        waitTimeforElement("//div[2]/input[2]");
        driver.findElement(By.id("tfTag")).sendKeys(tagName);
        waitTimeforElement("//div[2]/input[3]");
        driver.findElement(By.xpath("//div[2]/input[3]")).click();
        waitTimeforElement("//div[3]/a");
    }


    private void deleteComment() throws InterruptedException {
        driver.findElement(By.id("closeC0")).click();
        waitTimeforElement("//body/div[4]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Comment Delete pop-up  failed :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this comment?"),
                   "Comment Delete pop-up  failed :");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");  //sign out button appear

    }

    private void addComment(String comment) throws InterruptedException {
        driver.findElement(By.id("commentsIconMinimized")).click();
        waitTimeforElement("//div[15]/div/div[3]/div[2]/a");   // wait till add comment link appear
        driver.findElement(By.linkText("Add Comment")).click();
        waitTimeforElement("//div[15]/div/div[3]/div[3]/form/table/tbody/tr[2]/td/input"); //add button
        assertTrue(selenium.isTextPresent("Add New Comment"),
                   "Add comment window pop -up title failed :");
        assertEquals("Add", selenium.getValue("//div[15]/div/div[3]/div[3]/form/table/tbody/" +
                                              "tr[2]/td/input"),
                     "Add comment window  button add not present failed :");
        assertEquals("Cancel", selenium.getValue("//div[15]/div/div[3]/div[3]/form/table/" +
                                                 "tbody/tr[2]/td/input[2]"),
                     "Add comment window  pop -up failed :");
        driver.findElement(By.id("comment")).sendKeys(comment);
        waitTimeforElement("//div[15]/div/div[3]/div[3]/form/table/tbody/tr[2]/td/input"); //add button
        driver.findElement(By.xpath("//div[15]/div/div[3]/div[3]/form/table/tbody/tr[2]/td/input")).click();
        waitTimeforElement("//div[4]/div/div[2]/table/tbody/tr/td/div"); // added commnet appear
    }


    private void findLocation(String path) throws Exception {
        driver.findElement(By.id("uLocationBar")).clear();
        driver.findElement(By.id("uLocationBar")).sendKeys(path);
        driver.findElement(By.xpath("//input[2]")).click();
        Thread.sleep(sleeptime);
    }


    private void addCollection(String collectionPath) throws Exception {
        waitTimeforElement("//div[3]/div[2]/a");
        driver.findElement(By.linkText("Add Collection")).click();
        waitTimeforElement("//div[7]/form/table/tbody/tr[2]/td[2]/input");
        driver.findElement(By.id("collectionName")).sendKeys(collectionPath);
        driver.findElement(By.id("colDesc")).sendKeys("Selenium Test");
        waitTimeforElement("//div[7]/form/table/tbody/tr[5]/td/input");
        driver.findElement(By.xpath("//div[7]/form/table/tbody/tr[5]/td/input")).click();
        Thread.sleep(sleeptime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                   "Add new Collection pop -up Title failed:");
        assertTrue(selenium.isTextPresent("Successfully added new collection."),
                   "Add new Collection pop -up Message failed:");
        selenium.click("//button");
        Thread.sleep(sleeptime);
    }


    private void gotoDetailViewTab() throws Exception {
        driver.findElement(By.linkText("Browse")).click();    //Click on Browse link
        waitTimeforElement("//a[2]");
        driver.findElement(By.id("stdView")).click();        //Go to Detail view Tab
        waitTimeforElement("//div[3]/div[3]/div/a");
        assertTrue(selenium.isTextPresent("Browse"), "Browse Detail View Page fail :");
        assertTrue(selenium.isTextPresent("Metadata"), "Browse Detail View Page fail Metadata:");
        assertTrue(selenium.isTextPresent("Entries"), "Browse Detail View Page Entries fail :");
        Thread.sleep(sleeptime);

    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }

    private void addResource(String resourceName) throws Exception {
        waitTimeforElement("//div[3]/div[3]/div/a");
        driver.findElement(By.linkText("Add Resource")).click();
        waitTimeforElement("//tr[3]/td[2]/input");
        assertTrue(selenium.isTextPresent("Add Resource"), "Add new resource page failed :");
        //select create text content
        selenium.select("id=addMethodSelector", "label=Create Text content");
        selenium.click("css=option[value=\"text\"]");
        waitTimeforElement("//tr[4]/td/form/table/tbody/tr[2]/td[2]/input");
        // Enter name
        driver.findElement(By.id("trFileName")).sendKeys(resourceName);
        driver.findElement(By.id("trMediaType")).sendKeys("txt");
        driver.findElement(By.id("trDescription")).sendKeys("selenium test resource");
        driver.findElement(By.id("trPlainContent")).sendKeys("selenium test123");
        // Click on Add button
        driver.findElement(By.xpath("//tr[4]/td/form/table/tbody/tr[6]/td/input")).click();
        waitTimeforElement("//body/div[3]/div/div");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add Resource pop-up title fail :");
        assertTrue(selenium.isTextPresent("Successfully added Text content."),
                   "Add Resource pop-up message fail :");
        //Click on OK button
        driver.findElement(By.xpath("//button")).click();
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
