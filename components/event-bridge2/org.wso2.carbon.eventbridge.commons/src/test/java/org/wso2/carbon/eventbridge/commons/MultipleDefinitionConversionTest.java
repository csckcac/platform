package org.wso2.carbon.eventbridge.commons;

import junit.framework.Assert;
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
    @Test
    public void multipleDefnConversion()
            throws MalformedStreamDefinitionException {
        String definition1 = "{" +
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

        String definition2 = "{" +
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


        EventStreamDefinition convertedEventStreamDefinition1 =
                EventDefinitionConverterUtils.convertFromJson(definition1);
        EventStreamDefinition convertedeventStreamDefinition2 =
                EventDefinitionConverterUtils.convertFromJson(definition2);

        List<EventStreamDefinition> convertedEventDefns = new ArrayList<EventStreamDefinition>();
        convertedEventDefns.add(convertedEventStreamDefinition1);
        convertedEventDefns.add(convertedeventStreamDefinition2);

//        System.out.println(gson.toJson(eventStreamDefinition1));

        EventStreamDefinition actualEventStreamDefinition1 =
                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0",
                        convertedEventStreamDefinition1.getStreamId());
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        actualEventStreamDefinition1.setMetaData(meta);
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("max", AttributeType.DOUBLE));
        payload.add(new Attribute("min", AttributeType.DOUBLE));
        actualEventStreamDefinition1.setPayloadData(payload);

        EventStreamDefinition actualEventStreamDefinition2 =
                new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0",
                        convertedeventStreamDefinition2.getStreamId());
        List<Attribute> meta2 = new ArrayList<Attribute>(1);
        meta2.add(new Attribute("ipAdd", AttributeType.STRING));
        actualEventStreamDefinition2.setMetaData(meta2);
        List<Attribute> payload2 = new ArrayList<Attribute>(5);
        payload2.add(new Attribute("symbol", AttributeType.FLOAT));
        payload2.add(new Attribute("price", AttributeType.FLOAT));
        payload2.add(new Attribute("volume", AttributeType.STRING));
        payload2.add(new Attribute("max", AttributeType.DOUBLE));
        payload2.add(new Attribute("min", AttributeType.DOUBLE));
        actualEventStreamDefinition2.setPayloadData(payload2);
        List<Attribute> correlation2 = new ArrayList<Attribute>(5);
        correlation2.add(new Attribute("symbol", AttributeType.STRING));
        correlation2.add(new Attribute("price", AttributeType.FLOAT));
        actualEventStreamDefinition2.setCorrelationData(correlation2);

        List<EventStreamDefinition> actualEventDefns = new ArrayList<EventStreamDefinition>();
        actualEventDefns.add(actualEventStreamDefinition1);
        actualEventDefns.add(actualEventStreamDefinition2);

        Assert.assertEquals(convertedEventDefns, actualEventDefns);

    }
}
