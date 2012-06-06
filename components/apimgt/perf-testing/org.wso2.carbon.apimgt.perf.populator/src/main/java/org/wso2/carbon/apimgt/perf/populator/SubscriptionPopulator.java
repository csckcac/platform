package org.wso2.carbon.apimgt.perf.populator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionPopulator {

	public static void main(String[] args) {

		/*String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost:3306/am2    ";
		//String DB_URL = "jdbc:h2:~/am";
		String USER = "root";
		String PASS = "root123";*/

        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/newam";
        String USER = "root";
        String PASS = "root123";

		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);

			String INSERT_APPLICATIONS_SQL = "INSERT INTO AM_APPLICATION (NAME,SUBSCRIBER_ID) VALUES (?,?)";
            String INSERT_API_SQL = "INSERT INTO AM_API (API_PROVIDER, API_NAME, API_VERSION, CONTEXT) VALUES (?,?,?,?)";
			String INSERT_SUBSCRIPTIONS_SQL = "INSERT INTO AM_SUBSCRIPTION ( TIER_ID , API_ID , APPLICATION_ID) VALUES (?,?,?)";
			String INSERT_OAUTH2_ACCESS_TOKEN_SQL = "INSERT INTO IDENTITY_OAUTH2_ACCESS_TOKEN ( ACCESS_TOKEN ,AUTHZ_USER , CONSUMER_KEY , REFRESH_TOKEN ,TIME_CREATED , TOKEN_SCOPE , TOKEN_STATE ,VALIDITY_PERIOD ) "
					+ "VALUES (?,NULL,?,NULL,NULL,'PRODUCTION','ACTIVE',-1)";
			String INSERT_SUBSCRIPTION_KEY_MAPPING_SQL = "INSERT INTO AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, ACCESS_TOKEN, KEY_TYPE) VALUES (?,?,?)";
			String INSERT_CONSUMER_APPLICATION_SQL = "INSERT INTO IDENTITY_OAUTH_CONSUMER_APPLICATIONS ( APP_NAME , CALLBACK_URL , CONSUMER_KEY , CONSUMER_SECRET ,OAUTH_VERSION"
					+ " , TENANT_ID , USERNAME ) VALUES (NULL,NULL,?,?,'2.0',0,'admin')";
			String INSERT_SUBSCRIBER_SQL = "INSERT INTO AM_SUBSCRIBER  (USER_ID ,TENANT_ID,DATE_SUBSCRIBED ) VALUES(?,?,NOW())";

			PreparedStatement psApplications = conn.prepareStatement(
					INSERT_APPLICATIONS_SQL,
					PreparedStatement.RETURN_GENERATED_KEYS);
			PreparedStatement psSubscriptions = conn.prepareStatement(
					INSERT_SUBSCRIPTIONS_SQL,
					PreparedStatement.RETURN_GENERATED_KEYS);
			PreparedStatement psAPIs = conn.prepareStatement(
                    INSERT_API_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement psOAuth2AccessTokenSQL = conn
					.prepareStatement(INSERT_OAUTH2_ACCESS_TOKEN_SQL);
			PreparedStatement psSubscriptionKeyMappingSQL = conn
					.prepareStatement(INSERT_SUBSCRIPTION_KEY_MAPPING_SQL);
			PreparedStatement psConsumerApplicationSQL = conn
					.prepareStatement(INSERT_CONSUMER_APPLICATION_SQL);
			PreparedStatement psSubscriberSQL = conn
					.prepareStatement(INSERT_SUBSCRIBER_SQL);

			String applicationNamePrefix = "app";

			// Add Subscriber
			psSubscriberSQL.setString(1, "perf1");
			psSubscriberSQL.setInt(2, 0);
			psSubscriberSQL.executeUpdate();
			ResultSet rs = psSubscriberSQL.getGeneratedKeys();
			int subscriberId = 0;
            int id;
			if (rs.next()) {
				subscriberId = rs.getInt(1);

			}
			rs.close();

            psAPIs.setString(1, "admin");
            psAPIs.setString(2, "StockTicker");
            psAPIs.setString(3, "1.0.0");
            psAPIs.setString(4, "/ticker");
            psAPIs.executeUpdate();
            rs = psAPIs.getGeneratedKeys();
            int apiId = -1;
            if (rs.next()) {
                apiId = rs.getInt(1);
            }
            rs.close();

			int loopCount = 100000;
			String applicationName;
			for (int i = 0; i < loopCount; i++) {

				applicationName = applicationNamePrefix + i;
				// Add Application
				psApplications.setString(1, applicationName);
				psApplications.setInt(2, Integer.valueOf(subscriberId));
				psApplications.executeUpdate();
				rs = psApplications.getGeneratedKeys();
				int applicationId = 0;
				if (rs.next()) {
					applicationId = rs.getInt(1);
				}
				rs.close();

				// Add Subscription
				psSubscriptions.setString(1, "Unlimited");
				psSubscriptions.setInt(2, apiId);
				psSubscriptions.setInt(3, Integer.valueOf(applicationId));
				psSubscriptions.executeUpdate();
				rs = psSubscriptions.getGeneratedKeys();
				int subscriptionId = 0;
				if (rs.next()) {
					subscriptionId = rs.getInt(1);
				}
				rs.close();

				// Add Key Context Mapping
				String accessKey = "9nEQnijLZ0Gi0gZ6a3pZIC" + i;

				// Add Subscription Key Mapping
				psSubscriptionKeyMappingSQL.setInt(1,
						Integer.valueOf(subscriptionId));
				psSubscriptionKeyMappingSQL.setString(2, accessKey);
				psSubscriptionKeyMappingSQL.setString(3, "PRODUCTION");
				psSubscriptionKeyMappingSQL.executeUpdate();

				// Add Consumer Application
				psConsumerApplicationSQL.setString(1, applicationName);
				psConsumerApplicationSQL.setString(2, applicationName);
				psConsumerApplicationSQL.executeUpdate();

				// Add OAuth2 access token
				psOAuth2AccessTokenSQL.setString(1, accessKey);
				psOAuth2AccessTokenSQL.setString(2, applicationName);
				psOAuth2AccessTokenSQL.executeUpdate();

				psApplications.clearParameters();
				psSubscriptions.clearParameters();
				psOAuth2AccessTokenSQL.clearParameters();
				psSubscriptionKeyMappingSQL.clearParameters();
				psConsumerApplicationSQL.clearParameters();
				conn.commit();
                System.out.println("Iteration: " + i + " completed");
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
