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
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class APIUsageStatisticsClient {

    private QueryServiceStub qss;

    private APIProvider apiProviderImpl;
    private APIConsumer apiConsumerImpl;

    public APIUsageStatisticsClient() throws APIMgtUsageQueryServiceClientException {
        APIManagerConfiguration config = APIUsageClientServiceComponent.getAPIManagerConfiguration();
        String targetEndpoint = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL);
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            throw new APIMgtUsageQueryServiceClientException("Required BAM server URL parameter unspecified");
        }

        try {
            qss = new QueryServiceStub(targetEndpoint + "services/QueryService");
            apiProviderImpl = APIManagerFactory.getInstance().getAPIProvider();
            apiConsumerImpl = APIManagerFactory.getInstance().getAPIConsumer();
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
                APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE,
                APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,APIUsageDTO> usageByAPIs = new HashMap<String, APIUsageDTO>();
        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion)) {
                    APIUsageDTO usageDTO = usageByAPIs.get(usage.apiName);
                    if (usageDTO != null) {
                        usageDTO.setCount(usageDTO.getCount() + usage.requestCount);
                    } else {
                        usageDTO = new APIUsageDTO();
                        usageDTO.setApiName(usage.apiName);
                        usageDTO.setCount(usage.requestCount);
                        usageByAPIs.put(usage.apiName, usageDTO);
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
                APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE, 
                APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX, 
                compositeIndex);
        Collection<APIUsage> usageData = getUsageData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,APIVersionUsageDTO> usageByVersions = new HashMap<String, APIVersionUsageDTO>();

        for (APIUsage usage : usageData) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(usage.apiName) &&
                        providerAPI.getId().getVersion().equals(usage.apiVersion)) {

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
                APIMgtUsageQueryServiceClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE,
                APIMgtUsageQueryServiceClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE_INDEX,
                null);
        Collection<APIResponseTime> responseTimes = getResponseTimeData(omElement);
        List<API> providerAPIs = getAPIsByProvider(providerName);
        Map<String,Double> apiCumulativeServiceTimeMap = new HashMap<String,Double>();
        Map<String,Long> apiUsageMap = new HashMap<String,Long>();
        for (APIResponseTime responseTime : responseTimes) {
            for (API providerAPI : providerAPIs) {
                if (providerAPI.getId().getApiName().equals(responseTime.apiName) &&
                        providerAPI.getId().getVersion().equals(responseTime.apiVersion)) {
                    Double cumulativeResponseTime = apiCumulativeServiceTimeMap.get(responseTime.apiName);
                    if (cumulativeResponseTime != null) {
                        apiCumulativeServiceTimeMap.put(responseTime.apiName,
                                cumulativeResponseTime + responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(responseTime.apiName,
                                apiUsageMap.get(responseTime.apiName) + responseTime.responseCount);
                    } else {
                        apiCumulativeServiceTimeMap.put(responseTime.apiName,
                                responseTime.responseTime * responseTime.responseCount);
                        apiUsageMap.put(responseTime.apiName, responseTime.responseCount);
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
     * This method can be used to get total request count for each combination of API version and subscriber for provider.
     * @return  List<ProviderAPIVersionUserUsageDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIVersionUserUsageDTO> getProviderAPIVersionUserUsage(String providerName, String apiName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionUserUsageDTO> result = new ArrayList<ProviderAPIVersionUserUsageDTO>();
        OMElement omElement = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_KEY_USAGE_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_KEY_USAGE_SUMMARY_TABLE_INDEX, compositeIndex);
        Set<String> versions = this.getAPIVersions(providerName, apiName);
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        for (String version : versions) {
            Set<Subscriber> subscribers = this.getSubscribersOfAPI(providerName,apiName,version);
            Map<String,Subscriber> subscriberMap = new HashMap<String, Subscriber>();
            // Make sure the subscribers passed down from here are unique by name
            for (Subscriber subscriber : subscribers) {
                subscriberMap.put(subscriber.getName(), subscriber);
            }
            for (Subscriber subscriber : subscriberMap.values()) {
                subscribedAPIs.addAll(this.getSubscribedIdentifiers(subscriber, providerName, apiName, version));
            }
        }

        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            OMElement rowsElement = omElement.getFirstChildWithName(
                    new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(
                    new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while (rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if (row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).
                        getText().equals(subscribedAPI.getApiId().getVersion()) &&
                        row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.CONSUMER_KEY)).getText().equals(getProductionKey(subscribedAPI))){
                    result.add(new ProviderAPIVersionUserUsageDTO(subscribedAPI.getApiId().getVersion(),
                            subscribedAPI.getSubscriber().getName(), String.valueOf(
                            (Float.valueOf(row.getFirstChildWithName(new QName(
                                    APIMgtUsageQueryServiceClientConstants.REQUEST)).getText())).intValue())));
                    break;
                }
            }
        }
        return result;
    }

    private String getProductionKey(SubscribedAPI api) {
        // TODO: Remove this when the UI is improved to handle sand boxing (Hiranya)
        List<APIKey> apiKeys = api.getKeys();
        for (APIKey key : apiKeys) {
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(key.getType())) {
                return key.getKey();
            }
        }
        return null;
    }

    /**
     * This method can be used to get total request count by subscribers for a single API provided by a particular provider.
     * @return  List<ProviderAPIUserUsageDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIUserUsageDTO> getProviderAPIUserUsage(String providerName, String apiName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIUserUsageDTO> result = new ArrayList<ProviderAPIUserUsageDTO>();
        OMElement omElement = null;
        omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.KEY_USAGE_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.KEY_USAGE_SUMMARY_TABLE_INDEX, null);
        Set<String> versions = this.getAPIVersions(providerName,apiName);
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        for(String version:versions){
            Set<Subscriber> subscribers = this.getSubscribersOfAPI(providerName,apiName,version);
            Map<String,Subscriber> subscriberMap = new HashMap<String, Subscriber>();
            // Make sure the subscribers passed down from here are unique by name
            for (Subscriber subscriber : subscribers) {
                subscriberMap.put(subscriber.getName(), subscriber);
            }
            for(Subscriber subscriber:subscriberMap.values()){
                subscribedAPIs.addAll(this.getSubscribedIdentifiers(subscriber, providerName, apiName, version));
            }
        }
        Map<String,Float> userUsageMap = new HashMap<String,Float>();
        for(SubscribedAPI subscribedAPI:subscribedAPIs){
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.CONSUMER_KEY)).getText().equals(getProductionKey(subscribedAPI))){
                    String userId = subscribedAPI.getSubscriber().getName();
                    if(userUsageMap.containsKey(userId)){
                        userUsageMap.put(userId,userUsageMap.get(userId)+Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                    }else{
                        userUsageMap.put(userId,Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                    }
                }
            }
        }
        Set<String> keys = userUsageMap.keySet();
        for(String key:keys){
            int count = userUsageMap.get(key).intValue();
            result.add(new ProviderAPIUserUsageDTO(key,String.valueOf(count)));
        }
        return result;
    }    

    /**
     * This method can be used to get last access time for each combination of API version and subscriber for particular provider.
     * @return List<ProviderAPIVersionUserLastAccessDTO>.
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIVersionUserLastAccessDTO> getProviderAPIVersionUserLastAccess(String providerName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionUserLastAccessDTO> result = new ArrayList<ProviderAPIVersionUserLastAccessDTO>();
        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_KEY_LAST_ACCESS_SUMMARY_TABLE_INDEX, null);
        List<API> apis = this.getAPIsByProvider(providerName);
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        for(API api:apis){
            String apiName = api.getId().getApiName();
            Set<String> versions = this.getAPIVersions(providerName,apiName);
            for(String version:versions){
                Set<Subscriber> subscribers = this.getSubscribersOfAPI(providerName,apiName,version);
                // Make sure the subscribers passed down from here are unique by name
                Map<String,Subscriber> subscriberMap = new HashMap<String, Subscriber>();
                for (Subscriber subscriber : subscribers) {
                    subscriberMap.put(subscriber.getName(), subscriber);
                }
                for(Subscriber subscriber:subscriberMap.values()){
                    subscribedAPIs.addAll(this.getSubscribedIdentifiers(subscriber,providerName,apiName,version));
                }
            }
        }
        
        Map<String,Double> lastAccessTimes = new HashMap<String, Double>();
        for (SubscribedAPI subscribedAPI:subscribedAPIs) {
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(oMElementIterator.hasNext()){
                OMElement row = (OMElement)oMElementIterator.next();
                String key = subscribedAPI.getApiId().getApiName() + ":" +
                        subscribedAPI.getApiId().getVersion() + ":" +
                        subscribedAPI.getSubscriber().getName();
                if (row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API_VERSION)).getText().equals(
                        subscribedAPI.getApiId().getApiName()+":v"+subscribedAPI.getApiId().getVersion()) &&
                        row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.CONSUMER_KEY)).getText().equals(
                                getProductionKey(subscribedAPI))) {
                    if (lastAccessTimes.containsKey(key)) {
                        Double date = lastAccessTimes.get(key);
                        Double current = Double.parseDouble(row.getFirstChildWithName(
                                new QName(APIMgtUsageQueryServiceClientConstants.REQUEST_TIME)).getText());
                        if (current > date) {
                            lastAccessTimes.put(key, current);
                        }
                    } else {
                        lastAccessTimes.put(key, Double.parseDouble(row.getFirstChildWithName(
                                new QName(APIMgtUsageQueryServiceClientConstants.REQUEST_TIME)).getText()));
                    }
                }
            }
        }
        
        for (Map.Entry<String,Double> accessTime : lastAccessTimes.entrySet()) {
            String[] keySegments = accessTime.getKey().split(":");
            result.add(new ProviderAPIVersionUserLastAccessDTO(keySegments[1],
                    keySegments[2],
                    new SimpleDateFormat().format(accessTime.getValue())));
        }
        return result;
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

    private Set<String> getAPIVersions(String providerId,String apiName) throws APIMgtUsageQueryServiceClientException{
        Set<String> temp_result = null;
        Set<String> return_result = new HashSet<String>();
        try {
            temp_result = apiProviderImpl.getAPIVersions(providerId, apiName);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving versions for "+providerId+"-"+apiName+" combination", e);
        }
        for(String version:temp_result){
            return_result.add(version.substring(1));
        }
        return return_result;
    }

    private Set<Subscriber> getSubscribersOfAPI(String providerId,String apiName, String version) throws APIMgtUsageQueryServiceClientException{
        Set<Subscriber> subscribers = null;
        try {
            subscribers = apiProviderImpl.getSubscribersOfAPI(new APIIdentifier(providerId,apiName,version));
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while getting subscribers for "+providerId+"-"+apiName+"-"+version+" combination", e);
        }
        return subscribers;
    }

    private List<API> getAPIsByProvider(String providerId) throws APIMgtUsageQueryServiceClientException{
        try {
            return apiProviderImpl.getAPIsByProvider(providerId);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving APIs by " + providerId, e);
        }
    }

    private Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber, String providerName,String apiName, String version) throws APIMgtUsageQueryServiceClientException{
        Set<SubscribedAPI> subscribedAPIs = null;
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        try {
            subscribedAPIs = apiConsumerImpl.getSubscribedIdentifiers(subscriber, apiIdentifier);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while getting subscribedAPIs for "+subscriber.getName()+"-"+providerName+"-"+apiName+"-"+version+" combination", e);
        }
        return subscribedAPIs;
    }

    private Collection<APIUsage> getUsageData(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIMgtUsageQueryServiceClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIMgtUsageQueryServiceClientConstants.ROW));
        List<APIUsage> usageData = new ArrayList<APIUsage>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            usageData.add(new APIUsage(rowElement));
        }
        return usageData;
    }

    private Collection<APIResponseTime> getResponseTimeData(OMElement data) {
        OMElement rowsElement = data.getFirstChildWithName(new QName(
                APIMgtUsageQueryServiceClientConstants.ROWS));
        Iterator rowIterator = rowsElement.getChildrenWithName(new QName(
                APIMgtUsageQueryServiceClientConstants.ROW));
        List<APIResponseTime> responseTimeData = new ArrayList<APIResponseTime>();
        while (rowIterator.hasNext()) {
            OMElement rowElement = (OMElement) rowIterator.next();
            responseTimeData.add(new APIResponseTime(rowElement));
        }
        return responseTimeData;
    }
    
    private static class APIUsage {

        private String apiName;
        private String apiVersion;
        private long requestCount;

        public APIUsage(OMElement row) {
            apiName = row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.API)).getText();
            apiVersion = row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.VERSION)).getText();
            requestCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.REQUEST)).getText());
        }
    }

    private static class APIResponseTime {

        private String apiName;
        private String apiVersion;
        private double responseTime;
        private long responseCount;

        public APIResponseTime(OMElement row) {
            String nameVersion = row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.API_VERSION)).getText();
            int index = nameVersion.lastIndexOf(":v");
            apiName = nameVersion.substring(0, index);
            apiVersion = nameVersion.substring(index + 2);
            responseTime = Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.SERVICE_TIME)).getText());
            responseCount = (long) Double.parseDouble(row.getFirstChildWithName(new QName(
                    APIMgtUsageQueryServiceClientConstants.RESPONSE)).getText());
        }
    }

}