package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.schema.Person;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedInClientLoader;
import org.wso2.carbon.mediation.library.connectors.linkedin.util.LinkedinMediatorUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 2/21/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */

public class LinkedinPostStatusMediator extends AbstractMediator implements ManagedLifecycle {

    public static final String STATUS = "status";

    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {

    }

    public boolean mediate(MessageContext messageContext) {
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

            System.out.println("Status updated : @" + profile.getId() + " - " + profile.getCurrentStatus());
        } catch (Exception e) {
            System.out.println("Failed to post status: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        return true;
    }
}

