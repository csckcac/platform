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
package org.wso2.carbon.mediator.entitlement;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;

public class EntitlementMediator extends AbstractMediator {
    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private static final QName PROP_NAME_SERVICE_EPR = new QName("remoteServiceUrl");
    private static final QName PROP_NAME_USER = new QName("remoteServiceUserName");
    private static final QName PROP_NAME_PASSWORD = new QName("remoteServicePassword");

    public String getRemoteServiceUserName() {
        return remoteServiceUserName;
    }

    public void setRemoteServiceUserName(String remoteServiceUserName) {
        this.remoteServiceUserName = remoteServiceUserName;
    }

    public String getRemoteServicePassword() {
        return remoteServicePassword;
    }

    public void setRemoteServicePassword(String remoteServicePassword) {
        this.remoteServicePassword = remoteServicePassword;
    }

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serialize(OMElement parent) {
        OMElement entitlementService = fac.createOMElement("entitlementService", synNS);

        if (remoteServiceUrl != null) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS,
                    remoteServiceUrl));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator.Entitlement service epr required");
        }

        if (remoteServiceUserName != null) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUserName", nullNS,
                    remoteServiceUserName));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator. Remote service user name required");
        }

        if (remoteServicePassword != null) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServicePassword", nullNS,
                    remoteServicePassword));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator. Remote service password required");
        }

        saveTracingState(entitlementService, this);

        if (parent != null) {
            parent.addChild(entitlementService);
        }
        return entitlementService;
    }

    /**
     * {@inheritDoc}
     */
    public void build(OMElement elem) {
        OMAttribute attRemoteServiceUri = elem.getAttribute(PROP_NAME_SERVICE_EPR);
        OMAttribute attRemoteServiceUserName = elem.getAttribute(PROP_NAME_USER);
        OMAttribute attRemoteServicePassword = elem.getAttribute(PROP_NAME_PASSWORD);

        if (attRemoteServiceUri != null) {
            remoteServiceUrl = attRemoteServiceUri.getAttributeValue();
        } else {
            throw new MediatorException(
                    "The 'remoteServiceUrl' attribute is required for the Entitlement mediator");
        }

        if (attRemoteServiceUserName != null) {
            remoteServiceUserName = attRemoteServiceUserName.getAttributeValue();
        } else {
            throw new MediatorException(
                    "The 'remoteServiceUserName' attribute is required for the Entitlement mediator");
        }

        if (attRemoteServicePassword != null) {
            remoteServicePassword = attRemoteServicePassword.getAttributeValue();
        } else {
            throw new MediatorException(
                    "The 'remoteServicePassword' attribute is required for the Entitlement mediator");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getTagLocalName() {
        return "entitlementService";
    }
}
