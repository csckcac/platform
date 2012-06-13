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
package org.wso2.carbon.governance.generic.services;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.generic.beans.ArtifactBean;
import org.wso2.carbon.governance.generic.beans.ArtifactsBean;
import org.wso2.carbon.governance.list.util.GovernanceArtifactFilter;
import org.wso2.carbon.governance.services.util.Util;
import org.wso2.carbon.registry.admin.api.governance.IManageGenericArtifactService;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"unused", "NonJaxWsWebServices", "ValidExternallyBoundObject"})
public class ManageGenericArtifactService extends RegistryAbstractAdmin implements IManageGenericArtifactService {
    private static final Log log = LogFactory.getLog(ManageGenericArtifactService.class);
    private static final String GOVERNANCE_ARTIFACT_CONFIGURATION_PATH =
            RegistryConstants.GOVERNANCE_COMPONENT_PATH + "/configuration/";

    public String addArtifact(String key, String info, String lifecycleAttribute) throws
            RegistryException {
        RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
        Registry registry = getGovernanceUserRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        try {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(info));

            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(registry,
                    configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                    configuration.getArtifactNamespaceAttribute(),
                    configuration.getArtifactElementRoot(),
                    configuration.getArtifactElementNamespace(),
                    configuration.getPathExpression(),
                    configuration.getRelationshipDefinitions());
            GenericArtifact artifact = manager.newGovernanceArtifact(
                    new StAXOMBuilder(reader).getDocumentElement());
            manager.addGenericArtifact(artifact);
            if (lifecycleAttribute != null) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                if (lifecycle != null) {
                    artifact.attachLifecycle(lifecycle);
                }
            }
            return RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
        } catch (Exception e) {
            String msg = "Unable to add artifact. ";
            if (e instanceof RegistryException) {
                throw (RegistryException) e;
            } else if (e instanceof OMException) {
                msg += "Unexpected character found in input-field name.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            throw new RegistryException(
                    msg + (e.getCause() instanceof SQLException ? "" : e.getCause().getMessage()),
                    e);
        }
    }

    public ArtifactsBean listArtifacts(String key, String criteria) {
        RegistryUtils.recordStatistics(key, criteria);
        UserRegistry governanceRegistry = (UserRegistry) getGovernanceUserRegistry();
        ArtifactsBean bean = new ArtifactsBean();
        try {
            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry,
                    configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                    configuration.getArtifactNamespaceAttribute(),
                    configuration.getArtifactElementRoot(),
                    configuration.getArtifactElementNamespace(),
                    configuration.getPathExpression(),
                    configuration.getRelationshipDefinitions());
            final GenericArtifact referenceArtifact;
            if (criteria != null) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                        new StringReader(criteria));
                referenceArtifact = manager.newGovernanceArtifact(
                        new StAXOMBuilder(reader).getDocumentElement());
            } else {
                referenceArtifact = null;
            }
            GenericArtifactFilter artifactFilter = new GenericArtifactFilter() {
                GovernanceArtifactFilter filter = new GovernanceArtifactFilter(referenceArtifact);

                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    return filter.matches(artifact);
                }
            };
            bean.setNames(configuration.getNamesOnListUI());
            bean.setTypes(configuration.getTypesOnListUI());
            String[] expressions = configuration.getExpressionsOnListUI();
            String[] keys = configuration.getKeysOnListUI();
            GenericArtifact[] artifacts = manager.findGenericArtifacts(artifactFilter);
            if (artifacts != null) {
                List<ArtifactBean> artifactBeans = new LinkedList<ArtifactBean>();
                for (GenericArtifact artifact : artifacts) {
                    ArtifactBean artifactBean = new ArtifactBean();
                    List<String> paths = new ArrayList<String>();
                    List<String> values = new ArrayList<String>();
                    String path =
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
                    artifactBean.setPath(path);
                    for (String expression : expressions) {
                        if (expression != null) {
                            if (expression.contains("@{storagePath}") && artifact.getPath() != null) {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                        GovernanceUtils
                                                .getPathFromPathExpression(expression, artifact,
                                                        artifact.getPath()));
                            } else {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                        GovernanceUtils
                                                .getPathFromPathExpression(expression, artifact,
                                                        configuration.getPathExpression()));
                            }
                        } else {
                            paths.add("");
                        }
                    }
                    artifactBean.setValuesB(paths.toArray(new String[paths.size()]));
                    for (String keyForValue : keys) {
                        if (keyForValue != null) {
                            values.add(artifact.getAttribute(keyForValue));
                        } else {
                            values.add("");
                        }
                    }
                    artifactBean.setValuesA(values.toArray(new String[values.size()]));
                    artifactBean.setCanDelete(
                            governanceRegistry.getUserRealm().getAuthorizationManager()
                                    .isUserAuthorized(governanceRegistry.getUserName(),
                                            path, ActionConstants.DELETE));
                    artifactBeans.add(artifactBean);
                }
                bean.setArtifacts(artifactBeans.toArray(new ArtifactBean[artifactBeans.size()]));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of artifacts.", e);
        }
        return bean;
    }

    public String editArtifact(String key, String info, String lifecycleAttribute)
            throws RegistryException {
        RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
        Registry registry = getGovernanceUserRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        try {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(info));

            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(registry,
                    configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                    configuration.getArtifactNamespaceAttribute(),
                    configuration.getArtifactElementRoot(),
                    configuration.getArtifactElementNamespace(),
                    configuration.getPathExpression(),
                    configuration.getRelationshipDefinitions());
            GenericArtifact artifact = manager.newGovernanceArtifact(
                    new StAXOMBuilder(reader).getDocumentElement());
            String currentPath = GovernanceUtils.getPathFromPathExpression(
                    configuration.getPathExpression(), artifact);
            if (registry.resourceExists(currentPath)) {
                GovernanceArtifact oldArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, currentPath);
                if (!(oldArtifact instanceof GenericArtifact)) {
                    String msg = "The updated path is occupied by a non-generic artifact. path: " +
                            currentPath + ".";
                    log.error(msg);
                    throw new Exception(msg);
                }
                // id is used to differentiate the artifact
                String id = oldArtifact.getId();
                artifact.setId(id);
                manager.updateGenericArtifact(artifact);
            } else {
                manager.addGenericArtifact(artifact);
            }
            if (lifecycleAttribute != null && !lifecycleAttribute.equals("null")) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                artifact.attachLifecycle(lifecycle);
            }
            return RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
        } catch (Exception e) {
            String msg = "Unable to edit artifact. ";
            if (e instanceof RegistryException) {
                throw (RegistryException) e;
            } else if (e instanceof OMException) {
                msg += "Unexpected character found in input-field name.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            throw new RegistryException(msg + (e.getCause() instanceof SQLException ? "" :
                    e.getCause().getMessage()), e);
        }
    }

    public String getArtifactContent(String path) throws RegistryException {
        Registry registry = getGovernanceUserRegistry();
        // resource path is created to make sure the version page doesn't give null values
        if (!registry.resourceExists(new ResourcePath(path).getPath())) {
            return null;
        }
        return new String((byte[]) registry.get(path).getContent());
    }

    public String getArtifactUIConfiguration(String key) throws RegistryException {
        try {
            Registry registry = getConfigSystemRegistry();
            return new String(
                    (byte[]) registry.get(GOVERNANCE_ARTIFACT_CONFIGURATION_PATH + key)
                            .getContent());
        } catch (Exception e) {
            log.error("An error occurred while obtaining configuration", e);
            return null;
        }
    }

    public boolean setArtifactUIConfiguration(String key, String update) throws RegistryException {
        Registry registry = getConfigSystemRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        try {
            Util.validateOMContent(Util.buildOMElement(update));

            String path = GOVERNANCE_ARTIFACT_CONFIGURATION_PATH + key;
            Resource resource = registry.get(path);
            resource.setContent(update);
            registry.put(path, resource);
            return true;
        } catch (Exception e) {
            log.error("An error occurred while saving configuration", e);
            return false;
        }
    }

    public boolean canChange(String path) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        if(registry.getUserName() != null && registry.getUserRealm() != null){
            if (registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                    registry.getUserName(), path, ActionConstants.PUT)) {
                Resource resource = registry.get(path);
                String property = resource.getProperty(
                        CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME);
                return property == null || !Boolean.parseBoolean(property) ||
                        registry.getUserName().equals(
                                resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME));

            }
        }
        return false;
    }

    /* get available aspects */
    public String[] getAvailableAspects()throws Exception{
        return GovernanceUtils.getAvailableAspects();
    }
}

