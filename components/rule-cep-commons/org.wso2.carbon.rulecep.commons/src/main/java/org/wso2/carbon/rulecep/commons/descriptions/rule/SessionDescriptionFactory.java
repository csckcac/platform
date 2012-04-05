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
package org.wso2.carbon.rulecep.commons.descriptions.rule;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

import java.util.List;

/**
 * Factory to creates <code>SessionDescription</code> from a XML
 */
public class SessionDescriptionFactory {

    /**
     * Creates a <code>SessionDescription</code> from the given configuration XML
     *
     * @param sessionElement an XML representation of the <code>SessionDescription</code>
     * @param xPathFactory   to be used to create XPaths
     * @return Not null <code>SessionDescription</code> instance
     */
    public static SessionDescription create(OMElement sessionElement, XPathFactory xPathFactory) {

        SessionDescription sessionDescription = new SessionDescription();
        String sessionType = sessionElement.getAttributeValue(CommonsConstants.ATT_TYPE_Q);
        if (sessionType != null && !"".equals(sessionType)) {
            sessionDescription.setSessionType(sessionType.trim());
        }

        final List<PropertyDescription> list =
                PropertyDescriptionFactory.createPropertyDescriptionList(sessionElement,
                        xPathFactory, null);
        for (PropertyDescription description : list) {
            sessionDescription.addSessionPropertyDescription(description);
        }
        return sessionDescription;
    }
}
