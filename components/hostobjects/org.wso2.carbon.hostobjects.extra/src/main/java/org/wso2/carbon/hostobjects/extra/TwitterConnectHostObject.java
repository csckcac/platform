package org.wso2.carbon.hostobjects.extra;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.util.Arrays;
import java.util.Iterator;

public class TwitterConnectHostObject extends ScriptableObject {

    private String apiKey;
    private String apiSecret;
    private String protectedResource;
    private OAuthService oauthService;
    private Token requestToken;
    private Token accessToken;
    private OAuthRequest oauthRequest;

    @Override
    public String getClassName() {
        return "TwitterConnect";
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        TwitterConnectHostObject tcho = new TwitterConnectHostObject();
        if (args.length == 2) {
            if (!(args[0] == Context.getUndefinedValue()) && args[0] instanceof String) {
                tcho.apiKey = (String) args[0];
            }

            if (!(args[1] == Context.getUndefinedValue()) && args[1] instanceof String) {
                tcho.apiSecret = (String) args[1];
            }
            tcho.oauthService = new ServiceBuilder()
                    .provider(TwitterApi.class)
                    .apiKey(tcho.apiKey)
                    .apiSecret(tcho.apiSecret)
                    .build();
            return tcho;
        } else {
            throw new ScriptException("Twitter API key and Secret is not specified");
        }
    }

    public String jsFunction_getAuthorizationUrl() throws ScriptException {
        return this.oauthService.getAuthorizationUrl(this.requestToken);
    }

    public Token jsFunction_getRequestToken() {
        this.requestToken = this.oauthService.getRequestToken();
        return this.requestToken;
    }

    public static void jsFunction_createOAuthRequest(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        TwitterConnectHostObject tcho = (TwitterConnectHostObject) thisObj;
        Verb verb = Verb.GET;
        if ((args.length > 0) && !(args[0] == Context.getUndefinedValue()) && args[0] instanceof String) {
            tcho.protectedResource = (String) args[0];
            if ((args.length > 1) && !(args[1] == Context.getUndefinedValue()) && args[1] instanceof String) {
                String inputVerb = (String) args[1];
                if ("GET".equals(inputVerb.toUpperCase())) {
                    verb = Verb.GET;
                } else if ("PUT".equals(inputVerb.toUpperCase())) {
                    verb = Verb.PUT;
                } else if ("POST".equals(inputVerb.toUpperCase())) {
                    verb = Verb.POST;
                } else if ("DELETE".equals(inputVerb.toUpperCase())) {
                    verb = Verb.DELETE;
                }
            }

            tcho.oauthRequest = new OAuthRequest(verb, tcho.protectedResource);

            if ((args.length > 2) && !(args[2] == Context.getUndefinedValue()) && args[2] instanceof Scriptable) {
                Scriptable queryJsonString = (Scriptable) args[2];
                String[] ids = Arrays.copyOf(queryJsonString.getIds(), queryJsonString.getIds().length, String[].class);
                for (String id : ids) {
                    String value = (String) ((Scriptable) args[2]).get(id, cx.initStandardObjects());
                    tcho.oauthRequest.addQuerystringParameter(id, value);
                }
            }
        } else {
            throw new ScriptException("Twitter API URL is not provided, Request cannot be built");
        }
    }

    public String jsFunction_getResponseBody() throws ScriptException {
        this.oauthService.signRequest(this.accessToken, this.oauthRequest);
        Response response = this.oauthRequest.send();
        return response.getBody();
    }

    public Token jsFunction_getAccessToken(Object object) throws ScriptException {
        if (object instanceof String) {
            Verifier verifier = new Verifier((String) object);
            this.accessToken = this.oauthService.getAccessToken(this.requestToken, verifier);
            return this.accessToken;
        } else {
            throw new ScriptException("Illegal argument for the verifier : Add the code given from Twitter");
        }
    }

    public void jsFunction_setRequestToken(Object object) throws ScriptException {
        Token rtFromSession = (Token) Context.jsToJava(object, Token.class);
        this.requestToken = rtFromSession;
    }

    public void jsFunction_setAccessToken(Object object) throws ScriptException {
        Token atFromSession = (Token) Context.jsToJava(object, Token.class);
        this.accessToken = atFromSession;
    }
}
