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

import org.testng.IClass;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.Set;

public class CustomTestngResults {

    public ITestResult setResults(final ISuite suite, final ITestNGMethod methods, int customStatus,
                                  Exception exception) {
        final int resultStatus = customStatus;
        final Exception reportException = exception;
        ITestResult testResult = new ITestResult() {
            public int getStatus() {

                return resultStatus;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setStatus(int status) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public ITestNGMethod getMethod() {
                return methods;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object[] getParameters() {
                return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setParameters(Object[] parameters) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public IClass getTestClass() {
                return methods.getTestClass();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Throwable getThrowable() {
                return reportException;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setThrowable(Throwable throwable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public long getStartMillis() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public long getEndMillis() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setEndMillis(long millis) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isSuccess() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getHost() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Object getInstance() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getTestName() {
                return suite.getName();  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int compareTo(ITestResult o) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
        return testResult;
    }
}
