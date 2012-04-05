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
package org.wso2.carbon.rulecep.commons.descriptions.cep;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;

/**
 * Serialize <code>EventStreamDescription</code> into an XML
 */
public class EventStreamDescriptionSerializer {

    private static Log log = LogFactory.getLog(EventStreamDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Creates an XML for the given <code>EventStreamDescription</code>  instance
     * and adds to the parent OMElement as a child
     *
     * @param description     <code>EventStreamDescription</code>  to be mapped into XML
     * @param xPathSerializer to be used to serialize XPaths
     * @param parent          the parent tag
     * @return parent OMElement with rule set as a child
     */
    public static OMElement serialize(EventStreamDescription description,
                                      XPathSerializer xPathSerializer,
                                      OMElement parent) {

        OMNamespace omNamespace = parent.getNamespace();
        String topic = description.getTopic();
        String uri = description.getURI();

        if (topic != null && !"".equals(topic)) {
            parent.addAttribute(OM_FACTORY.createOMAttribute(
                    "topic", NULL_NS, topic));
        }
        if (uri != null && !"".equals(uri)) {
            parent.addAttribute(OM_FACTORY.createOMAttribute(
                    "uri", NULL_NS, uri));
        }
        return parent;
    }
}
