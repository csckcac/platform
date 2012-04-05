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
package org.wso2.carbon.bam.data.publisher.mediationstats.observer;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.mediationstats.MDPublisherConstants;
import org.wso2.carbon.bam.data.publisher.mediationstats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.mediationstats.config.MediationStatConfig;
import org.wso2.carbon.bam.data.publisher.mediationstats.services.BAMMediationStatsPublisherAdmin;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;
import org.wso2.carbon.mediation.statistics.MediationStatisticsObserver;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.wso2.carbon.mediation.statistics.TenantInformation;

import java.util.Hashtable;
import java.util.Map;

/**
 * Receives statistics updates from the mediation statistics component and fires the relevant event on meeting
 * the user defined thresholds
 */
public class BAMMediationStatisticsObserver implements MediationStatisticsObserver, TenantInformation {

    private Hashtable<String, Integer> lastSequenceInCounts = new Hashtable<String, Integer>();
    private Hashtable<String, Integer> lastProxyInCounts = new Hashtable<String, Integer>();
    private Hashtable<String, Integer> lastEndpointInCounts = new Hashtable<String, Integer>();

    private Hashtable<String, Integer> lastSequenceOutCounts = new Hashtable<String, Integer>();
    private Hashtable<String, Integer> lastProxyOutCounts = new Hashtable<String, Integer>();
    private Hashtable<String, Integer> lastEndpointOutCounts = new Hashtable<String, Integer>();

    private AxisConfiguration axisConfiguration;
    private MediationStatConfig eventConfig;
    private int tenantId = 0;

    private static final int NO_TENANT_MODE = -1;


    private static final Log log = LogFactory.getLog(BAMMediationStatisticsObserver.class);

