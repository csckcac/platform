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
package org.wso2.automation.common.test.dss.service;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.DataServiceAdminService;
import org.wso2.carbon.admin.service.DataSourceAdminService;
import org.wso2.carbon.admin.service.RSSAdminConsoleService;
import org.wso2.carbon.datasource.ui.stub.DataSourceManagementException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.securevault.secret.SecretInformation;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarbonDataSourceTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(CarbonDataSourceTest.class);

    private final String serviceFile = "CarbonDSDataServiceTest.dbs";

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");

    private String carbonDataSourceName;
    private SqlDataSourceUtil sqlDataSource;


    @Override
    protected void setServiceName() {
        serviceName = "CarbonDSDataServiceTest";
    }

    @Test(priority = 0)
    public void createDataSourceTest()
            throws RemoteException, DataSourceManagementException,
                   RSSAdminRSSDAOExceptionException {
        carbonDataSourceName = createDataSource();
        log.info(carbonDataSourceName + " carbon Data Source Created");
    }

    @Test(priority = 1, dependsOnMethods = {"createDataSourceTest"})
    @Override
    public void serviceDeployment() throws ServiceAdminException, RemoteException {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact = createArtifactWithDataSource(serviceFile);
        adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact);
        isServiceDeployed(serviceName);
        setServiceEndPointHttp(serviceName);
    }

    @Test(priority = 2, dependsOnMethods = {"serviceDeployment"})
    public void selectOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            getCustomerInBoston();
        }
        log.info("Select Operation Success");
    }

    @Test(priority = 3, dependsOnMethods = {"serviceDeployment"})
    public void insertOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            addEmployee(String.valueOf(i));
        }
        log.info("Insert Operation Success");
    }

    @Test(priority = 4, dependsOnMethods = {"insertOperation"})
    public void selectByNumber() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            getEmployeeById(String.valueOf(i));
        }
        log.info("Select Operation with parameter Success");
    }

    @Test(priority = 5, dependsOnMethods = {"insertOperation"})
    public void updateOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            IncreaseEmployeeSalary(String.valueOf(i));
        }
        log.info("Update Operation success");
    }

    @Test(priority = 6, dependsOnMethods = {"updateOperation"})
    public void deleteOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            deleteEmployeeById(String.valueOf(i));
            verifyDeletion(String.valueOf(i));
        }
        log.info("Delete operation success");
    }

    @Test(priority = 7, dependsOnMethods = {"deleteOperation"})
    public void deleteService() throws ServiceAdminException, RemoteException {
        deleteService(serviceName);
        log.info(serviceName + " Deleted");
    }


    private String createDataSource()
            throws RemoteException, DataSourceManagementException,
                   RSSAdminRSSDAOExceptionException {

        DataServiceAdminService dataServiceAdminService = new DataServiceAdminService(dssBackEndUrl);
        DataSourceAdminService dataSourceAdminService = new DataSourceAdminService(dssBackEndUrl);
        RSSAdminConsoleService rSSAdminConsoleService = new RSSAdminConsoleService(dssBackEndUrl);
        String[] list = dataServiceAdminService.getCarbonDataSources(sessionCookie);
        String createDataSourceResponse;
        DataSourceInformation dataSourceInfo;
        String carbonDataSourceName = null;

        sqlDataSource = new SqlDataSourceUtil(sessionCookie, dssBackEndUrl, FrameworkFactory.getFrameworkProperties("DSS"), 3);
        sqlDataSource.createDataSource(getSqlScript());
        String databaseName = sqlDataSource.getDatabaseName();
        if (environment.is_runningOnStratos()) {
            if (list != null) {
                for (String ds : list) {
                    if (ds.startsWith(databaseName + "_")) {
                        dataSourceAdminService.removeCarbonDataSources(sessionCookie, ds);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            log.error("InterruptedException : " + e.getMessage());
                            Assert.fail("InterruptedException : " + e.getMessage());
                        }
                    }
                }
            }
            createDataSourceResponse = rSSAdminConsoleService.createCarbonDSFromDatabaseUserEntry(sessionCookie, sqlDataSource.getDatabaseInstanceId(), sqlDataSource.getDatabaseUserId());
            Assert.assertTrue((createDataSourceResponse.indexOf(databaseName + "_") == 0), "Database name not found in create data source response message");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("InterruptedException : " + e.getMessage());
                Assert.fail("InterruptedException : " + e.getMessage());
            }

            dataSourceInfo = dataSourceAdminService.getCarbonDataSources(sessionCookie, createDataSourceResponse);
            dataSourceInfo.getSecretInformation().setAliasSecret(sqlDataSource.getDatabasePassword());
            dataSourceAdminService.editCarbonDataSources(sessionCookie, createDataSourceResponse, dataSourceInfo);
        } else {
            String dataSourceName = databaseName + "DataSource";
            if (list != null) {
                for (String ds : list) {
                    if (dataSourceName.equalsIgnoreCase(ds)) {
                        dataSourceAdminService.removeCarbonDataSources(sessionCookie, ds);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            log.error("InterruptedException : " + e.getMessage());
                            Assert.fail("InterruptedException : " + e.getMessage());
                        }
                    }
                }
            }

            dataSourceInfo = getDataSourceInformation(dataSourceName);

            dataSourceAdminService.addDataSourceInformation(sessionCookie, dataSourceName, dataSourceInfo);
            createDataSourceResponse = dataSourceName;
        }

        list = dataServiceAdminService.getCarbonDataSources(sessionCookie);
        Assert.assertNotNull(list, "Data Source list null");
        for (String ds : list) {
            if (ds.equals(createDataSourceResponse)) {
                carbonDataSourceName = ds;
                break;
            }
        }

        Assert.assertNotNull("DataSource Not found in DataSource List", carbonDataSourceName);
        return carbonDataSourceName;
    }

    private List<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(new File(resourceFileLocation + File.separator + "sql" +  File.separator + "MySql" + File.separator + "CreateTables.sql"));
        al.add(new File(resourceFileLocation + File.separator + "sql" +  File.separator + "MySql" + File.separator + "Customers.sql")) ;
        return al;
    }

    private DataHandler createArtifactWithDataSource(String serviceFileName) {
        Assert.assertNotNull("Carbon data source name null. create carbon data source first", carbonDataSourceName);
        try {

            OMElement dbsFile = AXIOMUtil.stringToOM(FileManager.readFile(serviceFileLocation + File.separator + serviceFileName).trim());
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));

            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("carbon_datasource_name".equals(value)) {
                    property.setText(carbonDataSourceName);

                }
            }

            log.debug(dbsFile);
            ByteArrayDataSource dbs = new ByteArrayDataSource(dbsFile.toString().getBytes());
            return new DataHandler(dbs);

        } catch (XMLStreamException e) {
            log.error("XMLStreamException when Reading Service File" + e.getMessage());
            Assert.fail("XMLStreamException when Reading Service File" + e.getMessage());
        } catch (IOException e) {
            log.error("IOException when Reading Service File" + e.getMessage());
            Assert.fail("IOException  when Reading Service File" + e.getMessage());
        }
        return null;
    }

    private DataSourceInformation getDataSourceInformation(String dataSourceName) {
        DataSourceInformation dataSourceInfo = new DataSourceInformation();
        SecretInformation secretInformation = new SecretInformation();
        dataSourceInfo.setSecretInformation(secretInformation);

        dataSourceInfo.setAlias(dataSourceName);
        dataSourceInfo.setDriver("com.mysql.jdbc.Driver");
        dataSourceInfo.setUrl(sqlDataSource.getJdbcUrl());
        dataSourceInfo.getSecretInformation().setUser(sqlDataSource.getDatabaseUser());
        dataSourceInfo.getSecretInformation().setAliasSecret(sqlDataSource.getDatabasePassword());
        dataSourceInfo.setType("BasicDataSource");
        dataSourceInfo.setDatasourceName(dataSourceName);
        dataSourceInfo.setDefaultAutoCommit(true);
        dataSourceInfo.setMaxActive(8);
        dataSourceInfo.setMaxIdle(8);
        dataSourceInfo.setMaxOpenPreparedStatements(-1);
        dataSourceInfo.setMaxWait(-1);
        dataSourceInfo.setMinIdle(0);
        dataSourceInfo.setInitialSize(0);
        dataSourceInfo.setPoolPreparedStatements(false);
        dataSourceInfo.setTestOnBorrow(true);
        dataSourceInfo.setValidationQuery("SELECT 1");
        dataSourceInfo.setTestWhileIdle(false);



//        dataSourceInfo.setNumTestsPerEvictionRun(3);
//        dataSourceInfo.setTimeBetweenEvictionRunsMillis(-1);
//        dataSourceInfo.setRemoveAbandonedTimeout(0);
//        dataSourceInfo.setDefaultTransactionIsolation(-1);
//        dataSourceInfo.setAccessToUnderlyingConnectionAllowed(false);
//        dataSourceInfo.setTestOnReturn(false);
//        dataSourceInfo.setRemoveAbandoned(false);
//        dataSourceInfo.setDefaultReadOnly(false);
//        dataSourceInfo.setLogAbandoned(true);
//        dataSourceInfo.addParameter("registry", "memory");
//        dataSourceInfo.setMinEvictableIdleTimeMillis(1800000);

        return dataSourceInfo;
    }

    private void getCustomerInBoston() throws AxisFault {
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "customersInBoston");
        Assert.assertTrue(result.toString().contains("<city>Boston</city>"), "Expected Result Mismatched");

    }

    private void addEmployee(String employeeNumber) throws AxisFault {

        OMElement payload = fac.createOMElement("addEmployee", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement lastName = fac.createOMElement("lastName", omNs);
        lastName.setText("BBB");
        payload.addChild(lastName);

        OMElement fName = fac.createOMElement("firstName", omNs);
        fName.setText("AAA");
        payload.addChild(fName);

        OMElement email = fac.createOMElement("email", omNs);
        email.setText("aaa@ccc.com");
        payload.addChild(email);

        OMElement salary = fac.createOMElement("salary", omNs);
        salary.setText("50000");
        payload.addChild(salary);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "addEmployee");

    }

    private OMElement getEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("employeesByNumber", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "employeesByNumber");
        Assert.assertTrue(result.toString().contains("<first-name>AAA</first-name>"), "Expected Result Mismatched");
        return result;
    }

    private void IncreaseEmployeeSalary(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("incrementEmployeeSalary", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement salary = fac.createOMElement("increment", omNs);
        salary.setText("10000");
        payload.addChild(salary);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "incrementEmployeeSalary");

        OMElement result = getEmployeeById(employeeNumber);
        Assert.assertTrue(result.toString().contains("<salary>60000.0</salary>"), "Expected Result Mismatched. update operation is not working fine");

    }

    private void deleteEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("deleteEmployeeById", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "deleteEmployeeById");


    }

    private void verifyDeletion(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("employeesByNumber", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "employeesByNumber");
        Assert.assertFalse(result.toString().contains("<employee>"), "Employee record found. deletion is now working fine");
    }
}
