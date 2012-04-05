/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.usage.summary.generator;

import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.summary.generators.SummaryGenerator;
import org.wso2.carbon.bam.core.summary.generators.SummaryGeneratorFactory;
import org.wso2.carbon.usage.summary.generator.client.UsageSummaryGeneratorClient;

/**
 *
 */
public class MeteringSummaryGeneratorFactory implements SummaryGeneratorFactory {
    private UsageSummaryGeneratorClient client;
    
    public MeteringSummaryGeneratorFactory(UsageSummaryGeneratorClient client){
        this.client = client;
    }
    
    /* 
     * Creates an instance of Metering Summary Generator
     */
    public SummaryGenerator getSummaryGenerator(ServerDO server, int timeInterval) {
        return new MeteringSummaryGenerator(server, timeInterval, client);
    }
}
