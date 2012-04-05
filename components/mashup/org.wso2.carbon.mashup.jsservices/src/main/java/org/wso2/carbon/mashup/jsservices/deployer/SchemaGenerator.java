/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mashup.jsservices.deployer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.mashup.utils.MashupConstants;

import javax.xml.namespace.QName;

public class SchemaGenerator {

    private String schemaTargetNamespace;

    private String prefix = "ws";

    private XmlSchema xmlSchema;

    private String TYPE = "Type";

    private TypeTable typeTable = new TypeTable();

    private NamespaceMap nameSpacesMap = new NamespaceMap();

    /**
     * This class is used to genarate the XMLSchema for input and output messages on JavaScript
     * services
     *
     * @param schemaTargetNamespace The schema Target Namespace for this service
     */
    public SchemaGenerator(String schemaTargetNamespace) {
        this.schemaTargetNamespace = schemaTargetNamespace;

        // Initialize XmlSchema and XmlSchemaCollection
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchema = new XmlSchema(this.schemaTargetNamespace, xmlSchemaCollection);

        // Setting attributeFormDefault and elementFormDefault to unqualified
        xmlSchema.setAttributeFormDefault(new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED));
        xmlSchema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED));

        // Adding the schema Namespace Prefix and the default prefix to the namespace map
        nameSpacesMap.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                          Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        nameSpacesMap.put(prefix, this.schemaTargetNamespace);
        xmlSchema.setNamespaceContext(nameSpacesMap);
    }

    /**
     * This method creates a XmlSchemaElement for the input message
     *
     * @param message      The in AxisMessage
     * @param input        The input objets
     * @param functionName The operation name
     * @param params       The parameters of the function as a String array
     * @param methodName   The JavaScript method name
     * @return XmlSchemaElement A XmlSchemaElement corresponding to the input message
     * @throws AxisFault Thrown in case an exception occurs while genarating the schema
     */
    public XmlSchemaElement createInputElement(AxisMessage message, Object input,
                                               String functionName, String[] params,
                                               String methodName)
            throws AxisFault {
        return createElement(message, functionName, input, functionName, params, methodName);
    }

    /**
     * This method creates a XmlSchemaElement for the output message
     *
     * @param message      The out AxisMessage
     * @param output       The output objets
     * @param functionName The operation name
     * @param params       The output parameter of the function as a String array
     * @param methodName   The JavaScript method name
     * @return XmlSchemaElement A XmlSchemaElement corresponding to the output message
     * @throws AxisFault Thrown in case an exception occurs while genarating the schema
     */
    public XmlSchemaElement createOutputElement(AxisMessage message, Object output,
                                                String functionName, String[] params,
                                                String methodName)
            throws AxisFault {

        // If the output of the function is complex we need to get the parameter names of the
        // complex object into an array so that we can reuse the same function we use to genarate
        // schema for the input message
        if (output instanceof NativeObject) {
            NativeObject nativeObject = (NativeObject) output;
            Object[] objects = ScriptableObject.getPropertyIds(nativeObject);
            int length = objects.length;
            if (length == 0) {
                return null;
            }

            // Get all the parameter names into a String array
            String[] innerParams = new String[length];
            for (int j = 0; j < length; j++) {
                Object object = objects[j];
                if (object instanceof String) {
                    innerParams[j] = (String) object;
                }
            }
            return createElement(message, functionName, output,
                                 functionName + Java2WSDLConstants.RESPONSE,
                                 innerParams, methodName);
        } else if (output instanceof String) {
            String outputString = (String) output;

            // The XmlSchemaElement been null corresponds to #none in wsdl2
            if ("".equals(outputString) || "none".equals(outputString)) {
                return null;
            }
        }
        return createElement(message, functionName, output,
                             functionName + Java2WSDLConstants.RESPONSE,
                             params, methodName);
    }

    /**
     * A Native object in JavaScript means that it corresponds to a complex Type in XML schema.
     * Hence this method returns XmlSchemaElement which has a complex Type
     *
     * @param axisMessage  The corresponding AxisMessage
     * @param nativeObject The JsvsScript NativeObject that holds the type information
     * @param elementName  The element Name of this complex type
     * @param params       A String array which hold the parameter names
     * @param methodName   The JavaScript nethod name
     * @return XmlSchemaElement which wrappes the nativeObject
     * @throws AxisFault Thrown in case an exception occurs
     */
    private XmlSchemaElement handleNativeObject(AxisMessage axisMessage, NativeObject nativeObject,
                                                String elementName, String[] params,
                                                String methodName) throws AxisFault {
        XmlSchemaElement xmlSchemaElement;
        XmlSchemaComplexType complexType =
                createComplexType(axisMessage, elementName + TYPE, nativeObject, params,
                                  methodName);
        if (complexType == null) {
            return null;
        }
        xmlSchemaElement = createXMLSchemaElement(methodName,elementName, elementName + TYPE);
        xmlSchemaElement.setSchemaType(complexType);
        xmlSchemaElement.setQName(new QName(schemaTargetNamespace, elementName));
        return xmlSchemaElement;
    }

    /**
     * Given a set of params returns a JavaScript nativeObject object that holds those parameters
     *
     * @param params A String array that holds the parameters
     * @return NativeObject that wrapps the parameters
     */
    private NativeObject createNativeObject(String params[]) {
        NativeObject nativeObject = new NativeObject();
        for (int i = 0; i < params.length; i++) {
            NativeObject.putProperty(nativeObject, params[i].trim(), "any");
        }
        return nativeObject;
    }

    /**
     * This method creates a XmlSchemaElement for the message
     *
     * @param message      The AxisMessage
     * @param object       Object that represents the parameters
     * @param functionName The operation name
     * @param elementName  The element Name to be used
     * @param params       The parameters of the function as a String array
     * @param methodName   The JavaScript method name
     * @return XmlSchemaElement A XmlSchemaElement corresponding to the message
     * @throws AxisFault Thrown in case an exception occurs while genarating the schema
     */
    private XmlSchemaElement createElement(AxisMessage message, String functionName, Object object,
                                           String elementName,
                                           String[] params, String methodName)
            throws AxisFault {
        XmlSchemaElement xmlSchemaElement;
        if (object instanceof NativeObject) {
            NativeObject nativeObject = (NativeObject) object;
            xmlSchemaElement =
                    handleNativeObject(message, nativeObject, elementName, params, methodName);
            if (xmlSchemaElement != null) {
                message.addParameter(MashupConstants.ANNOTATED, Boolean.TRUE);
            } else {
                return null;
            }
        } else if (object instanceof String) {
            xmlSchemaElement = createComplexTypeFromString(message, functionName, params,
                                                           elementName, (String) object);
            if (xmlSchemaElement != null) {
                message.addParameter(MashupConstants.ANNOTATED, Boolean.TRUE);
            } else {
                return null;
            }
        } else if ((params.length == 1) && "".equals(params[0].trim())) {
            // If no annotations are present and if the function does not have any parameters no
            // need any schema stuff
            return null;
        } else {
            xmlSchemaElement = handleNativeObject(message, createNativeObject(params), elementName,
                                                  params, methodName);
        }
        if (xmlSchemaElement == null) {
            return null;
        }
        QName element =
                new QName(this.schemaTargetNamespace, elementName, this.prefix);
        xmlSchema.getItems().add(xmlSchemaElement);
        xmlSchema.getElements().add(element, xmlSchemaElement);
        return xmlSchemaElement;
    }

    /**
     * Given a String this function creates a complex Type for it
     *
     * @param axisMessage  The AxisMessage
     * @param functionName The operation name
     * @param params       The parameters as a String array
     * @param name         The name of the complex type
     * @param type         The type
     * @return XmlSchemaElement the created complex Type
     * @throws AxisFault Thrown in case an exception occurs
     */
    private XmlSchemaElement createComplexTypeFromString(AxisMessage axisMessage,
                                                         String functionName, String[] params,
                                                         String name,
                                                         String type) throws AxisFault {
        String paramName = params[0].trim();
        // If the type is none we just return null so that in the WSDL2 it will be #none and
        // WSDL 1.1 will show no input message
        if ("none".equalsIgnoreCase(type) ||
                ("".equals(paramName) && (("".equals(type)) || "#raw".equalsIgnoreCase(type)))) {
            return null;
        }
        if ("#raw".equalsIgnoreCase(type)) {
            axisMessage.setElementQName(Constants.XSD_ANY);
            return null;
        }
        if (params.length != 1 || "".equals(paramName)) {
            throw new AxisFault(
                    "The function " + functionName + " should contain one input parameter");
        }
        XmlSchemaElement xmlSchemaElement;
        QName element =
                new QName(this.schemaTargetNamespace, name + TYPE, this.prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
        complexType.setName(name + TYPE);
        xmlSchema.getItems().add(complexType);

        XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
        XmlSchemaElement schemaElement = createXMLSchemaElement(functionName,paramName, type);
        xmlSchemaSequence.getItems().add(schemaElement);
        complexType.setParticle(xmlSchemaSequence);
        typeTable.addComplexSchema(name + TYPE, element);
        xmlSchemaElement = createXMLSchemaElement(functionName,name, name + TYPE);
        xmlSchemaElement.setQName(new QName(schemaTargetNamespace, name));
        xmlSchemaElement.setSchemaType(complexType);
        return xmlSchemaElement;
    }

    /**
     * Given a JavaScript native object creates a complex type
     *
     * @param axisMessage The AxisMessage
     * @param elementName The name of the element
     * @param inputObject The JavaScript NativeObject
     * @param params      An array holding the parameters
     * @param method      The JavaScript method name
     * @return XmlSchemaComplexType whcih represents the JavaScript NativeObject
     * @throws AxisFault Thrown in case an exception occurs
     */
    private XmlSchemaComplexType createComplexType(AxisMessage axisMessage, String elementName,
                                                   NativeObject inputObject,
                                                   String[] params, String method)
            throws AxisFault {
        Object[] propertyIds = ScriptableObject.getPropertyIds(inputObject);
        int length = propertyIds.length;
        if (params.length == 1) {
            String param = params[0].trim();
            if ("".equals(param)) {
                if (length > 0) {
                    throw new AxisFault(
                            "The number of parameters defined in the annotation for " +
                                    method + " does not match the number of parameters defined " +
                                    "for the function");
                } else {
                    return null;
                }
            } else if ("none".equalsIgnoreCase(param)) {
                return null;
            } else if ("#raw".equals(param)) {
                axisMessage.setElementQName(Constants.XSD_ANY);
                return null;
            }
        } else if (params.length != propertyIds.length) {
            throw new AxisFault(
                    "The number of parameters defined in the annotation for " +
                            method +
                            " does not match the number of parameters defined for the function");
        }
        QName element =
                new QName(this.schemaTargetNamespace, elementName, this.prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
        complexType.setName(elementName);
        xmlSchema.getItems().add(complexType);

        XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
        for (int i = 0; i < params.length; i++) {
            XmlSchemaElement xmlSchemaElement;
            String paramName = params[i].trim();
            Object paramType = ScriptableObject.getProperty(inputObject, paramName);
            if (paramType == null) {
                throw new AxisFault("annotation for the parameter " + paramName + " not found.");
            } else if (paramType instanceof String) {
                if ((xmlSchemaElement = createXMLSchemaElement(method,paramName, paramType)) != null) {
                    xmlSchemaSequence.getItems().add(xmlSchemaElement);
                }
            } else if (paramType instanceof NativeObject) {
                NativeObject nativeObject = (NativeObject) paramType;
                Object[] objects = ScriptableObject.getPropertyIds(nativeObject);
                String[] innerParams = new String[objects.length];
                for (int j = 0; j < objects.length; j++) {
                    Object object = objects[j];
                    if (object instanceof String) {
                        innerParams[j] = (String) object;
                    }
                }
                if ((xmlSchemaElement = handleNativeObject(axisMessage, nativeObject,
                                                           elementName + paramName + TYPE,
                                                           innerParams, paramName)) != null) {
                    xmlSchemaSequence.getItems().add(xmlSchemaElement);
                }
            }
        }
        complexType.setParticle(xmlSchemaSequence);
        typeTable.addComplexSchema(elementName, element);
        return complexType;
    }

    /**
     * Given a name and a type creates an XmlSchemaElement for it
     * @param methodName The javascript method name
     * @param name      The name of the elemnt
     * @param paramType The type of the element
     * @return XmlSchemaElement representing the name and the type
     * @throws AxisFault Thrown in case an exception occurs
     */
    private XmlSchemaElement createXMLSchemaElement(String methodName,String name, Object paramType)
            throws AxisFault {
        XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
        xmlSchemaElement.setName(name);
        if (paramType instanceof String) {
            String param = (String) paramType;
            if ("".equals(param.trim())) {
                return null;
            }
            if (param.indexOf('|') > -1) {
                String[] enums = param.split("\\|");
                XmlSchemaSimpleType schemaSimpleType =
                        handleEnumeration(methodName+name + "EnumerationType", enums);
                xmlSchema.getItems().add(schemaSimpleType);
                xmlSchemaElement.setSchemaTypeName(schemaSimpleType.getQName());
                xmlSchemaElement.setSchemaType(schemaSimpleType);
                return xmlSchemaElement;
            }
            if (param.equalsIgnoreCase("array") || param.equalsIgnoreCase("xmlList")
                    || param.equalsIgnoreCase("object")) {
                xmlSchemaElement.setMaxOccurs(Long.MAX_VALUE);
                xmlSchemaElement.setMinOccurs(0);
            } else {
                switch (param.charAt(param.length() - 1)) {
                    case '*': {
                        // Handle arrays
                        xmlSchemaElement.setMaxOccurs(Long.MAX_VALUE);
                        xmlSchemaElement.setMinOccurs(0);
                        param = param.substring(0, param.length() - 1);
                        break;
                    }
                    case '+': {
                        xmlSchemaElement.setMaxOccurs(Long.MAX_VALUE);
                        param = param.substring(0, param.length() - 1);
                        break;
                    }
                    case '?': {
                        // Handle optional parameters
                        xmlSchemaElement.setMinOccurs(0);
                        param = param.substring(0, param.length() - 1);
                        break;
                    }

                }
            }
            QName qName = typeTable.getQNamefortheType(param);
            if (qName == null) {
                throw new AxisFault(
                        "No matching schematype could be found for the type : " + param);
            }
            xmlSchemaElement.setSchemaTypeName(qName);
            return xmlSchemaElement;
        }
        return null;
    }

    /**
     * Returns the schemaTargetNamespace for the service
     *
     * @return String The schemaTargetNamespace for the service
     */
    public String getSchemaTargetNamespace() {
        return schemaTargetNamespace;
    }

    /**
     * Returns the nameSpacesMap for the service
     *
     * @return NamespaceMap The nameSpacesMap for the service
     */
    public NamespaceMap getNamespaceMap() {
        return nameSpacesMap;
    }

    /**
     * Returns the complete schema for the service
     *
     * @return XmlSchema the complete schema for the service
     */
    public XmlSchema getSchema() {
        return xmlSchema;
    }

    /**
     * If the inputType annotations match the pattern of enumeration create the appropriate schema
     * element to handle that.
     *
     * @param name  - The name of the parameter
     * @param enums - Array of enumeration values
     * @return XmlSchemaSimpleType - An XmlSchemaSimpleType which has a restriction and the
     *         enumaration.
     */
    private XmlSchemaSimpleType handleEnumeration(String name, String[] enums) {
        XmlSchemaSimpleTypeRestriction simpleTypeRestriction = new XmlSchemaSimpleTypeRestriction();
        // Set the base type to string. 95% of the time enumerations are strings so use it.
        simpleTypeRestriction.setBaseTypeName(Constants.XSD_STRING);
        XmlSchemaSimpleType simpleType = new XmlSchemaSimpleType(xmlSchema);
        simpleType.setName(name);
        simpleType.setContent(simpleTypeRestriction);

        // Create enumeration facets for each value
        for (int i = 0; i < enums.length; i++) {

            String enumeration = enums[i].trim();
            XmlSchemaEnumerationFacet enumerationFacet = new XmlSchemaEnumerationFacet();
            enumerationFacet.setValue(enumeration);
            simpleTypeRestriction.getFacets().add(enumerationFacet);
        }

        return simpleType;
    }
}