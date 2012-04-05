/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.extension;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.epr.WSDL11Endpoint;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.BpelRuntimeContext;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.bpel.b4p.internal.B4PServiceComponent;
import org.wso2.carbon.bpel.b4p.utils.SOAPHelper;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.AxisServiceUtils;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.List;

public class PeopleActivity {
    protected final Log log = LogFactory.getLog(PeopleActivity.class);

    private String name;
    private String inputVarName;
    private String outputVarName;
    private boolean isSkipable = false;

    private String partnerLinkName;
    private String operation;
    private String callbackOperationName;

    private String serviceURI;
    private String servicePort;
    private String callbackServicePort;

    private InteractionType activityType;

    private QName processId;

    private boolean isRPC = false;
    private boolean isTwoWay = true;

    private static final long serialVersionUID = -89894857418738012L;

    public InteractionType getActivityType() {
        return activityType;
    }

    public QName getCallbackServiceName() {
        return callbackServiceName;
    }

    public String getCallbackServicePort() {
        return callbackServicePort;
    }

    QName serviceName;
    QName callbackServiceName;
    Definition hiWSDL;

    public PeopleActivity(ExtensionContext extensionContext, Element element) throws FaultException {
        init(extensionContext, element);
    }

    public Element getInputMessage(ExtensionContext extensionContext) throws FaultException {
        Node inputNode = extensionContext.readVariable(inputVarName);

        if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) inputNode;
        } else {
            log.error("The node type of the variable is not ELEMENT");
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "Unsupported variable type"));
        }
    }

    public Operation getOperation(ExtensionContext extensionContext) {
        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();

        OProcess process = runTimeContext.getProcessModel();

        OPartnerLink partnerLink = process.getPartnerLink(partnerLinkName);

        return partnerLink.getPartnerRoleOperation(operation);
    }

    public Operation getCallbackOperation(ExtensionContext extensionContext) {
        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();

        OProcess process = runTimeContext.getProcessModel();

        OPartnerLink partnerLink = process.getPartnerLink(partnerLinkName);

        return partnerLink.getMyRoleOperation(callbackOperationName);

    }

    public String getOperationName() {
        return operation;
    }

    public String getEPRURL() {
        return serviceURI;
    }

    public String getServicePort() {
        return servicePort;
    }

    public String getOutputVarName() {
        return outputVarName;
    }

//    public DeploymentUnitDir getDu() {
//        return du;
//    }

    public QName getServiceName() {
        return serviceName;
    }

    public Definition getHiWSDL() {
        return hiWSDL;
    }

