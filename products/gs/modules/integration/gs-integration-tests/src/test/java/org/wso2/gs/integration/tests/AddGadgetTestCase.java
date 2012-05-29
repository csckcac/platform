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

package org.wso2.gs.integration.tests;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jasper.tagplugins.jstl.core.Catch;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.GadgetRepoServiceStub;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget;
import org.wso2.carbon.dashboard.mgt.users.stub.GadgetServerUserManagementServiceStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Comment;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;
import static org.wso2.gs.integration.tests.GadgetRepoTestUtils.getGadgetResourcePath;

/*
 Tests for AddGadgetTestCase in GS - Gadget Repository
 */
public class AddGadgetTestCase {

    private static final Log log = LogFactory.getLog(AddGadgetTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    GadgetRepoServiceStub gadgetRepoServiceStub;
    String gadgetFile;
    FileDataSource gadgetFileDataSource;
    DataHandler dataHandler;
    String gadgetName;
    String gadgetDescription;
    String gadgetPath;
    String gadgetUrl;
    String userID = "admin";
    String tabId = "0";
    String dashboardName = null;
    String gadgetGroup = "G2#";
    Comment comment;
    int commentStart;
    int commentSetSize;
    int rating;

    Gadget addedGadget;


    @BeforeMethod(groups = {"wso2.gs"})
    public void init() throws java.lang.Exception {
        String loggedInSessionCookie = util.login();
        gadgetRepoServiceStub = GadgetRepoTestUtils.getGadgetRepoServiceStub(loggedInSessionCookie);
        gadgetFile = getGadgetResourcePath(FrameworkSettings.getFrameworkPath());
        gadgetFileDataSource = new FileDataSource(gadgetFile);
        dataHandler = new DataHandler(gadgetFileDataSource);
        gadgetName = "TestGadget";
        gadgetDescription = "TestGadgetAddedFromIntegrationTest";
        comment = new Comment();
        comment.setCommentText("TestCommentOnGadget");
        comment.setAuthorUserName(userID);
        commentStart = 0;
        commentSetSize = 1;
        rating = 3;
    }

    //  Add a Gadget to gadget repository
    @Test(groups = {"wso2.gs"}, description = "Add a Gadget to gadget repo")
    public void testAddGadgetToRepo() throws Exception {
        boolean addGadgetStatus = gadgetRepoServiceStub.addGadgetEntryToRepo(
                gadgetName, null, gadgetDescription, null, null, dataHandler);
        assertTrue(addGadgetStatus, "Failed to add gadget to repository");

    }

    // Check the existence of added gadget and Get the path of it
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddGadgetToRepo"}, description = "Check the existence of added gadget and Get the path of it")
    public void testGetGadgetFromGadgetList() throws Exception {
        Gadget[] gadgets = gadgetRepoServiceStub.getGadgetData();
        Gadget addedGadget = null;
        if (gadgets != null) {
            for (Gadget gadget : gadgets) {
                if (gadget != null) {
                    if (gadgetName.equals(gadget.getGadgetName())) {
                        log.debug("Added Gadget path is :" + gadget.getGadgetPath());
                        log.debug("Added Gadget Url is :" + gadget.getGadgetUrl());
                        log.info("Successfully executed getGadgetFromGadgetList test");
                        addedGadget = gadget;
                        break;
                    }
                }
            }
        }

        assertTrue((addedGadget != null), "Failed to get gadget Data");
        gadgetPath = addedGadget.getGadgetPath();
        gadgetUrl = addedGadget.getGadgetUrl();


    }

    // Check the existence of the added gadget using the gadget path
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetGadgetFromGadgetList"}, description = "Check the existence of the added gadget using the gadget path")
    public void testGetGadgetFromPath() throws RemoteException {
        Gadget gadget = gadgetRepoServiceStub.getGadget(gadgetPath);
        assertTrue((gadget != null), "Failed to get gadget using gadget path");

    }

    // Add gadget to user's portal
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetGadgetFromGadgetList"}, description = "Add gadget to user's portal")
    public void testAddGadgetToPortal() throws RemoteException {
        boolean addGadgetToPortalStatus = gadgetRepoServiceStub.addGadget(
                userID, tabId, gadgetUrl, dashboardName, gadgetGroup, gadgetPath);
        assertTrue(addGadgetToPortalStatus, "Failed to add a gadget to the user's portal");

    }

    // Check if the user has the added gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddGadgetToPortal"}, description = "Check if the user has the added gadget")
    public void testUserHasGadget() throws RemoteException {
        boolean userHasGadgetStatus = gadgetRepoServiceStub.userHasGadget(gadgetPath);
        assertTrue(userHasGadgetStatus, "Failed to executed userHasGadget test");

    }

    // Add a comment to the gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testUserHasGadget"}, description = "Add a comment to the gadget")
    public void testAddCommentForGadget() throws RemoteException {
        boolean addCommentStatus = gadgetRepoServiceStub.addCommentForGadget(gadgetPath, comment);
        assertTrue(addCommentStatus, "Failed to add a comment for the gadget");

    }

    // Get the comment count of a gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddCommentForGadget"}, description = " Get the comment count of a gadget")
    public void testGetCommentCountForGadget() throws RemoteException {
        int commentCount = gadgetRepoServiceStub.getCommentsCount(gadgetPath);
        assertTrue((commentCount == 1), "Failed to get comment count for the gadget");

    }

    // Get comment set on a gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetCommentCountForGadget"}, description = "Get comment set on a gadget")
    public void testGetCommentSetForGadget() throws RemoteException {
        Comment commentSet[] = gadgetRepoServiceStub.getCommentSet(gadgetPath, commentStart, commentSetSize);
        assertTrue((commentSet.length == 1), "Failed to executed getCommentSetForGadget test");

    }

    // Delete comment on a gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetCommentSetForGadget"}, description = "Delete comment on a gadget")
    public void testDeleteCommentOnGadget() throws RemoteException {
        String commentPath = gadgetPath + ";comments:1";
        boolean deleteCommentStatus = gadgetRepoServiceStub.deleteComment(commentPath);
        assertTrue(deleteCommentStatus, "Failed to delete comment on gadget");

    }

    // Add Rating on a Gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testUserHasGadget"}, description = "Add Rating on a Gadget")
    public void testAddRatingForGadget() throws RemoteException {
        boolean addRatingStatus = gadgetRepoServiceStub.addRatingForGadget(gadgetPath, rating, tabId, gadgetGroup);
        assertTrue(addRatingStatus, "Failed to add rating for the gadget");

    }

    // Get user Rating on a Gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddRatingForGadget"}, description = "Get user Rating on a Gadget")
    public void testGetRatingOnGadget() throws RemoteException {
        String userRating = gadgetRepoServiceStub.getUserRating(gadgetPath, userID);
        assertTrue(("3".equals(userRating)), "Failed to executed getRatingOnGadget test");

    }

    // Delete the added gadget from repository
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetRatingOnGadget"}, description = "Delete the added gadget from repository")
    public void testDeleteGadgetFromRepo() throws RemoteException {
        boolean deleteStatus = gadgetRepoServiceStub.deleteGadget(gadgetPath);
        assertTrue(deleteStatus, "Failed to Delete the gadget from repository");

    }

    // Check whether the user is an Admin
    @Test(groups = {"wso2.gs"}, description = "Check whether the user is an Admin")
    public void testIsAdmin() throws RemoteException {
        boolean adminStatus = gadgetRepoServiceStub.isAdmin(userID);
        assertTrue(adminStatus, "The user is not an Admin");

    }

    //Change the gadget status as a default gadget
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddGadgetToRepo"}, description = "Change the gadget status as a default gadget")
    public void testMakeGadgetDefault() throws RemoteException {
        boolean gadgetStatus = gadgetRepoServiceStub.makeGadgetDefault(gadgetPath,true);
        assertTrue(gadgetStatus, "Could not change the gadget status as a default gadget");

    }

}
