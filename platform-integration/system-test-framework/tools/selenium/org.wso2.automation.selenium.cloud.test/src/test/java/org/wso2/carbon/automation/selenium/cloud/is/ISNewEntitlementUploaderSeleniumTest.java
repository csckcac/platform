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
package org.wso2.carbon.automation.selenium.cloud.is;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ISNewEntitlementUploaderSeleniumTest {
    private static final Log log = LogFactory.getLog(ISNewEntitlementUploaderSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    String productName = "is";
    String userName;
    String password;
    long sleeptime = 5000;
    String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION;


    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        UserInfo userDetails = UserListCsvReader.getUserInfo(3);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.IS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
    }

//    @Test(groups = {"wso2.is"}, description = "Apply xacmal policy IIA001 from file", priority = 1)
    public void testaddxacmlPolicyIIA001() throws Exception {
        String policyfilePath = resourcePath + File.separator + "artifacts" + File.separator + "IS" + File.separator + "IIA001Policy.xml";
        String requestFilePath = resourcePath + File.separator + "artifacts" + File.separator + "IS" + File.separator + "IIA001Request.xml";
        String requestXML = getDatafromFile(requestFilePath);
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            uploadPolicyfromFile(policyfilePath);
            waitTimeforElement("//td[3]/a");
            assertTrue(selenium.isTextPresent("exact:urn:oasis:names:tc:xacml:2.0:conformance-test:" +
                                              "IIA1:policy"), "Failed to upload policy IIA001Policy.xml");
            driver.findElement(By.linkText("Enable")).click();
            waitTimeforElement("//td[3]/a");
            gotoTryITpage(requestXML);
            waitTimeforElement("//div[3]/div/div");
            assertTrue(selenium.isTextPresent("Permit"), "Policy IIA001 Response Failed :");
            selenium.click("//button");
            Thread.sleep(sleeptime);
            deletePolicy();
            userLogout();
            log.info("********IS Stratos XACML Policy IIA001 Uploader Test -Passed **************");
        } catch (AssertionFailedError e) {
            log.info("XACML Policy IIA001 Uploader Test Failed:" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("XACML Policy IIA001 Uploader Test Failed:" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("XACML Policy IIA001 Uploader Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("XACML Policy IIA001 Uploader Test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("XACML Policy IIA001 Uploader Test Failed:" + e.getMessage());
            userLogout();
            throw new Exception("XACML Policy IIA001 Uploader Test Failed :" + e.getMessage());
        }

    }

    @Test(groups = {"wso2.is"}, description = "Apply xacmal policy IIA003 from file", priority = 2)
    public void testaddxacmlPolicyIIA003() throws Exception {
        String filePath = resourcePath + File.separator + "artifacts" + File.separator +
                          "IS" + File.separator +"IIA003Policy.xml";
        String requestFilePath = resourcePath + File.separator + "artifacts" + File.separator +
                           "IS" + File.separator +"IIA003Request.xml";
        String requestXML = getDatafromFile(requestFilePath);
        try {
            new StratosUserLogin().userLogin(driver, selenium, userName, password, productName);
            log.info("Stratos IS Login Success");
            uploadPolicyfromFile(filePath);
            waitTimeforElement("//td[3]/a");
            assertTrue(selenium.isTextPresent("exact:urn:oasis:names:tc:xacml:2.0:conformance-test" +
                                              ":IIA003:policy"),
                       "Failed to upload policy IIA001Policy.xml");
            driver.findElement(By.linkText("Enable")).click();
            waitTimeforElement("//td[3]/a");
            gotoTryITpage(requestXML);
            waitTimeforElement("//div[3]/div/div");
            assertTrue(selenium.isTextPresent("NotApplicable"), "Policy IIA003 Response Failed :");
            selenium.click("//button");
            Thread.sleep(sleeptime);
            deletePolicy();
            userLogout();
            log.info("********IS Stratos XACML Policy IIA003 Uploader Test -Passed **************");
        } catch (AssertionFailedError e) {
            log.info("XACML Policy IIA003 Uploader Test Failed:" + e.getMessage());
            userLogout();
            throw new AssertionFailedError("XACML Policy IIA003 Uploader Test Failed:" +
                                           e.getMessage());
        } catch (WebDriverException e) {
            log.info("XACML Policy IIA003 Uploader Test Failed :" + e.getMessage());
            userLogout();
            throw new WebDriverException("XACML Policy IIA003 Uploader Test Failed :" +
                                         e.getMessage());
        } catch (Exception e) {
            log.info("XACML Policy IIA003 Uploader Test Failed:" + e.getMessage());
            userLogout();
            throw new Exception("XACML Policy IIA003 Uploader Test Failed :" + e.getMessage());
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        driver.quit();
    }

    private void userLogout() throws InterruptedException {
        driver.findElement(By.linkText("Sign-out")).click();
        waitTimeforElement("//a[2]/img");
    }

    private void deletePolicy() throws InterruptedException {
        driver.findElement(By.linkText("Administration")).click();
        waitTimeforElement("//td[3]/div/a");
        driver.findElement(By.name("policies")).click();
        driver.findElement(By.id("delete1")).click();
        waitTimeforElement("//div[3]/div/div");
        assertTrue(selenium.isTextPresent("exact:Do you want to delete the selected polices?"),
                   "Failed to Delete policy :");
        selenium.click("//button");
        waitTimeforElement("//li[3]/a");
    }

    private void gotoTryITpage(String requestXML) throws InterruptedException {
        driver.findElement(By.linkText("TryIt")).click();
        waitTimeforElement("//tr[7]/td/input");
        //click on  Create Request Using Editor
        driver.findElement(By.linkText("Create Request Using Editor")).click();
        waitTimeforElement("//textarea");
        selenium.selectFrame("frame_txtPolicyTemp");
        driver.findElement(By.id("textarea")).clear();
        Thread.sleep(sleeptime);
        driver.findElement(By.id("textarea")).sendKeys(requestXML);
        driver.switchTo().defaultContent();
        driver.findElement(By.xpath("//td/input")).click();
        Thread.sleep(sleeptime);
    }

    private void uploadPolicyfromFile(String filePath) throws InterruptedException {
        driver.findElement(By.linkText("Administration")).click();
        waitTimeforElement("//td[3]/div/a");
        driver.findElement(By.linkText("Import New Entitlement Policy")).click();
        waitTimeforElement("//select");
        driver.findElement(By.id("policyFromFileSystem")).sendKeys(filePath);
        driver.findElement(By.xpath("//tr[4]/td/input")).click();
        waitTimeforElement("//div[3]/div/div");
        assertEquals("Entitlement policy imported successfully", selenium.getText("//p"),
                     "Entitlement Policy Import Message Failed:");
        selenium.click("//button");
        log.info("xacml Policy uploaded successgully");
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

    private String getDatafromFile(String requestFilePath) throws IOException {
        File xmlFile = new File(requestFilePath);
        BufferedReader bufferedReader = null;
        String xmlString = "";

        try {
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader(xmlFile));
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                //Process the data, here we just print it out
                xmlString += line + "\n";
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return xmlString;
    }

}
