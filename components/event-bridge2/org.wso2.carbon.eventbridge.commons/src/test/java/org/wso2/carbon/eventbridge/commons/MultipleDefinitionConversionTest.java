package org.wso2.carbon.eventbridge.commons;

import junit.framework.Assert;
import org.json.JSONException;
import org.junit.Test;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;

import java.util.ArrayList;
import java.util.List;

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
public class MultipleDefinitionConversionTest {
    private String definition1 = "{" +
            "  'name':'org.wso2.esb.MediatorStatistics'," +
            "  'version':'2.3.0'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'string'}," +
            "          {'name':'price','type':'double'}," +
            "          {'name':'volume','type':'int'}," +
            "          {'name':'max','type':'double'}," +
            "          {'name':'min','type':'double'}" +
            "  ]" +
            "}";
    private String definition2 = "{" +
            "  'name':'org.wso2.esb.MediatorStatistics'," +
            "  'version':'3.0.0'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'float'}," +
            "          {'name':'price','type':'float'}," +
            "          {'name':'volume','type':'string'}," +
            "          {'name':'max','type':'double'}," +
            "          {'name':'min','type':'double'}" +
            "  ]," +
            "  'correlationData':[" +
            "          {'name':'symbol','type':'string'}," +
            "          {'name':'price','type':'float'}" +
            "  ]" +
            "}";

