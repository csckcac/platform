/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.usage.api;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.constants.UsageConstants;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.usage.beans.*;
import org.wso2.carbon.usage.meteringqueryds.stub.beans.xsd.BandwidthStat;
import org.wso2.carbon.usage.meteringqueryds.stub.beans.xsd.RegBandwidthStat;
import org.wso2.carbon.usage.meteringqueryds.stub.beans.xsd.ServiceRequestStat;
import org.wso2.carbon.usage.meteringqueryds.stub.MeteringQueryDSStub;
import org.wso2.carbon.usage.meteringqueryds.stub.beans.xsd.InstanceUsageStat;
import org.wso2.carbon.usage.util.Util;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class TenantUsageRetriever {
    private static final Log log = LogFactory.getLog(TenantUsageRetriever.class);
    private static final String DEFAULT_SERVICE_NAME = "Stratos";
    private static final String METERING_ENDPOINT = "local://services/MeteringQueryDS";
    private static final String TOTAL_LABEL = "Total";
    public static final int REG_BANDWIDTH_INDEX = 0;
    public static final int SVC_BANDWIDTH_INDEX = 1;
    public static final int WEBAPP_BANDWIDTH_INDEX = 2;

    private RegistryService registryService;
    private MeteringQueryDSStub meteringStub;

    public TenantUsageRetriever(RegistryService registryService, ConfigurationContext configContext)
            throws Exception {

        // we are loading the essentials from the constructors in order to restrict the users
        // to use the usage retrievers.
        this.registryService = registryService;

        if (configContext != null) {
            try {
                this.meteringStub = new MeteringQueryDSStub(configContext, METERING_ENDPOINT);
            } catch (AxisFault e) {
                String msg = "Error in creating BAM metering stub.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        } else {
            //We can't do any useful functionality with TenantUsageRetriever
            String msg = "Unable to create TenantUsageRetriever";
            log.error(msg);
            throw new Exception(msg);
        }
    }


    public TenantDataCapacity getDataCapacity(int tenantId, Calendar startDate, Calendar endDate,
                                              boolean currentMonth) throws Exception {

        RegBandwidthStat[] stats;
        if (currentMonth) {
            stats = meteringStub.getDailyRegistryBandwidthUsageStats(tenantId, startDate, endDate);
        } else {
            stats = meteringStub.getRegistryBandwidthUsageStats(tenantId, startDate, endDate);
        }

        TenantDataCapacity capacity = new TenantDataCapacity();

        //We will be sure that there will be only one record
        if ((stats != null) && (stats[0] != null)) {
            capacity.setRegistryContentCapacity(stats[0].getRegistryBandwidth());
            capacity.setRegistryContentHistoryCapacity(stats[0].getRegistryHistoryBandwidth());
        }
        return capacity;
    }

    public int getCurrentUserCount(int tenantId) throws RegistryException {
        UserRealm userRealm = registryService.getUserRealm(tenantId);
        int usersCount;
        try {
            String[] users = userRealm.getUserStoreManager().listUsers("*", -1);
            usersCount = users.length;
        } catch (UserStoreException e) {
            String msg = "Error in getting the current users.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return usersCount;
    }

    public BandwidthStatistics[][] getBandwidthStatistics(int tenantId, Calendar startDate,
                                                          Calendar endDate, boolean currentMonth) throws Exception {

        BandwidthStat[] stats;
        if (currentMonth) {
            stats = meteringStub.getHourlyBandwidthStats(tenantId, startDate, endDate);
        } else {
            stats = meteringStub.getBandwidthStats(tenantId, startDate, endDate);
        }

        // store the statistics in a temporary map. This is because, we are getting the server name 
        // from the URL and there might be two distinct URL gives same server name. 
        // For example, http://esb.a.b/ and http://esb.c.d/ both will give the server name as "esb"
        // Hence, the value should be accumulated value
        HashMap<String, BandwidthStatistics> regBwMap = new HashMap<String, BandwidthStatistics>();
        HashMap<String, BandwidthStatistics> svcBwMap = new HashMap<String, BandwidthStatistics>();
        HashMap<String, BandwidthStatistics> webappBwMap = new HashMap<String, BandwidthStatistics>();

        if (stats != null) {
            for (BandwidthStat stat : stats) {
                //Proceed only if incoming bandwidth or outgoing bandwidth is not zero
                if ((stat.getIncomingBandwidth() == 0) && (stat.getOutgoingBandwidth() == 0)) {
                    continue;
                }

                String serverName = extractServiceNameFromUrl(stat.getServerUrl());
                String bandwidthName = stat.getBandwidthName();

                HashMap<String, BandwidthStatistics> bwMap;
                if (bandwidthName.equals(UsageConstants.REGISTRY_BANDWIDTH)) {
                    bwMap = regBwMap;
                } else if (bandwidthName.equals(UsageConstants.SERVICE_BANDWIDTH)) {
                    bwMap = svcBwMap;
                } else if (bandwidthName.equals(UsageConstants.WEBAPP_BANDWIDTH)) {
                    bwMap = webappBwMap;
                } else {
                    log.warn("Unable to identify bandwidth name " + bandwidthName);
                    continue;
                }

                //find whether the map already has this key; If not, insert a new one
                BandwidthStatistics reqStat = bwMap.get(serverName);
                if (reqStat == null) {
                    reqStat = new BandwidthStatistics(serverName);
                    bwMap.put(serverName, reqStat);
                }

                // Update the service specific statistics
                reqStat.setIncomingBandwidth(
                        reqStat.getIncomingBandwidth() + stat.getIncomingBandwidth());
                reqStat.setOutgoingBandwidth(
                        reqStat.getOutgoingBandwidth() + stat.getOutgoingBandwidth());
            }
        }

        //Convert to array and return it
        BandwidthStatistics[][] returnValue = new BandwidthStatistics[3][];
        Collection<BandwidthStatistics> values = regBwMap.values();
        returnValue[REG_BANDWIDTH_INDEX] = values.toArray(new BandwidthStatistics[values.size()]);
        values = svcBwMap.values();
        returnValue[SVC_BANDWIDTH_INDEX] = values.toArray(new BandwidthStatistics[values.size()]);
        values = webappBwMap.values();
        returnValue[WEBAPP_BANDWIDTH_INDEX] = values.toArray(new BandwidthStatistics[values.size()]);

        return returnValue;
    }

    public RequestStatistics[] getRequestStatistics(int tenantId, Calendar startDate,
                                                    Calendar endDate, boolean currentMonth) throws Exception {

        ServiceRequestStat[] stats;
        if (currentMonth) {
            //Read from hourly table. Monthly table will not get updated until end of month
            stats = meteringStub.getHourlyServiceRequestStats(tenantId, startDate, endDate);
        } else {
            //Not a current month; Read from Monthly table. 
            stats = meteringStub.getServiceRequestStats(tenantId, startDate, endDate);
        }

        // store the statistics in a temporary map. This is because, we are getting the server name 
        // from the URL and there might be two distinct URL gives same server name. 
        // For example, http://esb.a.b/ and http://esb.c.d/ both will give the server name as "esb"
        // Hence, the value should be accumulated value
        HashMap<String, RequestStatistics> tempReqStatMap = new HashMap<String, RequestStatistics>();

        if (stats != null) {
            for (ServiceRequestStat stat : stats) {
                //Proceed only if request count is not zero
                if (stat.getReqCount() == 0) {
                    continue;
                }

                String serverName = extractServiceNameFromUrl(stat.getServerUrl());

                //find whether the map already has this key; If not, insert a new one
                RequestStatistics reqStat = tempReqStatMap.get(serverName);
                if (reqStat == null) {
                    reqStat = new RequestStatistics(serverName);
                    tempReqStatMap.put(serverName, reqStat);
                }

                // Update the service specific statistics
                reqStat.setRequestCount(reqStat.getRequestCount() + stat.getReqCount());
                reqStat.setResponseCount(reqStat.getResponseCount() + stat.getResCount());
                reqStat.setFaultCount(reqStat.getFaultCount() + stat.getFaultCount());
            }
        }

        //Convert to array and return it
        Collection<RequestStatistics> values = tempReqStatMap.values();
        return values.toArray(new RequestStatistics[values.size()]);
    }

    public TenantUsage getTenantUsage(int tenantId, String yearMonth) throws Exception {
        //get the domain name
        TenantManager tenantManger = Util.getRealmService().getTenantManager();
        String domain = tenantManger.getDomain(tenantId);
        TenantUsage tenantUsage = new TenantUsage(tenantId, domain);

        //Get the startDate, endDate from yearMonth String
        Date date = CommonUtil.getDateFromMonthString(yearMonth);
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);
        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.MONTH, 1);

        //Calculate whether the yearMonth fits to current month; if the current date is less than
        // endDate, then we treat it as current month
        boolean isCurrentMonth = (Calendar.getInstance().compareTo(endDate) <= 0);

        //get the data capacity
        TenantDataCapacity capacity;
        try {
            capacity = getDataCapacity(tenantId, startDate, endDate, isCurrentMonth);
        } catch (Exception e) {
            String msg = "Error in getting data capacity from metering service.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        tenantUsage.setRegistryCapacity(capacity);

        //get the service request statistics
        RequestStatistics[] reqStats = null;
        try {
            reqStats = getRequestStatistics(tenantId, startDate, endDate, isCurrentMonth);
        } catch (Exception e) {
            String msg = "Error in getting request statistics from metering service.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        tenantUsage.setRequestStatistics(reqStats);

        //Calculate total Request statistics
        RequestStatistics totalReqStat = new RequestStatistics(TOTAL_LABEL);
        long totalReq = 0;
        long totalRes = 0;
        long totalFault = 0;
        for (RequestStatistics stat : reqStats) {
            totalReq += stat.getRequestCount();
            totalRes += stat.getResponseCount();
            totalFault += stat.getFaultCount();
        }
        totalReqStat.setRequestCount(totalReq);
        totalReqStat.setResponseCount(totalRes);
        totalReqStat.setFaultCount(totalFault);
        tenantUsage.setTotalRequestStatistics(totalReqStat);

        //get Bandwidth statistics
        BandwidthStatistics[][] bwStats = null;
        try {
            bwStats = getBandwidthStatistics(tenantId, startDate, endDate, isCurrentMonth);
        } catch (Exception e) {
            String msg = "Error in getting bandwidth statistics from metering service.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        tenantUsage.setRegistryBandwidthStatistics(bwStats[REG_BANDWIDTH_INDEX]);
        tenantUsage.setServiceBandwidthStatistics(bwStats[SVC_BANDWIDTH_INDEX]);
        tenantUsage.setWebappBandwidthStatistics(bwStats[WEBAPP_BANDWIDTH_INDEX]);

        //get the total bandwidths
        int index = 0;
        for (BandwidthStatistics[] bwArray : bwStats) {
            long incomingBandwidth = 0;
            long outgoingBandwidth = 0;
            for (BandwidthStatistics bandwidth : bwArray) {
                incomingBandwidth += bandwidth.getIncomingBandwidth();
                outgoingBandwidth += bandwidth.getOutgoingBandwidth();
            }
            BandwidthStatistics total = new BandwidthStatistics(TOTAL_LABEL);
            total.setIncomingBandwidth(incomingBandwidth);
            total.setOutgoingBandwidth(outgoingBandwidth);
            switch (index) {
                case REG_BANDWIDTH_INDEX:
                    tenantUsage.setTotalRegistryBandwidth(total);
                    break;
                case SVC_BANDWIDTH_INDEX:
                    tenantUsage.setTotalServiceBandwidth(total);
                    break;
                case WEBAPP_BANDWIDTH_INDEX:
                    tenantUsage.setTotalWebappBandwidth(total);
                    break;
            }
            ++index;
        }

        // the users count will be calculated only if the yearMonth is the current yearMonth
        if (isCurrentMonth) {
            int usersCount = getCurrentUserCount(tenantId);
            tenantUsage.setNumberOfUsers(usersCount);
        }

        return tenantUsage;
    }

    /**
     * @param serviceURL
     * @return service name
     *         <p/>
     *         Extract the stratos service part from URL; expecting the URL as
     *         protocol://service.domain:port/tenant-domain/ or service.domain:port/tenant
     *         We are interested in "service" part only
     */
    private String extractServiceNameFromUrl(String serviceURL) {
        if (serviceURL == null || serviceURL.equals("")) {
            //No service URL is given, so return a default value
            return DEFAULT_SERVICE_NAME;
        }

        int startIndex = serviceURL.indexOf("://"); //exclude protocol:// part
        if (startIndex != -1) {
            // protocol://service.domain:port/tenant-domain/ case
            startIndex += 3;
        } else {
            //service.domain:port/tenant case
            startIndex = 0;
        }

        int endIndex = serviceURL.indexOf('.', startIndex); //take upto first "."
        if (endIndex == -1) {
            // "." is not there; search for ":"
            endIndex = serviceURL.indexOf(':', startIndex);

            if (endIndex == -1) {
                //Still could not find ":", then search for "/"
                endIndex = serviceURL.indexOf('/', startIndex);

                if (endIndex == -1) {
                    //Noting is there, so take the whole service URL
                    endIndex = serviceURL.length();
                }
            }

        }
        return serviceURL.substring(startIndex, endIndex);
    }

    /**
     * @return Instance Usages Statics Array that contains data
     * @throws Exception when back end error occurs
     */
    public InstanceUsageStatics[] getInstanceUsages() throws Exception {
        InstanceUsageStat[] instanceData = meteringStub.getInstanceUsageStats();
        if (instanceData == null || instanceData.length == 0) {
            return null;
        }
        InstanceUsageStatics[] returnValue = new InstanceUsageStatics[instanceData.length];
        int elementID = 0;
        for (InstanceUsageStat iu : instanceData) {
            InstanceUsageStatics iu1 = new InstanceUsageStatics();
            iu1.setInstanceID(iu.getInstanceId().intValue());
            iu1.setInstanceURL(iu.getServerURL());
            iu1.setStartTime(iu.getStartTimestamp());
            iu1.setStopTime(iu.getStopTimestamp());
            iu1.setRunning(iu.getIsRunning());
            returnValue[elementID] = iu1;
            elementID = elementID + 1;
        }
        return returnValue;
    }
}
