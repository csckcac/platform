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

package org.wso2.carbon.cep.core.internal.process;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.cep.core.XpathDefinition;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestInputMapping extends TestCase {

    public void testXMLGetValue() throws XMLStreamException, CEPEventProcessingException {
        String xmlString = "<statdata:ServiceStatisticsDataEvent xmlns:statdata=\"http://wso2.org/ns/2009/09/bam/service/statistics/data\">\n" +
                           "    <statdata:ServiceStatisticsData>\n" +
                           "        <statdata:ServerName>http://127.0.0.1:9763</statdata:ServerName>\n" +
                           "        <statdata:AveageResponseTime>16.4</statdata:AveageResponseTime>\n" +
                           "        <statdata:MinimumResponseTime>0</statdata:MinimumResponseTime>\n" +
                           "        <statdata:MaximumResponseTime>109</statdata:MaximumResponseTime>\n" +
                           "        <statdata:RequestCount>23</statdata:RequestCount>\n" +
                           "        <statdata:ResponseCount>20</statdata:ResponseCount>\n" +
                           "        <statdata:FaultCount>120</statdata:FaultCount>\n" +
                           "        <statdata:ServiceName>HelloService</statdata:ServiceName>\n" +
                           "        <statdata:OperationName>greet</statdata:OperationName>\n" +
                           "    </statdata:ServiceStatisticsData>\n" +
                           "</statdata:ServiceStatisticsDataEvent>";

        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(new ByteArrayInputStream(xmlString.getBytes()));
        OMElement omElement = stAXOMBuilder.getDocumentElement();

        ArrayList<XpathDefinition> xpathDefinitionsList = new ArrayList<XpathDefinition>();
        XpathDefinition xpathDefinition = new XpathDefinition();
        xpathDefinition.setPrefix("statdata");
        xpathDefinition.setNamespace("http://wso2.org/ns/2009/09/bam/service/statistics/data");
        xpathDefinitionsList.add(xpathDefinition);

        Object value = XMLInputMapping.getValue(omElement,
                                                "//statdata:ServiceStatisticsData/statdata:RequestCount",
                                                "java.lang.Integer",
                                                xpathDefinitionsList);
        Assert.assertTrue(value.equals(new Integer(23)));

    }

    public void testTupleToTupleInputConversion() throws CEPEventProcessingException {
        //Event
        Event event = new Event();
        event.setStreamId("TestStream");
        event.setMetaData(new Object[]{"10.0.0.8"});
        event.setPayloadData(new Object[]{"IBM", 145.4, 500});

        //TypeDef
        StreamDefinition eventStreamDefinition = new StreamDefinition("TestStream");
        List<Attribute> metaDataList = new ArrayList<Attribute>();
        metaDataList.add(new Attribute("ipAdd", AttributeType.STRING));
        eventStreamDefinition.setMetaData(metaDataList);

        List<Attribute> payloadDataList = new ArrayList<Attribute>();
        payloadDataList.add(new Attribute("symbol", AttributeType.STRING));
        payloadDataList.add(new Attribute("price", AttributeType.DOUBLE));
        payloadDataList.add(new Attribute("volume", AttributeType.INT));
        eventStreamDefinition.setPayloadData(payloadDataList);

        //Input Mapping
        List<TupleProperty> tupleProperties = new ArrayList<TupleProperty>();
        TupleProperty tupleProperty = new TupleProperty();
        tupleProperty.setName("ipAdd");
        tupleProperty.setDataType(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_META);
        tupleProperty.setType(String.class.getName());
        tupleProperties.add(tupleProperty);

        TupleInputMapping tupleInputMapping = new TupleInputMapping();
        tupleInputMapping.setProperties(tupleProperties);

        //Set definition
        tupleInputMapping.setEventDefinition(eventStreamDefinition);

        //Convert event
        Object convertedEvent=tupleInputMapping.convert(event);
        
        Assert.assertTrue(convertedEvent instanceof Event);
        Assert.assertTrue(((Event)convertedEvent).getPayloadData()[0].equals("10.0.0.8"));

    }  
    
    public void testTupleToMapInputConversion() throws CEPEventProcessingException {
        //Event
        Event event = new Event();
        event.setStreamId("TestStream");
        event.setMetaData(new Object[]{"10.0.0.8"});
        event.setPayloadData(new Object[]{"IBM", 145.4, 500});

        //TypeDef
        StreamDefinition eventStreamDefinition = new StreamDefinition("TestStream");
        List<Attribute> metaDataList = new ArrayList<Attribute>();
        metaDataList.add(new Attribute("ipAdd", AttributeType.STRING));
        eventStreamDefinition.setMetaData(metaDataList);

        List<Attribute> payloadDataList = new ArrayList<Attribute>();
        payloadDataList.add(new Attribute("symbol", AttributeType.STRING));
        payloadDataList.add(new Attribute("price", AttributeType.DOUBLE));
        payloadDataList.add(new Attribute("volume", AttributeType.INT));
        eventStreamDefinition.setPayloadData(payloadDataList);

        //Input Mapping
        List<TupleProperty> tupleProperties = new ArrayList<TupleProperty>();
        TupleProperty tupleProperty = new TupleProperty();
        tupleProperty.setName("ipAdd");
        tupleProperty.setDataType(CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_META);
        tupleProperty.setType(String.class.getName());
        tupleProperties.add(tupleProperty);

        TupleInputMapping tupleInputMapping = new TupleInputMapping();
        tupleInputMapping.setProperties(tupleProperties);
        
        tupleInputMapping.setMappingClass(Map.class);

        //Set definition
        tupleInputMapping.setEventDefinition(eventStreamDefinition);

        //Convert event
        Object convertedEvent=tupleInputMapping.convert(event);
        
        Assert.assertTrue(convertedEvent instanceof Map);
        Assert.assertTrue(((Map)convertedEvent).get("ipAdd").equals("10.0.0.8"));

    }
}
