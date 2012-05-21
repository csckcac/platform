/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.cloud.regression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceGadgetDashbordService;
import org.wso2.carbon.admin.service.AdminServiceGadgetRepositoryService;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.GadgetRepoServiceStub;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Comment;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget;
import org.wso2.carbon.dashboard.stub.DashboardServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.automation.cloud.regression.stratosutils.ServiceLoginClient;
import org.wso2.automation.cloud.regression.stratosutils.gadgetutils.GadgetTestUtils;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class StratosGSServiceTest {

    private static final Log log = LogFactory.getLog(StratosGSServiceTest.class);
    private UserInfo userInfo;
    private String gsHostName;
    private EnvironmentVariables gsServer;

    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().gs(4);
        gsServer = builder.build().getGs();
        userInfo = UserListCsvReader.getUserInfo(4);
        gsHostName = gsServer.getProductVariables().getHostName();
    }

    @Test()
    public void gadgetTest() throws MalformedURLException, RegistryException, RemoteException,
                                    ResourceAdminServiceExceptionException {
        String gsServerHostName = gsHostName;
        String sessionCookie = ServiceLoginClient.loginChecker(gsServerHostName);
        gadgetXMLTestClient();

        GadgetRepoServiceStub gadgetRepoServiceStub;
        gadgetRepoServiceStub = GadgetTestUtils.getGadgetRepoServiceStub(sessionCookie);
        DashboardServiceStub dashboardServiceStub =
                GadgetTestUtils.getDashboardServiceStub(sessionCookie);

        String gadgetFile = ProductConstant.getResourceLocations(ProductConstant.GS_SERVER_NAME) +
                            File.separator + "gadget" + File.separator + "HelloWorldGadget.xml";
        FileDataSource gadgetFileDataSource = new FileDataSource(gadgetFile);
        log.info(gadgetFile);
        DataHandler dataHandler = new DataHandler(gadgetFileDataSource);
        AdminServiceGadgetDashbordService dashbordService = new AdminServiceGadgetDashbordService();
        AdminServiceGadgetRepositoryService repositoryService = new AdminServiceGadgetRepositoryService();

        //Define parameters
        String gadgetName = "TestGadget";
        String gadgetDescription = "TestGadgetAddedFromIntegrationTest";
        String gadgetPath;
        String gadgetUrl;
        String userID;
        String[] userIDs = userInfo.getUserName().split("@");
        userID = userIDs[0];

        String dashboardName = null;
        String gadgetGroup = "G1#";

        Comment comment = new Comment();
        comment.setCommentText("TestCommentOnGadget");
        comment.setAuthorUserName(userID);

        int rating = 3;
        Gadget addedGadget;

        // Add gadget to gadget repository test
        repositoryService.addGadgetToRepo(gadgetRepoServiceStub, dataHandler, gadgetName, gadgetDescription);
        // Get gadget data set test
        addedGadget = repositoryService.getGadgetFromGadgetList(gadgetRepoServiceStub, gadgetName);
        // assign gadget path and url
        if (addedGadget == null) {
            log.error("Added gadget can not be retrieved, getGadgetFromGadgetList test Failed");
            Assert.fail("Added gadget can not be retrieved, getGadgetFromGadgetList test Failed");
        } else {
            gadgetPath = addedGadget.getGadgetPath();
            gadgetUrl = addedGadget.getGadgetUrl();
            log.info("Get gadget using gadget path test");
            repositoryService.getGadgetFromPath(gadgetRepoServiceStub, gadgetPath);
            log.info("Add gadget to user's portal test");

            String newtabTitle = "newTab";
            int addtabID = dashbordService.addNewTab(dashboardServiceStub, userID, newtabTitle, dashboardName);
            if (addtabID == 0) {
                log.error("Failed to add new tab");
                Assert.fail("Failed to add new tab");
            } else {

                String tabIdValue = "" + addtabID;
                // Populate Default Three Column Layout(This need before delete)
                dashbordService.populateDefaultThreeColumnLayout(dashboardServiceStub, userID, tabIdValue);
                // Get tab title test
                String addedTab = dashbordService.getTabTitle(dashboardServiceStub, userID, tabIdValue, dashboardName, newtabTitle);
                // Add gadget to user test
                repositoryService.addGadgetToPortal(gadgetRepoServiceStub, userID, addedTab, gadgetUrl, dashboardName, gadgetGroup,
                                                    gadgetPath);
                log.info("User has added gadget test");
                repositoryService.userHasGadget(gadgetRepoServiceStub, gadgetPath);
                log.info("Add comment on gadget test");
                repositoryService.addCommentForGadget(gadgetRepoServiceStub, gadgetPath, comment);
                log.info("Get comment count on a gadget test");
                repositoryService.getCommentCountForGadget(gadgetRepoServiceStub, gadgetPath);
                log.info(" Get comment set on a gadget test ");

                dashbordService.addGadgetToUser(dashboardServiceStub, userID, tabIdValue, gadgetUrl, dashboardName, gadgetGroup);
                // Remove added tab test
                dashbordService.removeTab(dashboardServiceStub, userID, tabIdValue, dashboardName);
            }
            log.info("Add rating on gadget test");
            repositoryService.addRatingForGadget(gadgetRepoServiceStub, gadgetPath, rating, String.valueOf(addtabID), gadgetGroup);
            log.info("Get rating on gadget test");
            // getRatingOnGadget(gadgetRepoServiceStub, gadgetPath, userID);
            log.info(" Delete the added gadget from repository test ");
            repositoryService.deleteGadgetFromRepo(gadgetRepoServiceStub, gadgetPath);
            log.info("Remove the added Tab");
            dashbordService.removeTab(dashboardServiceStub, userID, newtabTitle, dashboardName);
        }

    }

    public void gadgetXMLTestClient()
            throws MalformedURLException, RegistryException, RemoteException,
                   ResourceAdminServiceExceptionException {
        String path = "/_system/config/repository/gadget-server/gadgets/AmazonSearchGadget/amazon-search.xml";
        AdminServiceResourceAdmin resourceAdmin = new AdminServiceResourceAdmin(gsServer.getBackEndUrl());
        ResourceData[] resourceData =  resourceAdmin.getResource(gsServer.getSessionCookie(), path);
        assertTrue(resourceData[0].getName().equals("amazon-search.xml"));
    }

}