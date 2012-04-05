package org.wso2.carbon.api.handler.throttle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.api.handler.throttle.rolebase.AuthenticatorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CarbonAPIThrottleConstants {

    static Log log = LogFactory.getLog(CarbonAPIThrottleConstants.class);

    static Properties property = new Properties();


    public static String _OAUTH_HEADERS_SPLITTER /*= ","*/;
    public static String _OAUTH_CONSUMER_KEY /*= "oauth_consumer_key"*/;
    public static String _OAUTH_CONSUMER_KEY_SEGMENT_DELIMETER /*= =*/;
    public static String _O_AUTH_HEADER /*= "Authorization"*/; /*= "OAuth"*/;
    public static String _AUTH_FACTORY /*= "org.wso2.carbon.api.handler.throttle.rolebase.factory.BasicOAuthAuthenticatorFactory"*/;
    public static String _AUTH_ADMIN_SERVER /*= "org.wso2.carbon.api.handler.throttle.rolebase.factory.BasicOAuthAuthenticatorFactory"*/;
    public static String _AUTH_ADMIN_PORT /*= "org.wso2.carbon.api.handler.throttle.rolebase.factory.BasicOAuthAuthenticatorFactory"*/;
    public static String _AUTH_ADMIN_TRUST_STORE_TYPE;

    public static String _AUTH_ADMIN_TRUST_STORE_PASS ;

    public static String _AUTH_ADMIN_TRUST_STORE /*= "org.wso2.carbon.api.handler.throttle.rolebase.factory.BasicOAuthAuthenticatorFactory"*/;

    public static AuthenticatorFactory _DEFAULT_AUTH_FACTORY /*= "org.wso2.carbon.api.handler.throttle.rolebase.factory.BasicOAuthAuthenticatorFactory"*/;

    //property values
    private static final String PROP_CARBON_API_THROTTLE_OAUTH_HEADER = "carbon.api.throttle.oauth.header";

    private static final String PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_SEGMENT = "carbon.api.throttle.oauth.consumer-key.header.segment";

    private static final String PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_DELIMITER = "carbon.api.throttle.oauth.headers.delimiter";

    private static final String PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_SEGMENT_DELIMITER = "carbon.api.throttle.oauth.consumer-key.header.segment.delimeter";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_FACTORY = "carbon.api.throttle.authentication.factory";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_SERVER = "carbon.api.throttle.authentication.admin.server";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_PORT = "carbon.api.throttle.authentication.admin.port";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE = "carbon.api.throttle.authentication.admin.truststore";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE_TYPE = "carbon.api.throttle.authentication.admin.truststore.type";

    private static final String PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE_PASS = "carbon.api.throttle.authentication.admin.truststore.pass";

    static {
        try {
            InputStream is = CarbonAPIThrottleConstants.class.getResourceAsStream("/carbon-throttle-api.properties");
            property.load(is);
            is.close();
        } catch (IOException e) {
            log.error("Error while reading from properties file \n" + e.getMessage());
        }
        _O_AUTH_HEADER = property.getProperty(PROP_CARBON_API_THROTTLE_OAUTH_HEADER);
        _OAUTH_CONSUMER_KEY = property.getProperty(PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_SEGMENT);
        _OAUTH_CONSUMER_KEY_SEGMENT_DELIMETER = property.getProperty(PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_SEGMENT_DELIMITER);
        _OAUTH_HEADERS_SPLITTER = property.getProperty(PROP_CARBON_API_THROTTLE_OAUTH_CONSUMER_KEY_HEADER_DELIMITER);
        _AUTH_FACTORY = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_FACTORY);
        _AUTH_FACTORY = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_FACTORY);

        try {
            if (_AUTH_FACTORY != null && !"".equals(_AUTH_FACTORY.trim())) {
                _DEFAULT_AUTH_FACTORY = (AuthenticatorFactory) Class.forName(_AUTH_FACTORY).newInstance();
            }
        } catch (Exception e) {
            log.warn("unable to create factory instnace for authentication handling for class: " +
                     _AUTH_FACTORY + " Error :" + e.getMessage());
        }
        _AUTH_ADMIN_SERVER = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_SERVER);
        _AUTH_ADMIN_PORT = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_PORT);
        _AUTH_ADMIN_TRUST_STORE = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE);
        _AUTH_ADMIN_TRUST_STORE_TYPE = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE_TYPE);
        _AUTH_ADMIN_TRUST_STORE_PASS = property.getProperty(PROP_CARBON_API_THROTTLE_AUTHENTICATION_ADMIN_TRUST_STORE_PASS);

    }

}
