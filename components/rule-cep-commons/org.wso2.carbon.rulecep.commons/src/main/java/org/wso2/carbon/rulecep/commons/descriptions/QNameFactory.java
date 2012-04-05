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
package org.wso2.carbon.rulecep.commons.descriptions;

import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;

/**
 * Creates QName based on various criteria
 */
public class QNameFactory {

    private static QNameFactory ourInstance = new QNameFactory();

    public static QNameFactory getInstance() {
        return ourInstance;
    }

    private QNameFactory() {
    }

    /**
     * Creates a QName from given local name and the <code>OMNamespace</code>
     *
     * @param localName   the local name for the QName
     * @param omNamespace <code>OMNamespace</code> to be used to extractPayload name spaces
     *                    and prefixes
     * @return <code>QName</code> instance
     */
    public QName createQName(String localName, OMNamespace omNamespace) {
        return new QName(omNamespace.getNamespaceURI(), localName, omNamespace.getPrefix());
    }

    /**
     * Creates a QName from given local name and the <code>QName</code>
     *
     * @param localName he local name for the QName
     * @param qName     an <code>QName</code> instance  to be used to extractPayload name
     *                  spaces and prefixes
     * @return <code>QName</code> instance
     */
    public QName createQName(String localName, QName qName) {
        return new QName(qName.getNamespaceURI(), localName, qName.getPrefix());
    }
}
