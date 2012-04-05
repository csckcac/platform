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
package org.wso2.carbon.endpoint.ui.util;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.WSDLEndpointFactory;
import org.apache.synapse.endpoints.*;
import org.wso2.carbon.endpoint.common.to.AddressEndpointData;
import org.wso2.carbon.endpoint.common.to.DefaultEndpointData;
import org.wso2.carbon.endpoint.common.to.WSDLEndpointData;
import org.wso2.carbon.endpoint.ui.factory.TemplateDefinitionFactory;

import java.util.List;
import java.util.Properties;

public class TemplateConfigurationBuilder {

    public static final int ADDRESS_EP = 0;
    public static final int WSDL_EP = 1;
    public static final int FAILOVER_EP = 2;
    public static final int LOADBALANCE_EP = 3;
    public static final int DEFAULT_EP=4;
    public static final int TEMPLATE_EP=5;

    public static DefaultEndpointData getDefaultEndpointDetailsFrom(Template template, DefinitionFactory factory){
        OMElement endpointEl = template.getElement();
        if (endpointEl != null) {
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(endpointEl, factory, false, new Properties());
            if(endpoint!=null && endpoint instanceof DefaultEndpoint){
                DefaultEndpoint add = (DefaultEndpoint) endpoint;
                DefaultEndpointData data = new DefaultEndpointData();
                data.setEpName(add.getName());
                data.setEpType(DEFAULT_EP);
                //data.setFormat(add.getDefinition().getFormat());
                data.setSoap11(add.getDefinition().isForceSOAP11());
                data.setSoap12(add.getDefinition().isForceSOAP12());
                data.setRest(add.getDefinition().isForceGET());
                data.setPox(add.getDefinition().isForcePOX());
                data.setSwa(add.getDefinition().isUseSwa());
                data.setMtom(add.getDefinition().isUseMTOM());
                data.setSuspendDurationOnFailure(add.getDefinition().getInitialSuspendDuration());
                data.setTimeoutAct(add.getDefinition().getTimeoutAction());
                data.setTimeoutActionDur(add.getDefinition().getTimeoutDuration());
                data.setWsadd(add.getDefinition().isAddressingOn());
                data.setSepList(add.getDefinition().isUseSeparateListener());
                data.setWssec(add.getDefinition().isSecurityOn());
                data.setWsrm(add.getDefinition().isReliableMessagingOn());
                data.setRmPolKey(add.getDefinition().getWsRMPolicyKey());
                data.setSecPolKey(add.getDefinition().getWsSecPolicyKey());
                data.setMaxSusDuration(add.getDefinition().getSuspendMaximumDuration());
                data.setSusProgFactor(add.getDefinition().getSuspendProgressionFactor());
                data.setErrorCodes(errorCodeListBuilder(add.getDefinition().getSuspendErrorCodes()).trim());
                data.setRetryDisabledErrorCodes(errorCodeListBuilder(add.getDefinition().
                        getRetryDisabledErrorCodes()).trim());
                data.setTimdedOutErrorCodes(errorCodeListBuilder(add.getDefinition().getTimeoutErrorCodes()));
                data.setRetryTimeout(add.getDefinition().getRetryDurationOnTimeout());
                data.setRetryDelay(add.getDefinition().getRetriesOnTimeoutBeforeSuspend());

                return data;

            }
        }

        return null;
    }

