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
package org.wso2.carbon.bam.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.ClientUtil;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationDataDO;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationSummaryStatistic;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.BAMSummaryGenerationDSStub;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.MedStatValue;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.MedSummaryStat;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.SummaryTime;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.TimeStamp;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.Calendar;

public class BAMSummaryGenerationDSClient {
    private static final String BAM_SUMMARY_GENERATION_DS = "BAMSummaryGenerationDS";
    private static final Log log = LogFactory.getLog(BAMSummaryGenerationDSClient.class);
    private BAMSummaryGenerationDSStub summaryGenerationDSStub;

    public BAMSummaryGenerationDSClient(String backendServerURL,
                                        ConfigurationContext configCtx) throws BAMException {
        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_SUMMARY_GENERATION_DS);
            summaryGenerationDSStub = new BAMSummaryGenerationDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
    }

    public BAMSummaryGenerationDSClient(String cookie, String backendServerURL,
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

    public void cleanupTransports() {
        try {
            summaryGenerationDSStub._getServiceClient().cleanupTransport();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    //TODO: rename endpointString to mediationTypeString or something likewise.
    public double[] getMediationStatHourlySummaryDouble(int serverId, String endpointString, BAMCalendar startTime,
                                                        BAMCalendar endTime) throws BAMException {

        double[] valArr = new double[0];
        try {
            MedStatValue[] stats = summaryGenerationDSStub.getDataForMediationHourlySummary(serverId, endpointString,
                    startTime, endTime);


            if (stats != null) {
                valArr = new double[stats.length];
                for (int i = 0; i < stats.length; i++) {

                    if (stats[i] != null) {
                        try {
                            valArr[i] = Double.parseDouble(stats[i].getMedStatValue());
                        }
                        catch (NumberFormatException e) {
                            valArr[i] = 0;
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new BAMException("getMediationStatHourlySummaryDouble failed", e);
        }
        return valArr;
    }

    public int[] getDataForMediationMaxCount(int severId, String mediationString, BAMCalendar startTime, BAMCalendar endTime) throws BAMException {
        int[] valArr = new int[0];
        try {
            MedStatValue[] stats = summaryGenerationDSStub.getDataForMediationMaxCount(severId, mediationString, startTime, endTime);
            if (stats != null) {
                valArr = new int[stats.length];
                 for (int i = 0; i < stats.length; i++) {

                    if (stats[i] != null) {
                        try {
                            valArr[i] = Integer.parseInt(stats[i].getMedStatValue());
                        } catch (NumberFormatException e) {
                            valArr[i] = 0;
                        }
                    }
                 }
            }
        } catch (Exception e) {
            throw new BAMException("getDataForMediation failed", e);
        }
        return valArr;
    }

    public int[] getMediationStatHourlySummaryInt(int serverId, String endpointString, BAMCalendar startTime,
                                                  BAMCalendar endTime) throws BAMException {

        int[] valArr = new int[0];

        try {
            MedStatValue[] stats = summaryGenerationDSStub.getDataForMediationHourlySummary(serverId, endpointString,
                    startTime, endTime);
            if (stats != null) {
                valArr = new int[stats.length];
                for (int i = 0; i < stats.length; i++) {

                    if (stats[i] != null) {
                        try {
                            valArr[i] = Integer.parseInt(stats[i].getMedStatValue());
                        } catch (NumberFormatException e) {
                            valArr[i] = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BAMException("getMediationStatHourlySummaryInt failed", e);
        }
        return valArr;
    }

    public MediationSummaryStatistic getEndpointStatDailySummary(int serverId, String endpoint, String direction,
                                                                 BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getEndpointStatDailySummary(serverId, endpoint, direction, startTime,
                    endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getEndpointStatDailySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getEndpointStatMonthlySummary(int serverId, String endpoint, String direction,
                                                                   BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getEndpointStatMonthlySummary(serverId, endpoint, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getEndpointStatMonthlySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getEndpointStatQuarterlySummary(int serverId, String endpoint, String direction,
                                                                     BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();

        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getEndpointStatQuarterlySummary(serverId, endpoint, direction,
                    startTime, endTime);


            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getEndpointStatQuarterlySummary failed", e);
        }

        return msst;
    }

    public MediationSummaryStatistic getEndpointStatYearlySummary(int serverId, String endpoint, String direction,
                                                                  BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getEndpointStatYearlySummary(serverId, endpoint, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getEndpointStatYearlySummary failed", e);
        }

        return msst;
    }


    public void addEndpointStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addEndpointStatHourlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addEndpointStatHourlySummary failed", e);
        }

    }


    public void addEndpointStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addEndpointStatDailySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addEndpointStatDailySummary failed", e);
        }

    }

    public void addEndpointStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addEndpointStatMonthlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addEndpointStatMonthlySummary failed", e);
        }

    }

    public void addEndpointStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addEndpointStatQuarterlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addEndpointStatQuarterlySummary failed", e);
        }
    }

    public void addEndpointStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addEndpointStatYearlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addEndpointStatYearlySummary failed", e);
        }


    }

    public Calendar getLatestEndpointStatSummaryPeriod(int summaryPeriod, MediationDataDO endpoint) throws BAMException{
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
            case BAMCalendar.HOUR_OF_DAY:
                time = summaryGenerationDSStub.getLatestEndpointStatHourlySummaryHourId(
                		endpoint.getServerId(), endpoint.getName(), endpoint.getDirection());
                break;
            case BAMCalendar.DAY_OF_MONTH:
                time = summaryGenerationDSStub.getLatestEndpointStatDailySummaryDayId(
                		endpoint.getServerId(), endpoint.getName(), endpoint.getDirection());
                break;
            case BAMCalendar.MONTH:
                time = summaryGenerationDSStub.getLatestEndpointStatMonthlySummaryMonthId(
                		endpoint.getServerId(), endpoint.getName(), endpoint.getDirection());
                break;
            case BAMCalendar.QUATER:
                time = summaryGenerationDSStub.getLatestEndpointStatQuarterlySummaryQuarterId(
                		endpoint.getServerId(), endpoint.getName(), endpoint.getDirection());
                break;
            case BAMCalendar.YEAR:
                time = summaryGenerationDSStub.getLatestEndpointStatYearlySummaryYearId(
                		endpoint.getServerId(), endpoint.getName(), endpoint.getDirection());
                break;
            default:
                throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "server user data" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getServerUserDataMinimumPeriodId(endpoint.getServerId());
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                }else{
                    //We are running for the first time and there are no records in
                    //"server user data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }
            
        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for endpoint";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }


    //ProxyService


    public MediationSummaryStatistic getProxyServiceStatDailySummary(int serverId, String proxyService, String direction,
                                                                     BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();

        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getProxyStatDailySummary(serverId, proxyService, direction, startTime,
                    endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getProxyStatDailySummary failed", e);
        }

        return msst;
    }

    public MediationSummaryStatistic getProxyServiceStatMonthlySummary(int serverId, String proxyService, String direction,
                                                                       BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getProxyStatMonthlySummary(serverId, proxyService, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getProxyStatMonthlySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getProxyServiceStatQuarterlySummary(int serverId, String proxyService, String direction,
                                                                         BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getProxyStatQuarterlySummary(serverId, proxyService, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getProxyStatQuarterlySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getProxyServiceStatYearlySummary(int serverId, String proxyService, String direction,
                                                                      BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getProxyStatYearlySummary(serverId, proxyService, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getProxyStatYearlySummary failed", e);
        }

        return msst;
    }


    public void addProxyServiceStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addProxyStatHourlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addProxyStatHourlySummary failed", e);
        }

    }


    public void addProxyServiceStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addProxyStatDailySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addProxyStatDailySummary failed", e);
        }
    }

    public void addProxyServiceStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addProxyStatMonthlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addProxyStatMonthlySummary failed", e);
        }

    }

    public void addProxyServiceStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addProxyStatQuarterlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addProxyStatQuarterlySummary failed", e);
        }

    }

    public void addProxyServiceStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addProxyStatYearlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addProxyStatYearlySummary failed", e);
        }

    }

    public Calendar getLatestProxyServiceStatSummaryPeriod(int summaryPeriod, MediationDataDO proxyService) throws BAMException{
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
            case BAMCalendar.HOUR_OF_DAY:
                time = summaryGenerationDSStub.getLatestProxyStatHourlySummaryHourId(proxyService.getServerId(),
                		proxyService.getName(),proxyService.getDirection());
                break;
            case BAMCalendar.DAY_OF_MONTH:
                time = summaryGenerationDSStub.getLatestProxyStatDailySummaryDayId(proxyService.getServerId(),
                		proxyService.getName(), proxyService.getDirection());
                break;
            case BAMCalendar.MONTH:
                time = summaryGenerationDSStub.getLatestProxyStatMonthlySummaryMonthId(proxyService.getServerId(),
                		proxyService.getName(), proxyService.getDirection());
                break;
            case BAMCalendar.QUATER:
                time = summaryGenerationDSStub.getLatestProxyStatQuarterlySummaryQuarterId(proxyService.getServerId(),
                		proxyService.getName(), proxyService.getDirection());
                break;
            case BAMCalendar.YEAR:
                time = summaryGenerationDSStub.getLatestProxyStatYearlySummaryYearId(proxyService.getServerId(),
                		proxyService.getName(), proxyService.getDirection());
                break;
            default:
                throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "server user data" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getServerUserDataMinimumPeriodId(proxyService.getServerId());
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                }else{
                    //We are running for the first time and there are no records in
                    //"server user data". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }

        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for proxy service";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }

    //Sequence


    public MediationSummaryStatistic getSequenceStatDailySummary(int serverId, String sequence, String direction,
                                                                 BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getSequenceStatDailySummary(serverId, sequence, direction, startTime,
                    endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getSequenceStatDailySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getSequenceStatMonthlySummary(int serverId, String sequence, String direction,
                                                                   BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getSequenceStatMonthlySummary(serverId, sequence, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getSequenceStatMonthlySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getSequenceStatQuarterlySummary(int serverId, String sequence, String direction,
                                                                     BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getSequenceStatQuarterlySummary(serverId, sequence, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getSequenceStatQuarterlySummary failed", e);
        }
        return msst;
    }

    public MediationSummaryStatistic getSequenceStatYearlySummary(int serverId, String sequence, String direction,
                                                                  BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        MediationSummaryStatistic msst = new MediationSummaryStatistic();
        try {
            MedSummaryStat[] stat = summaryGenerationDSStub.getSequenceStatYearlySummary(serverId, sequence, direction,
                    startTime, endTime);

            if (stat != null) {
                MedSummaryStat mst = stat[0];
                if (mst != null) {
                    populateMedSummaryStatistic(mst, msst);
                }
            }
        } catch (Exception e) {
            throw new BAMException("getSequenceStatYearlySummary failed", e);
        }
        return msst;
    }


    public void addSequenceStatHourlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addSequenceStatHourlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addSequenceStatHourlySummary failed", e);
        }


    }


    public void addSequenceStatDailySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addSequenceStatDailySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addSequenceStatDailySummary failed", e);
        }

    }

    public void addSequenceStatMonthlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addSequenceStatMonthlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addSequenceStatMonthlySummary failed", e);
        }

    }

    public void addSequenceStatQuarterlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addSequenceStatQuarterlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addSequenceStatQuarterlySummary failed", e);
        }

    }

    public void addSequenceStatYearlySummary(MediationSummaryStatistic stat) throws BAMException {
        try {
            summaryGenerationDSStub.addSequenceStatYearlySummary(stat.getServerId(), stat.getTimeDimensionId(), stat.getName(),
                    stat.getDirection(), stat.getAvgProcessingTime(), stat.getMaxProcessingTime(),
                    stat.getMinProcessingTime(), stat.getCount(), stat.getFaultCount());
        } catch (Exception e) {
            throw new BAMException("addSequenceStatYearlySummary failed", e);
        }

    }

    public Calendar getLatestSequenceStatSummaryPeriod(int summaryPeriod, MediationDataDO sequence) throws BAMException{
        Calendar cal;

        try {
            SummaryTime[] time;
            switch (summaryPeriod) {
            case BAMCalendar.HOUR_OF_DAY:
                time = summaryGenerationDSStub.getLatestSequenceStatHourlySummaryHourId(sequence.getServerId(),
                		sequence.getName(), sequence.getDirection());
                break;
            case BAMCalendar.DAY_OF_MONTH:
                time = summaryGenerationDSStub.getLatestSequenceStatDailySummaryDayId(sequence.getServerId(),
                		sequence.getName(), sequence.getDirection());
                break;
            case BAMCalendar.MONTH:
                time = summaryGenerationDSStub.getLatestSequenceStatMonthlySummaryMonthId(sequence.getServerId(),
                		sequence.getName(), sequence.getDirection());
                break;
            case BAMCalendar.QUATER:
                time = summaryGenerationDSStub.getLatestSequenceStatQuarterlySummaryQuarterId(sequence.getServerId(),
                		sequence.getName(), sequence.getDirection());
                break;
            case BAMCalendar.YEAR:
                time = summaryGenerationDSStub.getLatestSequenceStatYearlySummaryYearId(sequence.getServerId(),
                		sequence.getName(), sequence.getDirection());
                break;
            default:
                throw new BAMException("Unexpected timeInterval");
            }

            cal = getTimeStampForId(summaryPeriod, time);

            if (cal == null) {
                //This is the first time we are running the summary. So, get the minimum time stamp
                //for this server from "server user dataobjects" table
                TimeStamp[] timeStamps = summaryGenerationDSStub.getServerUserDataMinimumPeriodId(sequence.getServerId());
                if (timeStamps != null && timeStamps[0] != null) {
                    //we are sure that there will be only one record.
                    cal = BAMCalendar.getInstance(timeStamps[0].getTimeStamp());
                }else{
                    //We are running for the first time and there are no records in
                    //"server user dataobjects". So, we can start from now
                    cal = BAMCalendar.getInstance();
                }
                //Start with last period so that it will include our intended period
                cal.add(summaryPeriod, -2);
            }

        } catch (Exception e) {
            String msg = "Unable to get LatestSummaryTime for sequence";
            log.error(msg);
            throw new BAMException(msg, e);
        }
        return cal;
    }

    private static void populateMedSummaryStatistic(MedSummaryStat mst, MediationSummaryStatistic msst) {
        if (mst.getAvgResTime() != null && mst.getAvgResTime().length() > 0) {
            msst.setAvgProcessingTime(Double.parseDouble(mst.getAvgResTime()));
        }

        if (mst.getMaxResTime() != null && mst.getMaxResTime().length() > 0) {
            msst.setMaxProcessingTime(Double.parseDouble(mst.getMaxResTime()));
        }

        if (mst.getMinResTime() != null && mst.getMinResTime().length() > 0) {
            msst.setMinProcessingTime(Double.parseDouble(mst.getMinResTime()));
        }

        if (mst.getReqCount() != null && mst.getReqCount().length() > 0) {
            msst.setCount(Integer.parseInt(mst.getReqCount()));
        }

        if (mst.getFaultCount() != null && mst.getFaultCount().length() > 0) {
            msst.setFaultCount(Integer.parseInt(mst.getFaultCount()));
        }
    }


    public void deleteServerData(int serverId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            summaryGenerationDSStub.deleteServerData(serverId, startTime, endTime);
        } catch (Exception e) {
            throw new BAMException("deleteServerData failed", e);
        }
    }

    public void deleteServiceData(int serviceId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            summaryGenerationDSStub.deleteServiceData(serviceId, startTime, endTime);
        } catch (Exception e) {
            throw new BAMException("deleteServiceData failed", e);
        }
    }

    public void deleteOperationData(int operationId, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            summaryGenerationDSStub.deleteOperationData(operationId, startTime, endTime);
        } catch (Exception e) {
            throw new BAMException("deleteOperationData failed", e);
        }
    }

    public void deleteServerUserData(int serverId, String mediationName, BAMCalendar startTime, BAMCalendar endTime)
            throws BAMException {
        try {
            summaryGenerationDSStub.deleteServerUserData(serverId, mediationName, startTime, endTime);
        } catch (Exception e) {
            throw new BAMException("deleteServerUserData failed", e);
        }
    }


    private Calendar getTimeStampForId(int summaryPeriod, SummaryTime[] time) throws Exception {
        Calendar cal = null;
        if (time != null && time[0] != null) {
            String idStr = time[0].getSummaryTime();
            int id = 0;

            try {
                id = Integer.parseInt(idStr);
            } catch (Exception e) {
                // We may be able to recover from this since we are getting the current time as the
                //  last resort in the calling method.
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

        }
        if (cal != null) {
            return BAMCalendar.getInstance(cal);
        } else {
            return null;
        }
    }
}