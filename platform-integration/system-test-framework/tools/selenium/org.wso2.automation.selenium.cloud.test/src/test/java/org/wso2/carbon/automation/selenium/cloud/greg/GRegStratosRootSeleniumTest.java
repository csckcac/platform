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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class GRegStratosRootSeleniumTest {
    private static final Log log = LogFactory.getLog(GRegStratosRootSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "greg";
    String userName;
    String password;
    long sleepTime = 3000;


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

    @Test(groups = {"wso2.greg"}, description = "add a collection to root", priority = 1)
    public void testAddCollectionToRoot() throws Exception {
        String collectionPath = "/selenium_root";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addCollection(collectionPath);
            userLogout();
            log.info("********GReg Stratos - Add Collection to Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Add Collection to Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add Collection to Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add Collection to Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add Collection to Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add Collection to Root test Failed :" + e);
            userLogout();
            throw new Exception("Add Collection to Root test Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "add a resource to root", priority = 2)
    public void testAddResourceToRoot() throws Exception {
        String resourceName = "root_resource";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addResource(resourceName);
            userLogout();
            log.info("********GReg Stratos - Add Resource to Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Add Resource to Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add Resource to Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add Resource to Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add Resource to Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add Resource to Root test Failed :" + e);
            userLogout();
            throw new Exception("Add Resource to Root test Failed :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "add a comment to root", priority = 3)
    public void testAddCommentToRoot() throws Exception {
        String comment = "rootcomment";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addComment(comment);
            deleteComment();
            userLogout();
            log.info("********GReg Stratos - Add a Comment to Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Add a Comment to Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Add a Comment to Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Add a Comment to Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Add a Comment to Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Add a Comment to Root test Failed :" + e);
            userLogout();
            throw new Exception("Add a Comment to Root test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a tag to root", priority = 4)
    public void addTagToRoot() throws Exception {
        String tagName = "roottag";
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            addTag(tagName);
            selenium.mouseOver("//div[12]/div[3]/a");
            Thread.sleep(sleepTime);
            deleteTag();
            userLogout();
            log.info("********GReg Stratos - Apply a Tag to Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Apply a Tag to Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Apply a Tag to Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Apply a Tag to Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Apply a Tag to Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Apply a Tag to Root test Failed :" + e);
            userLogout();
            throw new Exception("Apply a Tag to Root test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "apply a rating to root", priority = 5)
    public void addRatingToRoot() throws Exception, AssertionFailedError {
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            applyRating();
            userLogout();
            log.info("********GReg Stratos - Apply Rating to Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Apply Rating to Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Apply Rating to Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Apply Rating to Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Apply Rating to Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Apply Rating to Root test Failed :" + e);
            userLogout();
            throw new Exception("Apply Rating to Root test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "delete a resource from root", priority = 6)
    public void testDeleteResourceFromRoot() throws Exception {
        try {
            StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
            gotoDetailViewTab();
            deleteResource();
            userLogout();
            log.info("********GReg Stratos - Delete a  Resource from Root test - Passed********");
        } catch (AssertionFailedError e) {
            log.info("Delete a  Resource from Root test Failed :" + e);
            userLogout();
            throw new AssertionFailedError("Delete a  Resource from Root test Failed :" +
                    e);
        } catch (WebDriverException e) {
            log.info("Delete a  Resource from Root test Failed :" + e);
            userLogout();
            throw new WebDriverException("Delete a  Resource from Root test Failed :" +
                    e);
        } catch (Exception e) {
            log.info("Delete a  Resource from Root test Failed :" + e);
            userLogout();
            throw new Exception("Delete a  Resource from Root test Failed :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "delete a collection from root", priority = 7)
    public void testDeleteCollectionFromRoot() throws Exception {
        StratosUserLogin.userLogin(driver, selenium, userName, password, productName);
        deleteTestArtifact();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        driver.quit();
    }

    private void deleteResource() throws InterruptedException {
        driver.findElement(By.id("actionLink2")).click();
        driver.findElement(By.xpath("//tr[9]/td/div/a[3]")).click();
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "Delete root resource pop-up dialog title fail:");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete " +
                "'/root_resource' permanently?"),
                "Delete root resource pop-up message fail :");
        selenium.click("//button");
        Thread.sleep(sleepTime);
    }


    private void applyRating() throws InterruptedException {
        // Add rating 1
        driver.findElement(By.xpath("//span/img[3]")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("(1.0)"), "Rating 1 has failed :");
        // Add rating 2
        driver.findElement(By.xpath("//span/img[5]")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("(2.0)"), "Rating 2 has failed :");
        // Add rating 3
        driver.findElement(By.xpath("//img[7]")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("(3.0)"), "Rating 3 has failed :");
        // Add rating 4
        driver.findElement(By.xpath("//img[9]")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("(4.0)"), "Rating 4 has failed :");
        // Add rating 5
        driver.findElement(By.xpath("//img[11]")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("(5.0)"), "Rating 5 has failed :");
    }

    private void deleteTag() throws InterruptedException {
        driver.findElement(By.xpath("//a[2]/img")).click();           //Delete Tag
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Delete Tag Pop-up Title fail :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this tag?"),
                "Delete Tag Pop-up Message fail :");
        selenium.click("//button");                //click on "yes" button
        Thread.sleep(sleepTime);
    }

    private void addTag(String tagName) throws InterruptedException {
        driver.findElement(By.id("tagsIconMinimized")).click();        //Apply Tag

        driver.findElement(By.linkText("Add New Tag")).click();          //click on Add New Tag
        driver.findElement(By.id("tfTag")).sendKeys(tagName);
        Thread.sleep(sleepTime);
        driver.findElement(By.xpath("//div[2]/input[3]")).click();
    }

    private void addComment(String comment) throws InterruptedException {
        driver.findElement(By.id("commentsIconMinimized")).click();
        Thread.sleep(sleepTime);
        driver.findElement(By.linkText("Add Comment")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("Add New Comment"), "Add comment window pop -up failed :");
        assertEquals("Add", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/td/input"),
                "Add comment window  pop -up Add button failed :");
        assertEquals("Cancel", selenium.getValue("//div[3]/div[3]/form/table/tbody/tr[2]/" +
                "td/input[2]"),
                "Add comment window  pop -up Cancel Button failed :");
        driver.findElement(By.id("comment")).sendKeys(comment);
        driver.findElement(By.xpath("//div[3]/div[3]/form/table/tbody/tr[2]/td/input")).click();
        Thread.sleep(sleepTime);
     }


    private void deleteComment() throws InterruptedException {
        driver.findElement(By.id("closeC0")).click();
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "root Comment Delete pop-up  title failed :");
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete this comment?"),
                "root Comment Delete pop-up  message failed :");
        selenium.click("//button");
        Thread.sleep(sleepTime);
    }


    private void gotoDetailViewTab() throws Exception {
        driver.findElement(By.linkText("Browse")).click();    //Click on Browse link
        Thread.sleep(sleepTime);
        driver.findElement(By.id("stdView")).click();        //Go to Detail view Tab
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("Browse"), "Browse Detail View Page fail :");
        assertTrue(selenium.isTextPresent("Metadata"), "Browse Detail View Page fail Metadata:");
        Thread.sleep(sleepTime);

    }


    private void addCollection(String collectionPath) throws Exception {
        Thread.sleep(sleepTime);
        driver.findElement(By.linkText("Add Collection")).click();
        Thread.sleep(sleepTime);
        driver.findElement(By.id("collectionName")).sendKeys(collectionPath);
        driver.findElement(By.id("colDesc")).sendKeys("Selenium Test");
        driver.findElement(By.xpath("//div[7]/form/table/tbody/tr[5]/td/input")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"), "Add new Collection pop -up failed :");
        assertTrue(selenium.isTextPresent("Successfully added new collection."),
                "Add new Collection pop -up failed :");
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleepTime);

    }

    private void addResource(String resourceName) throws Exception {
        driver.findElement(By.linkText("Add Resource")).click();
        assertTrue(selenium.isTextPresent("Add Resource"), "Add new resource page failed :");
        //select create text content
        selenium.select("id=addMethodSelector", "label=Create Text content");
        selenium.click("css=option[value=\"text\"]");
        Thread.sleep(sleepTime);
        driver.findElement(By.id("trFileName")).sendKeys(resourceName);
        driver.findElement(By.id("trMediaType")).sendKeys("txt");
        driver.findElement(By.id("trDescription")).sendKeys("selenium test resource");
        driver.findElement(By.id("trPlainContent")).sendKeys("selenium test123");
        // Click on Add button
        driver.findElement(By.xpath("//tr[4]/td/form/table/tbody/tr[6]/td/input")).click();
        Thread.sleep(sleepTime);
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                "Add Resource pop-up message title fail :");
        assertTrue(selenium.isTextPresent("Successfully added Text content."),
                "Add Resource pop-up message fail :");
        //Click on OK button
        driver.findElement(By.xpath("//button")).click();
        Thread.sleep(sleepTime);
    }


    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        Thread.sleep(sleepTime);
    }

    private void deleteTestArtifact() throws InterruptedException {
        driver.findElement(By.linkText("Browse")).click();    //Click on Browse link
        Thread.sleep(sleepTime);
        driver.findElement(By.linkText("Root")).click();
        driver.findElement(By.id("actionLink2")).click();
        driver.findElement(By.linkText("Delete")).click();
        assertTrue(selenium.isTextPresent("exact:Are you sure you want to delete " +
                "'/selenium_root' permanently?"),
                "Delete root resource pop-up message fail :");
        selenium.click("//button");
    }


}