    public static WSDLEndpointData getWSDLEndpointDetailsFrom(Template template, DefinitionFactory factory){
        OMElement endpointEl = template.getElement();
        if (endpointEl != null) {
            Properties properties = new Properties();
            //we will skip parsing of wsdl since we are trying to build a template
            //wsdl parsing will anyway be done when an endpoint is materialized from a template
            properties.setProperty(WSDLEndpointFactory.SKIP_WSDL_PARSING,"true");
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(endpointEl, factory, false, properties);
            if(endpoint!=null && endpoint instanceof WSDLEndpoint){
                WSDLEndpoint wsdlEp = (WSDLEndpoint) endpoint;

                WSDLEndpointData data = new WSDLEndpointData();
                data.setEpName(wsdlEp.getName());
                data.setEpUri(wsdlEp.getWsdlURI());
                data.setEpServ(wsdlEp.getServiceName());
                data.setEpPort(wsdlEp.getPortName());
                data.setEpType(WSDL_EP);
                data.setEpDur(wsdlEp.getDefinition().getInitialSuspendDuration());

                data.setEpwsdlTimeoutAction(wsdlEp.getDefinition().getTimeoutAction());

                data.setEpactionDuration(wsdlEp.getDefinition().getTimeoutDuration());
                data.setEpaddressingOn(wsdlEp.getDefinition().isAddressingOn());

                data.setEpsecutiryOn(wsdlEp.getDefinition().isSecurityOn());
                data.setEpwsaddSepListener(wsdlEp.getDefinition().isUseSeparateListener());

                data.setEprelMesg(wsdlEp.getDefinition().isReliableMessagingOn());
                data.setEpwsdlSecutiryKey(wsdlEp.getDefinition().getWsSecPolicyKey());
                data.setEprmKey(wsdlEp.getDefinition().getWsRMPolicyKey());

                data.setEperrorCodes(errorCodeListBuilder(wsdlEp.getDefinition().getSuspendErrorCodes()));
                data.setRetryDisabledErrorCodes(errorCodeListBuilder(wsdlEp.getDefinition().
                        getRetryDisabledErrorCodes()).trim());
                data.setEpmaxSusDuration(wsdlEp.getDefinition().getSuspendMaximumDuration());
                data.setEpsusProgFactor(wsdlEp.getDefinition().getSuspendProgressionFactor());


                data.setEptimdedOutErrorCodes(errorCodeListBuilder(wsdlEp.getDefinition().getTimeoutErrorCodes()));
                data.setEpretryTimeout(wsdlEp.getDefinition().getRetryDurationOnTimeout());
                data.setEpretryDelay(wsdlEp.getDefinition().getRetriesOnTimeoutBeforeSuspend());

                if (wsdlEp.getWsdlDoc() != null) {
                    data.setInLineWSDL(wsdlEp.getWsdlDoc().toString());
                }

                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));


            }
        }

        return null;
    }

    public static AddressEndpointData getAddressEndpointDetailsFrom(Template template, DefinitionFactory factory){
        OMElement endpointEl = template.getElement();
        if (endpointEl != null) {
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(endpointEl, factory, false, new Properties());
            if(endpoint!=null && endpoint instanceof AddressEndpoint){
                AddressEndpoint add = (AddressEndpoint) endpoint;

                AddressEndpointData data = new AddressEndpointData();
                data.setEpName(add.getName());
                data.setAddress(add.getDefinition().getAddress());
                data.setEpType(ADDRESS_EP);
                //data.setFormat(add.getDefinition().getFormat());
                data.setSoap11(add.getDefinition().isForceSOAP11());
                data.setSoap12(add.getDefinition().isForceSOAP12());
                data.setRest(add.getDefinition().isForceREST());
                data.setGet(add.getDefinition().isForceGET());
                data.setPox(add.getDefinition().isForcePOX());
                data.setSwa(add.getDefinition().isUseSwa());
                data.setMtom(add.getDefinition().isUseMTOM());
                data.setSuspendDurationOnFailure(add.getDefinition().getInitialSuspendDuration());
                data.setTimeoutAct(add.getDefinition().getTimeoutAction());
                data.setTimeoutActionDur(add.getDefinition().getTimeoutDuration());
                data.setWsadd(add.getDefinition().isAddressingOn());
                data.setSepList(add.getDefinition().isUseSeparateListener());
                data.setWssec(add.getDefinition().isSecurityOn());
                data.setWsrm(add.getDefinition().isReliableMessagingOn());
                data.setRmPolKey(add.getDefinition().getWsRMPolicyKey());
                data.setSecPolKey(add.getDefinition().getWsSecPolicyKey());
                data.setMaxSusDuration(add.getDefinition().getSuspendMaximumDuration());
                data.setSusProgFactor(add.getDefinition().getSuspendProgressionFactor());
                data.setErrorCodes(errorCodeListBuilder(add.getDefinition().getSuspendErrorCodes()).trim());
                data.setRetryDisabledErrorCodes(errorCodeListBuilder(add.getDefinition().
                        getRetryDisabledErrorCodes()).trim());
                data.setTimdedOutErrorCodes(errorCodeListBuilder(add.getDefinition().getTimeoutErrorCodes()));
                data.setRetryTimeout(add.getDefinition().getRetryDurationOnTimeout());
                data.setRetryDelay(add.getDefinition().getRetriesOnTimeoutBeforeSuspend());

                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));


            }
        }

        return null;
    }

    private static String errorCodeListBuilder(List<Integer> errCodes) {
        String errorCodes = " ";
        for (Integer errCode : errCodes) {
            errorCodes += errCode;
            errorCodes += ",";
        }
        return errorCodes.substring(0, errorCodes.length() - 1);
    }

    public static String getMappingFrom(TemplateParameterContainer container, TemplateParameterContainer.EndpointDefKey key){
        String mapping = container.getTemplateMapping(key);
        if (mapping != null) {
            return mapping;
        }
        if (key == TemplateParameterContainer.EndpointDefKey.suspendProgressionFactor) {
            return "1.0";
        } else if (key == TemplateParameterContainer.EndpointDefKey.retryDurationOnTimeout) {
            return "0";
        } else if (key == TemplateParameterContainer.EndpointDefKey.retriesOnTimeoutBeforeSuspend) {
            return "0";
        } else {
            return "";
        }
    }
}
