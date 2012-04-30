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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerImpl;
import org.wso2.carbon.apimgt.impl.APIProviderImpl;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIServiceTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionLastAccessDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceConfigurationException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceIndexingException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceStub;
import org.wso2.carbon.bam.index.stub.service.types.IndexDTO;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStoreException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageQueryServiceClient {

    private QueryServiceStub qss;

    private APIManagerImpl apiManagerImpl;
    private APIProviderImpl apiProviderImpl;

    private IndexAdminServiceStub indexAdminStub;

    public APIMgtUsageQueryServiceClient(String targetEndpoint) throws APIMgtUsageQueryServiceClientException {
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            targetEndpoint = "https://localhost:9444/";
        }
        String queryServiceEndpoint = targetEndpoint + "services/QueryService";
        String indexAdminServiceEndpoint = targetEndpoint + "services/IndexAdminService";
        try {
            qss = new QueryServiceStub(queryServiceEndpoint);
            indexAdminStub = new IndexAdminServiceStub(indexAdminServiceEndpoint);
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating QueryServiceStub", e);
        }
        try{
            apiManagerImpl = new APIManagerImpl("admin","admin","https://localhost:9443/");
            apiProviderImpl = new APIProviderImpl();
        }
        catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating APIManagerImpl", e);
        }
    }

    /**
     * This method can be used to get total request count for each API version by provider.
     * @return  List<ProviderAPIVersionUsageDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
//    public List<ProviderAPIVersionUsageDTO> getProviderAPIVersionUsage(String providerName, String apiName) throws APIMgtUsageQueryServiceClientException {
//        List<ProviderAPIVersionUsageDTO> result = new ArrayList<ProviderAPIVersionUsageDTO>();
//        OMElement omElement = null;
//        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
//        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
//        compositeIndex[0].setIndexName("api");
//        compositeIndex[0].setRangeFirst(apiName);
//        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
//        omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX, compositeIndex);
//        Set<String> versions = this.getAPIVersions(providerName, apiName);
//        for(String version:versions){
//            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
//            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
//            while(rowIterator.hasNext()){
//                OMElement row = (OMElement)rowIterator.next();
//                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(version)){
//                    result.add(new ProviderAPIVersionUsageDTO(version,row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
//                    break;
//                }
//            }
//
//        }
//        return result;
//    }

    // Incorrect method to get usage count for for multiple versions of APIs provided by a particular provider.
    public List<ProviderAPIVersionUsageDTO> getProviderAPIVersionUsage(String api) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionUsageDTO> result = new ArrayList<ProviderAPIVersionUsageDTO>();
        OMElement omElement = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(api);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(api));
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        Pattern pattern = Pattern.compile("(<row>.*?</row>)");
        Matcher matcher = pattern.matcher(omElementString);
        while(matcher.find()) {
            temp = matcher.group(1);
            Pattern pattern1 = Pattern.compile("<version>(.*?)</version>");
	        Matcher matcher1 = pattern1.matcher(temp);
            Pattern pattern2 = Pattern.compile("<request>(.*?)</request>");
            Matcher matcher2 = pattern2.matcher(temp);
            if(matcher1.find() && matcher2.find()){
                result.add(new ProviderAPIVersionUsageDTO(matcher1.group(1),matcher2.group(1)));
            }
       }
        return result;
    }

    /**
     * This method can be used to get total request count by subscribers for a single API provided by a particular provider.
     * @return  List<ProviderAPIUserUsageDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIUserUsageDTO> getProviderAPIUserUsage(String providerName, String apiName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIUserUsageDTO> result = new ArrayList<ProviderAPIUserUsageDTO>();
        OMElement omElement = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(apiName);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));
        omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_USER_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_USER_SUMMARY_TABLE_INDEX, compositeIndex);
        Set<String> versions = this.getAPIVersions(providerName,apiName);
        Set<APIIdentifier> apiIdentifiers = null;
        for(String version:versions){
            apiIdentifiers.add(new APIIdentifier(providerName, apiName, version));
        }
        Map<String,Float> map = new HashMap<String,Float>();
        for(APIIdentifier apiIdentifier:apiIdentifiers){
            Set<Subscriber> subscribers = this.getSubscribersOfAPI(providerName,apiName,apiIdentifier.getVersion());
            for(Subscriber subscriber:subscribers){
                OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
                Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
                while(rowIterator.hasNext()){
                    OMElement row = (OMElement)rowIterator.next();
                    if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(apiIdentifier.getVersion()) && row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.USER)).getText().equals(subscriber.getName())){
                        if(map.containsKey(subscriber.getName())){
                            map.put(subscriber.getName(),map.get(subscriber.getName())+Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                        }else{
                            map.put(subscriber.getName(),Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
                        }
                        break;
                    }
                }
            }
        }
        Set<String> keys = map.keySet();
        for(String key:keys){
            result.add(new ProviderAPIUserUsageDTO(key,map.get(key).toString()));
        }
        return result;
    }

    /**
     * This method can be used to get total request count for each API by provider.
     * @return  List<ProviderAPIDTO>
     * @throws APIMgtUsageQueryServiceClientException
     */