//    public Definition getCallbackWSDL() {
//        return du.getDefinitionForService(callbackServiceName);
//    }

    public String getCallbackOperationName() {
        return callbackOperationName;
    }

    public String getPartnerLinkName() {
        return partnerLinkName;
    }

    private void init(ExtensionContext extensionContext, Element element) throws FaultException {
        if (!element.getLocalName().equals("peopleActivity") || !element.getNamespaceURI().equals(BPEL4PeopleConstants.B4P_NAMESPACE)) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "no peopleActivity found"));
        }

        name = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_NAME);
        inputVarName = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_INPUT_VARIABLE);
        outputVarName = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OUTPUT_VARIABLE);
        isSkipable = "yes".equalsIgnoreCase(
                element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_IS_SKIPABLE));

        NodeList taskList = element.getChildNodes();

        Node task = null;
        int elementNodeCount = 0;
        for (int i = 0; i < taskList.getLength(); i++) {
            if (taskList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elementNodeCount++;
                if (elementNodeCount > 1) {
                    break;
                }
                task = taskList.item(i);
            }
        }

        if (elementNodeCount != 1 || task == null) {
            log.error("Invalid peopleActivity definition");
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "Invalid peopleActivity definition"));
        }

        /*TODO this only checks for b4p namespace, we need to check for <htd:task>...</htd:task> and
         <htd:notification>...</htd:notification> which r in ht namespace*/
        if (!task.getNamespaceURI().equals(BPEL4PeopleConstants.B4P_NAMESPACE)) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Invalid namespace uri for the task. " +
                            "The valid namespace: " + BPEL4PeopleConstants.B4P_NAMESPACE));
        }
        //TODO is it required to read through the element per task type? cant we jst do it once? what about null elements 
        if (task.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_TASK)) {
            activityType = InteractionType.TASK;
            if (task.getNodeType() == Node.ELEMENT_NODE) {
                Element remoteTaskEle = (Element) task;
                partnerLinkName =
                        remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_PARTNER_LINK);
                operation = remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OPERATION);
                callbackOperationName = remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_RESPONSE_OPERATION);
                if (log.isDebugEnabled()) {
                    log.debug("name: " + name + " inputVarName: " + inputVarName +
                            " outPutVarName: " + outputVarName + " isSkipable: " +
                            isSkipable + " partnerLinkName: " + partnerLinkName + " operation: " +
                            operation + " responseOperation: " + callbackOperationName);
                }
            } //TODO what if NODE type is not ELEMENT_NODE
        } else if (task.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_NOTIFICATION)) {
            activityType = InteractionType.NOTIFICATION;
            if (task.getNodeType() == Node.ELEMENT_NODE) {
                Element remoteTaskEle = (Element) task;
                partnerLinkName = remoteTaskEle.
                        getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_PARTNER_LINK);
                operation = remoteTaskEle.
                        getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OPERATION);
                if (log.isDebugEnabled()) {
                    log.debug("name: " + name + " inputVarName: " + inputVarName + " partnerLinkName: " +
                            partnerLinkName + " operation: " + operation);
                }
            } //TODO what if NODE type is not ELEMENT_NODE
        } else if (task.getLocalName().
                equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_NOTIFICATION)) {
            activityType = InteractionType.NOTIFICATION;
            log.warn(task.getLocalName() + " is not supported yet!");
            throw new RuntimeException(task.getLocalName() + " is not supported yet!");
        } else if (task.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_TASK)) {
            activityType = InteractionType.TASK;
            log.warn(task.getLocalName() + " is not supported yet!");
            throw new RuntimeException(task.getLocalName() + " is not supported yet!");
        }

        DeploymentUnitDir du = new DeploymentUnitDir(new File(extensionContext.getDUDir()));
        processId = new QName(extensionContext.getProcessModel().getQName().getNamespaceURI(),
                extensionContext.getProcessModel().getQName().getLocalPart() + "-" +
                        du.getStaticVersion());

        isTwoWay = activityType.equals(InteractionType.TASK);

        deriveServiceEPR(du, extensionContext);
    }

    private void deriveServiceEPR(DeploymentUnitDir du, ExtensionContext extensionContext) throws FaultException {
        DeployDocument deployDocument = du.getDeploymentDescriptor();
        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();

        //TODO neeed to extend ExtentionContext
        OProcess oProcess = runTimeContext.getProcessModel();

        TDeployment.Process hiProcess = null;
        List<TDeployment.Process> processList = deployDocument.getDeploy().getProcessList();
        for (TDeployment.Process process : processList) {
            if (process.getName().equals(oProcess.getQName())) {
                hiProcess = process;
                break;
            }
        }

        if (hiProcess == null) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "related process not found"));
        }

        List<TInvoke> tInvokeList = hiProcess.getInvokeList();
        for (TInvoke tInvoke : tInvokeList) {
            if (tInvoke.getPartnerLink().equals(partnerLinkName)) {
                serviceName = tInvoke.getService().getName();
                servicePort = tInvoke.getService().getPort();
                break;
            }
        }

        if (serviceName == null || servicePort == null) {
            log.error("service and port for human interaction is not found in the deploy.xml");
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "service and port for human interaction is not found in the deploy.xml"));
        }

        //get the callback information for the TASK
        if (activityType.equals(InteractionType.TASK)) {
            List<TProvide> tProvideList = hiProcess.getProvideList();
            for (TProvide tProvide : tProvideList) {
                if (tProvide.getPartnerLink().equals(partnerLinkName)) {
                    callbackServiceName = tProvide.getService().getName();
                    callbackServicePort = tProvide.getService().getPort();
                    break;
                }
            }
            if (callbackServiceName == null || callbackServicePort == null) {
                throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                        "service and port for human task callback is not found in the deploy.xml"));
            }
        }

        hiWSDL = du.getDefinitionForService(serviceName);

        Service service = hiWSDL.getService(serviceName);
        Port port = service.getPort(servicePort);
        List extList = port.getExtensibilityElements();
        for (Object extEle : extList) {
            if (extEle instanceof SOAPAddressImpl) {
                SOAPAddressImpl soapAddress = (SOAPAddressImpl) extEle;
                serviceURI = soapAddress.getLocationURI();
                break;
            }
        }

        if (serviceURI == null) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "Service URI is not available"));
        }
    }

    public Binding getBinding() throws FaultException {
        Service serviceDef = hiWSDL.getService(serviceName);
        if (serviceDef == null) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Service definition is not available for service " + serviceName));
        }
        Port port = serviceDef.getPort(getServicePort());
        if (port == null) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Service port is not available for service " + serviceName + " and port " +
                            getServicePort()));
        }

        Binding binding = port.getBinding();

        if (binding == null) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Service binding is not available for service " + serviceName + " and port " +
                            getServicePort()));
        }

        return binding;
    }

    public SOAPFactory getSoapFactory() throws FaultException {
        Binding binding = getBinding();
        ExtensibilityElement bindingType = SOAPHelper.getBindingExtension(binding);

        if (!(bindingType instanceof SOAPBinding || bindingType instanceof SOAP12Binding ||
                bindingType instanceof HTTPBinding)) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Service binding is not supported for service " + serviceName + " and port " +
                            getServicePort()));
        }

        if (bindingType instanceof SOAPBinding) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            return OMAbstractFactory.getSOAP12Factory();
        }
    }

    public UnifiedEndpoint getUnifiedEndpoint() throws FaultException {
        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().
                getTenantId(processId);
        ProcessConfigurationImpl processConf = (ProcessConfigurationImpl) B4PServiceComponent.
                getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId).
                getProcessConfiguration(processId);
        EndpointConfiguration epConf = processConf.
                getEndpointConfiguration(new WSDL11Endpoint(serviceName, servicePort));
        try {
            return epConf.getUnifiedEndpoint();
        } catch (AxisFault axisFault) {
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Error occurred while reading UnifiedEndpoint for service " + serviceName),
                    axisFault);
        }
    }

    public ConfigurationContext getConfigurationContext() throws FaultException {
        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().
                getTenantId(processId);
        ProcessConfigurationImpl processConf = (ProcessConfigurationImpl) B4PServiceComponent.
                getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId).
                getProcessConfiguration(processId);
        return processConf.getTenantConfigurationContext();
    }

