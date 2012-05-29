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
package org.wso2.automation.common.test.greg.metadatasearch;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.metadatasearch.bean.SearchParameterBean;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class RegistrySearchByAssociationType {
    private String gregBackEndUrl;

    private String sessionCookie;
    private EnvironmentVariables gregServer;

    private RegistrySearchAdminService searchAdminService;
    private WSRegistryServiceClient registry;
    private Registry governance;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        gregServer = builder.build().getGreg();


        sessionCookie = gregServer.getSessionCookie();
        gregBackEndUrl = gregServer.getBackEndUrl();
        searchAdminService = new RegistrySearchAdminService(gregBackEndUrl);
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);

        addResources();
        //wait to cache  resources in order to search
        Thread.sleep(1000 * 60);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by available AssociationType")
    public void searchResourceByAssociationType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        final String searchAssociationType = "associationType1";
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType(searchAssociationType);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Type");

        for (ResourceData resource : result.getResourceDataList()) {
            Association[] array = registry.getAssociations(resource.getResourcePath(), searchAssociationType);
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            for (Association association : array) {
                Assert.assertEquals(association.getAssociationType(), searchAssociationType,
                                    "AssociationT type not found on Resource");
            }

        }


    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by available Association Types")
    public void searchResourceByAssociationTypes()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType("associationType1 associationType2");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Types");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationTypeFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if ("associationType1".equalsIgnoreCase(association.getAssociationType())
                    || "associationType2".equalsIgnoreCase(association.getAssociationType())) {
                    associationTypeFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationTypeFound, "Association Types not found on Resource");

        }


    }

    @Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search by Association Type pattern matching")
    public void searchResourceByAssociationTypePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType("%Type%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Type pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationTypeFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if (association.getAssociationType().contains("Type")) {
                    associationTypeFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationTypeFound, "Association Type pattern not found on Resource");

        }


    }


    @Test(priority = 4, groups = {"wso2.greg"}, description = "Metadata search by unavailable AssociationType")
    public void searchResourceByUnAvailableAssociationType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType("xyz1234ggf");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(priority = 5, dataProvider = "invalidCharacter", groups = {"wso2.greg"},
          description = "Metadata search by AssociationType with invalid characters")
    public void searchResourceByAssociationTypeWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setAssociationType(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found.");


    }

    private void addResources() throws Exception {
        String path1 = addService("sns1", "autoService1");
        String path2 = addService("sns2", "autoService2");
        addWSDL(path1, "associationType1");
        addPolicy(path1, "associationType1");
        addSchema(path2, "associationType2");
    }

    private String addService(String nameSpace, String serviceName)
            throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().contains(serviceName)) {

                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");

    }

    private void addWSDL(String destinationPath, String type)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        governance.addAssociation(wsdl.getPath(), destinationPath, type);
    }

    private void addSchema(String destinationPath, String typ)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.addAssociation(schema.getPath(), destinationPath, typ);
    }

    private void addPolicy(String destinationPath, String typ)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.addAssociation(policy.getPath(), destinationPath, typ);
    }

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
                {"   "},
                {""},
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