//    public List<ProviderAPIUsageDTO> getProviderAPIUsage(String providerName) throws APIMgtUsageQueryServiceClientException {
//        List<ProviderAPIUsageDTO> result = new ArrayList<ProviderAPIUsageDTO>();
//        List<API> apis = this.getAPIsByProvider(providerName);
//        Set<APIIdentifier> apiIdentifiers = new HashSet<APIIdentifier>();
//        for(API api:apis){
//            Set<String> versions = this.getAPIVersions(providerName,api.getId().getApiName());
//            for(String version:versions){
//                apiIdentifiers.add(new APIIdentifier(providerName, api.getId().getApiName(), version));
//            }
//        }
//        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX, null);
//        Map<String,Float> map = new HashMap<String,Float>();
//        for (APIIdentifier apiIdentifier:apiIdentifiers){
//            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
//            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
//            while(rowIterator.hasNext()){
//                OMElement row = (OMElement)rowIterator.next();
//                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API)).getText().equals(apiIdentifier.getApiName()) && row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(apiIdentifier.getVersion())){
//                    if(map.containsKey(apiIdentifier.getApiName())){
//                        map.put(apiIdentifier.getApiName(),map.get(apiIdentifier.getApiName()) + Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
//                    }else{
//                        map.put(apiIdentifier.getApiName(),Float.parseFloat(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText()));
//                    }
//                    break;
//                }
//            }
//        }
//        Set<String> keys = map.keySet();
//        for(String key:keys){
//            result.add(new ProviderAPIUsageDTO(key,map.get(key).toString()));
//        }
//        return result;
//    }

    /**
     * Incorrect method to get all API names and total request count for each API.
     * @return  all API names and total request count for each API.
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIUsageDTO> getProviderAPIUsage() throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIUsageDTO> result = new ArrayList<ProviderAPIUsageDTO>();
        OMElement omElement = null;
        String[] apiNames = getAPIList();
        if(apiNames != null){
            for (String apiName:apiNames){
                QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
                compositeIndex[0] = new QueryServiceStub.CompositeIndex();
                compositeIndex[0].setIndexName("api");
                compositeIndex[0].setRangeFirst(apiName);
                compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));

                try {
                    omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX, compositeIndex);
                } catch (RemoteException e) {
                    throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
                } catch (QueryServiceStoreException e) {
                     throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
                }
                OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
                Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
                float requestCount = 0;
                while (oMElementIterator.hasNext()) {
                    OMElement element = (OMElement) oMElementIterator.next();
                    requestCount += Float.parseFloat(element.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST)).getText());
                }
                result.add(new ProviderAPIUsageDTO(apiName,((Float)requestCount).toString()));
            }
        }
        return result;
    }

    /**
     * This method can be used to get last access time for each API versions by particular provider.
     * @return List<ProviderAPIVersionLastAccessDTO>.
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIVersionLastAccessDTO> getProviderAPIVersionLastAccess(String providerName) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionLastAccessDTO> result = new ArrayList<ProviderAPIVersionLastAccessDTO>();
        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_VERSION_LAST_ACCESS_TABLE, APIMgtUsageQueryServiceClientConstants.API_VERSION_LAST_ACCESS_TABLE_INDEX, null);
        List<API> apis = this.getAPIsByProvider(providerName);
        Set<APIIdentifier> apiIdentifiers = new HashSet<APIIdentifier>();
        for(API api:apis){
        Set<String> versions = this.getAPIVersions(providerName,api.getId().getApiName());
            for(String version:versions){
                apiIdentifiers.add(new APIIdentifier(providerName, api.getId().getApiName(), version));
            }
        }
        for (APIIdentifier apiIdentifier:apiIdentifiers) {
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(oMElementIterator.hasNext()){
                OMElement row = (OMElement)oMElementIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API)).getText().equals(apiIdentifier.getApiName()) && row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.VERSION)).getText().equals(apiIdentifier.getVersion())){
                    result.add(new ProviderAPIVersionLastAccessDTO(apiIdentifier.getApiName(),apiIdentifier.getVersion(), (new SimpleDateFormat()).format(Long.parseLong(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.REQUEST_TIME)).getText()))));
                    break;
                }
            }
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
        OMElement omElement = this.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.API_SERVICE_TIME_SUMMARY_TABLE, APIMgtUsageQueryServiceClientConstants.API_SERVICE_TIME_SUMMARY_TABLE_INDEX, null);
        List<API> apis = this.getAPIsByProvider(providerName);
        for (API api:apis) {
            OMElement rowsElement = omElement.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROWS));
            Iterator rowIterator = rowsElement.getChildrenWithName(new QName(APIMgtUsageQueryServiceClientConstants.ROW));
            while(rowIterator.hasNext()){
                OMElement row = (OMElement)rowIterator.next();
                if(row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.API)).equals(api.getId().getApiName())){
                    String serviceTime = (row.getFirstChildWithName(new QName(APIMgtUsageQueryServiceClientConstants.SERVICE_TIME))).getText();
                    result.add(new ProviderAPIServiceTimeDTO(api.getId().getApiName(), serviceTime));
                }
            }
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
            qss.queryColumnFamily(columnFamily,index,compositeIndex);
        }catch(RemoteException e){
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }catch(QueryServiceStoreException e){
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        return result;
    }

    private Set<String> getAPIVersions(String providerId,String apiName) throws APIMgtUsageQueryServiceClientException{
        Set<String> result = null;
        try {
            result = apiManagerImpl.getAPIVersions(providerId, apiName);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while retrieving versions for provider-api combination", e);
        }
        return result;
    }

    private Set<Subscriber> getSubscribersOfAPI(String providerId,String apiName, String version) throws APIMgtUsageQueryServiceClientException{
        Set<Subscriber> subscribers = null;
        try {
            subscribers = apiProviderImpl.getSubscribersOfAPI(new APIIdentifier(providerId,apiName,version));
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while getting subscribers for provider-api combination", e);
        }
        return subscribers;
    }

    private List<API> getAPIsByProvider(String providerId) throws APIMgtUsageQueryServiceClientException{
        List<API> apis;
        try {
            apis = apiProviderImpl.getAPIsByProvider(providerId);
        } catch (APIManagementException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while retrieving APIs by provider", e);
        }
        return apis;
    }

    /**
     * Incorrect method to get all API names
     * @return
     * @throws APIMgtUsageQueryServiceClientException
     */
    private String[] getAPIList() throws APIMgtUsageQueryServiceClientException {
        IndexDTO indexDTO = null;
        String[] apiNames = null;

        try {
            IndexDTO apiIndex = indexAdminStub.getIndex(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX);
            apiNames = indexAdminStub.getIndexValues(APIMgtUsageQueryServiceClientConstants.API_VERSION_SUMMARY_TABLE_INDEX,apiIndex.getIndexedColumns()[0]);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (IndexAdminServiceConfigurationException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (IndexAdminServiceIndexingException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        return apiNames;
    }

}
