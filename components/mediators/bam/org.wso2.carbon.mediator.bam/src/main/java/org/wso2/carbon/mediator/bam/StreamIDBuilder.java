/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.bam;

import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;

import java.util.List;

/**
 * Builds the Stream ID at the stream initialization
 */
public class StreamIDBuilder {

    private List<Property> properties;
    private List<StreamEntry> streamEntries;

    public String createStreamID(String streamName, String streamVersion, String streamNickName,
                                 String streamDescription, List<Property> properties,
                                 List<StreamEntry> streamEntries){
        this.properties = properties;
        this.streamEntries = streamEntries;

        return "{" +
               "'" + BamMediatorConstants.NAME + "':'" + streamName + "'," +
               "'" + BamMediatorConstants.VERSION + "':'" + streamVersion + "'," +
               "'" + BamMediatorConstants.NICK_NAME + "':'" + streamNickName + "'," +
               "'" + BamMediatorConstants.DESCRIPTION + "':'" + streamDescription + "'," +
               "'" + BamMediatorConstants.CORRELATION_DATA + "':[" +
               "{'" + BamMediatorConstants.NAME + "':'" + BamMediatorConstants.ACTIVITY_ID + "','" +
               BamMediatorConstants.TYPE + "':'" + BamMediatorConstants.STRING + "'}" +
               "]," +
               "'" + BamMediatorConstants.META_DATA + "':[" +
               "{'" + BamMediatorConstants.NAME +"':'" + BamMediatorConstants.TENANT_ID + "','" +
               BamMediatorConstants.TYPE + "':'" + BamMediatorConstants.INT + "'}" +
               "]," +
               "'" + BamMediatorConstants.PAYLOAD_DATA + "':[" +
               this.getConstantStreamDefinitionString() +
               this.getPropertyStreamDefinitionString() +
               this.getEntityStreamDefinitionString() +
               "]" +
               "}";
    }

    private String getConstantStreamDefinitionString(){
        String[] nameStrings = new String[BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS -1];
        int i = 0;
        nameStrings[i++] = BamMediatorConstants.SERVICE_NAME;
        nameStrings[i++] = BamMediatorConstants.OPERATION_NAME;
        nameStrings[i++] = BamMediatorConstants.MSG_ID;
        nameStrings[i++] = BamMediatorConstants.REQUEST_RECEIVED_TIME;
        nameStrings[i++] = BamMediatorConstants.HTTP_METHOD;
        nameStrings[i++] = BamMediatorConstants.CHARACTER_SET_ENCODING;
        nameStrings[i++] = BamMediatorConstants.REMOTE_ADDRESS;
        nameStrings[i++] = BamMediatorConstants.TRANSPORT_IN_URL;
        nameStrings[i++] = BamMediatorConstants.MESSAGE_TYPE;
        nameStrings[i++] = BamMediatorConstants.REMOTE_HOST;
        nameStrings[i] = BamMediatorConstants.SERVICE_PREFIX;

        String outputString = "{'" + BamMediatorConstants.NAME + "':'" +
                              BamMediatorConstants.MSG_DIRECTION + "','" +
                              BamMediatorConstants.TYPE + "':'" +
                              BamMediatorConstants.STRING + "'}";

        for (String nameString : nameStrings) {
            outputString = outputString + ","
                           + this.getStreamDefinitionEntryString(nameString,
                                                                 BamMediatorConstants.STRING);
        }

        return outputString;
    }

    private String getPropertyStreamDefinitionString(){
        String propertyString = "";
        for (Property property : properties) {
            propertyString = propertyString + "," +
                             this.getStreamDefinitionEntryString(property.getKey(),
                                                                 BamMediatorConstants.STRING);
        }
        return propertyString;
    }

    private String getEntityStreamDefinitionString(){
        String entityString = "";
        for (StreamEntry streamEntry : streamEntries) {
            entityString = entityString + "," +
                           this.getStreamDefinitionEntryString(streamEntry.getName(),
                                                               streamEntry.getType());
        }
        return entityString;
    }

    private String getStreamDefinitionEntryString(String name, String type){
        return  "{'" + BamMediatorConstants.NAME + "':'" + name + "','" +
                BamMediatorConstants.TYPE + "':'" + type +"'}";
    }

}
