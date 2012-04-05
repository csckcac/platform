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

import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Caching XPaths instances
 */
public class XPathCache {

    private static Log log = LogFactory.getLog(XPathCache.class);

    private final Map<String, BaseXPath> xPathMap = new HashMap<String, BaseXPath>();
    /**
     * Used to create an XPath
     */
    private XPathFactory xPathFactory;

    private Collection<OMNamespace> nameSpaces;

    public XPathCache(XPathFactory xPathFactory, Collection<OMNamespace> nameSpaces) {
        this.xPathFactory = xPathFactory;
        this.nameSpaces = nameSpaces;
    }

    /**
     * Find an XPath in the cache for a given expression. if there is no Xpath for the given
     * expression, then a new XPath is created for the given expression
     *
     * @param expression the expression to be used to find an XPath from the cache
     * @return <code>BaseXPath</code>
     */
    public BaseXPath getXPath(String expression) {

        if (expression == null || "".equals(expression)) {
            throw new LoggedRuntimeException("Given expression is empty or null.", log);
        }

        BaseXPath baseXPath = xPathMap.get(expression);
        if (baseXPath != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found an XPath from the cache for the expression :" + expression);
            }
            return baseXPath;
        }

        if (log.isDebugEnabled()) {
            log.debug("There is no XPath in the cache for the given expression : " +
                    expression + ". Creating a new one");
        }

        BaseXPath newXPath = xPathFactory.createXPath(expression, nameSpaces);
        if (newXPath == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot create a XPath for the expression : " + expression);
            }
            return null;
        }

        xPathMap.put(expression, newXPath);
        return newXPath;
    }

}
