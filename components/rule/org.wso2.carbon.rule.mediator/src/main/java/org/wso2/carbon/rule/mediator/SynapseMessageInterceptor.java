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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.commons.ReturnValue;

/**
 * The {@link MessageInterceptor} implementation for the synapse message. This intercepts
 * the synapse message to access resources in the synapse and
 * to enrich the message with results from the rule engine execution
 */
public class SynapseMessageInterceptor implements MessageInterceptor {

    private static final Log log = LogFactory.getLog(SynapseMessageInterceptor.class);

    /* Lock used to ensure thread-safe look up of the object from the registry */
    private final Object resourceLock = new Object();

    /**
     * Look up for data with the given key in message context , synapse configuration and registry
     *
     * @param key          an identifier to locate the resource
     * @param source       Synapse Message Context
     * @param defaultValue default value in the case of there is no resource for the given key
     * @return Data located by the key or default value
     */
    public ReturnValue extract(String key, Object source, Object defaultValue) {

        if (source instanceof MessageContext) {
            MessageContext synCtx = (MessageContext) source;

            // On the first check the availability of the key in property bag
            Object result = synCtx.getProperty(key);
            if (result != null) {
                return new ReturnValue(result);

            }  //Then, it must be a entry key
            //reload the XML document from the registry
            boolean reLoad = false;
            Entry dp = synCtx.getConfiguration().getEntryDefinition(key);
            // if the key refers to a dynamic resource
            if (dp != null && dp.isDynamic()) {
                if (!dp.isCached() || dp.isExpired()) {
                    reLoad = true;
                }
            }
            if (reLoad) {
                // it is need to synchronized this to avoid recreation of the cachedValue by
                // the multiple threads
                synchronized (resourceLock) {
                    return new ReturnValue(synCtx.getEntry(key), true);
                }
            } else {
                return new ReturnValue(synCtx.getEntry(key), defaultValue != null);
                // recreation by the multiple threads
            }
        }
        return new ReturnValue(defaultValue);

    }

    /**
     * To evaluates an expression on the synapse message and extractPayload data
     *
     * @param xPath        an expression to locate the resource
     * @param source       Synapse Message Context
     * @param defaultValue default value in the case of there is no resource for the given expression
     * @return Data extracted from evaluating XPath on the Synapse Message Context if the XPath is not null.
     *         Otherwise, the default value itself
     */
    public ReturnValue extract(XPath xPath, Object source, Object defaultValue) {

        try {
            if (source instanceof MessageContext) {
                source = ((MessageContext) source).getEnvelope();
            }
            Object result = xPath.evaluate(source);
            if (result == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Evaluation of the XPath :  " + xPath + " result in null," +
                            " returning given default value");
                }
                return new ReturnValue(defaultValue);
            }
            return new ReturnValue(result, true);
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error when evaluating XPath : " + xPath, e, log);
        }
    }

    /**
     * Gets the default resource when there is no a key or an expression. The implementation is
     * to return the first child of the SOAP body if there is such a one.Otherwise , the SOAP Body itself
     *
     * @param message the context object being passed between components of the system the rule
     *                component is used (i.e {@link org.apache.synapse.MessageContext}})
     * @return Either the first child of the SOAP body or the SOAP Body itself
     */
    public ReturnValue extractPayload(Object message) {
        if (message instanceof MessageContext) {
            MessageContext messageContext = ((MessageContext) message);
            OMElement returnElement = messageContext.getEnvelope().getBody().getFirstElement();
            if (returnElement == null) {
                returnElement = messageContext.getEnvelope().getBody();
            }
            return new ReturnValue(returnElement, true);
        }
        return null;
    }

    public ReturnValue extractEnvelope(Object message) {

        if (message instanceof MessageContext) {
            MessageContext messageContext = ((MessageContext) message);
            return new ReturnValue(messageContext.getEnvelope(), true);
        }
        throw new LoggedRuntimeException("Invalid context object, expected Synapse Message Context",
                log);
    }

    /**
     * Puts the given result to the synapse message context
     * Here source need to be an Synapse Message Context
     */
    public void enrich(String key, Object source, Object result) {

        if (source instanceof MessageContext) {
            ((MessageContext) source).setProperty(key, result);
        }
    }
}
