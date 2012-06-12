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
package org.wso2.automation.common.test.greg.lifecycle;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.lifecycle.utils.Utils;
import org.wso2.carbon.admin.service.LifeCycleManagerAdminService;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.fileutils.FileManager;

import java.io.File;
import java.rmi.RemoteException;

public class LifeCycleErrorHandling {
    private String sessionCookie;

    private LifeCycleManagerAdminService lifeCycleManagerAdminService;

    private final String ASPECT_NAME = "LifeCycleSyntaxTest";
    private String lifeCycleConfiguration;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleManagerAdminService = new LifeCycleManagerAdminService(gregServer.getBackEndUrl());
        String filePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                          + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        lifeCycleConfiguration = FileManager.readFile(filePath);
        lifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("IntergalacticServiceLC", ASPECT_NAME);

        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);

    }

    @Test(priority = 1)
    public void createLifeCycleWithInvalidXmlSyntax()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("id=\"Commencement\">", "id=\"Commencement\"");
        try {
            Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, invalidLifeCycleConfiguration),
                               "Life Cycle Added with invalid Syntax");

        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("Unable to initiate aspect. Unexpected")
                    , "Unable to initiate aspect. Unexpected not contain in message. " + e.getMessage());
        }

        Thread.sleep(2000);
    }

    @Test(priority = 2)
    public void createLifeCycleWithInvalidTagName()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("<configuration", "<config");
        invalidLifeCycleConfiguration = invalidLifeCycleConfiguration.replaceFirst("</configuration>", "</config>");

        Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, invalidLifeCycleConfiguration),
                           "Life Cycle Added with invalid tag");

        Thread.sleep(2000);
    }

    @Test(priority = 3)
    public void addLifeCycleHavingExistingName()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, lifeCycleConfiguration)
                , "Adding New LifeCycle Failed first time");
        Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, lifeCycleConfiguration)
                , "Adding New LifeCycle again having same name success");
    }

    @AfterClass
    public void cleanUp() {
        lifeCycleManagerAdminService = null;
        lifeCycleConfiguration = null;
    }

}
