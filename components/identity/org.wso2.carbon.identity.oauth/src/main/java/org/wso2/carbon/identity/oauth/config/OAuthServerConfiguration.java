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

import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.OAuthConstants;
import org.wso2.carbon.identity.oauth.preprocessor.TokenPersistencePreprocessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Runtime representation of the OAuth Configuration as configured through identity.xml
 */
public class OAuthServerConfiguration {

    private static Log log = LogFactory.getLog(OAuthServerConfiguration.class);

    private static final String CONFIG_ELEM_OAUTH = "OAuth";

    /**
     * Localpart names for the OAuth configuration in identity.xml.
     */
    private class ConfigElements {
        // Callback handler related configuration elements
        private static final String OAUTH_CALLBACK_HANDLERS = "OAuthCallbackHandlers";
        private static final String OAUTH_CALLBACK_HANDLER = "OAuthCallbackHandler";
        private static final String CALLBACK_CLASS = "Class";
        private static final String CALLBACK_PRIORITY = "Priority";
        private static final String CALLBACK_PROPERTIES = "Properties";
        private static final String CALLBACK_PROPERTY = "Property";
        private static final String CALLBACK_ATTR_NAME = "Name";

        // Default timestamp skew
        public static final String TIMESTAMP_SKEW = "TimestampSkew";

        // Default validity periods
        private static final String AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD =
                "AuthorizationCodeDefaultValidityPeriod";
        private static final String ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD =
                "AccessTokenDefaultValidityPeriod";
        // Enable/Disable cache
        public static final String ENABLE_CACHE = "EnableCache";

        //TokenStoragePreprocessor
        public static final String TOKEN_PERSISTENCE_PREPROCESSOR = "TokenPersistencePreprocessor";

        //Supported Grant Types
        public static final String SUPPORTED_GRANT_TYPES = "SupportedGrantTypes";

        //Supported Response Types
        public static final String SUPPORTED_RESP_TYPES = "SupportedResponseTypes";
    }


    private static OAuthServerConfiguration instance;

    private long defaultAuthorizationCodeValidityPeriodInSeconds = 300;

    private long defaultAccessTokenValidityPeriodInSeconds = 3600;

    private long defaultTimeStampSkewInSeconds = 300;

    private boolean cacheEnabled = true;

    private String tokenPersistencePreProcessorClassName =
            "org.wso2.carbon.identity.oauth.preprocessor.PlainTokenPersistencePreprocessor";

    private TokenPersistencePreprocessor tokenPersistencePreprocessor;

    private Set<OAuthCallbackHandlerMetaData> callbackHandlerMetaData =
            new HashSet<OAuthCallbackHandlerMetaData>();

    private List<String> supportedGrantTypes = new ArrayList<String>();

