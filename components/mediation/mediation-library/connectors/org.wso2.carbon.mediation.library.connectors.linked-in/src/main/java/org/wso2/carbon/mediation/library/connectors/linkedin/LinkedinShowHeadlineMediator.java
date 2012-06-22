package org.wso2.carbon.mediation.library.connectors.linkedin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.schema.Connections;
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
 * Date: 2/29/12
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedinShowHeadlineMediator extends AbstractMediator implements ManagedLifecycle {

    public static final String ID = "id";

    public void init(SynapseEnvironment synapseEnvironment) {

    }

    public void destroy() {

    }

    public boolean mediate(MessageContext messageContext) {
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
                System.out.println("Headline of the ID @" + id + " : " + response[0] + " - " + response[1]);
            }
        } catch (Exception e) {
            System.out.println("Failed to get headline: " + e.getMessage());
            LinkedinMediatorUtils.storeErrorResponseStatus(messageContext, e);
        }
        return true;
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
            System.out.println("Profile with Profile ID " + id + "is not accessible.");
            return null;
        }
    }

}
