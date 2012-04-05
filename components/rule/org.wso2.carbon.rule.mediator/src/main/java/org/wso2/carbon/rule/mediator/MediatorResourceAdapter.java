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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.Properties;

/**
 * Adapts input into mediators
 */
public class MediatorResourceAdapter extends ResourceAdapter implements InputAdaptable {

    private static final Log log = LogFactory.getLog(MediatorResourceAdapter.class);
    public final static String TYPE = "mediator";

    public String getType() {
        return TYPE;
    }

    /**
     * Creates an mediator from the given input. The allowed inputs are mediators and OMElement
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert that into correct type
     * @return A Mediator instance
     */
    public Object adaptInput(ResourceDescription resourceDescription, Object tobeAdapted) {
        if (tobeAdapted instanceof Mediator) {
            return tobeAdapted;
        } else if (tobeAdapted instanceof OMElement) {

            /* The mapper that can convert XML into custom object */
            XMLToObjectMapper mapper = MediatorFactoryFinder.getInstance();
            Object result = mapper.getObjectFromOMNode((OMNode) tobeAdapted,new Properties());

            if (result instanceof Mediator) {
                return result;
            } else {
                throw new LoggedRuntimeException("Incompatible value for the sequence " +
                        result, log);
            }

        } else {
            throw new LoggedRuntimeException("Incompatible value for the sequence  " +
                    tobeAdapted, log);
        }
    }
}
