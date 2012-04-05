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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.PayloadHelper;
import org.apache.synapse.util.SimpleMap;
import org.apache.synapse.util.SimpleMapImpl;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.OutputAdaptable;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.adapters.impl.ContextPropertyOutputAdapter;
import org.wso2.carbon.rulecep.adapters.utils.ResourceDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.Map;

/**
 * Adapts inputs into Maps and Map outputs into XML
 */
public class MapResourceAdapter extends ResourceAdapter implements InputAdaptable, OutputAdaptable {

    private static final Log log = LogFactory.getLog(MapResourceAdapter.class);
    public final static String TYPE = "simplemap";
    private final ContextPropertyOutputAdapter propertyOutputAdapter =
            new ContextPropertyOutputAdapter();

    public String getType() {
        return TYPE;
    }

    /**
     * Converts the input into a Map. The allowed inputs are OMElement and Map
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert that into correct type
     * @return A Map
     */
    public Object adaptInput(ResourceDescription resourceDescription, Object tobeAdapted) {

        if (tobeAdapted instanceof Map) {
            return tobeAdapted;
        } else if (tobeAdapted instanceof OMElement) {
            OMElement omElement = (OMElement) tobeAdapted;
            if (PayloadHelper.MAPELT.equals(omElement.getQName())) {
                return new SimpleMapImpl(omElement);
            } else {
                throw new LoggedRuntimeException("Incompatible value for the map " + "Wrong QName" +
                        omElement.getQName() + " expected " + PayloadHelper.MAPELT, log);
            }
        } else {
            throw new LoggedRuntimeException("Incompatible value for the map " +
                    tobeAdapted, log);
        }
    }

    /**
     * Converts the given Map result into an XML
     *
     * @param description        Output ResourceDescription
     * @param result             Result from the engine
     * @param context            the context to be used for looking up resources
     * @param messageInterceptor a helper class to locate resources from given context
     * @return <code>true</code> if the adaptation was completed successfully.
     */
    public boolean adaptOutput(ResourceDescription description,
                               Object result,
                               Object context,
                               MessageInterceptor messageInterceptor) {

        if (description == null) {
            throw new LoggedRuntimeException("Cannot find Resource description. " +
                    "Invalid Resource !!", log);
        }
        if (result == null) {
            return false;
        }
        if (propertyOutputAdapter.adaptOutput(description, result, context, messageInterceptor)) {
            return true;
        }
        if (result instanceof Map) {
            Map map = (Map) result;
            SimpleMap simplemap = new SimpleMapImpl();
            for (Object o : map.keySet()) {
                if (o == null || "".equals(o)) {
                    continue;
                }
                String key = (String) o;
                Object value = map.get(key);
                simplemap.put(key, value);
            }
            Object targetNode = ResourceDescriptionEvaluator.evaluateExpression(description,
                    context,
                    messageInterceptor);
            if (targetNode instanceof SOAPEnvelope) {
                PayloadHelper.setMapPayload((SOAPEnvelope) targetNode, simplemap);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the output is a Map
     *
     * @param description Output ResourceDescription
     * @param output      Result from the engine
     * @return <code>true</code> if the given output is a Map
     */
    public boolean canAdaptOutput(ResourceDescription description, Object output) {
        return output instanceof Map;
    }

    /**
     * This operation is not supported
     *
     * @param description information about target object
     * @param result      the object to be adapted
     * @return throws UnsupportedOperationException
     */
    public Object adaptOutput(ResourceDescription description, Object result) {
        throw new UnsupportedOperationException("adaptOutput(ResourceDescription description," +
                "Object result) operation is not supported.");
    }
}
