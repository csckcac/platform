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
package org.wso2.carbon.rulecep.adapters.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.OutputAdaptable;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.adapters.utils.ResourceDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

/**
 * Adapts inputs as DOM
 * Adapts DOM outputs into OMElement
 */
public class DOMResorceAdapter extends ResourceAdapter implements OutputAdaptable, InputAdaptable {

    private static Log log = LogFactory.getLog(DOMResorceAdapter.class);
    private final ContextPropertyOutputAdapter propertyOutputAdapter =
            new ContextPropertyOutputAdapter();
    public static final String TYPE = "dom";

    public String getType() {
        return TYPE;
    }

    /**
     * Convert provided DOM result into <code>OMElement</code> and enrich the message
     * with the converted OMElement
     *
     * @param description        Output ResourceDescription
     * @param value              DOM result to be converted into OMElement
     * @param context            the context to be used for looking up resources
     * @param messageInterceptor a helper class to locate resources from given context
     * @return <code>true</code> if the adaptation is completed successfully.
     */
    public boolean adaptOutput(ResourceDescription description,
                               Object value, Object context,
                               MessageInterceptor messageInterceptor) {

        if (description == null) {
            throw new LoggedRuntimeException("Cannot find Resource description. " +
                    "Invalid Resource !!", log);
        }

        if (value == null) {
            return false;
        }
        if (propertyOutputAdapter.adaptOutput(description, value, context, messageInterceptor)) {
            return true;
        }

        Object targetNode = ResourceDescriptionEvaluator.evaluateExpression(description,
                context,
                messageInterceptor);

        if (targetNode == null) {
            ReturnValue returnValue = messageInterceptor.extractPayload(context);
            targetNode = returnValue.getValue();
        }

        if (targetNode instanceof OMElement) {
            OMElement targetOMNode = (OMElement) targetNode;
            targetOMNode.insertSiblingAfter(ElementHelper.importOMElement(
                    (OMElement) value, OMAbstractFactory.getOMFactory()));
            targetOMNode.detach();
            return true;
        }
        return false;
    }

    /**
     * Checks the result is the type of DOM
     *
     * @param description Output ResourceDescription
     * @param output      result to be adapted
     * @return <code>true</code> if the result is the type of DOM
     */
    public boolean canAdaptOutput(ResourceDescription description, Object output) {
        return output instanceof Node;
    }

    /**
     * Convert to the given DOOM object to the OMElement
     *
     * @param description information about target object
     * @param value       the object to be adapted
     * @return if the given object is type of DOOM
     */
    public Object adaptOutput(ResourceDescription description, Object value) {
        if (value instanceof OMElement) {
            return ElementHelper.importOMElement(
                    (OMElement) value, OMAbstractFactory.getOMFactory());
        }
        throw new LoggedRuntimeException("Incompatible value for the DOM " +
                value, log);
    }

    /**
     * Convert the provided object into the {@link Node} or DOOM (if the provided object
     * is @{OMElement})
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert that into correct type
     * @return <code>Node<code> if the provided object is either <code>Node</code> or
     *         <code>OMElement</code>
     */
    public Object adaptInput(ResourceDescription resourceDescription, Object tobeAdapted) {

        if (tobeAdapted instanceof Node) {
            return tobeAdapted;
        } else if (tobeAdapted instanceof OMElement) {
            return ((Element) ElementHelper.importOMElement((OMElement) tobeAdapted,
                    DOOMAbstractFactory.getOMFactory())).getOwnerDocument();
        } else {
            throw new LoggedRuntimeException("Incompatible value for the DOM " +
                    tobeAdapted, log);
        }
    }
}
