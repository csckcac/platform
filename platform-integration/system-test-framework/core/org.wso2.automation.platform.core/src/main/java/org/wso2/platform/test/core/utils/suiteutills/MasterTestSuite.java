package org.wso2.platform.test.core.utils.suiteutills;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.platform.test.core.PlatformPriorityManager;
import org.wso2.platform.test.core.PlatformReportManager;
import org.wso2.platform.test.core.ProductConstant;

import java.io.File;
import java.util.*;

public class MasterTestSuite {

    public TestNG superSuite(String SuiteName, List<SuiteVariables> suiteVariablesList) {
        XmlSuite suite = new XmlSuite();
        suite.setName(SuiteName);
        suite.setVerbose(1);
        suite.setThreadCount(2);
        Map<String, String> parameters = new HashMap<String, String>();
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
        listnerClasses.add(org.wso2.platform.test.core.PlatformTestManager.class);
        listnerClasses.add(org.wso2.platform.test.core.PlatformSuiteManager.class);
        listnerClasses.add(PlatformReportManager.class);
        listnerClasses.add(PlatformPriorityManager.class);
        tng.setListenerClasses(listnerClasses);
        tng.setDefaultSuiteName("DefaultSuite");
        tng.setXmlSuites(Arrays.asList(new XmlSuite[]{suite}));
        tng.setOutputDirectory(ProductConstant.REPORT_LOCATION + File.separator + "reports");
        return tng;
    }
}
