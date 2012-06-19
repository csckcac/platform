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

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

public class ReportContextSetter {

    public ITestContext setContext(ITestContext testContext, IResultMap result, ISuite suite) {
        final ITestContext testContext2 = testContext;
        final IResultMap resultMap = result;
        final ISuite reportsuite = suite;
        final ITestContext context = new ITestContext() {
            public String getName() {
                return testContext2.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getStartDate() {
                return testContext2.getStartDate();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getEndDate() {

                return testContext2.getStartDate();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedButWithinSuccessPercentageTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedTests() {
                return resultMap;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getIncludedGroups() {
                return testContext2.getIncludedGroups();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getExcludedGroups() {
                return testContext2.getExcludedGroups();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getOutputDirectory() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ISuite getSuite() {
                return reportsuite;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ITestNGMethod[] getAllTestMethods() {
                return new ITestNGMethod[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getHost() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Collection<ITestNGMethod> getExcludedMethods() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public XmlTest getCurrentXmlTest() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object getAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String name, Object value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set<String> getAttributeNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object removeAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return context;
    }


    public ITestContext setContext(IResultMap result, ISuite suite) {

        final IResultMap resultMap = result;
        final ISuite reportsuite = suite;
        final ITestContext context = new ITestContext() {
            public String getName() {
                return reportsuite.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getStartDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getEndDate() {

                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedButWithinSuccessPercentageTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedTests() {
                return resultMap;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getIncludedGroups() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getExcludedGroups() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getOutputDirectory() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ISuite getSuite() {
                return reportsuite;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ITestNGMethod[] getAllTestMethods() {
                return new ITestNGMethod[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getHost() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Collection<ITestNGMethod> getExcludedMethods() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public XmlTest getCurrentXmlTest() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object getAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String name, Object value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set<String> getAttributeNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object removeAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return context;
    }
    public ITestContext setContext(IResultMap result,XmlSuite suite) {

        final IResultMap resultMap = result;
        final XmlSuite reportsuite = suite;
        final ITestContext context = new ITestContext() {
            public String getName() {
                return reportsuite.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getStartDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Date getEndDate() {

                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedButWithinSuccessPercentageTests() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedTests() {
                return resultMap;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getIncludedGroups() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String[] getExcludedGroups() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getOutputDirectory() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ISuite getSuite() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ITestNGMethod[] getAllTestMethods() {
                return new ITestNGMethod[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getHost() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Collection<ITestNGMethod> getExcludedMethods() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getPassedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getSkippedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IResultMap getFailedConfigurations() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public XmlTest getCurrentXmlTest() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object getAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String name, Object value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set<String> getAttributeNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object removeAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return context;
    }
}
