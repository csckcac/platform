/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.header;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.OMElementUtils;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;

public class HeaderMediator extends AbstractMediator {

    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;
    private static final QName ATT_ACTION = new QName("action");

    /** The qName of the header @see HeaderType */
    private QName qName = null;
    /** The literal value to be set as the header (if one was specified) */
    private String value = null;
    /** Set the header (ACTION_SET) or remove it (ACTION_REMOVE). Defaults to ACTION_SET */
    private int action = ACTION_SET;
    /** An expression which should be evaluated, and the result set as the header value */
    private SynapseXPath expression = null;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public QName getQName() {
        return qName;
    }

    public void setQName(QName qName) {
        this.qName = qName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public String getTagLocalName() {
        return "header";
    }

    public OMElement serialize(OMElement parent) {
        OMElement header = fac.createOMElement("header", synNS);
        saveTracingState(header, this);

        QName qName = getQName();
        if (qName != null) {
            if (qName.getNamespaceURI() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS,
                    (qName.getPrefix() != null && !"".equals(qName.getPrefix())
                        ? qName.getPrefix() + ":" : "") +
                    qName.getLocalPart()));
                header.declareNamespace(qName.getNamespaceURI(), qName.getPrefix());
            } else {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS, qName.getLocalPart()));
            }
        }

        if (getAction() == org.apache.synapse.mediators.transform.HeaderMediator.ACTION_REMOVE) {
            header.addAttribute(fac.createOMAttribute(
                "action", nullNS, "remove"));
        } else {
            if (getValue() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "value", nullNS, getValue()));

            } else if (getExpression() != null) {

                SynapseXPathSerializer.serializeXPath(
                    getExpression(), header, "expression");

            } else {
                //TODO error
            }
        }

        if (parent != null) {
            parent.addChild(header);
        }
        return header;
    }

    public void build(OMElement elem) {
        // reset
        qName = null;
        value = null;
        expression = null;
        action = ACTION_SET;

        OMAttribute name   = elem.getAttribute(ATT_NAME);
        OMAttribute value  = elem.getAttribute(ATT_VALUE);
        OMAttribute exprn  = elem.getAttribute(ATT_EXPRN);
        OMAttribute action = elem.getAttribute(ATT_ACTION);

        if (name == null || name.getAttributeValue() == null) {
            //String msg = "A valid name attribute is required for the header mediator";
            // TODO error
        } else {
            String nameAtt = name.getAttributeValue();
            int colonPos = nameAtt.indexOf(":");
            if (colonPos != -1) {
                // has a NS prefix.. find it and the NS it maps into
                String prefix = nameAtt.substring(0, colonPos);
                String namespaceURI = OMElementUtils.getNameSpaceWithPrefix(prefix, elem);
                if (namespaceURI == null) {
                    // String msg = "Invalid namespace prefix '" + prefix + "' in name attribute";
                    // TODO error
                } else {
                	setQName(new QName(namespaceURI, nameAtt.substring(colonPos+1), prefix));
                }
            } else {
                // no prefix
                setQName(new QName(nameAtt));
            }
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        // The action attribute is optional, if provided and equals to 'remove' the
        // header mediator will act as a header remove mediator
        if (action != null && "remove".equals(action.getAttributeValue())) {
            setAction(ACTION_REMOVE);
        }

        if (getAction() == org.apache.synapse.mediators.transform.HeaderMediator.ACTION_SET &&
            value == null && exprn == null) {
            // String msg = "A 'value' or 'expression' attribute is required for a [set] header mediator";
            // TODO error
        }

        if (value != null && value.getAttributeValue() != null) {
            setValue(value.getAttributeValue());

        } else if (exprn != null && exprn.getAttributeValue() != null) {
            try {
                setExpression(SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN));
            } catch (JaxenException je) {
                // TODO error
            }
        }            
    }
}
