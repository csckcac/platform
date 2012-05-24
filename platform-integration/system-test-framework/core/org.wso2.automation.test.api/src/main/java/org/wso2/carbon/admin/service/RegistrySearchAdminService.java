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
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.MediaTypeValueList;
import org.wso2.carbon.registry.search.stub.beans.xsd.SearchResultsBean;

import java.rmi.RemoteException;

public class RegistrySearchAdminService {
    private static final Log log = LogFactory.getLog(RegistrySearchAdminService.class);

    private final String serviceName = "SearchAdminService";
    private SearchAdminServiceStub searchAdminServiceStub;
    private String endPoint;

    public RegistrySearchAdminService(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        searchAdminServiceStub = new SearchAdminServiceStub(endPoint);
    }

    public void deleteFilter(String sessionCookie, String filterName)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        searchAdminServiceStub.deleteFilter(filterName);
    }

    public CustomSearchParameterBean getAdvancedSearchFilter(String sessionCookie,
                                                             String filterName)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        return searchAdminServiceStub.getAdvancedSearchFilter(filterName);
    }

    public MediaTypeValueList getMediaTypeSearch(String sessionCookie, String mediaType)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        return searchAdminServiceStub.getMediaTypeSearch(mediaType);
    }

    public AdvancedSearchResultsBean getAdvancedSearchResults(String sessionCookie,
                                                              CustomSearchParameterBean searchParams)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        return searchAdminServiceStub.getAdvancedSearchResults(searchParams);
    }

    public String[] getSavedFilters(String sessionCookie)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        return searchAdminServiceStub.getSavedFilters();
    }

    public SearchResultsBean getSearchResults(String sessionCookie, String searchType,
                                              String criteria)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        return searchAdminServiceStub.getSearchResults(searchType, criteria);

    }

    public void saveAdvancedSearchFilter(String sessionCookie, CustomSearchParameterBean queryBean,
                                         String filterName)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, searchAdminServiceStub);
        searchAdminServiceStub.saveAdvancedSearchFilter(queryBean, filterName);

    }
}
