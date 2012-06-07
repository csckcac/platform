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

package org.wso2.platform.test.core.utils.reportutills;


import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.internal.ResultMap;
import org.testng.internal.Utils;
import org.testng.reporters.XMLReporterConfig;
import org.testng.reporters.XMLStringBuffer;
import org.testng.reporters.XMLSuiteResultWriter;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CustomTestNGrReporter implements IReporter {


    private final XMLReporterConfig config = new XMLReporterConfig();
    private XMLStringBuffer rootBuffer;
    ITestContext testRootContext = null;
    ISuite testRootSuite=null;
    Exception exception = null;

    public CustomTestNGrReporter(ITestContext context, Exception e) {
        testRootContext = context;
        exception = e;
    }

    public CustomTestNGrReporter(ISuite suite, Exception e) {
        testRootSuite = suite;
        exception = e;
    }

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                               String outputDirectory) {
        if (Utils.isStringEmpty(config.getOutputDirectory())) {
            config.setOutputDirectory(outputDirectory);
        }

        rootBuffer = new XMLStringBuffer("");
        rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS);
        writeReporterOutput(rootBuffer);
        for (int i = 0; i < suites.size(); i++) {
            writeSuite(suites.get(i).getXmlSuite(), suites.get(i));
        }
        rootBuffer.pop();
        Utils.writeUtf8File(config.getOutputDirectory(), "testng-results.xml", rootBuffer.toXML());
    }

    private void writeReporterOutput(XMLStringBuffer xmlBuffer) {
        xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
        List<String> output = Reporter.getOutput();
        for (String line : output) {
            if (line != null) {
                xmlBuffer.push(XMLReporterConfig.TAG_LINE);
                xmlBuffer.addCDATA(line);
                xmlBuffer.pop();
            }
        }
        xmlBuffer.pop();
    }

    private void writeSuite(XmlSuite xmlSuite, ISuite suite) {
        switch (config.getFileFragmentationLevel()) {
            case XMLReporterConfig.FF_LEVEL_NONE:
                writeSuiteToBuffer(rootBuffer, suite);
                break;
            case XMLReporterConfig.FF_LEVEL_SUITE:
            case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
                File suiteFile = referenceSuite(rootBuffer, suite);
                writeSuiteToFile(suiteFile, suite);
        }
    }

    private void writeSuiteToFile(File suiteFile, ISuite suite) {
        XMLStringBuffer xmlBuffer = new XMLStringBuffer("");
        writeSuiteToBuffer(xmlBuffer, suite);
        File parentDir = suiteFile.getParentFile();
        if (parentDir.exists() || suiteFile.getParentFile().mkdirs()) {
            Utils.writeFile(parentDir.getAbsolutePath(), "testng-results.xml", xmlBuffer.toXML());
        }
    }

    private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
        String relativePath = suite.getName() + File.separatorChar + "testng-results.xml";
        File suiteFile = new File(config.getOutputDirectory(), relativePath);
        Properties attrs = new Properties();
        attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
        xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
        return suiteFile;
    }

    private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, final ISuite suite) {
        xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
        writeSuiteGroups(xmlBuffer, suite);
        final ISuite iSuite = suite;
        final ITestContext testContext2 = testRootContext;
        final IResultMap resultMap = new ResultMap();
        CustomTestngResults results2 = new CustomTestngResults();
        ISuiteResult iSuiteResult = new ISuiteResult() {
            public String getPropertyFileName() {
                return iSuite.getXmlSuite().getFileName();
            }

            public ITestContext getTestContext() {
                return testRootContext;
            }
        };
        final Map<String, ISuiteResult> results = new HashMap<String, ISuiteResult>();
        results.put("Test", iSuiteResult);
        final Map<String, ISuiteResult> resultsFinal = results;
        ReportSuiteSetter suiteSetter = new ReportSuiteSetter();
        ISuite newSuite = suiteSetter.suiteSetter(suite, resultsFinal);
        for (ITestNGMethod method : suite.getAllMethods()) {
            resultMap.addResult(results2.setResults(newSuite, method, 2, exception), method);
        }

        final IResultMap reportmap = resultMap;
        final ISuite reportsuite = newSuite;

        ReportContextSetter contextSetter = new ReportContextSetter();

        testRootContext = contextSetter.setContext(testContext2, resultMap, newSuite);
        ISuiteResult iSuiteResult2 = new ISuiteResult() {
            public String getPropertyFileName() {
                return reportsuite.getXmlSuite().getFileName();
            }

            public ITestContext getTestContext() {
                return testRootContext;
            }
        };

        final Map<String, ISuiteResult> reportResults1 = new HashMap<String, ISuiteResult>();

        reportResults1.put("Test", iSuiteResult2);
        XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config);
        for (Map.Entry<String, ISuiteResult> result : reportResults1.entrySet()) {
            suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
        }

        xmlBuffer.pop();
    }

    private void writeSuiteGroups(XMLStringBuffer xmlBuffer, ISuite suite) {
        xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
        Map<String, Collection<ITestNGMethod>> methodsByGroups = suite.getMethodsByGroups();
        for (Map.Entry<String, Collection<ITestNGMethod>> entry : methodsByGroups.entrySet()) {
            Properties groupAttrs = new Properties();
            groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, entry.getKey());
            xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
            Set<ITestNGMethod> groupMethods = getUniqueMethodSet(entry.getValue());
            for (ITestNGMethod groupMethod : groupMethods) {
                Properties methodAttrs = new Properties();
                methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.getMethodName());
                methodAttrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, groupMethod.toString());
                methodAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, groupMethod.getRealClass().getName());
                xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
            }
            xmlBuffer.pop();
        }
        xmlBuffer.pop();
    }

    private Properties getSuiteAttributes(ISuite suite) {
        Properties props = new Properties();
        props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());


        Map<String, ISuiteResult> results = suite.getResults();
        Date minStartDate = new Date();
        Date maxEndDate = null;
        for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
            ITestContext testContext = result.getValue().getTestContext();
            Date startDate = testContext.getStartDate();
            Date endDate = testContext.getEndDate();
            if (minStartDate.after(startDate)) {
                minStartDate = startDate;
            }
            if (maxEndDate == null || maxEndDate.before(endDate)) {
                maxEndDate = endDate != null ? endDate : startDate;
            }
        }

        if (maxEndDate == null) {
            maxEndDate = minStartDate;
        }
        addDurationAttributes(config, props, minStartDate, maxEndDate);
        return props;
    }

    public static void addDurationAttributes(XMLReporterConfig config, Properties attributes,
                                             Date minStartDate, Date maxEndDate) {
        SimpleDateFormat format = new SimpleDateFormat(config.getTimestampFormat());
        String startTime = format.format(minStartDate);
        String endTime = format.format(maxEndDate);
        long duration = maxEndDate.getTime() - minStartDate.getTime();

        attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
        attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
        attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
    }

    private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {

        Set<ITestNGMethod> result = new LinkedHashSet<ITestNGMethod>();
        for (ITestNGMethod method : methods) {
            result.add(method);
        }
        return result;
    }
}