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
package org.wso2.carbon.automation.selenium.cloud.dss;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.selenium.cloud.dss.utils.DSSServerUIUtils;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.util.Iterator;

/*This class tests DSS> Tools > DB Explorer page*/
public class DSSDatabaseExplorerSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSDatabaseExplorerSeleniumTest.class);
    private WebDriver driver;
    private DSSServerUIUtils dssServerUI;
    private UserInfo userDetails;

    private FrameworkProperties dssProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
    private final String driverClass = "com.mysql.jdbc.Driver";
    private String dataBaseName;
    private String dbUserName;
    private String dbUserPassword;

    private String jdbcUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUI = new DSSServerUIUtils(driver);
        dssServerUI.login(userDetails.getUserName(), userDetails.getPassword());
        dataBaseName = dssProperties.getDataSource().getDbName() + "_" + userDetails.getDomain().replace('.', '_');
        dbUserName = dssServerUI.getFullyQualifiedUsername(dssProperties.getDataSource().getRssDbUser(), userDetails.getDomain());
        dbUserPassword = dssProperties.getDataSource().getRssDbPassword();
        jdbcUrl = dssServerUI.getJdbcUrl(dataBaseName);
        if (jdbcUrl == null) {
            throw new RuntimeException(dataBaseName +
                                       " Database Not found to connect");
        }


    }

    @Test(priority = 1)
    public void testConnectionUnSuccessful() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");

        log.info("Driver Class : " + driverClass);
        log.info("JDBC URL : " + jdbcUrl);
        log.info("User : " + dbUserName);

        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).
                sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys("12345");
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Test Connection".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(2000);
        Assert.assertNotEquals(driver.findElement(By.id("login")).findElement(By.className("error")).getText(), "Test successful",
                               "Connection failed message mismatched");
    }

    @Test(priority = 2)
    public void testConnectionSuccessful() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");


        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).
                sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys(dbUserPassword);
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Test Connection".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(2000);

        Assert.assertEquals(driver.findElement(By.id("login")).findElement(By.className("error")).getText(), "Test successful",
                            "Test Connection Failed");
    }

    @Test(priority = 3)
    public void explorerDatabaseByTool() throws InterruptedException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");

        log.info("Driver Class : " + driverClass);
        log.info("JDBC URL : " + jdbcUrl);
        log.info("User : " + dbUserName);

        driver.findElement(By.id("login")).findElement(By.name("driver")).sendKeys(driverClass);
        driver.findElement(By.id("login")).findElement(By.name("url")).sendKeys(jdbcUrl);
        driver.findElement(By.id("login")).findElement(By.name("user")).sendKeys(dbUserName);
        driver.findElement(By.id("login")).findElement(By.name("password")).sendKeys(dbUserPassword);
        Thread.sleep(1000);
        for (WebElement button : driver.findElement(By.id("login")).findElements(By.className("button"))) {
            if ("Connect".equalsIgnoreCase(button.getAttribute("Value"))) {
                button.click();
                break;
            }
        }
        Thread.sleep(3000);
        driver.switchTo().frame("h2query");
        Assert.assertEquals(driver.findElement(By.xpath("/html/body/form/input")).getAttribute("value"),
                            "Run (Ctrl+Enter)", "Database Console page not found. Run (Ctrl+Enter) " +
                                                "button not found in Database Console.");
//        driver.findElement(By.xpath("/html/body/form/table/tbody/tr/td/a/img")).click();
        driver.switchTo().defaultContent();

    }

    @Test(priority = 3)
    public void DatabaseExplorerLoginInfoCashing() throws InterruptedException,
                                                                  XMLStreamException {
        driver.switchTo().defaultContent();
        dssServerUI.clickOnTools();
        driver.findElement(By.linkText("Database Explorer")).click();
        driver.switchTo().frame("inlineframe");
        // to verify required attribute found during test
        boolean isDriverTextFound = false;
        boolean isJDBCUrlFound = false;
        boolean isUserNameFound = false;
        boolean isPasswordFound = false;

        Thread.sleep(2000);
        /* to get a table content*/
        OMElement pageSource = AXIOMUtil.stringToOM(driver.getPageSource().substring(
                driver.getPageSource().indexOf("<tbody>"),
                driver.getPageSource().indexOf("</table>")));
        Iterator<OMElement> trItr = pageSource.getChildrenWithName(new QName("tr"));

        while (trItr.hasNext()) {
            OMElement tr = trItr.next();
            Iterator<OMElement> tdItr = tr.getChildrenWithName(new QName("td"));
            while (tdItr.hasNext()) {
                OMElement td = tdItr.next();
                Iterator<OMElement> inputItr = td.getChildrenWithName(new QName("input"));
                while (inputItr.hasNext()) {
                    OMElement input = inputItr.next();
                    if ("text".equalsIgnoreCase(input.getAttributeValue(new QName("type")))) {
                        if ("url".equalsIgnoreCase(input.getAttributeValue(new QName("name")))) {
                            isJDBCUrlFound = true;
                            Assert.assertTrue((input.getAttributeValue(new QName("value")) == null ||
                                               input.getAttributeValue(new QName("value")).equalsIgnoreCase("")),
                                              "Critical Security Issue. URL cashed in page source");
                        } else if ("user".equalsIgnoreCase(input.getAttributeValue(new QName("name")))) {
                            isUserNameFound = true;
                            Assert.assertTrue((input.getAttributeValue(new QName("value")) == null ||
                                               input.getAttributeValue(new QName("value")).equalsIgnoreCase("")),
                                              "Critical Security Issue. User Name cashed in page source");

                        } else if ("password".equalsIgnoreCase(input.getAttributeValue(new QName("name")))) {
                            isPasswordFound = true;
                            Assert.assertTrue((input.getAttributeValue(new QName("value")) == null ||
                                               input.getAttributeValue(new QName("value")).equalsIgnoreCase("")),
                                              "Critical Security Issue. Password cashed in page source");
                        } else if ("driver".equalsIgnoreCase(input.getAttributeValue(new QName("name")))) {
                            isDriverTextFound = true;
                            Assert.assertTrue((input.getAttributeValue(new QName("value")) == null ||
                                               input.getAttributeValue(new QName("value")).equalsIgnoreCase("")),
                                              "Critical Security Issue. Driver name cashed in page source");
                        }
                    }

                }
            }


        }
        Assert.assertTrue(isDriverTextFound, "Driver Text Box not found. Check the test case code to " +
                                             "verify the issue. There may be UI change");
        Assert.assertTrue(isJDBCUrlFound, "URL Text Box not found. Check the test case code to verify " +
                                          "the issue. There may be UI change");
        Assert.assertTrue(isUserNameFound, "UserName Text Box not found. Check the test case code to " +
                                           "verify the issue. There may be UI change");
        Assert.assertTrue(isPasswordFound, "Password Text Box not found. Check the test case code to " +
                                           "verify the issue. There may be UI change");
        driver.switchTo().defaultContent();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        try {
            driver.switchTo().defaultContent();
            dssServerUI.logOut();
        } finally {
            driver.quit();
        }

    }

}
