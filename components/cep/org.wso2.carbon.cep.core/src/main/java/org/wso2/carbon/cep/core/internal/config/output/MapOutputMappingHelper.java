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

import org.wso2.carbon.cep.core.mapping.output.mapping.MapOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.TupleOutputMapping;
import org.wso2.carbon.cep.core.mapping.output.mapping.ElementOutputMapping;
import org.wso2.carbon.cep.core.mapping.property.XMLProperty;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.axiom.om.OMElement;

import java.util.List;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;

public class MapOutputMappingHelper {

    public static MapOutputMapping fromOM(OMElement mapMappingElement) {
        return new MapOutputMapping();
    }

    public static void addMapMappingToRegistry(Registry registry,
                                               MapOutputMapping outputMapping,
                                               String queryPath)
            throws CEPConfigurationException {

        try {
            String mapMappingPathString = CEPConstants.CEP_REGISTRY_BS +
                                                CEPConstants.CEP_REGISTRY_OUTPUT +
                                                CEPConstants.CEP_REGISTRY_BS +
                                                CEPConstants.CEP_REGISTRY_MAPPING_MAP;
            Resource tupleMappingResource = registry.newCollection();
            registry.put(queryPath + mapMappingPathString, tupleMappingResource);
        } catch (RegistryException e) {
            throw new CEPConfigurationException("Can not add data to registry");
        }

    }

    public static void modifyMapMappingInRegistry(Registry registry,
                                                  MapOutputMapping outputMapping,
                                                  String queryPath)
            throws CEPConfigurationException {


    }

    public static MapOutputMapping loadMapMappingFromRegistry(Registry registry,
                                                              String mappingName) throws CEPConfigurationException {
        return new MapOutputMapping();

    }

}
