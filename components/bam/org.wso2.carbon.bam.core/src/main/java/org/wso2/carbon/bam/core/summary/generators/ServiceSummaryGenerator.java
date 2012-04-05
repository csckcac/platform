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
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.summary.SummaryPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;

import java.util.Calendar;

public class ServiceSummaryGenerator extends AbstractSummaryGenerator {
    private static Log log = LogFactory.getLog(ServiceSummaryGenerator.class);
    private ServiceDO service;
    private ServerDO server;

    /**
     * Constructs a ServiceSummaryGenerator for the given server of type timeInterval
     *
     * @param server server data object
     * @param service service data object
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public ServiceSummaryGenerator(ServerDO server, ServiceDO service, int timeInterval) {
        super(timeInterval);
        this.service = service;
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

            SummaryStatistic stat = spm.getServiceStatHourlySummary(this.getService().getId(), loHour, hiHour);
            if (stat != null) {
                stat.setTypeId(this.getService().getId());
                stat.setTimeDimensionId(hd.getId());
                SummaryPersistenceManager.getInstance().addServiceStatHourlySummary(stat);
            }
            TimeRange retention = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataRetentionPeriod();

            // do not delete anything if the retention period specified is 0.
            if (retention.getValue() != 0) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(loHour);
                BAMCalendar delHiHour = BAMCalendar.getInstance(hiHour);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());


                spm.deleteServiceData(getService().getId(), delLoHour, delHiHour);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running hourly summary generator for service: " + getService().getName()
                        + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeDaily(BAMCalendar loDay, BAMCalendar hiDay) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            DayDimension dd = spm.getDayDimension(loDay);
            if (dd == null) {
                spm.addDayDimension(loDay);
                dd = spm.getDayDimension(loDay);
            }

            SummaryStatistic stat = spm.getServiceStatDailySummary(this.getService().getId(), loDay, hiDay);
            if (stat != null) {
                stat.setTypeId(this.getService().getId());
                stat.setTimeDimensionId(dd.getId());
                SummaryPersistenceManager.getInstance().addServiceStatDailySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running daily summary generator for service: " + getService().getName()
                        + " of server: " + getServer().getServerURL(), e);
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

            SummaryStatistic stat = spm.getServiceStatMonthlySummary(this.getService().getId(), loMonth, hiMonth);
            if (stat != null) {
                stat.setTypeId(this.getService().getId());
                stat.setTimeDimensionId(md.getId());
                SummaryPersistenceManager.getInstance().addServiceStatMonthlySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running monthly summary generator for service: " + getService().getName()
                        + " of server: " + getServer().getServerURL(), e);
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

            SummaryStatistic stat = spm.getServiceStatQuarterlySummary(this.getService().getId(), loQuarter, hiQuarter);
            if (stat != null) {
                stat.setTypeId(this.getService().getId());
                stat.setTimeDimensionId(qd.getId());
                SummaryPersistenceManager.getInstance().addServiceStatQuarterlySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running quarterly summary generator for service: " + getService().getName()
                        + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected void summarizeYearly(BAMCalendar loYear, BAMCalendar hiYear) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            YearDimension qd = spm.getYearDimension(loYear);
            if (qd == null) {
                spm.addYearDimension(loYear);
                qd = spm.getYearDimension(loYear);
            }

            SummaryStatistic stat = spm.getServiceStatYearlySummary(this.getService().getId(), loYear, hiYear);
            if (stat != null) {
                stat.setTypeId(this.getService().getId());
                stat.setTimeDimensionId(qd.getId());
                SummaryPersistenceManager.getInstance().addServiceStatYearlySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running yearly summary generator for service: " + getService().getName()
                        + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected Calendar getLatestYearlySummaryTime() throws BAMException {
    	BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServiceStatSummaryPeriod(getTimeInterval(), service.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServiceStatSummaryPeriod(getTimeInterval(), service.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServiceStatSummaryPeriod(getTimeInterval(), service.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestDailySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServiceStatSummaryPeriod(getTimeInterval(), service.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestHourlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestServiceStatSummaryPeriod(getTimeInterval(), service.getId());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL() + ", Service: " + service.getName();
    }

    public ServiceDO getService() {
        return service;
    }

    public ServerDO getServer() {
        return server;
    }
}
