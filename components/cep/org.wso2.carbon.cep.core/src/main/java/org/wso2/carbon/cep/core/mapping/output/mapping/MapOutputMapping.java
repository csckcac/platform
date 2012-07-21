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

package org.wso2.carbon.cep.core.mapping.output.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapOutputMapping extends OutputMapping {
    private static final Log log = LogFactory.getLog(OutputMapping.class);
    private List<String> propertyList;

    @Override
    public Object convert(Object event) {
       try {
        return buildMapEvent(event);
    } catch (CEPEventProcessingException e) {
        log.error("Error in accessing information from the output event to build the OM Element " + e);
    }
    return null;
}

    private Map buildMapEvent(Object event) throws CEPEventProcessingException {
        Map<String,Object> map=new HashMap<String, Object>();
        if (event instanceof Event) {
           for(String property:propertyList){
               map.put(property,getPropertyValue(event,property));
           }
        } else if(event instanceof Map){
            for(String property:propertyList){
                map.put(property,((Map)event).get(property));
            }
        }

        return map;
    }

    public void setPropertyList(List<String> propertyList) {
        this.propertyList = propertyList;
    }

    public List<String> getPropertyList() {
        return propertyList;
    }
}
