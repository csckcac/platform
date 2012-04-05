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

import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.OutputAdaptable;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.adapters.utils.ResourceDescriptionEvaluator;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.utils.ClassHelper;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.util.List;

/**
 * Adapts inputs as POJO
 * Adapts POJO outputs and covert them either into OMElement or set as context property
 */
public class POJOResourceAdapter extends ResourceAdapter implements InputAdaptable,
        OutputAdaptable {

    public final static String TYPE = "pojo";
    private static final Log log = LogFactory.getLog(POJOResourceAdapter.class);
    private final ContextPropertyOutputAdapter propertyOutputAdapter =
            new ContextPropertyOutputAdapter();

    public String getType() {
        return TYPE;
    }

    /**
     * Creates a object of the type specified in the given resource description
     * out of the provided source object
     * if the source object is type OMElement , axis2 adb data binding is used to create the POJOs
     *
     * @param description information about the target resource
     * @param source      object to be converted into a POJO
     * @return a POJO of the type specified in the given resource description if there is no errors.
     */
    public Object adaptInput(ResourceDescription description, Object source) {
        String className = description.getType();
        if (className == null) {
            throw new LoggedRuntimeException("The class name cannot be found.", log);
        }
        if (source instanceof OMText) {
            source = ((OMText) source).getText();
        }
        if (className.equals(source.getClass().getName())) {
            return source;
        } else if (source instanceof String && ClassHelper.isWrapperType(className)) {
            return ClassHelper.createWrapperTypeInstance(className, (String) source);
        } else if (source instanceof OMElement) {
            OMElement omElement = (OMElement) source;
            int index = className.indexOf(",");
            if (index > 0) {
                String[] classNames = className.split(",");
                return adaptToMultiplePOJOs(omElement, classNames);
            } else if (description.hasChildren()) {
                final Collection<ResourceDescription> children = description.getChildResources();
                Collection<Class> classes = description.getChildrenClasses();
                if (classes.isEmpty()) {
                    classes = new ArrayList<Class>();
                    for (ResourceDescription child : children) {
                        if (child == null) {
                            continue;
                        }
                        String childClass = child.getType();
                        Class aClass = ClassHelper.loadAClass(childClass,
                                description.getResourceClassLoader());
                        if (aClass == null) {
                            continue;
                        }
                        classes.add(aClass);
                    }
                    description.addChildrenClasses(classes);
                }

                if (classes.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Required arguments can not be found." + "Returning null");
                    }
                    return null;
                }
                Collection<String> childrenNames = description.getChildrenNames();

                Iterator it = omElement.getChildElements();
                OMElement tobeAdapted;

                ArrayList<String> childTypeList =new ArrayList<String>();
                for (ResourceDescription child : children) {
                    if (child == null) {
                        continue;
                    }
                    childTypeList.add(child.getType());
                }
                classes.clear();
                while (it.hasNext()) {
                    tobeAdapted = (OMElement) it.next();
                    String localClass = tobeAdapted.getQName().getLocalPart();
                    for (String name : childTypeList) {
                        String classNamePart = name.substring(name.lastIndexOf(".") + 1);
                        if (classNamePart.equals(localClass)) {
                            Class aClass = ClassHelper.loadAClass(name,
                                    description.getResourceClassLoader());
                            if (aClass == null) {
                                continue;
                            }
                            classes.add(aClass);
                        }
                    }

                }

                if (childrenNames.size() != classes.size()) {
                    return toObjects(omElement, classes, null);
                } else {
                    return toObjects(omElement, classes,
                            childrenNames.toArray(new String[childrenNames.size()]));
                }
            } else {
                OMElement tobeAdapted;
                if (omElement.getParent() instanceof SOAPBody) {
                    tobeAdapted = omElement.getFirstElement();
                    Iterator it = omElement.getChildElements();
                    List adaptedInputs = new ArrayList();
                    while (it.hasNext()) {
                        tobeAdapted = (OMElement) it.next();
                        adaptedInputs.add(getAdaptedFacts(description, className, tobeAdapted, source));
                    }
                    return adaptedInputs;
                } else {
                    tobeAdapted = omElement;
                }
                QName qName = getQName(description, className, tobeAdapted);
                if (!qName.equals(tobeAdapted.getQName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("The source element QName does not match with the expected QName." +
                                "[ Source Element QName:  " + tobeAdapted.getQName() + " ] " +
                                "[ Expected QName : " + qName + " ].");
                    }
                    return null;
                }
                try {
                    return BeanUtil.deserialize(ClassHelper.loadAClass(className,
                            description.getResourceClassLoader()),
                            tobeAdapted, new DefaultObjectSupplier(), null);
                } catch (AxisFault axisFault) {
                    throw new LoggedRuntimeException("Cannot create a custom Java object " +
                            "form XML. Java class is : " + className + " and XML is : " +
                            source, log);
                }
            }

        } else {
            throw new LoggedRuntimeException("Incompatible type , " +
                    "the provided class name ' " +
                    className + " ' doesn't match " +
                    "with the class name of the custom Java object.", log);
        }
    }

    public Object getAdaptedFacts(ResourceDescription description, String className, OMElement tobeAdapted, Object source) {
        QName qName = getQName(description, className, tobeAdapted);
        if (!qName.equals(tobeAdapted.getQName())) {
            if (log.isDebugEnabled()) {
                log.debug("The source element QName does not match with the expected QName." +
                        "[ Source Element QName:  " + tobeAdapted.getQName() + " ] " +
                        "[ Expected QName : " + qName + " ].");
            }
            return null;
        }
        try {
            return BeanUtil.deserialize(ClassHelper.loadAClass(className,
                    description.getResourceClassLoader()),
                    tobeAdapted, new DefaultObjectSupplier(), null);
        } catch (AxisFault axisFault) {
            throw new LoggedRuntimeException("Cannot create a custom Java object " +
                    "form XML. Java class is : " + className + " and XML is : " +
                    source, log);
        }
    }


    /**
     * Creates a multiple POJOs or a list of POJOs from the given object to adapted
     * This method uses axis2 ADB data binding to create POJOs from the XML
     *
     * @param tobeAdapted object is in OMElement
     * @param classNames  class names of the target objects
     * @return a list of adapted objects
     */
    private Object adaptToMultiplePOJOs(OMElement tobeAdapted, String[] classNames) {
        final List<Class> parameters = new ArrayList<Class>();
        for (String className : classNames) {

            Class aClass = ClassHelper.loadAClass(className);
            if (aClass == null) {
                continue;
            }
            parameters.add(aClass);
        }

        if (parameters.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Input type Axis2operation : required arguments can not be found." +
                        "Returning null");
            }
            return null;
        }
        return toObjects(tobeAdapted, parameters, null);
    }

    /**
     * A helper method for creating a list of POJOs from a XML
     *
     * @param tobeAdapted XML
     * @param parameters  a list of class representing expected types
     * @param strings     a list of string representing tag names
     * @return a list of POJO
     */
    private List<Object> toObjects(OMElement tobeAdapted,
                                   Collection<Class> parameters,
                                   String[] strings) {
        try {
            Object[] objects =
                    BeanUtil.deserialize(tobeAdapted, parameters.toArray(
                            new Object[parameters.size()]), new DefaultObjectSupplier(),
                            strings, null);

            if (parameters.size() != objects.length) {
                throw new LoggedRuntimeException("Error processing value for the " +
                        "Axis2operation : required arguments can not be found." +
                        tobeAdapted, log);
            }

            final List<Object> pairs = new ArrayList<Object>();
            for (Object value : objects) {
                if (value != null) {
                    pairs.add(value);
                }
            }
            return pairs;
        } catch (AxisFault axisFault) {
            throw new LoggedRuntimeException("Error processing value for the Axis2operation " +
                    tobeAdapted, log);
        }
    }

    /**
     * The give POJO result is converted into an XML presentation of it (i.e. OMElement) and
     * uses the converted OMElement to enrich the message.
     * if there is a method named as <code>toXML</code> , invoke the method and take the XML.
     * Otherwise, axis2 ADB data binding is used
     *
     * @param description        Output ResourceDescription
     * @param value              POJO to be converted
     * @param context            the context to be used for looking up resources
     * @param messageInterceptor a helper class to locate resources from given context
     * @return <code>true</code> if the adaptation process is completed successfully.
     */
    public boolean adaptOutput(ResourceDescription description,
                               Object value,
                               Object context,
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

        if (value instanceof OMElement) {
            return false;
        }

        if (!(targetNode instanceof OMElement)) {
            throw new LoggedRuntimeException("The target node should have been an OMNode", log);
        }

        OMElement targetOMNode = (OMElement) targetNode;

        boolean baseType = ClassHelper.isWrapperType(value.getClass().getName());

        // if the type is basic , then set it as a text
        if (baseType) {
            targetOMNode.setText(String.valueOf(value));
        } else {

            // If the custom object provide the string that represents it's
            // internal data as a XML,then it will be used to replace the target node

            String methodName = "toXML";
            OMElement result = null;
            Method method = null;
            try {
                method = value.getClass().getMethod(methodName);
            } catch (NoSuchMethodException ignored) {

            }

            if (method != null) {
                try {
                    Object xml = method.invoke(value);
                    if (xml instanceof String) {
                        result = OMElementHelper.getInstance().toOM((String) xml);
                    } else if (xml instanceof OMElement) {
                        result = (OMElement) xml;
                    }
                } catch (InvocationTargetException e) {
                    throw new LoggedRuntimeException("Error invoking toXML() method " +
                            e.getMessage(),
                            e, log);
                } catch (IllegalAccessException e) {
                    throw new LoggedRuntimeException("Error invoking toXML() method " +
                            e.getMessage(),
                            e, log);
                }

            } else {
                result = OMElementHelper.getInstance().convertToOM(value,
                        getQName(description, value.getClass().getName(), targetOMNode),
                        description.getTypeTable());
            }

            if (result != null) {
                int nodeType = result.getType();

                if (OMElement.ELEMENT_NODE == nodeType) {

                    if (targetOMNode instanceof SOAPBody) {
                        OMElement firstElement = targetOMNode.getFirstElement();
                        if (firstElement != null) {
                            handleFirstChild(firstElement, result, description);
                        } else {
                            targetOMNode.addChild(result);
                        }
                    } else if (targetOMNode.getParent() instanceof SOAPBody) {
                        handleFirstChild(targetOMNode, result, description);
                    } else {
                        targetOMNode.addChild(result);
                    }
                } else if (OMElement.CDATA_SECTION_NODE == nodeType) {
                    targetOMNode.setText(result.getText());
                }
            }
        }
        return true;
    }

    /**
     * Checks the output is the type of the class given in the resource description
     *
     * @param description Output ResourceDescription
     * @param output      the output to be adapted
     * @return <code>true</code> if the output is the type of the class given in
     *         the resource description
     */
    public boolean canAdaptOutput(ResourceDescription description, Object output) {
        return output.getClass().getName().equals(description.getType());
    }

    /**
     * Convert the given POJO into a OMElement
     *
     * @param description information about target object
     * @param value       POJO to be converted into XML
     * @return <code>OMElement</code> for the given POJO
     */
    public Object adaptOutput(ResourceDescription description, Object value) {
        return OMElementHelper.getInstance().convertToOM(value,
                getQName(description, value.getClass().getName(), null), description.getTypeTable());
    }

    private QName getQName(ResourceDescription description,
                           String className,
                           OMElement targetOMNode) {

        QName qName = description.getElementQName();
        if (qName == null) {
            String cName = description.getName();
            if (cName == null || "".equals(cName)) {
                cName = className;
                if (cName.indexOf(".") != -1) {
                    cName = cName.substring(cName.lastIndexOf('.') + 1,
                            cName.length());
                }
            }
            if (targetOMNode != null) {
                OMNamespace namespace = targetOMNode.getNamespace();
                if (namespace != null && !(targetOMNode instanceof SOAPBody)) {
                    String nsURI = namespace.getNamespaceURI();
                    String preFix = namespace.getPrefix();
                    qName = new QName(nsURI, cName, preFix);
                }
            }
            if (qName == null) {
                qName = new QName(cName);
            }
        }
        return qName;
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
