/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.endpoint.ui.util;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.endpoints.TemplateEndpointSerializer;
import org.apache.synapse.config.xml.endpoints.TemplateSerializer;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.mediators.MediatorProperty;
import org.wso2.carbon.endpoint.common.to.AddressEndpointData;
import org.wso2.carbon.endpoint.common.to.TemplateEndpointData;
import org.wso2.carbon.endpoint.common.to.WSDLEndpointData;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.endpoint.ui.client.EndpointAdminClient;
import org.wso2.carbon.endpoint.common.to.DefaultEndpointData;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

public class EndpointConfigurationHelper {
    private static final String SYNAPSE_NS = SynapseConstants.SYNAPSE_NAMESPACE;
    private static final String gt = ">";
    private static final String lt = "<";
    public static final String ROUNDROBIN_ALGO_CLASS_NAME =
            "org.apache.synapse.endpoints.algorithms.RoundRobin";

    public static String buildAddressOrWSDLEpConfiguration(
            HttpServletRequest request, String endpointType, boolean isAnonymous) {

        String endpointName = request.getParameter("endpointName");
        String address = null;
        String formatOption = null;
        String optimizeOption = null;

        String wsdlURI = null;
        String inLineWSDL = null;
        String service = null;
        String port = null;
        boolean isInlineWSDL = false;
        String description = "";

        if (endpointType.equals("addressEndpoint")) {
            address = request.getParameter("address");
            formatOption = request.getParameter("format");
            optimizeOption = request.getParameter("optimize");
        } else if (endpointType.equals("WSDLEndpoint")) {
            String inlineWSDLVal = request.getParameter("inlineWSDLVal");
            String uriWSDLval = request.getParameter("uriWSDLVal");
            if (inlineWSDLVal.trim() != null && !"".equals(inlineWSDLVal.trim())) {
                // we have inline WSDL mode
                inLineWSDL = request.getParameter("inlineWSDLVal");
                inLineWSDL = inLineWSDL.replaceAll("\\s+", " "); // if there are more than one spaces replace it with a single space
                isInlineWSDL = true;
            } else if (uriWSDLval.trim() != null && !"".equals(uriWSDLval.trim())) {
                // we are in URI mode
                wsdlURI = request.getParameter("uriWSDLVal");
            }

            service = request.getParameter("wsdlendpointService");
            port = request.getParameter("wsdlendpointPort");
        } else if (endpointType.equals("defaultEndpoint")) {
            formatOption = request.getParameter("format");
            optimizeOption = request.getParameter("optimize");
        }

        description = request.getParameter("endpointDescription");

        String errorCode = request.getParameter("suspendErrorCode");
        String suspendDuration = request.getParameter("suspendDuration");
        String suspendMaxDuration = request.getParameter("suspendMaxDuration");
        String factor = request.getParameter("factor");

        String retryErrorCode = request.getParameter("retryErroCode");
        String retriesOnTimeOut = request.getParameter("retryTimeOut");
        String retryDelay = request.getParameter("retryDelay");
        String disabledErrorCodes = request.getParameter("disabledErrorCodes");
        String action = request.getParameter("actionSelect");
        String actionDuration = null;
        if (action != null && !action.equals("neverTimeout")) {
            actionDuration = request.getParameter("actionDuration");
        }

        String wsAddressing = request.getParameter("wsAddressing");
        String useSeprateListner = null;
        if (wsAddressing != null) {
            useSeprateListner = request.getParameter("sepListener");
        }
        String wsSecurity = request.getParameter("wsSecurity");
        String secPolicy = null;
        if (wsSecurity != null) {
            secPolicy = request.getParameter("wsSecPolicyKeyID");
        }
        String wsRM = request.getParameter("wsRM");
        String rmPolicy = null;
        if (wsRM != null) {
            rmPolicy = request.getParameter("wsrmPolicyKeyID");
        }
        String properties = request.getParameter("endpointProperties");
        //build the address endpoint configuration
        StringBuilder addressEpConfiguration = new StringBuilder();
        addressEpConfiguration.append(lt + "endpoint xmlns=\"" + SYNAPSE_NS + "\"");


        // if this is an anonymous endpoint we don't set the name
        if (isAnonymous) {
            addressEpConfiguration.append(gt);
        } else {
            addressEpConfiguration.append(" name=\"" + endpointName.trim() + "\"" + gt);
        }

        if (endpointType.equals("addressEndpoint") || endpointType.equals("defaultEndpoint")) {
            // address
            if (endpointType.equals("addressEndpoint")) {
                String validURL = getValidXMLString(address);
//            addressEpConfiguration.append(lt + "address uri=\"" + address.trim());
                addressEpConfiguration.append(lt + "address uri=\"" + validURL.trim());
                addressEpConfiguration.append("\"");
            } else {
                addressEpConfiguration.append(lt + "default");
            }
            // format
            if (formatOption != null && !"".equals(formatOption) && !"leave-as-is".equals(formatOption)) {
                addressEpConfiguration.append(" format=\"");
                if ("soap11".equals(formatOption)) {
                    addressEpConfiguration.append("soap11");
                } else if ("soap12".equals(formatOption)) {
                    addressEpConfiguration.append("soap12");
                } else if ("POX".equals(formatOption)) {
                    addressEpConfiguration.append("pox");
                } else if ("REST".equals(formatOption)) {
                    addressEpConfiguration.append("rest");
                } else if ("GET".equals(formatOption)) {
                    addressEpConfiguration.append("get");
                }
                addressEpConfiguration.append("\"");
            }

            // optimize
            if (optimizeOption != null && !"".equals(optimizeOption) && !"leave-as-is".equals(optimizeOption)) {
                addressEpConfiguration.append(" optimize=\"");
                if ("SWA".equals(optimizeOption)) {
                    addressEpConfiguration.append("swa");
                } else if ("MTOM".equals(optimizeOption)) {
                    addressEpConfiguration.append("mtom");
                }
                addressEpConfiguration.append("\"");
            }
            addressEpConfiguration.append(" " + gt);

        } else if (endpointType.equals("WSDLEndpoint")) {
            addressEpConfiguration.append(lt + "wsdl ");
            if (!isInlineWSDL) {
                addressEpConfiguration.append("uri=\"" + wsdlURI + "\" ");
            }
            addressEpConfiguration.append("service=\"" + service + "\" ");
            addressEpConfiguration.append("port=\"" + port + "\" ");
            addressEpConfiguration.append(gt);
            if (isInlineWSDL) {
                if (inLineWSDL != null && !"".equals(inLineWSDL)) {
                    if (inLineWSDL.startsWith("<?xml ")) {
                        String decl = inLineWSDL.substring(0, inLineWSDL.indexOf('>') + 1);
                        addressEpConfiguration.insert(0, decl);
                        inLineWSDL = inLineWSDL.substring(inLineWSDL.indexOf(">") + 1);
                    }
                    addressEpConfiguration.append(inLineWSDL);
                }
            }
        }


        //Suspend configuration
        String suspendConfiguration;
        if ((errorCode != null && !"".equals(errorCode)) || (suspendDuration != null && !"".equals(suspendDuration))
                || (suspendMaxDuration != null && !"".equals(suspendMaxDuration)) ||
                (factor != null && !"".equals(factor))) {
            suspendConfiguration = "<suspendOnFailure>";
            if (errorCode != null && !"".equals(errorCode)) {
                suspendConfiguration += "<errorCodes>" + errorCode.trim() + "</errorCodes>";
            }
            if (suspendDuration != null && !"".equals(suspendDuration)) {
                suspendConfiguration += "<initialDuration>" + (suspendDuration.trim().startsWith("$") ? suspendDuration.trim() : Long.valueOf(suspendDuration.trim())) + "</initialDuration>";
            }
            if (factor != null && !"".equals(factor)) {
                suspendConfiguration += "<progressionFactor>" + (factor.trim().startsWith("$") ? factor : Float.valueOf(factor)) + "</progressionFactor>";

            }
            if (suspendMaxDuration != null && !"".equals(suspendMaxDuration)) {
                suspendConfiguration += "<maximumDuration>" + (suspendMaxDuration.trim().startsWith("$") ? suspendMaxDuration : Long.valueOf(suspendMaxDuration)) + "</maximumDuration>";
            }
            suspendConfiguration += "</suspendOnFailure>";
            addressEpConfiguration.append(suspendConfiguration);
        }

        // retry time configuration
        String retryTimeConfiguration;
        if ((retryErrorCode != null && !"".equals(retryErrorCode)) || (retryDelay != null && !"".equals(retryDelay))
                || (retriesOnTimeOut != null && !"".equals(retriesOnTimeOut))) {
            retryTimeConfiguration = "<markForSuspension>";
            if (retryErrorCode != null && !"".equals(retryErrorCode)) {
                retryTimeConfiguration += "<errorCodes>" + retryErrorCode.trim() + "</errorCodes>";
            }
            if (retriesOnTimeOut != null && !"".equals(retriesOnTimeOut)) {
                retryTimeConfiguration += "<retriesBeforeSuspension>" + retriesOnTimeOut.trim() + "</retriesBeforeSuspension>";
            }
            if (retryDelay != null && !"".equals(retryDelay)) {
                retryTimeConfiguration += "<retryDelay>" + retryDelay.trim() + "</retryDelay>";
            }
            retryTimeConfiguration += "</markForSuspension>";
            addressEpConfiguration.append(retryTimeConfiguration);
        }

        String disabledErrorCodesConfiguration;
        if ((disabledErrorCodes != null) && (!"".equals(disabledErrorCodes))) {
            disabledErrorCodesConfiguration = "<retryConfig><disabledErrorCodes>" +
                    disabledErrorCodes + "</disabledErrorCodes></retryConfig>";
            addressEpConfiguration.append(disabledErrorCodesConfiguration);
        }

        //time out configuration
        String timeOutConfiguration;
        if (((action != null && !"".equals(action)) || (actionDuration != null && !"".equals(actionDuration)))
                && !"neverTimeout".equals(action)) {
            timeOutConfiguration = "<timeout>";
            if (actionDuration != null && !"".equals(actionDuration)) {
                timeOutConfiguration += "<duration>" + actionDuration.trim() + "</duration>";
            }
            if (action != null && !"".equals(action)) {
                if (action.equals("discardMessage")) {
                    timeOutConfiguration += "<responseAction>discard</responseAction>";
                } else if (action.equals("executeFaultSequence")) {
                    timeOutConfiguration += "<responseAction>fault</responseAction>";
                }
            }
            timeOutConfiguration += "</timeout>";
            addressEpConfiguration.append(timeOutConfiguration);
        }

        // QoS configuration
        String useWSAConf;
        if (wsAddressing != null) {
            if (useSeprateListner != null && !"".equals(useSeprateListner)) {
                useWSAConf = "<enableAddressing separateListener=\"true\" />";
            } else {
                useWSAConf = "<enableAddressing/>";
            }
            if (useWSAConf != null) {
                addressEpConfiguration.append(useWSAConf);
            }
        }

        String useWSSecConf;
        if (wsSecurity != null) {
            if (secPolicy != null && !"".equals(secPolicy)) {
                useWSSecConf = "<enableSec policy=\"" + secPolicy + "\"/>";
            } else {
                useWSSecConf = "<enableSec/>";
            }
            if (useWSSecConf != null) {
                addressEpConfiguration.append(useWSSecConf);
            }
        }

        String useWSRMConf;
        if (wsRM != null) {
            if (rmPolicy != null && !"".equals(rmPolicy)) {
                useWSRMConf = "<enableRM policy=\"" + rmPolicy + "\"/>";
            } else {
                useWSRMConf = "<enableRM/>";
            }
            if (useWSRMConf != null) {
                addressEpConfiguration.append(useWSRMConf);
            }
        }

        String epPropertyConfig = "";
        if(properties != null && properties.length()!=0) {
            String[] props = properties.split("::");
            for(String s:props){
                String[] elements = s.split(",");
                epPropertyConfig = epPropertyConfig + "<property name=\"" + elements[0] +
                        "\" value=\"" + elements[1] + "\" scope=\"" + elements[2] + "\" />";
            }
        }

        if (endpointType.equals("addressEndpoint")) {
            addressEpConfiguration.append(lt + "/address" + gt);
        } else if (endpointType.equals("WSDLEndpoint")) {
            addressEpConfiguration.append(lt + "/wsdl" + gt);
        } else if (endpointType.equals("defaultEndpoint")) {
            addressEpConfiguration.append(lt + "/default" + gt);
        }

        addressEpConfiguration.append(epPropertyConfig);

        //Description Configuration
        if (description != null && !description.equals("")) {
            addressEpConfiguration.append(lt + "description" + gt + description + lt
                    + "/description" + gt);

        }

        addressEpConfiguration.append(lt + "/endpoint" + gt);

        return addressEpConfiguration.toString().trim();
    }

