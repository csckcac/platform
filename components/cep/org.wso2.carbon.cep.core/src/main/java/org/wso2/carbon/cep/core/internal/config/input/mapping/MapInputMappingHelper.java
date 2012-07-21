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

package org.wso2.carbon.cep.core.internal.config.input.mapping;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.cep.core.mapping.input.mapping.MapInputMapping;
import org.wso2.carbon.cep.core.mapping.property.MapProperty;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapInputMappingHelper {

    public static MapInputMapping fromOM(OMElement mapMappingElement)
            throws CEPConfigurationException {
//        return new MapInputMapping();
        MapInputMapping mapInputMapping = new MapInputMapping();

        String stream =
                mapMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_STREAM));
        mapInputMapping.setStream(stream);

        String className =
                mapMappingElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_EVENT_CLASS));
        if (className != null) {
            if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_TUPLE)) {
                mapInputMapping.setMappingClass(Event.class);
            } else if (className.equals(CEPConstants.CEP_CONF_CLASS_NAME_MAP)) {
                mapInputMapping.setMappingClass(Map.class);
            } else {
                try {
                    Class mappingClass = Class.forName(className);
                    mapInputMapping.setMappingClass(mappingClass);
                } catch (ClassNotFoundException e) {
                    throw new CEPConfigurationException("No class found matching " + className, e);
                }
            }
        } else {
            mapInputMapping.setMappingClass(Event.class);
        }

        for (Iterator iterator = mapMappingElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                   CEPConstants.CEP_CONF_ELE_PROPERTY)); iterator.hasNext(); ) {
            OMElement propertyElement = (OMElement) iterator.next();
            MapProperty property = PropertyHelper.mapPropertyFromOM(propertyElement);
            property.setInputProperty(true);
            mapInputMapping.addProperty(property);
            if (mapInputMapping.getMappingClass() != Map.class && mapInputMapping.getMappingClass() != Event.class) {
                mapInputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(mapInputMapping.getMappingClass(), property.getName()));
            }
        }

        return mapInputMapping;
    }




    public static void addMappingToRegistry(Registry registry, MapInputMapping mapInputMapping,
                                            String mappingPath) throws RegistryException {
        if (mapInputMapping.getProperties() != null) {
            List<MapProperty> properties = mapInputMapping.getProperties();
            for (MapProperty property : properties) {
                Resource propertyResource = registry.newResource();
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_NAME, property.getName());
                propertyResource.addProperty(CEPConstants.CEP_REGISTRY_TYPE, property.getType());
                registry.put(mappingPath + CEPConstants.CEP_REGISTRY_PROPERTIES + CEPConstants.CEP_REGISTRY_BS + property.getName(), propertyResource);
            }
        }
    }

    public static void loadMappingsFromRegistry(Registry registry, MapInputMapping inputMapping,
                                                Collection mappingCollection)
            throws RegistryException, CEPConfigurationException {
        for (String mappingChild : mappingCollection.getChildren()) {
            if (registry.get(mappingChild) instanceof Collection) {
                Collection mapCollection = (Collection) registry.get(mappingChild);
                if (
                        (CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROPERTIES)
                                .equals(mappingChild.substring(mappingChild.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                    List<MapProperty> mapProperties= new ArrayList<MapProperty>();
                    for (String defs : mapCollection.getChildren()) {
                        Resource propertyResource = registry.get(defs);
                        MapProperty property = new MapProperty();
                        property.setName(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_NAME));
                        property.setType(propertyResource.getProperty(CEPConstants.CEP_REGISTRY_TYPE));
                        mapProperties.add(property);
                        if (inputMapping.getMappingClass() != Map.class && inputMapping.getMappingClass() != Event.class) {
                            inputMapping.putWriteMethod(property.getName(), InputMappingHelper.getMethod(inputMapping.getMappingClass(), property.getName()));
                        }
                    }
                    inputMapping.setProperties(mapProperties);
                }
            }
        }
    }
}
