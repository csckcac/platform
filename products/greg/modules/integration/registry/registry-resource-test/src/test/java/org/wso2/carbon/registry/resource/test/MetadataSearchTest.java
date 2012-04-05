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

package org.wso2.carbon.registry.resource.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MetadataSearchTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(MetadataSearchTest.class);

    private String wsdlPath = "/_system/governance/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private SearchAdminServiceStub searchAdminServiceStub;
    private ResourceAdminServiceStub resourceAdminServiceStub;


    @Override
    public void init() {
        log.info("Initializing Tests for Meta-data Search");
        log.debug("Meta-data Search Test Initialised");

    }

    @Override
    public void runSuccessCase() {

        log.debug("Running SuccessCase");
        searchAdminServiceStub = TestUtils.getSearchAdminServiceStub(sessionCookie);
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);

        addResource();
        searchMetadata();
    }

    private void addResource() {

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(wsdlPath + resourceName,
                    "application/wsdl+xml", "test resource", new DataHandler(new URL("file:///" + resource)), null);

        } catch (Exception e) {
            Assert.fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }
    }

    private void searchMetadata() {
        AdvancedSearchResultsBean bean = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {

            // searchAdminCommand.getSearchResultsSuccessCase("sample.wsdl", "admin", dateFormat.format(calendar.getTime()), dateFormat.format(calendar.getTime()));
            //advanced search
            CustomSearchParameterBean parameterBean = new CustomSearchParameterBean();


            ArrayOfString[] finalArray = new ArrayOfString[3];

            ArrayOfString nameArray = new ArrayOfString();
            nameArray.addArray("resourcePath");
            nameArray.addArray("sample.wsdl");
            finalArray[0] = nameArray;

            ArrayOfString authorArray = new ArrayOfString();
            authorArray.addArray("author");
            authorArray.addArray("admin");
            finalArray[1] = authorArray;

            ArrayOfString contentArray = new ArrayOfString();
            contentArray.addArray("content");
            contentArray.addArray("ArrayOftCountrySelectedTopScorer");
            finalArray[2] = contentArray;

            parameterBean.setParameterValues(finalArray);


            bean = searchAdminServiceStub.getAdvancedSearchResults(parameterBean);

            if (bean.getResourceDataList() != null) {
                bean.setResourceDataList(new ResourceData[0]);

            } else {
                Assert.fail("Failed to get search results from the search service");
                log.error("Failed to get search results from the search service");
            }

        } catch (Exception e) {
            Assert.fail("Failed to get search results from the search service: " + e);
            log.error("Failed to get search results from the search service: " + e);

        }


    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }
}