    public static String buildTemplateEndpointConfiguration(HttpServletRequest request, String endpointType
            , boolean isAnonymous) {
        TemplateEndpoint temp = new TemplateEndpoint();
        String endpointName = request.getParameter("endpointName");
        String address = request.getParameter("address");
        String targetTemplate = request.getParameter("mediator.call.target");

        if (endpointName != null & !"".equals(endpointName.trim())) {
            if(isAnonymous){
                temp.setEnableMBeanStats(false);
            }
            temp.setName(endpointName.trim());
            temp.addParameter("name", endpointName.trim());
        }
        if (address != null) {
            temp.setAddress(address.trim());
            temp.addParameter("uri", address.trim());
        }
        if (targetTemplate != null) {
            temp.setTemplate(targetTemplate.trim());
        }
        int paramCount = Integer.parseInt(request.getParameter("propertyCount"));
        for (int i = 0; i < paramCount; i++) {
            String paramName = request.getParameter("propertyName" + i);
            String paramValue = request.getParameter("propertyValue" + i);
            if (paramName != null && paramValue != null && !"".equals(paramName.trim())) {
                temp.addParameter(paramName.trim(), paramValue.trim());
            }
        }

        OMElement templateEndpointEl = new TemplateEndpointSerializer().serializeEndpoint(temp);


        if (templateEndpointEl != null) {
            return templateEndpointEl.toString();
        }
        return "";
    }

