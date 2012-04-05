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
package org.wso2.carbon.rulecep.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.impl.POJOResourceAdapter;
import org.wso2.carbon.rulecep.adapters.utils.ResourceDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.Collection;

/**
 * Default <code>InputAdaptationStrategy</code> - only adapt up to child resources
 */
public class DefaultInputAdaptationStrategy implements InputAdaptationStrategy {

    private static final Log log = LogFactory.getLog(DefaultInputAdaptationStrategy.class);

    private InputAdapterFactory factory;

    public DefaultInputAdaptationStrategy(InputAdapterFactory factory) {
        this.factory = factory;
    }

    /**
     * The default  <code>InputAdaptationStrategy</code> algorithm
     * <ul>
     * <li> step1 - Gets the adapter for the type. If there is no adapter, uses the POJO adapter
     * and type as the class.
     * <li> step2 - Formulates the data to be adapted
     * <li> step3 - Converts the formulated data into the required type
     * <li> step4 - if the adapter is a <code>NestedInputAdaptable</code> , then adapts the children
     * <ul>
     *
     * @param description        input resource description
     * @param context            the context object being passed between components in the system
     *                           that the this component is used
     * @param messageInterceptor a helper class to locate data from given context
     * @return the required input object to be injected into the rule/event engine
     */
    public Object adapt(ResourceDescription description,
                        Object context,
                        MessageInterceptor messageInterceptor) {

        InputAdaptable adapter = factory.getInputAdapter(description.getType());
        if (adapter == null) {
            if (log.isDebugEnabled()) {
                log.info("There is no registered output adapter for the given type : " +
                        description.getType() + " . using the POJO Adapter");
            }
            adapter = new POJOResourceAdapter(); // TODO remove this in a proper way
        }

        Object resourceValue = ResourceDescriptionEvaluator.evaluate(description, context,
                messageInterceptor);
        if (resourceValue == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resulted object from the evaluation of the resource description : " +
                        description + " is null.");
            }
            return null;
        }

        Object value = adapter.adaptInput(description, resourceValue);
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("Adapted object is null. Source object was : " + resourceValue);
            }
            return value;
        }

        if (value instanceof Message) {
            //TODO make this into API , remove this in a proper way
            Message fact = (Message) value;
            fact.setMessage(context);
            fact.setMessageInterceptor(messageInterceptor);
        }

        if (adapter instanceof NestedInputAdaptable) {

            if (log.isDebugEnabled()) {
                log.debug("Adapting inputs with the children resource descriptions");
            }

            Collection<ResourceDescription> children = description.getChildResources();
            for (ResourceDescription child : children) {
                if (child == null) {
                    continue;
                }
                Object childValue = ResourceDescriptionEvaluator.evaluate(child, context,
                        messageInterceptor);
                if (childValue == null) {
                    continue;
                }
                InputAdaptable childAdapter = factory.getInputAdapter(child.getType());

                Object adaptedValue = childAdapter.adaptInput(child, childValue);
                if (adaptedValue == null) {
                    continue;
                }
                ((NestedInputAdaptable) adapter).adaptNestedInput(child, adaptedValue, value);
            }
        }
        return value;
    }

}
