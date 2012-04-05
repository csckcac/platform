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

package org.wso2.carbon.registry.metadata.test.policy;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

/**
 * Add Policy Test from File System and From URL Functionality Tests
 */

public class PolicyDeleteTest extends TestTemplate {

    private static final Log log = LogFactory.getLog(PolicyDeleteTest.class);
    private String policyPath = "/_system/governance/policies/";
    private String testRes1 = "RMpolicy3.xml";
    private String testRes2 = "sample_policy.xml";
    private ResourceAdminServiceStub resourceAdminServiceStub;

    @Override
    public void init() {
        log.info("Initializing Add/Remove Policy Registry Test");
        log.debug("Add/Remove Policy Registry Test Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);

        try {
            /**
             * delete the added resource
             */
            resourceAdminServiceStub.delete(policyPath + testRes1);
            resourceAdminServiceStub.delete(policyPath + testRes2);

            /**
             * check if the deleted file exists in registry
             */

            if ((isFileExist(sessionCookie, policyPath, testRes1) == false) && (isFileExist(sessionCookie, policyPath, testRes2) == false)) {
                log.info("Policy files successfully deleted from the registry");
            } else {
                log.error("Policy files have not been deleted from the registry");
                Assert.fail("Policy Files have not been deleted from the registry");
            }

        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content : " + e.getMessage());
        }
    }

    public boolean isFileExist(String sessionCookie, String resourcePath, String resourceName) throws  Exception{
        boolean isResourceExist = false;
        CollectionContentBean collectionContentBean = null;
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);
        collectionContentBean = resourceAdminServiceStub.getCollectionContent(resourcePath);
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(resourcePath + resourceName)) ;
                isResourceExist = true;
            }
        }
        return isResourceExist;
    }


    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }
}
