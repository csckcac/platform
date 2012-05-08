package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;

import java.util.Arrays;
import java.util.List;

public class PlatformSuiteManager implements ISuiteListener {

    private static final Log log = LogFactory.getLog(PlatformSuiteManager.class);

    /**
     * This method is invoked before the SuiteRunner starts.
     */
    public void onStart(ISuite suite) {
        setKeyStoreProperties();
        try {
            if (suite.getParameter("server.list") != null) {
                startMultipleServers(suite.getParameter("server.list"));
            }
        } catch (Exception e) {  /*cannot throw the exception */
            log.error(e);
            Assert.fail("Fail start servers " + e.getMessage());
        }
    }

    /**
     * This method is invoked after the SuiteRunner has run all
     * the test suites.
     */
    public void onFinish(ISuite suite) {
        try {
            if (suite.getParameter("server.list") != null) {
                stopMultipleServers(suite.getParameter("server.list"));
            }
        } catch (Exception e) { /*cannot throw the exception */
            log.error(e);
            Assert.fail("Fail to stop servers " + e.getMessage());
        }
    }

    /**
     * Responsible for stopping multiple servers after test execution.
     * <p/>
     * Add the @AfterSuite TestNG annotation in the method overriding this method
     *
     * @throws Exception if an error occurs while in server stop process
     */
    protected void stopMultipleServers(String serverList) throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        if (environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework()
            && !environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            List<String> productList = Arrays.asList(serverList.split(","));
            log.info("Stopping all server");
            ServerGroupManager.shutdownServers(productList);
        }
    }

    /**
     * Responsible for starting the multiple servers befor
     * e test execution.
     * Also, initialize all required system properties before the test.
     * <p/>
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     *
     * @throws Exception if an error occurs while in server startup process
     */
    private void startMultipleServers(String serverList) throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        boolean deploymentEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework();
        boolean startosEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        if (deploymentEnabled && !startosEnabled) {
            List<String> productList = Arrays.asList(serverList.split(","));
            log.info("Starting all servers");
            ServerGroupManager.startServers(productList);
        } else {
            List<String> productList = Arrays.asList(serverList.split(","));
            new UserPopulator().populateUsers(productList);
        }
    }

    private void setKeyStoreProperties() {
        EnvironmentBuilder builder = new EnvironmentBuilder();
        System.setProperty("javax.net.ssl.trustStore", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeyStrorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        log.debug("javax.net.ssl.trustStore :" + System.getProperty("javax.net.ssl.trustStore"));
        log.debug("javax.net.ssl.trustStorePassword :" + System.getProperty("javax.net.ssl.trustStorePassword"));
        log.debug("javax.net.ssl.trustStoreType :" + System.getProperty("javax.net.ssl.trustStoreType"));
    }


}
