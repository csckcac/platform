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
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.metadatasearch.bean.SearchParameterBean;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
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
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
test matching resource with all fields
 */
public class RegistrySearchByAll {
    private String sessionCookie;
    private String userName;
    private String destinationPath;

    private RegistrySearchAdminService searchAdminService;
    private Registry governance;

    @BeforeClass
    public void init()
            throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        userName = UserListCsvReader.getUserInfo(userId).getUserName();
        searchAdminService = new RegistrySearchAdminService(gregServer.getBackEndUrl());
        WSRegistryServiceClient registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);

        addResources();
//        //wait to cache  resources in order to search
        Thread.sleep(1000 * 60 * 1);

    }

    @Test(priority = 1, description = "Metadata search by All fields for wsdl")
    public void searchWsdlByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        searchWsdl();

    }

    @Test(priority = 2, description = "Metadata search by All fields for wsdl when having two wsdl name starting same prefix")
    public void searchWsdlByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, GovernanceException,
                   InterruptedException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo1.wsdl");
        wsdlManager.addWsdl(wsdl);
        Thread.sleep(1000 * 60 * 2);

        searchWsdl();

    }


    @Test(priority = 3, description = "Metadata search by All fields for schema")
    public void searchSchemaByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        searchSchemaFile();

    }

    @Test(priority = 4, description = "Metadata search by All fields for schema when having two schema name starting same prefix")
    public void searchSchemaByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, GovernanceException,
                   InterruptedException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person1.xsd");
        schemaManager.addSchema(schema);

        Thread.sleep(1000 * 60 * 2);

        searchSchemaFile();

    }

    @Test(priority = 5, description = "Metadata search by All fields for policy")
    public void searchPolicyByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        searchPolicyFile();

    }

    @Test(priority = 6, description = "Metadata search by All fields for policy when having two policy name starting same prefix")
    public void searchPolicyByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, GovernanceException,
                   InterruptedException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy1.xml");
        policyManager.addPolicy(policy);

        Thread.sleep(1000 * 60 * 2);

        searchPolicyFile();
    }

    @Test(priority = 7, description = "Search schema with all fields with wrong tag")
    public void searchSchemaNotExist()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("Person");
        paramBean.setContent("PersonType");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag1234");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(destinationPath);
        paramBean.setMediaType("application/x-xsd+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "No Record Found");

    }

    private void addResources() throws Exception {
        destinationPath = addService("sns1", "autoService1");
        addWSDL(destinationPath, "associationType1");
        addSchema(destinationPath, "associationType1");
        addPolicy(destinationPath, "associationType1");

    }

    private String addService(String nameSpace, String serviceName)
            throws Exception {
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
        Comment comment = new Comment();
        comment.setText("TestAutomation Comment");
        governance.addComment(wsdl.getPath(), comment);
        governance.applyTag(wsdl.getPath(), "autoTag");

    }

    private void addSchema(String destinationPath, String type)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.addAssociation(schema.getPath(), destinationPath, type);
        Comment comment = new Comment();
        comment.setText("TestAutomation Comment");
        governance.addComment(schema.getPath(), comment);
        governance.applyTag(schema.getPath(), "autoTag");

    }

    private void addPolicy(String destinationPath, String type)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.addAssociation(policy.getPath(), destinationPath, type);
        Comment comment = new Comment();
        comment.setText("TestAutomation Comment");
        governance.addComment(policy.getPath(), comment);
        governance.applyTag(policy.getPath(), "autoTag");
    }

    private String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }

    private void searchPolicyFile()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("UTPolicy");
        paramBean.setContent("TransportToken");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(destinationPath);
        paramBean.setMediaType("application/policy+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), "UTPolicy.xml", "Schema not found");
        }
    }

    private void searchSchemaFile()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("Person");
        paramBean.setContent("PersonType");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(destinationPath);
        paramBean.setMediaType("application/x-xsd+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), "Person.xsd", "Schema not found");
        }
    }

    private void searchWsdl() throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("echo");
        paramBean.setContent("echoString");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(destinationPath);
        paramBean.setMediaType("application/wsdl+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), "echo.wsdl", "wsdl not found");
        }
    }
}
