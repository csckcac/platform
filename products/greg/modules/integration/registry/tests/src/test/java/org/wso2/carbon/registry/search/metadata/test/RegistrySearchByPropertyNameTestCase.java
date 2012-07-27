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
package org.wso2.carbon.registry.search.metadata.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;

public class RegistrySearchByPropertyNameTestCase {
    private String sessionCookie;

    private SearchAdminServiceClient searchAdminService;
    private WSRegistryServiceClient registry;
    private Registry governance;

    @BeforeClass
    public void init() throws Exception {
        final String SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);

        addResources();
        //wait to cache  resources in order to search
        Thread.sleep(1000 * 60);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by available Property Name")
    public void searchResourceByPropertyName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("x");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) registry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("x")) {
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());

        }

    }

    @Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search by PropertyName pattern matching")
    public void searchResourceByPropertyNamePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("abc%pqr");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid PropertyName pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) registry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                String prop = properties.next();
                if (prop.contains("abc") && prop.contains("pqr")) {
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "PropertyName pattern not found on Resource. " + resource.getResourcePath());

        }


    }


    @Test(priority = 4, groups = {"wso2.greg"}, description = "Metadata search by unavailable PropertyName")
    public void searchResourceByUnAvailablePropertyName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(priority = 5, dataProvider = "invalidCharacter", groups = {"wso2.greg"},
          description = "Metadata search by PropertyName with invalid characters")
    public void searchResourceByPropertyNameWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setPropertyName(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminService.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                Assert.assertNull(result.getResourceDataList(), "Result Object found.");
            }else{
                Assert.assertNull(result, "No results returned");
            }
        }


    }

    private void addResources() throws RegistryException, IOException {
        addService("sns1", "autoService1");
        addService("sns2", "autoService2");
        addWSDL();
        addPolicy();
        addSchema();
    }

    private void addService(String nameSpace, String serviceName)
            throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {
                Resource resource = governance.get(service.getPath());
                resource.addProperty("x", "10");
                governance.put(service.getPath(), resource);

            }

        }

    }

    private void addWSDL() throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = GregTestUtils.getResourcePath()
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(GregTestUtils.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Resource resource = governance.get(wsdl.getPath());
        resource.addProperty("y", "20");
        governance.put(wsdl.getPath(), resource);

    }

    private void addSchema() throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person4.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        Resource resource = governance.get(schema.getPath());
        resource.addProperty("z", "30");
        governance.put(schema.getPath(), resource);
    }

    private void addPolicy() throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy4.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        Resource resource = governance.get(policy.getPath());
        resource.addProperty("abcxyzpqr", "40");
        governance.put(policy.getPath(), resource);

    }

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
                {"@"},
                {"|"},
                {"^"},
                {"/"},
                {"\\"},
                {","},
                {"\""},
                {"~"},
                {"!"},
                {"*"},
                {"{"},
                {"}"},
                {"["},
                {"]"},
                {"-"},
                {"("},
                {")"}
        };


    }
}
