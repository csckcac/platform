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
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistrySpecialSearchTestCase {


    private CarbonAppUploaderClient cAppUploader;
    private SearchAdminServiceClient searchAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        cAppUploader = new CarbonAppUploaderClient(environment.getGreg().getBackEndUrl(),
                                                   userInfo.getUserName(), userInfo.getPassword());

        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                userInfo.getUserName(), userInfo.getPassword());

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    userInfo.getUserName(), userInfo.getPassword());

        uploadCApplication();
        //wait to cache  resources in order to search
        Thread.sleep(1000 * 20);
    }

    public void uploadCApplication()
            throws MalformedURLException, RemoteException, InterruptedException,
                   ApplicationAdminExceptionException {
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "text_resources_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("text_resources_1.0.0.car",
                                             new DataHandler(new URL("file://" + filePath)));

    }

    @Test(groups = {"wso2.greg"}, description = "verify CApp search")
    public void verifyCAppSearchContent()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, InterruptedException {
        String resourceName = "text_files.xml";
//        long startTime = System.currentTimeMillis();
//        long timeDifference = 0;
//        boolean status = false;
//        while ((timeDifference / 1000) < 60) {
//            ResourceData[] resourceData = resourceAdminServiceClient.getResource("/_system/capps1/" + resourceName);
//            for (ResourceData resourceInfo : resourceData) {
//                if (resourceInfo.getName().contains(resourceName)) {
//                    status = true;
//                    break;
//                }
//            }
//
//            if (status) {
//                break;
//            }
//            Thread.sleep(1000);
//            timeDifference = startTime - System.currentTimeMillis();
//        }

        Assert.assertTrue(searchResource("text_files.xml"));
        Assert.assertTrue(searchResource("buggggg.txt"));
    }


    public boolean searchResource(String resourceName)
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(resourceName);
        boolean resourceExists = false;
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0));

        for (org.wso2.carbon.registry.search.stub.common.xsd.ResourceData resource : result.getResourceDataList()) {
            if (resource.getResourcePath().equals("/_system/capps/" + resourceName)) {
                resourceExists = true;
                break;
            }

        }

        return resourceExists;
    }
}