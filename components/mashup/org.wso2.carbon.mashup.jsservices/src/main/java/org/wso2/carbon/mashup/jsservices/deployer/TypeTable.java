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

import org.apache.ws.commons.schema.constants.Constants;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * This is the table that holds the js annotation types to xs types mappings. Though the js services accepts types as
 * js types in schema all of them need to be xs types. This is the table to look up in order to schieve that.
 */
public class TypeTable {

    private HashMap<String, QName> simpleTypetoxsd;
    private HashMap<String, QName> complexTypeMap;

    public TypeTable() {
        simpleTypetoxsd = new HashMap<String, QName>();
        complexTypeMap = new HashMap<String, QName>();
        populateSimpleTypes();
    }

    private void populateSimpleTypes() {
        // Support for xsd datatypes
        simpleTypetoxsd.put("xs:string", Constants.XSD_STRING);
        simpleTypetoxsd.put("xs:normalizedString", Constants.XSD_NORMALIZEDSTRING);
        simpleTypetoxsd.put("xs:token", Constants.XSD_TOKEN);
        simpleTypetoxsd.put("xs:language", Constants.XSD_LANGUAGE);
        simpleTypetoxsd.put("xs:Name", Constants.XSD_NAME);
        simpleTypetoxsd.put("xs:NCName", Constants.XSD_NCNAME);
        simpleTypetoxsd.put("xs:NOTATION", Constants.XSD_NOTATION);
        simpleTypetoxsd.put("xs:anyURI", Constants.XSD_ANYURI);
        simpleTypetoxsd.put("xs:float", Constants.XSD_FLOAT);
        simpleTypetoxsd.put("xs:double", Constants.XSD_DOUBLE);
        simpleTypetoxsd.put("xs:duration", Constants.XSD_DURATION);
        simpleTypetoxsd.put("xs:integer", Constants.XSD_INTEGER);
        simpleTypetoxsd.put("xs:nonPositiveInteger", Constants.XSD_NONPOSITIVEINTEGER);
        simpleTypetoxsd.put("xs:negativeInteger", Constants.XSD_NEGATIVEINTEGER);
        simpleTypetoxsd.put("xs:long", Constants.XSD_LONG);
        simpleTypetoxsd.put("xs:int", Constants.XSD_INT);
        simpleTypetoxsd.put("xs:short", Constants.XSD_SHORT);
        simpleTypetoxsd.put("xs:byte", Constants.XSD_BYTE);
        simpleTypetoxsd.put("xs:nonNegativeInteger", Constants.XSD_NONNEGATIVEINTEGER);
        simpleTypetoxsd.put("xs:unsignedLong", Constants.XSD_UNSIGNEDLONG);
        simpleTypetoxsd.put("xs:unsignedInt", Constants.XSD_UNSIGNEDINT);
        simpleTypetoxsd.put("xs:unsignedShort", Constants.XSD_UNSIGNEDSHORT);
        simpleTypetoxsd.put("xs:unsignedByte", Constants.XSD_UNSIGNEDBYTE);
        simpleTypetoxsd.put("xs:positiveInteger", Constants.XSD_POSITIVEINTEGER);
        simpleTypetoxsd.put("xs:decimal", Constants.XSD_DECIMAL);
        simpleTypetoxsd.put("xs:boolean", Constants.XSD_BOOLEAN);
        simpleTypetoxsd.put("xs:dateTime", Constants.XSD_DATETIME);
        simpleTypetoxsd.put("xs:date", Constants.XSD_DATE);
        simpleTypetoxsd.put("xs:time", Constants.XSD_TIME);
        simpleTypetoxsd.put("xs:gYearMonth", Constants.XSD_YEARMONTH);
        simpleTypetoxsd.put("xs:gMonthDay", Constants.XSD_MONTHDAY);
        simpleTypetoxsd.put("xs:gYear", Constants.XSD_YEAR);
        simpleTypetoxsd.put("xs:gDay", Constants.XSD_DAY);
        simpleTypetoxsd.put("xs:gMonth", Constants.XSD_MONTH);
        simpleTypetoxsd.put("xs:QName", Constants.XSD_QNAME);
        simpleTypetoxsd.put("xs:hexBinary", Constants.XSD_HEXBIN);
        simpleTypetoxsd.put("xs:base64Binary", Constants.XSD_BASE64);
        simpleTypetoxsd.put("xs:anyType", Constants.XSD_ANYTYPE);

        // Support for JS data types
        simpleTypetoxsd.put("string", Constants.XSD_STRING);
        simpleTypetoxsd.put("String", Constants.XSD_STRING);
        simpleTypetoxsd.put("Number", Constants.XSD_DOUBLE);
        simpleTypetoxsd.put("number", Constants.XSD_DOUBLE);
        simpleTypetoxsd.put("Boolean", Constants.XSD_BOOLEAN);
        simpleTypetoxsd.put("boolean", Constants.XSD_BOOLEAN);
        simpleTypetoxsd.put("Date", Constants.XSD_DATETIME);
        simpleTypetoxsd.put("date", Constants.XSD_DATETIME);
        simpleTypetoxsd.put("array", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("Array", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("Xml", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("XML", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("xml", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("Xmllist", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("XMLList", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("XMLlist", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("xmlList", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("xmllist", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("Object", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("object", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("any", Constants.XSD_ANYTYPE);
        simpleTypetoxsd.put("Any", Constants.XSD_ANYTYPE);

    }

    /**
     * Retuns the QNAme of the simple type given its name
     *
     * @param typeName - The type given in the annotation
     * @return -QName Representing the annotation type as a xs Type
     */
    public QName getSimpleSchemaTypeName(String typeName) {
        return simpleTypetoxsd.get(typeName);
    }

    /**
     * Adds a complex type to the complexTypeMap
     *
     * @param name       - The name of the complex type
     * @param schemaType - The schema Type of the complexType
     */
    public void addComplexSchema(String name, QName schemaType) {
        complexTypeMap.put(name, schemaType);
    }

    /**
     * Retuns the QNAme of the complex type given its name
     *
     * @param name - The name of the complexType
     * @return - The QName of the cmplexType
     */
    public QName getComplexSchemaType(String name) {
        return complexTypeMap.get(name);
    }

    /**
     * Given an annotation type returns the corresponding xs type
     *
     * @param typeName - The type given in the annotation
     * @return -QName Representing the annotation type as a xs Type
     */
    public QName getQNamefortheType(String typeName) {
        QName type = getSimpleSchemaTypeName(typeName);
        if (type == null) {
            type = getComplexSchemaType(typeName);
        }
        return type;
    }
}