package org.wso2.carbon.eventbridge.core.utils;

import com.google.gson.Gson;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.MalformedStreamDefinitionException;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StreamDefnConverterUtils {

    private static Gson gson = new Gson();

    public static EventStreamDefinition convertFromJson(String streamDefinition)
            throws MalformedStreamDefinitionException {
        try {
            EventStreamDefinition tempEventStreamDefinition = gson.fromJson(streamDefinition.
                    replaceAll("(?i)int", "INT").replaceAll("(?i)long", "LONG").
                    replaceAll("(?i)float", "FLOAT").replaceAll("(?i)double", "DOUBLE").
                    replaceAll("(?i)bool", "BOOL").replaceAll("(?i)string", "STRING"), EventStreamDefinition.class);

            String name = tempEventStreamDefinition.getName();
            String version = tempEventStreamDefinition.getVersion();
            String streamId = tempEventStreamDefinition.getStreamId();


            if (version == null) {
                version = "1.0.0";  //when populating the object using google gson the defaults are getting null values
            }
            if (name == null) {
                throw new MalformedStreamDefinitionException("stream name is null");
            }

            EventStreamDefinition eventStreamDefinition = null;
            if (streamId == null) {
                eventStreamDefinition = new EventStreamDefinition(name, version);
            } else {
                eventStreamDefinition = new EventStreamDefinition(name, version, streamId);
            }

            eventStreamDefinition.setMetaData(tempEventStreamDefinition.getMetaData());
            eventStreamDefinition.setCorrelationData(tempEventStreamDefinition.getCorrelationData());
            eventStreamDefinition.setPayloadData(tempEventStreamDefinition.getPayloadData());

            eventStreamDefinition.setNickName(tempEventStreamDefinition.getNickName());
            eventStreamDefinition.setDescription(tempEventStreamDefinition.getDescription());
            eventStreamDefinition.setDescription(tempEventStreamDefinition.getDescription());
            eventStreamDefinition.setTags(tempEventStreamDefinition.getTags());
            return eventStreamDefinition;
        } catch (RuntimeException e) {
            throw new MalformedStreamDefinitionException(" Malformed stream definition " + streamDefinition, e);
        }
    }

    public static String convertToJson(EventStreamDefinition existingDefinition) {
        return gson.toJson(existingDefinition);
    }

}
