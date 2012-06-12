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
import org.wso2.carbon.registry.core.Resource;
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
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;

/*
Search Registry metadata by mediaType
 */
public class RegistrySearchByMediaType {
    private String sessionCookie;

    private RegistrySearchAdminService searchAdminService;
    private WSRegistryServiceClient registry;

    @BeforeClass
    public void init()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(3);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        searchAdminService = new RegistrySearchAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(3, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by available MediaType")
    public void searchResourceByMediaType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("application/vnd.wso2-service+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid MediaType");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(registry.get(resource.getResourcePath()).getMediaType(), "application/vnd.wso2-service+xml",
                                "search keyword not contain on MediaType :" + resource.getResourcePath());
        }


    }

    @Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search by MediaType pattern matching")
    public void searchResourceByMediaTypePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("%vnd%service%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid MediaType pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());
            Assert.assertTrue((rs.getMediaType().contains("vnd") && rs.getMediaType().contains("service")),
                              "search word pattern not contain on MediaType :" + resource.getResourcePath());
        }


    }


    @Test(priority = 4, groups = {"wso2.greg"}, description = "Metadata search by unavailable MediaType")
    public void searchResourceByUnAvailableMediaType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(priority = 5, dataProvider = "invalidCharacter", groups = {"wso2.greg"},
          description = "Metadata search by MediaType with invalid characters")
    public void searchResourceByMediaTypeWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setMediaType(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found.");


    }

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
//                {"   "},
//                {""},
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
