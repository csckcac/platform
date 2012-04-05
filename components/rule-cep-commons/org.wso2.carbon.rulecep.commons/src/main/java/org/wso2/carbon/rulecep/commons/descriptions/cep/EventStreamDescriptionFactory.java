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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

/**
 * Factory for creating <code>EventStreamDescription</code> instances
 */
public class EventStreamDescriptionFactory {

    private static final Log log = LogFactory.getLog(EventStreamDescriptionFactory.class);

    /**
     * Creates a <code>EventStreamDescription</code>  instance from the given XML
     *
     * @param ruleSet      an XML representation of the <code>EventStreamDescription</code>
     * @param xPathFactory to be used to create XPaths
     * @return Not null <code>RuleSetDescription</code> instance
     */
    public static EventStreamDescription create(OMElement ruleSet,
                                                XPathFactory xPathFactory) {

        EventStreamDescription description = new EventStreamDescription();
        String uri = ruleSet.getAttributeValue(CommonsConstants.ATT_URI_Q);
        if (uri != null && !"".equals(uri)) {
            description.setURI(uri.trim());
        }
        description.setTopic(ruleSet.getAttributeValue(CommonsConstants.ATT_TOPIC_Q));
        description.setUsername(ruleSet.getAttributeValue(CommonsConstants.ATT_USER_NAME_Q));
        description.setPassword(ruleSet.getAttributeValue(CommonsConstants.ATT_PASSWORD_Q));
        return description;
    }
}
