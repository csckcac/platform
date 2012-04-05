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
package org.wso2.automation.common.test.dss.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.admin.service.AdminServiceService;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;

public class DataServiceUtility {
    private static final Log log = LogFactory.getLog(DataServiceUtility.class);

    public static void deleteServiceIfExist(String sessionCookie, String dssBackEndUrl, String serviceName) {
        AdminServiceService adminServiceService;
        adminServiceService = new AdminServiceService(dssBackEndUrl);

        if (adminServiceService.isServiceExists(sessionCookie, serviceName)) {
            log.info("Service already in server");
            adminServiceService.deleteService(sessionCookie, new String[]{adminServiceService.getServicesData(sessionCookie, serviceName).getServiceGroupName()});
            log.info("Service Deleted");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                Assert.fail("InterruptedException :" + e.getMessage());
            }
        } else if (adminServiceService.isServiceFaulty(sessionCookie, serviceName)) {
            log.info("Service already in faulty service list");
            adminServiceService.deleteFaultyService(sessionCookie, adminServiceService.getFaultyData(sessionCookie, serviceName).getArtifact());
            log.info("Service Deleted from faulty service list");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                Assert.fail("InterruptedException :" + e.getMessage());
            }
        }

        Assert.assertFalse(adminServiceService.isServiceExists(sessionCookie, serviceName), "Service Still in service list. service deletion failed");
        Assert.assertFalse(adminServiceService.isServiceFaulty(sessionCookie, serviceName), "Service Still in faulty service list. service deletion failed");
    }

    public static String getServiceEndpointHttp(String sessionCookie, String backEndUrl, String serviceName) {
        String serviceEndPoint = null;
        AdminServiceService adminServiceService;
        ServiceMetaData serviceMetaData;
        String[] endpoints;

        adminServiceService = new AdminServiceService(backEndUrl);
        serviceMetaData = adminServiceService.getServicesData(sessionCookie, serviceName);
        Assert.assertEquals(serviceName, serviceMetaData.getName(), "Service Name Mismatched");
        log.info("Service Deployed");

        endpoints = serviceMetaData.getEprs();
        Assert.assertNotNull(endpoints, "Service Endpoint object null");
        Assert.assertTrue((endpoints.length > 0), "No service endpoint found");
        for (String epr : endpoints) {
            if (epr.startsWith("http://")) {
                serviceEndPoint = epr;
                break;
            }
        }
        log.info("Service End point :" + serviceEndPoint);
        Assert.assertNotNull(serviceEndPoint, "service endpoint null");
        Assert.assertTrue(serviceEndPoint.contains(serviceName), "Service endpoint not contain service name");
        return serviceEndPoint;
    }

    public static String getServiceEndpointHttps(String sessionCookie, String backEndUrl, String serviceName) {
        String serviceEndPoint = null;
        AdminServiceService adminServiceService;
        ServiceMetaData serviceMetaData;
        String[] endpoints;

        adminServiceService = new AdminServiceService(backEndUrl);
        serviceMetaData = adminServiceService.getServicesData(sessionCookie, serviceName);
        Assert.assertEquals(serviceName, serviceMetaData.getName(), "Service Name Mismatched");
        log.info("Service Deployed");

        endpoints = serviceMetaData.getEprs();
        Assert.assertNotNull(endpoints, "Service Endpoint object null");
        Assert.assertTrue((endpoints.length > 0), "No service endpoint found");
        for (String epr : endpoints) {
            if (epr.startsWith("https://")) {
                serviceEndPoint = epr;
                break;
            }
        }
        log.info("Service End point :" + serviceEndPoint);
        Assert.assertNotNull(serviceEndPoint, "service endpoint null");
        Assert.assertTrue(serviceEndPoint.contains(serviceName), "Service endpoint not contain service name");
        return serviceEndPoint;
    }
}
