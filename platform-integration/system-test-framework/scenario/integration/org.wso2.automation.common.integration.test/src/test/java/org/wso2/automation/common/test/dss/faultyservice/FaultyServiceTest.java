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
package org.wso2.automation.common.test.dss.faultyservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FaultyServiceTest extends DataServiceTest {

    private static final Log log = LogFactory.getLog(FaultyServiceTest.class);

    private final String serviceFile = "FaultyDataService.dbs";

    @Override
    protected void setServiceName() {
        serviceName = "FaultyDataService";
    }

    @Test(priority = 0)
    @Override
    public void serviceDeployment() {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact = null;
        try {
            dhArtifact = new DataHandler(new URL("file://" + serviceFileLocation + File.separator + serviceFile));
        } catch (MalformedURLException e) {
            log.error("Resource file Not Found " + e.getMessage());
            Assert.fail("Resource file Not Found " + e.getMessage());
        }
        Assert.assertNotNull(dhArtifact, "Service File Not Found");
        adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact);
        log.info(serviceName + " uploaded");
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void isServiceFaulty() {
        adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName, frameworkSettings.getEnvironmentVariables().getDeploymentDelay());
        log.info(serviceName + " is faulty");
    }

    @Test(priority = 2, dependsOnMethods = {"isServiceFaulty"})
    public void deleteFaultyService() {
        FaultyService faultyService;
        faultyService = adminServiceClientDSS.getFaultyServiceData(sessionCookie, serviceName);
        adminServiceClientDSS.deleteFaultyService(sessionCookie, faultyService.getArtifact());
        log.info(serviceName + " deleted");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("InterruptedException " + e.getMessage());
            Assert.fail("InterruptedException " + e.getMessage());
        }
        Assert.assertFalse(adminServiceClientDSS.isServiceFaulty(sessionCookie, serviceName), "Service Still in service list");
    }


}
