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

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.DefaultNamespaceGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.NamespaceGenerator;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.wso2.carbon.rulecep.adapters.InputAdapterFactory;
import org.wso2.carbon.rulecep.adapters.OutputAdapterFactory;
import org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * TODO Some Code are copied from axis2 - need to review
 * Creates a WSDL for a Rule Service
 */
public class ServiceWSDLBuilder implements Java2WSDLConstants {

    private static final Log log = LogFactory.getLog(ServiceWSDLBuilder.class);
    public static final String NAME_SPACE_PREFIX = "rsns";// axis2 name space
    private int prefixCount = 1;
    private Map<String, String> targetNamespacePrefixMap = new Hashtable<String, String>();
    private XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
    private AxisService service;
    private Map<String, XmlSchema> schemaMap = new HashMap<String, XmlSchema>();
    private TypeTable typeTable = new TypeTable();
    private String schemaTargetNameSpace;
    private String schema_namespace_prefix;
    private String attrFormDefault = null;
    private String elementFormDefault = null;
    private boolean useWSDLTypesNamespace = false;
    private Map<String, String> pkg2nsmap = null;
    private NamespaceGenerator nsGen = null;
    private String targetNamespace = null;
    private boolean isGenerateWrappedArrayTypes = false;
    final List<String> names = new ArrayList<String>();
    private ServiceDescription serviceDescription;

    public ServiceWSDLBuilder(AxisService service,
                              ServiceDescription serviceDescription) {
        this.service = service;
        this.serviceDescription = serviceDescription;
    }

    /**
     * Starts the WSDL building process by setting target namespace , prefix , etc
     */
    public void startBuild() {

        this.targetNamespace = serviceDescription.getTargetNamespace();
        this.schemaTargetNameSpace = serviceDescription.getTargetNamespace();
        this.schema_namespace_prefix = serviceDescription.getTargetNSPrefix();

        if (this.targetNamespace == null) {
            this.targetNamespace = service.getTargetNamespace();
        }
        if (this.schemaTargetNameSpace == null) {
            this.schemaTargetNameSpace = service.getTargetNamespace();
        }
//        this.schema_namespace_prefix = service.getSchemaTargetNamespacePrefix();

        Parameter generateWrappedArrayTypes = service.getParameter("generateWrappedArrayTypes");
        if ((generateWrappedArrayTypes != null) &&
                JavaUtils.isTrue(generateWrappedArrayTypes.getValue())) {
            isGenerateWrappedArrayTypes = true;
        }
        getXmlSchema(schemaTargetNameSpace);
    }

