/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bam.integration.test.datacollection.mediation.mockobjects;

import org.apache.synapse.aspects.statistics.ErrorLog;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;

import java.util.List;

public class MockMediationStatisticsSnapshot extends MediationStatisticsSnapshot {

    private org.wso2.carbon.mediation.statistics.StatisticsRecord update;
    private org.wso2.carbon.mediation.statistics.StatisticsRecord entitySnapshot;
    private org.wso2.carbon.mediation.statistics.StatisticsRecord categorySnapshot;
    private List<org.apache.synapse.aspects.statistics.ErrorLog> errorLogs;


    public StatisticsRecord getUpdate() {
        return update;
    }

    public void setUpdate(StatisticsRecord update) {
        this.update = update;
    }

    public StatisticsRecord getEntitySnapshot() {
        return entitySnapshot;
    }

    public void setEntitySnapshot(StatisticsRecord entitySnapshot) {
        this.entitySnapshot = entitySnapshot;
    }

    public StatisticsRecord getCategorySnapshot() {
        return categorySnapshot;
    }

    public void setCategorySnapshot(StatisticsRecord categorySnapshot) {
        this.categorySnapshot = categorySnapshot;
    }

    public List<ErrorLog> getErrorLogs() {
        return errorLogs;
    }

    public void setErrorLogs(List<ErrorLog> errorLogs) {
        this.errorLogs = errorLogs;
    }

}