    public BAMMediationStatisticsObserver() {
        eventConfig = PublisherUtils.getMediationStatPublisherAdmin().getEventingConfigData();
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of BAM");
        }
    }

    /**
     * Updating Mediation statistics which are received from mediation-statistics service
     *
     * @param mediationStatisticsSnapshot
     */
    public void updateStatistics(MediationStatisticsSnapshot mediationStatisticsSnapshot) {
        StatisticsRecord update, entitySnapshot, categorySnapshot;

        update = mediationStatisticsSnapshot.getUpdate();
        entitySnapshot = mediationStatisticsSnapshot.getEntitySnapshot();
        categorySnapshot = mediationStatisticsSnapshot.getCategorySnapshot();

        Map<String, Object> errorMap = PublisherUtils.calculateErrorCounts(mediationStatisticsSnapshot);
        PublisherUtils.addErrorCategories(mediationStatisticsSnapshot, errorMap);

        try {
            updateStatisticsInternal(update, entitySnapshot, categorySnapshot, errorMap);
        } catch (Exception e) {
            log.error("failed to update statics from BAM publisher", e);
        }
    }

    private void updateStatisticsInternal(StatisticsRecord update, StatisticsRecord entitySnapshot,
                                          StatisticsRecord categorySnapshot, Map<String, Object> errorMap) throws Exception {
        int tenantID = this.getTenantId();
        BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin = PublisherUtils.getMediationStatPublisherAdmin();
        eventConfig = bamMediationStatsPublisherAdmin.getEventingConfigData();
        if (eventConfig == null || !eventConfig.eventingEnabled()) {
            return;
        }

        StatisticsRecord latestEntityRecord, latestCategoryRecord;
        if (entitySnapshot == null) {
            latestEntityRecord = update;
        } else {
            latestEntityRecord = new StatisticsRecord(entitySnapshot);
            latestEntityRecord.updateRecord(update);
        }

        if (categorySnapshot == null) {
            latestCategoryRecord = update;
        } else {
            latestCategoryRecord = new StatisticsRecord(categorySnapshot);
            latestCategoryRecord.updateRecord(update);
        }

        switch (latestCategoryRecord.getType()) {
            case SEQUENCE:
                processSequenceData(latestEntityRecord, tenantID);
                break;
            case PROXYSERVICE:
                processProxyData(latestEntityRecord, tenantID);
                break;
            case ENDPOINT:
                processEndpointData(latestEntityRecord, tenantID);
                break;
        }

        // fire an Event containing the set of errors, if errorMap is available.
        if (errorMap != null && !errorMap.isEmpty()) {
            OMElement errorMapMessage = PublisherUtils.getEventPayload(errorMap, this.getTenantAxisConfiguration(), tenantID);

            if (errorMapMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Firing errorMap Event: " + errorMapMessage.toString());
                }
                fireEvent(errorMapMessage);
            }

        }

    }

    private void processSequenceData(StatisticsRecord entitySnapshot, int tenantID) throws Exception {

        int sequenceInRequestCountCurrent = 0, sequenceInRequestCountLast = 0, sequenceOutRequestCountCurrent = 0,
                sequenceOutRequestCountLast = 0;

        eventConfig = PublisherUtils.getMediationStatPublisherAdmin().getEventingConfigData();
        if (eventConfig.getSequenceRequestCountThreshold() <= 0) {
            return;
        }

        if (entitySnapshot.isInStatistic()) {
            sequenceInRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastSequenceInCounts.get(entitySnapshot.getResourceId()) != null) {
                sequenceInRequestCountLast = lastSequenceInCounts.get(entitySnapshot.getResourceId());
            }
        } else {

            sequenceOutRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastSequenceOutCounts.get(entitySnapshot.getResourceId()) != null) {
                sequenceOutRequestCountLast = lastSequenceOutCounts.get(entitySnapshot.getResourceId());
            }
        }

        int inDiff = sequenceInRequestCountCurrent - sequenceInRequestCountLast;
        int outDiff = sequenceOutRequestCountCurrent - sequenceOutRequestCountLast;

        if (inDiff > eventConfig.getSequenceRequestCountThreshold()) {
            // If the difference exceeds the threshold we need to fire the events
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastSequenceInCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.SEQUENCE " + statMessage.toString());
                }
                fireEvent(statMessage);
            }
        }
        if (outDiff > eventConfig.getSequenceRequestCountThreshold()) {
            // If the difference exceeds the threshold we need to fire the events
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastSequenceOutCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.SEQUENCE " + statMessage.toString());
                }
                fireEvent(statMessage);
            }
        }
    }

    private void processProxyData(StatisticsRecord entitySnapshot, int tenantID) throws Exception {
        int proxyInRequestCountCurrent = 0, proxyInRequestCountLast = 0, proxyOutRequestCountCurrent = 0,
                proxyOutRequestCountLast = 0;

        BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin = PublisherUtils.getMediationStatPublisherAdmin();
        if (bamMediationStatsPublisherAdmin != null) {
            eventConfig = bamMediationStatsPublisherAdmin.getEventingConfigData();
            if (eventConfig.getProxyRequestCountThreshold() <= 0) {
                return;
            }
        }


        if (entitySnapshot.isInStatistic()) {
            proxyInRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastProxyInCounts.get(entitySnapshot.getResourceId()) != null) {
                proxyInRequestCountLast = lastProxyInCounts.get(entitySnapshot.getResourceId());
            }
        }

        if (!entitySnapshot.isInStatistic()) {
            proxyOutRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastProxyOutCounts.get(entitySnapshot.getResourceId()) != null) {
                proxyOutRequestCountLast = lastProxyOutCounts.get(entitySnapshot.getResourceId());
            }
        }

        int inDiff = proxyInRequestCountCurrent - proxyInRequestCountLast;
        int outDiff = proxyOutRequestCountCurrent - proxyOutRequestCountLast;

        if (inDiff > eventConfig.getProxyRequestCountThreshold()) {
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastProxyInCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.PROXYSERVICE " + statMessage.toString());
                }
                fireEvent(statMessage);

            }

        }
        if (outDiff > eventConfig.getProxyRequestCountThreshold()) {
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastProxyOutCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.PROXYSERVICE " + statMessage.toString());
                }
                fireEvent(statMessage);

            }

        }
    }

    private void processEndpointData(StatisticsRecord entitySnapshot, int tenantID) throws Exception {
        int endpointInRequestCountCurrent = 0, endpointInRequestCountLast = 0, endpointOutRequestCountCurrent = 0,
                endpointOutRequestCountLast = 0;
        BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin = PublisherUtils.getMediationStatPublisherAdmin();
        if (bamMediationStatsPublisherAdmin != null) {
            eventConfig = bamMediationStatsPublisherAdmin.getEventingConfigData();
            if (eventConfig.getEndpointRequestCountThreshold() <= 0) {
                return;
            }
        }


        if (entitySnapshot.isInStatistic()) {
            endpointInRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastEndpointInCounts.get(entitySnapshot.getResourceId()) != null) {
                endpointInRequestCountLast = lastEndpointInCounts.get(entitySnapshot.getResourceId());
            }
        }

        if (!entitySnapshot.isInStatistic()) {
            endpointOutRequestCountCurrent = entitySnapshot.getTotalCount();
            if (lastEndpointOutCounts.get(entitySnapshot.getResourceId()) != null) {
                endpointOutRequestCountLast = lastEndpointOutCounts.get(entitySnapshot.getResourceId());
            }
        }

        int inDiff = endpointInRequestCountCurrent - endpointInRequestCountLast;
        int outDiff = endpointOutRequestCountCurrent - endpointOutRequestCountLast;

        if (inDiff > eventConfig.getEndpointRequestCountThreshold()) {
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastEndpointInCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.ENDPOINT " + statMessage.toString());
                }
                fireEvent(statMessage);

            }
        }
        if (outDiff > eventConfig.getEndpointRequestCountThreshold()) {
            OMElement statMessage = PublisherUtils.getEventPayload(entitySnapshot, this.getTenantAxisConfiguration(), tenantID);
            lastEndpointOutCounts.put(entitySnapshot.getResourceId(), entitySnapshot.getTotalCount());
            if (statMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("ComponentType.ENDPOINT " + statMessage.toString());
                }
                fireEvent(statMessage);

            }
        }
    }

    private void fireEvent(OMElement statMessage) {
        try {

            int tenantId = NO_TENANT_MODE;
            LightWeightEventBrokerInterface broker = org.wso2.carbon.bam.data.publisher.mediationstats.ServiceHolder.getLWEventBroker();
            try {
                SuperTenantCarbonContext.startTenantFlow();

                // This check is for integration tests where no ConfigurationContextService would be present
                if (PublisherUtils.getConfigurationContextService() != null) {
                   tenantId = SuperTenantCarbonContext.getCurrentContext(PublisherUtils.
                        getConfigurationContextService().getServerConfigContext()).getTenantId();
                }

                SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
                SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

                LightWeightEventBrokerInterface lwBroker = org.wso2.carbon.bam.data.publisher.mediationstats.ServiceHolder.getLWEventBroker();
                lwBroker.publish(MDPublisherConstants.BAM_REG_PATH,
                        statMessage);

            } finally {
                SuperTenantCarbonContext.endTenantFlow();
            }

//            try {
//                if (broker != null) {
//                    broker.publish(message, MDPublisherConstants.BAM_REG_PATH);
//
//                    if (log.isDebugEnabled()) {
//                        log.debug("Event is published" + message.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("EventGenerator - Unable to publish event", e);
//            }
        } catch (Throwable e) {
            // We catch throwable here because even if this handler fails, it should not affect the original
            // message.
            // TODO: should ignore
            e.printStackTrace();
        }
    }

    public void notifyTraceLogs(MessageTraceLog[] logs) {
        // TODO Auto-generated method stub

    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int i) {
        tenantId = i;
    }

    public void setTenantAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;

    }

    public AxisConfiguration getTenantAxisConfiguration() {
        return axisConfiguration;
    }
}