    public static String buildTemplateConfiguration(HttpServletRequest request, String endpointType
            , boolean isAnonymous) {
        String epConfig = buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
        OMElement epConfigElement = null;
        try {
            epConfigElement = AXIOMUtil.stringToOM(epConfig);
        } catch (XMLStreamException e) {
            //TODO handle this
        }

        String templateName = request.getParameter("templateName");
        Template template = new Template();
        if (templateName != null) {
            template.setName(templateName);
        }
        if (epConfig != null) {
            template.setElement(epConfigElement);
        } else {
            String emptyEndpointConf = lt + "endpoint xmlns=\"" + SYNAPSE_NS + "\"" + "/" + gt;
            OMElement emptyEndpointEl = null;
            try {
                emptyEndpointEl = AXIOMUtil.stringToOM(emptyEndpointConf);
            } catch (XMLStreamException e) {
                //TODO do nothing?
            }
            template.setElement(emptyEndpointEl);
        }

        OMElement parentElement = null;
        /*String dummyParentEl = lt + "parent xmlns=\"" + SYNAPSE_NS + "\"" + "/" + gt;
        try {
            parentElement = AXIOMUtil.stringToOM(dummyParentEl);
        } catch (XMLStreamException e) {

        }*/

        String paramCount = request.getParameter("propertyCount");
        String paramAction = request.getParameter("paramAction");
//        System.out.println("param count ....." + paramCount + " param action ....." + paramAction);

        int count = Integer.parseInt(paramCount);

        for (int i = 0; i < count; i++) {
            String paramName = request.getParameter("propertyName" + i);
            if (paramName != null && !"".equals(paramName.trim()) &&
                    (!"name".equals(paramName.trim()) && !"uri".equals(paramName.trim()))) {
                template.addParameter(paramName);
            }
//            System.out.println("param name ....." + paramName);
        }

        OMElement serializedTemplateEl = new TemplateSerializer().serializeEndpointTemplate(template,
                parentElement);

//        return serializedTemplateEl.getFirstElement().toString();
        return serializedTemplateEl.toString();
    }

