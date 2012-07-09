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

import org.testng.IInvokedMethod;
import org.testng.IObjectFactory;
import org.testng.IObjectFactory2;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.SuiteRunState;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportSuiteSetter {
    public ISuite suiteSetter(ISuite reportSuite, Map<String, ISuiteResult> resultMap) {
        final ISuite suite = reportSuite;
        final Map<String, ISuiteResult> resultsFinal = resultMap;
        ISuite newSuite = new ISuite() {
            public String getName() {
                return suite.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Map<String, ISuiteResult> getResults() {
                return resultsFinal;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IObjectFactory getObjectFactory() {
                return suite.getObjectFactory();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IObjectFactory2 getObjectFactory2() {
                return suite.getObjectFactory2();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getOutputDirectory() {
                return suite.getOutputDirectory();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getParallel() {
                return suite.getParallel();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getParameter(String parameterName) {
                return suite.getParameter(parameterName);  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
                return suite.getMethodsByGroups();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Collection<ITestNGMethod> getInvokedMethods() {
                return suite.getInvokedMethods();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<IInvokedMethod> getAllInvokedMethods() {
                return suite.getAllInvokedMethods();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Collection<ITestNGMethod> getExcludedMethods() {
                return suite.getExcludedMethods();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void run() {
                suite.run();
            }

            public String getHost() {
                return suite.getHost();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public SuiteRunState getSuiteState() {
                SuiteRunState state = new SuiteRunState();
                state.failed();
                return state;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public IAnnotationFinder getAnnotationFinder(String type) {
                return suite.getAnnotationFinder(type);  //To change body of implemented methods use File | Settings | File Templates.
            }

            public XmlSuite getXmlSuite() {
                return suite.getXmlSuite();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void addListener(ITestNGListener listener) {

            }

            public List<ITestNGMethod> getAllMethods() {
                return suite.getAllMethods();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object getAttribute(String name) {
                return suite.getAttribute(name);  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String name, Object value) {

            }

            public Set<String> getAttributeNames() {
                return suite.getAttributeNames();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object removeAttribute(String name) {
                return suite.removeAttribute(name);  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        return newSuite;
    }
}
