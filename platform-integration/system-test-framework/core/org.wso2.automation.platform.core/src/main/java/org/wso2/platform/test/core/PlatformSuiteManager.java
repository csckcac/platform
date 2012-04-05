package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;

public class PlatformSuiteManager implements ISuiteListener {

    private static final Log log = LogFactory.getLog(PlatformSuiteManager.class);

    /**
     * This method is invoked before the SuiteRunner starts.
     */
    public void onStart(ISuite suite) {
        log.info("Starting all servers");
        setKeyStoreProperties();
        try {
            startMulitpleServers();
        } catch (Exception e) {  /*cannot throw the exception */
            Assert.fail("Fail start servers " + e.getMessage());
        }
    }

    /**
     * This method is invoked after the SuiteRunner has run all
     * the test suites.
     */
    public void onFinish(ISuite suite) {
        log.info("Stopping all server");
        try {
            stopMultipleServers();
        } catch (Exception e) { /*cannot throw the exception */
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
    protected void stopMultipleServers() throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        if ( environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework()
             && !environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            ServerGroupManager.shutdownServers();
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
    private void startMulitpleServers() throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        boolean deploymentEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework();
        boolean startosEnabled =
                !environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        if (deploymentEnabled && startosEnabled) {
            ServerGroupManager.startServers();
        } else {
            new UserPopulator().populateUsers();
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
