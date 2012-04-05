/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.httpclient;

import org.wso2.carbon.CarbonException;
import org.mozilla.javascript.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a set of functions to do HTTP POST/GET
 * for the javascript service developers.
 * </p>
 */
public class HttpClientHostObject extends ScriptableObject {

    private HttpMethod method;
    private HttpClient httpClient;

    private NativeArray authSchemePriority = null;
    private NativeArray cookies = null;
    private NativeObject credentials = null;
    private NativeObject proxyCredentials = null;
    private NativeObject host = null;

    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String DEFAULT_CHARSET = "ISO-8859-1";

    private static final String DEFAULT_HOST_PROTOCOL = "http";
    private static final int DEFAULT_HOST_PORT = 80;

    private static final Object AGE = null;
    private static final String PATH = "/";
    private static final Boolean SECURE = false;

    public HttpClientHostObject() {
        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClient(connectionManager);
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "HttpClient";
    }

    /**
     * Constructor the user will be using inside javaScript, this doesn't need any arguments to be
     * passed
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {

        HttpClientHostObject httpClient = new HttpClientHostObject();
        if (args.length != 0) {
            throw new CarbonException("HttpClient constructor doesn't accept any arguments");
        }
        return httpClient;
    }

    /**
     * <p/>
     * This method executes the given HTTP method with user configured details.ject to invoke a Web
     * service. This method corresponds to the following function of the HttpClient java script
     * object.
     * </p>
     * <p/>
     * <pre>
     *    int executeMethod (
     *          in String method | in String url [, in String/Object content [, in Object params [,
     *          Object headers]]]);
     */
    public static int jsFunction_executeMethod(Context cx, Scriptable thisObj, Object[] args,
                                               Function funObj) throws CarbonException {
        HttpClientHostObject httpClient = (HttpClientHostObject) thisObj;

        List<String> authSchemes = new ArrayList<String>();

        String contentType = httpClient.DEFAULT_CONTENT_TYPE;
        String charset = httpClient.DEFAULT_CHARSET;

        /*
         * we check weather authSchemes Priority has been set and put them into a List
         */
        if (httpClient.authSchemePriority != null) {
            // authSchemePriority has been set
            for (int i = 0; i < httpClient.authSchemePriority.getLength(); i++) {
                if (httpClient.authSchemePriority
                        .get(i, httpClient.authSchemePriority) instanceof String) {
                    String currentScheme = (String) httpClient.authSchemePriority
                            .get(i, httpClient.authSchemePriority);
                    if (currentScheme.equals("NTLM")) {
                        authSchemes.add("NTLM");
                    } else if (currentScheme.equals("BASIC")) {
                        authSchemes.add("BASIC");
                    } else if (currentScheme.equals("DIGEST")) {
                        authSchemes.add("DIGEST");
                    } else {
                        throw new CarbonException("Unsupported Authentication Scheme");
                    }
                } else {
                    throw new CarbonException("Authentication Schemes should be Strings values");
                }
            }
            // sets the AuthScheme priority
            httpClient.httpClient.getParams()
                    .setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authSchemes);
        }

