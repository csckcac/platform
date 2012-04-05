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

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.jruby.*;



import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 11, 2009
 * Time: 3:49:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseBuilder {

    public static void handleComplexTypeInResponse(XmlSchemaComplexType complexType, OMElement outElement,
                                                 Object response,
                                                 OMFactory fac,
                                                 boolean isInnerParam) throws AxisFault {
            XmlSchemaParticle particle = complexType.getParticle();
            if (particle instanceof XmlSchemaSequence) {
                XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                XmlSchemaObjectCollection schemaObjectCollection = xmlSchemaSequence.getItems();
                int count = schemaObjectCollection.getCount();
                Iterator iterator = schemaObjectCollection.getIterator();
                int responseIndex=0;



                // now we need to know some information from the binding operation.
                while (iterator.hasNext()) {
                    XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                    String name = innerElement.getName();
                    XmlSchemaType schemaType = innerElement.getSchemaType();
                    if (schemaType instanceof XmlSchemaComplexType) {
                        Object object = response;

                        if (checkRequired(innerElement, object)) {
                            continue;
                        }
                        XmlSchemaComplexType innerComplexType = (XmlSchemaComplexType) schemaType;
                        OMElement complexTypeElement =
                                fac.createOMElement(name, outElement.getNamespace());
                        outElement.addChild(complexTypeElement);

                        handleComplexTypeInResponse(innerComplexType, complexTypeElement, object, fac,
                                true);
                    }
                    else if(response instanceof RubyArray || response instanceof RubyObject[] ){
                        Object res=null;
                             if(response instanceof RubyArray){

                                RubyArray temp = (RubyArray)response;
                                res = (temp.toJavaArray())[responseIndex];
                             }
                             else if (response instanceof RubyObject[]){
                                RubyObject[] temp = (RubyObject [])response;
                                res = temp[responseIndex];
                             }
                          if (checkRequired(innerElement, res)) {
                            if (innerElement.getSchemaTypeName() != Constants.XSD_ANYTYPE) {
                                continue;
                            }
                        }
                        handleSimpleTypeinResponse(innerElement, res, fac,
                                outElement);


                    }
                    else {
                        Object object = response;
                        if (isInnerParam || count > 1) {

                        } else {
                            object = response;
                        }
                        if (checkRequired(innerElement, object)) {
                            if (innerElement.getSchemaTypeName() != Constants.XSD_ANYTYPE) {
                                continue;
                            }
                        }
                        handleSimpleTypeinResponse(innerElement, object, fac,
                                outElement);
                    }
                    responseIndex++;
                }
            } else {
                throw new AxisFault("Unsupported schema type in response.");
            }
        }


    private static boolean checkRequired(XmlSchemaElement innerElement, Object object) throws AxisFault {
           if (object==null) {
               if (innerElement.getSchemaTypeName() == Constants.XSD_ANYTYPE ||
                       innerElement.getMinOccurs() == 0) {
                   return true;
               }
               throw new AxisFault("Required element " + innerElement.getName() + " of type " +
                       innerElement.getSchemaTypeName() +
                       " was not found in the response");
           }
           return false;
       }

     private static void handleSimpleTypeinResponse(XmlSchemaElement innerElement, Object RubyObject,
                                            OMFactory factory,
                                            OMElement outElement) throws AxisFault {
        long maxOccurs = innerElement.getMaxOccurs();
        if (maxOccurs > 1 && !innerElement.getSchemaTypeName().equals(Constants.XSD_ANYTYPE)) {
            if (RubyObject instanceof Object[]) {
                Object[] objects = (Object[]) RubyObject;
                for (Object object : objects) {
                    outElement.addChild(handleSchemaTypeinResponse(innerElement, object,
                            factory));
                }
            } else {
                outElement.addChild(handleSchemaTypeinResponse(innerElement, RubyObject, factory));
            }
            return;
        }
        outElement.addChild(
                handleSchemaTypeinResponse(innerElement, RubyObject, factory));
    }


    private static OMElement handleSchemaTypeinResponse(XmlSchemaElement innerElement, Object RubyObject,
                                                 OMFactory factory)
            throws AxisFault {
        QName qName = innerElement.getSchemaTypeName();
        OMElement element = factory.createOMElement(innerElement.getName(), null);
        if (qName.equals(Constants.XSD_ANYTYPE)) {
            if (RubyObject==null) {
                return element;
            }
            // need to set annotated as false cause we need to set xsi:type
            return buildResponse(false, RubyObject, innerElement);
        }
        if (qName.equals(Constants.XSD_INTEGER)) {
           String str = RubyToOMConverter.convertToInteger(RubyObject);
           element.setText(str);
            return element;
        }
        if (qName.equals(Constants.XSD_INT)) {
            String str = RubyToOMConverter.convertToInt(RubyObject);
            element.setText(str);
            return element;
        }
        if (qName.equals(Constants.XSD_FLOAT)) {
            String str = RubyToOMConverter.convertToFloat(RubyObject);
            element.setText(str);
            return element;
        }
        if (qName.equals(Constants.XSD_DOUBLE)) {
            String str = RubyToOMConverter.convertToFloat(RubyObject);
            element.setText(str);
            return element;
        }

        if (qName.equals(Constants.XSD_LONG)) {
            String str = RubyToOMConverter.convertToLong(RubyObject);
            element.setText(str);
            return element;
        }
        if (qName.equals(Constants.XSD_STRING)) {
            String str = RubyToOMConverter.convertToString(RubyObject);
            element.setText(str);
            return element;
        }
         if (qName.equals(Constants.XSD_BOOLEAN)) {
            String str = RubyToOMConverter.convertToBoolean(RubyObject);
            element.setText(str);
            return element;
        }
        element.setText(RubyObject.toString());
        
        return element;
    }

    public static OMElement buildResponse(boolean annotated, Object result,
                                       XmlSchemaElement innerElement) throws AxisFault {
           // Check whether the innerElement is null.
           if (innerElement != null) {
               return createResponseElement(result, innerElement.getName(), !annotated);
           } else {

               // wrap the response in a wrapper element called return. If the wrapper contains an OMElemet return the
               // OMElement (There is no use in the wrapper).
               // There are occations when the function returns a py type which is not XML.
               // Therefore we need the wrapper element tp wrap it. What if the service returned null? just return null.
               OMElement element = createResponseElement(result, "return", !annotated);
               OMElement omElement = element.getFirstElement();
               if (omElement == null) {
                   String elementText = element.getText();
                   if (elementText == null || "".equals(elementText)) {
                       return null;
                   }
                   return element;
               } else if (omElement.getNextOMSibling() != null) {
                   return element;
               } else {
                   return omElement;
               }
           }
       }

    private static OMElement createResponseElement(Object RubyObject, String elementName,
                                                boolean addTypeInfo) throws AxisFault {
            //String className = RubyObject.getClass().getName();
            OMFactory fac = OMAbstractFactory.getOMFactory();
            //OMNamespace namespace = fac.createOMNamespace("http://www.wso2.org/ns/Ruby    type", "Ruby");
            OMNamespace xsiNamespace = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            OMNamespace xsNamespace = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema", "xs");
            OMElement element = fac.createOMElement(elementName, null);

            // Get the OMNode inside the RubyObjecting object
            if (RubyObject instanceof RubyString) {
                element.setText((String) RubyObject);
                if (addTypeInfo) {
                    element.declareNamespace(xsNamespace);
                    element.addAttribute("type", "xs:string", xsiNamespace);
                }
            } else if (RubyObject instanceof RubyBignum) {
                element.setText(RubyObject.toString());
                if (addTypeInfo) {
                    element.declareNamespace(xsNamespace);
                    element.addAttribute("type", "xs:integer", xsiNamespace);
                }
            } else if (RubyObject instanceof RubyFloat) {
                element.setText(RubyObject.toString());
                if (addTypeInfo) {
                    element.declareNamespace(xsNamespace);
                    element.addAttribute("type", "xs:float", xsiNamespace);
                }
            } else if (RubyObject instanceof RubyInteger) {
                element.setText(RubyObject.toString());
                if (addTypeInfo) {
                    element.declareNamespace(xsNamespace);
                    element.addAttribute("type", "xs:long", xsiNamespace);
                }
            } else if (RubyObject instanceof RubyArray) {
                String strXml = "<List>";
                String st[];
                st = RubyObject.toString().substring(1, RubyObject.toString().length() - 1).split(",");
                for (int i = 0; i < st.length; i++) {
                    strXml = strXml + "<" + i + ">" + st[i] + "</" + i + ">";
                }
                strXml = strXml + "</List>";
                strXml = strXml.replaceAll("'", "");
                strXml = strXml.replaceAll(" ", "");
                element.setText(strXml);
                if (addTypeInfo) {
                    element.declareNamespace(xsNamespace);
                    element.addAttribute("type", "xs:anyType", xsiNamespace);
                }
            }
            return element;
        }


}