    public static AddressEndpointData getAddressEpFromSynEp(AddressEndpoint add,
                                                            HttpSession session,
                                                            boolean isAnonymos) {
        AddressEndpointData data = new AddressEndpointData();
        // assigns no name if epMode is anon
        String epMode;
        if (isAnonymos) {
            data.setEpName("");
        } else {
            data.setEpName(add.getName());
        }

        if (add.getDescription() != null && !add.getDescription().equals("")) {
            data.setDescription(add.getDescription());
        }

        data.setAddress(add.getDefinition().getAddress());
        data.setEpType(0);
        //data.setFormat(add.getDefinition().getFormat());
        data.setSoap11(add.getDefinition().isForceSOAP11());
        data.setSoap12(add.getDefinition().isForceSOAP12());
        data.setGet(add.getDefinition().isForceGET());
        data.setRest(add.getDefinition().isForceREST());
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
        data.setTimdedOutErrorCodes(errorCodeListBuilder(add.getDefinition().getTimeoutErrorCodes()));
        data.setRetryTimeout(add.getDefinition().getRetryDurationOnTimeout());
        data.setRetryDelay(add.getDefinition().getRetriesOnTimeoutBeforeSuspend());
        return data;
    }

    public static TemplateEndpointData getTemplateEpFromSynEp(TemplateEndpoint add,
                                                              HttpSession session,
                                                              boolean isAnonymos) {
        TemplateEndpointData data = new TemplateEndpointData();
        //TODO no need to set names? since already available in parameters
        if (isAnonymos) {
//            data.s("");
        } else {
//            data.setEpName(add.getName());
        }
        data.setEpType(5);
        data.setTargetTemplate(add.getTemplate());
        data.setParametersAsColonSepArray(TemplateEndpointHelper.getColonSepArrayFromMap(add.getParameters()));
        return data;
    }

