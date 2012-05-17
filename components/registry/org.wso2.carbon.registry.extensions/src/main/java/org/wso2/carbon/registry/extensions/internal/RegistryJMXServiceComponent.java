/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.extensions.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.statistics.StatisticsCollector;
import org.wso2.carbon.registry.extensions.jmx.Eventing;
import org.wso2.carbon.registry.extensions.jmx.InvocationStatistics;
import org.wso2.carbon.utils.CarbonUtils;

import javax.management.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Stack;

/**
 * @scr.component name="org.wso2.carbon.registry.jmx" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class RegistryJMXServiceComponent {

    // Property, Notification, Auditing, Search??
    // Query Processors

    private static Log log = LogFactory.getLog(RegistryJMXServiceComponent.class);
    private boolean isJMXEnabled = false;
    private Stack<ServiceRegistration> serviceRegistrations = new Stack<ServiceRegistration>();
    private Stack<ObjectName> mBeans = new Stack<ObjectName>();

    protected void activate(ComponentContext context) {
        if (isJMXEnabled()) {
            try {
                registerMBean(context, new InvocationStatistics(),
                        StatisticsCollector.class.getName());
                registerMBean(context, new Eventing(), Eventing.class.getName());
            } catch (JMException e) {
                log.error("Unable to register JMX extensions", e);
            }
        }
        log.debug("Registry JMX component is activated");
    }

    private void registerMBean(ComponentContext context, Object object, String serviceClass)
            throws MalformedObjectNameException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {
        mBeans.push(new ObjectName("org.wso2.carbon:Type=Registry,ConnectorName=" +
                object.getClass().getSimpleName()));
        ManagementFactory.getPlatformMBeanServer().registerMBean(object, mBeans.peek());
        serviceRegistrations.push(context.getBundleContext().registerService(
                serviceClass, object, null));
    }

    protected void deactivate(ComponentContext context) {
        while(!mBeans.empty()) {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(mBeans.pop());
            } catch (JMException e) {
                log.error("Unable to un-register JMX extensions", e);
            }
        }
        while(!serviceRegistrations.empty()) {
            serviceRegistrations.pop().unregister();
        }
        log.debug("Registry JMX component is deactivated");
    }

    protected void setRegistryService(RegistryService registryService) {
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    private boolean isJMXEnabled() {
        if (isJMXEnabled) {
            return true;
        }
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (registryXML.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(registryXML);
                    StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                    OMElement configElement = builder.getDocumentElement();
                    OMElement jmx = configElement.getFirstChildWithName(new QName("enableJMX"));
                    isJMXEnabled = jmx != null &&
                            Boolean.toString(true).equalsIgnoreCase(jmx.getText());
                    return isJMXEnabled;
                } catch (XMLStreamException e) {
                    log.error("Unable to parse registry.xml", e);
                } catch (IOException e) {
                    log.error("Unable to read registry.xml", e);
                }
            }
        }
        return false;
    }
}
