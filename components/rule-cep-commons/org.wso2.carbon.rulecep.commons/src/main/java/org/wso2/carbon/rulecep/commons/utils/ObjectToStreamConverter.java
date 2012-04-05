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
package org.wso2.carbon.rulecep.commons.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * Contains helper methods  used to create input/ output streams from given objects such  {@link URI},
 * {@link OMElement} {@link String}, {@link OMText}, etc
 */
public class ObjectToStreamConverter {

    private static Log log = LogFactory.getLog(ObjectToStreamConverter.class);

    /**
     * Helper method to get the stream from a given object
     *
     * @param value      the object which contains the source
     * @param properties controls the way getting the stream
     * @return the stream object of the given object value
     */
    public static InputStream toInputStream(Object value, Map<String, Object> properties) {

        if (value == null) {
            throw new LoggedRuntimeException("Cannot convert null object to a stream", log);
        }

        if (value instanceof InputStream) {
            return (InputStream) value;
        }

        if (log.isDebugEnabled()) {
            log.debug("Value to be converted to stream : " + value);
        }

        if (value instanceof OMElement) {
            if (properties != null) {
                String sourceFormat = (String) properties.get(CommonsConstants.SOURCE);
                if (!CommonsConstants.FORMAT_XML.equals(sourceFormat)) {
                    // if the rule format is native , it have to be wrapped by CDATA
                    String source = ((OMElement) (value)).getText();
                    if (source != null && !"".equals(source)) {
                        return new ByteArrayInputStream(source.trim().getBytes());
                    }
                }
            }
            OMElement omElement = (OMElement) value;
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            try {
                omElement.serialize(arrayOutputStream);
                return new ByteArrayInputStream(arrayOutputStream.toByteArray());
            } catch (XMLStreamException e) {
                throw new LoggedRuntimeException("Error converting to a Stream from OMElement : " +
                        value, e, log);
            }

        } else if (value instanceof OMText) {
            DataHandler dataHandler = (DataHandler) ((OMText) value).getDataHandler();
            if (dataHandler != null) {
                try {
                    return dataHandler.getInputStream();
                } catch (IOException e) {
                    throw new LoggedRuntimeException("Error in reading content as a stream " +
                            "from DataHandler", log);
                }
            }
        } else if (value instanceof URI) {
            try {
                return ((URI) (value)).toURL().openStream();
            } catch (IOException e) {
                throw new LoggedRuntimeException("Error opening stream form URI " + value, e, log);
            }
        } else if (value instanceof String) {
            return new ByteArrayInputStream(((String) value).trim().getBytes());
        } else {
            throw new LoggedRuntimeException("Cannot convert object to a Stream : " + value, log);
        }
        return null;
    }
}
