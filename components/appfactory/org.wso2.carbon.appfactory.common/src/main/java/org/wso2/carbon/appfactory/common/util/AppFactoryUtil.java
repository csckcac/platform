/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.common.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Util class
 */
public class AppFactoryUtil {
    private static final Log log = LogFactory.getLog(AppFactoryUtil.class);
    private static AppFactoryConfiguration configuration;
    public static final String APPFACTORY_CONFIG_FILE_NAME = "appfactory.xml";

    public static AppFactoryConfiguration loadAppFactoryConfiguration() throws AppFactoryException {
        OMElement appfactory = loadAppfactoryXML();
        Iterator iterator = appfactory.getChildElements();
        AppFactoryConfiguration config = new AppFactoryConfiguration();

        while (iterator.hasNext()) {
            OMElement element = (OMElement) iterator.next();

            if (AppFactoryConstants.SSO_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                getSSOConfigs(element, config);
            } else if (AppFactoryConstants.WEB_SERVICE_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                getWebServiceConfigs(element, config);
            } else if (AppFactoryConstants.BPEL_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                getBpelConfig(element, config);
            } else if (AppFactoryConstants.ADMIN_USER_NAME_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                config.setAdminUserName(element.getText());
            } else if (AppFactoryConstants.ADMIN_PASSWORD_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                config.setAdminPassword(element.getText());
            } else if (AppFactoryConstants.PROJECT_DEPLOYMENT_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                getProjectDeploymentConfig(element, config);
            } else if (AppFactoryConstants.SVN_REPO_MGT_CONFIG_ROOT_ELEMENT.equals(element.getLocalName())) {
                getSVNRepositoryMGTConfig(element, config);
            }
        }
        return config;
    }

    private static void getSVNRepositoryMGTConfig(OMElement element,
                                                  AppFactoryConfiguration config) {
        Iterator iterator = element.getChildElements();
        while (iterator.hasNext()) {
            OMElement childElement = (OMElement) iterator.next();
            if (AppFactoryConstants.SCM_SERVER_IP.equals(childElement.getLocalName())) {
                config.setsCMServerIp(childElement.getText());
            } else if (AppFactoryConstants.SCM_SERVER_PORT.equals(childElement.getLocalName())) {
                config.setsCMServerPort(childElement.getText());
            } else if (AppFactoryConstants.SCM_SERVER_REALM_NAME.equals(childElement.getLocalName())) {
                config.setsCMServerRealmName(childElement.getText());
            } else if (AppFactoryConstants.SCM_SERVER_ADMIN_USER_NAME.equals(childElement.getLocalName())) {
                config.setsCMServerAdminUserName(childElement.getText());
            } else if (AppFactoryConstants.SCM_SERVER_ADMIN_PASSWORD.equals(childElement.getLocalName())) {
                config.setsCMServerAdminPassword(childElement.getText());
            } else if (AppFactoryConstants.SCM_READ_WRITE_PERMISSION_NAME.equals(childElement.getLocalName())) {
                config.setsCMReadWritePermissionName(childElement.getText());
            }
        }
    }

    private static void getProjectDeploymentConfig(OMElement deploymentConfig,
                                                   AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement svnBaseURLElement = deploymentConfig.getFirstChildWithName(new QName(
                AppFactoryConstants.PROJECT_DEPLOYMENT_CONFIG_SVN_URL));
        if (svnBaseURLElement == null) {
            throw new AppFactoryException("SVN base url is not defined in deployment configuration.");
        }
        config.setSvnBaseURL(svnBaseURLElement.getText());

        Iterator stagesItr = deploymentConfig.getChildrenWithName(new QName(
                AppFactoryConstants.PROJECT_DEPLOYMENT_CONFIG_STAGE));
        if (!stagesItr.hasNext()) {
            throw new AppFactoryException("No stages defined for deployment of artifacts in " +
                                          "deployment configuration.");
        }
        while (stagesItr.hasNext()) {
            OMElement stageElement = (OMElement) stagesItr.next();
            String stageName = stageElement.getAttributeValue(new QName("name")).trim();
            Iterator deploymentServerLocationsItr = stageElement.getChildrenWithName(new QName(
                    AppFactoryConstants.PROJECT_DEPLOYMENT_CONFIG_SERVER_LOCATION));
            List<String> deploymentServerLocations = new ArrayList<String>();
            while (deploymentServerLocationsItr.hasNext()) {
                OMElement deploymentServerLocationElement
                        = (OMElement) deploymentServerLocationsItr.next();
                deploymentServerLocations.add(deploymentServerLocationElement.getText().trim());
            }
            config.getDeploymentServerLocations().put(stageName, deploymentServerLocations);
        }
    }

