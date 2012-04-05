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
import org.wso2.carbon.bam.common.clients.BAMSummaryGenerationDSClient;
import org.wso2.carbon.bam.common.dataobjects.dimensions.*;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationDataDO;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationSummaryStatistic;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.summary.SummaryPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;

import java.util.Calendar;

public class ProxyServiceSummaryGenerator extends AbstractSummaryGenerator {
    private static Log log = LogFactory.getLog(ProxyServiceSummaryGenerator.class);
    private MediationDataDO proxyService;
    private ServerDO server;

    /**
     * Constructs a ProxyServiceSummaryGenerator for the given server of type timeInterval
     *
     * @param proxyService
     * @param server
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public ProxyServiceSummaryGenerator(ServerDO server, MediationDataDO proxyService, int timeInterval) {
        super(timeInterval);
        this.setProxyService(proxyService);
        this.setServer(server);
    }

    protected void summarizeHourly(BAMCalendar loHour, BAMCalendar hiHour) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            HourDimension hd = spm.getHourDimension(loHour);
            if (hd == null) {
                spm.addHourDimension(loHour);
                hd = spm.getHourDimension(loHour);
            }

            //TODO: flag
            MediationSummaryStatistic stat = spm.getProxyServiceStatHourlySummary(getServer().getId(),
                    getProxyService().getName(), getProxyService().getDirection(), loHour, hiHour);
            stat.setName(getProxyService().getName());
            stat.setTimeDimensionId(hd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getProxyService().getDirection());

            SummaryPersistenceManager.getInstance().addProxyServiceStatHourlySummary(stat);

            TimeRange retention = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataRetentionPeriod();

            // do not delete anything if the retention period specified is 0.
            if (retention.getValue() != 0) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(loHour);
                BAMCalendar delHiHour = BAMCalendar.getInstance(hiHour);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());


                spm.deleteServerProxyServiceUserData(getServer().getId(),
                        getProxyService().getName(), getProxyService().getDirection(), delLoHour, delHiHour);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running hourly summary generator for proxyService: " + getProxyService()
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

            MediationSummaryStatistic stat = spm.getProxyServiceStatDailySummary(getServer().getId(),
                    getProxyService().getName(), getProxyService().getDirection(), loDay, hiDay);
            stat.setName(getProxyService().getName());
            stat.setTimeDimensionId(dd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getProxyService().getDirection());

            SummaryPersistenceManager.getInstance().addProxyServiceStatDailySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running daily summary generator for proxyService: " + getProxyService()
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

            MediationSummaryStatistic stat = spm.getProxyServiceStatMonthlySummary(getServer().getId(),
                    getProxyService().getName(), getProxyService().getDirection(), loMonth, hiMonth);
            stat.setName(getProxyService().getName());
            stat.setTimeDimensionId(md.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getProxyService().getDirection());

            SummaryPersistenceManager.getInstance().addProxyServiceStatMonthlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running monthly summary generator for proxyService: " + getProxyService()
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

            MediationSummaryStatistic stat = spm.getProxyServiceStatQuarterlySummary(getServer().getId(),
                    getProxyService().getName(), getProxyService().getDirection(), loQuarter, hiQuarter);
            stat.setName(getProxyService().getName());
            stat.setTimeDimensionId(qd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getProxyService().getDirection());

            SummaryPersistenceManager.getInstance().addProxyServiceStatQuarterlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running quarterly summary generator for operaion: " + getProxyService()
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

            MediationSummaryStatistic stat = spm.getProxyServiceStatYearlySummary(getServer().getId(),
                    getProxyService().getName(), getProxyService().getDirection(), loYear, hiYear);
            stat.setName(getProxyService().getName());
            stat.setTimeDimensionId(qd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getProxyService().getDirection());

            SummaryPersistenceManager.getInstance().addProxyServiceStatYearlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running yearly summary generator for operaion: " + getProxyService()
                        + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected Calendar getLatestYearlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestProxyServiceStatSummaryPeriod(getTimeInterval(), proxyService);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestProxyServiceStatSummaryPeriod(getTimeInterval(), proxyService);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestProxyServiceStatSummaryPeriod(getTimeInterval(), proxyService);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestDailySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestProxyServiceStatSummaryPeriod(getTimeInterval(), proxyService);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestHourlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestProxyServiceStatSummaryPeriod(getTimeInterval(), proxyService);
        } finally {
            client.cleanup();
        }
    }

    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL() + ", Proxy: " + proxyService.getName();
    }

    public MediationDataDO getProxyService() {
        return proxyService;
    }

    public void setProxyService(MediationDataDO proxyService) {
        this.proxyService = proxyService;
    }

    public ServerDO getServer() {
        return server;
    }

    public void setServer(ServerDO server) {
        this.server = server;
    }
}
