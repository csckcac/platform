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
package org.wso2.carbon.bam.analyzer.analyzers.configs;

import java.util.HashMap;
import java.util.Map;

public class AggregationMeasure {

    private String name;

    private AggregationType aggregationType;

    private String fieldType;

    private Map<String, AggregationType> aggregationTypes = new HashMap<String, AggregationType>(){
        {
            put("SUM", AggregationType.SUM);
            put("MAX", AggregationType.MAX);
            put("MIN", AggregationType.MIN);
            put("CUMULATIVE", AggregationType.CUMULATIVE);
            put("AVG", AggregationType.AVG);
        }
    };

    public AggregationMeasure(String name, String aggregationType, String fieldType) {
        this.setName(name);
        this.setAggregationType(aggregationType.toUpperCase());
        this.setFieldType(fieldType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationTypes.get(aggregationType);
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
