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
package org.wso2.carbon.mediator.callout;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;


public class CalloutMediator extends AbstractMediator {
    private static final QName ATT_URL = new QName("serviceURL");
    private static final QName ATT_ACTION = new QName("action");
    private static final QName ATT_AXIS2XML = new QName("axis2xml");
    private static final QName ATT_REPOSITORY = new QName("repository");
    private static final QName Q_CONFIG
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "configuration");
    private static final QName Q_SOURCE
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "source");
    private static final QName Q_TARGET
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    private String serviceURL = null;
    private String action = null;
    private String requestKey = null;
    private SynapseXPath requestXPath = null;
    private SynapseXPath targetXPath = null;
    private String targetKey = null;
    private String clientRepository = null;
    private String axis2xml = null;
    public final static String DEFAULT_CLIENT_REPO = "./samples/axis2Client/client_repo";
    public final static String DEFAULT_AXIS2_XML = "./samples/axis2Client/client_repo/conf/axis2.xml";

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public void setRequestXPath(SynapseXPath requestXPath) throws JaxenException {
        this.requestXPath = requestXPath;
    }

    public void setTargetXPath(SynapseXPath targetXPath) throws JaxenException {
        this.targetXPath = targetXPath;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public SynapseXPath getRequestXPath() {
        return requestXPath;
    }

    public SynapseXPath getTargetXPath() {
        return targetXPath;
    }

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }
   
    public String getTagLocalName() {
        return "callout";
    }

    public OMElement serialize(OMElement parent) {
        OMElement callout = fac.createOMElement("callout", synNS);
        saveTracingState(callout, this);

        callout.addAttribute(fac.createOMAttribute("serviceURL", nullNS, serviceURL));
        if (action != null) {
            callout.addAttribute(fac.createOMAttribute("action", nullNS, action));
        }

        if (clientRepository != null || axis2xml != null) {
            OMElement config = fac.createOMElement("configuration", synNS);
            if (clientRepository != null) {
                config.addAttribute(fac.createOMAttribute(
                        "repository", nullNS, clientRepository));
            }
            if (axis2xml != null) {
                config.addAttribute(fac.createOMAttribute(
                        "axis2xml", nullNS, axis2xml));
            }
            callout.addChild(config);
        }

        OMElement source = fac.createOMElement("source", synNS, callout);
        if (requestXPath != null) {
            SynapseXPathSerializer.serializeXPath(requestXPath, source, "xpath");
        } else if (requestKey != null) {
            source.addAttribute(fac.createOMAttribute(
                "key", nullNS, requestKey));
        }

        OMElement target = fac.createOMElement("target", synNS, callout);
        if (targetXPath != null) {
            SynapseXPathSerializer.serializeXPath(targetXPath, target, "xpath");
        } else if (targetKey != null) {
            target.addAttribute(fac.createOMAttribute(
                "key", nullNS, targetKey));
        }

        if (parent != null) {
            parent.addChild(callout);
        }
        return callout;
    }

    public void build(OMElement elem) {
        OMAttribute attServiceURL = elem.getAttribute(ATT_URL);
        OMAttribute attAction     = elem.getAttribute(ATT_ACTION);
        OMElement   configElt     = elem.getFirstChildWithName(Q_CONFIG);
        OMElement   sourceElt     = elem.getFirstChildWithName(Q_SOURCE);
        OMElement   targetElt     = elem.getFirstChildWithName(Q_TARGET);

        if (attServiceURL != null) {
            serviceURL = attServiceURL.getAttributeValue();
        } else {
            throw new MediatorException("The 'serviceURL' attribute is required for the Callout mediator");
        }

        if (attAction != null) {
            action = attAction.getAttributeValue();
        }

        if (configElt != null) {

            OMAttribute axis2xmlAttr = configElt.getAttribute(ATT_AXIS2XML);
            OMAttribute repoAttr = configElt.getAttribute(ATT_REPOSITORY);

            if (axis2xmlAttr != null && axis2xmlAttr.getAttributeValue() != null) {
                axis2xml = axis2xmlAttr.getAttributeValue();
            }

            if (repoAttr != null && repoAttr.getAttributeValue() != null) {
                clientRepository = repoAttr.getAttributeValue();
            }
        }

        if (sourceElt != null) {
            if (sourceElt.getAttribute(ATT_XPATH) != null) {
                try {
                    requestXPath = SynapseXPathFactory.getSynapseXPath(sourceElt, ATT_XPATH);
                } catch (JaxenException e) {
                    throw new MediatorException("Invalid source XPath : "
                        + sourceElt.getAttributeValue(ATT_XPATH));
                }
            } else if (sourceElt.getAttribute(ATT_KEY) != null) {
                requestKey = sourceElt.getAttributeValue(ATT_KEY);
            } else {
                throw new MediatorException("A 'xpath' or 'key' attribute " +
                    "is required for the Callout 'source'");
            }
        } else {
            throw new MediatorException("The message 'source' must be specified for a Callout mediator");
        }

        if (targetElt != null) {
            if (targetElt.getAttribute(ATT_XPATH) != null) {
                try {
                    targetXPath = SynapseXPathFactory.getSynapseXPath(targetElt, ATT_XPATH);
                } catch (JaxenException e) {
                    throw new MediatorException("Invalid target XPath : "
                        + targetElt.getAttributeValue(ATT_XPATH));
                }
            } else if (targetElt.getAttribute(ATT_KEY) != null) {
                targetKey = targetElt.getAttributeValue(ATT_KEY);
            } else {
                throw new MediatorException("A 'xpath' or 'key' attribute " +
                    "is required for the Callout 'target'");
            }
        } else {
            throw new MediatorException("The message 'target' must be specified for a Callout mediator");
        }        
    }
}
