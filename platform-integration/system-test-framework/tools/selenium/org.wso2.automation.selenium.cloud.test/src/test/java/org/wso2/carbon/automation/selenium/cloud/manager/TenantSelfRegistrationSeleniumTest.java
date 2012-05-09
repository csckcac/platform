package org.wso2.carbon.automation.selenium.cloud.manager;

import com.thoughtworks.selenium.Selenium;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.admin.service.AdminServiceTenantMgtServiceAdmin;
import org.wso2.carbon.admin.service.TenantSelfRegistrationClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.platform.test.core.BrowserManager;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.seleniumutils.StratosUserLogin;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Set;

import static org.testng.Assert.assertTrue;

public class TenantSelfRegistrationSeleniumTest {

    private static final Log log = LogFactory.getLog(TenantSelfRegistrationSeleniumTest.class);
    private static WebDriver driver;
    String productName = "manager";
    private static UserInfo userDetails;
    private static String baseURL;
    String userName;
    String password;
    String domain;
    public static final String CAPTCHA_DETAILS_PATH =
            "/_system/config/repository/components/org.wso2.carbon.captcha-details";

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, InterruptedException {
        userDetails = UserListCsvReader.getUserInfo(0);
        userName = userDetails.getUserName();
        password = userDetails.getPassword();
        domain = userDetails.getDomain();
        baseURL = new ProductUrlGeneratorUtil().getServiceHomeURL(
                ProductConstant.MANAGER_SERVER_NAME);
        log.info("baseURL is :" + baseURL);
        driver = BrowserManager.getWebDriver();
        driver.get(baseURL);
    }

    @Test(groups = {"wso2.manager"}, description = "self tenant registration", priority = 1)
    public void testLoginToManager() throws Exception {

        driver.findElement(By.xpath("/html/body/div/div[2]/div[2]/div/a/img")).click();

        driver.findElement(By.xpath("//*[@id=\"domain\"]")).sendKeys("wso2automationdomain" + 2 + ".org");
        driver.findElement(By.xpath("//*[@id=\"admin-firstname\"]")).sendKeys("admin123");
        driver.findElement(By.xpath("//*[@id=\"admin-lastname\"]")).sendKeys("admin123");
        driver.findElement(By.xpath("//*[@id=\"admin\"]")).sendKeys("admin123");
        driver.findElement(By.xpath("//*[@id=\"admin-password\"]")).sendKeys("admin123");
        driver.findElement(By.xpath("//*[@id=\"admin-password-repeat\"]")).sendKeys("admin123");
        driver.findElement(By.xpath("//*[@id=\"admin-email\"]")).sendKeys("wso2automation@gmail.com");
        Set<Cookie> sessions = driver.manage().getCookies();
        for (Cookie sess : sessions) {
            if (sess.getName().equals("JSESSIONID")) {
                System.out.println(sess.getExpiry());
                FrameworkProperties manProperties =
                        FrameworkFactory.getFrameworkProperties(ProductConstant.MANAGER_SERVER_NAME);
                System.out.println(manProperties.getProductVariables().getBackendUrl());
                TenantSelfRegistrationClient client =
                        new TenantSelfRegistrationClient(sess.getValue(), manProperties.getProductVariables().getBackendUrl());
                System.out.println(client.checkDomainAvailability("wso2.org"));
                System.out.println(client.generateRandomCaptcha().getImagePath());
                Thread.sleep(5000);


                String path = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr/td/div[3]/table/tbody/tr[9]/td[2]/div/img")).getAttribute("src");
                System.out.println(path);
                String newPath = path.substring(path.lastIndexOf("resource") + "resource".length(), path.length());
                System.out.println("New path " + newPath);

                String newDetailPath = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                System.out.println("Detail Path " + newDetailPath);

                String value = getSecretKey(newPath, newDetailPath);
                driver.findElement(By.xpath("id('captcha-user-answer')")).sendKeys(value);
                driver.findElement(By.xpath("//*[@id=\"activateButton\"]")).click();
                driver.findElement(By.xpath("//*[@id=\"submit-button\"]")).click();
                assertTrue(driver.getPageSource().contains("Account Created Successfully"), "Account creation was not successful");
                clickLink();

            }
        }
    }

    private String getSecretKey(String path, String resourceName)
            throws LoginAuthenticationExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {

        UserInfo info = UserListCsvReader.getUserInfo(0);
        EnvironmentBuilder builder = new EnvironmentBuilder().manager(0);

        ManageEnvironment manageEnvironment = builder.build();
        AdminServiceResourceAdmin resourceAdmin = new AdminServiceResourceAdmin(manageEnvironment.getManager().getBackEndUrl());
        String sessionCookie = manageEnvironment.getManager().getSessionCookie();

        ResourceData[] registryResource = resourceAdmin.getResource(sessionCookie, path);
        resourceAdmin.getHumanReadableMediaTypes(sessionCookie, path);


        String value = resourceAdmin.getProperty(sessionCookie, CAPTCHA_DETAILS_PATH + "/" + resourceName, "captcha-text");

        System.out.println("Thee value " + value);
        return value;
    }
    
    private void clickLink(){
        driver.get("https://mail.google.com/mail");
        driver.findElement(By.id("Email")).sendKeys("wso2automation");
        driver.findElement(By.id("Passwd")).sendKeys("wso2automation123");
        driver.findElement(By.id("signIn")).click();
        driver.findElement(By.id("gbqfq")).sendKeys("from:stratoslive-noreply@wso2.com");
        driver.findElement(By.id("gbqfb")) .click();
    }


}
