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
package org.wso2.carbon.registry.resource.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

/**
 *
 */
public class CommunityFeatureTest extends TestTemplate {
    /**
     * @goal testing comment feature in registry
     */

    private static final Log log = LogFactory.getLog(DependencyTest.class);

    @Override
    public void init() {

    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");

        try {

            ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);
            InfoAdminServiceStub infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(sessionCookie);
            CollectionContentBean collectionContentBean = new CollectionContentBean();
            collectionContentBean = resourceAdminServiceStub.getCollectionContent("/");
            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i <= childPath.length - 1; i++) {
                    if (childPath[i].equalsIgnoreCase("/TestAutomation")) {
                        resourceAdminServiceStub.delete("/TestAutomation");
                    }
                }
            }
            String collectionPath = resourceAdminServiceStub.addCollection("/", "TestAutomation", "", "");
            log.info("collection added to " + collectionPath);
            collectionPath = resourceAdminServiceStub.addCollection("/TestAutomation", "InfoAdminTest", "", "");
            log.info("collection added to " + collectionPath);
            CommentTest(infoAdminServiceStub);

        }
        catch (Exception e) {
        }
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }

    public void CommentTest(InfoAdminServiceStub infoAdminServiceStub) {
        try {
            infoAdminServiceStub.addComment("this is sample comment", "/TestAutomation/InfoAdminTest", sessionCookie);
            infoAdminServiceStub.addComment("this is sample comment2", "/TestAutomation/InfoAdminTest", sessionCookie);
            CommentBean commentBean = infoAdminServiceStub.getComments("/TestAutomation/InfoAdminTest", sessionCookie);
            Comment[] comment = commentBean.getComments();
            if (!comment[0].getDescription().equalsIgnoreCase("this is sample comment")) {
                log.error("Added comment not found");
                Assert.fail("Added comment not found");
            }
            if (!comment[1].getDescription().equalsIgnoreCase("this is sample comment2")) {
                log.error("Added comment not found");
                Assert.fail("Added comment not found");
            }
            infoAdminServiceStub.removeComment(comment[0].getCommentPath(), sessionCookie);
            commentBean = infoAdminServiceStub.getComments("/TestAutomation/InfoAdminTest", sessionCookie);
            comment = commentBean.getComments();
            if (comment[0].getDescription().equalsIgnoreCase("this is sample comment")) {
                log.error("Comment not deleted");
                Assert.fail("Comment not deleted");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occured while adding/getting comment :" + e.getMessage());
            Assert.fail("Exception occured while adding/getting comment :" + e.getMessage());
        }
    }
}
