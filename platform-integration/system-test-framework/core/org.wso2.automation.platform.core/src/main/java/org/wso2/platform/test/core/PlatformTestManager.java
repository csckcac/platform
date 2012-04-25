package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.*;
import org.wso2.platform.test.core.utils.UnknownArtifactTypeException;

public class PlatformTestManager implements ITestListener {

    private ArtifactManager artifactManager;

    private static final Log log = LogFactory.getLog(PlatformTestManager.class);


    /**
     * Invoked each time before a test will be invoked.
     * The <code>ITestResult</code> is only partially filled with the references to
     * class, method, start millis and status.
     *
     * @param result the partially filled <code>ITestResult</code>
     * @see org.testng.ITestResult#STARTED
     */
    public void onTestStart(ITestResult result) {
        log.info("Running the test method " + result.getMethod().getMethodName());
    }

    /**
     * Invoked each time a test succeeds.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see org.testng.ITestResult#SUCCESS
     */
    public void onTestSuccess(ITestResult result) {
        //To change body of implemented methods use File | Settings | File Templates.
        log.info("On test success..");
    }

    /**
     * Invoked each time a test fails.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see org.testng.ITestResult#FAILURE
     */
    public void onTestFailure(ITestResult result) {
        log.error("On Test Failure : " + result.getThrowable().getMessage());
    }

    /**
     * Invoked each time a test is skipped.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see org.testng.ITestResult#SKIP
     */
    public void onTestSkipped(ITestResult result) {
        log.warn("On Test Skipped");

    }

    /**
     * Invoked each time a method fails but has been annotated with
     * successPercentage and this failure still keeps it within the
     * success percentage requested.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see org.testng.ITestResult#SUCCESS_PERCENTAGE_FAILURE
     */
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    /**
     * Invoked after the test class is instantiated and before
     * any configuration method is called.
     */
    public void onStart(ITestContext context) {
        String currentTestClassName = ((TestRunner) context).getCurrentXmlTest().getClasses().get(0).getName();
        log.info("Before executing the test class :" + currentTestClassName);
        if (currentTestClassName != null) {
            artifactManager = new ArtifactManager(((TestRunner) context).getCurrentXmlTest().getClasses().get(0).getName());
            try {
                artifactManager.deployArtifacts();
            } catch (UnknownArtifactTypeException e) { /*cannot throw the exception */
                log.error("Unknown Artifact type to be deployed ", e);
            } catch (Exception e) {
                log.error("Artifact Deployment Error ", e);
            }
        }
    }

    /**
     * Invoked after all the tests have run and all their
     * Configuration methods have been called.
     */
    public void onFinish(ITestContext context) {
        try {
            assert artifactManager != null;
            artifactManager.cleanArtifacts();
        } catch (UnknownArtifactTypeException e) { /*cannot throw the exception */
            log.error("Unknown Artifact type to be cleared ", e);
        } catch (Exception e) {
            log.error("Artifact Cleaning Error ", e);
        }
    }

    private String getCurrentTestClassName(ITestNGMethod allTestMethods[]) {
        int zeroElement = 0;
        if (allTestMethods.length > 0) {
            return allTestMethods[zeroElement].getTestClass().getName();
        }
        return null;
    }
}


