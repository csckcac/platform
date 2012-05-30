package org.wso2.carbon.apimgt.perf.populator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionPopulator {

	public static void main(String[] args) {

		String JDBC_DRIVER = "org.h2.Driver";
		String DB_URL = "jdbc:h2:tcp://localhost/~/am2";
		//String DB_URL = "jdbc:h2:~/am";
		String USER = "sa";
		String PASS = "password";

		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);

			String INSERT_APPLICATIONS_SQL = "INSERT INTO AM_APPLICATION (NAME,SUBSCRIBER_ID ) VALUES (?,?)";
			String INSERT_SUBSCRIPTIONS_SQL = "INSERT INTO AM_SUBSCRIPTION ( TIER_ID , API_ID , APPLICATION_ID,) VALUES (?,?,?)";
			String INSERT_KEY_CONTEXT_MAPPING_SQL = "INSERT INTO AM_KEY_CONTEXT_MAPPING ( CONTEXT , VERSION , ACCESS_TOKEN) VALUES (?,?,?)";
			String INSERT_OAUTH2_ACCESS_TOKEN_SQL = "INSERT INTO IDENTITY_OAUTH2_ACCESS_TOKEN ( ACCESS_TOKEN ,AUTHZ_USER , CONSUMER_KEY , REFRESH_TOKEN ,TIME_CREATED , TOKEN_SCOPE , TOKEN_STATE ,VALIDITY_PERIOD ) "
					+ "VALUES (?,NULL,?,NULL,NULL,'PRODUCTION','ACTIVE',-1)";
			String INSERT_SUBSCRIPTION_KEY_MAPPING_SQL = "INSERT INTO AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, KEY_CONTEXT_MAPPING_ID, KEY_TYPE) VALUES (?,?,?)";
			String INSERT_CONSUMER_APPLICATION_SQL = "INSERT INTO IDENTITY_OAUTH_CONSUMER_APPLICATIONS ( APP_NAME , CALLBACK_URL , CONSUMER_KEY , CONSUMER_SECRET ,OAUTH_VERSION"
					+ " , TENANT_ID , USERNAME ) VALUES (NULL,NULL,?,?,'2.0',0,'admin')";
			String INSERT_SUBSCRIBER_SQL = "INSERT INTO AM_SUBSCRIBER  (USER_ID ,TENANT_ID,DATE_SUBSCRIBED ) VALUES(?,?,NOW())";

			PreparedStatement psApplications = conn.prepareStatement(
					INSERT_APPLICATIONS_SQL,
					PreparedStatement.RETURN_GENERATED_KEYS);
			PreparedStatement psSubscriptions = conn.prepareStatement(
					INSERT_SUBSCRIPTIONS_SQL,
					PreparedStatement.RETURN_GENERATED_KEYS);
			PreparedStatement psKeyContextMappings = conn.prepareStatement(
					INSERT_KEY_CONTEXT_MAPPING_SQL,
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
			ResultSet rs = psApplications.getGeneratedKeys();
			String subscriberId = null;
			if (rs.next()) {
				subscriberId = rs.getString(1);
			}
			rs.close();

			int loopCount = 10;
			String applicationName;
			for (int i = 0; i < loopCount; i++) {

				applicationName = applicationNamePrefix + i;
				// Add Application
				psApplications.setString(1, applicationName);
				psApplications.setInt(2, Integer.valueOf(subscriberId));
				psApplications.executeUpdate();
				rs = psApplications.getGeneratedKeys();
				String applicationId = null;
				if (rs.next()) {
					applicationId = rs.getString(1);
				}
				rs.close();

				// Add Subscription
				psSubscriptions.setString(1, "Unlimited");
				psSubscriptions.setString(2, "SUMEDHA_API1_V1.0.0");
				psSubscriptions.setInt(3, Integer.valueOf(applicationId));
				psSubscriptions.executeUpdate();
				rs = psSubscriptions.getGeneratedKeys();
				String subscriptionId = null;
				if (rs.next()) {
					subscriptionId = rs.getString(1);
				}
				rs.close();

				// Add Key Context Mapping
				String accessKey = "9nEQnijLZ0Gi0gZ6a3pZIC" + i;
				psKeyContextMappings.setString(1, "/api1");
				psKeyContextMappings.setString(2, "1.0.0");
				// TODO : add padding to make the key length constant
				psKeyContextMappings.setString(3, accessKey);
				psKeyContextMappings.executeUpdate();
				rs = psKeyContextMappings.getGeneratedKeys();
				String keyContexMappingId = null;
				if (rs.next()) {
					keyContexMappingId = rs.getString(1);
				}
				rs.close();

				// Add Subscription Key Mapping
				psSubscriptionKeyMappingSQL.setInt(1,
						Integer.valueOf(subscriptionId));
				psSubscriptionKeyMappingSQL.setInt(2,
						Integer.valueOf(keyContexMappingId));
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
				psKeyContextMappings.clearParameters();
				psOAuth2AccessTokenSQL.clearParameters();
				psSubscriptionKeyMappingSQL.clearParameters();
				psConsumerApplicationSQL.clearParameters();
				conn.commit();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
