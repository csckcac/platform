/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rule.mediator;

/**
 *
 */
public class RuleMediatorSerializationTest extends AbstractTestCase {

    private RuleMediatorFactory factory;
    private RuleMediatorSerializer serializer;

    public RuleMediatorSerializationTest() {
        factory = new RuleMediatorFactory();
        serializer = new RuleMediatorSerializer();
    }

    public void testRuleMediatorSerializationScenarioOne() throws Exception {

        String inputXml = "<syn:rule xmlns:syn=\"http://ws.apache.org/ns/synapse\">" +
                "<syn:ruleset uri=\"some uri\"><syn:source key=\"keyof script\" />" +
                "<syn:creation><syn:property xmlns:c=\"http://services.samples/xsd\" " +
                "name=\"propertytwo\" expression=\"//c:xpath\" />" +
                "<syn:property name=\"propertyOne\" value=\"one\" /></syn:creation>" +
                "<syn:registration><syn:property xmlns:c=\"http://services.samples/xsd\" " +
                "name=\"propertytwo\" expression=\"//c:xpath\" />" +
                "<syn:property name=\"propertyOne\" value=\"one\" /></syn:registration>" +
                "<syn:deregistration><syn:property xmlns:c=\"http://services.samples/xsd\"" +
                " name=\"propertytwo\" expression=\"//c:xpath\" /><syn:property name=\"propertyOne\"" +
                " value=\"one\" /></syn:deregistration></syn:ruleset>" +
                "<syn:session type=\"stateful\"><syn:property xmlns:c=\"http://services.samples/xsd\"" +
                " name=\"propertytwo\" expression=\"//c:xpath\" />" +
                "<syn:property name=\"propertyOne\" value=\"one\" /></syn:session><syn:facts>" +
                "<syn:fact xmlns:c=\"http://services.samples/xsd\" name=\"DomEvent\" expression=\"//c:xpath\" type=\"dom\" />" +
                "<syn:fact name=\"InputTwo\" type=\"A.Example\"/>" +
                "<syn:fact name=\"inputOone\" value=\"12\" type=\"int\" /></syn:facts>" +
                "<syn:results><syn:result xmlns:c=\"http://services.samples/xsd\" " +
                "name=\"DomEvent\" expression=\"//c:xpath\" type=\"dom\" /><syn:result name=\"InputTwo\"" +
                " type=\"A.Example\"/><syn:result name=\"inputOone\" key=\"keyone\" type=\"int\" /></syn:results>" +
                "<syn:childMediators><syn:send /></syn:childMediators></syn:rule>";
        assertTrue(serialization(inputXml, factory, serializer));
    }

}
