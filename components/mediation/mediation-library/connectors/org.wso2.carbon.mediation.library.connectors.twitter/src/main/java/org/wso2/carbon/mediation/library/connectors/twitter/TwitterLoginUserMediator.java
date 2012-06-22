package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 3/6/12
 * Time: 10:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterLoginUserMediator extends AbstractMediator implements ManagedLifecycle {

    public static final String CONSUMER_KEY = "oauth.consumerKey";
    public static final String CONSUMER_SECRET = "oauth.consumerSecret";
    public static final String ACCESS_TOKEN = "oauth.accessToken";
    public static final String ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret";

    public void init(SynapseEnvironment synapseEnvironment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean mediate(MessageContext messageContext) {
        try {
            String consumerKey = TwitterMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_KEY);
            String consumerSecret = TwitterMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_SECRET);
            String accessToken = TwitterMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN);
            String accessTokenSecret = TwitterMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN_SECRET);
            TwitterMediatorUtils.storeLoginUser(messageContext, consumerKey, consumerSecret, accessToken, accessTokenSecret);
            System.out.println("User registered");
        } catch (Exception e) {
            System.out.println("Failed to login user: " + e.getMessage());
            TwitterMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        System.out.println("testing synapse twitter.......");
        return true;
    }
}