        if (httpClient.credentials != null) {
            // gets the credentials of the scheme
            if (ScriptableObject
                    .getProperty(httpClient.credentials, "credentials") instanceof NativeObject) {

                NativeObject creds = (NativeObject) ScriptableObject
                        .getProperty(httpClient.credentials, "credentials");

                if (ScriptableObject.getProperty(creds, "username") instanceof String) {
                    // gets the values about scope of the auth scheme
                    NativeObject scope;
                    String host = AuthScope.ANY_HOST;
                    int port = AuthScope.ANY_PORT;
                    String realm = AuthScope.ANY_REALM;
                    String scheme = AuthScope.ANY_SCHEME;
                    String username;
                    String password;

                    username = (String) ScriptableObject.getProperty(creds, "username");

                    if (!username.equals("")) {
                        // if credentials are used the username shouldn't be an empty string
                        if (ScriptableObject.getProperty(creds, "password") instanceof String) {
                            password = (String) ScriptableObject.getProperty(creds, "password");
                        } else {
                            password = "";
                        }

                        if (ScriptableObject.getProperty(httpClient.credentials,
                                                         "scope") instanceof NativeObject) {
                            // gets the authentication scheme scope details. If this is not set,
                            // default scopes are used
                            scope = (NativeObject) ScriptableObject
                                    .getProperty(httpClient.credentials, "scope");

                            if (ScriptableObject.getProperty(scope, "host") instanceof String) {
                                host = (String) ScriptableObject.getProperty(scope, "host");
                            }
                            if (ScriptableObject.getProperty(scope, "port") instanceof Integer) {
                                port = (Integer) ScriptableObject.getProperty(scope, "port");
                            }
                            if (ScriptableObject.getProperty(scope, "realm") instanceof Integer) {
                                realm = (String) ScriptableObject.getProperty(scope, "realm");
                            }
                            if (ScriptableObject.getProperty(scope, "scheme") instanceof Integer) {
                                scheme = (String) ScriptableObject.getProperty(scope, "scheme");
                            }
                        }

                        if (authSchemes.contains("NTLM")) {
                            // NTLM authentication scheme has set, must use NTCredentials
                            String hostNT;
                            String domainNT;

                            // for NTLM scheme both client host and domain should be specified
                            if (ScriptableObject.getProperty(creds, "host") instanceof String &&
                                ScriptableObject.getProperty(creds, "domain") instanceof String) {
                                hostNT = (String) ScriptableObject.getProperty(creds, "host");
                                domainNT = (String) ScriptableObject.getProperty(creds, "domain");

                                httpClient.httpClient.getState()
                                        .setCredentials(new AuthScope(host, port, realm, scheme),
                                                        new NTCredentials(username, password,
                                                                          hostNT, domainNT));
                            } else {
                                // NTLM is used, but no proper configurations have been set
                                throw new CarbonException(
                                        "Both Host and Domain should be specified if you are using " +
                                        "NTLM Authentication Scheme");
                            }
                        } else {
                            // NTLM scheme has not specified, so add just UsernamePasswordCredentials
                            // instance
                            httpClient.httpClient.getState()
                                    .setCredentials(new AuthScope(host, port, realm, scheme),
                                                    new UsernamePasswordCredentials(username,
                                                                                    password));
                        }
                    } else {
                        throw new CarbonException("Username can not be an empty String");
                    }
                } else {
                    throw new CarbonException("Username should be a String");
                }
            }
        }

        if (httpClient.proxyCredentials != null) {
            // gets proxy credentials of the scheme
            if (ScriptableObject.getProperty(httpClient.proxyCredentials,
                                             "credentials") instanceof NativeObject) {

                NativeObject creds = (NativeObject) ScriptableObject
                        .getProperty(httpClient.proxyCredentials, "credentials");

                if (ScriptableObject.getProperty(creds, "username") instanceof String) {
                    // gets the values about scope of the auth scheme
                    NativeObject scope;
                    String host = AuthScope.ANY_HOST;
                    int port = AuthScope.ANY_PORT;
                    String realm = AuthScope.ANY_REALM;
                    String scheme = AuthScope.ANY_SCHEME;
                    String username;
                    String password;

                    username = (String) ScriptableObject.getProperty(creds, "username");

                    if (!username.equals("")) {
                        // proxy credentials username shouldn't be an empty string
                        if (ScriptableObject.getProperty(creds, "password") instanceof String) {
                            password = (String) ScriptableObject.getProperty(creds, "password");
                        } else {
                            password = "";
                        }

                        // gets the proxy authentication scheme scope
                        if (ScriptableObject.getProperty(httpClient.proxyCredentials,
                                                         "scope") instanceof NativeObject) {

                            scope = (NativeObject) ScriptableObject
                                    .getProperty(httpClient.proxyCredentials, "scope");

                            if (ScriptableObject.getProperty(scope, "host") instanceof String) {
                                host = (String) ScriptableObject.getProperty(scope, "host");
                            }
                            if (ScriptableObject.getProperty(scope, "port") instanceof Integer) {
                                port = (Integer) ScriptableObject.getProperty(scope, "port");
                            }
                            if (ScriptableObject.getProperty(scope, "realm") instanceof Integer) {
                                realm = (String) ScriptableObject.getProperty(scope, "realm");
                            }
                            if (ScriptableObject.getProperty(scope, "scheme") instanceof Integer) {
                                scheme = (String) ScriptableObject.getProperty(scope, "scheme");
                            }
                        }

                        if (authSchemes.contains("NTLM")) {
                            // NTLM authentication scheme has set, must use NTCredentials
                            String hostNT;
                            String domainNT;

                            // for NTLM scheme both client host and domain should be specified
                            if (ScriptableObject.getProperty(creds, "host") instanceof String &&
                                ScriptableObject.getProperty(creds, "domain") instanceof String) {
                                hostNT = (String) ScriptableObject.getProperty(creds, "host");
                                domainNT = (String) ScriptableObject.getProperty(creds, "domain");

                                httpClient.httpClient.getState().setProxyCredentials(
                                        new AuthScope(host, port, realm, scheme),
                                        new NTCredentials(username, password, hostNT, domainNT));
                            } else {
                                // NTLM is used, but no proper configurations have been set
                                throw new CarbonException(
                                        "Both Host and Domain should be specified if you are using " +
                                        "NTLM Authentication Scheme for Proxy");
                            }
                        } else {
                            // NTLM scheme has not specified, so add just UsernamePasswordCredentials
                            // instance
                            httpClient.httpClient.getState()
                                    .setProxyCredentials(new AuthScope(host, port, realm, scheme),
                                                         new UsernamePasswordCredentials(username,
                                                                                         password));
                        }
                    } else {
                        throw new CarbonException("Username can not be an empty String for Proxy");
                    }
                } else {
                    throw new CarbonException("Username should be a String for Proxy");
                }
            }
        }

