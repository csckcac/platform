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
 * Util class for building app factory configuration
 */
public class AppFactoryUtil {
    private static final Log log = LogFactory.getLog(AppFactoryUtil.class);
    private static AppFactoryConfiguration configuration;

    public static AppFactoryConfiguration loadAppFactoryConfiguration() throws AppFactoryException {
        OMElement appFactory = loadAppFactoryXML();
        AppFactoryConfiguration config = new AppFactoryConfiguration();

        if (!AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE.equals(appFactory.getNamespace().getNamespaceURI())) {
            handleError("AppFactory namespace is invalid.");
        }

        // set admin user name
        OMElement adminUsername = appFactory.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                             AppFactoryConstants.APPFACTORY_CONFIG_ADMIN_USER));
        if (adminUsername == null) {
            handleError("Admin user name is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            config.setAdminUserName(adminUsername.getText().trim());
        }

        // set admin password
        OMElement adminPassword = appFactory.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                             AppFactoryConstants.APPFACTORY_CONFIG_ADMIN_PASSWORD));
        if (adminPassword == null) {
            handleError("Admin password is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            config.setAdminPassword(adminPassword.getText().trim());
        }

        // set application management configuration
        setApplicationMgtConfig(appFactory, config);

        // set SSO relying party configuration
        setSSOConfigs(appFactory, config);

        // set web service end point configuration
        setWebServiceConfigs(appFactory, config);

        //set application repository configuration
        setRepositoryMGTConfig(appFactory, config);

        // set application deployment configuration
        setApplicationDeploymentConfig(appFactory, config);

        return config;
    }


    private static void setApplicationMgtConfig(OMElement element,
                                            AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement applicationMgt = element.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                       AppFactoryConstants.APPFACTORY_CONFIG_PROJECT_MGT));
        if (applicationMgt == null) {
            handleError("Application Management is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            OMElement defaultApplicationUserRoles = applicationMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                           AppFactoryConstants.APPFACTORY_CONFIG_PROJECT_USER_ROLES));
            if (defaultApplicationUserRoles == null) {
                handleError("DefaultApplicationUserRoles is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                String[] roles = defaultApplicationUserRoles.getText().trim().split(",");
                config.addDefaultApplicationUserRole(roles);
            }
        }


    }

    private static void setRepositoryMGTConfig(OMElement element,
                                               AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement repositoryMgt = element.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                          AppFactoryConstants.REPO_MGT_CONFIG_ROOT_ELEMENT));
        if (repositoryMgt == null) {
            handleError("Repository Management is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            OMElement scmServerIp = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                  AppFactoryConstants.SCM_SERVER_IP));
            if (scmServerIp == null) {
                handleError("scmServerIp is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmServerIp(scmServerIp.getText().trim());
            }

            OMElement scmServerPort = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                    AppFactoryConstants.SCM_SERVER_PORT));
            if (scmServerPort == null) {
                handleError("scmServerIp is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmServerPort(scmServerPort.getText().trim());
            }
            OMElement scmServerRealmName = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                         AppFactoryConstants.SCM_SERVER_REALM_NAME));
            if (scmServerRealmName == null) {
                handleError("scmServerRealmName is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmServerRealmName(scmServerRealmName.getText().trim());
            }
            OMElement scmServerAdminName = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                         AppFactoryConstants.SCM_SERVER_ADMIN_USER_NAME));
            if (scmServerAdminName == null) {
                handleError("scmServerAdminName is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmServerAdminUserName(scmServerAdminName.getText().trim());
            }
            OMElement scmServerAdminPassword = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                             AppFactoryConstants.SCM_SERVER_ADMIN_PASSWORD));
            if (scmServerAdminPassword == null) {
                handleError("scmServerAdminPassword is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmServerAdminPassword(scmServerAdminPassword.getText().trim());
            }
            OMElement scmReadWritePermissionName = repositoryMgt.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                                 AppFactoryConstants.SCM_READ_WRITE_PERMISSION_NAME));
            if (scmReadWritePermissionName == null) {
                handleError("scmReadWritePermissionName is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setScmReadWritePermissionName(scmReadWritePermissionName.getText().trim());
            }
        }
    }

    private static void setApplicationDeploymentConfig(OMElement element,
                                                   AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement applicationDeployment = element.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                              AppFactoryConstants.PROJECT_DEPLOYMENT_ROOT_ELEMENT));
        if (applicationDeployment == null) {
            handleError("Repository Management is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            Iterator stagesItr = applicationDeployment.getChildrenWithName(new QName(
                    AppFactoryConstants.PROJECT_DEPLOYMENT_STAGE));
            if (!stagesItr.hasNext()) {
                handleError("Application Deployment stages are not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            }
            while (stagesItr.hasNext()) {
                OMElement stageElement = (OMElement) stagesItr.next();
                String stageName = stageElement.getAttributeValue(new QName("name")).trim();
                Iterator deploymentServerUrlsItr = stageElement.getChildrenWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                              AppFactoryConstants.PROJECT_DEPLOYMENT_SERVER_URL));
                List<String> deploymentServerUrls = new ArrayList<String>();
                while (deploymentServerUrlsItr.hasNext()) {
                    OMElement deploymentServerUrlElement = (OMElement) deploymentServerUrlsItr.next();
                    deploymentServerUrls.add(deploymentServerUrlElement.getText().trim());
                }
                config.addDeploymentServerUrls(stageName, deploymentServerUrls);
            }
        }


    }


    private static void setWebServiceConfigs(OMElement element, AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement webServiceEndPoints = element.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                AppFactoryConstants.WEB_SERVICE_CONFIG_ROOT_ELEMENT));
        if (webServiceEndPoints == null) {
            handleError("Web service endpoint is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            OMElement getRolesOfUserApplication = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                                  AppFactoryConstants.WEB_SERVICE_CONFIG_GET_ROLES_OF_USER_FOR_PROJECT));
            if (getRolesOfUserApplication == null) {
                handleError("GetRolesOfUserApplication web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRGetRolesOfUserForApplication(getRolesOfUserApplication.getText().trim());
            }

            OMElement createRepo = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                       AppFactoryConstants.WEB_SERVICE_CONFIG_CREATE_REPO));
            if (createRepo == null) {
                handleError("CreateRepo web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRCreateRepo(createRepo.getText().trim());
            }

            OMElement getUsersOfApplication = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                              AppFactoryConstants.WEB_SERVICE_CONFIG_GET_USERS_OF_PROJECT));
            if (getUsersOfApplication == null) {
                handleError("getUsersOfApplication web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRGetUsersOfApplication(getUsersOfApplication.getText().trim());
            }
            OMElement emailVerification = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                              AppFactoryConstants.WEB_SERVICE_CONFIG_EMAIL_VERIFICATION_SERVICE));
            if (emailVerification == null) {
                handleError("emailVerification web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPREmailVerificationService(emailVerification.getText().trim());
            }
            OMElement addUserToApplication = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                             AppFactoryConstants.WEB_SERVICE_CONFIG_ADD_USER_TO_PROJECT));
            if (addUserToApplication == null) {
                handleError("addUserToApplication web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRAddUserToApplication(addUserToApplication.getText().trim());
            }
            OMElement createApplication = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                          AppFactoryConstants.WEB_SERVICE_CONFIG_CREATE_PROJECT));
            if (createApplication == null) {
                handleError("addUserToApplication web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRCreateApplication(createApplication.getText().trim());
            }
            OMElement createUser = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                       AppFactoryConstants.WEB_SERVICE_CONFIG_CREATE_USER));
            if (createUser == null) {
                handleError("addUserToApplication web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRCreateUser(createUser.getText().trim());
            }
            OMElement activateUser = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                         AppFactoryConstants.WEB_SERVICE_CONFIG_ACTIVATE_USER));
            if (activateUser == null) {
                handleError("activateUser web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRActivateUser(activateUser.getText().trim());
            }
            OMElement getAllApps = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                         AppFactoryConstants.WEB_SERVICE_CONFIG_GET_ALL_APPS));
            if (getAllApps == null) {
                handleError("getAllApp web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRGetAllApps(getAllApps.getText().trim());
            }
            OMElement getAuthCookie = webServiceEndPoints.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                       AppFactoryConstants.WEB_SERVICE_CONFIG_GET_AUTH_COOKIE));
            if (getAuthCookie == null) {
                handleError("getAuthCookie web service end point is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setWebServiceEPRGetAuthCookie(getAuthCookie.getText().trim());
            }
        }
    }

    private static void setSSOConfigs(OMElement element, AppFactoryConfiguration config)
            throws AppFactoryException {
        OMElement ssoRelyingParty = element.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                            AppFactoryConstants.SSO_CONFIG_ROOT_ELEMENT));
        if (ssoRelyingParty == null) {
            handleError("SSO Relying Party is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        } else {
            OMElement name = ssoRelyingParty.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                             AppFactoryConstants.SSO_CONFIG_NAME));
            if (name == null) {
                handleError("SSO relying party name is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setSsoRelyingPartyName(name.getText().trim());
            }

            OMElement identityProviderUrl = ssoRelyingParty.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                            AppFactoryConstants.SSO_CONFIG_IDENTITY_PROVIDER_URL));
            if (identityProviderUrl == null) {
                handleError("SSO identity provider url is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setSsoIdentityProviderEpr(identityProviderUrl.getText().trim());
            }

            OMElement keyStore = ssoRelyingParty.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                 AppFactoryConstants.SSO_CONFIG_KEY_STORE_NAME));
            if (keyStore == null) {
                handleError("SSO key store name is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setSsoKeyStoreName(keyStore.getText().trim());
            }

            OMElement keyStorePassword = ssoRelyingParty.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                                         AppFactoryConstants.SSO_CONFIG_KEY_STORE_PASSWORD));
            if (keyStorePassword == null) {
                handleError("SSO key store password is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setSsoKeyStorePassword(keyStorePassword.getText().trim());
            }

            OMElement alias = ssoRelyingParty.getFirstChildWithName(new QName(AppFactoryConstants.APPFACTORY_CONFIG_NAMESPACE,
                                                                              AppFactoryConstants.SSO_CONFIG_IDENTITY_ALIAS));
            if (alias == null) {
                handleError("SSO identity alias is not configured in " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
            } else {
                config.setSsoIdentityAlias(alias.getText().trim());
            }
        }


    }

    private static OMElement loadAppFactoryXML() throws AppFactoryException {
        String fileLocation = new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath())
                .append(RegistryConstants.PATH_SEPARATOR).
                        append(AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME).toString();

        File configFile = new File(fileLocation);
        InputStream inputStream = null;
        OMElement configXMLFile = null;
        try {
            inputStream = new FileInputStream(configFile);
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            configXMLFile = builder.getDocumentElement();
        } catch (FileNotFoundException ignore) {
            handleError("Unable to locate the file " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME + " at " + fileLocation, ignore);
        } catch (XMLStreamException e) {
            handleError("Error in reading " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME, e);
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

    private static void handleError(String msg, Exception e) throws AppFactoryException {
        log.error(msg, e);
        throw new AppFactoryException(msg, e);
    }

    private static void handleError(String msg) throws AppFactoryException {
        log.error(msg);
        throw new AppFactoryException(msg);
    }

}
