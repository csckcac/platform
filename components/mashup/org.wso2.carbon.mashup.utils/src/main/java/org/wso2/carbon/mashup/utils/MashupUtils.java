/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mashup.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.scriptengine.cache.ScriptCachingContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;

public class MashupUtils {

    private static Log log = LogFactory.getLog(MashupUtils.class);

    /**
     * Given a HTTPMethod and a target this utility method fetches the resource for you. It will
     * take care of setting the user keyStore and stuff when the url is https.
     *
     * @param method    The HTTP Method instance
     * @param targetURL - The target URL to invoke
     * @param username  - An optional username for basic authnetication
     * @param password  - An optional password for basic authenticaiton
     * @return The statusCode as an int
     * @throws java.io.IOException Thrown in case any exception occurrs
     */
    public static int executeHTTPMethod(HttpMethod method, URL targetURL, String username,
                                        String password) throws IOException {

        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(connectionManager);
        // We should not use method.setURI and set the complete URI here.
        // If we do so commons-httpclient will not use our custom socket factory.
        // Hence we set the path and query separatly
        method.setPath(targetURL.getPath());
        method.setQueryString(targetURL.getQuery());
        method.setRequestHeader("Host", targetURL.getHost());
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

        // If a username and a password is provided we support basic auth
        if ((username != null) && (password != null)) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            int port = targetURL.getPort();
            httpClient.getState()
                    .setCredentials(new AuthScope(targetURL.getHost(), port), creds);
        }

        return httpClient.executeMethod(method);
    }

    public static ScriptCachingContext getScriptCachingContext(ConfigurationContext configurationContext,
                                                               AxisService service) {
        String tenantId = Integer.toString(MultitenantUtils.getTenantId(configurationContext));
        String scriptPath = (String) service.getParameterValue(MashupConstants.SERVICE_JS);
        ScriptCachingContext sctx = new ScriptCachingContext(tenantId, "/", "/", scriptPath);
        sctx.setSourceModifiedTime(service.getLastUpdate());
        return sctx;
    }

    public static ScriptCachingContext getScriptCachingContext(ConfigurationContext configurationContext,
                                                               String scriptPath, long lastModified) {
        String tenantId = Integer.toString(MultitenantUtils.getTenantId(configurationContext));
        ScriptCachingContext sctx = new ScriptCachingContext(tenantId, "/", "/", scriptPath);
        sctx.setSourceModifiedTime(lastModified);
        return sctx;
    }

    /**
     * Locates the service Javascript file associated with ServiceJS parameter and returns
     * a Reader for it.
     *
     * @param service Axis2 Service
     * @return an input stream to the javascript source file
     * @throws AxisFault if the parameter ServiceJS is not specified or if the service
     *                   implementation is not available
     */
    public static Reader readJS(AxisService service) throws AxisFault {
        InputStream jsFileStream = null;
        Parameter implInfoParam = service.getParameter(MashupConstants.SERVICE_JS);
        if (implInfoParam == null) {
            throw new AxisFault("Parameter 'ServiceJS' not specified");
        }
        Object value = implInfoParam.getValue();
        //We are reading the stream from the axis2 parameter
        jsFileStream = new ByteArrayInputStream(((String) service.getParameter(MashupConstants.SERVICE_JS_STREAM).getValue()).getBytes());
        if (jsFileStream == null) {
            if (value instanceof String) {
                try {
                    String path = (String) value;
                    String latter = path.split(File.separator+"repository")[1];
                    path = CarbonUtils.getCarbonHome() + File.separator+"repository" + latter;
                    jsFileStream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    throw new AxisFault("Unable to load the javaScript, File not Found", e);
                }
            } else {
                jsFileStream = service.getClassLoader().getResourceAsStream(
                        implInfoParam.getValue().toString());
            }
        }
        if (jsFileStream == null) {
            throw new AxisFault("Unable to load the javaScript");
        }
        return new BufferedReader(new InputStreamReader(jsFileStream));
    }

    public static String getImportScriptsList(AxisService service) {
        String scripts = null;

        // Get necessary JavaScripts to be loaded from services.xml
        Parameter param = service.getParameter(MashupConstants.LOAD_JSSCRIPTS);
        if (param != null) {
            scripts = (String) param.getValue();
        }

        /**** TODO We might not need the following code since getting a parameter
         from an Operation covers this(thilina) ****/
        // Get necessary JavaScripts to be loaded from axis2.xml
        param = service.getParameter(MashupConstants.LOAD_JSSCRIPTS);
        if (param != null) {
            if (scripts == null) {
                scripts = (String) param.getValue();
            } else {
                // Avoids loading the same set of script files twice
                if (!scripts.equals(param.getValue())) {
                    scripts += "," + param.getValue();
                }
            }
        }
        return scripts;
    }

    public static void writeFile(CarbonHttpResponse response, File file) throws CarbonException {
        String filePath = file.getAbsolutePath();
        if (!filePath.contains(CarbonUtils.getCarbonHome().concat(File.separator))) {
            String latter = filePath.split(File.separator+"repository")[1];
            filePath = CarbonUtils.getCarbonHome() + File.separator+"repository" + latter;
            file = new File(filePath);
        }
        response.addHeader(HTTP.CONTENT_LEN, Integer.toString((int) file.length()));
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(file);
            do {
                byte[] buffer = new byte[4000];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            } while (fileInputStream.available() > 0);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            throw new CarbonException("File " + file.getName() + " cannot be found", e);
        } catch (IOException e) {
            throw new CarbonException(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing the stream", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing the stream", e);
                }
            }
        }
    }

    public static void writeFile(HttpServletResponse response, File file) throws CarbonException {
        String filePath = file.getAbsolutePath();
        if (!filePath.contains(CarbonUtils.getCarbonHome().concat(File.separator))) {
            String latter = filePath.split(File.separator+"repository")[1];
            filePath = CarbonUtils.getCarbonHome() + File.separator+"repository" + latter;
            file = new File(filePath);
        }
        response.setContentLength((int) file.length());
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(file);
            do {
                byte[] buffer = new byte[4000];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            } while (fileInputStream.available() > 0);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            throw new CarbonException("File " + file.getName() + " cannot be found", e);
        } catch (IOException e) {
            throw new CarbonException(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing the stream", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("Error closing the stream", e);
                }
            }
        }
    }

    public static String filterUsername(String username) {
        String filteredUsername = username.replace('@', '_');
        return filteredUsername;
    }

}
