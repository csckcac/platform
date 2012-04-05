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
package org.wso2.carbon.rulecep.service;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.InputManager;
import org.wso2.carbon.rulecep.adapters.OutputManager;
import org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter;
import org.wso2.carbon.rulecep.adapters.impl.POJOResourceAdapter;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.commons.descriptions.*;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Creates Rule Services based on the rule deployment file information
 */
public class ServiceBuilder {

    private static Log log = LogFactory.getLog(ServiceBuilder.class);

    private InputOutputAdaptersService inputOutputAdaptersService;
    private final ArchiveReader archiveReader = new ArchiveReader();
    private AxisConfiguration axisConfig;
    private ConfigurationContext configurationContext;
    private static final Axis2MessageInterceptor INTERCEPTOR = new Axis2MessageInterceptor();
    private static final XPathFactory AXIOM_XPATH_FACTORY = new AXIOMXPathFactory();
    private static final XPathSerializer AXIOM_XPATH_SERIALIZER = new AXIOMXPathSerializer();
    private static final OMElementHelper OM_ELEMENT_HELPER = OMElementHelper.getInstance();
    private final ServiceDeployerInformation deployerInformation;
    private final ResourceLoader resourceLoader;

    public ServiceBuilder(ConfigurationContext configurationContext,
                          ServiceDeployerInformation deployerInformation) {
        this.deployerInformation = deployerInformation;
        this.configurationContext = configurationContext;
        this.axisConfig = configurationContext.getAxisConfiguration();
        ServiceManger manger = ServiceManger.getInstance();
        this.inputOutputAdaptersService = manger.getInputOutputAdaptersService();
        this.resourceLoader = new ResourceLoader(manger.getRegistryService());
    }

    /**
     * @param serviceGroup       <code>AxisServiceGroup</code> instance of the services to be built
     * @param deploymentFileData Information about rule archive or rule service file
     * @return A list of services built from the rule service archive or rule service file
     * @throws AxisFault for any errors during service build operation
     */
    public List<AxisService> build(AxisServiceGroup serviceGroup,
                                   DeploymentFileData deploymentFileData) throws AxisFault {
        // load services from WSDL
        HashMap<String, AxisService> wsdlservice =
                archiveReader.processWSDLs(deploymentFileData);
        if (wsdlservice != null && wsdlservice.size() > 0) {
            for (AxisService service : wsdlservice.values()) {
                if (service == null) {
                    continue;
                }
                Iterator operations = service.getOperations();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = (AxisOperation) operations.next();
                    axisConfig.getPhasesInfo().setOperationPhases(axisOperation);
                }
            }
        }

        // create rule services from the .rsl file
        InputStream inputStream = null;
        ArrayList<AxisService> serviceList = null;
        String ruleServiceFileName = deploymentFileData.getName();  //TODO validate
        if (ruleServiceFileName.endsWith(deployerInformation.getFileExtension())) {
            try {
                inputStream = new FileInputStream(deploymentFileData.getFile());
            } catch (FileNotFoundException e) {
                handleException("The rule service file can not be found. The file name was :" +
                        ruleServiceFileName);
            }
            if (inputStream == null) {
                handleException("Cannot get a input stream from the file :" +
                        ruleServiceFileName);
            }
        } else if (ruleServiceFileName.endsWith(deployerInformation.getArchiveExtension())) {
            String serviceName =
                    ruleServiceFileName.substring(0, ruleServiceFileName.lastIndexOf("."));
            String rslFile = serviceName + "." + deployerInformation.getFileExtension();
            inputStream = resourceLoader.loadResourceFromLocal(rslFile,
                    deploymentFileData.getClassLoader());
            if (inputStream == null) {     //TODO read a service xml parameter
                handleException("Cannot find the .rsl file from the rule service archive. " +
                        "The file should be " + rslFile);
            }
            if (containsServiceXML(deploymentFileData.getAbsolutePath())) {
                serviceList = archiveReader.processServiceGroup(
                        deploymentFileData.getAbsolutePath(), deploymentFileData,
                        serviceGroup, deploymentFileData.getFile().isDirectory(), wsdlservice,
                        configurationContext);
            }
        } else {
            handleException("Invalid file type! Either .rsl or .aar file" +
                    " should be as the rule service. File was : " + ruleServiceFileName);
        }

        ServiceDescription ruleServiceDescription = createRuleServiceDescription(inputStream);

        // can get the operaton extension points here.

        AxisService ruleService = createAxisService(ruleServiceDescription,
                serviceGroup, deploymentFileData, wsdlservice);
        if (ruleService == null) {
            handleException(" Can not create rule services form the file : " +
                    deploymentFileData.getName());
        }
        // process the service XML
        List<AxisService> servicesToBeUsed = new ArrayList<AxisService>();
        if (serviceList != null) {
            servicesToBeUsed.addAll(serviceList);
        } else {
            servicesToBeUsed.add(ruleService);
        }

