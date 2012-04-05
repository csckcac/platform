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
package org.wso2.carbon.identity.entitlement.mediator.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementMediator;

/**
 *  
 * <entitlementService remoteServiceUrl = "https://identityserever/services/EntitlementService"
 * remoteServiceUserName="administrator" remoteServicePassword="administrator"
 * callbackClass="org.wso2.carbon.identity.samples.entitlement.callback.EntitlementCallBack"/>
 */
public class EntitlementMediatorSerializer extends AbstractMediatorSerializer {

    /**
     * {@inheritDoc}
     */
    public String getMediatorClassName() {
        return EntitlementMediator.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serializeSpecificMediator(Mediator mediator) {
        if (!(mediator instanceof EntitlementMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                    + mediator.getType());
        }

        EntitlementMediator entitlement = null;
        OMElement entitlementElem = null;

        entitlement = (EntitlementMediator) mediator;
        entitlementElem = fac.createOMElement("entitlementService", synNS);
        saveTracingState(entitlementElem, entitlement);
        entitlementElem.addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS, entitlement
                .getRemoteServiceUrl()));
        entitlementElem.addAttribute(fac.createOMAttribute("remoteServiceUserName", nullNS,
                entitlement.getRemoteServiceUserName()));
        entitlementElem.addAttribute(fac.createOMAttribute("remoteServicePassword", nullNS,
                entitlement.getRemoteServicePassword()));

        if (entitlement.getCallbackClass() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute("callbackClass", nullNS,
                    entitlement.getCallbackClass()));
        }

        if (entitlement.getDecisionCaching() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute("decisionCaching", nullNS,
                    entitlement.getDecisionCaching()));
        }

        if (entitlement.getDecisionCachingInterval() != 0) {
            entitlementElem.addAttribute(fac.createOMAttribute("decisionCachingInterval", nullNS,
                    Integer.toString(entitlement.getDecisionCachingInterval())));
        }

        if (entitlement.getMaxCacheEntries() != 0) {
            entitlementElem.addAttribute(fac.createOMAttribute("maxCacheEntries", nullNS,
                    Integer.toString(entitlement.getMaxCacheEntries())));
        }

        if (entitlement.getBasicAuth() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute("basicAuth", nullNS,
                    entitlement.getBasicAuth()));
        }        

        if (entitlement.getThriftHost() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(EntitlementConstants.THRIFT_HOST,
                    nullNS, entitlement.getThriftHost()));
        }

        if (entitlement.getThriftPort() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(EntitlementConstants.THRIFT_PORT,
                    nullNS, entitlement.getThriftPort()));
        }

        if (entitlement.getReuseSession() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(EntitlementConstants.REUSE_SESSION,
                    nullNS, entitlement.getReuseSession()));
        }

        if (entitlement.getClientClass() != null) {
            entitlementElem.addAttribute(fac.createOMAttribute(EntitlementConstants.CLIENT_CLASS,
                    nullNS, entitlement.getClientClass()));
        }

        return entitlementElem;
    }

}
