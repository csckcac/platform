package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.reportutills.CustomTestngReportSetter;
import org.wso2.platform.test.core.utils.serverutils.ServerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlatformSuiteManager implements ISuiteListener {

    private static final Log log = LogFactory.getLog(PlatformSuiteManager.class);
    ServerManager serverManager = null;
    List<ServerManager> serverList = new ArrayList<ServerManager>();
    EnvironmentBuilder environmentBuilder;

    /**
     * This method is invoked before the SuiteRunner starts.
     */
    public synchronized void onStart(ISuite suite) {
        setKeyStoreProperties();
        environmentBuilder = new EnvironmentBuilder();
        try {

            boolean deploymentEnabled =
                    environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework();
            boolean startosEnabled =
                    environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
            List<String> defaultProductList = environmentBuilder.getFrameworkSettings().getEnvironmentVariables().getProductList();
            if (startosEnabled) {
                //stratos user populate on manager. there for product lis set to null

             //   new UserPopulator().populateUsers(null);
            } else {
             /*   if (environmentBuilder.getFrameworkSettings().getCoverageSettings().getCoverageEnable()) {
                    for (Object carbonHomePath : environmentBuilder.getFrameworkSettings().getCoverageSettings().getCarbonHome().values()) {
                        CodeCoverageUtils.instrument(carbonHomePath.toString());
                        serverManager = new ServerManager(carbonHomePath.toString());
                        serverManager.start();
                        serverList.add(serverManager);
                    }
                    CodeCoverageUtils.init();
                }*/

                if (suite.getParameter("server.list") != null) {
                    List<String> productList = Arrays.asList(suite.getParameter("server.list").split(","));
                    if (deploymentEnabled) {
                        log.info("Starting all servers");
                        ServerGroupManager.startServers(productList);
                    }
                 //   new UserPopulator().populateUsers(productList);
                } else {
                  //  new UserPopulator().populateUsers(defaultProductList);
                }
            }

        } catch (Exception e) {  /*cannot throw the exception */
            log.error(e);
            CustomTestngReportSetter reportSetter = new CustomTestngReportSetter();
            reportSetter.createReport(suite, e);
        }
    }

    /**
     * This method is invoked after the SuiteRunner has run all
     * the test suites.
     */
    public void onFinish(ISuite suite) {
        try {

          /*  if (serverList.size() != 0) {
                for (ServerManager server : serverList) {
                    server.shutdown();
                }
            }
            for(ServerManager servers:serverList)
            {
                servers.shutdown();
            }
            if (environmentBuilder.getFrameworkSettings().getCoverageSettings().getCoverageEnable()) {
                for (Object carbonHomePath : environmentBuilder.getFrameworkSettings().getCoverageSettings().getCarbonHome().values()) {
                    CodeCoverageUtils.generateReports(carbonHomePath.toString());
                }
            }*/
            stopMultipleServers(suite.getParameter("server.list"));
            if (suite.getParameter("server.list") != null) {

            }

        } catch (Exception e) { /*cannot throw the exception */
            log.error(e);
            CustomTestngReportSetter reportSetter = new CustomTestngReportSetter();
          reportSetter.createReport(suite, e);

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
    /*private void startMultipleServers(List<String> productList) throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        boolean deploymentEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework();
        boolean startosEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
        if (deploymentEnabled && !startosEnabled) {
            log.info("Starting all servers");
            ServerGroupManager.startServers(productList);
        }
    }*/
    private void setKeyStoreProperties() {
        EnvironmentBuilder builder = new EnvironmentBuilder();
        System.setProperty("javax.net.ssl.trustStore", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeyStrorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        if (log.isDebugEnabled()) {
            log.debug("javax.net.ssl.trustStore :" + System.getProperty("javax.net.ssl.trustStore"));
            log.debug("javax.net.ssl.trustStorePassword :" + System.getProperty("javax.net.ssl.trustStorePassword"));
            log.debug("javax.net.ssl.trustStoreType :" + System.getProperty("javax.net.ssl.trustStoreType"));
        }
    }


}
