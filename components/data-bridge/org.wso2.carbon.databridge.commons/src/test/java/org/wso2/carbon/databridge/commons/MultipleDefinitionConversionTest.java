package org.wso2.carbon.databridge.commons;

import junit.framework.Assert;
import org.json.JSONException;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

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


//        StreamDefinition convertedStreamDefinition1 =
//                EventDefinitionConverterUtils.convertFromJson(definition1);
//        StreamDefinition convertedstreamDefinition2 =
//                EventDefinitionConverterUtils.convertFromJson(definition2);
//
//        List<StreamDefinition> convertedEventDefns = new ArrayList<StreamDefinition>();
//        convertedEventDefns.add(convertedStreamDefinition1);
//        convertedEventDefns.add(convertedstreamDefinition2);

        List<StreamDefinition> actualStreamDefinitions =
                EventDefinitionConverterUtils.convertMultipleEventDefns(multipleDefns);
        StreamDefinition actualStreamDefinition1 = actualStreamDefinitions.get(0);
        StreamDefinition actualStreamDefinition2 = actualStreamDefinitions.get(1);

//        System.out.println(gson.toJson(streamDefinition1));

        // add stream defns as otherwise they will be generated into a unique value
        StreamDefinition expectedEventDefinitions1 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0",
                        actualStreamDefinition1.getStreamId());
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
        StreamDefinition expectedEventDefinitions2 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0",
                        actualStreamDefinition2.getStreamId());
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

        List<StreamDefinition> expectedEventDefns = new ArrayList<StreamDefinition>();
        expectedEventDefns.add(expectedEventDefinitions1);
        expectedEventDefns.add(expectedEventDefinitions2);

        Assert.assertEquals(actualStreamDefinitions, expectedEventDefns);

    }

    private String combineJSONEventDefinitons() {
        return "["+ definition1 + ", " + definition2 + "]";
    }

    @Test
    public void multipleStreamDefnToJSON() throws MalformedStreamDefinitionException, JSONException {
       StreamDefinition actualStreamDefinition1 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");

//        List<Attribute> meta = new ArrayList<Attribute>(1);
//        meta.add(new Attribute("ipAdd", AttributeType.STRING));
//        actualStreamDefinition1.setMetaData(meta);
//        List<Attribute> payload = new ArrayList<Attribute>(5);
//        payload.add(new Attribute("symbol", AttributeType.STRING));
//        payload.add(new Attribute("price", AttributeType.DOUBLE));
//        payload.add(new Attribute("volume", AttributeType.INT));
//        payload.add(new Attribute("max", AttributeType.DOUBLE));
//        payload.add(new Attribute("min", AttributeType.DOUBLE));
//        actualStreamDefinition1.setPayloadData(payload);
//        List<String> tags = new ArrayList<String>();
//        tags.add("foo");
//        tags.add("bar");
//        actualStreamDefinition1.setTags(tags);
//
//        StreamDefinition actualStreamDefinition2 =
//                new StreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0");
//        List<Attribute> meta2 = new ArrayList<Attribute>(1);
//        meta2.add(new Attribute("ipAdd", AttributeType.STRING));
//        actualStreamDefinition2.setMetaData(meta2);
//        List<Attribute> payload2 = new ArrayList<Attribute>(5);
//        payload2.add(new Attribute("symbol", AttributeType.FLOAT));
//        payload2.add(new Attribute("price", AttributeType.FLOAT));
//        payload2.add(new Attribute("volume", AttributeType.STRING));
//        payload2.add(new Attribute("max", AttributeType.DOUBLE));
//        payload2.add(new Attribute("min", AttributeType.DOUBLE));
//        actualStreamDefinition2.setPayloadData(payload2);
//        List<Attribute> correlation2 = new ArrayList<Attribute>(5);
//        correlation2.add(new Attribute("symbol", AttributeType.STRING));
//        correlation2.add(new Attribute("price", AttributeType.FLOAT));
//        actualStreamDefinition2.setCorrelationData(correlation2);
//        actualStreamDefinition2.setTags(tags);
//
//        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
//        streamDefinitions.add(actualStreamDefinition1);
//        streamDefinitions.add(actualStreamDefinition2);
//
//        String actualJSONEventDefinitions = EventDefinitionConverterUtils.convertToJson(streamDefinitions);
//
//        // inject stream ids from the actual definitions, as they will be unique otherwise
//        JSONObject jsonDefn1 = new JSONObject(definition1);
//        jsonDefn1.put(EBCommonsConstants.STREAM_ID, actualStreamDefinition1.getStreamId());
//        JSONObject jsonDefn2 = new JSONObject(definition2);
//        jsonDefn2.put(EBCommonsConstants.STREAM_ID, actualStreamDefinition2.getStreamId());
//
//
//        assertEquals(new JSONArray(actualJSONEventDefinitions), (new JSONArray()).put(jsonDefn1).put(jsonDefn2));
    }



}
