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
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import javax.activation.DataHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

/**
 * Contains helper methods used to convert given objects such  {@link URI},
 * {@link OMElement}, {@link InputStream},@link OutputStream}, {@link OMText}, etc into <code>String</code>
 */
public class ObjectToStringConverter {

    private static Log log = LogFactory.getLog(ObjectToStringConverter.class);

    /**
     * Converts the given object into a string
     *
     * @param value      object to be converted into a <code>String</code>
     * @param properties properties to be used during the conversion
     * @return <code>String</code> representation of the given object
     */
    public static String toString(Object value, Map<String, Object> properties) {

        if (value == null) {
            throw new LoggedRuntimeException("Cannot convert null object to a string", log);
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof OMElement) {
            return ((OMElement) (value)).getText();
        } else if (value instanceof OMText) {
            DataHandler dataHandler = (DataHandler) ((OMText) value).getDataHandler();
            if (dataHandler != null) {
                try {
                    return toString(dataHandler.getInputStream());
                } catch (IOException e) {
                    throw new LoggedRuntimeException("Error in reading content as a string ", log);
                }
            } else {
                return ((OMText) value).getText();
            }
        } else if (value instanceof URI) {
            try {
                return toString(((URI) (value)).toURL().openStream());
            } catch (IOException e) {
                throw new LoggedRuntimeException("Error opening stream form URI", e, log);
            }
        } else {
            throw new LoggedRuntimeException("Cannot convert object to a String", log);
        }

    }

    /**
     * Converts the given input stream into a String
     *
     * @param inputStream input stream to be converted into a string
     * @return String representation of the given inputStream
     */
    public static String toString(InputStream inputStream) {

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String str;
        try {
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException e) {
            throw new LoggedRuntimeException("Error converting stream to String ", e, log);
        } finally {
            try {
                br.close();
            } catch (IOException ignored) {
            }
        }
        return sb.toString();
    }
}
