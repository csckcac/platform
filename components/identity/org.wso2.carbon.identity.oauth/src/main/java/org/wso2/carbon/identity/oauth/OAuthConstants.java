package org.wso2.carbon.identity.oauth;

import org.wso2.carbon.identity.core.IdentityRegistryResources;

public final class OAuthConstants {

    public static class OAuthVersions {
        public static final String VERSION_1A = "OAuth-1.0a";
        public static final String VERSION_2 = "OAuth-2.0";
    }

	// OAuth request parameters
	public static final String OAUTH_VERSION = "oauth_version";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String OAUTH_SIGNATURE = "oauth_signature";
	public static final String SCOPE = "scope";
	public static final String OAUTH_DISPLAY_NAME = "xoauth_displayname";

	// OAuth response parameters
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_VERIFIER = "oauth_verifier";

	public static final String ASSOCIATION_OAUTH_CONSUMER_TOKEN = "identity.user.oauth.consumer,token";
	public final static String ASSOCIATION_USER_OAUTH_APP = "identity.user.oauth.app";

	public static final String OAUTHORIZED_USER = "oauthorized_user";
	public static final String APPLICATION_NAME = "application_name";
	public static final String OAUTH_CONSUMERS = IdentityRegistryResources.IDENTITY_PATH
			+ "oauth/consumers/";
	public static final String OAUTH_TOKENS = IdentityRegistryResources.IDENTITY_PATH
			+ "oauth/tokens/";

	public final static String OAUTH_USER_CONSUMER_KEY = "consumer_key";
	public final static String OAUTH_APP_CALLBACK = "callback_url";
	public final static String OAUTH_APP_CONSUMER_KEY = "consumer_key";
	public final static String OAUTH_APP_CONSUMER_SECRET = "consumer_secret";
	public final static String OAUTH_APP_NAME = "oauth_app_name";
	public final static String OAUTH_USER_NAME = "oauth_user_name";
	public final static String OAUTH_ACCESS_TOKEN_ISSUED = "oauth_access_token_issued";

    public static final String OAUTH_REQUEST_TOK_ENDPOINT = "/request_token";
    public static final String OAUTH_AUTHORIZE_TOK_ENDPOINT = "/authorize_token";
    public static final String OAUTH_ACCESS_TOK_ENDPOINT = "/access_token";

    public static final int OAUTH_AUTHZ_CB_HANDLER_DEFAULT_PRIORITY = 1;

}
