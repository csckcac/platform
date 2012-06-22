package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedInClientLoader;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedinMediatorUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 2/29/12
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedinSendMessageMediator extends AbstractMediator implements ManagedLifecycle {

    public static final String IDLIST = "idList";
    public static final String SUBJECT = "subject";
    public static final String MESSAGE = "message";

    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {

    }

    public boolean mediate(MessageContext messageContext) {
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

            System.out.println("Message Sent to " + idList);
        } catch (Exception e) {
            System.out.println("Failed to send message: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        return true;
    }

}
