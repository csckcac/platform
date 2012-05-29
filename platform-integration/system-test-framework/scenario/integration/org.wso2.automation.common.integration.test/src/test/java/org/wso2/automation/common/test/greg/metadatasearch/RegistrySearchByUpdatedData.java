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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.metadatasearch.bean.SearchParameterBean;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
search registry metadata by resource updated data
*/
public class RegistrySearchByUpdatedData {
    private static final Log log = LogFactory.getLog(RegistrySearchByUpdatedData.class);
    private String gregBackEndUrl;

    private String sessionCookie;
    private EnvironmentVariables gregServer;

    private RegistrySearchAdminService searchAdminService;
    private WSRegistryServiceClient registry;

    @BeforeClass
    public void init()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(3);
        gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        gregBackEndUrl = gregServer.getBackEndUrl();
        searchAdminService = new RegistrySearchAdminService(gregBackEndUrl);
        registry = new RegistryProvider().getRegistry(3, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by updated date from")
    public void searchResourceByUpdatedDateFrom()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.YEAR, -1);
        paramBean.setUpdatedAfter(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();
        log.info("From Date : " + formatDate(calender.getTime()));

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid from date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.before(registry.getMetaData(resource.getResourcePath()).getLastModified()),
                              "Resource updated date is a previous date of the mentioned date on From date");


        }

    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by updated date To")
    public void searchResourceByUpdatedDateTo()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        log.info("To Date : " + formatDate(calender.getTime()));
        paramBean.setUpdatedBefore(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid to date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.after(registry.getMetaData(resource.getResourcePath()).getLastModified()),
                              "Resource updated date is a later date of the mentioned date on From date");

        }
    }

    @Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceByValidUpdatedDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -1);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(toCalender.after(registry.getMetaData(resource.getResourcePath()).getLastModified())
                              && fromCalender.before(registry.getMetaData(resource.getResourcePath()).getLastModified()),
                              "Resource updated date is a not within the mentioned date range");

        }
    }

    @Test(priority = 4, groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceWithInvalidDateFormat()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Format formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -1);
        log.info("From Date : " + formatter.format(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatter.format(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatter.format(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatter.format(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Record Found for invalid data format");
    }

    @Test(priority = 5, groups = {"wso2.greg"}, description = "Metadata search records not in valid date range ")
    public void searchResourceByValidUpdatedDateRangeNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -3);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.add(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);

        // to set updatedRangeNegate
        ArrayOfString updatedRangeNegate = new ArrayOfString();
        updatedRangeNegate.setArray(new String[]{"updatedRangeNegate", "on"});

        searchQuery.addParameterValues(updatedRangeNegate);

        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertFalse((toCalender.after(registry.getMetaData(resource.getResourcePath()).getLastModified())
                                && fromCalender.before(registry.getMetaData(resource.getResourcePath()).getLastModified())),
                               "Resource updated date is a not within the mentioned date range");

        }
    }

    @Test(priority = 6, groups = {"wso2.greg"},
          description = "Metadata search from valid updated date range having no resource")
    public void searchResourceByValidUpdatedDateRangeHavingNoRecords()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -5);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -3);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Record Found");
    }

    @Test(priority = 7, groups = {"wso2.greg"}, description = "Metadata search from invalid updated date range")
    public void searchResourceByInValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Record Found");
    }

    @Test(priority = 8, groups = {"wso2.greg"}, dataProvider = "invalidCharacter",
          description = "Metadata search by invalid String for updated date")
    public void searchResourceByInvalidValueForUpdatedDate(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdatedAfter(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found.");

    }

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"invalid-date"},
                {"<a>"},
                {"#"},
                {"a|b"},
                {"   "},
                {"@"},
                {"|"},
                {"^"},
                {"abc^"},
                {"/"},
                {"\\"}
        };

    }

    private String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }
}
