package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.schema.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedInClientLoader;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedinMediatorUtils;
import org.apache.synapse.MessageContext;

import java.util.EnumSet;


public class LinkedinPostStatusMediator extends AbstractConnector {
    private static Log log = LogFactory.getLog(LinkedinPostStatusMediator.class);

    public static final String STATUS = "status";


    public void connect() throws ConnectException {
        MessageContext messageContext = getMessageContext();
        try {
            String statusStr = LinkedinMediatorUtils.lookupFunctionParam(messageContext, STATUS);
            if (statusStr == null || "".equals(statusStr.trim())) {
                statusStr = "";
            }
            LinkedinMediatorUtils.setupClassLoadingForLinkedIn(LinkedinPostStatusMediator.class);

            final LinkedInApiClient client = new LinkedInClientLoader(messageContext).loadApiClient();

            //do the actual  client call for Post
            client.updateCurrentStatus(statusStr);
            Person profile = client.getProfileById(LinkedinConstants._PROFILE_ID, EnumSet.of(ProfileField.ID, ProfileField.CURRENT_STATUS));
            LinkedinMediatorUtils.storeResponseStatus(messageContext, profile);

            log.info("Status updated : @" + profile.getId() + " - " + profile.getCurrentStatus());
        } catch (Exception e) {
            log.info("Failed to post status: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
    }
}

