package org.wso2.carbon.mediation.library.connectors.linkedin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 2/27/12
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedinConstants {
    private static Log log = LogFactory.getLog(LinkedinConstants.class);

    static Properties property = new Properties();

    public static final String LINKEDIN_STATUS_USER_ID = "linkedin.status.user.id";
    public static final String LINKEDIN_STATUS_STATUS = "linkedin.status.status";
    public static final String LINKEDIN_HEADLINE_USER_ID = "linkedin.headline.user.id";
    public static final String LINKEDIN_HEADLINE_HEADLINE = "linkedin.headline.headline";
    public static final String LINKEDIN_MESSAGING_ID_LIST = "linkedin.messaging.id.list";
    public static final String LINKEDIN_MESSAGING_SUBJECT = "linkedin.messaging.subject";
    public static final String LINKEDIN_USER_CONSUMER_KEY = "linkedin.user.oauth.consumerKey";
    public static final String LINKEDIN_USER_CONSUMER_SECRET = "linkedin.user.oauth.consumerSecret";
    public static final String LINKEDIN_USER_ACCESS_TOKEN = "linkedin.user.oauth.accessToken";
    public static final String LINKEDIN_USER_ACCESS_TOKEN_SECRET = "linkedin.user.oauth.accessTokenSecret";
    public static final String LINKEDIN_API_RESPONSE = "linkedin.response";

    //Linked In specific access tokens
    public static final String _CONSUMER_KEY;
    public static final String _CONSUMER_SECRET;
    public static final String _ACCESS_TOKEN;
    public static final String _ACCESS_TOKEN_SECRET;
    public static final String _PROFILE_ID;

    static {
        try {
            InputStream is = LinkedinConstants.class.getResourceAsStream("/linkedin-j.properties");
            property.load(is);
            is.close();
        } catch (IOException e) {
            log.error("Error while reading from properties file \n" + e.getMessage());
        }
        //set default access tokens/secrets at startup
        _CONSUMER_KEY = property.getProperty("oauth.consumerKey");
        _CONSUMER_SECRET = property.getProperty("oauth.consumerSecret");
        _ACCESS_TOKEN = property.getProperty("oauth.accessToken");
        _ACCESS_TOKEN_SECRET = property.getProperty("oauth.accessTokenSecret");
        _PROFILE_ID = property.getProperty("profile.id");
    }

}
