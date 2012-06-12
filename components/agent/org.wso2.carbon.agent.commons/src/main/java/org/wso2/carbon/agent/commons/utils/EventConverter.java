/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.agent.commons.utils;


import com.google.gson.Gson;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;

import java.util.List;

/**
 * the util class that converts Events and its definitions in to various forms
 */
public final class EventConverter {
    private static Gson gson = new Gson();

    private EventConverter() {

    }

    public static AttributeType[] generateAttributeTypeArray(List<Attribute> attributes) {
        if (attributes != null) {
            AttributeType[] attributeTypes = new AttributeType[attributes.size()];
            for (int i = 0, metaDataSize = attributes.size(); i < metaDataSize; i++) {
                Attribute attribute = attributes.get(i);
                attributeTypes[i] = attribute.getType();
            }
            return attributeTypes;
        } else {
            return null;  //to improve performance
        }
    }


    public static EventStreamDefinition convertFromJson(String streamDefinition)
            throws MalformedStreamDefinitionException {
        try {
            EventStreamDefinition tempEventStreamDefinition = gson.fromJson(streamDefinition.
                    replaceAll("(?i)int", "INT").replaceAll("(?i)long", "LONG").
                    replaceAll("(?i)float", "FLOAT").replaceAll("(?i)double", "DOUBLE").
                    replaceAll("(?i)bool", "BOOL").replaceAll("(?i)string", "STRING"), EventStreamDefinition.class);

            String name = tempEventStreamDefinition.getName();
            String version = tempEventStreamDefinition.getVersion();


            if (version == null) {
                version = "1.0.0";  //when populating the object using google gson the defaults are getting null values
            }
            if (name == null) {
                throw new MalformedStreamDefinitionException("stream name is null");
            }

            EventStreamDefinition eventStreamDefinition = new EventStreamDefinition(name, version);

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
