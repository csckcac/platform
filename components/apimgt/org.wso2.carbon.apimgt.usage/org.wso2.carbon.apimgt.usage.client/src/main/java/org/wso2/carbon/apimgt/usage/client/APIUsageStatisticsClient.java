/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.usage.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStoreException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class APIUsageStatisticsClient {

    private QueryServiceStub qss;

    private APIProvider apiProviderImpl;

    public APIUsageStatisticsClient() throws APIMgtUsageQueryServiceClientException {
        APIManagerConfiguration config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
        String targetEndpoint = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL);
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
        }

        try {
            qss = new QueryServiceStub(targetEndpoint + "services/QueryService");
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider();
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Error while instantiating QueryServiceStub", e);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating API manager core objects", e);
        }
    }

    /**
     * Returns a list of APIUsageDTO objects that contain information related to APIs that 
     * belong to a particular provider and the number of total API calls each API has processed
     * up to now. This method does not distinguish between different API versions. That is all 
     * versions of a single API are treated as one, and their individual request counts are summed 
     * up to calculate a grand total per each API.
     * 
     * @param providerName Name of the API provider 
     * @return a List of APIUsageDTO objects - possibly empty
     * @throws APIMgtUsageQueryServiceClientException if an error occurs while contacting backend services
     */
    public List<APIUsageDTO> getUsageByAPIs(String providerName) 
            throws APIMgtUsageQueryServiceClientException {
        
        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,APIUsageDTO> usageByAPIs = new TreeMap<String, APIUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {
                    String apiName = usage.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIUsageDTO usageDTO = usageByAPIs.get(apiName);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usage.requestCount);
                    } else {
                        usageDTO = new APIUsageDTO();
                        usageDTO.setApiName(apiName);
                        usageDTO.setCount(usage.requestCount);
                        usageByAPIs.put(apiName, usageDTO);
                    }
                }
            }
        }
        
        return new ArrayList<APIUsageDTO>(usageByAPIs.values());
    }

    /**
     * Returns a list of APIVersionUsageDTO objects that contain information related to a
     * particular API of a specified provider, along with the number of API calls processed
     * by each version of that API.
     *
     * @param providerName Name of the API provider
     * @param apiName Name of th API
     * @return a List of APIVersionUsageDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException on error
     */
    public List<APIVersionUsageDTO> getUsageByAPIVersions(String providerName,
                                                          String apiName) throws APIMgtUsageQueryServiceClientException {

        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX,
                compositeIndex);
        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,APIVersionUsageDTO> usageByVersions = new TreeMap<String, APIVersionUsageDTO>();

        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion) &&
                        providerAPI.getContext().equals(usage.context)) {

                    APIVersionUsageDTO usageDTO = new APIVersionUsageDTO();
                    usageDTO.setVersion(usage.apiVersion);
                    usageDTO.setCount(usage.requestCount);
                    usageByVersions.put(usage.apiVersion, usageDTO);
                }
            }
        }

        return new ArrayList<APIVersionUsageDTO>(usageByVersions.values());
    }

    /**
     * Gets a list of APIResponseTimeDTO objects containing information related to APIs belonging
     * to a particular provider along with their average response times.
     *
     * @param providerName Name of the API provider
     * @return a List of APIResponseTimeDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException on error
     */
    public List<APIResponseTimeDTO> getResponseTimesByAPIs(String providerName)
            throws APIMgtUsageQueryServiceClientException {

        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIResponseTime> responseTimes = getResponseTimeData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,Double> apiCumulativeServiceTimeMap = new HashMap<String,Double>();
        Map<String,Long> apiUsageMap = new TreeMap<String,Long>();
        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.apiName) &&
                        providerAPI.getId().getVersion().equals(responseTime.apiVersion) &&
                        providerAPI.getContext().equals(responseTime.context)) {
                    Double cumulativeResponseTime = apiCumulativeServiceTimeMap.get(responseTime.apiName);
                    String apiName = responseTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    if (cumulativeResponseTime != null) {
                        apiCumulativeServiceTimeMap.put(apiName,
                                cumulativeResponseTime + responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(apiName,
                                apiUsageMap.get(apiName) + responseTime.responseCount);
                    } else {
                        apiCumulativeServiceTimeMap.put(apiName,
                                responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(apiName, responseTime.responseCount);
                    }
                }
            }
        }
        
        List<APIResponseTimeDTO> result = new ArrayList<APIResponseTimeDTO>();
        DecimalFormat format = new DecimalFormat("#.##");
        for (String key : apiUsageMap.keySet()) {
            APIResponseTimeDTO responseTimeDTO = new APIResponseTimeDTO();
            responseTimeDTO.setApiName(key);
            double responseTime = apiCumulativeServiceTimeMap.get(key)/apiUsageMap.get(key);
            responseTimeDTO.setServiceTime(Double.parseDouble(format.format(responseTime)));
            result.add(responseTimeDTO);
        }
        return result;
    }

    /**
     * Returns a list of APIVersionLastAccessTimeDTO objects for all the APIs belonging to the
     * specified provider. Last access times are calculated without taking API versions into
     * account. That is all the versions of an API are treated as one.
     *
     * @param providerName Name of the API provider
     * @return a list of APIVersionLastAccessTimeDTO objects, possibly empty
     * @throws APIMgtUsageQueryServiceClientException on error
     */
    public List<APIVersionLastAccessTimeDTO> getLastAccessTimesByAPI(String providerName)
            throws APIMgtUsageQueryServiceClientException {

        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIAccessTime> accessTimes = getAccessTimeData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,APIAccessTime> lastAccessTimes = new TreeMap<String,APIAccessTime>();
        for (APIAccessTime accessTime : accessTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(accessTime.apiName) &&
                        providerAPI.getId().getVersion().equals(accessTime.apiVersion) &&
                        providerAPI.getContext().equals(accessTime.context)) {

                    String apiName = accessTime.apiName + " (" + providerAPI.getId().getProviderName() + ")";
                    APIAccessTime lastAccessTime = lastAccessTimes.get(accessTime.apiName);
                    if (lastAccessTime == null || lastAccessTime.accessTime < accessTime.accessTime) {
                        lastAccessTimes.put(apiName, accessTime);
                    }
                }
            }
        }

        List<APIVersionLastAccessTimeDTO> accessTimeDTOs = new ArrayList<APIVersionLastAccessTimeDTO>();
        DateFormat dateFormat = new SimpleDateFormat();
        for (Map.Entry<String,APIAccessTime> entry : lastAccessTimes.entrySet()) {
            APIVersionLastAccessTimeDTO accessTimeDTO = new APIVersionLastAccessTimeDTO();
            accessTimeDTO.setApiName(entry.getKey());
            APIAccessTime lastAccessTime = entry.getValue();
            accessTimeDTO.setApiVersion(lastAccessTime.apiVersion);
            accessTimeDTO.setLastAccessTime(dateFormat.format(lastAccessTime.accessTime));
            accessTimeDTO.setUser(lastAccessTime.username);
            accessTimeDTOs.add(accessTimeDTO);
        }
        return accessTimeDTOs;
    }

    /**
     * Returns a sorted list of PerUserAPIUsageDTO objects related to a particular API. The returned
     * list will only have at most limit + 1 entries. This method does not differentiate between
     * API versions.
     *
     * @param providerName API provider name
     * @param apiName Name of the API
     * @param limit Number of sorted entries to return
     * @return a List of PerUserAPIUsageDTO objects - Possibly empty
     * @throws APIMgtUsageQueryServiceClientException on error
     */
    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, int limit)
            throws APIMgtUsageQueryServiceClientException {

        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIUsageByUser> usageData = getUsageBySubscriber(omElement);
        Map<String,PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.context) &&
                        api.getId().getApiName().equals(apiName)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.username);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.requestCount);
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.username);
                        usageDTO.setCount(usageEntry.requestCount);
                        usageByUsername.put(usageEntry.username, usageDTO);
                    }
                    break;
                }
            }                        
        }
        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }

    public List<PerUserAPIUsageDTO> getUsageBySubscribers(String providerName, String apiName, 
                                                          String apiVersion, int limit) throws APIMgtUsageQueryServiceClientException {

        OMElement omElement = this.queryColumnFamily(
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY_TABLE,
                APIUsageStatisticsClientConstants.KEY_USAGE_SUMMARY_TABLE_INDEX,
                null);

        Collection<APIUsageByUser> usageData = getUsageBySubscriber(omElement);
        Map<String,PerUserAPIUsageDTO> usageByUsername = new TreeMap<String, PerUserAPIUsageDTO>();
        List<API> apiList = getAPIsByProvider(providerName);
        for (APIUsageByUser usageEntry : usageData) {
            for (API api : apiList) {
                if (api.getContext().equals(usageEntry.context) &&
                        api.getId().getApiName().equals(apiName) &&
                        api.getId().getVersion().equals(apiVersion) &&
                        apiVersion.equals(usageEntry.apiVersion)) {
                    PerUserAPIUsageDTO usageDTO = usageByUsername.get(usageEntry.username);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usageEntry.requestCount);
                    } else {
                        usageDTO = new PerUserAPIUsageDTO();
                        usageDTO.setUsername(usageEntry.username);
                        usageDTO.setCount(usageEntry.requestCount);
                        usageByUsername.put(usageEntry.username, usageDTO);
                    }
                    break;
                }
            }
        }
        
        return getTopEntries(new ArrayList<PerUserAPIUsageDTO>(usageByUsername.values()), limit);
    }
    
    private List<PerUserAPIUsageDTO> getTopEntries(List<PerUserAPIUsageDTO> usageData, int limit) {
        Collections.sort(usageData, new Comparator<PerUserAPIUsageDTO>() {
            public int compare(PerUserAPIUsageDTO o1, PerUserAPIUsageDTO o2) {
                // Note that o2 appears before o1
                // This is because we need to sort in the descending order
                return (int) (o2.getCount() - o1.getCount());
            }
        });
        if (usageData.size() > limit) {
            PerUserAPIUsageDTO other = new PerUserAPIUsageDTO();
            other.setUsername("[Other]");
            for (int i = limit; i < usageData.size(); i++) {
                other.setCount(other.getCount() + usageData.get(i).getCount());
            }
            for (int i = limit; i < usageData.size(); i++) {
                usageData.remove(i);
            }
            usageData.add(other);
        }
        return usageData;
    }

    private String getNextStringInLexicalOrder(String str) {
        if ((str == null) || (str.equals(""))) {
            return str;
        }
        byte[] bytes = str.getBytes();
        byte last = bytes[bytes.length - 1];
        last = (byte) (last + 1);        // Not very accurate. Need to improve this more to handle overflows.
        bytes[bytes.length - 1] = last;
        return new String(bytes);
    }

    private OMElement queryColumnFamily(String columnFamily, String index,
                                        QueryServiceStub.CompositeIndex[] compositeIndex) throws APIMgtUsageQueryServiceClientException{
        try {
            return qss.queryColumnFamily(columnFamily,index,compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while querying BAM server " +
                    "column family: " + columnFamily, e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while querying BAM server " +
                    "column family: " + columnFamily, e);
        }
    }

    private List<API> getAPIsByProvider(String providerId) throws APIMgtUsageQueryServiceClientException{
        try {
            if (APIUsageStatisticsClientConstants.ALL_PROVIDERS.equals(providerId)) {
                return apiProviderImpl.getAllAPIs();
            } else {
                return apiProviderImpl.getAPIsByProvider(providerId);
            }
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving APIs by " + providerId, e);
        }
    }

    private Collection<APIUsage> getUsageData(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        List<APIUsage> usageData = new ArrayList<APIUsage>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            usageData.add(new APIUsage(rowElement));
        }
        return usageData;
    }

    private Collection<APIResponseTime> getResponseTimeData(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        List<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            responseTimeData.add(new APIResponseTime(rowElement));
        }
        return responseTimeData;
    }

    private Collection<APIAccessTime> getAccessTimeData(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        List<APIAccessTime> accessTimeData = new ArrayList<APIAccessTime>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            accessTimeData.add(new APIAccessTime(rowElement));
        }
        return accessTimeData;
    }
    
    private Collection<APIUsageByUser> getUsageBySubscriber(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIUsageStatisticsClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIUsageStatisticsClientConstants.ROW));
        List<APIUsageByUser> usageData = new ArrayList<APIUsageByUser>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            usageData.add(new APIUsageByUser(rowElement));
        }
        return usageData;
    }
    
    private static class APIUsage {

        private String apiName;
        private String apiVersion;
        private String context;
        private long requestCount;

        public APIUsage(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
        }
    }
    
    private static class APIUsageByUser {
        
        private String context;
        private String username;
        private long requestCount;
        private String apiVersion;

        public APIUsageByUser(OMElement row) {
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST)).getText());
            apiVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.VERSION)).getText();
        }
    }

    private static class APIResponseTime {

        private String apiName;
        private String apiVersion;
        private String context;
        private double responseTime;
        private long responseCount;

        public APIResponseTime(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            responseTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.SERVICE_TIME)).getText());
            responseCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.RESPONSE)).getText());
        }
    }
    
    private static class APIAccessTime {
        
        private String apiName;
        private String apiVersion;
        private String context;
        private double accessTime;
        private String username; 

        public APIAccessTime(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            context = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.CONTEXT)).getText();
            accessTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.REQUEST_TIME)).getText());
            username = row.getFirstChildWithName(new QName(
                    APIUsageStatisticsClientConstants.USER_ID)).getText();
        }
    }

}