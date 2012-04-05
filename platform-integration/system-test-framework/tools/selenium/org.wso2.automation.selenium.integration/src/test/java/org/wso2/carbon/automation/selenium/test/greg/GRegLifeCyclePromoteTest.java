package org.wso2.carbon.automation.selenium.test.greg;

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GRegBackEndURLEvaluator;
import org.wso2.platform.test.core.utils.seleniumutils.GregResourceURLUploader;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogin;
import org.wso2.platform.test.core.utils.seleniumutils.GregUserLogout;
import org.wso2.platform.test.core.utils.seleniumutils.SeleniumScreenCapture;

import java.net.MalformedURLException;
import java.util.Calendar;

import static org.testng.Assert.assertTrue;

/**
 * Test case for - When clicked on 'Proceed' button with invalid/blank version number, the button will not be enabled
 * until you refresh the service
 * https://wso2.org/jira/browse/CARBON-11703
 */
public class GRegLifeCyclePromoteTest {

    private static final Log log = LogFactory.getLog(GRegCollectionSeleniumTest.class);
    private static Selenium selenium;
    private static WebDriver driver;
    private static String username;
    private static String password;
    private static long SLEEP_TIME = 2 * 1000;
    private static final String SERVICE_NAME = "echoyuSer1";

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        int userId = new GregUserIDEvaluator().getTenantID();
        String baseUrl = new GRegBackEndURLEvaluator().getBackEndURL();
        driver = BrowserManager.getWebDriver();
        selenium = new WebDriverBackedSelenium(driver, baseUrl);
        driver.get(baseUrl);
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        username = userInfo.getUserName();
        password = userInfo.getPassword();
    }

    @Test(groups = {"wso2.greg"}, description = "Login to GReg and add a wsdl", priority = 1)
    public void testGRegUserLogin() throws InterruptedException {
        new GregUserLogin().userLogin(driver, username, password);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("WSO2 Governance Registry Home"));
        deleteFromServiceList(SERVICE_NAME);//delete wsdl and services if exists
        deleteFromWsdlList(SERVICE_NAME);
    }


    @Test(groups = {"wso2.greg"}, description = "add echo wsdl from URL", priority = 2)
    public void testAddWSDLfromURL() throws Exception {
        String wsdlURL = "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration" +
                         "/system-test-framework/core/org.wso2.automation.platform.core/src/main/" +
                         "resources/artifacts/GREG/wsdl/echo.wsdl";
        // Click on add wsdl link
        driver.findElement(By.linkText("WSDL")).click();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Add WSDL"), "Add WSDL Dash Board - Add WSDL Text does not appear::");
        assertTrue(selenium.isTextPresent("Add New WSDL"), "Add WSDL Dash Board - Add New WSDL Text does not appear::");
        // Add WSDL Dashboard
        new GregResourceURLUploader().uploadResource(driver, wsdlURL, null);
        Thread.sleep(15000L);
        // wsdl dash board
        assertTrue(selenium.isTextPresent("WSDL List"), "WSDL Dashboard Does not appear ::");
        assertTrue(selenium.isTextPresent("echo.wsdl"), "Uploaded WSDL name does appear on WSDL Dashboard :");
        log.info("WSDL was added successfully");
    }

    @Test(groups = {"wso2.greg"}, description = "assign LC to the wsdl", priority = 3)
    public void testAssignLifeCycle() throws Exception {
        // click on service list menu
        driver.findElement(By.linkText("Services")).click();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Service List"), "Service List - Service List Text does not appear::");
        assertTrue(selenium.isTextPresent(SERVICE_NAME), "Service List - Service not found in the list::");
        // click on service name
        driver.findElement(By.linkText(SERVICE_NAME)).click();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent(SERVICE_NAME), "Service Page - Service name not found:");
        assertTrue(driver.getPageSource().contains("1.0.0-SNAPSHOT"), "Service Page - Service version not found:");
        selenium.select("name=Service_Lifecycle_Lifecycle-Name", "label=ServiceLifeCycle");
        Thread.sleep(5000);
        selenium.click("css=input.button.registryWriteOperation");
        Thread.sleep(2000L);
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("ServiceLifeCycle"), "Service Info - Service LC name not found");
        assertTrue(selenium.isTextPresent("Code Completed"), "Service Info - LC checklist items not available");
        log.info("LC has been assigned to " + SERVICE_NAME);
    }

    @Test(groups = {"wso2.greg"}, description = "tick LC checklist", priority = 4)
    public void testPromoteLifeCycle() throws Exception {
        driver.findElement(By.id("option0")).click();
        Thread.sleep(SLEEP_TIME);
        driver.findElement(By.id("option1")).click();
        Thread.sleep(SLEEP_TIME);
        driver.findElement(By.id("option2")).click();
        Thread.sleep(SLEEP_TIME);
        // promote
        driver.findElement(By.xpath("//tr[2]/td/div/input")).click();
        Thread.sleep(SLEEP_TIME);
        log.info("Tick LC checklist...");
    }

    @Test(groups = {"wso2.greg"}, description = "invalid LC promotion", priority = 5)
    public void testInvalidProceed() throws Exception {
        driver.findElement(By.id("Proceed")).click();
        Thread.sleep(SLEEP_TIME);

        driver.findElement(By.id("dialog")).click();
        waitForElement("//button");
        assertTrue(selenium.isTextPresent("WSO2 Carbon"),
                   "Error pop up fail :");
        assertTrue(selenium.isTextPresent("The version of ep-echo-yu-echoHttpsSoap12Endpoint is incorrect"),
                   "Error pop up fail :");
        //click on OK button
        selenium.click("//button");
        Thread.sleep(SLEEP_TIME);
    }

    @Test(groups = {"wso2.greg"}, description = "valid LC promotion", priority = 6)
    public void testValidProceed() throws Exception {
        String version = "1.2.3";
        selenium.type("id=ep-echo-yu-echoHttpsSoap12Endpoint", version);
        selenium.type("id=ep-echo-yu-echoHttpsSoap11Endpoint", version);
        selenium.type("id=echoyuSer1", version);
        selenium.type("id=ep-echo-yu-echoHttpsEndpoint", version);
        selenium.type("id=ep-echo-yu-echoHttpSoap11Endpoint", version);
        selenium.type("id=ep-echo-yu-echoHttpEndpoint", version);
        selenium.type("id=ep-echo-yu-echoHttpSoap12Endpoint", version);
        selenium.type("id=echo.wsdl", version);
        driver.findElement(By.id("Proceed")).click();
        Thread.sleep(SLEEP_TIME);
        assertTrue(selenium.isTextPresent("Successfully Promoted"));
        selenium.click("//button");
        selenium.waitForPageToLoad("30000");
        Thread.sleep(SLEEP_TIME);
        log.info("LC promotion was successful");
    }

    @Test(groups = {"wso2.greg"}, description = "verity LC promotion", priority = 7)
    public void testVerifyLCPromotion() throws Exception {
        String version = "1.2.3";
        //go to service list and verity the version
        driver.findElement(By.linkText("Services")).click();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("Service List"), "Service List - Service List Text does not appear::");
        assertTrue(selenium.isTextPresent(SERVICE_NAME), "Service List - Service not found in the list::");
        assertTrue(selenium.isTextPresent(version), "Service List - Service version not found in the list::");

        //navigate to new service version
        driver.findElement(By.linkText(version)).click();
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent(SERVICE_NAME), "Service Info - Service name not found in the::");
        assertTrue(selenium.isTextPresent("Testing"), "LifeCycle Info - LC state not found in the page::");
        assertTrue(selenium.isTextPresent("Effective Inspection Completed"), "LifeCycle Info - LC checklist " +
                                                                             "not found in the page::");
        assertTrue(driver.getPageSource().contains(version), "Service Info - Service version not found in the::");
        assertTrue(driver.getPageSource().contains(version), "Service Info - Service version not found in the::");
        log.info("LC promotion was verified");
    }

    @AfterClass(groups = {"wso2.greg"}, description = "Clean all WSDLs and services")
    public void testCleanAndCapture() throws Exception {
        driver.findElement(By.linkText("Services")).click();
        selenium.waitForPageToLoad("30000");
        deleteFromServiceList(SERVICE_NAME);
        deleteFromWsdlList("echo.wsdl");
        new SeleniumScreenCapture().getScreenshot(driver, "greg", "GRegLifeCyclePromoteTest");
        new GregUserLogout().userLogout(driver);
        driver.quit();
        log.info("Cleanup wsdl and services is done");
    }

    private void waitForElement(String elementName) throws InterruptedException {
        Calendar startTime = Calendar.getInstance();
        boolean element = false;
        while (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()))
               < (120 * 1000)) {
            if (selenium.isElementPresent(elementName)) {
                element = true;
                break;
            }
            Thread.sleep(1000);
            log.info("waiting for element :" + elementName);
        }
        assertTrue(element, "Element Not Found within 2 minutes :");
    }

    private void deleteFromServiceList(String serviceName) throws InterruptedException {
        driver.findElement(By.linkText("Services")).click();
        selenium.waitForPageToLoad("30000");
        int rowCount = selenium.getXpathCount("//TABLE[@id='customTable']/TBODY/TR").intValue();
        while (rowCount >= 0 && selenium.isTextPresent(serviceName)) {
            if (selenium.getTable("customTable." + rowCount + ".0").contains(serviceName)) {
                driver.findElement(By.xpath("//tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form[2]/table/tbody/tr["
                                            + rowCount + "]/td[4]/a")).click();
                Thread.sleep(SLEEP_TIME);
                assertTrue(selenium.isTextPresent("Are you sure you want to delete"));
                selenium.click("//button");
                log.info("Deleting service - " + serviceName);
                selenium.waitForPageToLoad("30000");
            }
            rowCount--;
        }
        assertTrue(!selenium.isTextPresent(serviceName), "Service List - Service " + serviceName + "has not deleted");
    }

    private void deleteFromWsdlList(String wsdlName) throws InterruptedException {
        driver.findElement(By.linkText("WSDLs")).click();
        selenium.waitForPageToLoad("30000");
        int rowCount = selenium.getXpathCount("//TABLE[@id='customTable']/TBODY/TR").intValue();
        while (rowCount >= 0 && selenium.isTextPresent(wsdlName)) {
            if (selenium.getTable("customTable." + rowCount + ".0").contains(wsdlName)) {
                driver.findElement(By.xpath("//tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr["
                                            + rowCount + "]/td[4]/a")).click();
                Thread.sleep(SLEEP_TIME);
                assertTrue(selenium.isTextPresent("Are you sure you want to delete"));
                selenium.click("//button");
                log.info("Deleting wsdl - " + wsdlName);
                selenium.waitForPageToLoad("30000");
            }
            rowCount--;
        }
        assertTrue(!selenium.isTextPresent(wsdlName), "WSDL List - WSDL " + wsdlName + "has not deleted");
    }
}
