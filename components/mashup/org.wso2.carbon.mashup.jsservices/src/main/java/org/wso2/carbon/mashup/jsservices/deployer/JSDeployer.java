/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mashup.jsservices.deployer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.mashup.javascript.hostobjects.system.MSTaskAdmin;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngine;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngineUtils;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptReceiver;
import org.wso2.carbon.mashup.jsservices.JSConstants;
import org.wso2.carbon.mashup.jsservices.JSUtils;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This is a custom Axis2 deployer written for deploying JavaScript services.i.e. This deployer will
 * deploy {@code *.js} files as Axis2 services. {@code org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptReceiver}
 * is used as the MessageReceiver for the services deployed using this deployer. <br/><br/> Files
 * only within {@code repository/jsservices/x} itself are considered as JavaScript services and
 * deployed as services where {@code x} above could be any directory. If the mashup was created
 * using web console, then {@code x} will be username.Files which reside within sub directories of
 * the above directory, are just ignored by the deployer as it allows to keep additional resources
 * such as {@code *.js, *.html} etc. within a {@code foo.resources} folder along with the {@code
 * foo.js} mashup.<br/><br/> This deployer supports the <a href="http://www.wso2.org/wiki/display/mashup/Javascript+Web+Service+Annotations">JavaScript
 * Web Service Annotations</a> and the <a href="http://www.wso2.org/wiki/display/mashup/scripts+folder+structure+and+deployment">
 * deployment folder structure</a>.
 */
