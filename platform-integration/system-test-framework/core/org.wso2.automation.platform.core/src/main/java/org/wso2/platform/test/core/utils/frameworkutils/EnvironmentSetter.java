package org.wso2.platform.test.core.utils.frameworkutils;

import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.dashboardutils.DashboardVariables;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.CoverageSettings;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.DataSource;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.EnvironmentSettings;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.EnvironmentVariables;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.Ravana;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.Selenium;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EnvironmentSetter implements Framework {

    public Properties prop;
    DataSource dataSource = new DataSource();
    DashboardVariables dashboardVariables = new DashboardVariables();
    EnvironmentSettings environmentSettings = new EnvironmentSettings();
    EnvironmentVariables environmentVariables = new EnvironmentVariables();
    CoverageSettings coverageSettings = new CoverageSettings();


    Ravana ravana = new Ravana();
    Selenium selenium = new Selenium();
    FrameworkProperties frameworkProperties = new FrameworkProperties();

    public EnvironmentSetter() {
        this.prop = new ProductUrlGeneratorUtil().getStream();
    }

    public DataSource getDataSource() {
        String driverName = (prop.getProperty("database.driver.name", "com.mysql.jdbc.Driver"));
        String jdbcUrl = (prop.getProperty("jdbc.url", "jdbc:mysql://localhost:3306"));
        String user = (prop.getProperty("db.user", "root"));
        String passwd = (prop.getProperty("db.password", "root123"));
        String dbName = (prop.getProperty("db.name", "testAutomation"));
        String rssDbUser = (prop.getProperty("rss.database.user", "tstusr"));
        String rssDbPassword = (prop.getProperty("rss.database.password", "test1234"));
        dataSource.setDatasource(driverName, jdbcUrl, user, passwd, dbName, rssDbUser, rssDbPassword);
        return dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        boolean enabledRavana = Boolean.parseBoolean(prop.getProperty("ravana.test", "true"));
        boolean enableDeploymentFramework = Boolean.parseBoolean(prop.getProperty("deployment.framework.enable", "false"));
        boolean enableSelenium = Boolean.parseBoolean(prop.getProperty("remote.selenium.web.driver.start", "false"));
        boolean runOnStratos = Boolean.parseBoolean(prop.getProperty("stratos.test", "false"));
        boolean enebleStratosPort = Boolean.parseBoolean(prop.getProperty("port.enable"));
        boolean enableWebContextRoot = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable", "false"));
        boolean enableCluster = Boolean.parseBoolean(prop.getProperty("cluster.enable", "false"));
        environmentSettings.setEnvironmentSettings
                (enableDeploymentFramework, runOnStratos, enableSelenium, enabledRavana,
                 enebleStratosPort, enableWebContextRoot, enableCluster);
        return environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        String keystorePath;
        String keyStrorePassword;
        String deploymentFrameworkPath = (prop.getProperty("deployment.framework.home", "/"));
        List<String> productList = Arrays.asList((prop.getProperty("product.list", "AS").split(",")));
        int deploymentDelay = Integer.parseInt(prop.getProperty("service.deployment.delay", "1000"));
        String ldapUserName = (prop.getProperty("ldap.username", "admin"));
        String ldapPasswd = (prop.getProperty("ldap.password", "admin"));
        if (Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            keystorePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                           "keystores" + File.separator + "stratos" + File.separator + "wso2carbon.jks";
            keyStrorePassword = (prop.getProperty("truststore.password", "wso2carbon"));
        } else {
            keystorePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                           "keystores" + File.separator + "products" + File.separator + "wso2carbon.jks";
            keyStrorePassword = (prop.getProperty("truststore.password", "wso2carbon"));
        }
        environmentVariables.setEnvironmentVariables(deploymentFrameworkPath, productList,
                                                     deploymentDelay, ldapUserName, ldapPasswd,
                                                     keystorePath, keyStrorePassword);
        return environmentVariables;
    }

    public Ravana getRavana() {
        String jdbc_Url = (prop.getProperty("ravana.jdbc.url", "jdbc:mysql://localhost:3306"));
        String dbUser = (prop.getProperty("ravana.db.user", "root"));
        String dbPassword = (prop.getProperty("ravana.db.password", "root123"));
        String frameworkPath = (prop.getProperty("ravana.framework.path", "/home/krishantha/svn/ravana/ravana2"));
        String testStatus = (prop.getProperty("ravana.test", "true"));
        ravana.setRavana(jdbc_Url, dbUser, dbPassword, frameworkPath, testStatus);
        return ravana;

    }

    public Selenium getSelenium() {
        String browserName = prop.getProperty("browser.name", "firefox");
        String chromrDriverPath = prop.getProperty("path.to.chrome.driver", "/path/to/chrome/driver");
        boolean remoteWebDriver = Boolean.parseBoolean(prop.getProperty("remote.selenium.web.driver.start", "false"));
        String remoteWebDriverUrl = prop.getProperty("remote.webdirver.url", "http://10.100.3.95:3002/wd");
        selenium.setSelenium(browserName, chromrDriverPath, remoteWebDriver, remoteWebDriverUrl);
        return selenium;
    }

    public FrameworkProperties getFrameworkProperties() {
        frameworkProperties.setDataSource(getDataSource());
        frameworkProperties.setEnvironmentSettings(getEnvironmentSettings());
        frameworkProperties.setEnvironmentVariables(getEnvironmentVariables());
        frameworkProperties.setRavana(getRavana());
        frameworkProperties.setSelenium(getSelenium());
        frameworkProperties.setDashboardVariables(getDashboardVariables());
        frameworkProperties.setCoverageSettings(getCoverageSettings());

        return frameworkProperties;
    }

    public DashboardVariables getDashboardVariables() {
        String driverName = (prop.getProperty("dashboard.database.driver.name", "com.mysql.jdbc.Driver"));
        String jdbcUrl = (prop.getProperty("dashboard.jdbc.url", "jdbc:mysql://localhost:3306"));
        String user = (prop.getProperty("dashboard.db.user", "root"));
        String passwd = (prop.getProperty("dashboard.db.password", "root"));
        String dbName = (prop.getProperty("dashboard.db.name", "FRAMEWORK_DB1"));
        String isEnableDashboard = (prop.getProperty("dashboard.enable", "false"));
        dashboardVariables.setDashboardVariables(driverName, jdbcUrl, user, passwd, dbName, isEnableDashboard);
        return dashboardVariables;
    }

    public CoverageSettings getCoverageSettings() {
        boolean coverageEnable = Boolean.parseBoolean(prop.getProperty("coverage.enable", "false"));
        Map coverageHome =new HashMap<String, String>();
        if (prop.getProperty("greg.carbon.home") != null) {
            coverageHome.put(ProductConstant.GREG_SERVER_NAME, prop.getProperty("greg.carbon.home"));
        }
        if (prop.getProperty("as.carbon.home") != null) {
            coverageHome.put(ProductConstant.APP_SERVER_NAME, prop.getProperty("as.carbon.home"));
        }
        if (prop.getProperty("esb.carbon.home") != null) {
            coverageHome.put(ProductConstant.ESB_SERVER_NAME, prop.getProperty("esb.carbon.home"));
        }
        if (prop.getProperty("bps.carbon.home") != null) {
            coverageHome.put(ProductConstant.BPS_SERVER_NAME, prop.getProperty("bps.carbon.home"));
        }
        if (prop.getProperty("bam.carbon.home") != null) {
            coverageHome.put(ProductConstant.BAM_SERVER_NAME, prop.getProperty("bam.carbon.home"));
        }
        if (prop.getProperty("brs.carbon.home") != null) {
            coverageHome.put(ProductConstant.BRS_SERVER_NAME, prop.getProperty("brs.carbon.home"));
        }
        if (prop.getProperty("cep.carbon.home") != null) {
            coverageHome.put(ProductConstant.CEP_SERVER_NAME, prop.getProperty("cep.carbon.home"));
        }
        if (prop.getProperty("dss.carbon.home") != null) {
            coverageHome.put(ProductConstant.DSS_SERVER_NAME, prop.getProperty("dss.carbon.home"));
        }
        if (prop.getProperty("is.carbon.home") != null) {
            coverageHome.put(ProductConstant.IS_SERVER_NAME, prop.getProperty("is.carbon.home"));
        }
        if (prop.getProperty("mb.carbon.home") != null) {
            coverageHome.put(ProductConstant.MB_SERVER_NAME, prop.getProperty("mb.carbon.home"));
        }
        if (prop.getProperty("ms.carbon.home") != null) {
            coverageHome.put(ProductConstant.MS_SERVER_NAME, prop.getProperty("ms.carbon.home"));
        }
        if (prop.getProperty("gs.carbon.home") != null) {
            coverageHome.put(ProductConstant.GS_SERVER_NAME, prop.getProperty("gs.carbon.home"));
        }
        coverageSettings.setCoverage(coverageEnable,coverageHome);
        return coverageSettings;
    }
}
