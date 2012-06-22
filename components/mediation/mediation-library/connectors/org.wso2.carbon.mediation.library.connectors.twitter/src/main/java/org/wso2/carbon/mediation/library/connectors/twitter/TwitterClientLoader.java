package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.synapse.MessageContext;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 3/6/12
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterClientLoader {

    private MessageContext messageContext;


    public TwitterClientLoader(MessageContext ctxt) {
        this.messageContext = ctxt;
    }

    public Twitter loadApiClient() throws TwitterException {
        Twitter twitter;
        if (messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_KEY) != null && messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_SECRET) != null &&
                messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN) != null && messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN_SECRET) != null) {
            ConfigurationBuilder build = new ConfigurationBuilder();
            build.setOAuthAccessToken(messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN).toString());
            build.setOAuthAccessTokenSecret(messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_ACCESS_TOKEN_SECRET).toString());
            build.setOAuthConsumerKey(messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_KEY).toString());
            build.setOAuthConsumerSecret(messageContext.getProperty(TwitterConnectConstants.TWITTER_USER_CONSUMER_SECRET).toString());
            OAuthAuthorization auth = new OAuthAuthorization(build.build());
            twitter = new TwitterFactory().getInstance(auth);
            twitter.verifyCredentials();
        } else {
            twitter = new TwitterFactory().getInstance();
        }
        return twitter;
    }

}
