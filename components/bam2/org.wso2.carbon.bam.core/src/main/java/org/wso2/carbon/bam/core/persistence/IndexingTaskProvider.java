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
package org.wso2.carbon.bam.core.persistence;

import org.wso2.carbon.bam.core.configurations.IndexConfiguration;
import org.wso2.carbon.bam.core.configurations.IndexingTaskConfiguration;
import org.wso2.carbon.bam.core.persistence.exceptions.IndexingException;

public interface IndexingTaskProvider {
    
    public void scheduleIndexingTask(IndexConfiguration configuration,
                                     IndexingTaskConfiguration taskConfiguration) throws
                                                                                  IndexingException;

    // Deletes a scheduled indexing task. For current AnalyzerEngine task provider implementation
    // only taskName and tenantId information are required to be populated in provided
    // taskConfiguration parameter
    public void unscheduleIndexingTask(IndexingTaskConfiguration taskConfiguration)
            throws IndexingException;
    
}
