/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.config;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.OAuthConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class OAuthServerConfiguration {

    private static Log log = LogFactory.getLog(OAuthServerConfiguration.class);

    private static final String CONFIG_ELEM_OAUTH = "OAuth";
    private static final String CONFIG_ELEM_OAUTH_CALLBACK_HANDLERS =
            "OAuthCallbackHandlers";
    private static final String CONFIG_ELEM_OAUTH_CALLBACK_HANDLER =
            "OAuthCallbackHandler";
    private static final String CONFIG_ELEM_AUTHZ_CODE_DEFAULT_TIMEOUT =
            "AuthorizationCodeDefaultValidityPeriod";
    private static final String CONFIG_ELEM_ACCESS_TOK_DEFAULT_TIMEOUT =
            "AccessTokenDefaultValidityPeriod";
    public static final String CONFIG_ELEM_DEF_TIMESTAMP_SKEW = "TimestampSkew";
    private static final String CONFIG_ATTR_CLASS = "Class";
    private static final String CONFIG_ELEM_PRIORITY = "Priority";
    private static final String CONFIG_ELEM_PROPERTIES = "Properties";
    private static final String CONFIG_ELEM_PROPERTY = "Property";
    private static final String CONFIG_ATTR_NAME = "Name";

    private static OAuthServerConfiguration instance;

    private long defaultAuthorizationCodeValidityPeriodInSeconds = 300;

    private long defaultAccessTokenValidityPeriodInSeconds = 3600;

    private long defaultTimeStampSkewInSeconds = 300;

    private Set<OAuthCallbackHandlerMetaData> callbackHandlerMetaData =
            new HashSet<OAuthCallbackHandlerMetaData>();


    private OAuthServerConfiguration() {
        buildOAuthServerConfiguration();
    }

    public static OAuthServerConfiguration getInstance() {
        CarbonUtils.checkSecurity();
        if (instance == null) {
            synchronized (OAuthServerConfiguration.class) {
                if (instance == null) {
                    instance = new OAuthServerConfiguration();
                }
            }
        }
        return instance;
    }

    public Set<OAuthCallbackHandlerMetaData> getCallbackHandlerMetaData() {
        return callbackHandlerMetaData;
    }

    public long getDefaultAuthorizationCodeValidityPeriodInSeconds() {
        return defaultAuthorizationCodeValidityPeriodInSeconds;
    }

    public long getDefaultAccessTokenValidityPeriodInSeconds() {
        return defaultAccessTokenValidityPeriodInSeconds;
    }

    public long getDefaultTimeStampSkewInSeconds() {
        return defaultTimeStampSkewInSeconds;
    }

    private void buildOAuthServerConfiguration() {
        try {
            IdentityConfigParser configParser = IdentityConfigParser.getInstance();
            OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

            if (oauthElem == null) {
                warnOnFaultyConfiguration("OAuth element is not available.");
                return;
            }

            // read callback handler configurations
            parseOAuthCallbackHandlers(oauthElem.getFirstChildWithName(
                    new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,
                            CONFIG_ELEM_OAUTH_CALLBACK_HANDLERS)));

            // read default timeout periods
            parseDefaultValidityPeriods(oauthElem);

        } catch (ServerConfigurationException e) {
            log.error("Error when reading the OAuth Configurations. " +
                    "OAuth related functionality might be affected.", e);
        }
    }

    private void parseOAuthCallbackHandlers(OMElement callbackHandlersElem) {
        if (callbackHandlersElem == null) {
            warnOnFaultyConfiguration("AuthorizationCallbackHandlers element is not available.");
            return;
        }

        Iterator callbackHandlers = callbackHandlersElem.getChildrenWithLocalName(
                CONFIG_ELEM_OAUTH_CALLBACK_HANDLER);
        int callbackHandlerCount = 0;
        if (callbackHandlers != null) {
            for (; callbackHandlers.hasNext(); ) {
                OAuthCallbackHandlerMetaData cbHandlerMetadata =
                        buildAuthzCallbackHandlerMetadata((OMElement) callbackHandlers.next());
                if (cbHandlerMetadata != null) {
                    callbackHandlerMetaData.add(cbHandlerMetadata);
                    if (log.isDebugEnabled()) {
                        log.debug("OAuthAuthorizationCallbackHandleMetadata was added. Class : "
                                + cbHandlerMetadata.getClassName());
                    }
                    callbackHandlerCount++;
                }
            }
        }
        // if no callback handlers are registered, print a WARN
        if (!(callbackHandlerCount > 0)) {
            warnOnFaultyConfiguration("No AuthorizationCallbackHandler elements were found.");
        }
    }

    private static void warnOnFaultyConfiguration(String logMsg) {
        log.warn("Error in OAuth Configuration. " + logMsg);
    }

    private static OAuthCallbackHandlerMetaData buildAuthzCallbackHandlerMetadata(
            OMElement omElement) {
        // read the class attribute which is mandatory
        String className = omElement.getAttributeValue(new QName(CONFIG_ATTR_CLASS));

        if (className == null) {
            log.error("Mandatory attribute \"Class\" is not present in the " +
                    "AuthorizationCallbackHandler element. " +
                    "AuthorizationCallbackHandler will not be registered.");
            return null;
        }

        // read the priority element, if it is not there, use the default priority of 1
        int priority = OAuthConstants.OAUTH_AUTHZ_CB_HANDLER_DEFAULT_PRIORITY;
        OMElement priorityElem = omElement.getFirstChildWithName(new QName(
                IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, CONFIG_ELEM_PRIORITY));
        if (priorityElem != null) {
            priority = Integer.parseInt(priorityElem.getText());
        }

        if (log.isDebugEnabled()) {
            log.debug("Priority level of : " + priority + " is set for the " +
                    "AuthorizationCallbackHandler with the class : " + className);
        }

        // read the additional properties.
        OMElement paramsElem = omElement.getFirstChildWithName(new QName(
                IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, CONFIG_ELEM_PROPERTIES));
        Properties properties = null;
        if (paramsElem != null) {
            Iterator paramItr = paramsElem.getChildrenWithLocalName(CONFIG_ELEM_PROPERTY);
            properties = new Properties();
            if (log.isDebugEnabled()) {
                log.debug("Registering Properties for AuthorizationCallbackHandler class : "
                        + className);
            }
            for (; paramItr.hasNext(); ) {
                OMElement paramElem = (OMElement) paramItr.next();
                String paramName = paramElem.getAttributeValue(new QName(CONFIG_ATTR_NAME));
                String paramValue = paramElem.getText();
                properties.put(paramName, paramValue);
                if (log.isDebugEnabled()) {
                    log.debug("Property name : " + paramName + ", Property Value : " + paramValue);
                }
            }
        }
        return new OAuthCallbackHandlerMetaData(className, properties, priority);
    }

    private void parseDefaultValidityPeriods(OMElement oauthConfigElem) {
        // set the authorization code default timeout
        OMElement authzCodeTimeoutElem = oauthConfigElem.getFirstChildWithName(
                new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,
                        CONFIG_ELEM_AUTHZ_CODE_DEFAULT_TIMEOUT));
        if (authzCodeTimeoutElem != null) {
            defaultAuthorizationCodeValidityPeriodInSeconds = Long.parseLong(authzCodeTimeoutElem.getText());
        }

        // set the access token default timeout
        OMElement accessTokTimeoutElem = oauthConfigElem.getFirstChildWithName(
                new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,
                        CONFIG_ELEM_ACCESS_TOK_DEFAULT_TIMEOUT));
        if (accessTokTimeoutElem != null) {
            defaultAccessTokenValidityPeriodInSeconds = Long.parseLong(accessTokTimeoutElem.getText());
        }

        OMElement timeStampSkewElem = oauthConfigElem.getFirstChildWithName(
                new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,
                        CONFIG_ELEM_DEF_TIMESTAMP_SKEW));
        if (timeStampSkewElem != null) {
            defaultTimeStampSkewInSeconds = Long.parseLong(timeStampSkewElem.getText());
        }

        if (log.isDebugEnabled()) {
            if (authzCodeTimeoutElem == null) {
                log.debug("\"Authorization Code Default Timeout\" element was not available " +
                        "in identity.xml. Continuing with the default value.");
            }
            if (accessTokTimeoutElem == null) {
                log.debug("\"Access Token Default Timeout\" element was not available " +
                        "in from identity.xml. Continuing with the default value.");
            }
            if (timeStampSkewElem == null) {
                log.debug("\"Default Timestamp Skew\" element was not available " +
                        "in from identity.xml. Continuing with the default value.");
            }
            log.debug("Authorization Code Default Timeout is set to : " +
                    defaultAuthorizationCodeValidityPeriodInSeconds + "ms.");
            log.debug("Access Token Default Timeout is set to " +
                    defaultAccessTokenValidityPeriodInSeconds + "ms.");
            log.debug("Default TimestampSkew is set to " +
                    defaultTimeStampSkewInSeconds + "ms.");
        }
    }
}