        for (AxisService axisService : servicesToBeUsed) {
            if (axisService == null) {
                continue;
            }
            String name = axisService.getName();
            assert ruleService != null;
            if (!ruleService.getName().equals(name)) {
                continue;
            }
            finalizeBuild(axisService, ruleServiceDescription, deploymentFileData);
        }
        return servicesToBeUsed;
    }


    /**
     * //TODO - remove
     * Prepare the inputs as a composite one input.This is because , in a rule service ,
     * inputs are similar to teh argument
     *
     * @param resourceDescriptions A list of inputs
     * @param classLoader          Service class loader
     * @return A prepared composite input
     */
    private List<ResourceDescription> prepareInputs(List<ResourceDescription> resourceDescriptions,
                                                    ClassLoader classLoader) {
        if (resourceDescriptions.isEmpty() || resourceDescriptions.size() == 1) {
            return resourceDescriptions;
        }
        ResourceDescription composite = new ResourceDescription();
        for (ResourceDescription description : resourceDescriptions) {
            if (inputOutputAdaptersService.getFactAdapterFactory().containsInputAdapter(
                    description.getType()) || description.getExpression() != null) {
                return resourceDescriptions;
            } else {
                composite.addChildResource(description);
            }
        }
        composite.setResourceClassLoader(classLoader);
        composite.setType(POJOResourceAdapter.TYPE);
        List<ResourceDescription> descriptions = new ArrayList<ResourceDescription>();
        descriptions.add(composite);
        return descriptions;
    }

    /**
     * Creates a RuleServiceDescription instance from the .rsl file
     *
     * @param inputStream Stream of the .rsl file
     * @return a <code>RuleServiceDescription</code> instance
     */
    private ServiceDescription createRuleServiceDescription(InputStream inputStream) {
        try {
            OMElement omElement = OM_ELEMENT_HELPER.toOM(inputStream);
            omElement.detach();
            return ServiceDescriptionFactory.create(omElement,
                    AXIOM_XPATH_FACTORY, deployerInformation.getExtensionBuilder());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * @param serviceDescription Information about the rule service
     * @param axisServiceGroup   service group for the service to be built
     * @param deploymentFileData Information about the rule service artifact
     * @param wsdlServices       A list of services loaded from the WSDL if there is a WSDL
     * @return An <code>AxisService </code> representing the rule service
     * @throws DeploymentException for any error during service creation
     */
    private AxisService createAxisService(ServiceDescription serviceDescription,
                                          AxisServiceGroup axisServiceGroup,
                                          DeploymentFileData deploymentFileData,
                                          Map<String, AxisService> wsdlServices) throws DeploymentException {

        final Map<String, AxisService> serviceMap = new HashMap<String, AxisService>();
        AxisService axisService = null;
        OMElement serviceXML =
                ServiceDescriptionSerializer.serializeToServiceXML(serviceDescription, null,
                        AXIOM_XPATH_SERIALIZER);
        String serviceName = DescriptionBuilder.getShortFileName(deploymentFileData.getName());
        if (serviceName != null) {
            axisService = wsdlServices.get(serviceName);
        }
        if (axisService == null) {
            axisService = wsdlServices.get(
                    DescriptionBuilder.getShortFileName(deploymentFileData.getName()));
        }
        if (axisService == null) {
            axisService = new AxisService(serviceName);
        } else {
            axisService.setWsdlFound(true);
            axisService.setCustomWsdl(true);
        }

        axisService.setParent(axisServiceGroup);
        axisService.setClassLoader(deploymentFileData.getClassLoader());

        org.apache.axis2.deployment.ServiceBuilder serviceBuilder =
                new org.apache.axis2.deployment.ServiceBuilder(configurationContext, axisService);
        serviceBuilder.setWsdlServiceMap((HashMap<String, AxisService>) wsdlServices);
        AxisService service = serviceBuilder.populateService(serviceXML);
        serviceMap.put(service.getName(), service);
        return service;
    }

    /**
     * The final step of the rule service build
     * Create rule engine , wsdl and message receivers
     *
     * @param axisService            <code>AxisService</code> being built
     * @param ruleServiceDescription Information about the rule service
     * @param deploymentFileData     Information about the rule service artifact
     * @throws AxisFault for any error during this operation
     */
    private void finalizeBuild(AxisService axisService, ServiceDescription ruleServiceDescription,
                               DeploymentFileData deploymentFileData) throws AxisFault {

        axisService.addParameter(new Parameter(ServiceConstants.SERVICE_TYPE,
                deployerInformation.getServiceType()));
        axisService.addParameter(new Parameter(deployerInformation.getServicePathKey(),
                deploymentFileData.getFile().getAbsolutePath()));
        if (containsServiceXML(deploymentFileData.getAbsolutePath())) {
            axisService.addParameter(new Parameter(deployerInformation.getServiceArchiveGeneratableKey(), true));
        } else {
            axisService.addParameter(new Parameter(deployerInformation.getServiceArchiveGeneratableKey(), false));
        }
        axisService.setTargetNamespace(ruleServiceDescription.getTargetNamespace());
        axisService.setTargetNamespacePrefix(ruleServiceDescription.getTargetNSPrefix());

        ServiceEngineFactory serviceEngineFactory = deployerInformation.getServiceProvider();
        ServiceEngine serviceEngine =
                serviceEngineFactory.createServiceEngine(ruleServiceDescription, axisService, resourceLoader);
        // Start WSDL generation
        ServiceWSDLBuilder ruleServiceWSDLBuilder = new ServiceWSDLBuilder(axisService,
                ruleServiceDescription);
        ruleServiceWSDLBuilder.startBuild();
        Iterator<OperationDescription> iterator =
                ruleServiceDescription.getOperationDescriptions();
        MessageReceiverFactory messageReceiverFactory =
                deployerInformation.getMessageReceiverFactory();
        while (iterator.hasNext()) {
            OperationDescription opDes = iterator.next();
            AxisOperation axisOperation = axisService.getOperation(opDes.getName());
            // creates a rule service message receiver
            List<ResourceDescription> inputs = opDes.getFactDescriptions();
            List<ResourceDescription> outputs = opDes.getResultDescriptions();

            // if the operation is in-out but there is no result
            if (!(axisOperation instanceof InOnlyAxisOperation)) {

                if (outputs.isEmpty()) {

                    // this is because opDes.getResultDescriptions does not return a view
                    // which is wrong and should fix properly in the next release TODO
                    outputs = new ArrayList<ResourceDescription>();

                    if (log.isDebugEnabled()) {
                        log.debug("There is no results for the operation " +
                                axisOperation.getName() + ". It is not an in-only operation.");
                    }

                    ResourceDescription description = new ResourceDescription();
                    description.setType(OMElementResourceAdapter.TYPE);
                    description.setName(ServiceConstants.DEFAULT_WRAPPER_NAME);
                    outputs.add(description);
                }
            }

            // create input and output managers
            InputManager inputManager =
                    inputOutputAdaptersService.createInputManager(
                            prepareInputs(inputs, axisService.getClassLoader()),
                            INTERCEPTOR);
            OutputManager outputManager =
                    inputOutputAdaptersService.createOutputManager(outputs, INTERCEPTOR);


            // build the in-message
            ruleServiceWSDLBuilder.buildInMessage(axisOperation, inputs,
                    inputOutputAdaptersService.getFactAdapterFactory());

            //selectively create the message receiver
            MessageReceiver messageReceiver;
            if (axisOperation instanceof InOnlyAxisOperation) {
                messageReceiver = messageReceiverFactory.createInOnlyMessageReceiver(serviceEngine,
                        inputManager, opDes);
            } else {
                ruleServiceWSDLBuilder.buildOutMessage(axisOperation, outputs,
                        inputOutputAdaptersService.getResultAdapterFactory());
                messageReceiver = messageReceiverFactory.createInOutMessageReceiver(serviceEngine,
                        inputManager, outputManager, opDes);
            }

            if (serviceEngine.canListenForResult()) {
                serviceEngine.registerResultListener(outputManager, opDes);
            }

            axisOperation.setMessageReceiver(messageReceiver);
        }

        if (this.deployerInformation.getDeploymentExtenstion() != null){
            this.deployerInformation.getDeploymentExtenstion().doDeploy(axisService,
                    ruleServiceDescription);
        }

        ruleServiceWSDLBuilder.endBuild();
    }

    /**
     * Checks the existence of the service XML
     *
     * @param fileName archive file name
     * @return <code>true</code> if there is a services.xml
     * @throws DeploymentException for any error during operation
     */
    private boolean containsServiceXML(String fileName) throws DeploymentException {
        ZipInputStream zin = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
            zin = new ZipInputStream(fin);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase(DeploymentConstants.SERVICES_XML)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            handleException("Cannot find the file : " + fileName, e);
        } catch (IOException e) {
            handleException("IO error reading the file : " + fileName, e);
        } finally {
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException ignored) {

                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }


    private void handleException(String msg) throws DeploymentException {
        log.error(msg);
        throw new DeploymentException(msg);
    }

    private void handleException(String msg, Throwable e) throws DeploymentException {
        log.error(msg, e);
        throw new DeploymentException(msg, e);
    }
}
