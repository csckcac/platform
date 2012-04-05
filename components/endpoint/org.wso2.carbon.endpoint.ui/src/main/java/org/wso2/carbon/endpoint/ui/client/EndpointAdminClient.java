/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.endpoint.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.endpoints.*;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.mediators.MediatorProperty;
import org.wso2.carbon.endpoint.common.EndpointAdminException;
import org.wso2.carbon.endpoint.common.to.*;
import org.wso2.carbon.endpoint.common.to.AddressEndpointData;
import org.wso2.carbon.endpoint.common.to.DefaultEndpointData;
import org.wso2.carbon.endpoint.common.to.EndpointMetaData;
import org.wso2.carbon.endpoint.common.to.LoadBalanceEndpointData;
import org.wso2.carbon.endpoint.common.to.TemplateEndpointData;
import org.wso2.carbon.endpoint.common.to.WSDLEndpointData;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.endpoint.stub.types.common.ConfigurationObject;
import org.wso2.carbon.endpoint.stub.types.common.to.*;
import org.wso2.carbon.endpoint.ui.TestUtil;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;
import org.wso2.carbon.endpoint.ui.util.TemplateConfigurationBuilder;
import org.wso2.carbon.endpoint.ui.util.TemplateEndpointHelper;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.sql.Array;
import java.util.*;

public class EndpointAdminClient {

    public static final int ADDRESS_EP = 0;
    public static final int FAILOVER_EP = 2;
    public static final int WSDL_EP = 1;
    public static final int LOADBALANCE_EP = 3;
    public static final int DEFAULT_EP=4;

    public static final int ENDPOINT_PER_PAGE = 10;

    private EndpointAdminStub stub;
    private static final Log log = LogFactory.getLog(EndpointAdminClient.class);

