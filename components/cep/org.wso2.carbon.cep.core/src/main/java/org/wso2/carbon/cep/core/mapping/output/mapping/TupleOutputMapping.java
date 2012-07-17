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

package org.wso2.carbon.cep.core.mapping.output.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.List;

public class TupleOutputMapping extends OutputMapping {

    private static final Log log = LogFactory.getLog(TupleOutputMapping.class);

    private String streamId;
    private List<String> payloadDataProperties;
    private List<String> correlationDataProperties;
    private List<String> metaDataProperties;

    public Object convert(Object event) {
        try {
            return buildTupleEvent(event);
        } catch (CEPEventProcessingException e) {
            log.error("Error in accessing information from the output event to build the OM Element " + e);
        }
        return null;
    }

    private Event buildTupleEvent(Object event) throws CEPEventProcessingException {
        Event newEvent;
        if (event instanceof Event) {
            newEvent = (Event) event;
        } else {
            newEvent = new Event();
            newEvent.setStreamId(streamId);
        }
        if (metaDataProperties != null && metaDataProperties.size() > 0) {
            Object[] data = new Object[metaDataProperties.size()];
            for (int i = 0, metaDataPropertiesSize = metaDataProperties.size(); i < metaDataPropertiesSize; i++) {
                data[i] = getPropertyValue(event,  metaDataProperties.get(i));
            }
            (newEvent).setMetaData(data);
        }
        if (correlationDataProperties != null && correlationDataProperties.size() > 0) {
            Object[] data = new Object[correlationDataProperties.size()];
            for (int i = 0, metaDataPropertiesSize = correlationDataProperties.size(); i < metaDataPropertiesSize; i++) {
                data[i] = getPropertyValue(event, correlationDataProperties.get(i));
            }
            (newEvent).setCorrelationData(data);
        }
        //payload data has to processed last as all the current data is in the payload
        if (payloadDataProperties != null && payloadDataProperties.size() > 0) {
            Object[] data = new Object[payloadDataProperties.size()];
            for (int i = 0, metaDataPropertiesSize = payloadDataProperties.size(); i < metaDataPropertiesSize; i++) {
                data[i] = getPropertyValue(event, payloadDataProperties.get(i));
            }
            (newEvent).setPayloadData(data);
        }
        return newEvent;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public List<String> getPayloadDataProperties() {
        return payloadDataProperties;
    }

    public void setPayloadDataProperties(List<String> payloadDataProperties) {
        this.payloadDataProperties = payloadDataProperties;
    }

    public List<String> getCorrelationDataProperties() {
        return correlationDataProperties;
    }

    public void setCorrelationDataProperties(List<String> correlationDataProperties) {
        this.correlationDataProperties = correlationDataProperties;
    }

    public List<String> getMetaDataProperties() {
        return metaDataProperties;
    }

    public void setMetaDataProperties(List<String> metaDataProperties) {
        this.metaDataProperties = metaDataProperties;
    }
}
