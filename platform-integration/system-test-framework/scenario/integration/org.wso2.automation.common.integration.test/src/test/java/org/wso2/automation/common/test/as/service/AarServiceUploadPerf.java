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

package org.wso2.automation.common.test.as.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceBpelInstanceManager;
import org.wso2.carbon.admin.service.AdminServiceBpelPackageManager;
import org.wso2.carbon.admin.service.AdminServiceBpelProcessManager;
import org.wso2.carbon.admin.service.AdminServiceBpelUploader;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class AarServiceUploadPerf {

    // filePath = artifactLocation + File.separator + "war" + File.separator + artifact.getArtifactName();
    //  deployerUtil.warFileUploder(sessionCookie, backendURL, filePath);

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(AarServiceUploadPerf.class);
    String backEndUrl = null;
    String serviceUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {

        for (int tennent = 0; tennent <=90; tennent++) {

            EnvironmentBuilder builder = new EnvironmentBuilder().as(tennent);
            ManageEnvironment environment = builder.build();
            backEndUrl = environment.getAs().getBackEndUrl();
            serviceUrl =environment.getAs().getServiceUrl();
            sessionCookie = environment.getAs().getSessionCookie();
            ArtifactDeployerUtil deployerUtil = new ArtifactDeployerUtil();
            deployerUtil.warFileUploder(sessionCookie, backEndUrl, "/home/dharshana/Downloads/customerwebappWithDS.war");
        }
    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException {
        // bpelUploader.deployBPEL("TestCombineUrl", sessionCookie);


    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testCombineUrl() throws Exception, RemoteException {
    }

    @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
    }




}