    /**
     * EndpointAdminClient constructor
     * @param cookie cookie
     * @param backendServerURL the backend server URL
     * @param configCtx axis2 Configuaration Context
     * @throws AxisFault incase of an error
     */
    public EndpointAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx) throws AxisFault{

        String serviceURL = backendServerURL + "EndpointAdmin";
        stub = new EndpointAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        
    }

    /**
     * Return endpoint metadata
     * @return return Endpoint metadata
     * @throws Exception in-case of an error
     */
    public EndpointMetaData[] getEndpointMetaData(int pageNumber, int endpointsPerPage) throws Exception {
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.EndpointMetaData[] tempDatas = stub.endpointData(pageNumber, endpointsPerPage);
            if (tempDatas != null && tempDatas.length > 0 && tempDatas[0] != null) {
                EndpointMetaData[] datas = new EndpointMetaData[tempDatas.length];
                for (int i = 0; i < tempDatas.length; i++) {
                    org.wso2.carbon.endpoint.stub.types.common.to.EndpointMetaData tempData =
                            tempDatas[i];
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(tempData.getName());
                    data.setEnableStatistics(tempData.getEnableStatistics());
                    data.setType(tempData.getType());
                    data.setDescription(tempData.getDescription());
                    data.setSwitchOn(tempData.getSwitchOn());
                    datas[i] = data;
                }

                return datas;
            }
        } catch (Exception e) {
            handleFault(e);
        }

        // never executes but keeps the compiler happy :-)
        return null;
    }

    public int getEndpointCount() throws Exception {
        try {
            return stub.getEndpointCount(); 
        } catch (Exception e) {
            handleFault(e);
        }
        return 0;
    }

    /**
     * Save an endpoint
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void saveEndpoint(String epName) throws Exception {
        try {
            stub.saveEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Save an endpoint to the Synapse registry
     * @param key dynamic endpoint key
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void saveDynamicEndpoint(String key, String epName) throws Exception {
        try {
            stub.saveDynamicEndpoint(key, epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Add an endpoint
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void addEndpoint(String epName) throws Exception {
        try{
            stub.addEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Add an endpoint to the Synapse registry
     * @param key of the dynamica endpoint
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void addDynamicEndpoint(String key, String epName) throws Exception {
        try {
            if(!stub.addDynamicEndpoint(key, epName)){
                throw new Exception("Endpoint \'" + key + "\' is already found in Registry");
            }
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Enabeling statistics at an endpoint
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void enableStatistics(String epName) throws Exception {
        try {
            stub.enableStatistics(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Disabling statistics at an endpoint
     * @param epName endpoint name
     * @throws Exception incase of an error
     */
    public void disableStatistics(String epName) throws Exception {
        try {
            stub.disableStatistics(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    public void switchOn(String epName) throws Exception {
        try {
            stub.switchOn(epName);
        } catch (Exception ex) {
            handleFault(ex);
        }
    }


    public void switchOff(String epName) throws Exception {
        try {
            stub.switchOff(epName);
        } catch (Exception ex) {
            handleFault(ex);
        }
    }

    /**
     * Delete an endpoint given by the name
     * @param epName endpoint name
     * @throws Exception incase of an error 
     */
    public void deleteEndpoint(String epName) throws Exception {
        try{
            stub.deleteEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    public ConfigurationObject[] getDependents(String epName) throws Exception {
        try {
            ConfigurationObject[] dependents = stub.getDependents(epName);
            if (dependents != null && dependents.length > 0 && dependents[0] != null) {
                return dependents;
            }
        } catch (RemoteException e) {
            handleFault(e);
        }

        return null;
    }

    /**
     * Returns address endpoint data 
     * @param epName endpoint name
     * @return address endpoint data of the endpoint
     * @throws Exception incase of an error
     */
    public AddressEndpointData getAddressEndpoint(String epName) throws Exception {
        AddressEndpointData data = new AddressEndpointData();
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.AddressEndpointData tempData
                    = stub.getAddressEndpoint(epName);
            data.setAddress(tempData.getAddress());
            data.setEpName(tempData.getEpName());
            data.setEpType(tempData.getEpType());
            data.setErrorCodes(tempData.getErrorCodes());
            data.setFormat(tempData.getErrorCodes());
            data.setMaxSusDuration(tempData.getMaxSusDuration());
            data.setMtom(tempData.getMtom());
            data.setPox(tempData.getPox());
            data.setRest(tempData.getRest());
            data.setGet(tempData.getGet());
            data.setRetryDelay(tempData.getRetryDelay());
            data.setRetryTimeout(tempData.getRetryTimeout());
            data.setRmPolKey(tempData.getRmPolKey());
            data.setSecPolKey(tempData.getSecPolKey());
            data.setSepList(tempData.getSepList());
            data.setSoap11(tempData.getSoap11());
            data.setSoap12(tempData.getSoap12());
            data.setSuspendDurationOnFailure(tempData.getSuspendDurationOnFailure());
            data.setSusProgFactor(tempData.getSusProgFactor());
            data.setSwa(tempData.getSwa());
            data.setTimdedOutErrorCodes(tempData.getTimdedOutErrorCodes());
            data.setTimeoutAct(tempData.getTimeoutAct());
            data.setTimeoutActionDur(tempData.getTimeoutActionDur());
            data.setWsadd(tempData.getWsadd());
            data.setWsrm(tempData.getWsrm());
            data.setWssec(tempData.getWssec());
            data.setRetryDisabledErrorCodes(tempData.getRetryDisabledErrorCodes());
            data.setDescription(tempData.getDescription());
            data.setProperties(tempData.getProperties());
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    public AddressEndpointData getAddressEndpoint(Template template, DefinitionFactory factory) throws Exception {
        try {
            return TemplateConfigurationBuilder.getAddressEndpointDetailsFrom(template, factory);
        } catch (Exception e) {
            handleFault(e);
        }

//        return TestUtil.getAddressEndpoint(template);
        return null;
    }

    public TemplateEndpointData getTemplateEndpoint(String epName) throws Exception {
        TemplateEndpointData data = new TemplateEndpointData();
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.TemplateEndpointData tempData
                    = stub.getTemplateEndpoint(epName);
            data.setEpType(tempData.getEpType());
            data.setTargetTemplate(tempData.getTargetTemplate());
            String[] paramArray = tempData.getParametersAsColonSepArray();
//            Map<String ,String> params = new HashMap<String ,String>();
            /*for (String paramExpr : paramArray) {
                String[] entries = paramExpr.split(":");
                if (entries != null & entries.length == 2 && entries[0] != null && entries[1] != null
                        && !"".equals(entries[0]) && !"".equals(entries[1])) {
                    params.put(entries[0], entries[1]);
                }
            }*/
            data.setParametersAsColonSepArray(paramArray);
        } catch (Exception e) {
            handleFault(e);
        }
        return data;

//        return TestUtil.getTemplateEndpoint(epName);
    }

    /**
     * Returns address endpoint data
     * @param epName endpoint name
     * @return address endpoint data of the endpoint
     * @throws Exception incase of an error
     */
    public DefaultEndpointData getDefaultEndpoint(String epName) throws Exception {
        DefaultEndpointData data = new DefaultEndpointData();
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.DefaultEndpointData tempData
                    = stub.getDefaultEndpoint(epName);
            data.setEpName(tempData.getEpName());
            data.setEpType(tempData.getEpType());
            data.setErrorCodes(tempData.getErrorCodes());
            data.setFormat(tempData.getErrorCodes());
            data.setMaxSusDuration(tempData.getMaxSusDuration());
            data.setMtom(tempData.getMtom());
            data.setPox(tempData.getPox());
            data.setRest(tempData.getRest());
            data.setGet(tempData.getGet());
            data.setRetryDelay(tempData.getRetryDelay());
            data.setRetryTimeout(tempData.getRetryTimeout());
            data.setRmPolKey(tempData.getRmPolKey());
            data.setSecPolKey(tempData.getSecPolKey());
            data.setSepList(tempData.getSepList());
            data.setSoap11(tempData.getSoap11());
            data.setSoap12(tempData.getSoap12());
            data.setSuspendDurationOnFailure(tempData.getSuspendDurationOnFailure());
            data.setSusProgFactor(tempData.getSusProgFactor());
            data.setSwa(tempData.getSwa());
            data.setTimdedOutErrorCodes(tempData.getTimdedOutErrorCodes());
            data.setTimeoutAct(tempData.getTimeoutAct());
            data.setTimeoutActionDur(tempData.getTimeoutActionDur());
            data.setWsadd(tempData.getWsadd());
            data.setWsrm(tempData.getWsrm());
            data.setWssec(tempData.getWssec());
            data.setRetryDisabledErrorCodes(tempData.getRetryDisabledErrorCodes());
            data.setDescription(tempData.getDescription());
            data.setProperties(tempData.getProperties());
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Returns address endpoint data
     * @param template endpoint name
     * @return address endpoint data of the endpoint
     * @throws Exception incase of an error
     */
    public DefaultEndpointData getDefaultEndpoint(Template template, DefinitionFactory factory) throws Exception {
        try {
            return TemplateConfigurationBuilder.getDefaultEndpointDetailsFrom(template, factory);
        } catch (Exception e) {
            handleFault(e);
        }
//        return TestUtil.getDefaultEndpoint(template);
        return null;
    }

    /**
     * Returns WSDL endpoint data
     * @param epName endpoint name
     * @return WSDL endpoint data of the endpoint
     * @throws Exception incase of an error
     */
    public WSDLEndpointData getWSDLEndpoint(String epName) throws Exception {
        WSDLEndpointData data = new WSDLEndpointData();
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.WSDLEndpointData tempData
                    = stub.getdlEndpoint(epName);
            data.setEpName(tempData.getEpName());
            data.setEpType(tempData.getEpType());
            data.setEperrorCodes(tempData.getEperrorCodes());
            data.setEpmaxSusDuration(tempData.getEpmaxSusDuration());
            data.setEpactionDuration(tempData.getEpactionDuration());
            data.setEpaddressingOn(tempData.getEpaddressingOn());
            data.setEpDur(tempData.getEpDur());
            data.setEpPort(tempData.getEpPort());
            data.setEprelMesg(tempData.getEprelMesg());
            data.setEpretryDelay(tempData.getEpretryDelay());
            data.setEpretryTimeout(tempData.getEpretryTimeout());
            data.setEprmKey(tempData.getEprmKey());
            data.setEpsecutiryOn(tempData.getEpsecutiryOn());
            data.setEpServ(tempData.getEpServ());
            data.setEpsusProgFactor(tempData.getEpsusProgFactor());
            data.setEptimdedOutErrorCodes(tempData.getEptimdedOutErrorCodes());
            data.setEpUri(tempData.getEpUri());
            data.setEpwsaddSepListener(tempData.getEpwsaddSepListener());
            data.setEpwsdlSecutiryKey(tempData.getEpwsdlSecutiryKey());
            data.setEpwsdlTimeoutAction(tempData.getEpwsdlTimeoutAction());
            data.setInLineWSDL(tempData.getInLineWSDL());
            data.setRetryDisabledErrorCodes(tempData.getRetryDisabledErrorCodes());
            data.setDescription(tempData.getDescription());
            data.setProperties(tempData.getProperties());
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Returns WSDL endpoint data
     * @param template endpoint name
     * @return WSDL endpoint data of the endpoint
     * @throws Exception incase of an error
     */
    public WSDLEndpointData getWSDLEndpoint(Template template, DefinitionFactory factory) throws Exception {
        try {
            return TemplateConfigurationBuilder.getWSDLEndpointDetailsFrom(template, factory);
        } catch (Exception e) {
            handleFault(e);
        }

        return null;
    }

    /**
     * Returns endpoint name
     * @param epName endpoint name
     * @return endpoint name
     * @throws Exception incase of an error
     */
    public String getEndpoint(String epName) throws Exception {
        String data = null;
        try {
            data = stub.getEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Returns LoadBalance endpoint data
     * @param epName endpoint name
     * @return loadbalance endpoint data
     * @throws Exception incase of an error
     */
    public LoadBalanceEndpointData getLoadBalanceEndpoint(String epName) throws Exception {
        LoadBalanceEndpointData data = new LoadBalanceEndpointData();
        try {
            org.wso2.carbon.endpoint.stub.types.common.to.LoadBalanceEndpointData tempData
                    = stub.getLoadBalanceData(epName);
            data.setSessionTimeout(tempData.getSessionTimeout());
            data.setSessiontype(tempData.getSessiontype());
            data.setProperties(tempData.getProperties());
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Returns LoadBalance endpoint data
     * @param epName endpoint name
     * @return loadbalance endpoint data
     * @throws Exception incase of an error
     */
    public LoadBalanceEndpointData getLoadBalanceEndpoint(Template epName) throws Exception {
        LoadBalanceEndpointData data = new LoadBalanceEndpointData();
        try {

        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Exception handler
     * @param e exception
     * @throws Exception incase of an error 
     */
    private void handleFault(Exception e) throws Exception {
        log.error(e.getMessage(), e);
        throw e;
    }

    /**
     * Check for endpoint existance
     * @param epName endpoint name
     * @return true if endpint found, else false
     * @throws Exception incase of an error
     */
    public boolean isEndpointExist(String epName) throws Exception {
        String [] eps = stub.getEndPointsNames();
        if (eps != null && eps[0] != null && !"".equals(eps[0])) {
            for (String ep : eps) {
                if (ep.equals(epName)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns address endpoint data
     * @param epXML XML string representing the endpoint
     * @return address endpoint data
     * @throws XMLStreamException incase of an error
     */
    public static AddressEndpointData getAddressEndpointData(String epXML)
            throws XMLStreamException {
        try {
            OMElement elem = new StAXOMBuilder(
                    new ByteArrayInputStream(epXML.getBytes())).getDocumentElement();
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("",""));
            AddressEndpoint ep = (AddressEndpoint)
                    AddressEndpointFactory.getEndpointFromElement(elem, true, new Properties());
            return populateAddressEndpointData(ep);
        } catch (XMLStreamException e) {
            log.error("Unable to generate address endpoint data from the given anonymous XML", e);
            throw e;
        }
    }

    /**
     * Returns address endpoint data
     * @param epXML XML string representing the endpoint
     * @return address endpoint data
     * @throws XMLStreamException incase of an error
     */
    public static TemplateEndpointData getTemplateEndpointData(String epXML)
            throws XMLStreamException {
        try {
            OMElement elem = new StAXOMBuilder(
                    new ByteArrayInputStream(epXML.getBytes())).getDocumentElement();
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("",""));
            TemplateEndpoint ep = (TemplateEndpoint)
                    TemplateEndpointFactory.getEndpointFromElement(elem, true, new Properties());
            return EndpointConfigurationHelper.getTemplateEpFromSynEp(ep, null, true);
        } catch (XMLStreamException e) {
            log.error("Unable to generate address endpoint data from the given anonymous XML", e);
            throw e;
        }
    }

        /**
     * Returns address endpoint data
     * @param epXML XML string representing the endpoint
     * @return address endpoint data
     * @throws XMLStreamException incase of an error
     */
    public static DefaultEndpointData getDefaultEndpointData(String epXML)
            throws XMLStreamException {
        try {
            OMElement elem = new StAXOMBuilder(
                    new ByteArrayInputStream(epXML.getBytes())).getDocumentElement();
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("",""));
            DefaultEndpoint ep = (DefaultEndpoint)
                    DefaultEndpointFactory.getEndpointFromElement(elem, true, new Properties());
            return populateDefaultEndpointData(ep);
        } catch (XMLStreamException e) {
            log.error("Unable to generate address endpoint data from the given anonymous XML", e);
            throw e;
        }
    }

    public static String getAnonEpXMLwithName(String epXML) throws XMLStreamException{
        try {
            OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(epXML.getBytes())).getDocumentElement();
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("",""));
            return elem.toString();
        } catch (XMLStreamException e) {
            log.error("Unable to parse the given anonymous XML", e);
            throw e;
        }
    }

    public static AddressEndpointData populateAddressEndpointData(AddressEndpoint ep) {
        AddressEndpointData epData = new AddressEndpointData();
        epData.setEpName((ep.getName().equals("anonymous") ? "" : ep.getName()));
        epData.setAddress(ep.getDefinition().getAddress());
        epData.setEpType(ADDRESS_EP);
        epData.setSoap11(ep.getDefinition().isForceSOAP11());
        epData.setSoap12(ep.getDefinition().isForceSOAP12());
        epData.setRest(ep.getDefinition().isForceGET());
        epData.setPox(ep.getDefinition().isForcePOX());
        epData.setSwa(ep.getDefinition().isUseSwa());
        epData.setMtom(ep.getDefinition().isUseMTOM());
        epData.setSuspendDurationOnFailure(ep.getDefinition().getInitialSuspendDuration());
        epData.setTimeoutAct(ep.getDefinition().getTimeoutAction());
        epData.setTimeoutActionDur(ep.getDefinition().getTimeoutDuration());
        epData.setWsadd(ep.getDefinition().isAddressingOn());
        epData.setSepList(ep.getDefinition().isUseSeparateListener());
        epData.setWssec(ep.getDefinition().isSecurityOn());
        epData.setWsrm(ep.getDefinition().isReliableMessagingOn());
        epData.setRmPolKey(ep.getDefinition().getWsRMPolicyKey());
        epData.setSecPolKey(ep.getDefinition().getWsSecPolicyKey());
        epData.setMaxSusDuration(ep.getDefinition().getSuspendMaximumDuration());
        epData.setSusProgFactor(ep.getDefinition().getSuspendProgressionFactor());
        epData.setErrorCodes(errorCodeListBuilder(ep.getDefinition().getSuspendErrorCodes()).trim());
        epData.setTimdedOutErrorCodes(errorCodeListBuilder(ep.getDefinition().getTimeoutErrorCodes()));
        epData.setRetryTimeout(ep.getDefinition().getRetriesOnTimeoutBeforeSuspend());
        epData.setRetryDelay(ep.getDefinition().getRetryDurationOnTimeout());
        epData.setProperties(buildPropertyString(ep));
        epData.setDescription(ep.getDescription());
        return epData;
    }

    public static DefaultEndpointData populateDefaultEndpointData(DefaultEndpoint ep) {
        DefaultEndpointData epData = new DefaultEndpointData();
        epData.setEpName((ep.getName().equals("anonymous") ? "" : ep.getName()));
        epData.setEpType(DEFAULT_EP);
        epData.setSoap11(ep.getDefinition().isForceSOAP11());
        epData.setSoap12(ep.getDefinition().isForceSOAP12());
        epData.setRest(ep.getDefinition().isForceGET());
        epData.setPox(ep.getDefinition().isForcePOX());
        epData.setSwa(ep.getDefinition().isUseSwa());
        epData.setMtom(ep.getDefinition().isUseMTOM());
        epData.setSuspendDurationOnFailure(ep.getDefinition().getInitialSuspendDuration());
        epData.setTimeoutAct(ep.getDefinition().getTimeoutAction());
        epData.setTimeoutActionDur(ep.getDefinition().getTimeoutDuration());
        epData.setWsadd(ep.getDefinition().isAddressingOn());
        epData.setSepList(ep.getDefinition().isUseSeparateListener());
        epData.setWssec(ep.getDefinition().isSecurityOn());
        epData.setWsrm(ep.getDefinition().isReliableMessagingOn());
        epData.setRmPolKey(ep.getDefinition().getWsRMPolicyKey());
        epData.setSecPolKey(ep.getDefinition().getWsSecPolicyKey());
        epData.setMaxSusDuration(ep.getDefinition().getSuspendMaximumDuration());
        epData.setSusProgFactor(ep.getDefinition().getSuspendProgressionFactor());
        epData.setErrorCodes(errorCodeListBuilder(ep.getDefinition().getSuspendErrorCodes()).trim());
        epData.setTimdedOutErrorCodes(errorCodeListBuilder(ep.getDefinition().getTimeoutErrorCodes()));
        epData.setRetryTimeout(ep.getDefinition().getRetriesOnTimeoutBeforeSuspend());
        epData.setRetryDelay(ep.getDefinition().getRetryDurationOnTimeout());
        epData.setProperties(buildPropertyString(ep));
        epData.setDescription(ep.getDescription());
        return epData;
    }

    public static String errorCodeListBuilder(List<Integer> errCodes){
        String errorCodes = " ";
        for (Integer errCode : errCodes) {
            errorCodes += errCode;
            errorCodes += ",";
        }
        return errorCodes.substring(0,errorCodes.length()-1);
    }

    public static WSDLEndpointData getWsdlEndpointData(String epXML) throws XMLStreamException{
         try {
            OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(epXML.getBytes())).getDocumentElement();
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("",""));
             Properties props = new Properties();
             props.setProperty(WSDLEndpointFactory.SKIP_WSDL_PARSING, "true");
            WSDLEndpoint ep = (WSDLEndpoint) WSDLEndpointFactory.getEndpointFromElement(elem, true, props);
            return populateWsdlEndpointData(ep);
        } catch (XMLStreamException e) {
            log.error("Unable to generate WSDL endpoint data from the given anonymous XML", e);
            throw e;
        }
    }

    public static WSDLEndpointData populateWsdlEndpointData(WSDLEndpoint ep) {
        WSDLEndpointData data = new WSDLEndpointData();
        data.setEpName((ep.getName().equals("anonymous") ? "" : ep.getName()));
        data.setEpUri(ep.getWsdlURI());
        data.setEpServ(ep.getServiceName());
        data.setEpPort(ep.getPortName());
        data.setEpType(WSDL_EP);
        data.setEpDur(ep.getDefinition().getInitialSuspendDuration());

        data.setEpwsdlTimeoutAction(ep.getDefinition().getTimeoutAction());

        data.setEpactionDuration(ep.getDefinition().getTimeoutDuration());
        data.setEpaddressingOn(ep.getDefinition().isAddressingOn());

        data.setEpsecutiryOn(ep.getDefinition().isSecurityOn());
        data.setEpwsaddSepListener(ep.getDefinition().isUseSeparateListener());

        data.setEprelMesg(ep.getDefinition().isReliableMessagingOn());
        data.setEpwsdlSecutiryKey(ep.getDefinition().getWsSecPolicyKey());
        data.setEprmKey(ep.getDefinition().getWsRMPolicyKey());

        data.setEperrorCodes(errorCodeListBuilder(ep.getDefinition().getSuspendErrorCodes()));
        data.setEpmaxSusDuration(ep.getDefinition().getSuspendMaximumDuration());
        data.setEpsusProgFactor(ep.getDefinition().getSuspendProgressionFactor());


        data.setEptimdedOutErrorCodes(errorCodeListBuilder(ep.getDefinition().getTimeoutErrorCodes()));
        data.setEpretryTimeout(ep.getDefinition().getRetryDurationOnTimeout());
        data.setEpretryDelay(ep.getDefinition().getRetriesOnTimeoutBeforeSuspend());
        data.setProperties(buildPropertyString(ep));
        if(ep.getWsdlDoc()!=null){
            data.setInLineWSDL(ep.getWsdlDoc().toString());
        }
        data.setDescription(ep.getDescription());
        return data;
    }

    /**
     * Returns an OMElement for xml string
     * @param xml XML string
     * @return the OMElement
     * @throws XMLStreamException incase of an error
     */
    public static OMElement createOMFromString(String xml) throws XMLStreamException {
        try {
            return new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
        } catch (XMLStreamException e) {
            log.error("Unable to parse the endpoint XML", e);
            throw e;
        }
    }

    public void updateDynamicEndpoint(String key, String epName) throws Exception {
        try {
            stub.deleteDynamicEndpoint(key);
            stub.addDynamicEndpoint(key, epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    public void deleteDynamicEndpoint(String key) throws Exception {
        try {
            stub.deleteDynamicEndpoint(key);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    public String getDynamicEndpoint(String key) throws Exception {
        String data = null;
        try {
            data = stub.getDynamicEndpoint(key);
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    public String[] getDynamicEndpoints(int pageNumber, int endpointsPerPage) throws Exception {
        String[] endpoints = null;
        try {
            endpoints = stub.getDynamicEndpoints(pageNumber, endpointsPerPage);
        }
        catch (Exception e) {
            handleFault(e);
        }
        return endpoints;
    }

    public int getDynamicEndpointCount() throws Exception {
        try{
            return stub.getDynamicEndpointCount();
        } catch (Exception e) {
            handleFault(e);
        }
        return 0;
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
}
