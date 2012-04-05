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
 *//**
 *
 */
package org.wso2.carbon.rulecep.commons.descriptions;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;

/**
 * Creates OMNamespaces based on various criteria
 */
public class OMNamespaceFactory {

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static OMNamespaceFactory ourInstance = new OMNamespaceFactory();

    public static OMNamespaceFactory getInstance() {
        return ourInstance;
    }

    private OMNamespaceFactory() {
    }

    /**
     * Creates an OMNamespace based on the given QName using it's namespace URI and prefix
     *
     * @param qName QName providing namespace URI and prefix
     * @return <code>OMNamespace</code> instance
     */
    public OMNamespace createOMNamespace(QName qName) {
        return OM_FACTORY.createOMNamespace(qName.getNamespaceURI(), qName.getPrefix());
    }

    /**
     * Creates an OMNamespace based on the given namespace URI and prefix
     *
     * @param uri    namespace URI
     * @param prefix prefix
     * @return <code>OMNamespace</code> instance
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return OM_FACTORY.createOMNamespace(uri, prefix);
    }
}
