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
package org.wso2.carbon.bam.core.summary.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.clients.BAMServiceSummaryDSClient;
import org.wso2.carbon.bam.common.dataobjects.dimensions.*;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.summary.SummaryPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;

import java.util.Calendar;

public class ServerSummaryGenerator extends AbstractSummaryGenerator {
    private static final Log log = LogFactory.getLog(ServerSummaryGenerator.class);

    private ServerDO server;

    /**
     * Constructs a ServerSummaryGenerator for the given server of type timeInterval
     *
     * @param server       server on which the summary generation should happen.
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public ServerSummaryGenerator(ServerDO server, int timeInterval) {
        super(timeInterval);
        this.server = server;
    }

    protected void summarizeHourly(BAMCalendar loHour, BAMCalendar hiHour) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            HourDimension hd = spm.getHourDimension(loHour);
            if (hd == null) {
                spm.addHourDimension(loHour);
                hd = spm.getHourDimension(loHour);
            }

            SummaryStatistic stat = spm.getServerStatHourlySummary(this.getServer().getId(), loHour, hiHour);
            if (stat != null) {
                stat.setTypeId(this.getServer().getId());
                stat.setTimeDimensionId(hd.getId());
                SummaryPersistenceManager.getInstance().addServerStatHourlySummary(stat);
            }


            TimeRange retention = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataRetentionPeriod();

            // do not delete anything if the retention period specified is 0.
            if (retention.getValue() != 0) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(loHour);
                BAMCalendar delHiHour = BAMCalendar.getInstance(hiHour);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());


                spm.deleteServerData(getServer().getId(), delLoHour, delHiHour);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running hourly summary generator for server: " + this.getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeDaily(BAMCalendar loDay, BAMCalendar hiDay) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            DayDimension dd = spm.getDayDimension(loDay);
            if (dd == null) {
                spm.addHourDimension(loDay);
                dd = spm.getDayDimension(loDay);
            }

            SummaryStatistic stat = spm.getServerStatDailySummary(this.getServer().getId(), loDay, hiDay);
            if (stat != null) {
                stat.setTypeId(this.getServer().getId());
                stat.setTimeDimensionId(dd.getId());
                SummaryPersistenceManager.getInstance().addServerStatDailySummary(stat);
            }

        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running daily summary generator for server: " + this.getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeMonthly(BAMCalendar loMonth, BAMCalendar hiMonth) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            MonthDimension md = spm.getMonthDimension(loMonth);
            if (md == null) {
                spm.addMonthDimension(loMonth);
                md = spm.getMonthDimension(loMonth);
            }

            SummaryStatistic stat = spm.getServerStatMonthlySummary(this.getServer().getId(), loMonth, hiMonth);
            if (stat != null) {
                stat.setTypeId(this.getServer().getId());
                stat.setTimeDimensionId(md.getId());
                SummaryPersistenceManager.getInstance().addServerStatMonthlySummary(stat);
            }


        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running monthly summary generator for server: " + this.getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeQuarterly(BAMCalendar loQuarter, BAMCalendar hiQuarter) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            QuarterDimension qd = spm.getQuarterDimension(loQuarter);
            if (qd == null) {
                spm.addQuarterDimension(loQuarter);
                qd = spm.getQuarterDimension(loQuarter);
            }

            SummaryStatistic stat = spm.getServerStatQuarterlySummary(this.getServer().getId(), loQuarter, hiQuarter);
            if (stat != null) {
                stat.setTypeId(this.getServer().getId());
                stat.setTimeDimensionId(qd.getId());
                SummaryPersistenceManager.getInstance().addServerStatQuarterlySummary(stat);
            }

        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running quarterly summary generator for server: " + this.getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeYearly(BAMCalendar loYear, BAMCalendar hiYear) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            YearDimension md = spm.getYearDimension(loYear);
            if (md == null) {
                spm.addYearDimension(loYear);
                md = spm.getYearDimension(loYear);
            }

            SummaryStatistic stat = spm.getServerStatYearlySummary(this.getServer().getId(), loYear, hiYear);
            if (stat != null) {
                stat.setTypeId(this.getServer().getId());
                stat.setTimeDimensionId(md.getId());
                SummaryPersistenceManager.getInstance().addServerStatYearlySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running yearly summary generator for server: " + this.getServer().getServerURL(), e);
            }
        }
    }

    protected Calendar getLatestYearlySummaryTime() throws BAMException {
    	BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServerStatSummaryPeriod(getTimeInterval(), server.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServerStatSummaryPeriod(getTimeInterval(), server.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServerStatSummaryPeriod(getTimeInterval(), server.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestDailySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServerStatSummaryPeriod(getTimeInterval(), server.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestHourlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServerStatSummaryPeriod(getTimeInterval(), server.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL();
    }

    public ServerDO getServer() {
        return server;
    }
}
