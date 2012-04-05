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
package org.wso2.carbon.rule.engine.drools;

import org.drools.builder.ResourceType;
import org.wso2.carbon.rule.core.RuleConstants;

import java.util.Map;

/**
 * Default ResourceTypeDetection logic implementation
 */
public class DefaultResourceTypeDetectionStrategy implements ResourceTypeDetectionStrategy {

    public ResourceType getResourceType(Map<String, Object> properties) {

        if (!properties.containsKey(RuleConstants.SOURCE)) {
            return ResourceType.XDRL;
        }
        String type = (String) properties.get(RuleConstants.SOURCE);
        if (type == null) {
            return ResourceType.XDRL;
        }
        type = type.trim();
        return ResourceType.getResourceType(type.toUpperCase());
    }
}