        //checks whether cookies have been set
        if (httpClient.cookies != null) {

            String domain;
            String name;
            String value;
            String path = httpClient.PATH;
            Object age = httpClient.AGE;
            boolean secure = httpClient.SECURE;

            NativeObject cookie;

            // iterate through cookies and insert them into the httpstate
            for (int i = 0; i < httpClient.cookies.getLength(); i++) {
                cookie = (NativeObject) httpClient.cookies.get(i, httpClient.cookies);
                if (ScriptableObject.getProperty(cookie, "domain") instanceof String) {
                    domain = (String) ScriptableObject.getProperty(cookie, "domain");
                } else {
                    throw new CarbonException("domain property of Cookies should be a String");
                }

                if (ScriptableObject.getProperty(cookie, "name") instanceof String) {
                    name = (String) ScriptableObject.getProperty(cookie, "name");
                } else {
                    throw new CarbonException("name property of Cookies should be a String");
                }

                if (ScriptableObject.getProperty(cookie, "value") instanceof String) {
                    value = (String) ScriptableObject.getProperty(cookie, "value");
                } else {
                    throw new CarbonException("value property of Cookies should be a String");
                }

                if (ScriptableObject.getProperty(cookie, "path") instanceof String) {
                    path = (String) ScriptableObject.getProperty(cookie, "path");
                } else if (!ScriptableObject.getProperty(cookie, "path")
                        .equals(UniqueTag.NOT_FOUND)) {
                    throw new CarbonException("path property of Cookies should be a String");
                }

                if (ScriptableObject.getProperty(cookie, "age") instanceof Date) {
                    age = (Date) ScriptableObject.getProperty(cookie, "age");
                } else if (ScriptableObject.getProperty(cookie, "age") instanceof Integer) {
                    age = (Integer) ScriptableObject.getProperty(cookie, "age");
                } else if (!ScriptableObject.getProperty(cookie, "age")
                        .equals(UniqueTag.NOT_FOUND)) {
                    throw new CarbonException(
                            "age property of Cookies should be an Integer or Date");
                }

                if (ScriptableObject.getProperty(cookie, "secure") instanceof Boolean) {
                    secure = (Boolean) ScriptableObject.getProperty(cookie, "secure");
                } else if (!ScriptableObject.getProperty(cookie, "secure")
                        .equals(UniqueTag.NOT_FOUND)) {
                    throw new CarbonException("name property of Cookies should be a String");
                }

                if (age != null) {
                    if (age instanceof Date) {
                        // cookie expire date has set
                        httpClient.httpClient.getState().addCookie(
                                new Cookie(domain, name, value, path, (Date) age, secure));
                    } else {
                        // cookie TTL is set
                        httpClient.httpClient.getState().addCookie(
                                new Cookie(domain, name, value, path, (Integer) age, secure));
                    }
                } else {
                    // cookie expiration not set, user default values
                    httpClient.httpClient.getState()
                            .addCookie(new Cookie(domain, name, value, path, (Date) age, secure));
                }
            }
        }

        String methodName = null;
        String url = null;
        Object content = null;
        NativeObject params = null;
        NativeArray headers = null;