    public static DefaultEndpointData getDefaultEpFromSynEp(DefaultEndpoint def,
                                                            HttpSession session,
                                                            boolean isAnonymos) {
        DefaultEndpointData data = new DefaultEndpointData();
        // assigns no name if epMode is anon
        String epMode;
        if (isAnonymos) {
            data.setEpName("");
        } else {
            data.setEpName(def.getName());
        }
        data.setDescription(def.getDescription());
        data.setEpType(0);
        //data.setFormat(def.getDefinition().getFormat());
        data.setSoap11(def.getDefinition().isForceSOAP11());
        data.setSoap12(def.getDefinition().isForceSOAP12());
        data.setGet(def.getDefinition().isForceGET());
        data.setRest(def.getDefinition().isForceREST());
        data.setPox(def.getDefinition().isForcePOX());
        data.setSwa(def.getDefinition().isUseSwa());
        data.setMtom(def.getDefinition().isUseMTOM());
        data.setSuspendDurationOnFailure(def.getDefinition().getInitialSuspendDuration());
        data.setTimeoutAct(def.getDefinition().getTimeoutAction());
        data.setTimeoutActionDur(def.getDefinition().getTimeoutDuration());
        data.setWsadd(def.getDefinition().isAddressingOn());
        data.setSepList(def.getDefinition().isUseSeparateListener());
        data.setWssec(def.getDefinition().isSecurityOn());
        data.setWsrm(def.getDefinition().isReliableMessagingOn());
        data.setRmPolKey(def.getDefinition().getWsRMPolicyKey());
        data.setSecPolKey(def.getDefinition().getWsSecPolicyKey());
        data.setMaxSusDuration(def.getDefinition().getSuspendMaximumDuration());
        data.setSusProgFactor(def.getDefinition().getSuspendProgressionFactor());
        data.setErrorCodes(errorCodeListBuilder(def.getDefinition().getSuspendErrorCodes()).trim());
        data.setTimdedOutErrorCodes(errorCodeListBuilder(def.getDefinition().getTimeoutErrorCodes()));
        data.setRetryTimeout(def.getDefinition().getRetryDurationOnTimeout());
        data.setRetryDelay(def.getDefinition().getRetriesOnTimeoutBeforeSuspend());
        return data;
    }

