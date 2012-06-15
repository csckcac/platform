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
package org.wso2.carbon.eventbridge.test.thrift;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.eventbridge.commons.Attribute;
import org.wso2.carbon.eventbridge.commons.AttributeType;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverter;

import java.util.ArrayList;
import java.util.List;

public class DefinitionConversionTest {

    private static Gson gson;

    @BeforeClass
    public static void init() {
        gson = new Gson();
    }

    @Test
    public void testDefinitionConversion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
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


        EventStreamDefinition eventStreamDefinition1 = EventDefinitionConverter.convertFromJson(definition);
        Assert.assertTrue(null != eventStreamDefinition1.getStreamId());
//        System.out.println(gson.toJson(eventStreamDefinition1));

        EventStreamDefinition eventStreamDefinition2 = new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        eventStreamDefinition2.setMetaData(meta);
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("max", AttributeType.DOUBLE));
        payload.add(new Attribute("min", AttributeType.DOUBLE));
        eventStreamDefinition2.setPayloadData(payload);

        Assert.assertEquals(eventStreamDefinition1, eventStreamDefinition2);
    }

    @Test
    public void testDefinitionConversionWithoutVersion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
                            "  'name':'org.wso2.esb.MediatorStatistics'," +
//                            "  'version':'2.3.0'," +
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


        EventStreamDefinition eventStreamDefinition1 = EventDefinitionConverter.convertFromJson(definition);
        Assert.assertTrue(null != eventStreamDefinition1.getStreamId());
//        System.out.println(gson.toJson(eventStreamDefinition1));

        EventStreamDefinition eventStreamDefinition2 = new EventStreamDefinition("org.wso2.esb.MediatorStatistics", "1.0.0");
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        eventStreamDefinition2.setMetaData(meta);
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("max", AttributeType.DOUBLE));
        payload.add(new Attribute("min", AttributeType.DOUBLE));
        eventStreamDefinition2.setPayloadData(payload);

        Assert.assertEquals(eventStreamDefinition1, eventStreamDefinition2);
    }
}
