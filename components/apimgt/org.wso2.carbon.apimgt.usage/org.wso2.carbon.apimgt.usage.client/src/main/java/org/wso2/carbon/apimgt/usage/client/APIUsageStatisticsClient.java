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
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIServiceTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUserLastAccessDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.internal.APIUsageClientServiceComponent;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStoreException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
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
     * This method can be used to get total request count for each API version by provider.
     * @return  List<ProviderAPIVersionUsageDTO> a list of usage information beans
     * @throws APIMgtUsageQueryServiceClientException on error
     */
    public List<ProviderAPIVersionUsageDTO> getProviderAPIVersionUsage(String providerName, String apiName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionUsageDTO> result = new ArrayList<ProviderAPIVersionUsageDTO>();
        OMElement omElement;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX, compositeIndex);
        Set<String> versions = this.getAPIVersions(providerName, apiName);
        for(String version:versions){
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(version)){
                    result.add(new ProviderAPIVersionUsageDTO(version,String.valueOf((Float.valueOf(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText())).intValue())));
                    break;
                }
            }

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
     * This method can be used to get total request count for each API by provider.
     * @return  List<ProviderAPIUsageDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIUsageDTO> getProviderAPIUsage(String providerName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIUsageDTO> result = new ArrayList<ProviderAPIUsageDTO>();
        List<API> apis = this.getAPIsByProvider(providerName);
        Set<APIIdentifier> apiIdentifiers = new HashSet<APIIdentifier>();
        for(API api:apis){
            Set<String> versions = this.getAPIVersions(providerName,api.getId().getApiName());
            for(String version:versions){
                apiIdentifiers.add(new APIIdentifier(providerName, api.getId().getApiName(), version));
            }
        }
        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_USAGE_SUMMARY_TABLE_INDEX, null);
        Map<String,Float> map = new HashMap<String,Float>();
        for (APIIdentifier apiIdentifier:apiIdentifiers){
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API)).getText().equals(apiIdentifier.getApiName()) && row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(apiIdentifier.getVersion())){
                    if(map.containsKey(apiIdentifier.getApiName())){
                        map.put(apiIdentifier.getApiName(),map.get(apiIdentifier.getApiName()) + Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                    }else{
                        map.put(apiIdentifier.getApiName(),Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                    }
                    break;
                }
            }
        }
        Set<String> keys = map.keySet();
        for(String key:keys){
            result.add(new ProviderAPIUsageDTO(key,String.valueOf(map.get(key).intValue())));
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

    /**
     * This method can be used to get average service time for each API by provider.
     * @return List<ProviderAPIServiceTimeDTO>.
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIServiceTimeDTO> getProviderAPIServiceTime(String providerName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIServiceTimeDTO> result = new ArrayList<ProviderAPIServiceTimeDTO>();
        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_SERVICE_TIME_SUMMARY_TABLE_INDEX, null);
        List<API> apis = this.getAPIsByProvider(providerName);
        Set<APIIdentifier> apiIdentifiers = new HashSet<APIIdentifier>();
        for(API api:apis){
        Set<String> versions = this.getAPIVersions(providerName,api.getId().getApiName());
            for(String version:versions){
                apiIdentifiers.add(new APIIdentifier(providerName, api.getId().getApiName(), version));
            }
        }
        List<String[]> calculationList = new ArrayList<String[]>();
        for (APIIdentifier apiIdentifier:apiIdentifiers) {
            String[] api_serviceTime_usage = new String[3];
            api_serviceTime_usage[0] = apiIdentifier.getApiName();
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API_VERSION)).getText().equals(apiIdentifier.getApiName()+":v"+apiIdentifier.getVersion())){
                    api_serviceTime_usage[1] = (row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.SERVICE_TIME))).getText();
                    api_serviceTime_usage[2] = (row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.RESPONSE))).getText();
                    calculationList.add(api_serviceTime_usage);
                }
            }

        }
        Map<String,Float> apiCumulativeServiceTimeMap = new HashMap<String,Float>();
        Map<String,Integer> apiUsageMap = new HashMap<String,Integer>();
        for(String[] item:calculationList){
            if(apiCumulativeServiceTimeMap.containsKey(item[0])){
                apiCumulativeServiceTimeMap.put(item[0],apiUsageMap.get(item[0])+Float.valueOf(item[1]) * Float.valueOf(item[2]));
            }else{
                apiCumulativeServiceTimeMap.put(item[0],Float.valueOf(item[1]) * Float.valueOf(item[2]));
            }
            if(apiUsageMap.containsKey(item[0])){
                apiUsageMap.put(item[0],Float.valueOf(apiUsageMap.get(item[0])+Float.parseFloat(item[2])).intValue());
            }else{
                apiUsageMap.put(item[0],Float.valueOf(item[2]).intValue());
            }
        }
        Set<String> keys = apiUsageMap.keySet();
        for(String key:keys){
            result.add(new ProviderAPIServiceTimeDTO(key,String.valueOf(apiCumulativeServiceTimeMap.get(key)/apiUsageMap.get(key))));
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

    private OMElement queryColumnFamily(String columnFamily,String index,QueryServiceStub.CompositeIndex[] compositeIndex) throws APIMgtUsageQueryServiceClientException{
        OMElement result = null;
        try{
            result = qss.queryColumnFamily(columnFamily,index,compositeIndex);
        }catch(RemoteException e){
            throw new APIMgtUsageQueryServiceClientException("Error while querying BAM server column family: "+columnFamily, e);
        }catch(QueryServiceStoreException e){
            throw new APIMgtUsageQueryServiceClientException("Error while querying BAM server column family: "+columnFamily, e);
        }
        return result;
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
        List<API> apis;
        try {
            apis = apiProviderImpl.getAPIsByProvider(providerId);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Error while retrieving APIs by "+providerId, e);
        }
        return apis;
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

}
