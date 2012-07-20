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
import org.wso2.carbon.cep.core.mapping.input.mapping.MapInputMapping;
import org.wso2.carbon.cep.core.mapping.input.mapping.TupleInputMapping;
import org.wso2.carbon.cep.core.mapping.property.TupleProperty;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Map;
import java.util.Arrays;

public class MapInputMappingHelper {

    public static MapInputMapping fromOM(OMElement mapMappingElement){
        return new MapInputMapping();
    }

    public static void addMappingToRegistry(Registry registry, MapInputMapping mapInputMapping,
                                     String mappingPath) throws RegistryException {
        
    }

    public static void loadMappingsFromRegistry(Registry registry, MapInputMapping inputMapping,
                                         Collection mappingCollection)
            throws RegistryException, CEPConfigurationException {

    }
}
