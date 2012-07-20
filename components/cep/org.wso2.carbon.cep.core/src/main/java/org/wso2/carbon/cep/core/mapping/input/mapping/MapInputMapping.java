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

package org.wso2.carbon.cep.core.mapping.input.mapping;

import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.Map;

public class MapInputMapping extends InputMapping {

    protected Map convertToEventMap(Object event) throws CEPEventProcessingException {
        return (Map) event;  
    }

    protected Object convertToEventObject(Object event, Object resultEvent) throws CEPEventProcessingException {
        return null;
    }

    protected Event convertToEventTuple(Object event) throws CEPEventProcessingException {
        return null; 
    }
}