    private static void getBpelConfig(OMElement element, AppFactoryConfiguration config) {
        Iterator iterator = element.getChildElements();
        while (iterator.hasNext()) {
            OMElement childElement = (OMElement) iterator.next();
            if (AppFactoryConstants.BPEL_CONFIG_CREATE_USER.equals(childElement.getLocalName())) {
                config.setBpelEPRCreateUser(childElement.getText());
            } else if (AppFactoryConstants.BPEL_CONFIG_ACTIVATE_USER.equals(childElement.getLocalName())) {
                config.setBpelEPRActivateUser(childElement.getText());
            }
        }

    }

    private static void getWebServiceConfigs(OMElement element, AppFactoryConfiguration config) {
        Iterator iterator = element.getChildElements();
        while (iterator.hasNext()) {
            OMElement childElement = (OMElement) iterator.next();
            if (AppFactoryConstants.WEB_SERVICE_CONFIG_ADD_USER_TO_PROJECT.equals(childElement.getLocalName())) {
                config.setWebServiceEPRAddUserToProject(childElement.getText());
            } else if (AppFactoryConstants.WEB_SERVICE_CONFIG_CREATE_PROJECT.equals(childElement.getLocalName())) {
                config.setWebServiceEPRCreateProject(childElement.getText());
            } else if (AppFactoryConstants.WEB_SERVICE_CONFIG_GET_ROLES_OF_USER_FOR_PROJECT.equals(childElement.getLocalName())) {
                config.setWebServiceEPRGetRolesOfUserForProject(childElement.getText());
            } else if (AppFactoryConstants.WEB_SERVICE_CONFIG_GET_USERS_OF_PROJECT.equals(childElement.getLocalName())) {
                config.setWebServiceEPRGetUsersOfProject(childElement.getText());
            } else if (AppFactoryConstants.WEB_SERVICE_CONFIG_EMAIL_VERIFICATION_SERVICE.equals(childElement.getLocalName())) {
                config.setWebServiceEPREmailVarificationService(childElement.getText());
            }


        }
    }

    private static void getSSOConfigs(OMElement element, AppFactoryConfiguration config) {
        Iterator iterator = element.getChildElements();
        while (iterator.hasNext()) {
            OMElement childElement = (OMElement) iterator.next();
            if (AppFactoryConstants.SSO_CONFIG_NAME.equals((childElement.getLocalName()))) {
                config.setsSOName(childElement.getText());
            } else if (AppFactoryConstants.SSO_CONFIG_IDENTITY_PROVIDER_URL.equals(childElement.getLocalName())) {
                config.setsSOIdentityProviderEPR(childElement.getText());
            } else if (AppFactoryConstants.SSO_CONFIG_KEY_STORE_PASSWORD.equals(childElement.getLocalName())) {
                config.setsSOKeyStorePassword(childElement.getText());
            } else if (AppFactoryConstants.SSO_CONFIG_IDENTITY_ALIAS.equals(childElement.getLocalName())) {
                config.setsSOIdentityAlias(childElement.getText());
            } else if (AppFactoryConstants.SSO_CONFIG_KEY_STORE_NAME.equals(childElement.getLocalName())) {
                config.setsSOKeyStoreName(childElement.getLocalName());
            }
        }
    }

    private static OMElement loadAppfactoryXML() throws AppFactoryException {
        String fileLocation = new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath())
                .append(RegistryConstants.PATH_SEPARATOR).
                        append(AppFactoryUtil.APPFACTORY_CONFIG_FILE_NAME).toString();

        File configFile = new File(fileLocation);
        InputStream inputStream = null;
        OMElement configXMLFile;
        try {
            inputStream = new FileInputStream(configFile);
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            configXMLFile = builder.getDocumentElement();
        } catch (FileNotFoundException ignore) {
            String msg = "Unable to locate the " + APPFACTORY_CONFIG_FILE_NAME;
            log.error(msg);
            throw new AppFactoryException(msg);
        } catch (XMLStreamException e) {
            String msg = "Error in reading " + APPFACTORY_CONFIG_FILE_NAME;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String msg = "Error in closing stream ";
                log.error(msg, e);

            }
        }
        return configXMLFile;
    }

    public static void setAppFactoryConfiguration(AppFactoryConfiguration configuration) {
        AppFactoryUtil.configuration = configuration;
    }

    public static AppFactoryConfiguration getConfiguration() {
        return configuration;
    }
}
