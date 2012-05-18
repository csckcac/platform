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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class DSSPrivilegeGroupSeleniumTest {
    private static final Log log = LogFactory.getLog(DSSPrivilegeGroupSeleniumTest.class);
    private static WebDriver driver;
    private DSSServerUIUtils dssServerUI;
    private UserInfo userDetails;
    private final String privilegeGroupName = "AutomationTest";
    private List<String> enabledPrivilegeGroupList;


    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(10);

        String baseUrl = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.DSS_SERVER_NAME);
        log.info("baseURL is :" + baseUrl);
        driver = BrowserManager.getWebDriver();
        dssServerUI = new DSSServerUIUtils(driver);
        dssServerUI.login(userDetails.getUserName(), userDetails.getPassword());
        dssServerUI.deletePrivilegeGroupIfExists(privilegeGroupName);
        enabledPrivilegeGroupList = new ArrayList<String>();
        enabledPrivilegeGroupList.add("select_priv");
        enabledPrivilegeGroupList.add("insert_priv");
        enabledPrivilegeGroupList.add("update_priv");
        enabledPrivilegeGroupList.add("delete_priv");
        enabledPrivilegeGroupList.add("create_priv");
        enabledPrivilegeGroupList.add("drop_priv");
        enabledPrivilegeGroupList.add("execute_priv");
        enabledPrivilegeGroupList.add("event_priv");

    }


    @Test(description = "add a new privilege group", priority = 1)
    public void testAddPrivilegeGroup() throws InterruptedException {
        Thread.sleep(3000);
        dssServerUI.addPrivilegeGroup(privilegeGroupName);
        Thread.sleep(3000);
    }

    @Test(priority = 2, dependsOnMethods = {"testAddPrivilegeGroup"}, description = "Edit a existing privilege group")
    public void editPrivilegeGroup() throws InterruptedException {
        driver.findElement(By.linkText("Privilege Groups")).click();
        driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_" + privilegeGroupName)).findElement(By.linkText("Edit")).click();
        WebElement privGroupTd = driver.findElement(By.id("privGroupInfo")).findElement(By.tagName("tbody")).findElement(By.tagName("tr")).findElements(By.tagName("td")).get(1);
        Assert.assertEquals(privGroupTd.findElement(By.tagName("input")).getAttribute("value"), privilegeGroupName,
                            "Privilege Group Name Mismatched. Value : " + privGroupTd.getAttribute("value"));
        List<WebElement> permissionList = driver.findElement(By.id("dbUserTable")).
                findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

        for (WebElement permission : permissionList) {
            if (!enabledPrivilegeGroupList.contains(permission.findElements(By.tagName("td")).get(1).findElement(By.tagName("input")).getAttribute("name"))) {
                permission.findElements(By.tagName("td")).get(1).findElement(By.tagName("input")).click();
            }
        }
        Thread.sleep(2000);
        for (WebElement button : driver.findElement(By.id("content-table")).findElements(By.className("button"))) {
            if ("Save".equalsIgnoreCase(button.getAttribute("value"))) {
                button.click();
                break;
            }

        }
        Thread.sleep(3000);
        driver.findElement(By.id("privilegeGroupTable")).findElement(By.id("tr_" + privilegeGroupName)).findElement(By.linkText("Edit")).click();
        permissionList = driver.findElement(By.id("dbUserTable")).
                findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

        for (WebElement permission : permissionList) {
            if (enabledPrivilegeGroupList.contains(permission.findElements(By.tagName("td")).get(1).findElement(By.tagName("input")).getAttribute("name"))) {
                Assert.assertTrue(Boolean.parseBoolean(permission.findElements(By.tagName("td")).get(1).findElement(By.tagName("input")).getAttribute("checked")),
                                  "Editing privilege group failed. Enabled privilege group disabled");
            } else {
                Assert.assertTrue(permission.findElements(By.tagName("td")).get(1).findElement(By.tagName("input")).getAttribute("checked") == null,
                                  "Editing privilege group failed. Disabled privilege group Enabled");
            }
        }
        Thread.sleep(2000);
        for (WebElement button : driver.findElement(By.id("content-table")).findElements(By.className("button"))) {
            if ("Cancel".equalsIgnoreCase(button.getAttribute("value"))) {
                button.click();
                break;
            }

        }

    }

    @Test(priority = 3, dependsOnMethods = {"editPrivilegeGroup"}, description = "Add a new privilege group having existing privilege group name")
    public void addPrivilegeGroupNameAlreadyExist() throws InterruptedException {
        Thread.sleep(2000);
        driver.findElement(By.linkText("Privilege Groups")).click();

        driver.findElement(By.linkText("Add new privilege group")).click();
        driver.findElement(By.id("privGroupName")).sendKeys(privilegeGroupName);
        driver.findElement(By.id("selectAll")).click();
        Thread.sleep(2000);
        for (WebElement button : driver.findElement(By.id("content-table")).findElements(By.className("button"))) {
            if ("Save".equalsIgnoreCase(button.getAttribute("value"))) {
                button.click();
                break;
            }

        }
        Thread.sleep(2000);
        Assert.assertEquals(driver.findElement(By.id("dialog")).findElement(By.id("messagebox-error")).getText(),
                            "Failed to create privilege group: " + privilegeGroupName + "; nested exception is: org.apache.axis2.AxisFault: A privilege group with the same name already exists",
                            "Privilege Group Already Exist Pop-up message mismatched: Message : " + driver.findElement(By.id("dialog")).findElement(By.id("messagebox-error")).getText());
        driver.findElement(By.xpath("//button")).click();

    }

    @Test(priority = 4, dependsOnMethods = {"addPrivilegeGroupNameAlreadyExist"}, description = "Delete existing privilege group")
    public void deletePrivilegeGroup() throws InterruptedException {
        dssServerUI.deletePrivilegeGroup(privilegeGroupName);
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() throws InterruptedException {
        try {
            dssServerUI.logOut();
        } finally {
            driver.quit();
        }

    }


}