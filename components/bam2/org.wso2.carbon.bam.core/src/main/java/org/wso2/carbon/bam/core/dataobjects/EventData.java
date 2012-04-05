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
package org.wso2.carbon.bam.core.dataobjects;

import java.nio.ByteBuffer;
import java.util.Map;

public class EventData {
    
    private Map<String, ByteBuffer> metaData;
    private Map<String, ByteBuffer> correlationData;
    private Map<String, ByteBuffer> eventData;

    private Map<String, String> credentials;

    public  Map<String, ByteBuffer> getMetaData() {
        return metaData;
    }
    
    public Map<String, ByteBuffer> getCorrelationData() {
        return correlationData;
    }
    
    public Map<String, ByteBuffer> getEventData() {
        return eventData;
    }

    public void setMetaData(Map<String, ByteBuffer> metaData) {
        this.metaData = metaData;
    }

    public void setCorrelationData(Map<String, ByteBuffer> correlationData) {
        this.correlationData = correlationData;
    }

    public void setEventData(Map<String, ByteBuffer> eventData) {
        this.eventData = eventData;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }
}
