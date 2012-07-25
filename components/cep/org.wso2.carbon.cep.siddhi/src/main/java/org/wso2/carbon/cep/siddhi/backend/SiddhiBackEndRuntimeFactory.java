/*
 * Copyright 2004,2012 The Apache Software Foundation.
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

package org.wso2.carbon.cep.siddhi.backend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntimeFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.MapInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.XMLInputMapping;
import org.wso2.carbon.cep.core.mapping.property.Property;
import org.wso2.carbon.cep.siddhi.internal.ds.SiddhiBackendRuntimeValueHolder;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SiddhiBackEndRuntimeFactory implements CEPBackEndRuntimeFactory {

    private static final Log log = LogFactory.getLog(SiddhiBackEndRuntimeFactory.class);
    public static final String PERSISTENCE_SNAPSHOT_TIME_INTERVAL_MINUTES = "siddhi.persistence.snapshot.time.interval.minutes";

    public CEPBackEndRuntime createCEPBackEndRuntime(String bucketName,
                                                     Properties providerConfiguration,
                                                     List<InputMapping> mappings,
                                                     int tenantId)
            throws CEPConfigurationException {

        long persistenceTimeInterval = 1;
        if (providerConfiguration != null && providerConfiguration.size() > 0) {
            String timeString = providerConfiguration.getProperty(PERSISTENCE_SNAPSHOT_TIME_INTERVAL_MINUTES);
            try {
                persistenceTimeInterval = Long.parseLong(timeString);
            } catch (NumberFormatException e) {
                log.warn("Error reading siddhi persistence snapshot time interval, hence using " + persistenceTimeInterval + " min");
            }
        }
        SiddhiConfiguration siddhiConfig = new SiddhiConfiguration();
        siddhiConfig.setSingleThreading(true); //todo check which is good?
        siddhiConfig.setExecutionPlanIdentifier(bucketName);
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfig);
        siddhiManager.setPersistStore(SiddhiBackendRuntimeValueHolder.getInstance().getPersistenceStore());

        Map<String, InputHandler> siddhiInputHandlerMap = new HashMap<String, InputHandler>();

        for (InputMapping mapping : mappings) {

            StreamDefinition streamDefinition = new StreamDefinition();
            streamDefinition.name(mapping.getStream());

            List properties;
            if (mapping instanceof TupleInputMapping) {
                TupleInputMapping tupleInputMapping = (TupleInputMapping) mapping;
                properties = tupleInputMapping.getProperties();
            } else if (mapping instanceof MapInputMapping) {
                MapInputMapping mapInputMapping = (MapInputMapping) mapping;
                properties = mapInputMapping.getProperties();
            } else { //Xml mapping
                XMLInputMapping xmlInputMapping = (XMLInputMapping) mapping;
                properties = xmlInputMapping.getProperties();
            }
//            String[] attributeNames = new String[properties.size()];
//            Class[] attributeTypes = new Class[properties.size()];

            for (Object property1 : properties) {
                Property property = (Property) property1;
                streamDefinition.attribute(property.getName(), SiddhiBackEndRuntime.javaToSiddhiType.get(property.getType()));
            }


//            try {
            siddhiInputHandlerMap.put(mapping.getStream(), siddhiManager.defineStream(streamDefinition));
//            } catch (SiddhiException e) {
//                throw new CEPConfigurationException("Invalid input stream configuration for " +
//                                                    mapping.getStream(), e);
//            }
        }

//        try {
//            siddhiManager.init();
//        } catch (SiddhiException e) {
//            throw new CEPConfigurationException("Cannot init Siddhi Backend", e);
//        }
        return new SiddhiBackEndRuntime(bucketName, siddhiManager, siddhiInputHandlerMap, tenantId,persistenceTimeInterval);
    }
}
