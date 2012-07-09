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

package org.wso2.carbon.automation.core.utils.reportutills;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomTestNgReportSetter {
    public void createReport(ITestContext context, Exception e) {
        List<XmlSuite> xmlSuites = new ArrayList<XmlSuite>();
        List<ISuite> iSuites = new ArrayList<ISuite>();
        XmlSuite suite = new XmlSuite();
        suite = context.getSuite().getXmlSuite();
        XmlTest test = new XmlTest(suite);
        List<XmlTest> xmlTests = new ArrayList<XmlTest>();
        xmlTests.add(test);
        suite.setTests(xmlTests);
        xmlSuites.add(suite);
        iSuites.add(context.getSuite());
        String out = ProductConstant.REPORT_LOCATION + File.separator + "reports" + File.separator + suite.getName();
        CustomTestNgReporter customTestNGrReporter = new CustomTestNgReporter(context, e);
        customTestNGrReporter.generateReport(xmlSuites, iSuites, out);
    }


    public void createReport(ISuite testSuite, Exception e) {
        List<XmlSuite> xmlSuites = new ArrayList<XmlSuite>();
        List<ISuite> iSuites = new ArrayList<ISuite>();
        XmlSuite suite = testSuite.getXmlSuite();
        XmlTest test = new XmlTest(suite);
        List<XmlTest> xmlTests = new ArrayList<XmlTest>();
        xmlTests.add(test);
        suite.setTests(xmlTests);
        xmlSuites.add(suite);
        iSuites.add(testSuite);
        String out = ProductConstant.REPORT_LOCATION + File.separator + "reports" + File.separator + suite.getName();
        CustomTestNgReporter customTestNGrReporter = new CustomTestNgReporter(testSuite, e);
        customTestNGrReporter.generateReport(xmlSuites, iSuites, out);
    }
}