public class JSDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(JSDeployer.class);

    private AxisBinding soap11Binding;
    private AxisBinding soap12Binding;
    private AxisBinding httpBinding;

    /*
    This is the map that keeps the httpLocation to axisoperation mapping to be used by the
    httpLocationBasedDispatcher
    */
    Map<String, AxisOperation> httpLocationTable;

    // Used by the service name validator to generate the error message on an invalid name
    private final String SERVICE_NAME = "ServiceName";

    // Used by the operation name validator to generate the error message on an invalid name
    private final String OPERATION_NAME = "OperationName";

    private final String HTTP_TRANSPORT = "http";
    private final String HTTPS_TRANSPORT = "https";

    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;

    private String repoDir = null;
    private String extension = null;
    private int tenantId;

    /**
     * Initializes the deployer.
     *
     * @param configCtx The {@code ConfigurationContext} for {@code JSDeployer}. In case of
     *                  Multi-Tenant deployment, this will be the tenant specific {@code
     *                  ConfigurationContext} and will be the main {@code ConfigurationContext} in a
     *                  standalone deployment
     * @see org.apache.axis2.deployment.Deployer#init(org.apache.axis2.context.ConfigurationContext)
     */
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = this.configCtx.getAxisConfiguration();
    }

    /**
     * Process and deploy a given *.js file as an Axis2 service.
     *
     * @param deploymentFileData the DeploymentFileData object to deploy. i.e. it is the {@code
     *                           *.js} file which is gonna deployed
     * @throws DeploymentException if there is a problem
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        RhinoEngine.enterContext();

        String jsFilePath = deploymentFileData.getAbsolutePath();
        /*
        Due to hierarchical services deployment of axis2, we need to check for the location
        of the file being deployed. Only the JS files within the js services/foo directory
        will be deployed by the Mashup Server while others are skipped by the deployer
        */
        String jsRepository = axisConfig.getRepository().getPath();
        String pathSeparator = "/";
        if(!jsRepository.endsWith(pathSeparator)) {
            jsRepository += pathSeparator;
        }
        jsRepository += repoDir;
        // path of the *.js file relative to the repository i.e. foo/bar.js
        String jsPathRelative = getJSPathRelative(jsFilePath, jsRepository);

        StringTokenizer tokenizer = new StringTokenizer(jsPathRelative, File.separator);

        if (tokenizer.countTokens() < 2) {
            throw new DeploymentException("*.js files cannot be place at " + jsPathRelative +
                    " and should be placed as jsservices/foo/bar.js");
        } else if (tokenizer.countTokens() > 2) {
            return;
        }

        String username = tokenizer.nextToken();

        //state variable kept to check if the service was successfully deployed at the end
        boolean successfullyDeployed = false;
        String serviceStatus = null;
        String jsFileName = deploymentFileData.getName();
        try {
            //Set tenant flow with the relevant tenant id
            tenantId = SuperTenantCarbonContext.getCurrentContext(this.configCtx).getTenantId();
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            deploymentFileData.setClassLoader(axisConfig.getServiceClassLoader());
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());

            ArrayList serviceList =
                    processService(deploymentFileData, serviceGroup, configCtx, username);

            DeploymentEngine.addServiceGroup(serviceGroup, serviceList,
                    deploymentFileData.getFile().toURI().toURL(), deploymentFileData, axisConfig);

            log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS, jsFileName,
                    deploymentFileData.getFile().toURI().toURL().toString()));

            super.deploy(deploymentFileData);
            successfullyDeployed = true;
        } catch (DeploymentException deploymentException) {
            log.error("The service " + jsFileName + " is not valid.", deploymentException);
            serviceStatus = "Error:\n" + deploymentException.getMessage();

            throw deploymentException;
        } catch (Throwable t) {
            /*
            Even though catching Throwable is not recommended, we do not want
            the server to fail on errors like NoClassDefFoundError
            */
            log.error("The service " + jsFileName + " is not valid.", t);
            serviceStatus = "Error:\n" + t.getMessage();

            throw new DeploymentException(t);
        } finally {
            /*
            Now if the service was not deployed successfully we need to update the registry saying
            that this service was faulty
            */
            if (!successfullyDeployed) {
                axisConfig.getFaultyServices().put(jsFilePath, serviceStatus);
                try {
                    CarbonUtils.registerFaultyService(jsFilePath, MashupConstants.JS_SERVICE,
                            configCtx);
                } catch (AxisFault e) {
                    log.error("Cannot register faulty service with Carbon: " + jsFileName, e);
                }

            }
            //End tenant flow
            SuperTenantCarbonContext.endTenantFlow();
            RhinoEngine.exitContext();
        }
    }

    private String getJSPathRelative(String jsFilePath, String jsRepository) {
        return jsFilePath.substring(jsFilePath.indexOf(jsRepository) + jsRepository.length() + 1);
    }

    /**
     * This gets called when undeploying a java script web service deployed using this deployer.
     * This gets called even when hotUpdating a service as Axis2 updeploy and redeploys the service
     * when performing an hotUpdate.
     *
     * @param jsFilePath name of item to remove
     * @throws DeploymentException if there is a problem
     * @see org.apache.axis2.deployment.Deployer#undeploy(java.lang.String)
     */
    public void undeploy(String jsFilePath) throws DeploymentException {
        try {

            File jsFile = new File(jsFilePath);
            String jsFileName = jsFile.getName();
            String jsFileNameShort = DescriptionBuilder.getShortFileName(jsFileName);

            /*
            Due to hierarchical services deployment of axis2, we need to check for the location
            of the file being undeployed. Only the JS files within the jsservices/foo directory
            will be undeployed by the Mashup Server while others are skipped by the undeployer
            */
            String jsRepository = axisConfig.getRepository().getPath() + repoDir;
            String repoServicePath = getJSPathRelative(jsFilePath, jsRepository);

            StringTokenizer tokenizer = new StringTokenizer(repoServicePath, File.separator);

            if (tokenizer.countTokens() != 2) {
                return;
            }

            /*
            As the Mashup Server deployment model is that mashups lie in a folder with service name under each
            users username, we can safely infer the author of a mashups from the name of the parent
            folder
            */
            String username = tokenizer.nextToken();

            //Set tenant flow with the relevant tenant id
            tenantId = SuperTenantCarbonContext.getCurrentContext(this.configCtx).getTenantId();
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            String serviceGroupName = username + MashupConstants.SEPARATOR_CHAR + jsFileNameShort;
            AxisServiceGroup group = axisConfig.getServiceGroup(serviceGroupName);
            AxisService service;
            if (group != null) {
                Iterator iterator = group.getServices();
                service = (AxisService) iterator.next();
            } else {
                // May be a faulty service
                axisConfig.removeFaultyService(jsFilePath);
                return;
            }
            if (service != null) {
                // Unscheduling all the functions scheduled by this service
                
                MSTaskAdmin taskAdmin = new MSTaskAdmin();
                try {
        			taskAdmin.deleteTask(service.getName());
        		} catch (AxisFault e) {
        			log.error("Unable to delete job : " + e.getFaultAction());
        		}

                /*
                If a mashup had specified a function to be called on undeployment
                (Service LifeCycle support) we need to call it on service undeployment.
                The deployer adds a parameter to the axisService specifying which function to
                call on undeployment if such a function was specified.
                */
                Function destroy =
                        (Function) service.getParameterValue(JSConstants.MASHUP_DESTROY_FUNCTION);
                if (destroy != null) {
                    JavaScriptEngine engine = new JavaScriptEngine(jsFileName);
                    ScriptableObject scope = JavaScriptEngineUtils.getActiveScope();
                    Context cx = RhinoEngine.enterContext();
                    destroy.call(cx, scope, scope, new Object[0]);
                    RhinoEngine.exitContext();
                }
            }

            /*
            There exist only one service group for the JavaScript services deployed from this
            deployer
            */

            axisConfig.removeServiceGroup(serviceGroupName);
            configCtx.removeServiceGroupContext(group);
            super.undeploy(jsFilePath);
            log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED, serviceGroupName));

        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    /**
     * Set the directory
     *
     * @param directory directory name
     */
    public void setDirectory(String directory) {
        this.repoDir = directory;
    }

    /**
     * Set the extension to look for
     *
     * @param extension the file extension associated with this Deployer
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Creates and populates an AxisService
     *
     * @param deploymentFileData A Handle to the js file
     * @param axisServiceGroup   The AxisServiceGroup That the created service should be added to
     * @param configCtx          The Axis2 Configuration Context
     * @param username           username or the service author
     * @return An arrylist of AxisServices
     * @throws DeploymentException Thrown in case an Deployment Exception occurs
     */
    private ArrayList processService(DeploymentFileData deploymentFileData,
                                     AxisServiceGroup axisServiceGroup,
                                     ConfigurationContext configCtx, String username)
            throws DeploymentException {
        try {
            // we get the filename without the extension
            String jsFileName = deploymentFileData.getName();
            String jsFileNameShort = DescriptionBuilder.getShortFileName(jsFileName);

            AxisService axisService = new AxisService();
            axisService.setLastUpdate();

            /*
            org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptReceiver needs this to
            load the javascript file in order to execute the relevant JavaScript function
            */
            File jsFile = deploymentFileData.getFile();

            File resourcesDir = createDefaultFolders(jsFileNameShort, jsFile);

            Parameter serviceJSParameter =
                    new Parameter(MashupConstants.SERVICE_JS, jsFile.getAbsolutePath());
            axisService.addParameter(serviceJSParameter);

            //At deployment time we are puting the javascript file to the memory and later served from memory
            Parameter servicJSStreamParameter =
                    new Parameter(MashupConstants.SERVICE_JS_STREAM, fileInputStreamToString(jsFile.getAbsolutePath()));
            axisService.addParameter(servicJSStreamParameter);

            // service-mgt UI uses this information to differentiate services when listing them.
            Parameter serviceTypeParameter =
                    new Parameter(JSConstants.AXIS2_SERVICE_TYPE, MashupConstants.JS_SERVICE);
            axisService.addParameter(serviceTypeParameter);

            // Add a reference to the resources folder, as this is quite useful in the runtime
            Parameter resourceFolderParameter =
                    new Parameter(MashupConstants.RESOURCES_FOLDER, resourcesDir);
            axisService.addParameter(resourceFolderParameter);

            /*
            Add a reference to the mashup created user, as this is quite useful of getting user-specific
            registry for Registry hostobject
            */
            Parameter mashupAuthorParameter =
                    new Parameter(MashupConstants.MASHUP_AUTHOR, username);
            axisService.addParameter(mashupAuthorParameter);

            /*
             Service level java script annotations processing. We create a
             JavaScriptEngine and load the scripts and the associated host
             objects.
             */
            JavaScriptEngine engine = new JavaScriptEngine(jsFileNameShort);
            /*
             We inject the AxisService & ConfigContext as a workaround for not
             having the MessageContext injected in the deployment time. Some host objects need
             data from them at the initialize time.
             */
            ScriptableObject serviceScope = JavaScriptEngineUtils.getEngine().getRuntimeScope();
            JavaScriptEngineUtils.setActiveScope(serviceScope);
            RhinoEngine.putContextProperty(MashupConstants.AXIS2_SERVICE, axisService);
            RhinoEngine.putContextProperty(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configCtx);

            JavaScriptEngineUtils.initialize();
            // load the service java script file
            engine.evaluate(axisService);

            // Use the JavaScriptServiceAnnotationParser to extract serviceLevel annotations
            JavaScriptServiceAnnotationParser serviceAnnotationParser =
                    new JavaScriptServiceAnnotationParser(jsFileNameShort);

            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(deploymentFileData.getClassLoader());
            String serviceName = serviceAnnotationParser.getServiceName();

            // Setting Axis Parameters given in serviceParameters annotation
            for (Parameter parameter : serviceAnnotationParser.getServiceParameters()) {
                axisService.addParameter(parameter);
            }

            /*
            Checks the validity of the serviceName. If the serviceName is invalid an exception is
            thrown
            */
            JSUtils.validateName(serviceName, SERVICE_NAME);

            /*
            Although Mashup Server supports only one service per *.js file at the moment, we set
            the service group name as a combination of author name and *.js file name. If the
            mashup was created manually on the file system, then the parent directory of the
            mashup will be used as the prefix.
             */
            axisServiceGroup.setServiceGroupName(
                    username + MashupConstants.SEPARATOR_CHAR + jsFileNameShort);
            /*
            All mashup services are deployed under the authors name with the help of Axis2's
            hierarchical service deployment model. So each service name is prefixed
            by author name.
             */
            axisService.setName(username + MashupConstants.SEPARATOR_CHAR + serviceName);

            // Sets the namespace map which is defined using this.targetNamespace
            String targetNamespace = serviceAnnotationParser.getTargetNamespace();
            axisService.setTargetNamespace(targetNamespace);

            /*
            Sets the scope of the service, this is defined as a service level annotation in the mashup i.e.
            this.scope = "application | soapsession | transportsession | request"
             */
            axisService.setScope(serviceAnnotationParser.getServiceScope());
            // Sets service documentation which is defined using this.documentation annotation
            axisService.setDocumentation(serviceAnnotationParser.getServiceDocumentation());
            // Sets the namespace map which is defined using this.schemaTargetNamespace
            SchemaGenerator schemaGenerator =
                    new SchemaGenerator(serviceAnnotationParser.getSchemaTargetNamespace());
            axisService.setNamespaceMap(schemaGenerator.getNamespaceMap());

            /*
            The interfaceName is used by org.apache.axis2.description.AxisService2WSDL20 to
            set the interface during ?wsdl2
            */
            String interfaceName = serviceName + WSDL2Constants.INTERFACE_PREFIX;
            axisService.addParameter(WSDL2Constants.INTERFACE_LOCAL_NAME, interfaceName);

            /*
            Set a comparator so tha httpLocations are stored in descending order. We want the
            HTTPLocationBasedDiapatcher to make the best match hence we need them in descending
            order
            */
            httpLocationTable = new TreeMap<String, AxisOperation>(new Comparator() {
                public int compare(Object o1, Object o2) {
                    return (-1 * ((Comparable) o1).compareTo(o2));
                }
            });

            /*
            We create the AxisBinding Hierarchy in here. In the Mashup Server we take complete
            control of the Axis2 binding hierarchy cause we need to specify some HTTPBinding
            properties such as httpMethod and httpLocation
            */
            String bindingPrefix = username.replace('/', '-') + "-" + serviceName + "-";
            // Create a default SOAP 1.1 Binding
            createDefaultSOAP11Binding(bindingPrefix, interfaceName);

            // Create a default SOAP 1.2 Binding
            createDefaultSOAP12Binding(bindingPrefix, interfaceName);

            // Create a default HTTP Binding
            createDefaultHTTPBinding(bindingPrefix, interfaceName);

            /*
            We need to get all transports from the axis2 engine and create endpoints for each of
            those in here
            */
            createDefaultEndpoints(axisService);

            // Obtain the list of functions in the java script service and process each one of them.
            Object[] ids = serviceScope.getIds();
            boolean operationFound = false;
            for (Object id : ids) {
                String method = (String) id;
                Object object = serviceScope.get(method, serviceScope);
                // some id's are not functions
                if (object instanceof Function) {
                    if (!operationFound) {
                        operationFound = processOperation(engine, axisService, method, (Function) object,
                                schemaGenerator, targetNamespace);
                    } else {
                        processOperation(engine, axisService, method, (Function) object,
                                schemaGenerator, targetNamespace);
                    }
                }
            }
            if (!operationFound) {
                String msg = "No operations can be found in the mashup. You should have at least " +
                        "one visible operation : " + axisService.getName();
                log.error(msg);
                throw new DeploymentException(msg);
            }
            axisService.addSchema(schemaGenerator.getSchema());

            Function init = serviceAnnotationParser.getInit();
            Function destroy = serviceAnnotationParser.getDestroy();

            /*
            this.init and this.destroy correspond to service lifecycle functions.
            this.init is called upon service deployment and this.destroy is called on
            undeployment.
            */
            if (init != null) {
                Context cx = RhinoEngine.enterContext();
                init.call(cx, serviceScope, serviceScope, new Object[0]);
                RhinoEngine.exitContext();
            }

            if (destroy != null) {
                axisService.addParameter(JSConstants.MASHUP_DESTROY_FUNCTION, destroy);
            }

            ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
            serviceList.add(axisService);
            return serviceList;
        } catch (IOException e) {
            throw new DeploymentException(e);
        } catch (CarbonException e) {
            throw new DeploymentException(e);
        } catch (ScriptException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Creates a set of default endpoints for this service
     *
     * @param axisService The AxisService that the endpoints are created for
     */
    private void createDefaultEndpoints(AxisService axisService) {
        Map<String, TransportInDescription> transportsIn = axisConfig.getTransportsIn();
        for (TransportInDescription transportIn : transportsIn.values()) {
            /*
            Used to indicate whether a HTTPEndpoint is needed. Http endpoint is needed only
            for http and https transports
            */
            boolean needHttp = false;

            // The prefix is used to generate endpoint names
            String prefix = "";
            String transportInName = transportIn.getName();
            if (HTTP_TRANSPORT.equalsIgnoreCase(transportInName)) {
                needHttp = true;
            } else if (HTTPS_TRANSPORT.equalsIgnoreCase(transportInName)) {
                needHttp = true;
                prefix = WSDL2Constants.DEFAULT_HTTPS_PREFIX;
            } else if (transportInName != null) {
                prefix = transportInName.toUpperCase();
            }

            if (!Constants.TRANSPORT_MAIL.equalsIgnoreCase(transportInName)) {
                // Creates a default SOAP 1.1 endpoint
                AxisEndpoint soap11Endpoint = new AxisEndpoint();
                String soap11EndpointName = prefix + WSDL2Constants.DEFAULT_SOAP11_ENDPOINT_NAME;
                soap11Endpoint.setName(soap11EndpointName);
                soap11Endpoint.setBinding(soap11Binding);
                soap11Endpoint.setParent(axisService);
                soap11Endpoint.setTransportInDescription(transportInName);
                axisService.addEndpoint(soap11EndpointName, soap11Endpoint);
            }

            // Creates a default SOAP 1.2 endpoint
            AxisEndpoint soap12Endpoint = new AxisEndpoint();
            String soap12EndpointName = prefix + WSDL2Constants.DEFAULT_SOAP12_ENDPOINT_NAME;
            soap12Endpoint.setName(soap12EndpointName);
            soap12Endpoint.setBinding(soap12Binding);
            soap12Endpoint.setParent(axisService);
            soap12Endpoint.setTransportInDescription(transportInName);
            axisService.addEndpoint(soap12EndpointName, soap12Endpoint);
            axisService.setEndpointName(soap12EndpointName);

            // Creates a HTTP endpoint if its http or https transport is used
            if (needHttp) {
                AxisEndpoint httpEndpoint = new AxisEndpoint();
                String httpEndpointName = prefix + WSDL2Constants.DEFAULT_HTTP_ENDPOINT_NAME;
                httpEndpoint.setName(httpEndpointName);
                httpEndpoint.setBinding(httpBinding);
                httpEndpoint.setParent(axisService);
                httpEndpoint.setTransportInDescription(transportInName);
                axisService.addEndpoint(httpEndpointName, httpEndpoint);
                axisService.setEndpointName(httpEndpointName);
            }
        }
    }

    /**
     * Creates a AxisBinding and populates it with default HTTP properties
     *
     * @param name          The name of the service
     * @param interfaceName The interface name
     */
    private void createDefaultHTTPBinding(String name, String interfaceName) {
        httpBinding = new AxisBinding();
        httpBinding.setName(new QName(name + Java2WSDLConstants.HTTP_BINDING));
        httpBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);
        httpBinding.setProperty(WSDL2Constants.INTERFACE_LOCAL_NAME, interfaceName);
        httpBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
    }

    /**
     * Creates a AxisBinding and populates it with default SOAP 1.2 properties
     *
     * @param name          The name of the service
     * @param interfaceName The interface name
     */
    private void createDefaultSOAP12Binding(String name, String interfaceName) {
        soap12Binding = new AxisBinding();
        soap12Binding.setName(new QName(name + Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
        soap12Binding.setType(WSDL2Constants.URI_WSDL2_SOAP);
        soap12Binding.setProperty(WSDL2Constants.ATTR_WSOAP_PROTOCOL, WSDL2Constants.HTTP_PROTOCAL);
        soap12Binding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        soap12Binding.setProperty(WSDL2Constants.INTERFACE_LOCAL_NAME, interfaceName);
        soap12Binding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
    }

    /**
     * Creates a AxisBinding and populates it with default SOAP 1.1 properties
     *
     * @param name          The name of the service
     * @param interfaceName The interface name
     */
    private void createDefaultSOAP11Binding(String name, String interfaceName) {
        soap11Binding = new AxisBinding();
        soap11Binding.setName(new QName(name + Java2WSDLConstants.BINDING_NAME_SUFFIX));
        soap11Binding.setType(WSDL2Constants.URI_WSDL2_SOAP);
        soap11Binding.setProperty(WSDL2Constants.ATTR_WSOAP_PROTOCOL, WSDL2Constants.HTTP_PROTOCAL);
        soap11Binding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        soap11Binding.setProperty(WSDL2Constants.INTERFACE_LOCAL_NAME, interfaceName);
        soap11Binding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
    }

    /**
     * Creates a set of default folders for a service. These are the .resources folder and the
     * _private folder
     *
     * @param shortFileName The file name of the JavaScript service
     * @param jsFile        A handle to the JavaScript file
     * @return File The created resources folder
     * @throws CarbonException Throws when default folders couldn't be created
     */
    private File createDefaultFolders(String shortFileName, File jsFile) throws CarbonException {
        /*
        Creating the service.resources dir. For more details see
        http://www.wso2.org/wiki/display/mashup/scripts+folder+structure+and+deployment
        */
        File parentDir = jsFile.getParentFile();
        File resourcesDir =
                new File(parentDir, shortFileName + JSConstants.MASHUP_RESOURCES_FOLDER);
        if (!resourcesDir.exists() && !resourcesDir.mkdir()) {
            throw new CarbonException("Unable to create directory " + resourcesDir.getName());
        }

        /*
        creating _private folder in the resources folder to keep private stuff
        This folder can be used to keep information that the user does not want to share when
        a mashup is shared
        */
        File privateDir = new File(resourcesDir, JSConstants.MASHUP_PRIVATE_FOLDER_NAME);
        if (!privateDir.exists() && !privateDir.mkdir()) {
            throw new CarbonException("Unable to create directory " + privateDir.getName());
        }
        return resourcesDir;
    }

    /**
     * Processors an individual JavaScript function and creates a AxisOperation corresponding to it
     *
     * @param engine          An instance of JavaScriptEngine
     * @param axisService     The AxisService that this operation should be added to
     * @param method          The name of the JavaScript method
     * @param function        A handle to the JavaScript function
     * @param schemaGenerator An instance of SchemaGenerator in order to genarate the schema for
     *                        this operation
     * @param targetNamespace The targetNamespace for this service
     * @throws AxisFault Thrown in case an exception occured while creating the axisoperation
     */
    private boolean processOperation(JavaScriptEngine engine, AxisService axisService, String method,
                                     Function function, SchemaGenerator schemaGenerator,
                                     String targetNamespace) throws AxisFault {

        boolean operationFound = false;

        /*
        When some annotations are used to refer functions e.g this.init = function bar(){};
        Rhino gives init as the function name. This is correct in a JavaScript sence but this is
        not what we need to display in the WSDL. We need the function name as bar instead to be
        displayed in the WSDL. Hence we need to do something special here to get that to work.
        */
        String funcName = (String) function.get(JSConstants.NAME, function);
        /*
        A particular function can be defined in 3 ways in JavaScript.

        1. this.foo = function() { ... }         => method = foo and funcName = ""
                VALID only for this.init, this.destroy

        2. this.foo = function bar() { ... }     => method = foo and funcName = bar
                VALID and funcName i.e. bar is available in the WSDL

        3. function bar() { ... }                => method = bar and funcName = bar
                VALID bar is available in the WSDL

        Then the resulting top level object for a mashup would be
        {
            documentation : "service level documentation",// documentation annotation
            init : function() { ... }, // init function declaration
            foo : function bar() { ... }, // bar operation
            mar : function mar() { ... }, // mar operation
            .....
        }

        In a mashup, init or destroy is an annonymous function.
        Hence we don't need to create an axis operation for that.
        e.g this.init= function (){};
        */
        if ("".equals(funcName) && (JSConstants.INIT_ANNOTATION.equals(method) ||
                JSConstants.DESTROY_ANNOTATION.equals(method))) {
            return operationFound;
        }
        Scriptable parent = function.getParentScope();

        /*
        In the case of this.init = function bar(){}; we don't need to process an operation for
        init hence we return here
        */
        if (!method.equals(funcName) &&
                parent.get(method, parent).equals(parent.get(funcName, parent))) {
            return operationFound;
        }

        // In the case of this.foo = function bar(){ ... }; we want bar to appear in the WSDL
        String originalMethodName = method;
        if (funcName != null && !method.equals(funcName)) {
            method = funcName;
        }

        // Extract all operation level annotations using the JavaScriptOperationsAnnotationParser
        JavaScriptOperationsAnnotationParser annotationParser =
                new JavaScriptOperationsAnnotationParser(function, method);

        String schemaTargetNamespace = schemaGenerator.getSchemaTargetNamespace();
        if (annotationParser.isVisible()) {
            operationFound = true;
            /*
            If a particular method except this.init & this.destroy has defined using above #1 type,
            then the following operationName value will be evaluated to "" which will then fails at
            operation name validation.
             */
            String operationName = annotationParser.getOperationName();

            /*
            Checks the validity of the OperationName. If the OperationName in invalid an
            exception is thrown
            */
            JSUtils.validateName(operationName, OPERATION_NAME);

            AxisOperation operation = axisService.getOperation(new QName(operationName));
            if (operation != null) {
                throw new DeploymentException("There is a conflict in operation names. A " +
                        "function with the name (or a function containing the operationNmae " +
                        "annotation as) " + operationName + " already exists. ");
            }

            //We always assume that our operations are inout operations
            AxisOperation axisOperation = new InOutAxisOperation(new QName(operationName));

            Boolean safe = annotationParser.isSafe();
            if (safe != null) {
                Parameter safeParameter = new Parameter(WSDL2Constants.ATTR_WSDLX_SAFE, safe);
                axisOperation.addParameter(safeParameter);
            }

            String httpLocation = annotationParser.getHttpLocation();

            /*
            If the user did not specify a httpLocation default it to operationName
            cause this is the default that axis2 uses
            */
            if (httpLocation != null) {
                if (!httpLocation.startsWith("{{") && httpLocation.startsWith("{")) {
                    /*
                    We cannot extract parameters off the URL in situations such as
                    foo.httpLocation="{param}"; Rather it should be
                    foo.httpLocation="bar/{param}";
                    */
                    throw new DeploymentException(
                            "The httpLocation Annotation of operation " + operationName +
                                    " is invalid. The httpLocation found was \"" + httpLocation +
                                    "\". The httpLocation should not start with a parameter. " +
                                    "Please include a constant part at the start of the templete.");
                }
            } else {
                httpLocation = operationName;
            }

            String httpMethod = annotationParser.getHttpMethod();
            if (httpMethod == null) {
                /*
                If no httpMethod is specified we look for the safely annotation. If an operation
                is marked as safe then the httpMethod defaults to GET else its POST
                */
                if (safe != null && safe) {
                    httpMethod = HTTPConstants.HEADER_GET;
                } else {
                    httpMethod = HTTPConstants.HEADER_POST;
                }
            }

            // Setting Axis Parameters given in operationParameters annotation
            for (Parameter parameter : annotationParser.getOperationParameters()) {
                axisOperation.addParameter(parameter);
            }

            /*
            Calculate the values for input and output actions according to
            http://www.w3.org/TR/ws-addr-wsdl/#defactionwsdl20
            */
            String inputAction = "urn:" + operationName;
            String outAction = "urn:" + operationName + Java2WSDLConstants.RESPONSE;
            axisOperation.setSoapAction(inputAction);
            axisOperation.setOutputAction(outAction);

            // Create a default SOAP 1.1 Binding operation
            AxisBindingOperation soap11BindingOperation =
                    createDefaultSOAP11BindingOperation(axisOperation, httpLocation, inputAction);

            // Create a default SOAP 1.2 Binding operation
            AxisBindingOperation soap12BindingOperation =
                    createDefaultSOAP12BindingOperation(axisOperation, httpLocation, inputAction);

            // Create a default HTTP Binding operation
            AxisBindingOperation httpBindingOperation =
                    createDefaultHTTPBindingOperation(axisOperation, httpLocation, httpMethod,
                            annotationParser.isIgnoreUncited());

            /*
            We need to extract a constant value from the httpLocation so that the
            httpLocationBasedDispatcher can use that value to dispatch to the correct operation
            */
            String httpLocationString =
                    WSDLUtil.getConstantFromHTTPLocation(httpLocation, httpMethod);
            httpLocationTable.put(httpLocationString, axisOperation);

            /*
            set the org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptReceiver as the
            MessageReceiver for this operation
            */
            axisOperation.setMessageReceiver(new JavaScriptReceiver());
            axisOperation.setStyle(WSDLConstants.STYLE_DOC);
            axisOperation.setDocumentation(annotationParser.getDocumentation());

            /*
            This is needed in case the user used the "operationName" annoatation. for e.g if the
            following was used, the WSDL will show bar but when a request come in we should be
            executing foo instead.

            foo.operationName="bar";
            function foo () {};
            */
            Parameter jsFunctionNameParamter =
                    new Parameter(MashupConstants.JS_FUNCTION_NAME, originalMethodName);
            axisOperation.addParameter(jsFunctionNameParamter);

            String[] params = extractInputParameters(engine, originalMethodName);

            // Create the in and out axis messages for this operation
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            if (inMessage != null) {
                Object inputTypes = annotationParser.getInputTypesNameObject();
                inMessage.setName(method + Java2WSDLConstants.MESSAGE_SUFFIX);
                createAxisBindingMessage(soap11BindingOperation, inMessage,
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                createAxisBindingMessage(soap12BindingOperation, inMessage,
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                createAxisBindingMessage(httpBindingOperation, inMessage,
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                /*
                Generate the input element for the input message using the "inputTypes'
                annotation specified by the user
                */
                XmlSchemaElement element = schemaGenerator
                        .createInputElement(inMessage, inputTypes, operationName, params, method);
                if (element != null) {
                    inMessage.setElementQName(new QName(schemaTargetNamespace, element.getName()));
                }
            }

            AxisMessage outMessage =
                    axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            Object outputType = annotationParser.getOutputTypeNameObject();
            //we always assume return parameter as "return"
            params = new String[]{"return"};

            if (outMessage != null) {
                outMessage.setName(method + Java2WSDLConstants.RESPONSE_MESSAGE);
                createAxisBindingMessage(soap11BindingOperation, outMessage,
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                createAxisBindingMessage(soap12BindingOperation, outMessage,
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                createAxisBindingMessage(httpBindingOperation, outMessage,
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                /*
                Generate the output element for the input message using the "outputType"
                annotation specified by the user
                */
                XmlSchemaElement element = schemaGenerator
                        .createOutputElement(outMessage, outputType, operationName, params, method);
                if (element != null) {
                    outMessage.setElementQName(new QName(schemaTargetNamespace, element.getName()));
                }
            }
            axisService.addOperation(axisOperation);
            axisConfig.getPhasesInfo().setOperationPhases(axisOperation);
        }

        return operationFound;
    }

    private void createAxisBindingMessage(AxisBindingOperation bindingOperation,
                                          AxisMessage message, String direction) {
        AxisBindingMessage inBindingMessage = new AxisBindingMessage();
        inBindingMessage.setName(message.getName());
        inBindingMessage.setAxisMessage(message);
        inBindingMessage.setParent(bindingOperation);
        bindingOperation.addChild(direction, inBindingMessage);
    }

    /**
     * Given a name of a JavaScript function this operation converts that function to a string and
     * extract its defined parameters and put it into an array.
     *
     * @param engine       An instance of JavaScriptEngine
     * @param functionName The name of the JavaScript function
     * @return String[] A String array containing the parameters of this function
     * @throws DeploymentException Thrown in case the special function org_wso2_mashup_ConvertToString
     *                             was not injected into the script
     */
    private String[] extractInputParameters(JavaScriptEngine engine, String functionName)
            throws DeploymentException {
        /*
        In here we inject a special function into the JavaScript service that helps us
        convert a function to a string and get the full string corresponding to that
        function. We need that to get the order of the parameter names in that function.
        Rhino does not preserve the order of parameters as they are held in a map
        */
        Context cx = RhinoEngine.enterContext();
        ScriptableObject serviceScope = JavaScriptEngineUtils.getActiveScope();
        String sourceStr =
                "function org_wso2_mashup_ConvertToString(){ " + "var code = " + functionName +
                        ".toString();" + "return code;}";
        cx.evaluateString(serviceScope, sourceStr, "", 0, null);

        // Get the function from the scope the javascript object is in
        Object fObj = serviceScope.get("org_wso2_mashup_ConvertToString", serviceScope);
        if (!(fObj instanceof Function) || (fObj == Scriptable.NOT_FOUND)) {
            throw new DeploymentException("Method " + "org_wso2_mashup_ConvertToString" +
                    " is undefined or not a function");
        }

        Object functionArgs[] = {};
        Function f = (Function) fObj;

        // Execute our org_wso2_mashup_ConvertToString function and get the function a string
        Object args = f.call(cx, serviceScope, serviceScope, functionArgs);
        String[] params = null;
        if (args instanceof String) {
            String functionString = (String) args;
            int paramStartIndex = functionString.indexOf('(');
            int paramEndIndex = functionString.indexOf(')');

            /*
            Get the parameters of the function as a string. Parameters are always enclosed
            using braces
            */
            String paramString = functionString.substring(paramStartIndex + 1, paramEndIndex);

            // Get the paramer names by splitting them using ","
            params = paramString.split(",");
        }
        return params;
    }

    /**
     * Creates AxisBindingOperation and populates it with HTTP properties
     *
     * @param axisOp        The AxisOperation corresponding to this bindingOperation
     * @param httpLocation  The httpLocation annotation for this operation
     * @param httpMethod    The httpMethod annotation for this operation
     * @param ignoreUncited The httpIgnoreUncited annotation for this operation
     * @return AxisBindingOperation having sdefault HTTP values
     */
    private AxisBindingOperation createDefaultHTTPBindingOperation(AxisOperation axisOp,
                                                                   String httpLocation,
                                                                   String httpMethod,
                                                                   boolean ignoreUncited) {
        AxisBindingOperation httpBindingOperation = new AxisBindingOperation();
        httpBindingOperation.setAxisOperation(axisOp);
        httpBindingOperation.setName(axisOp.getName());
        httpBindingOperation.setParent(httpBinding);
        httpBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
        httpBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpMethod);
        httpBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED, ignoreUncited);
        httpBinding.addChild(httpBindingOperation.getName(), httpBindingOperation);
        return httpBindingOperation;
    }

    /**
     * Creates AxisBindingOperation and populates it with SOAP 1.2 properties
     *
     * @param axisOp       The AxisOperation corresponding to this bindingOperation
     * @param httpLocation The httpLocation annotation for this operation
     * @param inputAction  The input action for this operation
     * @return AxisBindingOperation having sdefault SOAP 1.2 values
     */
    private AxisBindingOperation createDefaultSOAP12BindingOperation(AxisOperation axisOp,
                                                                     String httpLocation,
                                                                     String inputAction) {
        AxisBindingOperation soap12BindingOperation = new AxisBindingOperation();
        soap12BindingOperation.setAxisOperation(axisOp);
        soap12BindingOperation.setName(axisOp.getName());
        soap12BindingOperation.setParent(soap12Binding);
        soap12BindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
        soap12Binding.addChild(soap12BindingOperation.getName(), soap12BindingOperation);
        soap12BindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, inputAction);
        return soap12BindingOperation;
    }

    /**
     * Creates AxisBindingOperation and populates it with SOAP 1.1 properties
     *
     * @param axisOp       The AxisOperation corresponding to this bindingOperation
     * @param httpLocation The httpLocation annotation for this operation
     * @param inputAction  The input action for this operation
     * @return AxisBindingOperation having default SOAP 1.1 values
     */
    private AxisBindingOperation createDefaultSOAP11BindingOperation(AxisOperation axisOp,
                                                                     String httpLocation,
                                                                     String inputAction) {
        AxisBindingOperation soap11BindingOperation = new AxisBindingOperation();
        soap11BindingOperation.setAxisOperation(axisOp);
        soap11BindingOperation.setName(axisOp.getName());
        soap11BindingOperation.setParent(soap11Binding);
        soap11BindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
        soap11Binding.addChild(soap11BindingOperation.getName(), soap11BindingOperation);
        soap11BindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, inputAction);
        return soap11BindingOperation;
    }

    private String fileInputStreamToString(String path) throws AxisFault {
        InputStream jsFileStream = null;
        try {
            jsFileStream = new FileInputStream(path);
            StringBuffer buffer = new StringBuffer();

            InputStreamReader isr = new InputStreamReader(jsFileStream);
            Reader in = new BufferedReader(isr);
            int ch;
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            return buffer.toString();
        } catch (FileNotFoundException e) {
            throw new AxisFault("Unable to load the javaScript at deployment time, File not Found", e);
        } catch (IOException e) {
            throw new AxisFault(e.getMessage(), e);
        }

    }
}

