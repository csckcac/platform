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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUsage;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceConfigurationException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceIndexingException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceStub;
import org.wso2.carbon.bam.index.stub.service.types.IndexDTO;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStoreException;
import org.wso2.carbon.bam.presentation.stub.QueryServiceStub;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageQueryServiceClient {

    public static final String ROWS = "rows";
    public static final String ROW = "row";
    public static final String REQUEST = "request";
    private QueryServiceStub qss;
    private IndexAdminServiceStub indexAdminStub;

    public APIMgtUsageQueryServiceClient(ConfigurationContext cc, String targetEndpoint,
                                         boolean useSeparateListener) throws
                                                                      APIMgtUsageQueryServiceClientException {
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            targetEndpoint = "https://localhost:9443/services/QueryService";
        }
        try {
            qss = new QueryServiceStub(cc, targetEndpoint, useSeparateListener);
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating org.wso2.carbon.apimgt.usage.client.APIMgtUsageQueryServiceClient", e);
        }
    }

    public APIMgtUsageQueryServiceClient(ConfigurationContext cc, String targetEndpoint) throws
                                                                                         APIMgtUsageQueryServiceClientException {
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            targetEndpoint = "https://localhost:9443/services/QueryService";
        }
        try {
            qss = new QueryServiceStub(cc, targetEndpoint);
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating org.wso2.carbon.apimgt.usage.client.APIMgtUsageQueryServiceClient", e);
        }
    }

    public APIMgtUsageQueryServiceClient(ConfigurationContext cc) throws
                                                                  APIMgtUsageQueryServiceClientException {
        try {
            qss = new QueryServiceStub(cc);
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating org.wso2.carbon.apimgt.usage.client.APIMgtUsageQueryServiceClient", e);
        }
    }

    public APIMgtUsageQueryServiceClient(String targetEndpoint) throws
                                                                APIMgtUsageQueryServiceClientException {
        if (targetEndpoint == null || targetEndpoint.equals("")) {
            targetEndpoint = "https://localhost:9444/";
        }
        String queryServiceEndpoint = targetEndpoint + "services/QueryService";
        String indexAdminServiceEndpoint = targetEndpoint + "services/IndexAdminService";
        try {
            qss = new QueryServiceStub(queryServiceEndpoint);
            indexAdminStub = new IndexAdminServiceStub(indexAdminServiceEndpoint);
        } catch (AxisFault e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while instantiating org.wso2.carbon.apimgt.usage.client.APIMgtUsageQueryServiceClient", e);
        }
    }

    // Get usage count for single API
    public String getAPIUsage(String api) throws APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(api);
        compositeIndex[0].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APISummaryTable, APIMgtUsageQueryServiceClientConstants.APISummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<request>(.*?)</request>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    // Get usage count for single API by time
    public String getAPIUsageByTime(String api, String start, String end) throws
                                                                          APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("requestTime");
        compositeIndex[0].setRangeFirst(start);
        compositeIndex[0].setRangeLast(end);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.BASEEnriched, APIMgtUsageQueryServiceClientConstants.BASEEnrichedIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        result = ((Integer) StringUtils.countMatches(omElementString, "<api>" + api + "</api>")).toString();
        return result;
    }

    // Get usage count for multiple APIs.
    // The count for each API is returned in the order of the APIs in the array passed.
    public String[] getAPIUsage(String[] api) throws APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APISummaryTable, APIMgtUsageQueryServiceClientConstants.APISummaryTableIndex, null);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<request>(.*?)</request>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
            }
        }
        return result;
    }

    // Get usage count for multiple APIs by time.
    // The count for each API is returned in the order of the APIs in the array passed.
    public String[] getAPIUsageByTime(String[] api, String start, String end) throws
                                                                              APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("requestTime");
        compositeIndex[0].setRangeFirst(start);
        compositeIndex[0].setRangeLast(end);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.BASEEnriched, APIMgtUsageQueryServiceClientConstants.BASEEnrichedIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        for (int i = 0; i < api.length; i++) {
            result[i] = ((Integer) StringUtils.countMatches(omElementString, "<api>" + api[i] + "</api>")).toString();
        }
        return result;
    }

    // Get usage count for single API for particular user
    public String getUserAPIUsage(String user, String api) throws
                                                           APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[2];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(user);
        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName("api");
        compositeIndex[1].setRangeFirst(api);
        compositeIndex[1].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPISummaryTable, APIMgtUsageQueryServiceClientConstants.UserAPISummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<request>(.*?)</request>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    // Get usage count for single API for particular user by time
    public String getUserAPIUsageByTime(String user, String api, String start, String end) throws
                                                                                           APIMgtUsageQueryServiceClientException {
        int result = 0;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("requestTime");
        compositeIndex[0].setRangeFirst(start);
        compositeIndex[0].setRangeLast(end);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.BASEEnriched, APIMgtUsageQueryServiceClientConstants.BASEEnrichedIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }

        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile(".*?(<row>.*?<user>" + user + "</user>.*?</row>)");
        Matcher matcher = pattern.matcher(omElementString);
        while (matcher.find()) {
            if (matcher.group(1).contains("<api>" + api + "</api>")) {
                result++;
            }
        }
        return ((Integer) result).toString();
    }

    // Get usage count for multiple APIs for particular user.
    // The count for each API is returned in the order of the APIs in the array passed.
    public String[] getUserAPIUsage(String user, String[] api) throws
                                                               APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(user));
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPISummaryTable, APIMgtUsageQueryServiceClientConstants.UserAPISummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<request>(.*?)</request>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
            }
        }
        return result;
    }

    // Get usage count for multiple APIs for particular user by time.
    // The count for each API is returned in the order of the APIs in the array passed.
    public String[] getUserAPIUsageByTime(String user, String[] api, String start, String end)
            throws
            APIMgtUsageQueryServiceClientException {
        int[] result = new int[api.length];
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("requestTime");
        compositeIndex[0].setRangeFirst(start);
        compositeIndex[0].setRangeLast(end);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.BASEEnriched, APIMgtUsageQueryServiceClientConstants.BASEEnrichedIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile(".*?(<row>.*?<user>" + user + "</user>.*?</row>)");
        Matcher matcher = pattern.matcher(omElementString);
        while (matcher.find()) {
            for (int i = 0; i < api.length; i++) {
                if (matcher.group(1).contains("<api>" + api[i] + "</api>")) {
                    result[i]++;
                }
            }
        }
        String[] return_result = new String[api.length];
        for (int i = 0; i < api.length; i++) {
            return_result[i] = ((Integer) result[i]).toString();
        }
        return return_result;
    }


    // Get last access time for single API
    public String getAPILastAccess(String api) throws APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(api);
        compositeIndex[0].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APILastAccessTable, APIMgtUsageQueryServiceClientConstants.APILastAccessTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<requestTime>(.*?)</requestTime>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }


    // Get last access time for multiple APIs.
    // The time for each API is returned in the order of the APIs in the array passed.
    public String[] getAPILastAccess(String[] api) throws APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APILastAccessTable, APIMgtUsageQueryServiceClientConstants.APILastAccessTableIndex, null);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<requestTime>(.*?)</requestTime>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
            }
        }
        return result;
    }


    // Get last access time for single API for particular user
    public String getUserAPILastAccess(String user, String api) throws
                                                                APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[2];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(user);
        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName("api");
        compositeIndex[1].setRangeFirst(api);
        compositeIndex[1].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPILastAccessTable, APIMgtUsageQueryServiceClientConstants.UserAPILastAccessTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<requestTime>(.*?)</requestTime>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    // Get last access time for multiple APIs.
    // The time for each API is returned in the order of the APIs in the array passed.
    public String[] getUserAPILastAccess(String user, String[] api) throws
                                                                    APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(user));
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPILastAccessTable, APIMgtUsageQueryServiceClientConstants.UserAPILastAccessTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<requestTime>(.*?)</requestTime>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
            }
        }
        return result;
    }


    // Get average service time for single API
    public String getAverageAPIServiceTime(String api) throws
                                                       APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(api);
        compositeIndex[0].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APIServiceTimeSummaryTable, APIMgtUsageQueryServiceClientConstants.APIServiceTimeSummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<serviceTime>(.*?)</serviceTime>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    // Get average service time for multiple APIs.
    // The time for each API is returned in the order of the APIs in the array passed.
    public String[] getAverageAPIServiceTime(String[] api) throws
                                                           APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APIServiceTimeSummaryTable, APIMgtUsageQueryServiceClientConstants.APIServiceTimeSummaryTableIndex, null);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<serviceTime>(.*?)</serviceTime>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
            }
        }
        return result;
    }

    // Get average service time for single API for particular user
    public String getAverageUserAPIServiceTime(String user, String api) throws
                                                                        APIMgtUsageQueryServiceClientException {
        String result = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[2];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(user);
        compositeIndex[1] = new QueryServiceStub.CompositeIndex();
        compositeIndex[1].setIndexName("api");
        compositeIndex[1].setRangeFirst(api);
        compositeIndex[1].setRangeLast(api);
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPIServiceTimeSummaryTable, APIMgtUsageQueryServiceClientConstants.UserAPIServiceTimeSummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        Pattern pattern = Pattern.compile("<serviceTime>(.*?)</serviceTime>");
        Matcher matcher = pattern.matcher(omElementString);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    // Get average service time for multiple APIs for particular user.
    // The time for each API is returned in the order of the APIs in the array passed.
    public String[] getAverageUserAPIServiceTime(String user, String[] api) throws
                                                                            APIMgtUsageQueryServiceClientException {
        String[] result = new String[api.length];
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("user");
        compositeIndex[0].setRangeFirst(user);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(user));
        OMElement omElement = null;
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.UserAPIServiceTimeSummaryTable, APIMgtUsageQueryServiceClientConstants.UserAPIServiceTimeSummaryTableIndex, compositeIndex);
        } catch (RemoteException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        } catch (QueryServiceStoreException e) {
            throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
        }
        String omElementString = omElement.toString();
        String temp = null;
        for (int i = 0; i < api.length; i++) {
            Pattern pattern = Pattern.compile(".*(<row>.*<api>" + api[i] + "</api>.*?</row>)");
            Matcher matcher = pattern.matcher(omElementString);
            if (matcher.find()) {
                temp = matcher.group(1);
            }
            Pattern pattern1 = Pattern.compile("<serviceTime>(.*?)</serviceTime>");
            Matcher matcher1 = pattern1.matcher(temp);
            if (matcher1.find()) {
                result[i] = matcher1.group(1);
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

// Get usage count for for multiple versions of APIs provided by a particular provider.
public List<ProviderAPIVersionDTO> getProviderAPIVersionsUsage(String api) throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIVersionDTO> result = new ArrayList<ProviderAPIVersionDTO>();
        OMElement omElement = null;
        QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
        compositeIndex[0] = new QueryServiceStub.CompositeIndex();
        compositeIndex[0].setIndexName("api");
        compositeIndex[0].setRangeFirst(api);
        compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(api));
        try {
            omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTable, APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTableIndex, compositeIndex);
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
            System.out.println(matcher.group(1));
            Pattern pattern1 = Pattern.compile("<version>(.*)</version>");
            Matcher matcher1 = pattern1.matcher(temp);
            Pattern pattern2 = Pattern.compile("<request>(.*)</request>");
            Matcher matcher2 = pattern2.matcher(temp);
            if(matcher1.find() && matcher2.find()){
                result.add(new ProviderAPIVersionDTO(matcher1.group(1),matcher2.group(1)));
            }
       }
        return result;
    }

    /**
     * This method can be used to get all API names and total request count for each API.
     * @return  all API names and total request count for each API.
     * @throws APIMgtUsageQueryServiceClientException
     */
    public List<ProviderAPIUsage> getProviderAPIUsage()
            throws APIMgtUsageQueryServiceClientException {
        List<ProviderAPIUsage> result = new ArrayList<ProviderAPIUsage>();
        OMElement omElement = null;
        String[] apiNames = getAPIList();

        for (String apiName:apiNames){
            QueryServiceStub.CompositeIndex[] compositeIndex = new QueryServiceStub.CompositeIndex[1];
            compositeIndex[0] = new QueryServiceStub.CompositeIndex();
            compositeIndex[0].setIndexName("api");
            compositeIndex[0].setRangeFirst(apiName);
            compositeIndex[0].setRangeLast(getNextStringInLexicalOrder(apiName));

            try {
                omElement = qss.queryColumnFamily(APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTable, APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTableIndex, compositeIndex);
            } catch (RemoteException e) {
                throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
            } catch (QueryServiceStoreException e) {
                throw new APIMgtUsageQueryServiceClientException("Exception while querying BAM server", e);
            }

            OMElement rowsElement = omElement.getFirstChildWithName(new QName(ROWS));
            Iterator oMElementIterator = rowsElement.getChildrenWithName(new QName(ROW));

            while (oMElementIterator.hasNext()) {
                OMElement element = (OMElement) oMElementIterator.next();
                String requestCount = element.getFirstChildWithName(new QName(REQUEST)).getText();
                double requestCountValue =  Double.parseDouble(requestCount);
                result.add(new ProviderAPIUsage(apiName,requestCountValue));
            }
        }
        return result;
    }


    /**
     * Get all API names
     * @return
     * @throws APIMgtUsageQueryServiceClientException
     */
    public String[] getAPIList() throws APIMgtUsageQueryServiceClientException {
        IndexDTO indexDTO = null;
        String[] apiNames = null;

        try {
            IndexDTO apiIndex = indexAdminStub.getIndex(APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTableIndex);
            apiNames = indexAdminStub.getIndexValues(APIMgtUsageQueryServiceClientConstants.APIVersionSummaryTableIndex,
                                          apiIndex.getIndexedColumns()[0]);
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
