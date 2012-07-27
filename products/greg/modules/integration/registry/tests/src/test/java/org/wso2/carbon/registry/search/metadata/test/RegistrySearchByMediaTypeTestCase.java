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
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
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

import java.rmi.RemoteException;

/*
Search Registry metadata by mediaType
 */
public class RegistrySearchByMediaTypeTestCase {
    private String sessionCookie;

    private SearchAdminServiceClient searchAdminService;
    private WSRegistryServiceClient registry;

    @BeforeClass
    public void init() throws Exception {
        final String SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();

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
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
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
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
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
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
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

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
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
