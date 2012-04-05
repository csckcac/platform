package org.wso2.carbon.bam.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.ClientUtil;
import org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.BAMSummaryGenerationDSStub;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.MaxCount;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.SummaryStat;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.SummaryTime;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.TimeStamp;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.Calendar;


public class BAMServiceSummaryDSClient {

    private BAMSummaryGenerationDSStub summaryGenerationDSStub;
    private static final String BAM_SUMMARY_GENERATION_DS = "BAMSummaryGenerationDS";
    private static final Log log = LogFactory.getLog(BAMServiceSummaryDSClient.class);

    public BAMServiceSummaryDSClient(String backendServerURL,
                                     ConfigurationContext configCtx) throws BAMException {
        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_SUMMARY_GENERATION_DS);
            summaryGenerationDSStub = new BAMSummaryGenerationDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
    }

    public BAMServiceSummaryDSClient(String cookie, String backendServerURL,
                                     ConfigurationContext configCtx) throws BAMException {
        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_SUMMARY_GENERATION_DS);
            summaryGenerationDSStub = new BAMSummaryGenerationDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
        ServiceClient client = summaryGenerationDSStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }


    public void cleanup() {
        try {
            summaryGenerationDSStub._getServiceClient().cleanupTransport();
//            summaryGenerationDSStub._getServiceClient().cleanup();
//            summaryGenerationDSStub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    public Calendar getLatestServerStatSummaryPeriod(int summaryPeriod, int serverId)
            throws BAMException {
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
                case BAMCalendar.HOUR_OF_DAY:
                    time = summaryGenerationDSStub.getLatestServerStatHourlySummaryHourId(serverId);
                    break;
                case BAMCalendar.DAY_OF_MONTH:
                    time = summaryGenerationDSStub.getLatestServerStatDailySummaryDayId(serverId);
                    break;
                case BAMCalendar.MONTH:
                    time = summaryGenerationDSStub.getLatestServerStatMonthlySummaryMonthId(serverId);
                    break;
                case BAMCalendar.QUATER:
                    time = summaryGenerationDSStub.getLatestServerStatQuarterlySummaryQuarterId(serverId);
                    break;
                case BAMCalendar.YEAR:
                    time = summaryGenerationDSStub.getLatestServerStatYearlySummaryYearId(serverId);
                    break;
                default:
                    throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "server data" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getServerStatMinimumPeriodId(serverId);
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                } else {
                    //We are running for the first time and there are no records in
                    //"server data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }

        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for server";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }

    // Service
    public Calendar getLatestServiceStatSummaryPeriod(int summaryPeriod, int serviceId)
            throws BAMException {
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
                case BAMCalendar.HOUR_OF_DAY:
                    time = summaryGenerationDSStub.getLatestServiceStatHourlySummaryHourId(serviceId);
                    break;
                case BAMCalendar.DAY_OF_MONTH:
                    time = summaryGenerationDSStub.getLatestServiceStatDailySummaryDayId(serviceId);
                    break;
                case BAMCalendar.MONTH:
                    time = summaryGenerationDSStub.getLatestServiceStatMonthlySummaryMonthId(serviceId);
                    break;
                case BAMCalendar.QUATER:
                    time = summaryGenerationDSStub.getLatestServiceStatQuarterlySummaryQuarterId(serviceId);
                    break;
                case BAMCalendar.YEAR:
                    time = summaryGenerationDSStub.getLatestServiceStatYearlySummaryYearId(serviceId);
                    break;
                default:
                    throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "service data" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getServiceStatMinimumPeriodId(serviceId);
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                } else {
                    //We are running for the first time and there are no records in
                    //"service data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }

        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for service";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }

    //  Operation
    public Calendar getLatestOperationStatSummaryPeriod(int summaryPeriod, int operationId)
            throws BAMException {
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
                case BAMCalendar.HOUR_OF_DAY:
                    time = summaryGenerationDSStub.getLatestOperationStatHourlySummaryHourId(operationId);
                    break;
                case BAMCalendar.DAY_OF_MONTH:
                    time = summaryGenerationDSStub.getLatestOperationStatDailySummaryDayId(operationId);
                    break;
                case BAMCalendar.MONTH:
                    time = summaryGenerationDSStub.getLatestOperationStatMonthlySummaryMonthId(operationId);
                    break;
                case BAMCalendar.QUATER:
                    time = summaryGenerationDSStub.getLatestOperationStatQuarterlySummaryQuarterId(operationId);
                    break;
                case BAMCalendar.YEAR:
                    time = summaryGenerationDSStub.getLatestOperationStatYearlySummaryYearId(operationId);
                    break;
                default:
                    throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "operation data" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getOperationStatMinimumPeriodId(operationId);
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                } else {
                    //We are running for the first time and there are no records in
                    //"operation data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }

        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for operation";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }

    public void addServerStatHourlySummary(SummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addServerStatHourlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                               stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                               stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServerStatHourlySummary failed", e);
        }
    }

    public void addServerStatDailySummary(SummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addServerStatDailySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                              stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                              stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServerStatDailySummary failed", e);
        }


    }

    public void addServerStatMonthlySummary(SummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addServerStatMonthlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServerStatMonthlySummary failed", e);
        }
    }

    public void addServerStatQuarterlySummary(SummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addServerStatQuarterlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                  stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                  stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServerStatQuarterlySummary failed", e);
        }
    }

    public void addServerStatYearlySummary(SummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addServerStatYearlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                               stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                               stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServerStatYearlySummary failed", e);
        }
    }

    public SummaryStatistic getServerStatHourlySummary(int serverId, BAMCalendar startTime,
                                                       BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServerHourlySummary(serverId, startTime, endTime);
            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServerHourlySummary failed", e);
        }
        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServerMaxCounts(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
            MaxCount[] maxCounts = summaryGenerationDSStub.getServerDataMaxCounts(serverId, startTime, endTime);
            if (maxCounts != null && maxCounts[0] != null) {
                populateSummaryStatisticFromMaxCounts(maxCounts[0], stat);
            }
            return stat;
        } catch (Exception e) {
            throw new BAMException("getServerMaxCounts failed", e);
        }
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceMaxCounts(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
            MaxCount[] maxCounts = summaryGenerationDSStub.getServiceDataMaxCounts(serverId, startTime, endTime);
            if (maxCounts != null && maxCounts[0] != null) {
                populateSummaryStatisticFromMaxCounts(maxCounts[0], stat);
            }
            return stat;
        } catch (Exception e) {
            throw new BAMException("getServiceMaxCounts failed", e);
        }
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationMaxCounts(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
            MaxCount[] maxCounts = summaryGenerationDSStub.getOperationDataMaxCounts(serverId, startTime, endTime);
            if (maxCounts != null && maxCounts[0] != null) {
                populateSummaryStatisticFromMaxCounts(maxCounts[0], stat);
            }
            return stat;
        } catch (Exception e) {
            throw new BAMException("getOperationMaxCounts failed", e);
        }
    }

    private void populateSummaryStatisticFromMaxCounts(MaxCount maxCount,
                                                       org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat) {
        if (maxCount.getReqCount() != null && maxCount.getReqCount().length() > 0) {
            stat.setReqCount(Integer.parseInt(maxCount.getReqCount()));
        }
        if (maxCount.getResCount() != null && maxCount.getResCount().length() > 0) {
            stat.setResCount(Integer.parseInt(maxCount.getResCount()));
        }
        if (maxCount.getFaultCount() != null && maxCount.getFaultCount().length() > 0) {
            stat.setFaultCount(Integer.parseInt(maxCount.getFaultCount()));
        }
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServerStatDailySummary(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServerDailySummary(serverId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServerDailySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServerStatMonthlySummary(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServerMonthlySummary(serverId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServerMonthlySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServerStatQuarterlySummary(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServerQuarterlySummary(serverId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServerQuarterlySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServerStatYearlySummary(
            int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServerYearlySummary(serverId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServerYearlySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceStatHourlySummary(
            int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServiceHourlySummary(serviceId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServiceHourlySummary failed", e);
        }

        return sst;
    }


    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceStatDailySummary(
            int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServiceDailySummary(serviceId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServiceHourlySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceStatMonthlySummary(
            int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServiceMonthlySummary(serviceId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServiceMonthlySummary failed", e);

        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceStatQuarterlySummary(
            int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServiceQuarterlySummary(serviceId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServiceQuarterlySummary failed", e);
        }

        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getServiceStatYearlySummary(
            int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getServiceYearlySummary(serviceId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getServiceYearlySummary failed", e);
        }

        return sst;
    }


    public void addServiceStatHourlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addServiceStatHourlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServiceStatHourlySummary failed", e);
        }
    }


    public void addServiceStatDailySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addServiceStatDailySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                               stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                               stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServiceStatDailySummary failed", e);
        }
    }

    public void addServiceStatMonthlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addServiceStatMonthlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                 stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                 stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServiceStatMonthlySummary failed", e);
        }

    }

    public void addServiceStatQuarterlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addServiceStatQuarterlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                   stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                   stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServiceStatQuarterlySummary failed", e);
        }

    }

    public void addServiceStatYearlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addServiceStatYearlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addServiceStatYearlySummary failed", e);
        }


    }


    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationStatHourlySummary(
            int opId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getOperationHourlySummary(opId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getOperationHourlySummary failed", e);
        }

        return sst;
    }


    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationStatDailySummary(
            int opId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getOperationDailySummary(opId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getOperationDailySummary failed", e);
        }
        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationStatMonthlySummary(
            int opId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getOperationMonthlySummary(opId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getOperationMonthlySummary failed", e);
        }
        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationStatQuarterlySummary(
            int opId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getOperationQuarterlySummary(opId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getOperationQuarterlySummary failed", e);
        }
        return sst;
    }

    public org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic getOperationStatYearlySummary(
            int opId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst = new org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic();
        try {
            SummaryStat[] stat = summaryGenerationDSStub.getOperationYearlySummary(opId, startTime, endTime);

            if (stat != null) {
                SummaryStat st = stat[0];
                if (st != null) {
                    populateSummaryStatistic(st, sst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getOperationYearlySummary failed", e);
        }
        return sst;
    }


    public void addOperationStatHourlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addOperationStatHourlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                  stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                  stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addOperationStatHourlySummary failed", e);
        }

    }


    public void addOperationStatDailySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addOperationStatDailySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                 stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                 stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addOperationStatDailySummary failed", e);
        }

    }

    public void addOperationStatMonthlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {

        try {
            summaryGenerationDSStub.addOperaionStatMonthlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                  stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                  stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addOperaionStatMonthlySummary failed", e);
        }


    }

    public void addOperationStatQuarterlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addOperationStatQuarterlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                     stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                     stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addOperationStatQuarterlySummary failed", e);
        }

    }

    public void addOperationStatYearlySummary(
            org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic stat)
            throws BAMException {
        try {
            summaryGenerationDSStub.addOperationStatYearlySummary(stat.getTypeId(), stat.getTimeDimensionId(), stat.getAvgResTime(),
                                                                  stat.getMaxResTime(), stat.getMinResTime(), stat.getReqCount(), stat.getResCount(),
                                                                  stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addOperationStatYearlySummary failed", e);
        }
    }

    private void populateSummaryStatistic(SummaryStat st,
                                          org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic sst) {
        if (st.getAvgResTime() != null && st.getAvgResTime().length() > 0) {
            sst.setAvgResTime(Double.parseDouble(st.getAvgResTime()));
        }

        if (st.getMaxResTime() != null && st.getMaxResTime().length() > 0) {
            sst.setMaxResTime(Double.parseDouble(st.getMaxResTime()));
        }

        if (st.getMinResTime() != null && st.getMinResTime().length() > 0) {
            sst.setMinResTime(Double.parseDouble(st.getMinResTime()));
        }

        if (st.getReqCount() != null && st.getReqCount().length() > 0) {
            sst.setReqCount(Integer.parseInt(st.getReqCount()));
        }

        if (st.getResCount() != null && st.getResCount().length() > 0) {
            sst.setResCount(Integer.parseInt(st.getResCount()));
        }

        if (st.getFaultCount() != null && st.getFaultCount().length() > 0) {
            sst.setFaultCount(Integer.parseInt(st.getFaultCount()));
        }
    }

    private Calendar getTimeStampForId(int summaryPeriod, SummaryTime[] time) throws Exception {
        Calendar cal;
        if (time != null && time[0] != null) {
            String idStr = time[0].getSummaryTime();
            int id;

            try {
                id = Integer.parseInt(idStr);
            } catch (Exception e) {
                // We may be able to recover from this since we are getting the current time as the
                // last resort in the calling method.
                return null;
            }

            switch (summaryPeriod) {
                case BAMCalendar.HOUR_OF_DAY:
                    cal = summaryGenerationDSStub.getHourDimFromId(id)[0].getStartTime();
                    break;
                case BAMCalendar.DAY_OF_MONTH:
                    cal = summaryGenerationDSStub.getDayDimFromId(id)[0].getStartTime();
                    break;
                case BAMCalendar.MONTH:
                    cal = summaryGenerationDSStub.getMonthDimFormId(id)[0].getStartTime();
                    break;
                case BAMCalendar.QUATER:
                    cal = summaryGenerationDSStub.getQuarterDimFromId(id)[0].getStartTime();
                    break;
                case BAMCalendar.YEAR:
                    cal = summaryGenerationDSStub.getYearDimFromId(id)[0].getStartTime();
                    break;
                default:
                    return null;
            }

            return BAMCalendar.getInstance(cal);
        }

        return null;
    }

}
