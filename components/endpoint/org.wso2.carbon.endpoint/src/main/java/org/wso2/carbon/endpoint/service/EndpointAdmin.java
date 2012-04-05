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

package org.wso2.carbon.endpoint.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.AbstractOMMetaFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SimpleClientSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SoapSessionDispatcher;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.registry.Registry;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.wso2.carbon.endpoint.common.EndpointAdminException;
import org.wso2.carbon.endpoint.common.IEndpointAdmin;
import org.wso2.carbon.endpoint.common.to.*;
import org.wso2.carbon.endpoint.util.ConfigHolder;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * EndpointAdmin for endpoints
 */
public class EndpointAdmin extends AbstractServiceBusAdmin implements IEndpointAdmin {

    private static final Log log = LogFactory.getLog(EndpointAdmin.class);

    public static final int ADDRESS_EP = 0;
    public static final int WSDL_EP = 1;
    public static final int FAILOVER_EP = 2;
    public static final int LOADBALANCE_EP = 3;
    public static final int DEFAULT_EP=4;
    public static final int TEMPLATE_EP=5;

    public static final String  WSO2_ENDPOINT_MEDIA_TYPE = "application/vnd+wso2.endpoint";

    public static final String SEP_CHAR = ";";
    /**
     * Gets the details about the endpoints as a EndpointData array
     *
     * @return Array of EndpointData representing the endpoints in the
     *         SynapseConfiguration
     * @throws EndpointAdminException if an error occured while getting the data from
     *                                SynapseConfiguration
     */
    public EndpointMetaData[] endpointData(int pageNumber, int endpointsPerPage) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

            List<Endpoint> epList = new ArrayList<Endpoint>();
            for (Endpoint ep : namedEndpointCollection) {
                epList.add(ep);
            }

            Collections.sort(epList, new Comparator<Endpoint>() {
                public int compare(Endpoint o1, Endpoint o2) {
                    return (o1).getName().compareToIgnoreCase((o2).getName());
                }
            });

