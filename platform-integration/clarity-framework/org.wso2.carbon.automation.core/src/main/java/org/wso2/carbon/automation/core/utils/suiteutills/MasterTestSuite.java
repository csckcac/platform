package org.wso2.carbon.automation.core.utils.suiteutills;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.carbon.automation.core.PlatformExecutionManager;
import org.wso2.carbon.automation.core.PlatformPriorityManager;
import org.wso2.carbon.automation.core.PlatformTestManager;
import org.wso2.carbon.automation.core.PlatformReportManager;
import org.wso2.carbon.automation.core.PlatformSuiteManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;
import java.util.*;

public class MasterTestSuite {
    private static final Log log = LogFactory.getLog(MasterTestSuite.class);
    private Map<String, String> parameters = new HashMap<String, String>();
    private static int counter = 0;


    public TestNG superSuite(String SuiteName, List<SuiteVariables> suiteVariablesList) {
        XmlSuite suite = new XmlSuite();
        suite.setName(SuiteName);
        suite.setVerbose(1);
        suite.setThreadCount(2);
        log.info("[TESTAUTOMATION]----" + SuiteName);

        parameters.put("first-name", "Cedric");

        suite.setParameters(parameters);
        for (SuiteVariables suiteVariables : suiteVariablesList) {

            XmlTest test = new XmlTest(suite);
            test.setName(suiteVariables.geTestName());
            test.setExcludedGroups(Arrays.asList(suiteVariables.getExcludeGrops()));
            XmlClass[] classes = new XmlClass[]{
                    new XmlClass(suiteVariables.getTestClass()),
            };
            test.setXmlClasses(Arrays.asList(classes));
        }
        TestNG tng = new TestNG();
        List<Class> listnerClasses = new ArrayList<Class>();
        listnerClasses.add(PlatformTestManager.class);
        listnerClasses.add(PlatformSuiteManager.class);
        listnerClasses.add(PlatformReportManager.class);

        System.out.println("XXXXXXXX + count " + counter);

        if (counter == 0) {
            listnerClasses.add(PlatformExecutionManager.class);
            counter++;
        }
        listnerClasses.add(PlatformPriorityManager.class);
        tng.setListenerClasses(listnerClasses);
        tng.setDefaultSuiteName(SuiteName);
        tng.setXmlSuites(Arrays.asList(new XmlSuite[]{suite}));
        tng.setOutputDirectory(ProductConstant.REPORT_LOCATION + File.separator + "reports");

        return tng;
    }

    public void setServerList(String serverList) {
        parameters.put("server.list", serverList);
    }

}
