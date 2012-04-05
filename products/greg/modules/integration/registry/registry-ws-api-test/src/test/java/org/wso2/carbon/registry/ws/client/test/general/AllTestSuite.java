/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.ws.client.test.general;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.wso2.carbon.integration.core.FrameworkSettings;


public class AllTestSuite extends TestSuite {

    public static TestSuite suite() {
        return suite(new TestSuite());
    }

    public static TestSuite suite(TestSuite testSuite) {
        FrameworkSettings.getProperty();
        String frameworkPath = FrameworkSettings.getFrameworkPath();
        System.setProperty("java.util.logging.config.file", frameworkPath + "/repository/conf/log4j.properties");

        testSuite.addTestSuite(VersionHandlingTest.class);
        testSuite.addTestSuite(TestContentStream.class);
        testSuite.addTestSuite(TestAssociation.class);
        testSuite.addTestSuite(TestPaths.class);
        testSuite.addTestSuite(TestCopy.class);
		testSuite.addTestSuite(UserSecurityTest.class);
        testSuite.addTestSuite(QueryTest.class);
		testSuite.addTestSuite(RemotePerfTest.class);
        testSuite.addTestSuite(ContinuousOperations.class);
        testSuite.addTestSuite(RenameTest.class);
		testSuite.addTestSuite(TestResources.class);
		testSuite.addTestSuite(TestMove.class);
		testSuite.addTestSuite(TestTagging.class);
		testSuite.addTestSuite(RatingTest.class);
		testSuite.addTestSuite(ResourceHandling.class);
		testSuite.addTestSuite(OnDemandContentTest.class);
		testSuite.addTestSuite(PropertiesTest.class);
		testSuite.addTestSuite(CommentTest.class);

        return testSuite;
    }
}