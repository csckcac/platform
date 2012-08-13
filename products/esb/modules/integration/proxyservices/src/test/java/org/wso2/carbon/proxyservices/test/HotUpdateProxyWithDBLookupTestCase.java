/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.proxyservices.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.synapse.commons.datasource.DataSourceInformation;
import org.apache.synapse.commons.datasource.serializer.DataSourceInformationSerializer;
import org.testng.annotations.Test;
import org.wso2.carbon.datasource.ui.stub.DataSourceAdminStub;
import org.wso2.carbon.datasource.ui.stub.DataSourceManagementException;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.proxyservices.test.util.StockQuoteClient;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.securevault.secret.SecretInformation;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Properties;

import static org.testng.Assert.assertNotNull;

public class HotUpdateProxyWithDBLookupTestCase extends ESBIntegrationTestCase {

    private ProxyServiceAdminStub proxyServiceAdminStub;
    private DataSourceAdminStub dataSourceAdminStub;
    private StockQuoteClient client;
    private ArtifactReader reader;

    private static final String PROXY_SERVICE_NAME = "DBLookupProxy";
    private static final String DATA_SOURCE_NAME = "TestDS";
    private static final String DATA_SOURCE_ADMIN = "DataSourceAdmin";
    private static final String PROXY_SERVICE_ADMIN = "ProxyServiceAdmin";

    public HotUpdateProxyWithDBLookupTestCase() {
        super();
    }

    public void init() throws Exception {
        reader = new ArtifactReader();

        dataSourceAdminStub = new DataSourceAdminStub(getAdminServiceURL(DATA_SOURCE_ADMIN));
        proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL(PROXY_SERVICE_ADMIN));
        authenticate(getDataSourceAdminStub());
        authenticate(getProxyServiceAdminStub());

