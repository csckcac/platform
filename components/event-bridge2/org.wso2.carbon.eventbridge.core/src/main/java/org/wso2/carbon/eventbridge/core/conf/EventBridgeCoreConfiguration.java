/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.eventbridge.core.conf;

import org.wso2.carbon.agent.internal.utils.AgentConstants;

/**
 * configuration details related to EventReceiver
 */
public class EventBridgeCoreConfiguration {
    private String streamDefinitionStoreName = AgentConstants.DEFAULT_DEFINITION_STORE;


    public void setStreamDefinitionStoreName(String eventDefStoreName){
        streamDefinitionStoreName = eventDefStoreName;
    }

    public String getStreamDefinitionStoreName(){
        return streamDefinitionStoreName;
    }
}
