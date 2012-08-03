package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

public class TwitterLoginUserMediator extends AbstractConnector {

    public static final String CONSUMER_KEY = "oauth.consumerKey";
    public static final String CONSUMER_SECRET = "oauth.consumerSecret";
    public static final String ACCESS_TOKEN = "oauth.accessToken";
    public static final String ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret";

    private static Log log = LogFactory.getLog(TwitterLoginUserMediator.class);

    @Override
    public void connect() throws ConnectException {
        MessageContext messageContext = null;
        try {
            messageContext = getMessageContext();
            String consumerKey = TwitterMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_KEY);
            String consumerSecret = TwitterMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_SECRET);
            String accessToken = TwitterMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN);
            String accessTokenSecret = TwitterMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN_SECRET);
            TwitterMediatorUtils.storeLoginUser(messageContext, consumerKey, consumerSecret, accessToken, accessTokenSecret);
            log.info("User registered");
        } catch (Exception e) {
            log.error("Failed to login user: " + e.getMessage(), e);
            TwitterMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        log.info("testing synapse twitter.......");
    }
}
