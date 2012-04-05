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
package org.wso2.carbon.rulecep.service;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;

import java.io.InputStream;

/**
 * Interceptor to deal with Axis2 Message Context
 */
public class Axis2MessageInterceptor implements MessageInterceptor {

    private static final Log log = LogFactory.getLog(Axis2MessageInterceptor.class);

    public Axis2MessageInterceptor() {
    }

    /**
     * Uses class loader to load resources //TODO add axis2 service class loader
     *
     * @param key          an identifier to locate the resource
     * @param source       Axis2 Message Context
     * @param defaultValue default value in the case of there is no resource for the given key
     * @return A Value located by the key or default value
     */
    public ReturnValue extract(String key, Object source, Object defaultValue) {

        if (source instanceof MessageContext) {
            return new ReturnValue(((MessageContext) source).getProperty(key));
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (log.isDebugEnabled()) {
            log.debug("Loading a file ' " + key + " ' from class-path");
        }

        InputStream in = cl.getResourceAsStream(key);
        if (in == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load file  ' " + key + " ' from class-path. " +
                        "Returning given default value");
            }
            return new ReturnValue(defaultValue);
        }
        return new ReturnValue(in);
    }

    /**
     * To evaluates an expression on the message and extractPayload data
     *
     * @param xPath        an expression to locate the resource
     * @param source       Axis2 Message Context
     * @param defaultValue default value in the case of there is no resource for the given expression
     * @return Data extracted from evaluating XPath on the SOAP Envelope if the XPath is not null.
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
            ReturnValue returnValue = new ReturnValue(result);
            returnValue.setFresh(true);
            return returnValue;
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error when evaluating XPath : " + xPath, log);
        }

    }

    /**
     * Returns  either the first child of the SOAP body or the SOAP Body itself
     *
     * @param message the context object being passed between components of the system the
     *                rule component is used (i.e @{MessageContext})
     * @return Either the first child of the SOAP body or the SOAP Body itself
     */
    public ReturnValue extractPayload(Object message) {
        if (message instanceof MessageContext) {
            MessageContext messageContext = ((MessageContext) message);
            OMElement returnElement = messageContext.getEnvelope().getBody().getFirstElement();
            if (returnElement == null) {
                returnElement = messageContext.getEnvelope().getBody();
            }
            ReturnValue returnValue = new ReturnValue(returnElement);
            returnValue.setFresh(true);
            return returnValue;
        }
        return null;
    }

    public ReturnValue extractEnvelope(Object message) {
        if (message instanceof MessageContext) {
            return new ReturnValue(((MessageContext) message).getEnvelope(), true);
        }
        throw new LoggedRuntimeException("Invalid context object, expected Axis2 Message Context",
                log);
    }

    /**
     * Here source need to be an Axis2 Message Context
     */
    public void enrich(String key, Object source, Object value) {

        if (source instanceof MessageContext) {
            ((MessageContext) source).setProperty(key, value);
        }
    }
}
