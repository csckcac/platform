/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.uddi.deployer;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMap;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMapManager;
import org.apache.axis2.jaxws.addressing.util.EndpointKey;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.framework.JAXWSDeployer;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Use custom JAX-WS deployer for preventing class loading issue in carbon. Functionality is same as Axis2 JAX-WS
 * deployer, but we have used OSGI class loader for deploying the JAX-WS services.
 */
public class JUDDIJAXWSDeployer extends JAXWSDeployer {

    private static final Log log = LogFactory.getLog(JUDDIJAXWSDeployer.class);

    private Deployer jaxwsDeployer;
    private ConfigurationContext configurationContext;

    public JUDDIJAXWSDeployer(Deployer jaxwsDeployer, ConfigurationContext configurationContext) {
        this.jaxwsDeployer = jaxwsDeployer;
        this.configurationContext = configurationContext;
    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData) {
        try {
            String groupName = deploymentFileData.getName();
            URL location = deploymentFileData.getFile().toURL();
            if (isJar(deploymentFileData.getFile())) {
                log.info("Deploying artifact : " + deploymentFileData.getAbsolutePath());
                ArrayList<URL> urls = new ArrayList<URL>();
                urls.add(deploymentFileData.getFile().toURL());

                List<String> classList = Utils.getListOfClasses(deploymentFileData);
                //Setting the OSGI class loader
                AxisServiceGroup serviceGroup = deployClasses(groupName, location,
                        Thread.currentThread().getContextClassLoader(), classList);
                if (serviceGroup == null) {
                    String msg = "Error:\n No @WebService annotated service implementations found in the jar: " +
                            location.toString() +
                            ". Service deployment failed.";
                    log.error(msg);
                    configurationContext.getAxisConfiguration().getFaultyServices().
                            put(deploymentFileData.getFile().getAbsolutePath(), msg);
                }
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        } catch (DeploymentException e) {
            log.error(e.getMessage());
        } catch (Throwable t) {
            storeFaultyService(deploymentFileData, t);
        }
    }

    @Override
    protected AxisServiceGroup deployClasses(String groupName, URL location, ClassLoader classLoader, List<String> classList) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxisFault {
        ArrayList<AxisService> axisServiceList = new ArrayList<AxisService>();
        for (String className : classList) {
            Class<?> pojoClass;
            try {
                pojoClass = Loader.loadClass(classLoader, className);
            } catch (Exception e) {
                continue;
            } catch (Throwable t) {
                continue;
            }
            WebService wsAnnotation = pojoClass.getAnnotation(WebService.class);
            WebServiceProvider wspAnnotation = null;
            if (wsAnnotation == null) {
                wspAnnotation = pojoClass.getAnnotation(WebServiceProvider.class);
            }

            // Create an Axis Service only if the class is not an interface and it has either
            // @WebService annotation or @WebServiceProvider annotation.
            if ((wsAnnotation != null
                    || wspAnnotation != null)
                    && !pojoClass.isInterface()) {
                AxisService axisService;
                axisService =
                        createAxisService(classLoader,
                                className,
                                location);
                if (axisService != null) {
                    log.info("Deploying JAXWS annotated class " + className + " as a service - "
                            + axisService.getName());
                    axisServiceList.add(axisService);
                }
            }
        }
        int size = axisServiceList.size();
        if (size <= 0) {
            return null;
        }
        //creating service group by considering the hierarchical path also
        AxisServiceGroup serviceGroup = new AxisServiceGroup();
        serviceGroup.setServiceGroupName(groupName);
        for (AxisService axisService : axisServiceList) {
            axisService.setName(axisService.getName());
            serviceGroup.addService(axisService);
        }
        configurationContext.getAxisConfiguration().addServiceGroup(serviceGroup);
        configureAddressing(serviceGroup);
        return serviceGroup;

    }

    @Override
    protected void storeFaultyService(DeploymentFileData deploymentFileData, Throwable t) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter ptintWriter = new PrintWriter(errorWriter);
        t.printStackTrace(ptintWriter);
        String error = "Error:\n" + errorWriter.toString();
        configurationContext.getAxisConfiguration().getFaultyServices().put(deploymentFileData.getFile().getAbsolutePath(), error);
    }


    @Override
    protected AxisService createAxisService(ClassLoader classLoader, String className, URL serviceLocation) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxisFault {
        Class<?> pojoClass = Loader.loadClass(classLoader, className);
        AxisService axisService;
        try {
            axisService = DescriptionFactory.createAxisService(pojoClass, configurationContext);
        } catch (Throwable t) {
            log.error("Exception creating Axis Service : " + t.getCause(), t);
            return null;
        }
        if (axisService != null) {
            Iterator<AxisOperation> operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation axisOperation = operations.next();
                if (axisOperation.getMessageReceiver() == null) {
                    axisOperation.setMessageReceiver(new JAXWSMessageReceiver());
                }
            }
            axisService.setElementFormDefault(false);
            axisService.setFileName(serviceLocation);
            axisService.setClassLoader(classLoader);
            axisService.addParameter(new Parameter(org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER, classLoader));
        }
        return axisService;
    }


    //Store the address URIs that we will need to create endpoint references at runtime.
    private void configureAddressing(AxisServiceGroup serviceGroup) {
        EndpointContextMap map =
                (EndpointContextMap) configurationContext.getProperty(org.apache.axis2.jaxws.Constants.ENDPOINT_CONTEXT_MAP);

        if (map == null) {
            map = EndpointContextMapManager.getEndpointContextMap();
            configurationContext.setProperty(org.apache.axis2.jaxws.Constants.ENDPOINT_CONTEXT_MAP, map);
        }

        Iterator<AxisService> iterator = serviceGroup.getServices();

        while (iterator.hasNext()) {
            AxisService axisService = iterator.next();
            Parameter param =
                    axisService.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
            EndpointDescription ed = (EndpointDescription) param.getValue();
            QName serviceName = ed.getServiceQName();
            QName portName = ed.getPortQName();
            EndpointKey key = new EndpointKey(serviceName, portName);

            map.put(key, axisService);
        }
    }

    @Override
    public void undeploy(String fileName) {
        try {
            jaxwsDeployer.undeploy(fileName);
        } catch (DeploymentException e) {
            log.error(e.getMessage());
        }
    }
}
