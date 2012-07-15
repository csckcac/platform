package org.wso2.carbon.identity.oauth;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.callback.OAuthCallbackHandlerMetaData;
import org.wso2.carbon.identity.oauth.callback.OAuthCallbackHandlerRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public final class OAuthUtil {

    public static final Log log = LogFactory.getLog(OAuthUtil.class);
    public static final String CONFIG_ELEM_OAUTH = "OAuth";
    public static final String CONFIG_ELEM_AUTHORIZATION_CALLBACK_HANDLERS =
            "AuthorizationCallbackHandlers";
    public static final String CONFIG_ELEM_AUTHORIZATION_CALLBACK_HANDLER =
            "AuthorizationCallbackHandler";
    public static final String CONFIG_ATTR_CLASS = "Class";
    public static final String CONFIG_ELEM_PRIORITY = "Priority";
    public static final String CONFIG_ELEM_PROPERTIES = "Properties";
    public static final String CONFIG_ELEM_PROPERTY = "Property";
    public static final String CONFIG_ATTR_NAME = "Name";

    /**
     * Generates a random number using two UUIDs and HMAC-SHA1
     *
     * @return generated secure random number
     * @throws IdentityOAuthAdminException Invalid Algorithm or Invalid Key
     */
    public static String getRandomNumber() throws IdentityOAuthAdminException {
        try {
            String secretKey = UUIDGenerator.generateUUID();
            String baseString = UUIDGenerator.generateUUID();

            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            String random = Base64.encode(rawHmac);
            // Registry doesn't have support for these character.
            random = random.replace("/", "_");
            random = random.replace("=", "a");
            random = random.replace("+", "f");
            return random;
        } catch (Exception e) {
            log.error("Error when generating a random number.", e);
            throw new IdentityOAuthAdminException("Error when generating a random number.", e);
        }
    }

    public static void parseAuthzCallbackHandlersConfig() throws IdentityOAuthAdminException {
        try {
            IdentityConfigParser configParser = IdentityConfigParser.getInstance();
            OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

            if (oauthElem == null) {
                warnOnFaultyConfiguration("OAuth element is not available.");
                return;
            }

            OMElement callbackHandlersElem = oauthElem.getFirstChildWithName(
                    new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,
                            CONFIG_ELEM_AUTHORIZATION_CALLBACK_HANDLERS));

            if (callbackHandlersElem == null) {
                warnOnFaultyConfiguration("AuthorizationCallbackHandlers element is not available.");
                return;
            }

            Iterator callbackHandlers = callbackHandlersElem.getChildrenWithLocalName(
                    CONFIG_ELEM_AUTHORIZATION_CALLBACK_HANDLER);
            int callbackHandlerCount = 0;
            if (callbackHandlers != null) {
                for (; callbackHandlers.hasNext(); ) {
                    OAuthCallbackHandlerMetaData cbHandlerMetadata =
                            buildAuthzCallbackHandlerMetadata((OMElement) callbackHandlers.next());
                    if (cbHandlerMetadata != null) {
                        OAuthCallbackHandlerRegistry.getInstance().
                                addOAuthAuthorizationCallbackHandlerMetadata(cbHandlerMetadata);
                        log.info("OAuthAuthorizationCallbackHandleMetadata was added. Class : "
                                + cbHandlerMetadata.getClassName());
                        callbackHandlerCount++;
                    }
                }
            }
            // if no callback handlers are registered, print a WARN
            if (!(callbackHandlerCount > 0)) {
                warnOnFaultyConfiguration("No AuthorizationCallbackHandler elements were found.");
            }

        } catch (ServerConfigurationException e) {
            log.error("Error when reading the OAuthCallbackManager Configurations.", e);
            throw new IdentityOAuthAdminException(
                    "Error when reading the OAuthCallbackManager Configurations.", e);
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
                log.debug("Registering Properties for AuthorizationCallbackHandler class : " + className);
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
}
