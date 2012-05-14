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
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceProxyServiceAdmin;
import org.wso2.carbon.admin.service.AdminServiceService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.dataservices.ui.fileupload.stub.ExceptionException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class EventingServiceTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(EventingServiceTest.class);

    private final String serviceFile = "EventingTest.dbs";

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");

    private String proxyUrl = null;

    @Override
    protected void setServiceName() {
        serviceName = "EventingTest";
    }

    @Test(priority = 0)
    public void addProxy()
            throws ProxyServiceAdminProxyAdminException, RemoteException, ServiceAdminException,
                   LoginAuthenticationExceptionException {
        createProxy();
        Assert.assertNotNull(proxyUrl, "proxy url is null");
        log.info("Proxy Service Added");
    }

    @Test(priority = 1, dependsOnMethods = {"addProxy"})
    @Override
    public void serviceDeployment()
            throws ServiceAdminException, IOException, RSSAdminRSSDAOExceptionException,
                   ExceptionException, ClassNotFoundException, SQLException {
        deleteServiceIfExist(serviceName);
        DataHandler dhArtifact;
        dhArtifact = getArtifactWithSubscription(serviceFile);

        Assert.assertTrue(adminServiceClientDSS.uploadArtifact(sessionCookie, serviceFile, dhArtifact)
                , "Service Deployment Failed while uploading service file");
        isServiceDeployed(serviceName);
        setServiceEndPointHttp(serviceName);
        log.info(serviceName + " uploaded");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceDeployment"})
    public void insertProduct() throws AxisFault {
        addProduct(150);
        OMElement response = getProduct(150);
        Assert.assertNotNull(response, "Response null");
        log.info("Product added");

    }

    @Test(priority = 3, dependsOnMethods = {"insertProduct"})
    public void updateProduct() throws AxisFault {
        editProduct(150, 200);
        OMElement response = getProduct(150);
        Assert.assertNotNull(response, "Response null");
        Assert.assertEquals(((OMElement) ((OMElement) response.getChildrenWithLocalName("Product").next()).getChildrenWithLocalName("quantityInStock").next()).getText(), "200", "Product edited failed");
        log.info("Product details edited");
    }

    @Test(priority = 4, dependsOnMethods = {"updateProduct"})
    public void triggerEvent() throws AxisFault {
        editProduct(150, 5);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException :" + e.getMessage());
        }

        OMElement response = getProduct(150);
        Assert.assertNotNull(response, "Response null");
        Assert.assertEquals(((OMElement) ((OMElement) response.getChildrenWithLocalName("Product").next()).getChildrenWithLocalName("quantityInStock").next()).getText(), "600", "Event Not Triggered");
        log.info("Event trigger verified");

    }

    @Test(priority = 5, dependsOnMethods = {"triggerEvent"})
    public void deleteService() throws ServiceAdminException, RemoteException {
        deleteService(serviceName);
        log.info(serviceName + " deleted");
    }

    private void addProduct(int productId) throws AxisFault {
        OMElement payload = fac.createOMElement("addProduct", omNs);

        OMElement productCode = fac.createOMElement("productCode", omNs);
        productCode.setText(productId + "");
        payload.addChild(productCode);

        OMElement productName = fac.createOMElement("productName", omNs);
        productName.setText("product");
        payload.addChild(productName);

        OMElement productLine = fac.createOMElement("productLine", omNs);
        productLine.setText("2");
        payload.addChild(productLine);

        OMElement quantityInStock = fac.createOMElement("quantityInStock", omNs);
        quantityInStock.setText("500");
        payload.addChild(quantityInStock);

        OMElement buyPrice = fac.createOMElement("buyPrice", omNs);
        buyPrice.setText("10");
        payload.addChild(buyPrice);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "addProduct");


    }

    private void editProduct(int productId, int quantity) throws AxisFault {
        OMElement payload = fac.createOMElement("updateProductQuantity", omNs);

        OMElement productCode = fac.createOMElement("productCode", omNs);
        productCode.setText(productId + "");
        payload.addChild(productCode);


        OMElement quantityInStock = fac.createOMElement("quantityInStock", omNs);
        quantityInStock.setText(quantity + "");
        payload.addChild(quantityInStock);


        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "updateProductQuantity");


    }

    private OMElement getProduct(int productId) throws AxisFault {
        OMElement payload = fac.createOMElement("getProductByCode", omNs);

        OMElement productCode = fac.createOMElement("productCode", omNs);
        productCode.setText(productId + "");
        payload.addChild(productCode);

        return new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getProductByCode");

    }

    private DataHandler getArtifactWithSubscription(String serviceFile)
            throws RSSAdminRSSDAOExceptionException, IOException, ClassNotFoundException,
                   SQLException {
        SqlDataSourceUtil sqlDataSource;

        sqlDataSource = new SqlDataSourceUtil(sessionCookie, dssBackEndUrl, FrameworkFactory.getFrameworkProperties("DSS"), 3);
        sqlDataSource.createDataSource(getSqlScript());
        Assert.assertNotNull("Initialize jdbcUrl", sqlDataSource.getJdbcUrl());
        Assert.assertNotNull("Initialize jdbcUrl", proxyUrl);
        try {
            OMElement dbsFile = AXIOMUtil.stringToOM(FileManager.readFile(serviceFileLocation + File.separator + serviceFile).trim());
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("org.wso2.ws.dataservice.protocol".equals(value)) {
                    property.setText(sqlDataSource.getJdbcUrl());

                } else if ("org.wso2.ws.dataservice.user".equals(value)) {
                    property.setText(sqlDataSource.getDatabaseUser());

                } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                    property.setText(sqlDataSource.getDatabasePassword());
                }
            }

            Iterator events = dbsFile.getChildrenWithLocalName("event-trigger");
            while (events.hasNext()) {
                OMElement event = (OMElement) events.next();
                if ("product_stock_low_trigger".equals(event.getAttributeValue(new QName("id")))) {
                    event.getFirstChildWithName(new QName("subscriptions")).getFirstChildWithName(new QName("subscription")).setText(proxyUrl);
                }
            }

            log.debug(dbsFile);
            ByteArrayDataSource dbs = new ByteArrayDataSource(dbsFile.toString().getBytes());
            return new DataHandler(dbs);


        } catch (XMLStreamException e) {
            log.error("XMLStreamException when Reading Service File" + e.getMessage());
            throw new RuntimeException("XMLStreamException when Reading Service File" + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IOException when Reading Service File" + e.getMessage());
            throw new RuntimeException("IOException  when Reading Service File" + e.getMessage(), e);
        }
    }

    private void createProxy()
            throws ProxyServiceAdminProxyAdminException, ServiceAdminException,
                   LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder;
        try {
            builder = new EnvironmentBuilder().esb(3);
        } catch (RemoteException e) {
            throw new RemoteException("No Response from ESB Server. Please Check whether ESB is up and running. "
                                      + e.getMessage(), e);
        }
        EnvironmentVariables esbServer = builder.build().getEsb();
        String esbBackEndUrl = esbServer.getBackEndUrl();

        AdminServiceAuthentication adminServiceAuthentication = new AdminServiceAuthentication(esbBackEndUrl);
        AdminServiceProxyServiceAdmin adminServiceProxyServiceAdmin = new AdminServiceProxyServiceAdmin(esbBackEndUrl);
        AdminServiceService adminServiceService = new AdminServiceService(esbBackEndUrl);
        String esbSessionCookie = adminServiceAuthentication.login(userInfo.getUserName(), userInfo.getPassword(), "localhost");
        final String proxyName = "eventTrigerProxy";
        ServiceMetaData serviceMetaData;
        String[] endpoints;

        if (adminServiceService.isServiceExists(esbSessionCookie, proxyName)) {
            adminServiceProxyServiceAdmin.deleteProxy(esbSessionCookie, proxyName);
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                Assert.fail("Thread InterruptedException :" + e.getMessage());
            }
        }

        try {
            OMElement proxyFile = AXIOMUtil.stringToOM(FileManager.readFile(resourceFileLocation + File.separator + "resources" + "/eventTrigerProxy.xml").trim());

            OMElement target = proxyFile.getFirstElement();
            Iterator i = target.getChildrenWithName(new QName("endpoint"));
            ProductUrlGeneratorUtil urlGenerator = new ProductUrlGeneratorUtil();
            while (i.hasNext()) {
                OMElement endpoint = (OMElement) i.next();
                OMElement address = endpoint.getFirstElement();
                OMAttribute uri = address.getAttribute(new QName("uri"));
                uri.setAttributeValue(urlGenerator.getHttpServiceURL(dssServer.getProductVariables().getHttpPort(), dssServer.getProductVariables().getNhttpPort(),
                                                                     dssServer.getProductVariables().getHostName(),
                                                                     FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME),
                                                                     userInfo) + "/EventingTest/updateProductQuantity");
            }

            ByteArrayDataSource dbs = new ByteArrayDataSource(proxyFile.toString().getBytes());
            adminServiceProxyServiceAdmin.addProxyService(esbSessionCookie, new DataHandler(dbs));

            Calendar startTime = Calendar.getInstance();
            long time;
            while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < frameworkSettings.getEnvironmentVariables().getDeploymentDelay()) {
                if (adminServiceService.isServiceExists(esbSessionCookie, proxyName)) {
                    break;
                }
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Assert.fail("Thread InterruptedException :" + e.getMessage());
                }

            }
            Assert.assertTrue((time < frameworkSettings.getEnvironmentVariables().getDeploymentDelay()), "Proxcy deployment time out");
            serviceMetaData = adminServiceService.getServicesData(esbSessionCookie, proxyName);
            endpoints = serviceMetaData.getEprs();
            Assert.assertNotNull(endpoints, "Service Endpoint object null");
            Assert.assertTrue((endpoints.length > 0), "No service endpoint found");
            for (String epr : endpoints) {
                if (epr.startsWith("http://")) {
                    proxyUrl = epr;
                    break;
                }
            }
            log.info("Proxy Service End point :" + proxyUrl);
            Assert.assertNotNull("service endpoint null", proxyUrl);
            Assert.assertTrue(proxyUrl.contains(proxyName), "Service endpoint not contain service name");

        } catch (MalformedURLException e) {
            log.error(e);
            throw new RuntimeException("MalformedURLException : " + e.getMessage(), e);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException("IOException : " + e.getMessage(), e);
        } catch (XMLStreamException e) {
            log.error(e);
            throw new RuntimeException("XMLStreamException : " + e.getMessage(), e);
        }

    }


    private List<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(new File(resourceFileLocation + File.separator + "sql" + File.separator + "MySql" + File.separator + "CreateTables.sql"));
        return al;
    }
}
