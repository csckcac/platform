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
package org.wso2.carbon.rulecep.commons.descriptions.rule.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;

import javax.xml.stream.XMLStreamConstants;

/**
 * ConfigurationExtensionSerializer for serializing extensions in the rule set configuration used in the rule service
 */
public class RuleServiceRuleSetExtensionSerializer implements ExtensionSerializer {

    private Log log = LogFactory.getLog(RuleServiceRuleSetExtensionSerializer.class);
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS = OM_FACTORY.createOMNamespace("", "");

    public OMElement serialize(ExtensibleConfiguration configuration,
                               XPathSerializer xPathSerializer,
                               OMElement parent) {

        if (!(configuration instanceof RuleSetDescription)) {
            throw new LoggedRuntimeException("Invalid rule configuration," +
                    "expect RuleSetDescription.", log);
        }

        RuleSetDescription description = (RuleSetDescription) configuration;
        String key = description.getKey();
        String path = description.getPath();
        Object inLinedScript = description.getRuleSource();

        if (key == null && inLinedScript == null && path == null) {
            throw new LoggedRuntimeException("Invalid Configuration !!- Either script in-lined " +
                    "value or key or path should be presented", log);
        }

        if (key != null && !"".equals(key)) {
            parent.addAttribute(OM_FACTORY.createOMAttribute(
                    "key", NULL_NS, key));
        } else {
            if (path != null && !"".equals(path)) {
                parent.addAttribute(OM_FACTORY.createOMAttribute(
                        "path", NULL_NS, path));
            } else {
                if (inLinedScript instanceof OMElement) {
                    parent.addChild((OMElement) inLinedScript);
                } else {
                    if (inLinedScript instanceof String) {
                        OMTextImpl textData = (OMTextImpl) OM_FACTORY.createOMText(
                                ((String) inLinedScript).trim());
                        textData.setType(XMLStreamConstants.CDATA);
                        parent.addChild(textData);
                    }
                }
            }
        }
        return parent;
    }
}
