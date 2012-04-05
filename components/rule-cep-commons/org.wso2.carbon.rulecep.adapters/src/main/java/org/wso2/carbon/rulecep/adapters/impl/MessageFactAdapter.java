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
package org.wso2.carbon.rulecep.adapters.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.Message;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

/**
 * Adapts input as Message
 */
public class MessageFactAdapter extends ResourceAdapter implements InputAdaptable {

    private static Log log = LogFactory.getLog(MessageFactAdapter.class);

    public static final String TYPE = "message";

    public String getType() {
        return TYPE;
    }

    /**
     * Creates a Message. if the input is a boolean and value if false  , the fact is null
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert that into correct type
     * @return <code>Message</code> if the input is boolean true or OMElement
     */
    public Object adaptInput(ResourceDescription resourceDescription,
                             Object tobeAdapted) {
        String name = resourceDescription.getName();
        if (name == null) {
            BaseXPath baseXPath = resourceDescription.getExpression();
            if (baseXPath != null) {
                name = baseXPath.toString();
            }
        }
        Message fact = null;
        if (tobeAdapted instanceof Boolean) {
            if (((Boolean) tobeAdapted).booleanValue()) {
                fact = new Message();
            }
        } else if (tobeAdapted instanceof String) {
            if (Boolean.parseBoolean((String) tobeAdapted)) {
                fact = new Message();
            }
        } else if (tobeAdapted instanceof OMText) {
            OMText omText = (OMText) tobeAdapted;
            if (Boolean.parseBoolean(omText.getText())) {
                fact = new Message();
            }
        } else if (tobeAdapted instanceof OMElement) {
            fact = new Message((OMElement) tobeAdapted, resourceDescription.getXPathCache());
        }
        if (fact != null && name != null) {
            fact.setName(name);
        }
        return fact;
    }
}
