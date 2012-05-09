/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.transform;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class Output {
    public static final String ACTION_REPLACE = "replace";
    public static final String ACTION_ADD_CHILD = "child";
    public static final String ACTION_ADD_SIBLING = "sibling";

    public final static String AXIOMPAYLOADNS = "http://ws.apache.org/commons/ns/payload";

    public final static QName TEXTELT = new QName(AXIOMPAYLOADNS, "text", "ax");

    private BamMediator.TYPES type = BamMediator.TYPES.XML;

    private SynapseXPath expression = null;

    private String property = null;

    private String action = null;

    public void process(ByteArrayOutputStream outputStream,
                        MessageContext synCtx, SynapseLog synLog) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        if (type == BamMediator.TYPES.XML) {
            try {
                StAXOMBuilder builder = new StAXOMBuilder(inputStream);

                // create the transformed element
                OMElement transformedElement = builder.getDocumentElement();
                if (property != null) {
                    synCtx.setProperty(property, transformedElement);
                } else if (expression != null) {
                    Object targetObj = expression.selectSingleNode(synCtx);

                    if (targetObj instanceof OMElement) {
                        OMElement targetElem = (OMElement) targetObj;
                        insertElement(transformedElement, targetElem, synLog);
                    } else if (targetObj instanceof OMText) {
                        OMText targetText = (OMText) targetObj;
                        Object targetParent = targetText.getParent();
                        if (targetParent instanceof OMElement) {
                            targetText.detach();
                            ((OMElement) targetParent).addChild(transformedElement);
                        }
                    } else {
                        handleException("Invalid Target object to be enrich.", synLog);
                    }
                } else {
                    setXMLPayload(synCtx.getEnvelope(), transformedElement);
                }
            } catch (XMLStreamException e) {
                handleException("Error creating the  parser form the result of bam", synLog, e);
            } catch (JaxenException e) {
                handleException("Error evaluating the Xpath expression: " + expression, synLog, e);
            }
        } else if (type == BamMediator.TYPES.TEXT) {
            if (expression != null) {
                Object targetObj = null;
                try {
                    targetObj = expression.selectSingleNode(synCtx);
                } catch (JaxenException e) {
                    handleException("Error evaluating the XPath: " + expression, synLog, e);
                }

                if (targetObj instanceof OMElement) {
                    ((OMElement) targetObj).setText(byteArrayIStoString(inputStream));
                } else if (targetObj instanceof OMText) {
                    OMText targetText = (OMText) targetObj;
                    if (targetText.getParent() != null) {
                        Object parent = targetText.getParent();
                        if (parent instanceof OMElement) {
                            ((OMElement) parent).setText(byteArrayIStoString(inputStream));
                        }
                    }
                } else {
                    handleException("Invalid Target object to be enrich.", synLog);
                }
            } else if (property != null) {
                synCtx.setProperty(property, byteArrayIStoString(inputStream));
            } else {
                setTextPayload(synCtx.getEnvelope(), byteArrayIStoString(inputStream));
            }
        }
    }

    public String byteArrayIStoString(ByteArrayInputStream is) {
        int size = is.available();
        char[] theChars = new char[size];
        byte[] bytes = new byte[size];

        int read = is.read(bytes, 0, size);

        for (int i = 0; i < size;) {
            theChars[i] = (char) (bytes[i++] & 0xff);
        }

        return new String(theChars);
    }

    private void insertElement(OMNode source, OMElement e, SynapseLog synLog) {
        if (action.equals(ACTION_REPLACE)) {
            boolean isInserted = false;

            if (source instanceof OMElement) {
                e.insertSiblingAfter(source);
                isInserted = true;
            } else if (source instanceof OMText) {
                e.setText(((OMText) source).getText());
            } else {
                handleException("Invalid Source object to be inserted.", synLog);
            }

            if (isInserted) {
                e.detach();
            }
        } else if (action.equals(ACTION_ADD_CHILD)) {
            if (source instanceof OMElement) {
                e.addChild(source);
            }
        } else if (action.equals(ACTION_ADD_SIBLING)) {
            if (source instanceof OMElement) {
                e.insertSiblingAfter(source);
            }
        }
    }

    public BamMediator.TYPES getType() {
        return type;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public String getProperty() {
        return property;
    }

    public String getAction() {
        return action;
    }

    public void setType(BamMediator.TYPES type) {
        this.type = type;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private void handleException(String message, SynapseLog log) {
        log.error(message);
        throw new SynapseException(message);
    }

    private void handleException(String message, SynapseLog log, Exception e) {
        log.error(message);
        throw new SynapseException(message, e);
    }

    public static void setXMLPayload(SOAPEnvelope envelope, OMElement element) {
        for (Iterator itr = envelope.getBody().getChildElements();
             itr.hasNext();) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }

        envelope.getBody().addChild(element);
    }

    public static void setTextPayload(SOAPEnvelope envelope, String text) {
		OMFactory fac = envelope.getOMFactory();
		OMElement textElt = envelope.getOMFactory().createOMElement(TEXTELT);
		OMText textNode = fac.createOMText(text);
		textElt.addChild(textNode);
		setXMLPayload(envelope, textElt);
	}
}
