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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.*;
import org.wso2.carbon.rulecep.adapters.utils.ResourceDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

/**
 * Adapts inputs as OMElement
 * Adapts OMElement outputs
 */
public class OMElementResourceAdapter extends ResourceAdapter implements OutputAdaptable,
        InputAdaptable, NestedOutputAdaptable {

    private static Log log = LogFactory.getLog(OMElementResourceAdapter.class);
    private final ContextPropertyOutputAdapter propertyOutputAdapter =
            new ContextPropertyOutputAdapter();
    public static final String TYPE = "omelement";

    public String getType() {
        return TYPE;
    }

    /**
     * Enrich the message with the given {@link OMElement}
     * Provided value must be an OMElement
     *
     * @param description        Output ResourceDescription
     * @param value              value in OMElement
     * @param context            the context to be used for looking up resources
     * @param messageInterceptor a helper class to locate resources from given context
     * @return <code>true</code> if the adaptation process is completed successfully.
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

        if (targetNode instanceof SOAPBody) {
            ((SOAPBody) targetNode).addChild((OMNode) value);
        } else if (targetNode instanceof OMElement) {
            OMElement targetOMNode = (OMElement) targetNode;
            targetOMNode.insertSiblingAfter((OMNode) value);
            targetOMNode.detach();
            return true;
        }
        return false;
    }

    /**
     * Checks the output is the type of OMElement
     *
     * @param description Output ResourceDescription
     * @param output      object to be adapted as an output
     * @return <code>true</code> if the provided output is the type of OMElement
     */
    public boolean canAdaptOutput(ResourceDescription description, Object output) {
        return output instanceof OMElement;
    }

    /**
     * Returns the given object as it is in OMElement
     *
     * @param description information about the target object
     * @param result      the object to be adapted
     * @return Returns the provided object 'as is'
     */
    public Object adaptOutput(ResourceDescription description, Object result) {
        return result;
    }

    /**
     * Adapts the given object into the OMElement
     * if the tobeAdapted is an OMElement , returns it 'as is'.
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         object in OMElement
     * @return provided object 'as is', if the object to be adapted is an OMElement.
     *         Otherwise throws an LoggedRuntimeException
     */
    public Object adaptInput(ResourceDescription resourceDescription, Object tobeAdapted) {

        if (tobeAdapted instanceof OMElement) {
            return tobeAdapted;
        } else {
            throw new LoggedRuntimeException("Incompatible value for the OMElement " +
                    tobeAdapted, log);
        }
    }

    /**
     * Creates an OMElement with the name of the parent resource and add adapted children as
     * children to the OMElement
     *
     * @param description        Parent resource description
     * @param results            a list of results from the rule engine
     * @param context            context object being passed between the components in the system that
     *                           the rule component is used
     * @param messageInterceptor intercepts the context message
     * @param adapterFactory     place holder for the output adapters
     * @return <code>true</code>  if the adaptation process is completed successfully
     */
    public boolean adaptChildren(ResourceDescription description,
                                 List results,
                                 Object context,
                                 MessageInterceptor messageInterceptor,
                                 OutputAdapterFactory adapterFactory) {
        if (!description.hasChildren()) {
            return false;
        }

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        QName wrapperQName = description.getElementQName();
        if (wrapperQName == null) {
            String name = description.getName();
            if (name == null || "".equals(name)) {
                name = AdaptersConstants.DEFAULT_WRAPPER_NAME;
            }
            wrapperQName = new QName(name);
        }

        OMElement wrapperElement = omFactory.createOMElement(wrapperQName);
        Object targetNode = ResourceDescriptionEvaluator.evaluateExpression(description,
                context,
                messageInterceptor);

        if (targetNode == null) {
            ReturnValue returnValue = messageInterceptor.extractPayload(context);
            targetNode = returnValue.getValue();
        }
        if (!(targetNode instanceof OMElement)) {
            throw new LoggedRuntimeException("The target node should have been an OMNode", log);
        }

        OMElement targetOMNode = (OMElement) targetNode;

        if (targetOMNode instanceof SOAPBody) {
            OMElement firstElement = targetOMNode.getFirstElement();
            if (firstElement != null) {
                handleFirstChild(firstElement, wrapperElement, description);
            } else {
                targetOMNode.addChild(wrapperElement);
            }
        } else if (targetOMNode.getParent() instanceof SOAPBody) {
            handleFirstChild(targetOMNode, wrapperElement, description);
        } else {
            targetOMNode.addChild(wrapperElement);
        }

        Collection<ResourceDescription> children = description.getChildResources();
        for (ResourceDescription child : children) {
            if (child == null) {
                continue;
            }
            OutputAdaptable outputAdaptable = adapterFactory.getOutputAdapter(child.getType());
            if (outputAdaptable == null) {
                outputAdaptable = new POJOResourceAdapter();
            }
            for (Object result : results) {
                if (result != null && outputAdaptable.canAdaptOutput(child, result)) {
                    Object adaptedValue = outputAdaptable.adaptOutput(child, result);
                    if (adaptedValue instanceof OMNode) {
                        wrapperElement.addChild((OMNode) adaptedValue);
                    }
                }
            }
        }
        return true;
    }

    private void handleFirstChild(OMElement firstChild,
                                  OMElement result,
                                  ResourceDescription description) {

        if (!firstChild.getQName().equals(description.getParentElementQName())) {
            firstChild.insertSiblingAfter(result);
            firstChild.detach();
        } else {
            firstChild.addChild(result);
        }
    }
}
