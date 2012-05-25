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
search registry metadata by resource created data
*/
public class RegistrySearchByCratedData {
    private static final Log log = LogFactory.getLog(RegistrySearchByCratedData.class);
    private String gregBackEndUrl;

    private String sessionCookie;
    private EnvironmentVariables gregServer;

    private RegistrySearchAdminService searchAdminService;
    WSRegistryServiceClient registry;

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

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Metadata search by created date from")
    public void searchResourceByCreatedDateFrom()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.YEAR, -1);
        paramBean.setCreatedAfter(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();
        log.info("From Date : " + formatDate(calender.getTime()));

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid from date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.before(resource.getCreatedOn()),
                              "Resource created date is a previous date of the mentioned date on From date");


        }

    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by created date To")
    public void searchResourceByCreatedDateTo()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        log.info("To Date : " + formatDate(calender.getTime()));
        paramBean.setCreatedBefore(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid to date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.after(resource.getCreatedOn()),
                              "Resource created date is a later date of the mentioned date on From date");

        }
    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceByValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -1);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(toCalender.after(resource.getCreatedOn()) && fromCalender.before(resource.getCreatedOn()),
                              "Resource created date is a not within the mentioned date range");

        }
    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search records not in valid date range ")
    public void searchResourceByValidDateRangeNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -4);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.add(Calendar.YEAR, -2);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setCreatedDateRangeNot(true);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertFalse((toCalender.after(resource.getCreatedOn()) && fromCalender.before(resource.getCreatedOn())),
                               "Resource created date is a not within the mentioned date range");

        }
    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search from valid date range having no resource")
    public void searchResourceByValidDateRangeHavingNoRecords()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -5);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -3);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Record Found");
    }

    @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search from invalid date range")
    public void searchResourceByInValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Record Found");
    }

    @Test(priority = 1, groups = {"wso2.greg"}, dataProvider = "invalidCharacter",
          description = "Metadata search by invalid String for date")
    public void searchResourceByInvalidValueForDate(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCreatedAfter(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Records object not null");

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
