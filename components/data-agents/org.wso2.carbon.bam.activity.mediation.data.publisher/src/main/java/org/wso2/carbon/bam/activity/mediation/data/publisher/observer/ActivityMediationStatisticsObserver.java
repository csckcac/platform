/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.activity.mediation.data.publisher.observer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.ComponentType;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.mediation.statistics.MediationStatisticsObserver;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;

public class ActivityMediationStatisticsObserver implements MediationStatisticsObserver {

    private static final Log log = LogFactory.getLog(ActivityMediationStatisticsObserver.class);

    public ActivityMediationStatisticsObserver() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing the activity mediation statistics observer");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateStatistics(MediationStatisticsSnapshot mediationStatisticsSnapshot) {

    }

    @Override
    public void notifyTraceLogs(MessageTraceLog[] messageTraceLogs) {
        for (MessageTraceLog traceLog : messageTraceLogs) {
            if (ComponentType.PROXYSERVICE.equals(traceLog.getType())) {
                continue;
            }

            Object activityId = traceLog.getProperties().get(BAMDataPublisherConstants.MSG_ACTIVITY_ID);
            if (activityId == null || "".equals(activityId)) {
                continue;
            }
            ActivityPublisherUtils.publishEvent(traceLog);
        }
    }
}
