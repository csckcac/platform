package org.wso2.carbon.identity.oauth.ui;

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

    //OAuth2 request headers.
    public static final String HTTP_REQ_HEADER_AUTHZ = "Authorization";

    // OAuth2 response headers
    public static final String HTTP_RESP_HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HTTP_RESP_HEADER_PRAGMA = "Pragma";
    public static final String HTTP_RESP_HEADER_AUTHENTICATE = "WWW-Authenticate";

    // OAuth2 response header values
    public static final String HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE = "no-store";
    public static final String HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE = "no-cache";


    // OAuth response parameters
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	public static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	public static final String OAUTH_VERIFIER = "oauth_verifier";

	
	public static final String ASSOCIATION_OAUTH_CONSUMER_TOKEN = "ASSOCIATION_OAUTH_CONSUMER_TOKEN";
	public static final String OAUTHORIZED_USER = "OAUTHORIZED_USER";

    // OAuth endpoints
    public static final String ACCESS_TOKEN_URL = "/access-token";
    public static final String REQUEST_TOKEN_URL = "/request-token";
    public static final String AUTHORIZE_TOKEN_URL = "/authorize-token";

    public static final String OAUTH2_PARAMS = "oauth2Parameters";

    public static final String OAUTH_ERROR_CODE = "oauthErrorCode";
    public static final String OAUTH_ERROR_MESSAGE = "oauthErrorMsg";

    public static final String REQ_PARAM_OAUTH_USER_NAME = "oauth_user_name";
    public static final String REQ_PARAM_OAUTH_USER_PASSWORD = "oauth_user_password";

}
