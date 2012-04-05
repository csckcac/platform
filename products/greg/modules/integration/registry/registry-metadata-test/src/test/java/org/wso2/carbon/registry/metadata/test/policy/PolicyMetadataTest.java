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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

public class PolicyMetadataTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(PolicyMetadataTest.class);

    private String policyPath = "/_system/governance/policies/";
    private String resourceName = "sample_policy.xml";

    private ResourceAdminServiceStub resourceAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;
    private CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub;
    private InfoAdminServiceStub infoAdminServiceStub;


    @Override
    public void init() {
        log.info("Initializing Tests for Community Features in Registry Policy");
        log.debug("Community Features in Registry Policy Tests Initialised");

    }

    @Override
    public void runSuccessCase() {
        try {
            log.debug("Running SuccessCase");
            infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(sessionCookie);
            customLifecyclesChecklistAdminServiceStub = TestUtils.getCustomLifecyclesChecklistAdminServiceStub(sessionCookie);
            relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(sessionCookie);
            resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);

            addTag();
            addComment();
            addRate("2");
            addLifeCycle();
            addAssociation();
            addDependency();
            getVersion();
        } catch (Exception e) {
            Assert.fail("Unable to run policy meta data test: " + e);
            log.error("Unable to run policy meta data test: " + e.getMessage());
        }

    }


    private void addTag() {

        try {
            infoAdminServiceStub.addTag("test tag added", policyPath + resourceName, sessionCookie);
            TagBean tagBean = infoAdminServiceStub.getTags(policyPath + resourceName, sessionCookie);
            Tag[] tag = tagBean.getTags();
            for (int i = 0; i <= tag.length - 1; i++) {
                if (!tag[i].getTagName().equalsIgnoreCase("test tag added")) {
                    log.error("The given tag not found");
                    Assert.fail("Tag not found");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding tag : " + e);
        }
    }

    private void addComment() {

        try {
            //adding comment to a resource
            infoAdminServiceStub.addComment("added a test comment", policyPath + resourceName, sessionCookie);
            CommentBean commentBean = infoAdminServiceStub.getComments(policyPath + resourceName, sessionCookie);
            Comment[] comment = commentBean.getComments();

            if (!comment[0].getDescription().equalsIgnoreCase("added a test comment")) {
                log.error("comment not found");
                Assert.fail("comment not found");
            }

            //removing comment from the resource
            infoAdminServiceStub.removeComment(comment[0].getCommentPath(), sessionCookie);

            if (comment[0].getDescription().equalsIgnoreCase("added test comment")) {
                log.error("comment can not be deleted");
                Assert.fail("comment can not be deleted");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while put and get comment :" + e.getMessage());
            Assert.fail("Exception occurred while put and get comment  :" + e.getMessage());
        }
    }

    private void addRate(String rateValue) {

        try {
            infoAdminServiceStub.rateResource(rateValue, policyPath + resourceName, sessionCookie);
            RatingBean ratingBean = infoAdminServiceStub.getRatings(policyPath + resourceName, sessionCookie);
            if (ratingBean.getUserRating() != Integer.parseInt(rateValue)) {
                log.error("Rating value not found");
                Assert.fail("Rating value not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while adding rate : " + e);
        }
    }

    private void addLifeCycle() throws Exception{

        String[] lifeCycleItem = {"Design Stage", "Implementation Stage", "Testing"};
        customLifecyclesChecklistAdminServiceStub.addAspect(policyPath + resourceName, "ServiceLifeCycle");
        customLifecyclesChecklistAdminServiceStub.invokeAspect(policyPath + resourceName, "ServiceLifeCycle", "promote", lifeCycleItem);
        LifecycleBean lifecycleBean = customLifecyclesChecklistAdminServiceStub.getLifecycleBean(policyPath + resourceName);
        LifecycleActions[] lifeCycleActions = lifecycleBean.getAvailableActions();

        for (int i = 0; i <= lifeCycleActions.length - 1; i++) {
            String[] actionList = lifeCycleActions[i].getActions();
            try {
                for (int j = 0; j <= actionList.length - 1; j++) {
                    if (!actionList[j].equalsIgnoreCase("demote")) {
                        log.error("Life-cycle not promoted");
                        Assert.fail("Life-cycle not promoted");
                    }
                }
            } catch (NullPointerException e) {
                Assert.fail("Life-cycle not promoted");
            } finally {
                customLifecyclesChecklistAdminServiceStub.removeAspect(policyPath + resourceName, "ServiceLifeCycle");
            }

        }
    }

    private void addAssociation() {

        AssociationTreeBean associationTreeBean = null;
        try {
            relationAdminServiceStub.addAssociation(policyPath + resourceName, "asso", "/_system/governance/policies/", "add");

            //check for added association
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/policies/", "asso");
            if (!(associationTreeBean.getAssoType().equals("asso"))) {
                log.error("Required Association Information Not Found");
                Assert.fail("Required Association Information Not Found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding an association : " + e);
        }

    }

    private void addDependency() {

        AssociationTreeBean associationTreeBean = null;

        try {
            relationAdminServiceStub.addAssociation(policyPath + resourceName, "depends", "/_system/governance/policies/", "add");

            //check for added dependencies
            associationTreeBean = relationAdminServiceStub.getAssociationTree("/_system/governance/policies/", "depends");
            if (!(associationTreeBean.getAssoType().equals("depends"))) {
                log.error("Required Dependency Information Not Found");
                Assert.fail("Required Dependency Information Not Found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown while adding a dependency : " + e);
        }
    }


    private void getVersion() {

        VersionPath[] versionPath = null;
        long versionNoBefore = 0L;
        long versionNoAfter = 0L;

        try {
            resourceAdminServiceStub.createVersion(policyPath + resourceName);
            versionPath = resourceAdminServiceStub.getVersionsBean(policyPath + resourceName).getVersionPaths();
            versionNoBefore = versionPath[0].getVersionNumber();

            /**
             * update resource content and checking the version number update
             */

            updatePolicyFromFile();

            versionPath = resourceAdminServiceStub.getVersionsBean(policyPath + resourceName).getVersionPaths();
            versionNoAfter = versionPath[0].getVersionNumber();

            if (versionNoBefore != versionNoAfter - 1) {
                Assert.fail("New Version has not been created: ");
                log.error("New Version has not been created: ");

            }

        } catch (Exception e) {
            throw new RuntimeException("Exception thrown when getting resource version : " + e);
        }

    }

    private void updatePolicyFromFile() {

        String resourceName = "sample_policy.xml";
        String resContent = "<?xml version=\"1.0\"?>\n" +
                "\n" +
                "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                "  <wsp:ExactlyOne>\n" +
                "    <wsp:All>\n" +
                "      <wsrmp10:RMAssertion\n" +
                "       xmlns:wsrmp10=\"http://schemas.xmlsoap.org/ws/2005/02/rm/policy\">\n" +
                "        <!--wsrmp10:InactivityTimeout Milliseconds=\"600000\"/-->\n" +
                "        <wsrmp10:InactivityTimeout/>\n" +
                "        <wsrmp10:BaseRetransmissionInterval Milliseconds=\"3000\"/>\n" +
                "        <wsrmp10:ExponentialBackoff/>\n" +
                "        <wsrmp10:AcknowledgementInterval Milliseconds=\"200\"/>\n" +
                "      </wsrmp10:RMAssertion>\n" +
                "    </wsp:All>\n" +
                "    <wsp:All>\n" +
                "      <wsrmp:RMAssertion\n" +
                "           xmlns:wsrmp=\"http://docs.oasis-open.org/ws-rx/wsrmp/200702\">\n" +
                "        <wsrmp:SequenceSTR/>\n" +
                "        <wsrmp:DeliveryAssurance>\n" +
                "          <wsp:Policy>\n" +
                "            <wsrmp:ExactlyOnce/>\n" +
                "          </wsp:Policy>\n" +
                "        </wsrmp:DeliveryAssurance>\n" +
                "      </wsrmp:RMAssertion>\n" +
                "    </wsp:All>\n" +
                "  </wsp:ExactlyOne>\n" +
                "</wsp:Policy>"; //to update

        try {

            /**
             *  update policy and check the content
             */
            resourceAdminServiceStub.updateTextContent(policyPath + resourceName, resContent);

            if (resourceAdminServiceStub.getTextContent(policyPath + resourceName).contains("InactivityTimeout")) {
                log.info("Policy file successfully updated");
            } else {
                log.error("Policy File has not been updated in the registry");
                Assert.fail("Policy File has not been updated in the registry");
            }

        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }

    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }
}
