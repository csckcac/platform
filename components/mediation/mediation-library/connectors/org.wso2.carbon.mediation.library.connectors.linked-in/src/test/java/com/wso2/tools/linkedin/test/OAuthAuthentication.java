package com.wso2.tools.linkedin.test;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 3/2/12
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthAuthentication {

    public static void main(String[] args) {
        String consumerKey = "4y6ifzb61ldd";
        String consumerSecret = "sy2HPyJpjPD4XvY7";
        new OAuthAuthentication().processCommandLine(consumerKey, consumerSecret);
        /*try {
            new OAuthAuthentication().submittingForm();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
    }

    private String[] getAccessTokens(String consumerKey, String consumerSecret) {

        return null;
    }

    private static void processCommandLine(String consumerKey, String consumerSecret) {

        try {
            final String consumerKeyValue = consumerKey;
            final String consumerSecretValue = consumerSecret;

            final LinkedInOAuthService oauthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(consumerKeyValue, consumerSecretValue);

            System.out.println("Fetching request token from LinkedIn...");

            LinkedInRequestToken requestToken = oauthService.getOAuthRequestToken();

            String authUrl = requestToken.getAuthorizationUrl();

            System.out.println("Request token: " + requestToken.getToken());
            System.out.println("Token secret: " + requestToken.getTokenSecret());
            System.out.println("Expiration time: " + requestToken.getExpirationTime());

            System.out.println("Now visit:\n" + authUrl
                    + "\n... and grant this app authorization");
            System.out.println("Enter the PIN code and hit ENTER when you're done:");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String pin = br.readLine();

            System.out.println("Fetching access token from LinkedIn...");

            LinkedInAccessToken accessToken = oauthService.getOAuthAccessToken(requestToken, pin);

            System.out.println("Access token: " + accessToken.getToken());
            System.out.println("Token secret: " + accessToken.getTokenSecret());

            final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(consumerKeyValue, consumerSecretValue);
            final LinkedInApiClient client = factory.createLinkedInApiClient(accessToken);

            System.out.println("Fetching profile for current user.");
            Person profile = client.getProfileForCurrentUser();
            printResult(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void printResult(Person profile) {
        System.out.println("================================");
        System.out.println("Name:" + profile.getFirstName() + " " + profile.getLastName());
        System.out.println("Headline:" + profile.getHeadline());
        System.out.println("Summary:" + profile.getSummary());
        System.out.println("Industry:" + profile.getIndustry());
        System.out.println("Picture:" + profile.getPictureUrl());
    }


}