            EndpointMetaData[] info = listToEndpointMetaDatas(epList.toArray(new Endpoint[epList.size()]));
            EndpointMetaData[] ret;
            if (info.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                ret = new EndpointMetaData[endpointsPerPage];
            } else {
                ret = new EndpointMetaData[info.length - (endpointsPerPage * pageNumber)];
            }
            for (int i = 0; i < endpointsPerPage; ++i) {
                if (ret.length > i)
                    ret[i] = info[endpointsPerPage * pageNumber + i];
            }
            return ret;
        } finally {
            lock.unlock();
        }
    }

    public int getEndpointCount() throws EndpointAdminException {
        final Lock lock = getLock();
        try{
            lock.lock();
            return getSynapseConfiguration().getDefinedEndpoints().size();
        } catch (Exception e) {
            handleFault("Error while retrieving Endpoint count", e);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    /**
     * Enable statistics collection for the specified endpoint
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException on error
     */
    public void enableStatistics(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            if (ep instanceof AddressEndpoint) {
                /*AspectConfiguration aspectConf = ((AddressEndpoint) ep).getDefinition().getAspectConfiguration();
                if (aspectConf == null) {
                    AspectConfiguration aspectConfiguration = new AspectConfiguration(endpointName);
                    ((AddressEndpoint) ep).getDefinition().configure(aspectConfiguration);
                } */
                ((AddressEndpoint) ep).getDefinition().enableStatistics();

            } else if (ep instanceof WSDLEndpoint) {
                ((WSDLEndpoint) ep).getDefinition().enableStatistics();
            } else if (ep instanceof DefaultEndpoint) {
                ((DefaultEndpoint) ep).getDefinition().enableStatistics();
            } else {
                handleFault("Selected endpoint : " + endpointName +
                        " does not support statistics", null);
            }
            persistEndpoint(ep);
            if (log.isDebugEnabled()) {
                log.debug("Statistics enabled on endpoint : " + endpointName);
            }
        } catch (SynapseException syne) {
            handleFault("Error enabling statistics for the endpoint : " + endpointName, syne);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stop collecting statistics for a specified endpoint
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException on error
     */
    public void disableStatistics(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            if (ep instanceof AddressEndpoint) {
                ((AddressEndpoint) ep).getDefinition().disableStatistics();
            } else if (ep instanceof WSDLEndpoint) {
                ((WSDLEndpoint) ep).getDefinition().disableStatistics();
            } else if (ep instanceof DefaultEndpoint) {
                ((DefaultEndpoint) ep).getDefinition().disableStatistics();
            } else {
                handleFault("Selected endpoint : " + endpointName +
                        " does not support statistics", null);
            }
            persistEndpoint(ep);
            if (log.isDebugEnabled()) {
                log.debug("Statistics disabled on endpoint : " + endpointName);
            }
        } catch (SynapseException syne) {
            handleFault("Error disabling statistics for the endpoint : " + endpointName, syne);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set Endpoint status to Active
     *
     * @param endpointName name of the endpoint that need to switch on
     * @throws EndpointAdminException
     */
    public void switchOn(String endpointName) throws EndpointAdminException{
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            ep.getContext().switchOn();
            persistEndpoint(ep);

            if (log.isDebugEnabled()) {
                log.debug("Endpoint " + ep.getName() + " switched on");
            }

        } catch (SynapseException ex) {
            handleFault("Error switch on endpoint : " + endpointName, ex);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Set Endpoint status to Maintenance
     *
     * @param endpointName name of the endpoint that need to switch off
     * @throws EndpointAdminException
     */
    public void switchOff(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            ep.getContext().switchOff();
            persistEndpoint(ep);

            if (log.isDebugEnabled()) {
                log.debug("Endpoint " + ep.getName() + " switched off");
            }

        } catch (SynapseException ex) {
            handleFault("Error switch off endpoint : " + endpointName, ex);
        } finally {
            lock.unlock();
        }
    }

    public String[] getEndPointsNames() throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();
            return listToNames(namedEndpointCollection.toArray(
                    new Endpoint[namedEndpointCollection.size()]));
        } finally {
            lock.unlock();
        }
    }

    private String[] listToNames(Endpoint[] eps) {
        if (eps == null) {
            return null;
        } else {
            String[] datas = new String[eps.length];
            for (int i = 0; i < eps.length; i++) {
                Endpoint ep = eps[i];
                datas[i] = ep.getName();
            }
            return datas;
        }
    }

    private EndpointMetaData[] listToEndpointMetaDatas(Endpoint[] eps) {
        if (eps == null) {
            return null;
        } else {
            EndpointMetaData[] datas = new EndpointMetaData[eps.length];
            for (int i = 0; i < eps.length; i++) {
                Endpoint ep = eps[i];
                if (ep instanceof AddressEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(ADDRESS_EP);
                    data.setDescription(ep.getDescription());
                    EndpointDefinition def = ((AddressEndpoint) ep).getDefinition();
                    if (def.isStatisticsEnable()) {
                        data.setEnableStatistics(true);
                    } else {
                        data.setEnableStatistics(false);
                    }
                    if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                    datas[i] = data;
                } else if(ep instanceof DefaultEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(DEFAULT_EP);
                    data.setDescription(ep.getDescription());
                    EndpointDefinition def = ((DefaultEndpoint) ep).getDefinition();
                    if (def.isStatisticsEnable()) {
                        data.setEnableStatistics(true);
                    } else {
                        data.setEnableStatistics(false);
                    }
                     if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                    datas[i] = data;
                }
                else if (ep instanceof WSDLEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(WSDL_EP);
                    data.setDescription(ep.getDescription());
                    EndpointDefinition def = ((WSDLEndpoint) ep).getDefinition();
                    if (def.isStatisticsEnable()) {
                        data.setEnableStatistics(true);
                    } else {
                        data.setEnableStatistics(false);
                    }
                     if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                    datas[i] = data;
                } else if (ep instanceof FailoverEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(FAILOVER_EP);
                    data.setDescription(ep.getDescription());
                    datas[i] = data;
                     if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                } else if (ep instanceof LoadbalanceEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(LOADBALANCE_EP);
                    data.setDescription(ep.getDescription());
                    datas[i] = data;
                     if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                } else if (ep instanceof TemplateEndpoint) {
                    EndpointMetaData data = new EndpointMetaData();
                    data.setName(ep.getName());
                    data.setType(TEMPLATE_EP);
                    data.setDescription(ep.getDescription());
                    datas[i] = data;
                     if(ep.getContext().isState(EndpointContext.ST_ACTIVE)){
                        data.setSwitchOn(true);
                    } else {
                        data.setSwitchOn(false);
                    }
                }

            }
            return datas;
        }
    }


    public OMElement convertToEndpointData(OMElement epElement) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(
                    epElement, false, getSynapseConfiguration().getProperties());
            if (endpoint != null) {
                return epElement;
            } else {
                handleFault("Unable to create an endpoint definition", null);
            }
        } catch (SynapseException e) {
            handleFault("Unable to access the endpoint factory instance", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Add an endpoint described by the given OMElement
     *
     * @param epName - OMelement representing the endpoint that needs
     *               to be added
     * @throws EndpointAdminException if the element is not an endpoint or if an endpoint
     *                                wiht the same name exists
     * @return true if the endpoint was successfully added and false otherwise
     */
    public boolean addEndpoint(String epName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            OMElement endpointElement;
            try {
                endpointElement = AXIOMUtil.stringToOM(epName);
            }
            catch (XMLStreamException e) {
                return false;
            }
            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = endpointElement.getAttributeValue(new QName("name"));
                assertNameNotEmpty(endpointName);
                endpointName = endpointName.trim();
                log.debug("Adding endpoint : " + endpointName + " to the configuration");

                if (getSynapseConfiguration().getLocalRegistry()
                        .get(endpointName) != null) {
                    handleFault("The name " + endpointName +
                            " is already used within the configuration", null);
                } else {
                    SynapseConfiguration config = getSynapseConfiguration();
                    if (config.getEndpoint(endpointName) != null) {
                        handleFault("A endpoint with name "
                                + endpointName + " is already there.", null);
                    }
                    SynapseXMLConfigurationFactory.defineEndpoint(
                            config, endpointElement, config.getProperties());
                    Endpoint endpoint = config.getEndpoint(endpointName);
                    if (endpoint != null) {
                        if (endpoint instanceof AbstractEndpoint) {
                            endpoint.setFileName(
                                    ServiceBusUtils.generateFileName(endpoint.getName()));
                        }
                        endpoint.init(getSynapseEnvironment());
                        persistEndpoint(endpoint);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Added endpoint : " + endpointName + " to the configuration");
                }
                return true;
            } else {
                handleFault("Unable to create endpoint. Invalid XML definition", null);
            }
        } catch (SynapseException syne) {
            handleFault("Unable to add Endpoint ", syne);
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean addDynamicEndpoint(String key, String epName) throws EndpointAdminException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.reset();
        String originalKey = key;
        try {
            org.wso2.carbon.registry.core.Registry registry;
            if(key.startsWith("conf:")) {
                registry = getConfigSystemRegistry();
                key = key.replace("conf:","");
            } else {
                registry = getGovernanceRegistry();
                key = key.replace("gov:","");
            }
            if (!registry.resourceExists(key)) {

                try {
                    OMElement endpointElement = AXIOMUtil.stringToOM(epName);
                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    String name = originalKey.replace(":", "/");
                    endpointElement.addAttribute(fac.createOMAttribute("name",
                            fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, ""),
                            name));
                    XMLPrettyPrinter.prettify(endpointElement, stream);
                } catch (Exception e) {
                    handleFault("Unable to pretty print configuration", e);
                }
                epName = new String(stream.toByteArray()).trim();

                Resource resource = registry.newResource();
                resource.setMediaType(WSO2_ENDPOINT_MEDIA_TYPE);
                resource.setContent(epName);
                registry.put(key, resource);
            } else {
                log.warn("Resource is already exists");
                return false;
            }
        } catch (RegistryException e) {
            handleFault("WSO2 Registry Exception", e);
            return false;
        }
        return true;
    }

    /**
     * Save an existing endpoint from the given String representation of the XML
     *
     * @param epName - String representing the XML which describes the
     *               Endpoint element
     * @throws EndpointAdminException if the endpoint does not exists in the
     *                                SynapseConfiguration
     * @return true if the endpoint was saved successfully and false otherwise
     */
    public boolean saveEndpoint(String epName) throws EndpointAdminException {
        OMElement endpointElement;
        final Lock lock = getLock();
        try {
            lock.lock();
            try {
                endpointElement = AXIOMUtil.stringToOM(epName);
            } catch (XMLStreamException e) {
                return false;
            }
            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = endpointElement.getAttributeValue(new QName("name"));
                assertNameNotEmpty(endpointName);
                endpointName = endpointName.trim();
                if (log.isDebugEnabled()) {
                    log.debug("Updating the definition of the endpoint : " + endpointName);
                }

                Endpoint previousEndpoint = getSynapseConfiguration().getEndpoint(
                        endpointName.trim());

                if (previousEndpoint == null) {
                    handleFault("The endpoint named " + endpointName + " does not exist", null);
                }

                boolean staisticsState = false;
                if (previousEndpoint instanceof AddressEndpoint) {
                    AspectConfiguration configuration = ((AddressEndpoint) previousEndpoint).
                            getDefinition().getAspectConfiguration();
                    staisticsState = configuration != null && configuration.isStatisticsEnable();

                } else if (previousEndpoint instanceof WSDLEndpoint) {
                    AspectConfiguration configuration = ((WSDLEndpoint) previousEndpoint).
                            getDefinition().getAspectConfiguration();
                    staisticsState = configuration != null && configuration.isStatisticsEnable();
                }

                String fileName = null;
                if (previousEndpoint instanceof AbstractEndpoint) {
                    fileName = previousEndpoint.getFileName();
                }

                Endpoint endpoint = EndpointFactory.getEndpointFromElement(
                        endpointElement, false, getSynapseConfiguration().getProperties());
                if (endpoint == null) {
                    handleFault("Newly created endpoint is null ", null);
                }

                if (endpoint instanceof AddressEndpoint) {
                    if (staisticsState) {
                        ((AddressEndpoint) endpoint).getDefinition()
                                .enableStatistics();
                    }
                } else if (endpoint instanceof WSDLEndpoint) {
                    if (staisticsState) {
                        ((WSDLEndpoint) endpoint).getDefinition()
                                .enableStatistics();
                    }
                }

                if (fileName != null && endpoint instanceof AbstractEndpoint) {
                    endpoint.setFileName(fileName);
                }

                endpoint.init(getSynapseEnvironment());
                endpointName = endpointName.trim();
                getSynapseConfiguration().removeEndpoint(endpointName);
                getSynapseConfiguration().addEndpoint(endpointName, endpoint);
                persistEndpoint(endpoint);
                if (log.isDebugEnabled()) {
                    log.debug("Updated the definition of the endpoint : " + endpointName);
                }
                return true;
            } else {
                handleFault("Unable to update endpoint. Invalid XML definition", null);
            }
        } catch (SynapseException syne) {
            handleFault("Unable to edit Endpoint ", syne);
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean saveDynamicEndpoint(String key, String epName) throws EndpointAdminException {
        OMElement endpointElement;
        final Lock lock = getLock();
        try {
            lock.lock();
            endpointElement = AXIOMUtil.stringToOM(epName);

            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = "dynamicEndpoint";
                if (log.isDebugEnabled()) {
                    log.debug("Updating endpoint : " + endpointName + " in the Synapse registry");
                }

                Registry registry = getSynapseConfiguration().getRegistry();
                if (registry != null) {
                    if (registry.getRegistryEntry(key).getType() == null) {
                        handleFault("No resource exists by the key '" + key + "'", null);
                    }

                    registry.updateResource(key, endpointElement);

                    if (log.isDebugEnabled()) {
                        log.debug("Updated endpoint : " + endpointName + " in the Synapse registry");
                    }
                    return true;
                }
            } else {
                handleFault("Unable to create endpoint. Invalid XML definition", null);
            }
        } catch (XMLStreamException e) {
            return false;
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Gets the endpoint element as a string
     *
     * @param endpointName - name of the endpoint to be get
     * @throws EndpointAdminException on error
     * @return String representing the endpoint with the given endpoint name
     */
    public String getEndpoint(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                OMElement ele = EndpointSerializer.getElementFromEndpoint(
                        synapseConfiguration.getEndpoint(endpointName));
                return ele.toString();
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public AddressEndpointData getAddressEndpoint(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                AddressEndpoint add = (AddressEndpoint) synapseConfiguration.getEndpoint(endpointName);

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
                data.setDescription(add.getDescription());
                data.setProperties(buildPropertyString(add));

                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public DefaultEndpointData getDefaultEndpoint(String endpointName) throws EndpointAdminException {
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                DefaultEndpoint add = (DefaultEndpoint) synapseConfiguration.getEndpoint(endpointName);

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
                data.setDescription(add.getDescription());
                data.setProperties(buildPropertyString(add));
                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    private String errorCodeListBuilder(List<Integer> errCodes) {
        String errorCodes = " ";
        for (Integer errCode : errCodes) {
            errorCodes += errCode;
            errorCodes += ",";
        }
        return errorCodes.substring(0, errorCodes.length() - 1);
    }

    public WSDLEndpointData getdlEndpoint(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                WSDLEndpoint wsdlEp = (WSDLEndpoint) synapseConfiguration.getEndpoint(endpointName);

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
                data.setDescription(wsdlEp.getDescription());
                data.setProperties(buildPropertyString(wsdlEp));

                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public LoadBalanceEndpointData getLoadBalanceData(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                Endpoint ep = synapseConfiguration.getEndpoint(endpointName);

                if (ep instanceof SALoadbalanceEndpoint) {
                    LoadBalanceEndpointData epData = new LoadBalanceEndpointData();
                    SALoadbalanceEndpoint lb = (SALoadbalanceEndpoint) ep;
                    Dispatcher dispatcher = lb.getDispatcher();

                    if (dispatcher instanceof HttpSessionDispatcher) {
                        epData.setSessiontype("http");
                        epData.setSessionTimeout(lb.getSessionTimeout());
                    } else if (dispatcher instanceof SimpleClientSessionDispatcher) {
                        epData.setSessiontype("simpleClientSession");
                        epData.setSessionTimeout(lb.getSessionTimeout());
                    } else if (dispatcher instanceof SoapSessionDispatcher) {
                        epData.setSessiontype("soap");
                        epData.setSessionTimeout(lb.getSessionTimeout());
                    }
                    epData.setProperties(buildPropertyString(lb));
                    return epData;
                }
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;

    }

    public TemplateEndpointData getTemplateEndpoint(String endpointName) throws EndpointAdminException{
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                TemplateEndpoint add = (TemplateEndpoint) synapseConfiguration.getEndpoint(endpointName);
                TemplateEndpointData data = new TemplateEndpointData();
                data.setTargetTemplate(add.getTemplate());
                data.setParametersAsColonSepArray((getParametersAsColonSepArray(add.getParameters())));
                data.setEpType(TEMPLATE_EP);
                return data;
                //return EndpointSerializer.getElementFromEndpoint(
                //   synapseConfiguration.getEndpoint(endpointName.trim()));
            } else {
                handleFault("The tempalte endpoint with name " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }


    private String[]  getParametersAsColonSepArray(Map<String ,String> parameters) {
        String paramArray[] = new String[parameters.size()];
        int i = 0;
        for (String key : parameters.keySet()) {
            paramArray[i] = key + SEP_CHAR +parameters.get(key);
            i++;
        }
        return paramArray;
    }
    /**
     * Deletes the endpoint from the SynapseConfiguration
     *
     * @param endpointName - name of the endpoint to be deleted
     * @throws EndpointAdminException if the proxy service name given is not existent in the
     *                                synapse configuration
     * @return true if the endpoint was successfully deleted and false otherwise
     */
    public boolean deleteEndpoint(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (log.isDebugEnabled()) {
                log.debug("Deleting endpoint : " + endpointName + " from the configuration");
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Endpoint endpoint = synapseConfiguration.getDefinedEndpoints().get(endpointName);
            endpoint.destroy();
            synapseConfiguration.removeEndpoint(endpointName);
            MediationPersistenceManager pm = getMediationPersistenceManager();
            String fileName = null;
            if (endpoint instanceof AbstractEndpoint) {
                fileName = endpoint.getFileName();
            }
            pm.deleteItem(endpointName, fileName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
            if (log.isDebugEnabled()) {
                log.debug("Endpoint : " + endpointName + " removed from the configuration");
            }
        } finally {
            lock.unlock();
        }
        return true;

    }

    public ConfigurationObject[] getDependents(String endpointName) {
        DependencyManagementService dependencyMgr = ConfigHolder.getInstance().
                getDependencyManager();
        if (dependencyMgr != null) {
            ConfigurationObject[] dependents = dependencyMgr.getDependents(
                    ConfigurationObject.TYPE_ENDPOINT, endpointName);
            if (dependents != null && dependents.length > 0) {
                List<ConfigurationObject> deps = new ArrayList<ConfigurationObject>();
                for (ConfigurationObject o : dependents) {
                    if (o.getType() != ConfigurationObject.TYPE_UNKNOWN) {
                        deps.add(o);
                    }
                }

                if (deps.size() > 0) {
                    return deps.toArray(new ConfigurationObject[deps.size()]);
                }
            }
        }
        return null;
    }

    public boolean deleteDynamicEndpoint(String key) throws EndpointAdminException {
        Lock lock = getLock();
        try {
            lock.lock();
            Registry registry = getSynapseConfiguration().getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleFault("The key '" + key +
                            "' cannot be found within the configuration", null);
                }
                registry.delete(key);

                if (log.isDebugEnabled()) {
                    log.debug("Deleted endpoint with key: " + key + " from the Synapse registry");
                }
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean updateDynamicEndpoint(String key, String epName) throws Exception {
        final Lock lock = getLock();
        try {
            lock.lock();
            return deleteDynamicEndpoint(key) && addDynamicEndpoint(key, epName);
        } finally {
            lock.unlock();
        }
    }

    public String getDynamicEndpoint(String key) throws Exception {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleFault("No resource is available by the key '" + key + "'", null);
                }
            } else {
                handleFault("Unable to access the registry instance for the ESB", null);
            }
            OMElement e = null;
            if (registry != null) {
                e = (OMElement) registry.getResource(new Entry(key), synConfig.getProperties());
            }
            if (e != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found dynamic endpoint " + key);
                }
                return e.toString();
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String[] getDynamicEndpoints(int pageNumber, int endpointsPerPage) throws Exception {
        org.wso2.carbon.registry.core.Registry registry;
        final Lock lock = getLock();
        try {
            lock.lock();
            String[] configInfo = getMimeTypeResult(getConfigSystemRegistry());
            String[] govInfo = getMimeTypeResult(getGovernanceRegistry());
            String[] info = new String[configInfo.length + govInfo.length];

            int ptr = 0;
            for (String aConfigInfo : configInfo) {
                info[ptr] = "conf:" + aConfigInfo;
                ++ptr;
            }
            for (String aGovInfo : govInfo) {
                info[ptr] = "gov:" + aGovInfo;
                ++ptr;
            }
            Arrays.sort(info);
            if (log.isDebugEnabled()) {
                log.debug("Found " + info.length + " dynamic endpoints");
            }

            String[] ret;
            if (info.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                ret = new String[endpointsPerPage];
            } else {
                ret = new String[info.length - (endpointsPerPage * pageNumber)];
            }
            for (int i = 0; i < endpointsPerPage; ++i) {
                if (ret.length > i)
                    ret[i] = info[endpointsPerPage * pageNumber + i];
            }
            return ret;
        } catch (RegistryException e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    public int getDynamicEndpointCount() throws EndpointAdminException {
        org.wso2.carbon.registry.core.Registry registry;
        try {
            String[] govList = getMimeTypeResult(getGovernanceRegistry());
            String[] confList = getMimeTypeResult(getConfigSystemRegistry());
            return confList.length + govList.length;
        } catch (Exception e) {
            handleFault("Error while retrieving dynamic endpoint count", e);
        }
        return 0;
    }
    @SuppressWarnings({"unchecked"})
    private String[] getMimeTypeResult(org.wso2.carbon.registry.core.Registry targetRegistry) throws EndpointAdminException, RegistryException {
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_MEDIA_TYPE = ?";
        Map parameters = new HashMap();
        parameters.put("query",sql);
        parameters.put("1", WSO2_ENDPOINT_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }

    private void persistEndpoint(Endpoint ep) throws EndpointAdminException {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        if (pm == null){
            handleFault("Cannot Persist endpoint because persistence manager is null, " +
                    "probably persistence is disabled", null);
        }
        pm.saveItem(ep.getName(), ServiceBusConstants.ITEM_TYPE_ENDPOINT);
    }

    private void assertNameNotEmpty(String endpointName) throws EndpointAdminException {
        if (endpointName == null || "".equals(endpointName.trim())) {
            handleFault("Invalid name : Name is empty.", null);
        }
    }

    private void handleFault(String message, Exception e) throws EndpointAdminException {
        if (e != null) {
            log.error(message, e);
            throw new EndpointAdminException(e.getMessage(), e);
        } else {
            log.error(message);
            throw new EndpointAdminException(message);
        }
    }

    private String buildPropertyString(AbstractEndpoint ep) {

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
