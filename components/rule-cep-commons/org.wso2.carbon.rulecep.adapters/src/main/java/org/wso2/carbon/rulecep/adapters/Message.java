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
package org.wso2.carbon.rulecep.adapters;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.utils.XPathCache;
import org.wso2.carbon.rulecep.commons.utils.XPathHelper;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/**
 * Message as a Fact. Provides a way to insert message as a fact into the rule engine.
 * This is a type of <code>TransientObject</code>
 */
@TransientObject()
@SuppressWarnings("unused")
public class Message {

    private static Log log = LogFactory.getLog(Message.class);

    /**
     * if this is a named message fact
     */
    private String name;

    /**
     * Underlying message
     */
    private Object message;

    /**
     * Payload of the message
     */
    private OMElement payload;

    /**
     * To be used to intercept message object
     */
    private MessageInterceptor messageInterceptor;

    /**
     * Caches XPaths to improve performance
     */
    private XPathCache xPathCache;

    public Message(OMElement payload, XPathCache xPathCache) {
        this.payload = payload;
        this.xPathCache = xPathCache;
    }

    public Message() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OMElement getPayload() {
        return payload;
    }

    public void setPayload(OMElement payload) {
        this.payload = payload;
    }

    /**
     * Evaluates an XPath on the payload
     *
     * @param xpath xpath to be used to extract the data form the payload
     * @return the extracted value if there is one , otherwise null
     */
    public Object select(String xpath) {
        if (payload == null) {
            if (log.isDebugEnabled()) {
                log.debug("The payload is null. Returning null");
            }
            return null;
        }
        BaseXPath baseXPath;
        if (xPathCache != null) {
            baseXPath = xPathCache.getXPath(xpath);
        } else {
            baseXPath = createAxiomXPath(payload, xpath);
        }
        return XPathHelper.evaluate(payload, baseXPath);
    }

    /**
     * Helper method to create an AXIOMXPath
     *
     * @param source OMElement to be used to find NameSpaces
     * @param xpath  XPath expression string
     * @return an <code>AXIOMXPath </code> instance
     */
    private AXIOMXPath createAxiomXPath(OMElement source, String xpath) {
        try {

            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            axiomxPath.addNamespaces(source);
            return axiomxPath;
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error creating XPath " + xpath, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Calendar
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>Calendar</code> if it is possible to create a Calendar,
     *         otherwise throws an exception
     */
    public Calendar selectDataTime(String xpath) {
        Object o = select(xpath);
        if (o instanceof Calendar) {
            return (Calendar) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToDateTime((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + ". Can not create" +
                    " a datatime from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into an int
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>int</code> if it is possible to create a int,
     *         otherwise throws an exception
     */
    public int selectInt(String xpath) {
        Object o = select(xpath);
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToInt((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + ". Can not select" +
                    " a int from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a BigInteger
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>BigInteger</code> if it is possible to create a  big integer,
     *         otherwise throws an exception
     */
    public BigInteger selectBigInteger(String xpath) {
        Object o = select(xpath);
        if (o instanceof BigInteger) {
            return (BigInteger) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToInteger((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + ". Can not select" +
                    " a integer from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Double
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>double</code> if it is possible to create a double,
     *         otherwise throws an exception
     */
    public double selectDouble(String xpath) {
        Object o = select(xpath);
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToDouble((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + ". Can not select" +
                    " a double from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Float
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>float</code> if it is possible to create a float,
     *         otherwise throws an exception
     */
    public float selectFloat(String xpath) {
        Object o = select(xpath);
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToFloat((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + " Can not select" +
                    " a float from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Date
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>Date</code> if it is possible to create a date,
     *         otherwise throws an exception
     */
    public Date selectDate(String xpath) {
        Object o = select(xpath);
        if (o instanceof Date) {
            return (Date) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToDate((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + " Can not select" +
                    " a date from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Long
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>Long</code> if it is possible to create a long,
     *         otherwise throws an exception
     */
    public Long selectLong(String xpath) {
        Object o = select(xpath);
        if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToLong((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + " Can not select" +
                    " a long from the value : " + o, log);
        }
    }

    /**
     * Extracts data from the payload using the provided XPath and converts into a Boolean
     *
     * @param xpath xpath to be used to extract the data
     * @return <code>boolean</code> if it is possible to create a boolean,
     *         otherwise throws an exception
     */
    public boolean selectBoolean(String xpath) {
        Object o = select(xpath);
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return ConverterUtil.convertToBoolean((String) o);
        } else {
            throw new LoggedRuntimeException("Invalid XPath :  " + xpath + " Can not select" +
                    " a boolean from the value : " + o, log);
        }
    }

    /**
     * Evaluates the given xpath on the complete message envelope and returns the extracted value
     *
     * @param xpath xpath to be used to extract the value
     * @return the extracted value if there is one , otherwise null
     */
    public Object selectOnMessage(String xpath) {
        if (message == null) {
            if (log.isDebugEnabled()) {
                log.debug("The message is null. Returning null");
            }
            return null;
        }
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            OMElement xml = (OMElement) messageInterceptor.extractEnvelope(message).getValue();
            axiomxPath.addNamespaces(xml);
            return XPathHelper.evaluate(xml, axiomxPath);
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error creating XPath " + xpath, log);
        }
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public MessageInterceptor getMessageInterceptor() {
        return messageInterceptor;
    }

    public void setMessageInterceptor(MessageInterceptor messageInterceptor) {
        this.messageInterceptor = messageInterceptor;
    }
}