        // parse the passed arguments into this executeMethod()
        switch (args.length) {
            case 2:
                if (args[0] instanceof String) {
                    methodName = (String) args[0];
                } else {
                    throw new CarbonException("HTTP method should be a String value");
                }

                if (args[1] instanceof String) {
                    url = (String) args[1];
                } else {
                    throw new CarbonException("Url should be a String value");
                }
                break;
            case 3:
                if (args[0] instanceof String) {
                    methodName = (String) args[0];
                } else {
                    throw new CarbonException("HTTP method should be a String value");
                }

                if (args[1] instanceof String) {
                    url = (String) args[1];
                } else {
                    throw new CarbonException("Url should be a String value");
                }

                if (args[2] instanceof String) {
                    content = (String) args[2];
                } else if (args[2] instanceof NativeArray) {
                    content = (NativeArray) args[2];
                } else if (args[2] != null) {
                    throw new CarbonException(
                            "Content should be a String value or Array of Name-value pairs");
                }
                break;
            case 4:
                if (args[0] instanceof String) {
                    methodName = (String) args[0];
                } else {
                    throw new CarbonException("HTTP method should be a String value");
                }

                if (args[1] instanceof String) {
                    url = (String) args[1];
                } else {
                    throw new CarbonException("Url should be a String value");
                }

                if (args[2] instanceof String) {
                    content = (String) args[2];
                } else if (args[2] instanceof NativeArray) {
                    content = (NativeArray) args[2];
                } else if (args[2] != null) {
                    throw new CarbonException(
                            "Content should be a String value or Array of Name-value pairs");
                }

                if (args[3] instanceof NativeObject) {
                    params = (NativeObject) args[3];
                } else if (args[3] != null) {
                    throw new CarbonException("Params argument should be an Object");
                }
                break;
            case 5:
                if (args[0] instanceof String) {
                    methodName = (String) args[0];
                } else {
                    throw new CarbonException("HTTP method should be a String value");
                }

                if (args[1] instanceof String) {
                    url = (String) args[1];
                } else {
                    throw new CarbonException("Url should be a String value");
                }

                if (args[2] instanceof String) {
                    content = (String) args[2];
                } else if (args[2] instanceof NativeArray) {
                    content = (NativeArray) args[2];
                } else if (args[2] != null) {
                    throw new CarbonException(
                            "Content should be a String value or Array of Name-value pairs");
                }

                if (args[3] instanceof NativeObject) {
                    params = (NativeObject) args[3];
                } else if (args[3] != null) {
                    throw new CarbonException("Params argument should be an Object");
                }

                if (args[4] instanceof NativeArray) {
                    headers = (NativeArray) args[4];
                } else if (args[4] != null) {
                    throw new CarbonException("Headers argument should be an Object");
                }
                break;
        }

        if (url != null) {
            if (methodName.equals("GET")) {
                httpClient.method = new GetMethod(url);
            } else if (methodName.equals("POST")) {
                httpClient.method = new PostMethod(url);
            } else if (methodName.equals("PUT")) {
                httpClient.method = new PutMethod(url);
            } else if (methodName.equals("DELETE")) {
                httpClient.method = new DeleteMethod(url);
            } else {
                throw new CarbonException("HTTP method you specified is not supported");
            }
        } else {
            throw new CarbonException("A url should be specified");
        }

        if (headers != null) {
            // headers array is parsed and headers are set
            String hName;
            String hValue;
            NativeObject header;
            for (int i = 0; i < headers.getLength(); i++) {
                header = (NativeObject) headers.get(i, headers);
                if (ScriptableObject.getProperty(header, "name") instanceof String &&
                    ScriptableObject.getProperty(header, "value") instanceof String) {

                    hName = (String) ScriptableObject.getProperty(header, "name");
                    hValue = (String) ScriptableObject.getProperty(header, "value");

                    httpClient.method.setRequestHeader(hName, hValue);
                } else {
                    throw new CarbonException("Name-Value pairs of headers should be Strings");
                }
            }
        }

