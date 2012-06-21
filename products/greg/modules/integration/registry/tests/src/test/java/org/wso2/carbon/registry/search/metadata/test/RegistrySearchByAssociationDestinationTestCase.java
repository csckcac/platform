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
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
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

/*
Search Registry metadata by AssociationDestination
 */
public class RegistrySearchByAssociationDestinationTestCase {

    private String sessionCookie;

    private RegistrySearchAdminService searchAdminService;
    private WSRegistryServiceClient registry;
    private Registry governance;

    private String destinationPath1 = null;

    @BeforeClass
    public void init() throws Exception {

        final String  SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie =new LoginLogoutUtil().login();

        searchAdminService = new RegistrySearchAdminService(SERVER_URL);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);

        addResources();
        //wait to cache  resources in order to search
        Thread.sleep(1000 * 60);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by available AssociationDestination")
    public void searchResourceByAssociationDestination()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        Assert.assertNotNull(destinationPath1, "Destination path not found for search Query. Clarity Error");
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest(destinationPath1);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination");

        for (ResourceData resource : result.getResourceDataList()) {
            Association[] array = registry.getAllAssociations(resource.getResourcePath());
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            boolean associationDestination = false;
            for (Association association : array) {
                if (association.getDestinationPath().contains(destinationPath1)) {
                    associationDestination = true;
                    break;
                }

            }

            Assert.assertTrue(associationDestination, "Association Destination not found on Resource"
                                                      + resource.getResourcePath());
        }


    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by available " +
                                                              "Association Destination relative path")
    public void searchResourceByAssociationDestinationRelativePath()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        final String relativePath = "sns";
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest(relativePath);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination");

        for (ResourceData resource : result.getResourceDataList()) {
            Association[] array = registry.getAllAssociations(resource.getResourcePath());
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            boolean associationDestination = false;
            for (Association association : array) {
                if (association.getDestinationPath().contains(relativePath)) {
                    associationDestination = true;
                    break;
                }

            }

            Assert.assertTrue(associationDestination, "Association Destination relative path not " +
                                                      "found on Resource" + resource.getResourcePath());
        }


    }

   /* @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by available Association Destinations")
    public void searchResourceByAssociationDestinations()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("autoService1 autoService2");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Types");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationDestinationFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if (association.getAssociationType().contains("autoService1")
                    || association.getAssociationType().contains("autoService2")) {
                    associationDestinationFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestinationFound, "Association Destinations not found on Resource"
                                                           + resource.getResourcePath());

        }


    }*/

    /*@Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search by Association Destination pattern matching")
    public void searchResourceByAssociationDestinationPattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("%sns%autoService%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationDestinationFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if (association.getDestinationPath().contains("sns")
                    && association.getDestinationPath().contains("autoService")) {
                    associationDestinationFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestinationFound, "Association Destination pattern not found on Resource"
                                                           + resource.getResourcePath());

        }

    }*/


    @Test(priority = 4, groups = {"wso2.greg"}, description = "Metadata search by unavailable Association Destination")
    public void searchResourceByUnAvailableAssociationDestination()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("xyz1234ggf76tgf");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(priority = 5, dataProvider = "invalidCharacter", groups = {"wso2.greg"},
          description = "Metadata search by Association Destination with invalid characters")
    public void searchResourceByAssociationDestinationWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setAssociationDest(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found.");

    }

    private void addResources() throws Exception {
        destinationPath1 = addService("sns1", "autoService1");
        String destinationPath2 = addService("sns2", "autoService2");
        addPolicy(destinationPath1, "associationType1");
        addSchema(destinationPath2, "associationType2");
    }

    private String addService(String nameSpace, String serviceName) throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {

                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");

    }

    private void addSchema(String destinationPath, String typ)  throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person1.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.addAssociation(schema.getPath(), destinationPath, typ);
    }

    private void addPolicy(String destinationPath, String type) throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy1.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.addAssociation(policy.getPath(), destinationPath, type);
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
                {"("},
                {")"}
        };


    }
}
