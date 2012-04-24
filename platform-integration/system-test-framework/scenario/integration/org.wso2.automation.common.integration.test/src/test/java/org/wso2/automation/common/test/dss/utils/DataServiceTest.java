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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.EnvironmentSettings;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class DataServiceTest {
    private static final Log log = LogFactory.getLog(DataServiceTest.class);

    protected String dssBackEndUrl;
    protected UserInfo userInfo;

    protected AdminServiceClientDSS adminServiceClientDSS;
    protected String serviceName;
    protected String serviceEndPoint;

    protected String sessionCookie;
    protected EnvironmentVariables dssServer;
    protected FrameworkSettings frameworkSettings;
    protected EnvironmentSettings environment;

    protected String resourceFileLocation;
    protected String serviceFileLocation;

    protected abstract void setServiceName();

    @BeforeClass
    public void init() {
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(3);
        dssServer = builder.build().getDss();

        frameworkSettings = builder.getFrameworkSettings();
        environment = frameworkSettings.getEnvironmentSettings();

        sessionCookie = dssServer.getSessionCookie();
        dssBackEndUrl = dssServer.getBackEndUrl();
        userInfo = UserListCsvReader.getUserInfo(3);
        adminServiceClientDSS = new AdminServiceClientDSS(dssBackEndUrl);
        resourceFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                               + "artifacts" + File.separator + "DSS";
        serviceFileLocation = resourceFileLocation + File.separator + "dbs" + File.separator
                              + "rdbms" + File.separator + "MySql";
        setServiceName();
    }

    @Test(priority = 0)
    public void serviceDeployment() throws Exception{
        isServiceDeployed(serviceName);
        setServiceEndPointHttp(serviceName);
    }

    @AfterClass
    public void destroy() {
        dssBackEndUrl = null;
        dssServer = null;
        frameworkSettings = null;
        sessionCookie = null;
        adminServiceClientDSS = null;
        userInfo = null;
    }

    protected void setServiceEndpointHttps(String serviceName)
            throws ServiceAdminException, RemoteException {
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttps(sessionCookie, dssBackEndUrl, serviceName);
        Assert.assertTrue(serviceEndPoint.contains(serviceName), "Service Name not found in service endpoint reference");
    }

    protected void setServiceEndPointHttp(String serviceName)
            throws ServiceAdminException, RemoteException {
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        Assert.assertTrue(serviceEndPoint.contains(serviceName), "Service Name not found in service endpoint reference");
    }

    protected void deleteService(String serviceName) throws ServiceAdminException, RemoteException {

        String serviceGroup = adminServiceClientDSS.getServiceData(sessionCookie, serviceName).getServiceGroupName();
        adminServiceClientDSS.deleteService(sessionCookie, new String[]{serviceGroup});
        Assert.assertFalse(adminServiceClientDSS.isServiceExist(sessionCookie, serviceName),
                           "Service Still in service list. service deletion failed");

    }

    protected void isServiceDeployed(String serviceName) throws RemoteException {
        log.info("waiting " + frameworkSettings.getEnvironmentVariables().getDeploymentDelay()
                 + " millis for service deployment");

        adminServiceClientDSS.isServiceDeployed(sessionCookie, serviceName,
                                                frameworkSettings.getEnvironmentVariables().getDeploymentDelay());

        //todo this sleep should be removed after fixing CARBON-11900 gira
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Assert.fail("Thread InterruptedException");
        }
    }

    protected DataHandler createArtifact(String serviceFileName, ArrayList<File> sqlScript)
            throws RSSAdminRSSDAOExceptionException, IOException, ClassNotFoundException,
                   SQLException, XMLStreamException {
        SqlDataSourceUtil dssUtil = new SqlDataSourceUtil(sessionCookie, dssBackEndUrl,
                                        FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME),
                                        Integer.parseInt(userInfo.getUserId()));
        dssUtil.createDataSource(sqlScript);
        return dssUtil.createArtifact(serviceFileLocation + File.separator + serviceFileName);
    }

    protected void deleteServiceIfExist(String serviceName)
            throws ServiceAdminException, RemoteException {
        DataServiceUtility.deleteServiceIfExist(sessionCookie, dssBackEndUrl, serviceName);
    }

    protected void logIn() {
        AdminServiceAuthentication  adminServiceAuthentication = new AdminServiceAuthentication(dssBackEndUrl);
        sessionCookie = adminServiceAuthentication.login(userInfo.getUserName(), userInfo.getPassword(), dssServer.getProductVariables().getHostName());
    }
}
