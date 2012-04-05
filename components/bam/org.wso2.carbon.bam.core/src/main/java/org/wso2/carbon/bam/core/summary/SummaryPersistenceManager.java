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
package org.wso2.carbon.bam.core.summary;

import org.wso2.carbon.bam.common.clients.BAMServiceSummaryDSClient;
import org.wso2.carbon.bam.common.clients.SummaryDimensionDSClient;
import org.wso2.carbon.bam.common.clients.BAMSummaryGenerationDSClient;
import org.wso2.carbon.bam.common.dataobjects.dimensions.*;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationSummaryStatistic;
import org.wso2.carbon.bam.common.dataobjects.stats.SummaryStatistic;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.BAMMath;

import java.util.Calendar;

import static org.wso2.carbon.bam.core.summary.BAMSummaryConstants.*;

/**
 * Wraps summary data service client to provide value added data access services.
 */
//TODO Cache summary dimensions
public class SummaryPersistenceManager {

	private static SummaryPersistenceManager spm;

	public static SummaryPersistenceManager getInstance() {

		if (spm == null) {
			spm = new SummaryPersistenceManager();
		}
		return spm;
	}

	public SummaryStatistic getServiceStatHourlySummary(int serviceId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
            SummaryStatistic stat = serviceSummaryDSClient.getServiceStatHourlySummary(serviceId, startTime, endTime);

            BAMCalendar prevHourStartTime = BAMCalendar.getInstance(startTime);
            prevHourStartTime.add(BAMCalendar.HOUR_OF_DAY, -24);
            BAMCalendar prevHourEndTime = BAMCalendar.getInstance(endTime);
            prevHourEndTime.add(BAMCalendar.HOUR_OF_DAY, -1);

            SummaryStatistic maxStatThisHour = serviceSummaryDSClient.getServiceMaxCounts(serviceId, startTime, endTime);
            SummaryStatistic maxStatPrevHour = serviceSummaryDSClient.getServiceMaxCounts(serviceId, prevHourStartTime,
                    prevHourEndTime);

            int val;
            val = maxStatThisHour.getReqCount() - maxStatPrevHour.getReqCount();
            stat.setReqCount(val >= 0 ? val : maxStatThisHour.getReqCount());

            if (maxStatThisHour.getReqCount() <= 0) {
            	stat.setAllZeros();
            } else {
            val = maxStatThisHour.getResCount() - maxStatPrevHour.getResCount();
            stat.setResCount(val >= 0 ? val : maxStatThisHour.getResCount());

            val = maxStatThisHour.getFaultCount() - maxStatPrevHour.getFaultCount();
            stat.setFaultCount(val >= 0? val : maxStatThisHour.getFaultCount());
            }
			return stat;
		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for service: " + serviceId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServiceStatDailySummary(int serviceId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServiceStatDailySummary(serviceId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for service: " + serviceId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServiceStatMonthlySummary(int serviceId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServiceStatMonthlySummary(serviceId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for service: " + serviceId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServiceStatQuarterlySummary(int serviceId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServiceStatQuarterlySummary(serviceId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for service: " + serviceId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServiceStatYearlySummary(int serviceId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServiceStatYearlySummary(serviceId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for service: " + serviceId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServiceStatHourlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServiceStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly service summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServiceStatDailySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServiceStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly service summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServiceStatMonthlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServiceStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly service summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServiceStatQuarterlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServiceStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly service summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServiceStatYearlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServiceStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly service summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServerStatHourlySummary(int serverId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			SummaryStatistic stat = serviceSummaryDSClient.getServerStatHourlySummary(serverId, startTime, endTime);

            BAMCalendar prevHourStartTime = BAMCalendar.getInstance(startTime);
            prevHourStartTime.add(BAMCalendar.HOUR_OF_DAY, -24);
            BAMCalendar prevHourEndTime = BAMCalendar.getInstance(endTime);
            prevHourEndTime.add(BAMCalendar.HOUR_OF_DAY, -1);

            SummaryStatistic maxStatThisHour = serviceSummaryDSClient.getServerMaxCounts(serverId, startTime, endTime);
            SummaryStatistic maxStatPrevHour = serviceSummaryDSClient.getServerMaxCounts(serverId, prevHourStartTime, prevHourEndTime);

            int val;
            val = maxStatThisHour.getReqCount() - maxStatPrevHour.getReqCount();
            stat.setReqCount(val >= 0 ? val : maxStatThisHour.getReqCount());

            if (maxStatThisHour.getReqCount() <= 0) {
            	stat.setAllZeros();
            } else {
            val = maxStatThisHour.getResCount() - maxStatPrevHour.getResCount();
            stat.setResCount(val >= 0 ? val : maxStatThisHour.getResCount());

            val = maxStatThisHour.getFaultCount() - maxStatPrevHour.getFaultCount();
            stat.setFaultCount(val >= 0? val : maxStatThisHour.getFaultCount());
            }

            return stat;
		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for server: " + serverId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServerStatDailySummary(int serverId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServerStatDailySummary(serverId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for server: " + serverId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServerStatMonthlySummary(int serverId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServerStatMonthlySummary(serverId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for server: " + serverId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServerStatQuarterlySummary(int serverId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServerStatQuarterlySummary(serverId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for server: " + serverId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getServerStatYearlySummary(int serverId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getServerStatYearlySummary(serverId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for server: " + serverId+ " start time: " +
                    startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServerStatHourlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServerStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly server summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServerStatDailySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServerStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding daily server summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServerStatMonthlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServerStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly server summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServerStatQuarterlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServerStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly server summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addServerStatYearlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
			serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addServerStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly server summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public YearDimension getYearDimension(Calendar year) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			return summaryDimension.getYearDimension(year.get(Calendar.YEAR));
		} catch (Exception e) {
			throw new BAMException("Retrieving year dimension failed for timestamp "
					+ BAMCalendar.getInstance(year).getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public QuarterDimension getQuarterDimension(Calendar quater) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();

            BAMCalendar bamQuater = BAMCalendar.getInstance(quater);
			YearDimension yd = getYearDimension(quater);
			if (yd == null)
				return null;
			return summaryDimension.getQuarterDimension(bamQuater.get(BAMCalendar.QUATER), yd.getId());
		} catch (Exception e) {
			throw new BAMException("Retrieving quarter dimension failed for timestamp "
					+ BAMCalendar.getInstance(quater).getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public MonthDimension getMonthDimension(Calendar month) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			QuarterDimension qd = getQuarterDimension(month);
			if (qd == null)
				return null;
			return summaryDimension.getMonthDimension(month.get(Calendar.MONTH), qd.getId());
		} catch (Exception e) {
			throw new BAMException("Retrieving month dimension failed for timestamp "
					+ BAMCalendar.getInstance(month).getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public DayDimension getDayDimension(Calendar day) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			MonthDimension md = getMonthDimension(day);
			if (md == null)
				return null;
			return summaryDimension.getDayDimension(day.get(Calendar.DAY_OF_MONTH), md.getId());
		} catch (Exception e) {
			throw new BAMException("Retrieving day dimension failed for timestamp "
					+ BAMCalendar.getInstance(day).getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public HourDimension getHourDimension(Calendar hour) throws BAMException {
        SummaryDimensionDSClient summaryDimension =  null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			DayDimension dd = getDayDimension(hour);
			if (dd == null)
				return null;
			return summaryDimension.getHourDimension(hour.get(Calendar.HOUR_OF_DAY), dd.getId());
		} catch (Exception e) {
			throw new BAMException("Retrieving hour dimension failed for timestamp "
					+ BAMCalendar.getInstance(hour).getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public void addYearDimension(BAMCalendar startTime) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			BAMCalendar yearStartTime = BAMCalendar.getYear(startTime);
			summaryDimension.addYearDimension(yearStartTime);
		} catch (Exception e) {
			throw new BAMException("Adding year dimension failed: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public void addQuarterDimension(BAMCalendar startTime) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			YearDimension yd = this.getYearDimension(startTime);
			if (yd == null) {
				addYearDimension(startTime);
				yd = this.getYearDimension(startTime);
			}
			if (yd == null) {
				throw new BAMException("Adding year dimension failed: " + startTime.getBAMTimestamp());
			}
			summaryDimension.addQuarterDimension(BAMCalendar.getQuarter(startTime), null, yd.getId());
		} catch (Exception e) {
			throw new BAMException("Adding quarter dimension failed: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public void addMonthDimension(BAMCalendar startTime) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			QuarterDimension qd = this.getQuarterDimension(startTime);
			if (qd == null) {
				addQuarterDimension(startTime);
				qd = this.getQuarterDimension(startTime);
			}
			if (qd == null) {
				throw new BAMException("Adding quarter dimension failed: " + startTime.getBAMTimestamp());
			}
			summaryDimension.addMonthDimension(BAMCalendar.getMonth(startTime), null, qd.getId());
		} catch (Exception e) {
			throw new BAMException("Adding month dimension failed: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public void addDayDimension(BAMCalendar startTime) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();

			MonthDimension md = this.getMonthDimension(startTime);
			if (md == null) {
				addMonthDimension(startTime);
				md = this.getMonthDimension(startTime);
			}
			if (md == null) {
				throw new BAMException("Adding month dimension failed: " + startTime.getBAMTimestamp());
			}
			summaryDimension.addDayDimension(BAMCalendar.getDay(startTime), null, md.getId());
		} catch (Exception e) {
			throw new BAMException("Adding day dimension failed: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public void addHourDimension(BAMCalendar startTime) throws BAMException {
        SummaryDimensionDSClient summaryDimension = null;
		try {
			summaryDimension = BAMUtil.getSummaryDimensionDSClient();
			DayDimension dd = this.getDayDimension(startTime);
			if (dd == null) {
				addDayDimension(startTime);
				dd = this.getDayDimension(startTime);
			}
			if (dd == null) {
				throw new BAMException("Adding hour dimension failed: " + startTime.getBAMTimestamp());
			}
			summaryDimension.addHourDimension(BAMCalendar.getHour(startTime), dd.getId());
		} catch (Exception e) {
			throw new BAMException("Adding hour dimension failed: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (summaryDimension != null) {
			    summaryDimension.cleanup();
			}
		}
	}

	public SummaryStatistic getOperationStatHourlySummary(int opId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
            SummaryStatistic stat = serviceSummaryDSClient.getOperationStatHourlySummary(opId, startTime, endTime);

            BAMCalendar prevHourStartTime = BAMCalendar.getInstance(startTime);
            prevHourStartTime.add(BAMCalendar.HOUR_OF_DAY, -24);
            BAMCalendar prevHourEndTime = BAMCalendar.getInstance(endTime);
            prevHourEndTime.add(BAMCalendar.HOUR_OF_DAY, -1);

            SummaryStatistic maxStatThisHour = serviceSummaryDSClient.getOperationMaxCounts(opId, startTime, endTime);
            SummaryStatistic maxStatPrevHour = serviceSummaryDSClient.getOperationMaxCounts(opId, prevHourStartTime, prevHourEndTime);

            int val;
            val = maxStatThisHour.getReqCount() - maxStatPrevHour.getReqCount();
            stat.setReqCount(val >= 0 ? val : maxStatThisHour.getReqCount());

            if (maxStatThisHour.getReqCount() <= 0) {
            	stat.setAllZeros();
            } else {
            val = maxStatThisHour.getResCount() - maxStatPrevHour.getResCount();
            stat.setResCount(val >= 0 ? val : maxStatThisHour.getResCount());

            val = maxStatThisHour.getFaultCount() - maxStatPrevHour.getFaultCount();
            stat.setFaultCount(val >= 0? val : maxStatThisHour.getFaultCount());
            }
			return stat;
		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for operation: " + opId
					+ " start time: " + startTime.getBAMTimestamp(), e);
		}  finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getOperationStatDailySummary(int opId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getOperationStatDailySummary(opId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for operation: " + opId
					+ " start time: " + startTime.getBAMTimestamp(), e);
		}  finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getOperationStatMonthlySummary(int opId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getOperationStatMonthlySummary(opId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for operation: " + opId
					+ " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getOperationStatQuarterlySummary(int opId, BAMCalendar startTime,
			BAMCalendar endTime) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getOperationStatQuarterlySummary(opId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for operation: " + opId
					+ " start time: " + startTime.getBAMTimestamp(), e);
		}  finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public SummaryStatistic getOperationStatYearlySummary(int opId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			return serviceSummaryDSClient.getOperationStatYearlySummary(opId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for operation: " + opId
					+ " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addOperationStatHourlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addOperationStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly operation summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addOperationStatDailySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addOperationStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly operation summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addOperationStatMonthlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addOperationStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly operation summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addOperationStatQuarterlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addOperationStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly operation summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	public void addOperationStatYearlySummary(SummaryStatistic stat) throws BAMException {
        BAMServiceSummaryDSClient serviceSummaryDSClient = null;
		try {
            serviceSummaryDSClient = BAMUtil.getBAMServiceSummaryDSClient();
			serviceSummaryDSClient.addOperationStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly operation summary stat failed", e);
		} finally {
			if (serviceSummaryDSClient != null) {
			    serviceSummaryDSClient.cleanup();
			}
		}
	}

	// ======================= ENDPOINT ========================

	private String createMediationKeyString(String mediationType, String direction, String dataType,
			String name) {
		return mediationType + direction + dataType + "-" + name;
	}

	public MediationSummaryStatistic getEndpointStatHourlySummary(int serverId, String endpoint,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
		BAMSummaryGenerationDSClient client = null;
		try {
			client = BAMUtil.getBAMSummaryGenerationDSClient();;
			String endpointString;
			double[] doubles;
            int[] intsThisHour;
            int[] intsPrevHour;
			double doubleVal;
			int intVal;

			MediationSummaryStatistic statistic = new MediationSummaryStatistic();

			endpointString = createMediationKeyString(ENDPOINT, direction, MAX_PROCESSING_TIME, endpoint);
			doubles = client
					.getMediationStatHourlySummaryDouble(serverId, endpointString, startTime, endTime);
			doubleVal = BAMMath.max(doubles);
			statistic.setMaxProcessingTime(doubleVal);

			endpointString = createMediationKeyString(ENDPOINT, direction, MIN_PROCESSING_TIME, endpoint);
			doubles = client
					.getMediationStatHourlySummaryDouble(serverId, endpointString, startTime, endTime);
			doubleVal = BAMMath.min(doubles);
			statistic.setMinProcessingTime(doubleVal);

			endpointString = createMediationKeyString(ENDPOINT, direction, AVG_PROCESSING_TIME, endpoint);
			doubles = client.getMediationStatHourlySummaryDouble(serverId, endpointString, startTime, endTime);
			doubleVal = BAMMath.avg(doubles);
			statistic.setAvgProcessingTime(doubleVal);

//			endpointString = createMediationKeyString(ENDPOINT, direction, CUMULATIVE_COUNT, endpoint);
//			ints = client.getMediationStatHourlySummaryInt(serverId, endpointString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setCount(intVal);
//
//			endpointString = createMediationKeyString(ENDPOINT, direction, FAULT_COUNT, endpoint);
//			ints = client.getMediationStatHourlySummaryInt(serverId, endpointString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setFaultCount(intVal);

			BAMCalendar prevStartTime = BAMCalendar.getInstance(startTime);
			prevStartTime.add(BAMCalendar.HOUR, -24);
            BAMCalendar prevEndTime = BAMCalendar.getInstance(endTime);
            prevEndTime.add(BAMCalendar.HOUR, -1);

			endpointString = createMediationKeyString(ENDPOINT, direction, CUMULATIVE_COUNT, endpoint);
            intsThisHour = client.getDataForMediationMaxCount(serverId, endpointString, startTime, endTime);
          	intsPrevHour = client.getDataForMediationMaxCount(serverId, endpointString, prevStartTime, prevEndTime);

            intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
			statistic.setCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));

			if (BAMMath.max(intsThisHour) <= 0){
				statistic.setAllZeros();
			} else {
			endpointString = createMediationKeyString(ENDPOINT, direction, FAULT_COUNT, endpoint);
	            intsThisHour = client.getDataForMediationMaxCount(serverId, endpointString, startTime, endTime);
	            intsPrevHour = client.getDataForMediationMaxCount(serverId, endpointString, prevStartTime, prevEndTime);
				intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
				statistic.setFaultCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));
			}

			return statistic;

		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for serverId: " + serverId
					+ "endpoint: " + endpoint + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteServerEndpointUserData(int serverId, String endpoint, String direction,
			BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
			client = BAMUtil.getBAMSummaryGenerationDSClient();
			String endpointString;

			endpointString = createMediationKeyString(ENDPOINT, direction, MAX_PROCESSING_TIME, endpoint);
			client.deleteServerUserData(serverId, endpointString, startTime, endTime);

			endpointString = createMediationKeyString(ENDPOINT, direction, MIN_PROCESSING_TIME, endpoint);
			client.deleteServerUserData(serverId, endpointString, startTime, endTime);

			endpointString = createMediationKeyString(ENDPOINT, direction, AVG_PROCESSING_TIME, endpoint);
			client.deleteServerUserData(serverId, endpointString, startTime, endTime);

			endpointString = createMediationKeyString(ENDPOINT, direction, CUMULATIVE_COUNT, endpoint);
			client.deleteServerUserData(serverId, endpointString, startTime, endTime);

			endpointString = createMediationKeyString(ENDPOINT, direction, FAULT_COUNT, endpoint);
			client.deleteServerUserData(serverId, endpointString, startTime, endTime);

		} catch (Exception e) {
			throw new BAMException("Could not delete hourly summary for serverId: " + serverId + "endpoint: "
					+ endpoint + direction + " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getEndpointStatDailySummary(int serverId, String endpoint,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
			client = BAMUtil.getBAMSummaryGenerationDSClient();
			return client.getEndpointStatDailySummary(serverId, endpoint, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for serverId: " + serverId
					+ "endpoint: " + endpoint + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getEndpointStatMonthlySummary(int serverId, String endpoint,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
			client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getEndpointStatMonthlySummary(serverId, endpoint, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for serverId: " + serverId
					+ "endpoint: " + endpoint + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getEndpointStatQuarterlySummary(int serverId, String endpoint,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			return client.getEndpointStatQuarterlySummary(serverId, endpoint, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for serverId: " + serverId
					+ "endpoint: " + endpoint + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getEndpointStatYearlySummary(int serverId, String endpoint,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			return client.getEndpointStatYearlySummary(serverId, endpoint, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for serverId: " + serverId
					+ "endpoint: " + endpoint + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addEndpointStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addEndpointStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addEndpointStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addEndpointStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addEndpointStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addEndpointStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addEndpointStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addEndpointStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addEndpointStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addEndpointStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	// ======================= PROXY ========================

	public MediationSummaryStatistic getProxyServiceStatHourlySummary(int serverId, String proxyService,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			String proxyServiceString;
			double[] doubles;
            int[] intsThisHour;
            int[] intsPrevHour;
			double doubleVal;
			int intVal;

			MediationSummaryStatistic statistic = new MediationSummaryStatistic();

			proxyServiceString = PROXY_SERVICE + direction + MAX_PROCESSING_TIME + "-" + proxyService;
			doubles = client.getMediationStatHourlySummaryDouble(serverId, proxyServiceString, startTime,
					endTime);
			doubleVal = BAMMath.max(doubles);
			statistic.setMaxProcessingTime(doubleVal);

			proxyServiceString = PROXY_SERVICE + direction + MIN_PROCESSING_TIME + "-" + proxyService;
			doubles = client.getMediationStatHourlySummaryDouble(serverId, proxyServiceString, startTime,
					endTime);
			doubleVal = BAMMath.min(doubles);
			statistic.setMinProcessingTime(doubleVal);

			proxyServiceString = PROXY_SERVICE + direction + AVG_PROCESSING_TIME + "-" + proxyService;
			doubles = client.getMediationStatHourlySummaryDouble(serverId, proxyServiceString, startTime,
					endTime);
			doubleVal = BAMMath.avg(doubles);
			statistic.setAvgProcessingTime(doubleVal);

//			proxyServiceString = PROXY_SERVICE + direction + CUMULATIVE_COUNT + "-" + proxyService;
//			ints = client.getMediationStatHourlySummaryInt(serverId, proxyServiceString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setCount(intVal);
//
//			proxyServiceString = PROXY_SERVICE + direction + FAULT_COUNT + "-" + proxyService;
//			ints = client.getMediationStatHourlySummaryInt(serverId, proxyServiceString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setFaultCount(intVal);

			BAMCalendar prevStartTime = BAMCalendar.getInstance(startTime);
			prevStartTime.add(BAMCalendar.HOUR, -24);
            BAMCalendar prevEndTime = BAMCalendar.getInstance(endTime);
            prevEndTime.add(BAMCalendar.HOUR, -1);

            proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, CUMULATIVE_COUNT, proxyService);
            intsThisHour = client.getDataForMediationMaxCount(serverId, proxyServiceString, startTime, endTime);
            intsPrevHour = client.getDataForMediationMaxCount(serverId, proxyServiceString, prevStartTime, prevEndTime);

            intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
            statistic.setCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));

			if (BAMMath.max(intsThisHour) <= 0){
				statistic.setAllZeros();
			} else {
	            proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, FAULT_COUNT, proxyService);
	            intsThisHour = client.getDataForMediationMaxCount(serverId, proxyServiceString, startTime, endTime);
	            intsPrevHour = client.getDataForMediationMaxCount(serverId, proxyServiceString, prevStartTime, prevEndTime);
	            intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
	            statistic.setFaultCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));
            }

			return statistic;

		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for serverId: " + serverId
					+ "proxyService: " + proxyService + direction + " start time: "
					+ startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteServerProxyServiceUserData(int serverId, String proxyService, String direction,
			BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			String proxyServiceString;

			proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, MAX_PROCESSING_TIME,
					proxyService);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, MIN_PROCESSING_TIME,
					proxyService);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, AVG_PROCESSING_TIME,
					proxyService);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, CUMULATIVE_COUNT,
					proxyService);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(PROXY_SERVICE, direction, FAULT_COUNT, proxyService);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

		} catch (Exception e) {
			throw new BAMException(
					"Could not delete hourly summary for serverId: " + serverId + "proxyService: "
							+ proxyService + direction + " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getProxyServiceStatDailySummary(int serverId, String proxyService,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getProxyServiceStatDailySummary(serverId, proxyService, direction, startTime,
					endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for serverId: " + serverId
					+ "proxyService: " + proxyService + direction + " start time: "
					+ startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getProxyServiceStatMonthlySummary(int serverId, String proxyService,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getProxyServiceStatMonthlySummary(serverId, proxyService, direction, startTime,
					endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for serverId: " + serverId
					+ "proxyService: " + proxyService + direction + " start time: "
					+ startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getProxyServiceStatQuarterlySummary(int serverId, String proxyService,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getProxyServiceStatQuarterlySummary(serverId, proxyService, direction, startTime,
					endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for serverId: " + serverId
					+ "proxyService: " + proxyService + direction + " start time: "
					+ startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getProxyServiceStatYearlySummary(int serverId, String proxyService,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getProxyServiceStatYearlySummary(serverId, proxyService, direction, startTime,
					endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for serverId: " + serverId
					+ "proxyService: " + proxyService + direction + " start time: "
					+ startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addProxyServiceStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addProxyServiceStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addProxyServiceStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addProxyServiceStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addProxyServiceStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addProxyServiceStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addProxyServiceStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addProxyServiceStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addProxyServiceStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addProxyServiceStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	// ======================= SEQUENCE ========================

	public MediationSummaryStatistic getSequenceStatHourlySummary(int serverId, String sequence,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			String sequenceString;
			double[] doubles;
            int[] intsThisHour;
            int[] intsPrevHour;
			double doubleVal;
			int intVal;

			MediationSummaryStatistic statistic = new MediationSummaryStatistic();

			sequenceString = SEQUENCE + direction + MAX_PROCESSING_TIME + "-" + sequence;
			doubles = client
					.getMediationStatHourlySummaryDouble(serverId, sequenceString, startTime, endTime);
			doubleVal = BAMMath.max(doubles);
			statistic.setMaxProcessingTime(doubleVal);

			sequenceString = SEQUENCE + direction + MIN_PROCESSING_TIME + "-" + sequence;
			doubles = client
					.getMediationStatHourlySummaryDouble(serverId, sequenceString, startTime, endTime);
			doubleVal = BAMMath.min(doubles);
			statistic.setMinProcessingTime(doubleVal);

			sequenceString = SEQUENCE + direction + AVG_PROCESSING_TIME + "-" + sequence;
			doubles = client
					.getMediationStatHourlySummaryDouble(serverId, sequenceString, startTime, endTime);
			doubleVal = BAMMath.avg(doubles);
			statistic.setAvgProcessingTime(doubleVal);

//			sequenceString = SEQUENCE + direction + CUMULATIVE_COUNT + "-" + sequence;
//			ints = client.getMediationStatHourlySummaryInt(serverId, sequenceString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setCount(intVal);
//
//			sequenceString = SEQUENCE + direction + FAULT_COUNT + "-" + sequence;
//			ints = client.getMediationStatHourlySummaryInt(serverId, sequenceString, startTime, endTime);
//			intVal = BAMMath.max(ints) - BAMMath.min(ints);
//			statistic.setFaultCount(intVal);

			BAMCalendar prevStartTime = BAMCalendar.getInstance(startTime);
			prevStartTime.add(BAMCalendar.HOUR, -24);
			BAMCalendar prevEndTime = BAMCalendar.getInstance(endTime);
            prevEndTime.add(BAMCalendar.HOUR, -1);

            sequenceString = createMediationKeyString(SEQUENCE, direction, CUMULATIVE_COUNT, sequence);
            intsThisHour = client.getDataForMediationMaxCount(serverId, sequenceString, startTime, endTime);
            intsPrevHour = client.getDataForMediationMaxCount(serverId, sequenceString, prevStartTime, prevEndTime);

            intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
            statistic.setCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));

			if (BAMMath.max(intsThisHour) <= 0){
				statistic.setAllZeros();
			} else {
	            sequenceString = createMediationKeyString(SEQUENCE, direction, FAULT_COUNT, sequence);
	            intsThisHour = client.getDataForMediationMaxCount(serverId, sequenceString, startTime, endTime);
	            intsPrevHour = client.getDataForMediationMaxCount(serverId, sequenceString, prevStartTime, prevEndTime);
	            intVal = BAMMath.max(intsThisHour) - BAMMath.max(intsPrevHour);
	            statistic.setFaultCount(intVal >=0 ? intVal : BAMMath.max(intsThisHour));
			}
			return statistic;

		} catch (Exception e) {
			throw new BAMException("Could not retrieve hourly summary for serverId: " + serverId
					+ "sequence: " + sequence + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteServerSequenceUserData(int serverId, String sequence, String direction,
			BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			String proxyServiceString;

			proxyServiceString = createMediationKeyString(SEQUENCE, direction, MAX_PROCESSING_TIME, sequence);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(SEQUENCE, direction, MIN_PROCESSING_TIME, sequence);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(SEQUENCE, direction, AVG_PROCESSING_TIME, sequence);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(SEQUENCE, direction, CUMULATIVE_COUNT, sequence);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

			proxyServiceString = createMediationKeyString(SEQUENCE, direction, FAULT_COUNT, sequence);
			client.deleteServerUserData(serverId, proxyServiceString, startTime, endTime);

		} catch (Exception e) {
			throw new BAMException("Could not delete hourly summary for serverId: " + serverId + "sequence: "
					+ sequence + direction + " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getSequenceStatDailySummary(int serverId, String sequence,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getSequenceStatDailySummary(serverId, sequence, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve daily summary for serverId: " + serverId
					+ "sequence: " + sequence + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getSequenceStatMonthlySummary(int serverId, String sequence,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getSequenceStatMonthlySummary(serverId, sequence, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve monthly summary for serverId: " + serverId
					+ "sequence: " + sequence + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getSequenceStatQuarterlySummary(int serverId, String sequence,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getSequenceStatQuarterlySummary(serverId, sequence, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve quarterly summary for serverId: " + serverId
					+ "sequence: " + sequence + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public MediationSummaryStatistic getSequenceStatYearlySummary(int serverId, String sequence,
			String direction, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();

			return client.getSequenceStatYearlySummary(serverId, sequence, direction, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not retrieve yearly summary for serverId: " + serverId
					+ "sequence: " + sequence + direction + " start time: " + startTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addSequenceStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addSequenceStatHourlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addSequenceStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addSequenceStatDailySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding hourly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addSequenceStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addSequenceStatMonthlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding monthly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addSequenceStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addSequenceStatQuarterlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding quarterly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void addSequenceStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.addSequenceStatYearlySummary(stat);
		} catch (Exception e) {
			throw new BAMException("Adding yearly mediation summary stat failed", e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteServerData(int serverId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.deleteServerData(serverId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not delete hourly summary for serverId: " + serverId
					+ " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteServiceData(int serviceId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.deleteServiceData(serviceId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not delete hourly summary for serviceId: " + serviceId
					+ " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	public void deleteOperationData(int operationId, BAMCalendar startTime, BAMCalendar endTime)
			throws BAMException {
        BAMSummaryGenerationDSClient client = null;
		try {
            client = BAMUtil.getBAMSummaryGenerationDSClient();
			client.deleteOperationData(operationId, startTime, endTime);
		} catch (Exception e) {
			throw new BAMException("Could not delete hourly summary for operationId: " + operationId
					+ " end time: " + endTime.getBAMTimestamp(), e);
		} finally {
			if (client != null) {
			    client.cleanup();
			}
		}
	}

	/*
	 * Server level User Defined Data Eventing MessageReceiver can process the messages that contain sever
	 * level user defined data that has key/value pairs defined by users to capture custom data to be stored
	 * with BAM database. Expected message format:
	 *
	 * (01) <svrusrdata:Event xmlns:svrusrdata="http://wso2.org/bam/server/user-defined/data"> (02)
	 * <svrusrdata:ServerUserDefinedData> (03)
	 * <svrusrdata:ServerName>http://127.0.0.1:8280</svrusrdata:ServerName> (04) <svrusrdata:Data> (05)
	 * <svrusrdata:Key>EndpointInMaxProcessingTime-simple</svrusrdata:Key> (06)
	 * <svrusrdata:Value>15</svrusrdata:Value> (07) </svrusrdata:Data> (08) <svrusrdata:Data> (09)
	 * <svrusrdata:Key>EndpointInAvgProcessingTime-simple</svrusrdata:Key> (10)
	 * <svrusrdata:Value>15.0</svrusrdata:Value> (11) </svrusrdata:Data> (12) <svrusrdata:Data> (13)
	 * <svrusrdata:Key>EndpointInMinProcessingTime-simple</svrusrdata:Key> (14)
	 * <svrusrdata:Value>15</svrusrdata:Value> (15) </svrusrdata:Data> (16) <svrusrdata:Data> (17)
	 * <svrusrdata:Key>EndpointInCount-simple</svrusrdata:Key> (18) <svrusrdata:Value>1</svrusrdata:Value>
	 * (19) </svrusrdata:Data> (20) <svrusrdata:Data> (21)
	 * <svrusrdata:Key>EndpointInFaultCount-simple</svrusrdata:Key> (22)
	 * <svrusrdata:Value>0</svrusrdata:Value> (23) </svrusrdata:Data> (24) <svrusrdata:Data> (25)
	 * <svrusrdata:Key>EndpointInID</svrusrdata:Key> (26) <svrusrdata:Value>simple</svrusrdata:Value> (27)
	 * </svrusrdata:Data> (28) <svrusrdata:Data> (29)
	 * <svrusrdata:Key>EndpointInCumulativeCount-simple</svrusrdata:Key> (30)
	 * <svrusrdata:Value>3</svrusrdata:Value> (31) </svrusrdata:Data> (32) <svrusrdata:Data> (33)
	 * <svrusrdata:Key>EndpointOutCumulativeCount-simple</svrusrdata:Key> (34)
	 * <svrusrdata:Value>0</svrusrdata:Value> (35) </svrusrdata:Data> (36) </svrusrdata:ServerUserDefinedData>
	 * (37) </svrusrdata:Event>
	 */
}