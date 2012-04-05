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
package org.wso2.mashup.deployer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.constants.Constants;


import org.jruby.*;
import org.jruby.javasupport.JavaArray;
import org.jruby.ast.Node;


import org.jruby.runtime.builtin.IRubyObject;
import org.wso2.mashup.deployer.util.RubyScriptReader;
import org.wso2.mashup.deployer.util.ParameterParser;
import org.wso2.mashup.deployer.util.ResponseBuilder;
import org.wso2.mashup.deployer.util.ScriptConfigurator;


import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Array;


public class RubyMessageReceiver extends AbstractInOutSyncMessageReceiver implements
        MessageReceiver {

    /**
         * Invokes the Javascript service with the parameters from the inMessage
         * and sets the outMessage with the response from the service.
         *
         * @param inMessage MessageContext object with information about the incoming message
         * @param outMessage MessageContext object with information about the outgoing message
         * @throws org.apache.axis2.AxisFault
         */
        public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
                throws AxisFault {
            try {
               
               SOAPEnvelope soapEnvelope = inMessage.getEnvelope();



               // BufferedReader reader = readRubyScript(inMessage);
                String script =  getRubyScript(inMessage.getAxisService());
                String rubyFunctionName = inferRubyFunctionName(inMessage);
                OMElement payload = getArgs(inMessage);

                AxisMessage inAxisMessage = inMessage.getAxisOperation().getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                AxisMessage outAxisMessage = inMessage.getAxisOperation().getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);



            Object args = payload;
            if (payload != null) {
                // We neet to get the Axis Message from the incomming message so that we can get its schema.
                // We need the schema in order to unwrap the parameters.

                AxisMessage axisMessage = inMessage.getAxisOperation().getMessage(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                XmlSchemaElement xmlSchemaElement = axisMessage.getSchemaElement();
                if (xmlSchemaElement != null) {
                    // Once the schema is obtained we iterate through the schema looking for the elemants in the payload.
                    // for Each element we extract its value and create a parameter which can be passed into the
                    // pythonscript function.
                    XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                    if (schemaType instanceof XmlSchemaComplexType) {
                        XmlSchemaComplexType complexType = ((XmlSchemaComplexType) schemaType);
                        args = ParameterParser.getParameters(complexType, payload, new ArrayList<String>());

                    } else if (xmlSchemaElement.getSchemaTypeName() == Constants.XSD_ANYTYPE) {
                        args = payload;
                    }
                }
            } else {
                // This validates whether the user has sent a bad SOAP message
                // with a non-XML payload.
                if (soapEnvelope.getBody().getFirstOMChild() != null) {
                    OMText textPayLoad = (OMText) soapEnvelope.getBody().getFirstOMChild();
                    //we allow only a sequence of spaces
                    if (textPayLoad.getText().trim().length() > 0) {
                        throw new AxisFault(
                                "Non-XML payload is not allowed. PayLoad inside the SOAP body needs to be an XML element.");
                    }
                }
            }

                boolean annotated=true;
                // Get the result by executing the javascript file

                /*****OMNode result = runtime.call(jsFunctionName, reader, payload, scripts);******/

                String scriptWithHeaders = ScriptConfigurator.appendRequiredHeaders(script);
                IRubyObject response = RubyScriptEngine.invokeMethod(scriptWithHeaders,rubyFunctionName,args);



                SOAPFactory fac;
            if (inMessage.isSOAP11()) {
                fac = OMAbstractFactory.getSOAP11Factory();
            } else {
                fac = OMAbstractFactory.getSOAP12Factory();
            }
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            SOAPBody body = envelope.getBody();
            XmlSchemaElement xmlSchemaElement = outAxisMessage.getSchemaElement();
            OMElement outElement;
            String prefix = "ws";
            if (xmlSchemaElement != null) {
                QName elementQName = xmlSchemaElement.getSchemaTypeName();
                OMNamespace namespace = fac.createOMNamespace(elementQName.getNamespaceURI(),
                        prefix);
                outElement = fac.createOMElement(xmlSchemaElement.getName(), namespace);
                XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                if (schemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = ((XmlSchemaComplexType) schemaType);
                    ResponseBuilder.handleComplexTypeInResponse(complexType, outElement, response, fac,
                            false);
                    body.addChild(outElement);
                } else if (xmlSchemaElement.getSchemaTypeName() == Constants.XSD_ANYTYPE) {
                    if (response !=null) {
                        // If its anyType that means we have to add xsi:type
                        OMElement element = ResponseBuilder.buildResponse(false, response,
                                xmlSchemaElement);
                        if (element != null) {
                            body.addChild(element);
                        }
                    }
                }
            } else if (response !=null) {
                OMElement element =
                        ResponseBuilder.buildResponse(annotated, response, xmlSchemaElement);
                if (log.isDebugEnabled()) {
                    log.debug("The built element is " + element.toString());
                }
                if (element != null) {
                    body.addChild(element);
                }
            }

             outMessage.setEnvelope(envelope);
                
            } catch (Throwable throwable) {
                throw AxisFault.makeFault(throwable);
            }


    }

        
    public OMElement getArgs(MessageContext inMessage) throws XMLStreamException {

           OMElement args = inMessage.getEnvelope().getBody().getFirstElement();
           //String value = firstChild.toStringWithConsume();
           if (args != null) {
               return args;
           }
           return null;

       }
    



        /*
         * Extracts and returns the name of the JS function to be invoked from the
         * inMessage
         *
         * @param inMessage
         *            MessageContext object with information about the incoming
         *            message
         * @return the name of the requested JS function
         * @throws AxisFault
         *             if the function name cannot be inferred.
         */
        private String inferRubyFunctionName(MessageContext inMessage) throws AxisFault {
            //Look at the method name. if available this should be a javascript method
            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault("Operation notFound");
            }

            String rubyFunctionName = op.getName().getLocalPart();
            if (rubyFunctionName == null)
                throw new AxisFault(
                        "Unable to infer the ruby function  corresponding to this message.");
            return rubyFunctionName;
        }



        private String getRubyScript(AxisService service) throws AxisFault {
            Parameter scrParam = service.getParameter(RubyConstants.SERVICE_RUBY_SCRIPT);
            if(scrParam!=null){
                String s = (String) scrParam.getValue();
                return s;
            }
            Parameter locParam = service.getParameter(RubyConstants.SERVICE_RUBY);

            if(locParam!=null){
                String location = (String)service.getParameter(RubyConstants.SERVICE_RUBY).getValue() ;
                File f = new File(location);
                RubyScriptReader reader = new RubyScriptReader();
                return reader.readScript(f);
            }
            else{
                throw new AxisFault("Can't retrieve ruby Script. Parameter missing..");
            }
        }

        /*
         * Locates the service Javascript associated with ServiceJS parameter and returns
         * an input stream to it.
         *
         * @param inMessage MessageContext object with information about the incoming message
         * @return an input stream to the javascript source file
         * @throws AxisFault if the parameter ServiceJS is not specified or if the service
         * implementation is not available
         */

    

    }
