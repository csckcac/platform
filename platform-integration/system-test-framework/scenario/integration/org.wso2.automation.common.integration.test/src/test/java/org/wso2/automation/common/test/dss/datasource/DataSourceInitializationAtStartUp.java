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
package org.wso2.automation.common.test.dss.datasource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.service.CarbonDataSourceTest;
import org.wso2.carbon.admin.service.AdminServiceCarbonServerAdmin;
import org.wso2.platform.test.core.utils.ClientConnectionUtil;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;

import java.rmi.RemoteException;

//https://wso2.org/jira/browse/STRATOS-1631
public class DataSourceInitializationAtStartUp extends CarbonDataSourceTest {

    private static final Log log = LogFactory.getLog(DataSourceInitializationAtStartUp.class);

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");


    @Test(priority = 7, dependsOnMethods = {"deleteOperation"})
    public void ServerRestarting()
            throws org.wso2.carbon.server.admin.stub.Exception, RemoteException,
                   InterruptedException {
        log.info("Restarting Server.....");
        restartServer();
        logIn();
    }

    @Test(priority = 8, dependsOnMethods = {"ServerRestarting"})
    public void isServiceExistAfterRestarting()
            throws org.wso2.carbon.server.admin.stub.Exception, RemoteException,
                   InterruptedException {
        isServiceDeployed(serviceName);
    }

    @Test(priority = 9, dependsOnMethods = {"isServiceExistAfterRestarting"})
    public void invokeOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            getCustomerInBoston();
        }
        log.info("Service Invocation Success");
    }


    @Override
    @Test(priority = 10, dependsOnMethods = {"invokeOperation"})
    public void deleteService() {
        deleteService(serviceName);
    }

    private void restartServer() throws RemoteException,
                                        org.wso2.carbon.server.admin.stub.Exception,
                                        InterruptedException {
        AdminServiceCarbonServerAdmin adminServiceCarbonServerAdmin = new AdminServiceCarbonServerAdmin(dssBackEndUrl);
        adminServiceCarbonServerAdmin.restartGracefully(sessionCookie);
        Thread.sleep(30000);
        ClientConnectionUtil.waitForPort(Integer.parseInt(dssServer.getProductVariables().getHttpsPort()), dssServer.getProductVariables().getHostName());

    }

    private void getCustomerInBoston() throws AxisFault {
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "customersInBoston");
        Assert.assertTrue(result.toString().contains("<city>Boston</city>"), "Expected Result Mismatched");

    }

}
