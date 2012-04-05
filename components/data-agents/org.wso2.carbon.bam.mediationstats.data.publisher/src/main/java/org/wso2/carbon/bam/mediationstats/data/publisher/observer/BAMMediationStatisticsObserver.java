/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.mediationstats.data.publisher.observer;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.bam.mediationstats.data.publisher.data.MediationData;
import org.wso2.carbon.bam.mediationstats.data.publisher.publish.DataPublisher;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.PublisherUtils;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.TenantMediationStatConfigData;
import org.wso2.carbon.mediation.statistics.MediationStatisticsObserver;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.wso2.carbon.mediation.statistics.TenantInformation;

import java.util.Map;

public class BAMMediationStatisticsObserver implements MediationStatisticsObserver,
                                                       TenantInformation {

    private static final Log log = LogFactory.getLog(BAMMediationStatisticsObserver.class);
    private AxisConfiguration axisConfiguration;
    private int tenantId = 0;

    public BAMMediationStatisticsObserver() {
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of BAM");
        }
    }

    @Override
    public void updateStatistics(MediationStatisticsSnapshot mediationStatisticsSnapshot) {

        StatisticsRecord update, entitySnapshot, categorySnapshot;

        update = mediationStatisticsSnapshot.getUpdate();

        Map<String, Object> errorMap = PublisherUtils.calculateErrorCounts(mediationStatisticsSnapshot);
        PublisherUtils.addErrorCategories(mediationStatisticsSnapshot, errorMap);

        try {
            updateStatisticsInternal(update, errorMap);
        } catch (Exception e) {
            log.error("failed to update statics from BAM publisher", e);
        }
    }

    private void updateStatisticsInternal(StatisticsRecord update, Map<String, Object> errorMap)
            throws Exception {
        int tenantID = getTenantId();
        Map<Integer, MediationStatConfig> tenantSpecificMediationStatConfig = TenantMediationStatConfigData.
                getTenantSpecificEventingConfigData();
        MediationStatConfig mediationStatConfig = tenantSpecificMediationStatConfig.get(tenantID);

        if (mediationStatConfig == null || !mediationStatConfig.isEnableMediationStats()) {
            return;
        }


        switch (update.getType()) {
            case SEQUENCE:
                processSequenceData(update, tenantID, errorMap);
                break;
            case PROXYSERVICE:
                processProxyData(update, tenantID, errorMap);
                break;
            case ENDPOINT:
                processEndpointData(update, tenantID, errorMap);
                break;
        }

    }

    private void processEndpointData(StatisticsRecord latestEntityRecord, int tenantID,
                                     Map<String, Object> errorMap) {
        if (latestEntityRecord == null) {
            return;
        }
        process(latestEntityRecord, errorMap, tenantID);
    }

    private void processProxyData(StatisticsRecord update, int tenantID,
                                  Map<String, Object> errorMap) {
        if (update == null) {
            return;
        }
        process(update, errorMap, tenantID);
    }

    private void processSequenceData(StatisticsRecord entitySnapshot, int tenantID,
                                     Map<String, Object> errorMap)
            throws Exception {

        if (entitySnapshot == null) {
            return;
        }

        process(entitySnapshot, errorMap, tenantID);
    }

    private void process(StatisticsRecord entitySnapshot, Map<String, Object> errorMap,
                         int tenantID) {
        MediationData mediationData = new MediationData();
        java.util.Date currentDate = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());

        String statTypePrefix;
        switch (entitySnapshot.getType()) {
            case PROXYSERVICE:
                statTypePrefix = "Proxy";
                break;
            case SEQUENCE:
                statTypePrefix = "Sequence";
                break;
            case ENDPOINT:
                statTypePrefix = "Endpoint";
                break;
            default:
                statTypePrefix = "Any";
        }
        String direction = entitySnapshot.isInStatistic() ?
                           MediationDataPublisherConstants.IN_STATISTIC :
                           MediationDataPublisherConstants.OUT_STATISTIC;

        mediationData.setStatsType(statTypePrefix);
        mediationData.setDirection(direction);
        mediationData.setResourceId(entitySnapshot.getResourceId());
        mediationData.setStatisticsRecord(entitySnapshot);
        mediationData.setTimestamp(timestamp);

        // if errorMap is available, send details abt errors.
        if (errorMap != null && !errorMap.isEmpty()) {
            mediationData.setErrorMap(errorMap);
        }

        DataPublisher.process(mediationData, tenantID);
    }

    @Override
    public void notifyTraceLogs(MessageTraceLog[] messageTraceLogs) {

    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }


    public AxisConfiguration getTenantAxisConfiguration() {
        return axisConfiguration;
    }

    public void setTenantAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }
}