    private static String errorCodeListBuilder(List<Integer> errCodes) {
        String errorCodes = " ";
        for (Integer errCode : errCodes) {
            errorCodes += errCode;
            errorCodes += ",";
        }
        return errorCodes.substring(0, errorCodes.length() - 1);
    }

    public static WSDLEndpointData getWSDLEpFromSynEp(WSDLEndpoint wsdlEp, HttpSession session, boolean isAnonymos) {
        WSDLEndpointData data = new WSDLEndpointData();
        // assigns no name if epMode is anon
        String epMode;
        if (isAnonymos) {
            data.setEpName("");
        } else {
            data.setEpName(wsdlEp.getName());
        }
        data.setDescription(wsdlEp.getDescription());
        data.setEpUri(wsdlEp.getWsdlURI());
        data.setEpServ(wsdlEp.getServiceName());
        data.setEpPort(wsdlEp.getPortName());
        data.setEpType(1);
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
        data.setEpmaxSusDuration(wsdlEp.getDefinition().getSuspendMaximumDuration());
        data.setEpsusProgFactor(wsdlEp.getDefinition().getSuspendProgressionFactor());


        data.setEptimdedOutErrorCodes(errorCodeListBuilder(wsdlEp.getDefinition().getTimeoutErrorCodes()));
        data.setEpretryTimeout(wsdlEp.getDefinition().getRetryDurationOnTimeout());
        data.setEpretryDelay(wsdlEp.getDefinition().getRetriesOnTimeoutBeforeSuspend());

        if (wsdlEp.getWsdlDoc() != null) {
            data.setInLineWSDL(wsdlEp.getWsdlDoc().toString());
        }

        return data;
    }

    public static OMElement getElementByID(OMElement e, String id) {
        QName qname = new QName(null, "id"); // id is always "id"
        Iterator childEleIterator = e.getChildElements();
        while (childEleIterator.hasNext()) {
            OMElement childElement = (OMElement) childEleIterator.next();
            if (childElement.getAttributeValue(qname) != null && childElement.getAttributeValue(qname).equals(id)) {
                return childElement;
            } else {
                return getElementByID(childElement, id);
            }
        }
        return null;
    }

    public static OMElement getSessionAttribute(OMElement e, String id) {
        QName qname = new QName(null, "id"); // id is always "id"
        Iterator childEleIterator = e.getChildElements();
        while (childEleIterator.hasNext()) {
            OMElement childElement = (OMElement) childEleIterator.next();
            if (childElement.getAttributeValue(qname) != null && childElement.getAttributeValue(qname).equals(id)) {
                return childElement;
            } else {
                return getElementByID(childElement, id);
            }
        }
        return null;
    }

    public static String getValidXMLString(String originalString) {
        // replace all the correct code with invalid code
        String validXMLString = originalString.replace("&amp;", "&");
        validXMLString = validXMLString.replace("&lt;", "<");
        validXMLString = validXMLString.replace("&gt;", ">");
        validXMLString = validXMLString.replace("&quot;", "\"");

        // now replace all the invalid code with correct one
        validXMLString = validXMLString.replace("&", "&amp;");
        validXMLString = validXMLString.replace("<", "&lt;");
        validXMLString = validXMLString.replace(">", "&gt;");
        validXMLString = validXMLString.replace("\"", "&quot;");

        return validXMLString;
    }