    @Test
    public void multipleDefnConversionFromJSON()
            throws MalformedStreamDefinitionException {

        String multipleDefns = combineJSONEventDefinitons();


//        EventStreamDefinition convertedEventStreamDefinition1 =
//                EventDefinitionConverterUtils.convertFromJson(definition1);
//        EventStreamDefinition convertedeventStreamDefinition2 =
//                EventDefinitionConverterUtils.convertFromJson(definition2);
//
//        List<EventStreamDefinition> convertedEventDefns = new ArrayList<EventStreamDefinition>();
//        convertedEventDefns.add(convertedEventStreamDefinition1);
//        convertedEventDefns.add(convertedeventStreamDefinition2);

        List<EventStreamDefinition> actualEventStreamDefinitions =
                EventDefinitionConverterUtils.convertMultipleEventDefns(multipleDefns);
        EventStreamDefinition actualEventStreamDefinition1 = actualEventStreamDefinitions.get(0);
        EventStreamDefinition actualEventStreamDefinition2 = actualEventStreamDefinitions.get(1);

//        System.out.println(gson.toJson(eventStreamDefinition1));

        // add stream defns as otherwise they will be generated into a unique value
        EventStreamDefinition expectedEventDefinitions1 =
                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0",
                        actualEventStreamDefinition1.getStreamId());
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        expectedEventDefinitions1.setMetaData(meta);
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("max", AttributeType.DOUBLE));
        payload.add(new Attribute("min", AttributeType.DOUBLE));
        expectedEventDefinitions1.setPayloadData(payload);
        List<String> tags = new ArrayList<String>();
        tags.add("foo");
        tags.add("bar");
        expectedEventDefinitions1.setTags(tags);

        // add stream defns as otherwise they will be generated into a unique value
        EventStreamDefinition expectedEventDefinitions2 =
                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0",
                        actualEventStreamDefinition2.getStreamId());
        List<Attribute> meta2 = new ArrayList<Attribute>(1);
        meta2.add(new Attribute("ipAdd", AttributeType.STRING));
        expectedEventDefinitions2.setMetaData(meta2);
        List<Attribute> payload2 = new ArrayList<Attribute>(5);
        payload2.add(new Attribute("symbol", AttributeType.FLOAT));
        payload2.add(new Attribute("price", AttributeType.FLOAT));
        payload2.add(new Attribute("volume", AttributeType.STRING));
        payload2.add(new Attribute("max", AttributeType.DOUBLE));
        payload2.add(new Attribute("min", AttributeType.DOUBLE));
        expectedEventDefinitions2.setPayloadData(payload2);
        List<Attribute> correlation2 = new ArrayList<Attribute>(5);
        correlation2.add(new Attribute("symbol", AttributeType.STRING));
        correlation2.add(new Attribute("price", AttributeType.FLOAT));
        expectedEventDefinitions2.setCorrelationData(correlation2);
        expectedEventDefinitions2.setTags(tags);

        List<EventStreamDefinition> expectedEventDefns = new ArrayList<EventStreamDefinition>();
        expectedEventDefns.add(expectedEventDefinitions1);
        expectedEventDefns.add(expectedEventDefinitions2);

        Assert.assertEquals(actualEventStreamDefinitions, expectedEventDefns);

    }

    private String combineJSONEventDefinitons() {
        return "["+ definition1 + ", " + definition2 + "]";
    }

    @Test
    public void multipleEventStreamDefnToJSON() throws MalformedStreamDefinitionException, JSONException {
//       EventStreamDefinition actualEventStreamDefinition1 =
//                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
//
//        List<Attribute> meta = new ArrayList<Attribute>(1);
//        meta.add(new Attribute("ipAdd", AttributeType.STRING));
//        actualEventStreamDefinition1.setMetaData(meta);
//        List<Attribute> payload = new ArrayList<Attribute>(5);
//        payload.add(new Attribute("symbol", AttributeType.STRING));
//        payload.add(new Attribute("price", AttributeType.DOUBLE));
//        payload.add(new Attribute("volume", AttributeType.INT));
//        payload.add(new Attribute("max", AttributeType.DOUBLE));
//        payload.add(new Attribute("min", AttributeType.DOUBLE));
//        actualEventStreamDefinition1.setPayloadData(payload);
//        List<String> tags = new ArrayList<String>();
//        tags.add("foo");
//        tags.add("bar");
//        actualEventStreamDefinition1.setTags(tags);
//
//        EventStreamDefinition actualEventStreamDefinition2 =
//                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0");
//        List<Attribute> meta2 = new ArrayList<Attribute>(1);
//        meta2.add(new Attribute("ipAdd", AttributeType.STRING));
//        actualEventStreamDefinition2.setMetaData(meta2);
//        List<Attribute> payload2 = new ArrayList<Attribute>(5);
//        payload2.add(new Attribute("symbol", AttributeType.FLOAT));
//        payload2.add(new Attribute("price", AttributeType.FLOAT));
//        payload2.add(new Attribute("volume", AttributeType.STRING));
//        payload2.add(new Attribute("max", AttributeType.DOUBLE));
//        payload2.add(new Attribute("min", AttributeType.DOUBLE));
//        actualEventStreamDefinition2.setPayloadData(payload2);
//        List<Attribute> correlation2 = new ArrayList<Attribute>(5);
//        correlation2.add(new Attribute("symbol", AttributeType.STRING));
//        correlation2.add(new Attribute("price", AttributeType.FLOAT));
//        actualEventStreamDefinition2.setCorrelationData(correlation2);
//        actualEventStreamDefinition2.setTags(tags);
//
//        List<EventStreamDefinition> eventStreamDefinitions = new ArrayList<EventStreamDefinition>();
//        eventStreamDefinitions.add(actualEventStreamDefinition1);
//        eventStreamDefinitions.add(actualEventStreamDefinition2);
//
//        String actualJSONEventDefinitions = EventDefinitionConverterUtils.convertToJson(eventStreamDefinitions);
//
//        // inject stream ids from the actual definitions, as they will be unique otherwise
//        JSONObject jsonDefn1 = new JSONObject(definition1);
//        jsonDefn1.put(EBCommonsConstants.STREAM_ID, actualEventStreamDefinition1.getStreamId());
//        JSONObject jsonDefn2 = new JSONObject(definition2);
//        jsonDefn2.put(EBCommonsConstants.STREAM_ID, actualEventStreamDefinition2.getStreamId());
//
//
//        assertEquals(new JSONArray(actualJSONEventDefinitions), (new JSONArray()).put(jsonDefn1).put(jsonDefn2));
    }



}
