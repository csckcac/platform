package org.wso2.carbon.hosting.wnagent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.wso2.carbon.hosting.wnagent.WNAgentTest;

/**
 * Created by IntelliJ IDEA.
 * User: damitha
 * Date: 1/19/12
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */

// / /// /// /// /// /// /// /// /// /// /// /// ///
// private static final String wso2hostingHome =
// System.getProperty("wso2hosting.home");
// You should import the Test Case Classes here.

// / /// /// /// /// ///
@RunWith(Suite.class)
// / /// /// /// /// /// /// /// ///
// List the Test Case Classes here.
@SuiteClasses({
// /// ///// ///// ///// ///// ///// ///// ///// /////
// Comment the class(es) that you doesn't want to run.
WNAgentTest.class

})
// / /// /// /// /// /// /// /// /// /// /// /// /// /// /// ////// /// /// ///
// /// /// /// /// ///
// Name your Test Suit Class here, you should point the IntelliJ JUnit
// Configuration to this class.
public class WNAgentTestSuite {
}
