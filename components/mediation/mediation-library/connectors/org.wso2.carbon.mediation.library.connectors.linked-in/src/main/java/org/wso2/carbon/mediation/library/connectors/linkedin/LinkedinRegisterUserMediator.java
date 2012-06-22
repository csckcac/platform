package org.wso2.carbon.mediation.library.connectors.linkedin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedinMediatorUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 3/2/12
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedinRegisterUserMediator extends AbstractMediator implements ManagedLifecycle {
    private static Log log = LogFactory.getLog(LinkedinRegisterUserMediator.class);

    public static final String CONSUMER_KEY = "oauth.consumerKey";
    public static final String CONSUMER_SECRET = "oauth.consumerSecret";
    public static final String ACCESS_TOKEN = "oauth.accessToken";
    public static final String ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret";

    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {

    }

    public boolean mediate(MessageContext messageContext) {
        try {
            String consumerKey = LinkedinMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_KEY);
            String consumerSecret = LinkedinMediatorUtils.lookupFunctionParam(messageContext, CONSUMER_SECRET);
            String accessToken = LinkedinMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN);
            String accessTokenSecret = LinkedinMediatorUtils.lookupFunctionParam(messageContext, ACCESS_TOKEN_SECRET);

            LinkedinMediatorUtils.setupClassLoadingForLinkedIn(LinkedinRegisterUserMediator.class);
            LinkedinMediatorUtils.StoreRegisteringUser(messageContext, consumerKey, consumerSecret, accessToken, accessTokenSecret);

            log.info("User registered");
        } catch (Exception e) {
            log.info("Failed to register the user");
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        return true;
    }

}
