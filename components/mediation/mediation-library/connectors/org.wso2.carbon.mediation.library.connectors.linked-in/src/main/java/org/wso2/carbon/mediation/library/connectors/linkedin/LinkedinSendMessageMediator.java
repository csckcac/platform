package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedInClientLoader;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedinMediatorUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.Arrays;

public class LinkedinSendMessageMediator extends AbstractConnector {

    private static Log log = LogFactory.getLog(LinkedinSendMessageMediator.class);

    public static final String IDLIST = "idList";
    public static final String SUBJECT = "subject";
    public static final String MESSAGE = "message";

    public void connect() throws ConnectException {
        MessageContext messageContext = getMessageContext();
        try {
            String idList = LinkedinMediatorUtils.lookupFunctionParam(messageContext, IDLIST);
            String subject = LinkedinMediatorUtils.lookupFunctionParam(messageContext, SUBJECT);
            String message = LinkedinMediatorUtils.lookupFunctionParam(messageContext, MESSAGE);
            if (idList == null || "".equals(idList.trim())) {
                idList = "";
            }
            LinkedinMediatorUtils.setupClassLoadingForLinkedIn(LinkedinSendMessageMediator.class);

            final LinkedInApiClient client = new LinkedInClientLoader(messageContext).loadApiClient();

            //do the actual  client call for Message Sending
            client.sendMessage(Arrays.asList(idList.split(",")), subject, message);
            LinkedinMediatorUtils.storeResponseMessaging(messageContext, idList, subject);

            log.info("Message Sent to " + idList);
        } catch (Exception e) {
            log.error("Failed to send message: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
    }

}