    private List<String> supportedResponseTypes = new ArrayList<String>();


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
                    getQNameWithIdentityNS(ConfigElements.OAUTH_CALLBACK_HANDLERS)));

            // read default timeout periods
            parseDefaultValidityPeriods(oauthElem);

            // read caching configurations
            parseCachingConfiguration(oauthElem);

            // read token preprocessor config
            parseTokenPersistencePreProcessorConfig(oauthElem);

            // read supported grant types
            parseSupportedGrantTypesConfig(oauthElem);

            // read supported response types
            parseSupportedResponseTypesConfig(oauthElem);

        } catch (ServerConfigurationException e) {
            log.error("Error when reading the OAuth Configurations. " +
                    "OAuth related functionality might be affected.", e);
        }
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

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public List<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public List<String> getSupportedResponseTypes() {
        return supportedResponseTypes;
    }

    public TokenPersistencePreprocessor getTokenPersistencePreprocessor()
            throws IdentityOAuth2Exception {
        // create an instance of a TokenPersistencePreprocessor. This is a one time operation
        // because there can be only on OAuthServerConfiguration in a given runtime.
        if (tokenPersistencePreprocessor == null) {
            synchronized (this) {
                try {
                    // create an instance of the TokenPersistencePreprocessor
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(
                            tokenPersistencePreProcessorClassName);
                    tokenPersistencePreprocessor = (TokenPersistencePreprocessor) clazz.newInstance();

                    if (log.isDebugEnabled()) {
                        log.debug("An instance of " + tokenPersistencePreProcessorClassName +
                                " is created for OAuthServerConfiguration.");
                    }

                } catch (Exception e) {
                    String errorMsg = "Error when instantiating the TokenPersistencePreprocessor : "
                            + tokenPersistencePreProcessorClassName;
                    log.error(errorMsg, e);
                    throw new IdentityOAuth2Exception(errorMsg, e);
                }
            }
        }
        return tokenPersistencePreprocessor;
    }

    private void parseOAuthCallbackHandlers(OMElement callbackHandlersElem) {
        if (callbackHandlersElem == null) {
            warnOnFaultyConfiguration("AuthorizationCallbackHandlers element is not available.");
            return;
        }

        Iterator callbackHandlers = callbackHandlersElem.getChildrenWithLocalName(
                ConfigElements.OAUTH_CALLBACK_HANDLER);
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

    private void warnOnFaultyConfiguration(String logMsg) {
        log.warn("Error in OAuth Configuration. " + logMsg);
    }

    private OAuthCallbackHandlerMetaData buildAuthzCallbackHandlerMetadata(
            OMElement omElement) {
        // read the class attribute which is mandatory
        String className = omElement.getAttributeValue(new QName(ConfigElements.CALLBACK_CLASS));

        if (className == null) {
            log.error("Mandatory attribute \"Class\" is not present in the " +
                    "AuthorizationCallbackHandler element. " +
                    "AuthorizationCallbackHandler will not be registered.");
            return null;
        }

        // read the priority element, if it is not there, use the default priority of 1
        int priority = OAuthConstants.OAUTH_AUTHZ_CB_HANDLER_DEFAULT_PRIORITY;
        OMElement priorityElem = omElement.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.CALLBACK_PRIORITY));
        if (priorityElem != null) {
            priority = Integer.parseInt(priorityElem.getText());
        }

        if (log.isDebugEnabled()) {
            log.debug("Priority level of : " + priority + " is set for the " +
                    "AuthorizationCallbackHandler with the class : " + className);
        }

        // read the additional properties.
        OMElement paramsElem = omElement.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.CALLBACK_PROPERTIES));
        Properties properties = null;
        if (paramsElem != null) {
            Iterator paramItr = paramsElem.getChildrenWithLocalName(
                    ConfigElements.CALLBACK_PROPERTY);
            properties = new Properties();
            if (log.isDebugEnabled()) {
                log.debug("Registering Properties for AuthorizationCallbackHandler class : "
                        + className);
            }
            for (; paramItr.hasNext(); ) {
                OMElement paramElem = (OMElement) paramItr.next();
                String paramName = paramElem.getAttributeValue(
                        new QName(ConfigElements.CALLBACK_ATTR_NAME));
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
                getQNameWithIdentityNS(ConfigElements.AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD));

        if (authzCodeTimeoutElem != null) {
            defaultAuthorizationCodeValidityPeriodInSeconds = Long.parseLong(authzCodeTimeoutElem.getText());
        }

        // set the access token default timeout
        OMElement accessTokTimeoutElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD));
        if (accessTokTimeoutElem != null) {
            defaultAccessTokenValidityPeriodInSeconds = Long.parseLong(accessTokTimeoutElem.getText());
        }

        OMElement timeStampSkewElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.TIMESTAMP_SKEW));
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

    private void parseCachingConfiguration(OMElement oauthConfigElem) {
        OMElement enableCacheElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.ENABLE_CACHE));
        if (enableCacheElem != null) {
            cacheEnabled = Boolean.parseBoolean(enableCacheElem.getText());
        }

        if (log.isDebugEnabled()) {
            log.debug("Enable OAuth Cache was set to : " + cacheEnabled);
        }
    }

    private void parseTokenPersistencePreProcessorConfig(OMElement oauthConfigElem) {
        OMElement preprocessorConfigElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.TOKEN_PERSISTENCE_PREPROCESSOR));
        if (preprocessorConfigElem != null) {
            tokenPersistencePreProcessorClassName = preprocessorConfigElem.getText();
        }

        if (log.isDebugEnabled()) {
            log.debug("Token Persistence Preprocessor was set to : "
                    + tokenPersistencePreProcessorClassName);
        }
    }

    private void parseSupportedGrantTypesConfig(OMElement oauthConfigElem) {
        OMElement supportedGrantTypesElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.SUPPORTED_GRANT_TYPES));

        // These are the default grant types supported.
        List<String> validGrantTypes = new ArrayList<String>(4);
        validGrantTypes.add(GrantType.AUTHORIZATION_CODE.toString());
        validGrantTypes.add(GrantType.CLIENT_CREDENTIALS.toString());
        validGrantTypes.add(GrantType.PASSWORD.toString());
        validGrantTypes.add(GrantType.REFRESH_TOKEN.toString());

        if (supportedGrantTypesElem != null) {
            String grantTypeStr = supportedGrantTypesElem.getText();
            if (grantTypeStr != null) {
                String[] grantTypes = grantTypeStr.split(",");
                // Check whether user provided grant types are valid
                if (grantTypes != null) {
                    for (String providedGrantType : grantTypes) {
                        providedGrantType = providedGrantType.trim();
                        if (validGrantTypes.contains(providedGrantType)) {
                            supportedGrantTypes.add(providedGrantType);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid Grant Type provided : " + providedGrantType +
                                        ". This will be ignored.");
                            }
                        }
                    }
                }
            }
        } else {
            // if this element is not present, assume the default case.
            supportedGrantTypes.addAll(validGrantTypes);
        }



        if (log.isDebugEnabled()) {
            log.debug("Supported Grant Types : " + supportedGrantTypes);
        }
    }

    private void parseSupportedResponseTypesConfig(OMElement oauthConfigElem) {
        OMElement supportedRespTypesElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.SUPPORTED_RESP_TYPES));

        // These are the default grant types supported.
        List<String> validRespTypes = new ArrayList<String>(4);
        validRespTypes.add(ResponseType.CODE.toString());
        validRespTypes.add(ResponseType.TOKEN.toString());

        if (supportedRespTypesElem != null) {
            String respTypesStr = supportedRespTypesElem.getText();
            if (respTypesStr != null) {
                String[] respTypes = respTypesStr.split(",");
                // Check whether user provided grant types are valid
                if (respTypes != null) {
                    for (String providedRespType : respTypes) {
                        providedRespType = providedRespType.trim();
                        if (validRespTypes.contains(providedRespType)) {
                            supportedResponseTypes.add(providedRespType);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid Response Type provided : " + providedRespType +
                                        ". This will be ignored.");
                            }
                        }
                    }
                }
            }
        } else {
            // if this element is not present, assume the default case.
            supportedResponseTypes.addAll(validRespTypes);
        }

        if (log.isDebugEnabled()) {
            log.debug("Supported Response Types : " + supportedResponseTypes);
        }
    }

    private QName getQNameWithIdentityNS(String localPart) {
        return new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }
}
