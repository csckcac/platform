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

package org.wso2.carbon.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.reporters.XMLReporter;
import org.testng.xml.XmlSuite;
import org.wso2.carbon.automation.core.utils.reportutills.XmlReporter;

import java.io.File;
import java.util.List;

public class PlatformReportManager implements IReporter {
    private static final Log log = LogFactory.getLog(PlatformReportManager.class);

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> iSuites, String s) {
        MySQLDataHandler mySQLDataHandler = new MySQLDataHandler();
        this.xmlReport(xmlSuites, iSuites, ProductConstant.REPORT_LOCATION + File.separator + "reports");
        for (ISuite suite : iSuites) {
            XmlReporter xmlReporter = new XmlReporter(false, new File(iSuites.get(0).getOutputDirectory() + File.separator + s));
            log.info("----" + iSuites.get(0).getOutputDirectory() + "---------");
            for (ISuiteResult results : suite.getResults().values()) {

                results.getTestContext().getCurrentXmlTest().getName();
                results.getPropertyFileName();
                for (ITestResult results1 : results.getTestContext().getFailedTests().getAllResults()) {
                    ReportEntry reportEntry = new SimpleReportEntry(results1.getTestClass().getName(), results1.getName());
                    xmlReporter.testFailed(reportEntry);
                }
                for (ITestResult results1 : results.getTestContext().getPassedTests().getAllResults()) {
                    ReportEntry reportEntry = new SimpleReportEntry(results1.getTestClass().getName(), "Test");
                    xmlReporter.testSucceeded(reportEntry);
                }
                for (ITestResult results1 : results.getTestContext().getSkippedTests().getAllResults()) {
                    ReportEntry reportEntry = new SimpleReportEntry(results1.getTestClass().getName(), "Test");
                    xmlReporter.testSkipped(reportEntry);
                }
            }
        }
        mySQLDataHandler.writeResultData();
    }

    public void xmlReport(List<XmlSuite> xmlSuites, List<ISuite> iSuites, String out) {
        XMLReporter xmlReporter = new XMLReporter();
        xmlReporter.setFileFragmentationLevel(2);
        xmlReporter.generateReport(xmlSuites, iSuites, out);
    }
}
