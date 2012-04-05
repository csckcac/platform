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
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
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

public class OperationSummaryGenerator extends AbstractSummaryGenerator {
    private static Log log = LogFactory.getLog(OperationSummaryGenerator.class);
    private ServiceDO service;
    private ServerDO server;
    private OperationDO operation;

    /**
     * Constructs a OperationSummaryGenerator for the given server of type timeInterval
     *
     * @param server added server for the monitor
     * @param service  service of the service
     * @param operation  operation of the service
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public OperationSummaryGenerator(ServerDO server, ServiceDO service, OperationDO operation, int timeInterval) {
        super(timeInterval);
        this.service = service;
        this.server = server;
        this.operation = operation;
    }

    protected void summarizeHourly(BAMCalendar loHour, BAMCalendar hiHour) {
        try {
            SummaryPersistenceManager spm = SummaryPersistenceManager.getInstance();

            HourDimension hd = spm.getHourDimension(loHour);
            if (hd == null) {
                spm.addHourDimension(loHour);
                hd = spm.getHourDimension(loHour);
            }

            SummaryStatistic stat = spm.getOperationStatHourlySummary(this.getOperation().getOperationID(), loHour, hiHour);
            if (stat != null) {
                stat.setTypeId(this.getOperation().getOperationID());
                stat.setTimeDimensionId(hd.getId());
                SummaryPersistenceManager.getInstance().addOperationStatHourlySummary(stat);
            }

            TimeRange retention = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataRetentionPeriod();

            // do not delete anything if the retention period specified is 0.
            if (retention.getValue() != 0) {

                BAMCalendar delLoHour = BAMCalendar.getInstance(loHour);
                BAMCalendar delHiHour = BAMCalendar.getInstance(hiHour);

                delHiHour.add(retention.getType(), -1 * retention.getValue());
                delLoHour.add(retention.getType(), -1 * retention.getValue());

                spm.deleteOperationData(getOperation().getOperationID(), delLoHour, delHiHour);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running hourly summary generator for operation: " + getOperation().getName()
                        + " of service: " + getService().getName()
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

            SummaryStatistic stat = spm.getOperationStatDailySummary(this.getOperation().getOperationID(), loDay, hiDay);
            if (stat != null) {
                stat.setTypeId(this.getOperation().getOperationID());
                stat.setTimeDimensionId(dd.getId());
                SummaryPersistenceManager.getInstance().addOperationStatDailySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running daily summary generator for operation: " + getOperation().getName()
                        + " of service: " + getService().getName()
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

            SummaryStatistic stat = spm.getOperationStatMonthlySummary(this.getOperation().getOperationID(), loMonth, hiMonth);
            if (stat != null) {
                stat.setTypeId(this.getOperation().getOperationID());
                stat.setTimeDimensionId(md.getId());
                SummaryPersistenceManager.getInstance().addOperationStatMonthlySummary(stat);
            }

        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running monthly summary generator for operation: " + getOperation().getName()
                        + " of service: " + getService().getName()
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

            SummaryStatistic stat = spm.getOperationStatQuarterlySummary(this.getOperation().getOperationID(), loQuarter, hiQuarter);
            if (stat != null) {
                stat.setTypeId(this.getOperation().getOperationID());
                stat.setTimeDimensionId(qd.getId());
                SummaryPersistenceManager.getInstance().addOperationStatQuarterlySummary(stat);
            }
        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running quarterly summary generator for operation: " + getOperation().getName()
                        + " of service: " + getService().getName()
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

            SummaryStatistic stat = spm.getOperationStatYearlySummary(this.getOperation().getOperationID(), loYear, hiYear);
            if (stat != null) {
                stat.setTypeId(this.getOperation().getOperationID());
                stat.setTimeDimensionId(qd.getId());
                SummaryPersistenceManager.getInstance().addOperationStatYearlySummary(stat);
            }

        } catch (BAMException e) {
            if (log.isErrorEnabled()) {
                log.error("Error while running yearly summary generator for operation: " + getOperation().getName()
                        + " of service: " + getService().getName() + " of server: " + getServer().getServerURL(), e);
            }
        }
    }

    protected Calendar getLatestYearlySummaryTime() throws BAMException {
    	BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestOperationStatSummaryPeriod(getTimeInterval(), operation.getOperationID());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestQuarterlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestOperationStatSummaryPeriod(getTimeInterval(), operation.getOperationID());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestMonthlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestOperationStatSummaryPeriod(getTimeInterval(),
                                                                              operation.getOperationID());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestDailySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestOperationStatSummaryPeriod(getTimeInterval(),
                                                                              operation.getOperationID());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected Calendar getLatestHourlySummaryTime() throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
        try {
            return serviceSummaryDSClient.getLatestOperationStatSummaryPeriod(getTimeInterval(), operation.getOperationID());
        } finally {
            serviceSummaryDSClient.cleanup();
        }
    }

    protected String getInstanceInfo() {
        return "Server: " + server.getServerURL() + ", Service: " + service.getName() + ", Operation:"
                + operation.getName();
    }

    public ServiceDO getService() {
        return service;
    }

    public ServerDO getServer() {
        return server;
    }

    public OperationDO getOperation() {
        return operation;
    }
}
