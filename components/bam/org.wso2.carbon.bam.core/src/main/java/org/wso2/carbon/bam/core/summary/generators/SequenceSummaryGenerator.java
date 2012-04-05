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

public class SequenceSummaryGenerator extends AbstractSummaryGenerator {
    private static Log log = LogFactory.getLog(SequenceSummaryGenerator.class);
    private MediationDataDO sequence;
    private ServerDO server;

    /**
     * Constructs a OperationSummaryGenerator for the given server of type timeInterval
     *
     * @param sequence
     * @param server
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public SequenceSummaryGenerator(ServerDO server, MediationDataDO sequence, int timeInterval) {
        super(timeInterval);
        this.setSequence(sequence);
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
            MediationSummaryStatistic stat = spm.getSequenceStatHourlySummary(getServer().getId(),
                    getSequence().getName(), getSequence().getDirection(), loHour, hiHour);
            stat.setName(getSequence().getName());
            stat.setTimeDimensionId(hd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getSequence().getDirection());

            SummaryPersistenceManager.getInstance().addSequenceStatHourlySummary(stat);

            TimeRange retention = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataRetentionPeriod();

            // do not delete anything if the retention period specified is 0.
            if (retention.getValue() != 0) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(loHour);
                BAMCalendar delHiHour = BAMCalendar.getInstance(hiHour);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());


                spm.deleteServerSequenceUserData(getServer().getId(),
                        getSequence().getName(), getSequence().getDirection(), delLoHour, delHiHour);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running hourly summary generator for sequence: " + getSequence()
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

            MediationSummaryStatistic stat = spm.getSequenceStatDailySummary(getServer().getId(),
                    getSequence().getName(), getSequence().getDirection(), loDay, hiDay);
            stat.setName(getSequence().getName());
            stat.setTimeDimensionId(dd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getSequence().getDirection());

            SummaryPersistenceManager.getInstance().addSequenceStatDailySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running daily summary generator for sequence: " + getSequence()
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

            MediationSummaryStatistic stat = spm.getSequenceStatMonthlySummary(getServer().getId(),
                    getSequence().getName(), getSequence().getDirection(), loMonth, hiMonth);
            stat.setName(getSequence().getName());
            stat.setTimeDimensionId(md.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getSequence().getDirection());

            SummaryPersistenceManager.getInstance().addSequenceStatMonthlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running monthly summary generator for sequence: " + getSequence()
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

            MediationSummaryStatistic stat = spm.getSequenceStatQuarterlySummary(getServer().getId(),
                    getSequence().getName(), getSequence().getDirection(), loQuarter, hiQuarter);
            stat.setName(getSequence().getName());
            stat.setTimeDimensionId(qd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getSequence().getDirection());

            SummaryPersistenceManager.getInstance().addSequenceStatQuarterlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running quarterly summary generator for operaion: " + getSequence()
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

            MediationSummaryStatistic stat = spm.getSequenceStatYearlySummary(getServer().getId(),
                    getSequence().getName(), getSequence().getDirection(), loYear, hiYear);
            stat.setName(getSequence().getName());
            stat.setTimeDimensionId(qd.getId());
            stat.setServerId(getServer().getId());
            stat.setDirection(getSequence().getDirection());

            SummaryPersistenceManager.getInstance().addSequenceStatYearlySummary(stat);
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running yearly summary generator for operaion: " + getSequence()
                        + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected Calendar getLatestYearlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestSequenceStatSummaryPeriod(getTimeInterval(), sequence);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestSequenceStatSummaryPeriod(getTimeInterval(), sequence);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestSequenceStatSummaryPeriod(getTimeInterval(), sequence);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestDailySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestSequenceStatSummaryPeriod(getTimeInterval(), sequence);
        } finally {
            client.cleanup();
        }
    }

    protected Calendar getLatestHourlySummaryTime() throws BAMException {
    	BAMSummaryGenerationDSClient client = BAMUtil.getBAMSummaryGenerationDSClient();
        try {
            return client.getLatestSequenceStatSummaryPeriod(getTimeInterval(), sequence);
        } finally {
            client.cleanup();
        }
    }

    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL() + ", Sequence: " + sequence.getName();
    }

    public MediationDataDO getSequence() {
        return sequence;
    }

    public void setSequence(MediationDataDO sequence) {
        this.sequence = sequence;
    }

    public ServerDO getServer() {
        return server;
    }

    public void setServer(ServerDO server) {
        this.server = server;
    }
}