        if (params != null) {
            // other parameters have been set, they are properly set to the corresponding context
            if (ScriptableObject.getProperty(params, "cookiePolicy") instanceof String) {
                httpClient.method.getParams().setCookiePolicy(
                        (String) ScriptableObject.getProperty(params, "cookiePolicy"));
            } else if (!ScriptableObject.getProperty(params, "cookiePolicy")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (ScriptableObject.getProperty(params, "contentType") instanceof String) {
                contentType = (String) ScriptableObject.getProperty(params, "contentType");
            } else if (!ScriptableObject.getProperty(params, "contentType")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (ScriptableObject.getProperty(params, "charset") instanceof String) {
                charset = (String) ScriptableObject.getProperty(params, "charset");
            } else if (!ScriptableObject.getProperty(params, "charset")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (ScriptableObject.getProperty(params, "timeout") instanceof Integer) {
                httpClient.method.getParams()
                        .setSoTimeout((Integer) ScriptableObject.getProperty(params, "timeout"));
            } else if (!ScriptableObject.getProperty(params, "timeout")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (ScriptableObject.getProperty(params, "doAuthentication") instanceof Boolean) {
                httpClient.method.setDoAuthentication(
                        (Boolean) ScriptableObject.getProperty(params, "doAuthentication"));
            } else if (!ScriptableObject.getProperty(params, "doAuthentication")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (ScriptableObject.getProperty(params, "followRedirect") instanceof Boolean) {
                httpClient.method.setFollowRedirects(
                        (Boolean) ScriptableObject.getProperty(params, "followRedirect"));
            } else if (!ScriptableObject.getProperty(params, "followRedirect")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }

            if (methodName.equals("POST")) {
                // several parameters are specific to POST method
                if (ScriptableObject.getProperty(params, "contentChunked") instanceof Boolean) {
                    boolean chuncked =
                            (Boolean) ScriptableObject.getProperty(params, "contentChunked");
                    ((PostMethod) httpClient.method).setContentChunked(chuncked);
                    if (chuncked && content != null) {
                        // if contentChucked is set true, then InputStreamRequestEntity or
                        // MultipartRequestEntity is used
                        if (content instanceof String) {
                            // InputStreamRequestEntity for string content
                            ((PostMethod) httpClient.method).setRequestEntity(
                                    new InputStreamRequestEntity(new ByteArrayInputStream(
                                            ((String) content).getBytes())));
                        } else {
                            // MultipartRequestEntity for Name-Value pair content
                            NativeObject element;
                            List<StringPart> parts = new ArrayList<StringPart>();
                            String eName;
                            String eValue;
                            // create pairs using name-value pairs                            
                            for (int i = 0; i < ((NativeArray) content).getLength(); i++) {
                                if (((NativeArray) content)
                                        .get(i, (NativeArray) content) instanceof NativeObject) {
                                    element = (NativeObject) ((NativeArray) content)
                                            .get(i, (NativeArray) content);
                                    if (ScriptableObject
                                            .getProperty(element, "name") instanceof String &&
                                        ScriptableObject
                                                .getProperty(element, "value") instanceof String) {
                                        eName = (String) ScriptableObject
                                                .getProperty(element, "name");
                                        eValue = (String) ScriptableObject
                                                .getProperty(element, "value");
                                        parts.add(new StringPart(eName, eValue));
                                    } else {
                                        throw new CarbonException(
                                                "Invalid content definition, objects of the content" +
                                                " array should consists with strings for both key/value");
                                    }

                                } else {
                                    throw new CarbonException(
                                            "Invalid content definition, content array should contain " +
                                            "Javascript Objects");
                                }
                            }
                            ((PostMethod) httpClient.method).setRequestEntity(
                                    new MultipartRequestEntity(
                                            parts.toArray(new Part[parts.size()]),
                                            httpClient.method.getParams()));
                        }
                    }

                } else if (ScriptableObject.getProperty(params, "contentChunked")
                        .equals(UniqueTag.NOT_FOUND) && content != null) {
                    // contentChunking has not used
                    if (content instanceof String) {
                        try {
                            ((PostMethod) httpClient.method).setRequestEntity(
                                    new StringRequestEntity((String) content, contentType,
                                                            charset));
                        } catch (UnsupportedEncodingException e) {
                            throw new CarbonException("Unsupported Charset");
                        }
                    } else {
                        NativeObject element;
                        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                        String eName;
                        String eValue;
                        // create pairs using name-value pairs
                        for (int i = 0; i < ((NativeArray) content).getLength(); i++) {
                            if (((NativeArray) content)
                                    .get(i, (NativeArray) content) instanceof NativeObject) {
                                element = (NativeObject) ((NativeArray) content)
                                        .get(i, (NativeArray) content);
                                if (ScriptableObject
                                        .getProperty(element, "name") instanceof String &&
                                    ScriptableObject
                                            .getProperty(element, "value") instanceof String) {
                                    eName = (String) ScriptableObject.getProperty(element, "name");
                                    eValue =
                                            (String) ScriptableObject.getProperty(element, "value");
                                    pairs.add(new NameValuePair(eName, eValue));
                                } else {
                                    throw new CarbonException(
                                            "Invalid content definition, objects of the content array " +
                                            "should consists with strings for both key/value");
                                }

                            } else {
                                throw new CarbonException(
                                        "Invalid content definition, content array should contain " +
                                        "Javascript Objects");
                            }
                        }
                        ((PostMethod) httpClient.method)
                                .setRequestBody(pairs.toArray(new NameValuePair[pairs.size()]));
                    }
                } else if (!ScriptableObject.getProperty(params, "contentChunked")
                        .equals(UniqueTag.NOT_FOUND)) {
                    throw new CarbonException("Method parameters should be Strings");
                }

            } else if (methodName.equals("GET")) {
                // here, the method now is GET
                if (content != null) {
                    if (content instanceof String) {
                        httpClient.method.setQueryString((String) content);
                    } else {
                        NativeObject element;
                        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                        String eName;
                        String eValue;
                        // create pairs using name-value pairs
                        for (int i = 0; i < ((NativeArray) content).getLength(); i++) {
                            if (((NativeArray) content)
                                    .get(i, (NativeArray) content) instanceof NativeObject) {
                                element = (NativeObject) ((NativeArray) content)
                                        .get(i, (NativeArray) content);
                                if (ScriptableObject
                                        .getProperty(element, "name") instanceof String &&
                                    ScriptableObject
                                            .getProperty(element, "value") instanceof String) {
                                    eName = (String) ScriptableObject.getProperty(element, "name");
                                    eValue =
                                            (String) ScriptableObject.getProperty(element, "value");
                                    pairs.add(new NameValuePair(eName, eValue));
                                } else {
                                    throw new CarbonException(
                                            "Invalid content definition, objects of the content array " +
                                            "should consists with strings for both key/value");
                                }

                            } else {
                                throw new CarbonException(
                                        "Invalid content definition, content array should contain " +
                                        "Javascript Objects");
                            }
                        }
                        httpClient.method
                                .setQueryString(pairs.toArray(new NameValuePair[pairs.size()]));
                    }
                }
            } else if (methodName.equals("PUT")) {
                // several parameters are specific to PUT method
                if (ScriptableObject.getProperty(params, "contentChunked") instanceof Boolean) {
                    boolean chuncked =
                            (Boolean) ScriptableObject.getProperty(params, "contentChunked");
                    ((PutMethod) httpClient.method).setContentChunked(chuncked);
                    if (chuncked && content != null) {
                        // if contentChucked is set true, then InputStreamRequestEntity or
                        // MultipartRequestEntity is used
                        if (content instanceof String) {
                            // InputStreamRequestEntity for string content
                            ((PostMethod) httpClient.method).setRequestEntity(
                                    new InputStreamRequestEntity(new ByteArrayInputStream(
                                            ((String) content).getBytes())));
                        } else {
                            throw new CarbonException(
                                    "Invalid content definition, content should be a string when PUT " +
                                    "method is used");
                        }
                    }

                } else if (ScriptableObject.getProperty(params, "contentChunked")
                        .equals(UniqueTag.NOT_FOUND) && content != null) {
                    // contentChunking has not used
                    if (content instanceof String) {
                        try {
                            ((PostMethod) httpClient.method).setRequestEntity(
                                    new StringRequestEntity((String) content, contentType,
                                                            charset));
                        } catch (UnsupportedEncodingException e) {
                            throw new CarbonException("Unsupported Charset");
                        }
                    } else {
                        throw new CarbonException(
                                "Invalid content definition, content should be a string when PUT " +
                                "method is used");
                    }
                } else if (!ScriptableObject.getProperty(params, "contentChunked")
                        .equals(UniqueTag.NOT_FOUND)) {
                    throw new CarbonException("Method parameters should be Strings");
                }

            }

            // check whether preemptive authentication is used
            if (ScriptableObject.getProperty(params, "preemptiveAuth") instanceof Boolean) {
                httpClient.httpClient.getParams().setAuthenticationPreemptive(
                        (Boolean) ScriptableObject.getProperty(params, "preemptiveAuth"));
            } else if (!ScriptableObject.getProperty(params, "preemptiveAuth")
                    .equals(UniqueTag.NOT_FOUND)) {
                throw new CarbonException("Method parameters should be Strings");
            }
        } else if (content != null) {
            // content is set, but params has not set
            // use default content type and encoding when posting
            if (methodName.equals("POST")) {
                if (content instanceof String) {
                    try {
                        ((PostMethod) httpClient.method).setRequestEntity(
                                new StringRequestEntity((String) content, contentType, charset));
                    } catch (UnsupportedEncodingException e) {
                        throw new CarbonException("Unsupported Charset");
                    }
                } else {
                    NativeObject element;
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    String eName;
                    String eValue;
                    // create pairs using name-value pairs
                    for (int i = 0; i < ((NativeArray) content).getLength(); i++) {
                        if (((NativeArray) content)
                                .get(i, (NativeArray) content) instanceof NativeObject) {
                            element = (NativeObject) ((NativeArray) content)
                                    .get(i, (NativeArray) content);
                            if (ScriptableObject.getProperty(element, "name") instanceof String &&
                                ScriptableObject.getProperty(element, "value") instanceof String) {
                                eName = (String) ScriptableObject.getProperty(element, "name");
                                eValue = (String) ScriptableObject.getProperty(element, "value");
                                pairs.add(new NameValuePair(eName, eValue));
                            } else {
                                throw new CarbonException(
                                        "Invalid content definition, objects of the content array " +
                                        "should consists with strings for both key/value");
                            }

                        } else {
                            throw new CarbonException(
                                    "Invalid content definition, content array should contain " +
                                    "Javascript Objects");
                        }
                    }
                    ((PostMethod) httpClient.method)
                            .setRequestBody(pairs.toArray(new NameValuePair[pairs.size()]));
                }
            } else if (methodName.equals("GET")) {
                if (content instanceof String) {
                    httpClient.method.setQueryString((String) content);
                } else {
                    NativeObject element;
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    String eName;
                    String eValue;
                    // create pairs using name-value pairs
                    for (int i = 0; i < ((NativeArray) content).getLength(); i++) {
                        if (((NativeArray) content)
                                .get(i, (NativeArray) content) instanceof NativeObject) {
                            element = (NativeObject) ((NativeArray) content)
                                    .get(i, (NativeArray) content);
                            if (ScriptableObject.getProperty(element, "name") instanceof String &&
                                ScriptableObject.getProperty(element, "value") instanceof String) {
                                eName = (String) ScriptableObject.getProperty(element, "name");
                                eValue = (String) ScriptableObject.getProperty(element, "value");
                                pairs.add(new NameValuePair(eName, eValue));
                            } else {
                                throw new CarbonException(
                                        "Invalid content definition, objects of the content array " +
                                        "should consists with strings for both key/value");
                            }

                        } else {
                            throw new CarbonException(
                                    "Invalid content definition, content array should contain " +
                                    "Javascript Objects");
                        }
                    }
                    httpClient.method
                            .setQueryString(pairs.toArray(new NameValuePair[pairs.size()]));
                }
            } else if (methodName.equals("PUT")) {
                // here, the method now is PUT
                if (content != null) {
                    if (content instanceof String) {
                        try {
                            ((PutMethod) httpClient.method).setRequestEntity(
                                    new StringRequestEntity((String) content, contentType,
                                                            charset));
                        } catch (UnsupportedEncodingException e) {
                            throw new CarbonException("Unsupported Charset");
                        }
                    } else {
                        throw new CarbonException(
                                "Invalid content definition, content should be a string when PUT " +
                                "method is used");
                    }
                }
            }
        }

        // check whether host configuration details has been set
        if (httpClient.host != null) {
            String host;
            int port = httpClient.DEFAULT_HOST_PORT;
            String protocol = httpClient.DEFAULT_HOST_PROTOCOL;

            if (ScriptableObject.getProperty(httpClient.host, "host") instanceof String) {
                host = (String) ScriptableObject.getProperty(httpClient.host, "host");
            } else {
                throw new CarbonException("Host property should be a String");
            }

            if (ScriptableObject.getProperty(httpClient.host, "port") instanceof Integer) {
                port = (Integer) ScriptableObject.getProperty(httpClient.host, "port");
            }

            if (ScriptableObject.getProperty(httpClient.host, "protocol") instanceof String) {
                protocol = (String) ScriptableObject.getProperty(httpClient.host, "protocol");
            }
            httpClient.httpClient.getHostConfiguration().setHost(host, port, protocol);
        }

        try {
            // finally, executes the http method
            return httpClient.httpClient.executeMethod(httpClient.method);
        } catch (IOException e) {
            throw new CarbonException("Error while executing HTTP method");
        }
    }

    /*
     * this method should be called when you want to release a previously executed method
     */

    public static void jsFunction_releaseConnection(Context cx, Scriptable thisObj, Object[] args,
                                                    Function funObj) throws CarbonException {
        if (args.length == 0) {
            HttpClientHostObject httpClient = (HttpClientHostObject) thisObj;
            httpClient.method.releaseConnection();
        } else {
            throw new CarbonException("Invalid usage of arguments");
        }
    }

    /*
     * <p>
     * This property sets the authSchemePriority of the HttpClient's authentications schemes
     * </p>
     * <pre>
     *  httpClient.authSchemePriority = ["NTLM", "BASIC", "DIGEST"];
     * </pre>
     *
     */

    public void jsSet_authSchemePriority(Object object) throws CarbonException {
        // sets authentication scheme priority of apache httpclient
        if (object instanceof NativeArray) {
            this.authSchemePriority = (NativeArray) object;
        } else {
            throw new CarbonException(
                    "HttpClient Authentication Scheme Priority should be an Array");
        }
    }

    /*
     * <p>
     * This property sets cookies of the HttpClient
     * </p>
     * <pre>
     *  httpClient.cookies = [
     *     { domain : ".wso2.com", name : "myCookie", value : "ADCFE113450593", path : "/", age :
     *      20000, secure : true},
     *     .....
     *     .....
     *  ];
     * </pre>
     */

    public void jsSet_cookies(Object object) throws CarbonException {
        // sets authentication scheme priority of apache httpclient
        if (object instanceof NativeArray) {
            this.cookies = (NativeArray) object;
        } else {
            throw new CarbonException("HttpClient Cookies should be an Array");
        }
    }

    public Scriptable jsGet_cookies() throws CarbonException {
        return Context.toObject(this.httpClient.getState().getCookies(), this);
    }

    /*
    * <p>
    * This property sets credentials of the HttpClient
    * </p>
    * <pre>
    *  httpClient.credentials =
    *     {
    *          scope : { host : "www.wso2.com", port : 80, realm : "web", scheme : "basic"},
    *          credentials : { username : "ruchira", password : "ruchira"}
    *      };
    * </pre>
    */

    public void jsSet_credentials(Object object) throws CarbonException {
        // sets authentication scheme priority of apache httpclient
        if (object instanceof NativeObject) {
            this.credentials = (NativeObject) object;
        } else {
            throw new CarbonException("HttpClient Credentials should be an Object");
        }
    }

    /*
     * <p>
     * This property sets credentials of the HttpClient
     * </p>
     * <pre>
     *  httpClient.credentials =
     *     {
     *          scope : { host : "www.wso2.com", port : 80, realm : "web", scheme : "basic"},
     *          credentials : { username : "ruchira", password : "ruchira"}
     *      };
     * </pre>
     */

    public void jsSet_proxyCredentials(Object object) throws CarbonException {
        // sets authentication scheme priority of apache httpclient
        if (object instanceof NativeObject) {
            this.proxyCredentials = (NativeObject) object;
            //httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, nativeAuthSchemePriority);
        } else {
            throw new CarbonException("HttpClient Proxy Credentials should be an Object");
        }
    }

    /*
     * <p>
     * This property sets host configurations of the HttpClient
     * </p>
     * <pre>
     *  httpClient.host = { host : "www.wso2.com", port : 80, protocol : "https"};
     * </pre>
     */

    public void jsSet_host(Object object) throws CarbonException {
        // sets authentication scheme priority of apache httpclient
        if (object instanceof NativeObject) {
            this.host = (NativeObject) object;
        } else {
            throw new CarbonException("HttpClient Host should be an Object");
        }
    }

    /*
     * This property returns the response received after executing the HTTP method
     */

    public String jsGet_response() throws CarbonException {
        try {
            return this.method.getResponseBodyAsString();
        } catch (IOException e) {
            throw new CarbonException("Error getting the response");
        }
    }

    /*
     * This property returns response headers after executing HTTP method
     */

    public Scriptable jsGet_responseHeaders() throws CarbonException {
        return Context.toObject(this.method.getResponseHeaders(), this);
    }

    /*
     * This property returns received status text after executing HTTP method
     */

    public String jsGet_statusText() throws CarbonException {
        return this.method.getStatusText();
    }
}
