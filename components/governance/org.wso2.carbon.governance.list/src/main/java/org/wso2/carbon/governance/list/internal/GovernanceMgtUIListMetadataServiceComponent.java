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
package org.wso2.carbon.governance.list.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.operations.*;
import org.wso2.carbon.governance.list.operations.util.OperationsConstants;
import org.wso2.carbon.governance.list.util.CommonUtil;
import org.wso2.carbon.governance.list.util.ListServiceUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.governance.list"
 * immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class GovernanceMgtUIListMetadataServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceMgtUIListMetadataServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext context) {
        final RegistryService registryService = CommonUtil.getRegistryService();
        try {
            UserRegistry registry =
                    registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            configureGovernanceArtifacts(registry, CommonUtil.getConfigurationContext().getAxisConfiguration());
            Axis2ConfigurationContextObserver observer =
                    new AbstractAxis2ConfigurationContextObserver() {
                        public void createdConfigurationContext(ConfigurationContext context) {
                            try {
                                int tenantId = SuperTenantCarbonContext.getCurrentContext(context).getTenantId();
                                configureGovernanceArtifacts(registryService.getRegistry(
                                        CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId),
                                        context.getAxisConfiguration());
                            } catch (RegistryException e) {
                                log.error("Unable to load governance artifacts.", e);
                            }
                        }
                    };
            serviceRegistration = context.getBundleContext().registerService(
                    Axis2ConfigurationContextObserver.class.getName(), observer, null);
            HandlerManager handlerManager = registry.getRegistryContext().getHandlerManager();
            if (handlerManager != null) {
                handlerManager.addHandler(null,
                        new MediaTypeMatcher(
                                GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE),
                        new Handler() {
                            public void put(RequestContext requestContext)
                                    throws RegistryException {
                                if (!org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .isUpdateLockAvailable()) {
                                    return;
                                }
                                org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .acquireUpdateLock();
                                try {
                                    if(!CommonUtil.validateXMLConfigOnSchema(new String((byte[]) requestContext.getResource().getContent()), "rxt-ui-config")) {
                                     throw new RegistryException("Violation of RXT definition in configuration file, follow the schema correctly..!!");
                                    }

                                    Registry userRegistry = requestContext.getRegistry();
                                    userRegistry.put(
                                            requestContext.getResourcePath().getPath(),
                                            requestContext.getResource());
                                    Registry systemRegistry = requestContext.getSystemRegistry();
                                    configureGovernanceArtifacts(systemRegistry,
                                            CommonUtil.getConfigurationContext().getAxisConfiguration());
                                    requestContext.setProcessingComplete(true);
                                } finally {
                                    org.wso2.carbon.registry.extensions.utils.CommonUtil
                                            .releaseUpdateLock();
                                }
                            }

                            public void delete(RequestContext requestContext) throws RegistryException {
                                Resource resource = requestContext.getResource();
                                Object content = resource.getContent();
                                String elementString;
                                if (content instanceof String) {
                                    elementString = (String) content;
                                } else {
                                    elementString = new String((byte[]) content);
                                }
                                GovernanceArtifactConfiguration artifactConfiguration =
                                        GovernanceUtils.getGovernanceArtifactConfiguration(elementString);
                                String needToDelete = artifactConfiguration.getKey();

                                UserRegistry systemRegistry =
                                        registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                                if(systemRegistry.resourceExists(GovernanceConstants.ARTIFACT_CONTENT_PATH+needToDelete)){
                                    systemRegistry.delete(GovernanceConstants.ARTIFACT_CONTENT_PATH+needToDelete);
                                }
                                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) systemRegistry);
                                List<GovernanceArtifactConfiguration> configurations =
                                        GovernanceUtils.findGovernanceArtifactConfigurations(systemRegistry);
                                for (GovernanceArtifactConfiguration configuration : configurations) {
                                    for (ManagementPermission uiPermission : configuration.getUIPermissions()) {
                                        String resourceId = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                uiPermission.getResourceId();
                                        if (systemRegistry.resourceExists(resourceId)&&needToDelete.equals(configuration.getKey())) {
                                            systemRegistry.delete(resourceId);
                                        }
                                    }
                                }

                                unDeployCRUDService(artifactConfiguration,
                                        CommonUtil.getConfigurationContext().getAxisConfiguration());
                            }
                        });
                handlerManager.addHandler(null,
                        new MediaTypeMatcher() {
                            public boolean handlePut(RequestContext requestContext)
                                    throws RegistryException {
                                Resource resource = requestContext.getResource();
                                if (resource == null) {
                                    return false;
                                }
                                String mType = resource.getMediaType();
                                return mType != null && (invert != (mType.matches(
                                        "application/vnd\\.[a-zA-Z0-9.-]+\\+xml") & !mType.matches(
                                        "application/vnd.wso2-service\\+xml")));
                            }

                            @Override
                            public boolean handleCreateLink(RequestContext requestContext) throws RegistryException {
                                String targetPath = requestContext.getTargetPath();
                                if (!requestContext.getRegistry().resourceExists(targetPath)) {
                                    return false;
                                }
                                Resource targetResource = requestContext.getRegistry().get(targetPath);
                                String mType = targetResource.getMediaType();

                                return mType != null && (invert != (mType.matches(
                                        "application/vnd\\.[a-zA-Z0-9.-]+\\+xml") & !mType.matches(
                                        "application/vnd.wso2-service\\+xml")));
                            }
                        },
                        new Handler() {
/*
                            public void put(RequestContext requestContext)
                                    throws RegistryException {
                                if (!org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .isUpdateLockAvailable()) {
                                    return;
                                }
                                org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .acquireUpdateLock();
                                try {
                                    String id = requestContext.getResource().getUUID();
                                    if (id != null) {
                                        String path = requestContext.getResourcePath().getPath();
                                        Registry unchrootedSystemRegistry =
                                                org.wso2.carbon.registry.extensions.utils.CommonUtil
                                                        .getUnchrootedSystemRegistry(
                                                                requestContext);
                                    }
                                } finally {
                                    org.wso2.carbon.registry.extensions.utils.CommonUtil
                                            .releaseUpdateLock();
                                }
                            }
*/

                            @Override
                            public void createLink(RequestContext requestContext) throws RegistryException {
                                String symlinkPath = requestContext.getResourcePath().getPath();

                                if(!symlinkPath.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)){
                                    throw new RegistryException("symlink creation is not allowed for artifact "
                                            + requestContext.getTargetPath());
                                }
                            }
                        });
            }

            ListServiceUtil.startArtifactFetcher(registry);
        } catch (RegistryException e) {
            log.error("Unable to load governance artifacts.", e);
        }
        log.debug("******* Governance List Metadata bundle is activated ******* ");
    }

    private void configureGovernanceArtifacts(Registry systemRegistry, AxisConfiguration axisConfig)
            throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)systemRegistry);
        List<GovernanceArtifactConfiguration> configurations =
                GovernanceUtils.findGovernanceArtifactConfigurations(systemRegistry);
        Registry governanceSystemRegistry = GovernanceUtils.getGovernanceSystemRegistry(systemRegistry);

        for (GovernanceArtifactConfiguration configuration : configurations) {
            for (ManagementPermission uiPermission : configuration.getUIPermissions()) {
                String resourceId = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        uiPermission.getResourceId();
                if (systemRegistry.resourceExists(resourceId)) {
                    continue;
                }
                Collection collection = systemRegistry.newCollection();
                collection.setProperty("name", uiPermission.getDisplayName());
                systemRegistry.put(resourceId, collection);
            }
            RXTMessageReceiver receiver = new RXTMessageReceiver();

            if (axisConfig != null) {
                try {

                    String singularLabel = configuration.getSingularLabel();
                    String pluralLabel = configuration.getPluralLabel();
                    String key = configuration.getKey();
                    String mediaType = configuration.getMediaType();

//                    We avoid creation of a axis service if there is a service with the same name
                    if(axisConfig.getService(singularLabel) != null){
                        continue;
                    }
                    AxisService service = new AxisService(singularLabel);

                    Parameter param1 = new Parameter("AuthorizationAction", "/permission/admin/login");
                    param1.setLocked(true);
                    service.addParameter(param1);

                    Parameter param2 = new Parameter("adminService", "true");
                    param2.setLocked(true);
                    service.addParameter(param2);

                    Parameter param3 = new Parameter("hiddenService", "true");
                    param3.setLocked(true);
                    service.addParameter(param3);

                    XmlSchemaCollection schemaCol = new XmlSchemaCollection();
                    List<XmlSchema> schemaList = new ArrayList<XmlSchema>();

                    AbstractOperation create = new CreateOperation(new QName(OperationsConstants.ADD + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.ADD + singularLabel + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionCreate = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/"+ pluralLabel +"/add");
                    authorizationActionCreate.setLocked(true);
                    create.addParameter(authorizationActionCreate);

                    service.addOperation(create);
                    schemaList.addAll(Arrays.asList(create.getSchemas(schemaCol)));

                    AbstractOperation read = new ReadOperation(new QName(OperationsConstants.GET + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.GET + singularLabel + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionRead = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/"+ pluralLabel +"/list");
                    authorizationActionRead.setLocked(true);
                    read.addParameter(authorizationActionRead);

                    service.addOperation(read);
                    schemaList.addAll(Arrays.asList(read.getSchemas(schemaCol)));

                    AbstractOperation update = new UpdateOperation(new QName(OperationsConstants.UPDATE + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.UPDATE + singularLabel + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionUpdate = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/"+ pluralLabel +"/add");
                    authorizationActionUpdate.setLocked(true);
                    update.addParameter(authorizationActionUpdate);

                    service.addOperation(update);
                    schemaList.addAll(Arrays.asList(update.getSchemas(schemaCol)));

                    AbstractOperation delete = new DeleteOperation(new QName(OperationsConstants.DELETE + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.DELETE + singularLabel + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionDelete = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/"+ pluralLabel +"/add");
                    authorizationActionDelete.setLocked(true);
                    delete.addParameter(authorizationActionDelete);

                    service.addOperation(delete);
                    schemaList.addAll(Arrays.asList(delete.getSchemas(schemaCol)));

                    axisConfig.addService(service);

                    XmlSchema schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.REGISTRY_EXCEPTION1_XSD.getBytes())), null);
                    schemaList.add(schema);

                    schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.GOVERNANCE_EXCEPTION_XSD.getBytes())), null);
                    schemaList.add(schema);

                    schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.REGISTRY_EXCEPTION2_XSD.getBytes())), null);
                    schemaList.add(schema);

                    service.addSchema(schemaList);

                } catch (AxisFault axisFault) {
                    String msg = "Error occured while adding services";
                    log.error(msg, axisFault);
                }
            }
        }
    }

    private void unDeployCRUDService(GovernanceArtifactConfiguration configuration, AxisConfiguration axisConfig){
        String singularLabel = configuration.getSingularLabel();

        try {
            if(axisConfig.getService(singularLabel) != null){
                axisConfig.removeService(singularLabel);
            }
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        }

    }

    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        ListServiceUtil.stopArtifactFetcher();
        log.debug("Governance List Metadata bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        if (configurationContextService != null) {
            CommonUtil.setConfigurationContext(configurationContextService.getServerConfigContext());
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        CommonUtil.setConfigurationContext(null);
    }
}
