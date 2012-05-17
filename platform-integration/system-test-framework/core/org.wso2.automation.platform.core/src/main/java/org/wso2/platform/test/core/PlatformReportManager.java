package org.wso2.platform.test.core;

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
import org.wso2.platform.test.core.utils.reportutills.XmlReporter;

import java.io.File;
import java.util.List;

public class PlatformReportManager implements IReporter {
    private static final Log log = LogFactory.getLog(PlatformReportManager.class);

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> iSuites, String s) {
        MySQLDataHandler mySQLDataHandler = new MySQLDataHandler();
        this.xmlReport(xmlSuites,iSuites,ProductConstant.REPORT_LOCATION + File.separator + "reports");
        for(ISuite suite:iSuites)
        {
            XmlReporter xmlReporter = new XmlReporter(false,new File(iSuites.get(0).getOutputDirectory()+File.separator+s));
            log.info("----"+iSuites.get(0).getOutputDirectory()+"---------");
            for(ISuiteResult results:suite.getResults().values())
            {
                results.getTestContext().getCurrentXmlTest().getName();
                results.getPropertyFileName();
                for(ITestResult results1:results.getTestContext().getFailedTests().getAllResults())
                {
                    ReportEntry reportEntry= new SimpleReportEntry(results1.getTestClass().getName(),results1.getName());
                    xmlReporter.testFailed(reportEntry);
                }
                for(ITestResult results1:results.getTestContext().getPassedTests().getAllResults())
                {
                    ReportEntry reportEntry= new SimpleReportEntry(results1.getTestClass().getName(),"Test");
                    xmlReporter.testSucceeded(reportEntry);
                }
                for(ITestResult results1:results.getTestContext().getSkippedTests().getAllResults())
                {
                    ReportEntry reportEntry= new SimpleReportEntry(results1.getTestClass().getName(),"Test");
                    xmlReporter.testSkipped(reportEntry);
                }

            }
        }
         mySQLDataHandler.writeResultData();
    }
    
    public void xmlReport(List<XmlSuite> xmlSuites,List<ISuite> iSuites,String out)
    {
        XMLReporter xmlReporter =new XMLReporter();
        xmlReporter.setFileFragmentationLevel(2);
        xmlReporter.generateReport(xmlSuites,iSuites,out);
    }
}
