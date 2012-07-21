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
import org.wso2.carbon.cep.core.mapping.output.mapping.MapOutputMapping;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class MapOutputMappingHelper {
    private static final Log log = LogFactory.getLog(MapOutputMappingHelper.class);

    public static MapOutputMapping fromOM(OMElement mapMappingElement) {
        MapOutputMapping mapOutputMapping = new MapOutputMapping();


        List<String> propertyList = null;
        for (Iterator iterator = mapMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                           CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext(); ) {
            if (propertyList == null) {
                propertyList = new ArrayList<String>();
            }
            OMElement propertyElement = (OMElement) iterator.next();
            propertyList.add(propertyElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_NAME)));
        }
        mapOutputMapping.setPropertyList(propertyList);

        return mapOutputMapping;

    }

    public static void addMapMappingToRegistry(Registry registry,
                                               MapOutputMapping outputMapping,
                                               String queryPath)
            throws CEPConfigurationException {


        try {
            String mapMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                                          CEPConstants.CEP_REGISTRY_OUTPUT +
                                          CEPConstants.CEP_REGISTRY_BS +
                                          CEPConstants.CEP_REGISTRY_MAP_MAPPING;
            Resource mapMappingResource = registry.newCollection();

            List<String> metaDataProperties = outputMapping.getPropertyList();
            if (metaDataProperties != null) {
//                Resource mapping = registry.newResource();
                for (int i = 0, metaDataPropertiesSize = metaDataProperties.size(); i < metaDataPropertiesSize; i++) {
                    String property = metaDataProperties.get(i);
                    mapMappingResource.addProperty(i + "", property);
                }
                registry.put(queryPath + mapMappingPathString, mapMappingResource);
            }

        } catch (Exception e) {
            String errorMessage = "Can not add map mapping to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }

    public static void modifyMapMappingInRegistry(Registry registry,
                                                  MapOutputMapping outputMapping,
                                                  String queryPath)
            throws CEPConfigurationException {
        //todo

    }

    public static MapOutputMapping loadMapMappingFromRegistry(Registry registry,
                                                              String mappingPath)
            throws CEPConfigurationException {
        MapOutputMapping mapOutputMapping = null;
        try {
            mapOutputMapping = new MapOutputMapping();

            Resource resource = registry.get(mappingPath);

            List<String> dataList = loadProperties(resource);
            mapOutputMapping.setPropertyList(dataList);

        } catch (RegistryException e) {
            String errorMessage = "Can not load tuple mapping from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return mapOutputMapping;

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
