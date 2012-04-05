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
package org.wso2.carbon.usage.summary.generator;

import java.util.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.dimensions.DayDimension;
import org.wso2.carbon.bam.common.dataobjects.dimensions.MonthDimension;
import org.wso2.carbon.bam.core.summary.SummaryPersistenceManager;
import org.wso2.carbon.bam.core.summary.generators.AbstractSummaryGenerator;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.stratos.common.constants.UsageConstants;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.beans.xsd.BandwidthUsage;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.beans.xsd.BandwidthUsageValue;
import org.wso2.carbon.usage.summary.generator.client.UsageSummaryGeneratorClient;

/**
 *
 */
public class RegistryUsageSummaryGenerator extends AbstractSummaryGenerator  {
    UsageSummaryGeneratorClient client;
    Tenant tenant;
    private static final Log log = LogFactory.getLog(RegistryUsageSummaryGenerator.class);
    
    /**
     * @param client
     */
    public RegistryUsageSummaryGenerator(
            Tenant tenant, int timeInterval, UsageSummaryGeneratorClient client) {
        super(timeInterval);
        this.client = client;
        this.tenant = tenant;
    }

    @Override
    protected Calendar getLatestDailySummaryTime() throws BAMException {
        return client.getLatestRegSummaryTime(getTimeInterval(), tenant.getId());
    }
    
    @Override
    protected void summarizeDaily(BAMCalendar start, BAMCalendar end) {
        long regBandwidth = 0;
        long regHistoryBandwidth=0;
        
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();
            DayDimension dd = spm.getDayDimension(start);
            if (dd == null) {
                spm.addDayDimension(start);
                dd = spm.getDayDimension(start);
            }
            
            //Get the usages 
            BandwidthUsage[] usages = client.getStub().getRegistryBandwidthUsage(tenant.getId());
            if((usages != null) && (usages[0] != null)){
                //We are sure there will be only one value
                regBandwidth = usages[0].getBandwidth();
                if(regBandwidth < 0){
                    //reg bandwidth should be not null, non-negative
                    regBandwidth = 0;
                }
            }
            
            usages = client.getStub().getRegistryHistoryBandwidthUsage(tenant.getId());
            if((usages != null) && (usages[0] != null)){
                //We are sure there will be only one value
                regHistoryBandwidth = usages[0].getBandwidth();
                if(regHistoryBandwidth < 0){
                    //reg history bandwidth should be not null, non-negative
                    regHistoryBandwidth = 0;
                }
            }
            
            //Write the summary
            client.getStub().addRegistryBandwidthUsageDailySummary(tenant.getId(), dd.getId(),
                    UsageConstants.REGISTRY_CONTENT_BANDWIDTH, regBandwidth, regHistoryBandwidth);
        } catch (Exception e) {
            String msg = "Unable to run Registry Usage daily summary for domain "
                    + tenant.getDomain();
            log.error(msg, e);
        } 
        
    }
    
    @Override
    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
        return client.getLatestRegSummaryTime(getTimeInterval(), tenant.getId());
    }

    @Override
    protected void summarizeMonthly(BAMCalendar start, BAMCalendar end) {
        long regBandwidth = 0;
        long regHistoryBandwidth=0;
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();
            MonthDimension md = spm.getMonthDimension(start);
            if (md == null) {
                spm.addMonthDimension(start);
                md = spm.getMonthDimension(start);
            }
            
            //Get the usage
            BandwidthUsageValue[] usages;
            usages = client.getStub().getRegistryBandwidthUsageForMonthlySummary(
                    tenant.getId(), start, end);
            if((usages != null) && (usages[0] != null)){
                regBandwidth = usages[0].getRegistryBandwidth();
                regHistoryBandwidth = usages[0].getRegistryHistoryBandwidth();
                if(regBandwidth < 0){
                    //reg bandwidth should be not null, non-negative
                    regBandwidth = 0;
                }
                if(regHistoryBandwidth < 0){
                    //reg history bandwidth should be not null, non-negative
                    regHistoryBandwidth = 0;
                }
            }
            
            //Write the summary
            client.getStub().addRegistryBandwidthUsageMonthlySummary(tenant.getId(), md.getId(),
                    UsageConstants.REGISTRY_CONTENT_BANDWIDTH, regBandwidth, regHistoryBandwidth);
        } catch (Exception e) {
            String msg = "Unable to run Registry Usage monthly summary for domain "
                    + tenant.getDomain();
            log.error(msg, e);
        }
    }
    
    @Override
    protected void summarizeHourly(BAMCalendar loHour, BAMCalendar hiHour) {
        //Not going to summarize Hourly
    }

    @Override
    protected void summarizeQuarterly(BAMCalendar loQuarter, BAMCalendar hiQuarter) {
        // Not going to Summarize Quarterly
    }

    @Override
    protected void summarizeYearly(BAMCalendar loYear, BAMCalendar hiYear) {
        //Not going to Summarize yearly
        
    }

    @Override
    protected Calendar getLatestYearlySummaryTime() throws BAMException {
        //Not going to Summarize yearly
        return null;
    }

    @Override
    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
     // Not going to Summarize Quarterly
        return null;
    }

    @Override
    protected Calendar getLatestHourlySummaryTime() throws BAMException {
      //Not going to summarize Hourly
        return null;
    }

    @Override
    protected String getInstanceInfo() {
        return "Tenant: " + tenant.getDomain();
    }
    
}