//    public WSDLAwareMessage getWSDLAwareMessage() throws FaultException {
//        Element request = getInputMessage();
//        WSDLAwareMessage message = new WSDLAwareMessage();
//        message.setBinding(getBinding());
//        message.setPortName(servicePort);
//        message.setRPC(isRPC);
//        message.setServiceName(serviceName.getLocalPart());
//        message.addBodyPart();
//    }

    public String invoke(ExtensionContext extensionContext) throws FaultException {
        BPELMessageContext taskMessageContext = new BPELMessageContext(hiWSDL);
        try {
            SOAPHelper soapHelper = new SOAPHelper(hiWSDL, getBinding(),
                    getServiceName().getLocalPart(), getServicePort(), getSoapFactory(), isRPC);
            MessageContext messageContext = new MessageContext();
            soapHelper.createSoapRequest(messageContext,
                    (Element) extensionContext.readVariable(inputVarName), getOperation(extensionContext));
            taskMessageContext.setOperationName(getOperationName());
            taskMessageContext.setInMessageContext(messageContext);
            taskMessageContext.setPort(getServicePort());
            taskMessageContext.setService(getServiceName());
            taskMessageContext.setRPCStyleOperation(isRPC);
            taskMessageContext.setTwoWay(isTwoWay);
            taskMessageContext.setSoapFactoryForCurrentMessageFlow(getSoapFactory());
            taskMessageContext.setWsdlBindingForCurrentMessageFlow(getBinding());
            taskMessageContext.setUep(getUnifiedEndpoint());
            taskMessageContext.setCaller(processId.getLocalPart());
            AxisServiceUtils.invokeService(taskMessageContext, getConfigurationContext());
        } catch (AxisFault axisFault) {
            log.error(axisFault);
            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
                    "Error occurred while invoking service " + serviceName),
                    axisFault);
        }
// it seems the WSDLAwareMessage is not required.
//        taskMessageContext.setRequestMessage();

        return SOAPHelper.parseResponseFeedback(
                taskMessageContext.getOutMessageContext().getEnvelope().getBody());
    }

    public String inferCorrelatorId(ExtensionContext extensionContext) throws FaultException {
        PartnerLinkInstance plink;
        plink = extensionContext.getPartnerLinkInstance(partnerLinkName);
        return plink.partnerLink.getName() + "." + callbackOperationName;
    }
}
