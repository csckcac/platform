package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
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

import java.util.EnumSet;

public class LinkedinShowHeadlineMediator extends AbstractConnector {
    private static Log log = LogFactory.getLog(LinkedinShowHeadlineMediator.class);
    public static final String ID = "id";


    public void connect() throws ConnectException {
        MessageContext messageContext = getMessageContext();
        try {
            String id = LinkedinMediatorUtils.lookupFunctionParam(messageContext, ID);
            if (id == null || "".equals(id.trim())) {
                id = "";
            }
            LinkedinMediatorUtils.setupClassLoadingForLinkedIn(LinkedinShowHeadlineMediator.class);

            final LinkedInApiClient client = new LinkedInClientLoader(messageContext).loadApiClient();

            //do the actual  client call for GetHeading
            String[] response = getHeadlineById(client, id);
            if (response != null && response.length == 2) {
                LinkedinMediatorUtils.storeResponseHeadline(messageContext, id, response[1]);
                log.info("Headline of the ID @" + id + " : " + response[0] + " - " + response[1]);
            }
        } catch (Exception e) {
            log.error("Failed to get headline: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
    }

    private String[] getHeadlineById(LinkedInApiClient client, String id) {
        String headline = "";
        String firstName = "";
        Connections connections = client.getConnectionsById(LinkedinConstants._PROFILE_ID);
        if (id.equals(LinkedinConstants._PROFILE_ID)) {
            Person person = client.getProfileById(LinkedinConstants._PROFILE_ID, EnumSet.of(ProfileField.HEADLINE, ProfileField.FIRST_NAME));
            headline = person.getHeadline();
            firstName = person.getFirstName();
        } else {
            for (Person person : connections.getPersonList()) {
                if (id.equals(person.getId())) {
                    headline = person.getHeadline();
                    firstName = person.getFirstName();
                }
            }
        }
        if (!"".equals(firstName)) {
            return new String[]{firstName, headline};
        } else {
            log.info("Profile with Profile ID " + id + "is not accessible.");
            return null;
        }
    }

}
