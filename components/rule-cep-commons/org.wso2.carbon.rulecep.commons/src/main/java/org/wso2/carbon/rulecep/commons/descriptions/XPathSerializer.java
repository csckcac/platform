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

import org.apache.axiom.om.OMElement;
import org.jaxen.BaseXPath;

/**
 * Serialize XPath into an OMElement. XPath is the type of <code>BaseXPath</code>
 */
public interface XPathSerializer {

    /**
     * @param xpath         XPath to be serialized
     * @param element       Target <code>OMElement</code>
     * @param attributeName Target attribute name
     */
    void serializeXPath(BaseXPath xpath, OMElement element, String attributeName);
}
