package com.wso2.tools.linkedin.test;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import org.wso2.carbon.mediation.library.connectors.linkedin.LinkedinConstants;

import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 2/23/12
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class PostStatus {


    public static void main(String[] args) {

/*
        String consumerKey = "xal849s4rfqw";
        String consumerSecret = "uK2AKiDK61eZVAiZ";
        String accessToken = "e1b2d24c-4721-4557-b128-0a23b138bf05";
        String accessTokenSecret = "0be2ebca-299e-40ca-abf1-3bd74192fb01";
        new PostStatus().processCommandLine(consumerKey, consumerSecret, accessToken, accessTokenSecret);
*/

        String consumerKey = "4y6ifzb61ldd";
        String consumerSecret = "sy2HPyJpjPD4XvY7";
        String accessToken = "5744a0c8-83e0-4108-9ac4-e52b38c8e38b";
        String accessTokenSecret = "6aac2038-2b8a-4bc3-a515-b26ec191a18c";
        new PostStatus().processCommandLine(consumerKey, consumerSecret, accessToken, accessTokenSecret);

    }

    private void processCommandLine(String ck, String cs, String at, String ats) {

        final String consumerKeyValue = ck;
        final String consumerSecretValue = cs;
        final String accessTokenValue = at;
        final String tokenSecretValue = ats;

        final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(consumerKeyValue, consumerSecretValue);
        final LinkedInApiClient client = factory.createLinkedInApiClient(accessTokenValue, tokenSecretValue);

       /* String id = "68jAFXz_cb";
        String[] response = getHeadlineById(client, id);
        System.out.println(id + " : " + response[0] + " - " + response[1]);*/

//        Person profile = client.getProfileForCurrentUser();
//        Person profile = client.getProfileByUrl("http://lk.linkedin.com/pub/udayanga-wickramasinghe/48/a26/a3a", ProfileType.PUBLIC);

       // Person profile = client.getProfileById("68jAFXz_cb", EnumSet.allOf(ProfileField.class));
//        printResult(profile);

        Connections connections = client.getConnectionsById("EHKY3OQf7x");
        printConnections(connections);

        /*String subject = "test";
        String message = "test123";
        List<String> idList=new ArrayList<String>();
        idList.add("K36UCJKL6d");
        idList.add("940LTIxtaX");
        client.sendMessage(idList, subject, message);*/
        //System.out.println("Your status has been posted. Check the LinkedIn site for confirmation.");

    }

    private static void printResult(Person profile) {
        System.out.println("================================");
        System.out.println("Name:" + profile.getFirstName() + " " + profile.getLastName());
        System.out.println("Headline:" + profile.getHeadline());
        System.out.println("Summary:" + profile.getSummary());
        System.out.println("Industry:" + profile.getIndustry());
        System.out.println("Picture:" + profile.getPictureUrl());
        System.out.println("Profile ID:" + profile.getId());
        System.out.println("Current status:" + profile.getCurrentStatus());
        /*List<Position> list = profile.getPositions().getPositionList();
        Iterator<Position> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.println("Position:" + iterator.next().getTitle());
        }*/

    }

    private static void printConnections(Connections connections) {
        System.out.println("================================");
        System.out.println("Total connections fetched:" + connections.getTotal());
        for (Person person : connections.getPersonList()) {
            System.out.println(person.getId() + ":" + person.getFirstName() + " " + person.getLastName() + ":" + person.getHeadline());
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
        return new String[]{firstName, headline};
    }


}
