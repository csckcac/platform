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

package org.wso2.carbon.cep.core.internal.config.output;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.output.mapping.TupleOutputMapping;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class will help to build the Tuple Output Object from a given OMELement
 */
public class TupleOutputMappingHelper {

    private static final Log log = LogFactory.getLog(TupleOutputMappingHelper.class);


    public static TupleOutputMapping fromOM(OMElement tupleMappingElement) {

        TupleOutputMapping tupleOutputMapping = new TupleOutputMapping();
        Iterator<OMElement> iterator = tupleMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                         CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_META));

        if (iterator.hasNext()) {
            tupleOutputMapping.setMetaDataProperties(generatePropertyList(iterator.next()));
        }
        iterator = tupleMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                     CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_CORRELATION));

        if (iterator.hasNext()) {
            tupleOutputMapping.setCorrelationDataProperties(generatePropertyList(iterator.next()));
        }
        iterator = tupleMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                     CEPConstants.CEP_CONF_ELE_TUPLE_DATA_TYPE_PAYLOAD));

        if (iterator.hasNext()) {
            tupleOutputMapping.setPayloadDataProperties(generatePropertyList(iterator.next()));
        }

        return tupleOutputMapping;
    }

    private static List<String> generatePropertyList(OMElement dataElement) {
        List<String> propertyList = null;
        for (Iterator iterator = dataElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                           CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext(); ) {
            if (propertyList == null) {
                propertyList = new ArrayList<String>();
            }
            OMElement propertyElement = (OMElement) iterator.next();
            propertyList.add(propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_NAME)));
        }
        return propertyList;
    }

    public static void addTupleMappingToRegistry(Registry registry,
                                                 TupleOutputMapping outputMapping,
                                                 String queryPath)
            throws CEPConfigurationException {
        try {
            String tupleMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                                            CEPConstants.CEP_REGISTRY_OUTPUT +
                                            CEPConstants.CEP_REGISTRY_BS +
                                            CEPConstants.CEP_REGISTRY_TUPLE_MAPPING;
            Resource tupleMappingResource = registry.newCollection();
            tupleMappingResource.addProperty(CEPConstants.CEP_REGISTRY_STREAM, outputMapping.getStreamId());
            registry.put(queryPath + tupleMappingPathString, tupleMappingResource);

            List<String> metaDataProperties = outputMapping.getMetaDataProperties();
            if (metaDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, metaDataPropertiesSize = metaDataProperties.size(); i < metaDataPropertiesSize; i++) {
                    String property = metaDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_META, mapping);
            }
            List<String> correlationDataProperties = outputMapping.getCorrelationDataProperties();

            if (correlationDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, correlationDataPropertiesISize = correlationDataProperties.size(); i < correlationDataPropertiesISize; i++) {
                    String property = correlationDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_CORRELATION, mapping);
            }
            List<String> payloadDataProperties = outputMapping.getPayloadDataProperties();

            if (payloadDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, payloadDataPropertiesSize = payloadDataProperties.size(); i < payloadDataPropertiesSize; i++) {
                    String property = payloadDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_PAYLOAD, mapping);
            }
        } catch (Exception e) {
            String errorMessage = "Can not add tuple mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyTupleMappingInRegistry(Registry registry,
                                                    TupleOutputMapping outputMapping,
                                                    String queryPath)
            throws CEPConfigurationException {
        try {
            String tupleMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                                            CEPConstants.CEP_REGISTRY_OUTPUT +
                                            CEPConstants.CEP_REGISTRY_BS +
                                            CEPConstants.CEP_REGISTRY_TUPLE_MAPPING;
            Resource tupleMappingResource = registry.newCollection();
            tupleMappingResource.addProperty(CEPConstants.CEP_REGISTRY_STREAM, outputMapping.getStreamId());
            registry.put(queryPath + tupleMappingPathString, tupleMappingResource);

            List<String> metaDataProperties = outputMapping.getMetaDataProperties();
            if (metaDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, metaDataPropertiesSize = metaDataProperties.size(); i < metaDataPropertiesSize; i++) {
                    String property = metaDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_META, mapping);
            }
            List<String> correlationDataProperties = outputMapping.getCorrelationDataProperties();

            if (correlationDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, correlationDataPropertiesSize = correlationDataProperties.size(); i < correlationDataPropertiesSize; i++) {
                    String property = correlationDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_CORRELATION, mapping);
            }
            List<String> payloadDataProperties = outputMapping.getPayloadDataProperties();

            if (payloadDataProperties != null) {
                Resource mapping = registry.newResource();
                for (int i = 0, payloadDataPropertiesSize = payloadDataProperties.size(); i < payloadDataPropertiesSize; i++) {
                    String property = payloadDataProperties.get(i);
                    mapping.addProperty(i + "", property);
                }
                registry.put(queryPath + tupleMappingPathString +
                             CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_PAYLOAD, mapping);
            }
        } catch (Exception e) {
            String errorMessage = "Can not modify tuple mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }

    public static TupleOutputMapping loadTupleMappingFromRegistry(Registry registry,
                                                                  String mappingPath)
            throws CEPConfigurationException {

        TupleOutputMapping tupleOutputMapping = null;
        try {
            tupleOutputMapping = new TupleOutputMapping();

            Resource resource = registry.get(mappingPath);
            tupleOutputMapping.setStreamId(resource.getProperty(CEPConstants.CEP_REGISTRY_STREAM));

            if (registry.resourceExists(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_META)) {
                Resource mappingResources = registry.get(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_META);
                List<String> dataList = loadProperties(mappingResources);
                tupleOutputMapping.setMetaDataProperties(dataList);
            }
            if (registry.resourceExists(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_CORRELATION)) {
                Resource mappingResources = registry.get(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_CORRELATION);
                List<String> dataList = loadProperties(mappingResources);
                tupleOutputMapping.setCorrelationDataProperties(dataList);
            }
            if (registry.resourceExists(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_PAYLOAD)) {
                Resource mappingResources = registry.get(mappingPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_TUPLE_MAPPING_PAYLOAD);
                List<String> dataList = loadProperties(mappingResources);
                tupleOutputMapping.setPayloadDataProperties(dataList);
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not load tuple mapping from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return tupleOutputMapping;

    }

    private static List<String> loadProperties(Resource mappingResources) {
        List<String> dataList = new ArrayList<String>();
        Properties properties = mappingResources.getProperties();
        int i = 0;
        while (properties.get(i + "") != null) {
            dataList.add(((List) properties.get(i + "")).get(0).toString());
            i++;
        }
        return dataList;
    }
}
