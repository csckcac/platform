package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterRetweetStatusMediator extends AbstractConnector {

    public static final String ID = "id";
    private static Log log = LogFactory.getLog(TwitterRetweetStatusMediator.class);

    public void connect() throws ConnectException {
        MessageContext messageContext = getMessageContext();
        try {
            String id = TwitterMediatorUtils.lookupFunctionParam(messageContext, ID);
            Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
            Status status = twitter.retweetStatus(Long.parseLong(id));
            TwitterMediatorUtils.storeResponseStatus(messageContext, status);
            log.info("@" + status.getUser().getScreenName() + " - " + status.getText());
        } catch (TwitterException te) {
            log.error("Failed to retweet status: " + te.getMessage(), te);
            TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
        }
        log.info("testing synapse twitter.......");
    }

}