        client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"},
            description = "Test whether the associated data source is closed after a hot update of the proxy service")
    public void testDataSourceStatus() throws AxisFault {
        //creating the data source
        createDataSource();
        //deploying the proxy service
        deployProxyService();
        //execute
        try {
            OMElement result =
                    getClient().stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME +
                            ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            assertNotNull(result);
            log.info(result);
        } catch (Exception e) {
            log.error("Error occurred while executing the db look up mediator proxy test", e);
        }
        //redeploying the proxy service
        reDeployProxyService();
        //execute
        try {
            OMElement result =
                    getClient().stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME +
                            ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            assertNotNull(result);
            log.info(result);
        } catch (Exception e) {
            log.error("Error occurred while executing the db look up mediator proxy test", e);
        }
        //removing the proxy service
        removeProxyService();
        //removing the data source
        removeDataSource();
    }

    /**
     * Creates the sample data source to be used in the db lookup configuration
     *
     * @throws AxisFault throws axisFault if the data source creation fails
     */
    public void createDataSource() throws AxisFault {
        DataSourceInformation dsInfo = new DataSourceInformation();
        dsInfo.setDatasourceName(HotUpdateProxyWithDBLookupTestCase.DATA_SOURCE_NAME);
        dsInfo.setUrl("jdbc:h2:" + FrameworkSettings.CARBON_HOME + File.separator +
                "repository" + File.separator + "database" + File.separator +
                "esbdb;DB_CLOSE_ON_EXIT=FALSE");
        dsInfo.setDriver("org.h2.Driver");
        SecretInformation secInfo = new SecretInformation();
        secInfo.setUser("esb");
        secInfo.setAliasSecret("esb");
        dsInfo.setSecretInformation(secInfo);

        Properties dsProperties = DataSourceInformationSerializer.serialize(dsInfo);
        OMElement dsConfigEl = createOMElement(dsProperties);
        try {
            getDataSourceAdminStub().addDataSourceInformation(
                    HotUpdateProxyWithDBLookupTestCase.DATA_SOURCE_NAME, dsConfigEl);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while creating the datasource '" +
                    DATA_SOURCE_NAME + "'", e);
        } catch (DataSourceManagementException e) {
            throw new AxisFault("Error occurred while creating the datasource '" +
                    DATA_SOURCE_NAME + "'", e);
        }
    }

    /**
     * Removes the data source created for testing purposes of the db look up mediator test
     *
     * @throws AxisFault throws an axisFault if the data source removal fails
     */
    public void removeDataSource() throws AxisFault {
        try {
            getDataSourceAdminStub().removeDataSourceInformation(
                    HotUpdateProxyWithDBLookupTestCase.DATA_SOURCE_NAME);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while removing data source '" +
                    DATA_SOURCE_NAME + "'", e);
        } catch (DataSourceManagementException e) {
            throw new AxisFault("Error occurred while removing data source '" +
                    DATA_SOURCE_NAME + "'", e);
        }
    }

    /**
     * Deploys a sample proxy service that would contain the db lookup mediator configuration
     *
     * @throws AxisFault throws an axisFault if the deployment of the proxy service fails
     */
    private void deployProxyService() throws AxisFault {
        ProxyData proxyData = new ProxyData();
        proxyData.setName(PROXY_SERVICE_NAME);
        OMElement outSeqEl =
                getArtifactReader().getOMElement(HotUpdateProxyWithDBLookupTestCase.class.getResource(
                        "/DBLookupProxyTest/dbLookupOutSeq.xml").getPath());
        proxyData.setOutSeqXML(outSeqEl.toString());
        OMElement endpointEl =
                getArtifactReader().getOMElement(HotUpdateProxyWithDBLookupTestCase.class.getResource(
                        "/DBLookupProxyTest/dbLookupProxyEndpoint.xml").getPath());
        proxyData.setEndpointXML(endpointEl.toString());
//        proxyData.setWsdlURI("file:repository/samples/resources/proxy/sample_proxy_1.wsdl");

        try {
            getProxyServiceAdminStub().addProxy(proxyData);
            assertNotNull(getProxyServiceAdminStub().getProxy(PROXY_SERVICE_NAME));

        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while creating proxy '" + proxyData.getName() +
                    "'", e);
        } catch (ProxyServiceAdminProxyAdminException e) {
            throw new AxisFault("Error occurred while creating proxy '" + proxyData.getName() +
                    "'", e);
        }
    }

    /**
     * Redeploys a the sample proxy service
     *
     * @throws AxisFault throws an axisFault if the redeployment of the sample proxy service fails
     */
    private void reDeployProxyService() throws AxisFault {
        try {
            getProxyServiceAdminStub().redeployProxyService(PROXY_SERVICE_NAME);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while retrieving the proxy '" + PROXY_SERVICE_NAME +
                    "'", e);
        } catch (ProxyServiceAdminProxyAdminException e) {
            throw new AxisFault("Error occurred while retrieving the proxy '" + PROXY_SERVICE_NAME +
                    "'", e);
        }
    }

    /**
     * Removes the proxy service added for testing purposes
     *
     * @throws AxisFault throws an axisFault if the removal of the sample proxy service fails
     */
    private void removeProxyService() throws AxisFault {
        try {
            getProxyServiceAdminStub().deleteProxyService(PROXY_SERVICE_NAME);
        } catch (RemoteException e) {
            throw new AxisFault("Error occurred while deleting the proxy '" + PROXY_SERVICE_NAME +
                    "'", e);
        } catch (ProxyServiceAdminProxyAdminException e) {
            throw new AxisFault("Error occurred while deleting the proxy '" + PROXY_SERVICE_NAME +
                    "'", e);
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        try {
            getDataSourceAdminStub().cleanup();
            getProxyServiceAdminStub().cleanup();
        } catch (AxisFault ignored) {

        }
    }

    private DataSourceAdminStub getDataSourceAdminStub() {
        return dataSourceAdminStub;
    }

    private ProxyServiceAdminStub getProxyServiceAdminStub() {
        return proxyServiceAdminStub;
    }

    private StockQuoteClient getClient() {
        return client;
    }

    private ArtifactReader getArtifactReader() {
        return reader;
    }

    /**
     * Serializes data source properties to an OMElement
     *
     * @param properties Data source properties
     * @return serialized data source properties
     */
    private static OMElement createOMElement(Properties properties) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            properties.storeToXML(baos, "");
            String propertyS = new String(baos.toByteArray());
            String correctedS = propertyS.substring(propertyS.indexOf("<properties>"),
                    propertyS.length());
            String inLined = "<!DOCTYPE properties   [\n" +
                    "\n" +
                    "<!ELEMENT properties ( comment?, entry* ) >\n" +
                    "\n" +
                    "<!ATTLIST properties version CDATA #FIXED \"1.0\">\n" +
                    "\n" +
                    "<!ELEMENT comment (#PCDATA) >\n" +
                    "\n" +
                    "<!ELEMENT entry (#PCDATA) >\n" +
                    "\n" +
                    "<!ATTLIST entry key CDATA #REQUIRED>\n" +
                    "]>";
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(inLined + correctedS));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            handleException("Error Creating a OMElement from properties : " + properties, e);
        } catch (IOException e) {
            handleException("IOError Creating a OMElement from properties : " + properties, e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }

            }
        }
        return null;
    }

    private static void handleException(String msg, Throwable e) {
        throw new IllegalArgumentException(msg, e);
    }

}