    /**
     * Builds the WSDL In message part for the given operation.
     *
     * @param axisOperation <code>AxisOperation</code> instance
     * @param facts         A list of facts that defines the arguments of the WSDL In message
     * @param factory       <code>InputAdapterFactory</code> to be used to find registered fact adapters
     */
    public void buildInMessage(AxisOperation axisOperation,
                               List<ResourceDescription> facts,
                               InputAdapterFactory factory) {
        try {
            String methodName = axisOperation.getName().getLocalPart();
            String partQName = methodName + Java2WSDLConstants.MESSAGE_SUFFIX;
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            inMessage.setName(partQName);
            XmlSchemaSequence sequence = generateSchema(facts,
                    partQName, factory.getInputAdapters(), "parameter");
            XmlSchemaComplexType methodSchemaType =
                    createSchemaTypeForMethodPart(partQName);
            if (sequence != null) {
                methodSchemaType.setParticle(sequence);
            }
            QName elementQName = typeTable.getQNamefortheType(partQName);
            setParentElementQName(facts, elementQName);
            inMessage.setElementQName(elementQName);
            service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                    axisOperation);
        } catch (Exception e) {
            throw new LoggedRuntimeException("Error when preparing in-message " +
                    "of operation : " + axisOperation.getName(), e, log);
        }
    }

    /**
     * Builds the WSDL out message part for the given operation.
     *
     * @param axisOperation <code>AxisOperation</code> instance
     * @param results       A list of results that defines the arguments of the WSDL out message
     * @param factory       <code>OutputAdapterFactory</code> to be used to find registered result adapters
     */
    public void buildOutMessage(AxisOperation axisOperation,
                                List<ResourceDescription> results,
                                OutputAdapterFactory factory) {
        try {
            ResourceDescription firstDescription = results.get(0);
            if (!OMElementResourceAdapter.TYPE.equals(firstDescription.getType())) {
                throw new LoggedRuntimeException("Invalid result !! result type is invalid", log);
            }
            String methodName = axisOperation.getName().getLocalPart();
            String partQName = methodName + RESPONSE;
            AxisMessage outMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            outMessage.setName(partQName);
            XmlSchemaSequence sequence = generateSchema(firstDescription.getChildResources(),
                    partQName, factory.getOutputAdapters(), "item");
             XmlSchemaComplexType methodSchemaType =
                    createSchemaTypeForMethodPart(partQName);
            if(sequence != null){
                methodSchemaType.setParticle(sequence);
            }
            QName elementQName = typeTable.getQNamefortheType(partQName);
            setParentElementQName((List)results.get(0).getChildResources(), elementQName);
            outMessage.setElementQName(elementQName);
            firstDescription.setElementQName(elementQName);
            service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                    axisOperation);
        } catch (Exception e) {
            throw new LoggedRuntimeException("Error when preparing out-message " +
                    "of operation : " + axisOperation.getName(), e, log);
        }
    }

    /**
     * Ends the WSDL building process
     */
    public void endBuild() {
        service.addSchema(schemaMap.values());
    }

    /**
     * Helper method to generate WSDL message parts and required schema from a given
     * a list of resource descriptions
     *
     * @param descriptions a list of resource descriptions
     * @param partName     part name of the WSDL message
     * @param types        a set of registered adapter types
     * @param id           default id to be used to named elements in the parts
     * @return <code> XmlSchemaComplexType</code> for a operation
     */
    private XmlSchemaSequence generateSchema(Collection<ResourceDescription> descriptions,
                                             String partName,
                                             Collection<String> types, String id) {

        if (descriptions.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no resources to generate schema types for part name :" +
                        partName);
            }
            return null;
        }

        int number = 0;
        XmlSchemaSequence sequence = new XmlSchemaSequence();
        for (ResourceDescription description : descriptions) {

            if (description == null) {
                continue;
            }

            String name = description.getName();
            if (!types.contains(description.getType())) {
                String className = description.getType();
                if (className == null) {
                    continue;
                }

                description.setTypeTable(typeTable);
                description.setResourceClassLoader(service.getClassLoader());
                boolean isSimpleType = (typeTable.getSimpleSchemaTypeName(className) != null);
                if (!names.contains(className) && !isSimpleType) {
                    generateSchemaFroClass(className);
                    names.add(className);
                }

                if (name == null || "".equals(name)) {
                    String cName = className;
                    if (cName.indexOf(".") != -1) {
                        name = cName.substring(cName.lastIndexOf('.') + 1,
                                cName.length());
//                            String firstChar = Character.toString(name.charAt(0)).toLowerCase();
//                            name = firstChar + name.substring(1);
                    }
                }

                if (name == null || "".equals(name)) {
                    name = id + number;
                    number++;
                }

                XmlSchemaElement el = new XmlSchemaElement();
                if (isSimpleType) {
                    el.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(className));
                } else {
                    setSchemaTypeOnElement(el, className);
                }
                sequence.getItems().add(el);
                description.setElementQName(
                        new QName(schemaTargetNameSpace, name, schema_namespace_prefix));
                el.setName(name);
                el.setMinOccurs(0);
                el.setMaxOccurs(Long.MAX_VALUE);
            } else {
                XmlSchemaAny el = new XmlSchemaAny();
                sequence.getItems().add(el);
            }
        }

        return sequence;

    }

    /**
     * Sets the Schema Type Name to the given  XmlSchemaElement
     *
     * @param el        <code >XmlSchemaElement</code>
     * @param className Class name to be used to find the  ComplexSchemaType using type-table
     */
    private void setSchemaTypeOnElement(XmlSchemaElement el, String className) {
        Collection<XmlSchema> xmlSchemas = schemaMap.values();
        for (XmlSchema schema : xmlSchemas) {
            Iterator<?> iterator = schema.getItems().getIterator();
            while (iterator.hasNext()) {

                XmlSchemaObject object = (XmlSchemaObject) iterator.next();
                if (!(object instanceof XmlSchemaType)) {
                    continue;
                }

                QName qName =
                        ((XmlSchemaType) object).getQName();
                if (qName == null) {
                    continue;
                }

                if (qName.equals(typeTable.getComplexSchemaType(className))) {
                    el.setSchemaTypeName(qName);
                    addImport(getXmlSchema(schemaTargetNameSpace), qName);
                    return;
                } else if (qName.equals(typeTable.getSimpleSchemaTypeName(className))) {
                    el.setSchemaTypeName(qName);
                    addImport(getXmlSchema(schemaTargetNameSpace), qName);
                    return;
                }
            }
        }
    }

    private void addImport(XmlSchema xmlSchema, QName schemaTypeName) {
        NamespacePrefixList map = xmlSchema.getNamespaceContext();
        if (map == null ||
                ((map instanceof NamespaceMap) && ((NamespaceMap) map).values() == null) ||
                schemaTypeName == null) {
            return;
        }
        if (map instanceof NamespaceMap && !((NamespaceMap) map).values().
                contains(schemaTypeName.getNamespaceURI())) {
            XmlSchemaImport importElement = new XmlSchemaImport();
            importElement.setNamespace(schemaTypeName.getNamespaceURI());
            xmlSchema.getItems().add(importElement);
            ((NamespaceMap) xmlSchema.getNamespaceContext()).
                    put(generatePrefix(), schemaTypeName.getNamespaceURI());
        }
    }

    /**
     * Helper method to generate a schema for a Java Type
     *
     * @param className Java Type
     */
    private void generateSchemaFroClass(String className) {
        try {
            POJO2SchemaGenerator schemaGenerator = new POJO2SchemaGenerator(
                    service.getClassLoader(),
                    className, null, null, service);
            schemaGenerator.generateSchema();
            Map map = schemaGenerator.getTypeTable().getComplexSchemaMap();
            for (Object typeName : map.keySet()) {
                typeTable.addComplexSchema((String) typeName, (QName) map.get(typeName));
            }
            mergeTwoSchemaTypes(schemaGenerator.getSchemaMap());
        } catch (Exception e) {
            throw new LoggedRuntimeException("Error generating schema", e, log);
        }
    }

    /**
     * Helper methods to merge two schema types
     *
     * @param xmlSchemaMap XmlSchema types map to be merged into main XmlSchema map
     */
    private void mergeTwoSchemaTypes(Map<String, XmlSchema> xmlSchemaMap) {
        for (String ns : xmlSchemaMap.keySet()) {

            XmlSchema tobeAdd = xmlSchemaMap.get(ns);
            if (!schemaMap.containsKey(ns)) {
                schemaMap.put(ns, tobeAdd);
                continue;
            }

            XmlSchema xmlSchema = schemaMap.get(ns);
            Iterator<?> iterator = tobeAdd.getItems().getIterator();
            while (iterator.hasNext()) {

                XmlSchemaObject object = (XmlSchemaObject) iterator.next();
                if (!(object instanceof XmlSchemaType)) {
                    continue;
                }

                XmlSchemaType xmlSchemaType = (XmlSchemaType) object;
                QName qName = xmlSchemaType.getQName();

                boolean canAdd = false;
                XmlSchemaObjectCollection collection = xmlSchema.getItems();
                Iterator<?> it = collection.getIterator();
                while (it.hasNext()) {
                    XmlSchemaObject schemaObject = (XmlSchemaObject) it.next();
                    if (!(schemaObject instanceof XmlSchemaType)) {
                        continue;
                    }

                    XmlSchemaType type = (XmlSchemaType) schemaObject;
                    if (!qName.equals(type.getQName())) {
                        canAdd = true;
                        break;
                    }
                }
                if (canAdd) {
                    collection.add(object);
                }
            }
        }
    }

    private void setParentElementQName(List<ResourceDescription> descriptions, QName parent) {
        for (ResourceDescription description : descriptions) {
            if (description != null) {
                description.setParentElementQName(parent);
            }
        }
    }

    /**
     * * Following all Code copied from the Axis2 - TODO**
     */

    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {

        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);
        XmlSchemaComplexType
                complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaElement globalElement = new XmlSchemaElement();
            globalElement.setSchemaType(complexType);
            globalElement.setName(localPartName);
            globalElement.setQName(elementName);
            xmlSchema.getItems().add(globalElement);
            xmlSchema.getElements().add(elementName, globalElement);
        }
        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchemaComplexType createComplexTypeForWrapper(String localPartName) {

        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);
        XmlSchemaComplexType
                complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);
            complexType.setName(localPartName);
            xmlSchema.getItems().add(complexType);
        }
        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix;

            if (targetNamespace.equals(schemaTargetNameSpace) &&
                    schema_namespace_prefix != null) {
                targetNamespacePrefix = schema_namespace_prefix;
            } else {
                targetNamespacePrefix = generatePrefix();
            }
            xmlSchema = new XmlSchema(targetNamespace, xmlSchemaCollection);
            xmlSchema.setAttributeFormDefault(getAttrFormDefaultSetting());
            xmlSchema.setElementFormDefault(getElementFormDefaultSetting());

            targetNamespacePrefixMap.put(targetNamespace, targetNamespacePrefix);
            schemaMap.put(targetNamespace, xmlSchema);

            NamespaceMap prefixmap = new NamespaceMap();
            prefixmap.put(DEFAULT_SCHEMA_NAMESPACE_PREFIX, URI_2001_SCHEMA_XSD);
            prefixmap.put(targetNamespacePrefix, targetNamespace);
            xmlSchema.setNamespaceContext(prefixmap);
        }
        return xmlSchema;
    }

    private String generatePrefix() {
        return NAME_SPACE_PREFIX + prefixCount++;
    }

    private String getAttrFormDefault() {
        return attrFormDefault;
    }

    private String getElementFormDefault() {
        return elementFormDefault;
    }

    private XmlSchemaForm getAttrFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getAttrFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    private XmlSchemaForm getElementFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getElementFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    private NamespaceGenerator getNsGen() throws Exception {
        if (nsGen == null) {
            nsGen = new DefaultNamespaceGenerator();
        }
        return nsGen;
    }

    private String resolveSchemaNamespace(String packageName) throws Exception {
        //if all types must go into the wsdl types schema namespace
        if (useWSDLTypesNamespace) {
            //return schemaTargetNameSpace;
            return pkg2nsmap.get("all");
        } else {
            if (pkg2nsmap != null && !pkg2nsmap.isEmpty()) {
                //if types should go into namespaces that are mapped against the package name for the type
                if (pkg2nsmap.get(packageName) != null) {
                    //return that mapping
                    return pkg2nsmap.get(packageName);
                } else {
                    return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
                }
            } else {
                // if  pkg2nsmap is null and if not default schema ns found for the custom bean
                return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
            }
        }
    }

    private XmlSchemaComplexType getComplexTypeForElement(XmlSchema xmlSchema, QName name) {
        Iterator<XmlSchemaObject> iterator = xmlSchema.getItems().getIterator();
        while (iterator.hasNext()) {
            XmlSchemaObject object = iterator.next();
            if (object instanceof XmlSchemaElement
                    && ((XmlSchemaElement) object).getQName().equals(name)) {
                return (XmlSchemaComplexType) ((XmlSchemaElement) object).getSchemaType();
            } else if (object instanceof XmlSchemaComplexType
                    && ((XmlSchemaComplexType) object).getQName().equals(name)) {
                //return (XmlSchemaComplexType) ((XmlSchemaElement) object).getSchemaType();
                return (XmlSchemaComplexType) object;
            }

        }
        return null;
    }
}
