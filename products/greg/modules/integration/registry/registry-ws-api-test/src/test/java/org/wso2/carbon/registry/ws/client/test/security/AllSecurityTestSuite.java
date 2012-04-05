/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.client.test.security;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.integration.core.ServerUtils;
import org.wso2.carbon.registry.ws.client.test.general.AllTestSuite;
import org.wso2.carbon.registry.ws.client.test.general.TestSetup;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class AllSecurityTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.wso2.carbon.registry.ws.client.test.security");
		//$JUnit-BEGIN$
        suite.addTest(new TestSetup() {
            {
                try {
                    setName(TestSetup.class.getMethod("testTemplate").getName());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setUp() throws Exception {
                System.setProperty("run.with.security", "true");
                String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
                ServerUtils.shutdown();
                System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
                try {
                    FileUtils.forceDelete(new File(carbonHome).getParentFile());
                } catch (IOException ignored) {
                }
                super.setUp();
            }
        });
        AllTestSuite.suite(suite);
        suite.addTest(new TestSetup() {
            {
                try {
                    setName(TestSetup.class.getMethod("testTemplate").getName());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setUp() throws Exception {
                System.clearProperty("run.with.security");
                super.setUp();
            }
        });
		//$JUnit-END$
		return suite;
	}

}
