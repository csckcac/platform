/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.mb.integration.tests;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.event.stub.internal.TopicManagerAdminServiceStub;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

/**
 * Test case for adding a topic
 */
public class EventTestCase {

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private TopicManagerAdminServiceStub topicManagerAdminServiceStub;
    private static final Log log = LogFactory.getLog(EventTestCase.class);

    @BeforeClass(groups = {"wso2.mb"})
    public void login() throws Exception {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.error("Error in thread sleep in EventTestCase ", e);
            Assert.fail("Error in thread sleep in EventTestCase");
        }
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();
        topicManagerAdminServiceStub = new TopicManagerAdminServiceStub("https://localhost:9443/services/TopicManagerAdminService");
        AuthenticateStub authenticateStub = new AuthenticateStub();
        authenticateStub.authenticateAdminStub(topicManagerAdminServiceStub, loggedInSessionCookie);
    }

    @Test(groups = {"wso2.mb"})
    public void createTopicTest() throws Exception {
        topicManagerAdminServiceStub.addTopic("/root/topic/myTopic");
    }

    @AfterClass(groups = {"wso2.mb"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

}
