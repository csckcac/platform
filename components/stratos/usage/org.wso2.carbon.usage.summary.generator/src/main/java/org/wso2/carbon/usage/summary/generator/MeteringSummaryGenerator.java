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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.dimensions.*;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.summary.SummaryPersistenceManager;
import org.wso2.carbon.bam.core.summary.generators.AbstractSummaryGenerator;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;
import org.wso2.carbon.stratos.common.constants.UsageConstants;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.beans.xsd.BandwidthHourlyStatValue;
import org.wso2.carbon.usage.meteringsummarygenerationds.stub.beans.xsd.BandwidthStatValue;
import org.wso2.carbon.usage.summary.generator.client.UsageSummaryGeneratorClient;

import java.util.Calendar;

/**
 * 
 */
public class MeteringSummaryGenerator extends AbstractSummaryGenerator {
    private static final Log log = LogFactory.getLog(MeteringSummaryGenerator.class);
    private ServerDO server;
    private UsageSummaryGeneratorClient client;
    
    /**
     * @param timeInterval
     */
    public MeteringSummaryGenerator(
            ServerDO server, int timeInterval, UsageSummaryGeneratorClient client) {
        super(timeInterval);
        this.server = server;
        this.client = client;
    }

    @Override
    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL();
    }

    //TODO Refactor: These get times methods are not needed. We have to remove all and just have a 
    //getLatestTime() method in BAM.
    @Override
    protected Calendar getLatestHourlySummaryTime() throws BAMException {
        return client.getLatestSummaryTime(getTimeInterval(), server.getId());
    }

    @Override
    protected Calendar getLatestDailySummaryTime() throws BAMException {
        return client.getLatestSummaryTime(getTimeInterval(), server.getId());
    }

    @Override
    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
        return client.getLatestSummaryTime(getTimeInterval(), server.getId());
    }

    @Override
    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
        return client.getLatestSummaryTime(getTimeInterval(), server.getId());
    }

    @Override
    protected Calendar getLatestYearlySummaryTime() throws BAMException {
        return client.getLatestSummaryTime(getTimeInterval(), server.getId());
    }

    @Override
    protected void summarizeHourly(BAMCalendar start, BAMCalendar end) {
        try {
            //TODO Re-factor: This should be done by AbstractSummaryGenerator 
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();
            HourDimension hd = spm.getHourDimension(start);
            if (hd == null) {
                spm.addHourDimension(start);
                hd = spm.getHourDimension(start);
            }
            //End TODO

            // Get the raw statistics
            BandwidthHourlyStatValue[] statValues = client.getStub().getDataForHourlySummary(
                    server.getId(), UsageConstants.BANDWIDTH_KEY_PATTERN, start, end);
            
            //Summarize
            BandwidthSummarizer summarizer = new BandwidthSummarizer(statValues);

            //Write the summary back
            // Skip writing 0 values for summery tables
            if (summarizer.regInBandwidth > 0 || summarizer.regOutBandwidth > 0) {
                client.getStub().addBandwidthStatHourlySummary(server.getId(), hd.getId(),
                        UsageConstants.REGISTRY_BANDWIDTH, summarizer.regInBandwidth,
                        summarizer.regOutBandwidth);
            }
            if (summarizer.svcInBandwidth > 0 || summarizer.svcOutBandwidth > 0) {
                client.getStub().addBandwidthStatHourlySummary(server.getId(), hd.getId(),
                        UsageConstants.SERVICE_BANDWIDTH, summarizer.svcInBandwidth,
                        summarizer.svcOutBandwidth);
            }
            // Here we will add one bandwidth without checking 0 or not since we need to get
            // last summery generated time per given server
            //Per one summery generation we must write at least one entry to table
            client.getStub().addBandwidthStatHourlySummary(server.getId(), hd.getId(),
                    UsageConstants.WEBAPP_BANDWIDTH, summarizer.webappInBandwidth,
                    summarizer.webappOutBandwidth);

            //TODO Re-factor: This should be done by AbstractSummaryGenerator
            // do not delete anything if the retention period specified is 0.
            TimeRange retention = BAMPersistenceManager.getPersistenceManager(
                    BAMUtil.getRegistry()).getDataRetentionPeriod();
            if ((retention != null) && (retention.getValue() != 0)) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(start);
                BAMCalendar delHiHour = BAMCalendar.getInstance(end);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());
                
                deleteServerUserData(delLoHour, delHiHour);
            }
            //End TODO
        } catch (Exception e) {
            log.error("Error while running hourly bandwidth summary generator for server "
                    + server.getServerURL(), e);
        } 
    }
    
    @Override
    protected void summarizeDaily(BAMCalendar start, BAMCalendar end) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            DayDimension dd = spm.getDayDimension(start);
            if (dd == null) {
                spm.addDayDimension(start);
                dd = spm.getDayDimension(start);
            }
            
            BandwidthStatValue[] statValues = client.getStub().getDataForDailySummary(
                    server.getId(), start, end);
          //Summarize
            BandwidthSummarizer summarizer = new BandwidthSummarizer(statValues);

            //Write the summary back
            // Skip writing 0 values for summery tables
            if (summarizer.regInBandwidth > 0 || summarizer.regOutBandwidth > 0) {
                client.getStub().addBandwidthStatDailySummary(server.getId(), dd.getId(),
                        UsageConstants.REGISTRY_BANDWIDTH, summarizer.regInBandwidth,
                        summarizer.regOutBandwidth);
            }
            if (summarizer.svcInBandwidth > 0 || summarizer.svcOutBandwidth > 0) {
                client.getStub().addBandwidthStatDailySummary(server.getId(), dd.getId(),
                        UsageConstants.SERVICE_BANDWIDTH, summarizer.svcInBandwidth,
                        summarizer.svcOutBandwidth);
            }
            // Here we will add one bandwidth without checking 0 or not since we need to get
            // last summery generated time per given server
            //Per one summery generation we must write at least one entry to table
            client.getStub().addBandwidthStatDailySummary(server.getId(), dd.getId(),
                    UsageConstants.WEBAPP_BANDWIDTH, summarizer.webappInBandwidth,
                    summarizer.webappOutBandwidth);
        } catch (Exception e) {
            log.error("Error while running daily bandwidth summary generator for server: " + 
                    server.getServerURL(), e);
        }
    }

    @Override
    protected void summarizeMonthly(BAMCalendar start, BAMCalendar end) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            MonthDimension md = spm.getMonthDimension(start);
            if (md == null) {
                spm.addMonthDimension(start);
                md = spm.getMonthDimension(start);
            }
            
            BandwidthStatValue[] statValues = client.getStub().getDataForMonthlySummary(
                    server.getId(), start, end);
          //Summarize
            BandwidthSummarizer summarizer = new BandwidthSummarizer(statValues);
            
          //Write the summary back
            client.getStub().addBandwidthStatMonthlySummary(server.getId(), md.getId(),
                    UsageConstants.REGISTRY_BANDWIDTH, summarizer.regInBandwidth,
                    summarizer.regOutBandwidth);
            client.getStub().addBandwidthStatMonthlySummary(server.getId(), md.getId(),
                    UsageConstants.SERVICE_BANDWIDTH, summarizer.svcInBandwidth,
                    summarizer.svcOutBandwidth);
            client.getStub().addBandwidthStatMonthlySummary(server.getId(), md.getId(),
                    UsageConstants.WEBAPP_BANDWIDTH, summarizer.webappInBandwidth,
                    summarizer.webappOutBandwidth);
        } catch (Exception e) {
            log.error("Error while running monthly bandwidth summary generator for server: " + 
                    server.getServerURL(), e);
        }
    }

    @Override
    protected void summarizeQuarterly(BAMCalendar start, BAMCalendar end) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            QuarterDimension qd = spm.getQuarterDimension(start);
            if (qd == null) {
                spm.addQuarterDimension(start);
                qd = spm.getQuarterDimension(start);
            }
            
            BandwidthStatValue[] statValues = client.getStub().getDataForQuarterlySummary(
                    server.getId(), start, end);
          //Summarize
            BandwidthSummarizer summarizer = new BandwidthSummarizer(statValues);
            
          //Write the summary back
            client.getStub().addBandwidthStatQuarterlySummary(server.getId(), qd.getId(),
                    UsageConstants.REGISTRY_BANDWIDTH, summarizer.regInBandwidth,
                    summarizer.regOutBandwidth);
            client.getStub().addBandwidthStatQuarterlySummary(server.getId(), qd.getId(),
                    UsageConstants.SERVICE_BANDWIDTH, summarizer.svcInBandwidth,
                    summarizer.svcOutBandwidth);
            client.getStub().addBandwidthStatQuarterlySummary(server.getId(), qd.getId(),
                    UsageConstants.WEBAPP_BANDWIDTH, summarizer.webappInBandwidth,
                    summarizer.webappOutBandwidth);
        } catch (Exception e) {
            log.error("Error while running quarterly bandwidth summary generator for server: " + 
                    server.getServerURL(), e);
        }
    }

    @Override
    protected void summarizeYearly(BAMCalendar start, BAMCalendar end) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            YearDimension yd = spm.getYearDimension(start);
            if (yd == null) {
                spm.addYearDimension(start);
                yd = spm.getYearDimension(start);
            }
            
            BandwidthStatValue[] statValues = client.getStub().getDataForYearlySummary(
                    server.getId(), start, end);
          //Summarize
            BandwidthSummarizer summarizer = new BandwidthSummarizer(statValues);
            
            // Write the summary back
            client.getStub().addBandwidthStatYearlySummary(server.getId(), yd.getId(),
                    UsageConstants.REGISTRY_BANDWIDTH, summarizer.regInBandwidth,
                    summarizer.regOutBandwidth);
            client.getStub().addBandwidthStatYearlySummary(server.getId(), yd.getId(),
                    UsageConstants.SERVICE_BANDWIDTH, summarizer.svcInBandwidth,
                    summarizer.svcOutBandwidth);
            client.getStub().addBandwidthStatYearlySummary(server.getId(), yd.getId(),
                    UsageConstants.WEBAPP_BANDWIDTH, summarizer.webappInBandwidth,
                    summarizer.webappOutBandwidth);
        } catch (Exception e) {
            log.error("Error while running yearly bandwidth summary generator for server: " + 
                    server.getServerURL(), e);
        }
    }
    
    private void deleteServerUserData(Calendar start, Calendar end) throws Exception {
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.REGISTRY_BANDWIDTH + UsageConstants.IN_LABLE, start, end);
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.REGISTRY_BANDWIDTH + UsageConstants.OUT_LABLE, start, end);
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.SERVICE_BANDWIDTH + UsageConstants.IN_LABLE, start, end);
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.SERVICE_BANDWIDTH + UsageConstants.OUT_LABLE, start, end);
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.WEBAPP_BANDWIDTH + UsageConstants.IN_LABLE, start, end);
        client.getStub().deleteServerUserData(server.getId(),
                UsageConstants.WEBAPP_BANDWIDTH + UsageConstants.OUT_LABLE, start, end);
    }

    private class BandwidthSummarizer{
        long regInBandwidth;
        long regOutBandwidth;
        long svcInBandwidth;
        long svcOutBandwidth;
        long webappInBandwidth;
        long webappOutBandwidth;
        
        public BandwidthSummarizer(BandwidthHourlyStatValue[] statValues){
            if(statValues != null){
                for(BandwidthHourlyStatValue statValue : statValues){
                    String keyName = statValue.getKeyName();
                    String keyValue = statValue.getKeyValue();
                    if((keyName == null) || (keyValue == null)){
                        //We don't need to do anything
                        continue;
                    }
                    long value = 0;
                    try {
                        value = Long.parseLong(keyValue);
                    } catch (NumberFormatException e) {
                        //We can't convert the value to long. Since we read using key pattern
                        //this might be some other's data. We can continue here
                        log.debug("Unable to parse value to long. Key[" + keyName +
                                "] and value [" + keyValue + "]");
                        continue;
                    }
                    
                    if (keyName.equals(UsageConstants.REGISTRY_INCOMING_BW)) {
                        regInBandwidth += value;
                    } else if (keyName.equals(UsageConstants.REGISTRY_OUTGOING_BW)) {
                        regOutBandwidth += value;
                    } else if (keyName.equals(UsageConstants.SERVICE_INCOMING_BW)) {
                        svcInBandwidth += value;
                    } else if (keyName.equals(UsageConstants.SERVICE_OUTGOING_BW)) {
                        svcOutBandwidth += value;
                    } else if (keyName.equals(UsageConstants.WEBAPP_INCOMING_BW)) {
                        webappInBandwidth += value;
                    } else if (keyName.equals(UsageConstants.WEBAPP_OUTGOING_BW)) {
                        webappOutBandwidth += value;
                    } else {
                        // If non of the keys match, still it will not be an
                        // issue.
                        log.debug("Keyname doesn't match any known keys. Key[" + keyName
                                + "] and value [" + keyValue + "]");
                    }
                }
            }
        }

        /**
         * @param statValues
         */
        public BandwidthSummarizer(BandwidthStatValue[] statValues) {
            if(statValues != null){
                for(BandwidthStatValue statValue : statValues){
                    String keyName = statValue.getKeyName();
                    
                    if(keyName.equals(UsageConstants.REGISTRY_BANDWIDTH)){
                        regInBandwidth += statValue.getIncomingBandwidth();
                        regOutBandwidth += statValue.getOutgoingBandwidth();
                    }else if(keyName.equals(UsageConstants.SERVICE_BANDWIDTH)){
                        svcInBandwidth += statValue.getIncomingBandwidth();
                        svcOutBandwidth += statValue.getOutgoingBandwidth();
                    }else if(keyName.equals(UsageConstants.WEBAPP_BANDWIDTH)){
                        webappInBandwidth += statValue.getIncomingBandwidth();
                        webappOutBandwidth += statValue.getOutgoingBandwidth();
                    }else{
                        //If non of the keys match, still it will not be an issue. 
                        log.debug("Keyname doesn't match any known keys. Key[" + keyName + "]");
                    }
                }
            }
        }
    } //end of private class
}
