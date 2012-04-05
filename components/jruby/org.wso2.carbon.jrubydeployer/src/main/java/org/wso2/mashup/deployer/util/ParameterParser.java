/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.mashup.deployer.util;

import org.apache.axiom.om.OMElement;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.types.Duration;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 6, 2009
 * Time: 4:51:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParameterParser {

    public static Object[] getParameters(XmlSchemaComplexType complexType, OMElement payload,
                                            List<String> paramNames) throws AxisFault {

        return handleComplexTypeInRequest(complexType,payload,paramNames).toArray();
    }

    private static List<Object> handleComplexTypeInRequest(XmlSchemaComplexType complexType, OMElement payload,
                                            List<String> paramNames) throws AxisFault {
            XmlSchemaParticle particle = complexType.getParticle();
        List<Object> params = new ArrayList<Object>();
        if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
            Iterator iterator = xmlSchemaSequence.getItems().getIterator();
            // now we need to know some information from the
            // binding operation.
            while (iterator.hasNext()) {
                XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                XmlSchemaType schemaType = innerElement.getSchemaType(); 
                if (schemaType instanceof XmlSchemaComplexType) {
                    String innerElementName = innerElement.getName();
                    QName payloadQname = payload.getQName();
                    OMElement complexTypePayload = null;
                    QName qName;
                    // When retrieving elements we have to look for the namespace too. There may be
                    // Ocations even when the children are namespace qualified. In such situations
                    // we should retrieve them using that namespace.
                    // If that fails we try using the localname only.
                    if (payloadQname != null) {
                        qName = new QName(payloadQname.getNamespaceURI(),
                                innerElementName);
                        complexTypePayload = payload.getFirstChildWithName(qName);
                    }
                    if (complexTypePayload == null) {
                        qName = new QName(innerElementName);
                        complexTypePayload = payload.getFirstChildWithName(qName);
                    }
                    if (complexTypePayload == null) {
                        throw new AxisFault(
                                "Required element " + complexType.getName()
                                        + " defined in the schema can not be found in the request");
                    }
                    List<String> innerParamNames = new ArrayList<String>();
                    List<Object> innerParams = handleComplexTypeInRequest((XmlSchemaComplexType) schemaType,
                            complexTypePayload,
                            innerParamNames);
                    params.add(innerParams);

                } else if (schemaType instanceof XmlSchemaSimpleType) {
                    // Handle enumerations in here.
                    /*
                    XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) schemaType;
                    XmlSchemaSimpleTypeContent content = xmlSchemaSimpleType.getContent();
                    if (content instanceof XmlSchemaSimpleTypeRestriction) {
                        // TODO: This feature will also be implemented in the future

                    } else {
                        throw new AxisFault("Unsupported restriction in Schema");
                    } */

                    params.add(handleSimpleTypeInRequest(payload, innerElement));
                    paramNames.add(innerElement.getName());

                } else {
                    params.add(handleSimpleTypeInRequest(payload, innerElement));
                    paramNames.add(innerElement.getName());
                }
            }
        }
        else {
            throw new AxisFault("Unsupported schema type in request");
        }
        return params;
    }


     private static Object handleSimpleTypeInRequest(OMElement payload, XmlSchemaElement innerElement) throws AxisFault {
        long maxOccurs = innerElement.getMaxOccurs();
        // Check whether the schema advertises this element as an array
        if (maxOccurs > 1) {
            // If its an array get all elements with that name and create a sinple parameter out of it
            String innerElemenrName = innerElement.getName();
            Iterator iterator1 = payload.getChildrenWithName(new QName(
                    innerElemenrName));
            return handleArray(iterator1, innerElement.getSchemaTypeName());
        } else {
            return handleSimpleElement(payload, innerElement.getName(),
                    innerElement.getMinOccurs(),
                    innerElement.getSchemaTypeName());
        }
    }


    private static Object handleArray(Iterator iterator, QName type)
            throws AxisFault {
        ArrayList objectList = new ArrayList();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            objectList.add(createParam(omElement, type));
        }
        return objectList;
    }

    private static Object createParam(OMElement omElement, QName type)
                throws AxisFault {


            if (Constants.XSD_ANYTYPE.equals(type)) {

            }
            String value = omElement.getText();
            if (value == null) {
                throw new AxisFault(
                        "The value of Element " + omElement.getLocalName() + " cannot be null");
            }
            if (Constants.XSD_BOOLEAN.equals(type)) {
                try {
                    return ConverterUtil.convertToBoolean(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "boolean"));
                }
            }
            if (Constants.XSD_DOUBLE.equals(type)) {
                try {
                    return ConverterUtil.convertToDouble(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "double"));
                }
            }
            if (Constants.XSD_FLOAT.equals(type)) {
                try {
                    return ConverterUtil.convertToFloat(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "float"));
                }
            }
            if (Constants.XSD_INT.equals(type)) {
                try {
                    return ConverterUtil.convertToInt(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "int"));
                }
            }
            if (Constants.XSD_INTEGER.equals(type)) {
                try {
                    if ((value == null) || value.equals("")) {
                        return Integer.MIN_VALUE;
                    }
                    if (value.startsWith("+")) {
                        value = value.substring(1);
                    }
                    return new Integer(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "integer"));
                }
            }
            if (Constants.XSD_POSITIVEINTEGER.equals(type)) {
                try {
                    return ConverterUtil.convertToPositiveInteger(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "positive integer"));
                }
            }
            if (Constants.XSD_NEGATIVEINTEGER.equals(type)) {
                try {
                    return ConverterUtil.convertToNegativeInteger(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "negative integer"));
                }
            }
            if (Constants.XSD_NONPOSITIVEINTEGER.equals(type)) {
                try {
                    return ConverterUtil.convertToNonPositiveInteger(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "non-positive integer"));
                }
            }
            if (Constants.XSD_NONNEGATIVEINTEGER.equals(type)) {
                try {
                    return ConverterUtil.convertToNonNegativeInteger(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "non-negative integer"));
                }
            }
            if (Constants.XSD_LONG.equals(type)) {
                try {
                    return ConverterUtil.convertToLong(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "long"));
                }
            }
            if (Constants.XSD_SHORT.equals(type)) {
                try {
                    return ConverterUtil.convertToShort(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "short"));
                }
            }
            if (Constants.XSD_BYTE.equals(type)) {
                try {
                    return ConverterUtil.convertToByte(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "byte"));
                }
            }
            if (Constants.XSD_UNSIGNEDINT.equals(type)) {
                try {
                    return ConverterUtil.convertToUnsignedInt(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "unsigned int"));
                }
            }
            if (Constants.XSD_UNSIGNEDLONG.equals(type)) {
                try {
                    return ConverterUtil.convertToUnsignedLong(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "unsigned long"));
                }
            }
            if (Constants.XSD_UNSIGNEDSHORT.equals(type)) {
                try {
                    return ConverterUtil.convertToUnsignedShort(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "unsigned short"));
                }
            }
            if (Constants.XSD_UNSIGNEDBYTE.equals(type)) {
                try {
                    return ConverterUtil.convertToUnsignedByte(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "unsigned byte"));
                }
            }
            if (Constants.XSD_DECIMAL.equals(type)) {
                try {
                    return ConverterUtil.convertToDecimal(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "decimal"));
                }
            }
            if (Constants.XSD_DURATION.equals(type)) {
                try {
                    Duration duration = ConverterUtil.convertToDuration(value);
                    return duration.toString();
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "duration"));
                }
            }
            if (Constants.XSD_QNAME.equals(type)) {

            }
            if (Constants.XSD_HEXBIN.equals(type)) {
                try {
                    return ConverterUtil.convertToString(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "hexBinary"));
                }
            }
            if (Constants.XSD_BASE64.equals(type)) {
                try {
                    return ConverterUtil.convertToString(value);
                } catch (Exception e) {
                    throw new AxisFault(getFaultString(value, "base64Binary"));
                }
            }
            return omElement.getText();
        }


    private static String getFaultString(String value, String type) {
        return "Unable to convert value \"" + value + "\" to " + type;
    }


    private static Object handleSimpleElement(OMElement payload,
                                       String innerElementName, long minOccurs, QName schemaType)
            throws AxisFault {
        QName payloadQname = payload.getQName();
        OMElement omElement = null;
        QName qName;
        // When retrieving elements we have to look for the namespace too. There may be
        // Occations even when the children are namespace qualified. In such situations
        // we should retrieve them using that namespace.
        // If that fails we try using the localname only.
        if (payloadQname != null) {
            qName = new QName(payloadQname.getNamespaceURI(),
                    innerElementName);
            omElement = payload.getFirstChildWithName(qName);
        }
        if (omElement == null) {
            qName = new QName(innerElementName);
            omElement = payload.getFirstChildWithName(qName);
        }
        if (omElement == null) {
            // There was no such element in the payload. Therefore we check for minoccurs
            // and if its 0 add null as a parameter (If not we might mess up the parameters
            // we pass into the function).
            if (minOccurs == 0) {
                //return Undefined.instance;
                return null;
            } else {
                // If minoccurs is not zero throw an exception.
                // Do we need to di strict schema validation?
                throw new AxisFault(
                        "Required element " + innerElementName
                                + " defined in the schema can not be found in the request");
            }
        }
        return createParam(omElement, schemaType);
    }
}
