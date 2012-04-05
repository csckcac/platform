/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.usage.summary.generator.client;

import java.util.Calendar;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.beans.xsd.SummaryTime;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.MeteringSummaryGenerationDSStub;

/**
 *
 */
public class UsageSummaryGeneratorClient {
    private MeteringSummaryGenerationDSStub meteringStub;
    private static final String METERING_ENDPOINT = "local://services/MeteringSummaryGenerationDS";
    private static final Log log = LogFactory.getLog(UsageSummaryGeneratorClient.class);
    
    public UsageSummaryGeneratorClient(ConfigurationContext configContext) throws Exception{
        if (configContext != null) {
            try {
                meteringStub = new MeteringSummaryGenerationDSStub(configContext, METERING_ENDPOINT);
            } catch (AxisFault e) {
                String msg = "Error in creating BAM metering stub.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }else{
            //We can't do any useful functionality with UsageSummaryGeneratorClient
            String msg = "Unable to create UsageSummaryGeneratorClient";
            log.error(msg);
            throw new Exception(msg);
        }
    }
    
    public MeteringSummaryGenerationDSStub getStub(){
        return meteringStub;
    }
    
    public Calendar getLatestSummaryTime(int summaryPeriod, int serverId) throws BAMException{
        BAMCalendar cal = null;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
            case BAMCalendar.HOUR_OF_DAY:
                time = meteringStub.getLatestHourlyBandwidthSummaryPeriodId(serverId);
                break;
            case BAMCalendar.DAY_OF_MONTH:
                time = meteringStub.getLatestDailyBandwidthSummaryPeriodId(serverId);
                break;
            case BAMCalendar.MONTH:
                time = meteringStub.getLatestMonthlyBandwidthSummaryPeriodId(serverId);
                break;
            case BAMCalendar.QUATER:
                time = meteringStub.getLatestQuarterlyBandwidthSummaryPeriodId(serverId);
                break;
            case BAMCalendar.YEAR:
                time = meteringStub.getLatestYearlyBandwidthSummaryPeriodId(serverId);
                break;
            default:
                throw new BAMException("Unexpected timeInterval");
            }
            
            if (time != null && time[0] != null) {
                //we are sure that there will be only one record.
                cal = BAMCalendar.getInstance(time[0].getStartTime());
            }else {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "server user data" table
                time = meteringStub.getMinimumPeriodId(serverId);
                if (time != null && time[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(time[0].getStartTime());
                }else{
                    //We are running for the first time and there are no records in 
                    //"server user data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            } 
            
        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }
    
    public Calendar getLatestRegSummaryTime(int summaryPeriod, int tenantId) throws BAMException{
        BAMCalendar cal = null;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
            case BAMCalendar.DAY_OF_MONTH:
                time = meteringStub.getLatestDailyRegistryBandwidthSummaryPeriodId(tenantId);
                break;
            case BAMCalendar.MONTH:
                time = meteringStub.getLatestMonthlyRegistryBandwidthSummaryPeriodId(tenantId);
                break;
            default:
                throw new BAMException("Unexpected timeInterval");
            }
            if (time == null || time[0] == null) {
                //This is the first time we are running the summary. So, start with current - 1 period
                cal = BAMCalendar.getInstance();
                cal.add(summaryPeriod, -2);
            }else{
                //we are sure that there will be only one record.
                cal = BAMCalendar.getInstance(time[0].getStartTime());
            }
        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime";
                log.error(msg);
                throw new BAMException(msg, e);
        }
        return cal;
    }

}
