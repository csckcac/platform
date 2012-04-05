/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.mashup.javascript.hostobjects.atom;

import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleLogin {

    private static final Log log = LogFactory.getLog(GoogleLogin.class);

    private static final String UTF8 = "UTF-8";
    private static final String URI = "https://www.google.com/accounts/ClientLogin";

    public static String getAuth(
            AbderaClient client,
            String service,
            String id,
            String pwd) {

        try {
            StringRequestEntity stringreq =
                    new StringRequestEntity(getRequest(id, pwd, service));
            RequestOptions options = client.getDefaultRequestOptions();
            options.setContentType("application/x-www-form-urlencoded");
            ClientResponse response = client.post(URI, stringreq, options);
            String auth = read(response.getInputStream());
            response.release();
            return auth.split("\n")[2].replaceAll("Auth=", "auth=");
        } catch (CarbonException e) {
            log.error("Error reading content from Google Client Login for \"" + id + "\"", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error parsing content from Google Client Login for \"" + id + "\"", e);
        } catch (IOException e) {
            log.error("Error reading getting content from Google Client Login for \"" + id + "\"", e);
        }

        return null;
    }

    private static String read(InputStream in) throws CarbonException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int n = -1;
        try {
            while ((n = in.read()) != -1) {
                out.write(n);
            }
            out.flush();
        } catch (IOException e) {
            throw new CarbonException(e);
        }
        return new String(out.toByteArray());
    }

    private static String getRequest(String id, String pwd, String service)
            throws UnsupportedEncodingException {
        return "Email=" + URLEncoder.encode(id, UTF8) +
                "&Passwd=" + URLEncoder.encode(pwd, UTF8) +
                "&service=" + URLEncoder.encode(service, UTF8) +
                "&source=" + URLEncoder.encode("wso2-mashup-server", UTF8);

    }
}