    public static String getValidStringXMlStringForAMP(String originalString) {
        String validXMLString = originalString.replace("&amp;", "&");
        validXMLString = validXMLString.replace("&", "&amp;");
        return validXMLString;
    }

    public static int getDynamicEndpointType(String key, EndpointAdminClient client) {
        try {
            String epXML = client.getDynamicEndpoint(key);
            StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(epXML.getBytes()));
            OMElement confElem = builder.getDocumentElement();
            OMElement elem = null;
            if ((elem = confElem.getFirstElement()) != null) {
                String type = elem.getLocalName();
                if ("address".equals(type)) {
                    return 0;
                } else if ("wsdl".equals(type)) {
                    return 1;
                } else if ("failover".equals(type)) {
                    return 2;
                } else if ("loadbalance".equals(type)) {
                    return 3;
                } else if ("default".equals(type)) {
                    return 4;
                } else if (confElem.getAttribute(new QName("template")) != null) {
                    return 5;
                }
            }
        } catch (Exception ignored) {

        }
        return -1;
    }

    public static String testAddressURL(String url) {
        String returnValue;
        if (url != null && !url.equals("")) {
            try {
                URL conn = new URL(url);
//                conn.getContent();
                returnValue = "success";
            }
//			catch (UnknownHostException e) {
//                returnValue = "unknown";
//          } 
			catch (MalformedURLException e) {
                returnValue = "malformed";
			} 
//			catch (ConnectException e) {
//                returnValue = "Cannot establish connection to the provided address.";
//       	} catch (UnknownServiceException e) {
//                returnValue = "unknown_service";
//          } catch (SSLHandshakeException e) {
//                returnValue = "ssl_error";
//          } 

			catch (Exception e) {
                // A HTTP 500 may result in an error here - so try to fetch the WSDL of endpoint
                returnValue = testWSDLURI(url + "?wsdl");
            }

        } else {
            returnValue = "Invalid address specified.";
        }
        //we cannot validate address EP other than HTTP and HTTPS. Also check for ':' in first few chars to distinguish
        // unsupported protocol and missing protocol identifier(malformed)
        if (url != null && !url.toUpperCase().startsWith("HTTP") && !url.toUpperCase().startsWith("HTTPS")) {
            if (url.contains(":") && url.indexOf(':') < 6) {
                returnValue = "unsupported";
            }
        }
        return returnValue;
    }

    public static String testWSDLURI(String wsdlUri) {
        String returnValue = "";
        if (wsdlUri != null && !wsdlUri.equals("")) {
            try {
                URI uri = new URI(wsdlUri);
                uri.toURL().getContent();
                returnValue = "success";
            } catch (URISyntaxException e) {
                returnValue = "malformed";
            } catch (MalformedURLException e) {
                returnValue = "malformed";
            } catch (UnknownHostException e) {
                returnValue = "unknown";
            } catch (ConnectException e) {
                // A HTTP 500 may result in an error here - But the address is considered valid
                returnValue = "Cannot establish connection to the provided address";
            } catch (SSLHandshakeException e) {
                returnValue = "ssl_error";
            } catch (Exception e) {
                returnValue = "Cannot establish connection to the provided address";
            }
        }
        return returnValue;
    }

    private static String buildPropertyString(AbstractEndpoint ep) {

        Iterator<MediatorProperty> itr = ep.getProperties().iterator();
        String ret = "";
        while (itr.hasNext()) {
            MediatorProperty prop = itr.next();
            if (ret.equals("")) {
                ret = prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            } else {
                ret = ret + "::" + prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            }
        }
        return ret;
    }

    public static String buildPropertyString(Collection<MediatorProperty> props) {
        if (props == null) {
            return "";
        }
        Iterator<MediatorProperty> itr = props.iterator();
        String ret = "";
        while (itr.hasNext()) {
            MediatorProperty prop = itr.next();
            if (ret.equals("")) {
                ret = prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            } else {
                ret = ret + "::" + prop.getName() + "," + prop.getValue() + "," + prop.getScope();
            }
        }
        return ret;
    }
}