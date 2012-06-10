package org.wso2.carbon.eventbridge.restapi.rest;

import org.apache.commons.codec.binary.Base64;
import org.osgi.service.http.HttpContext;
import org.wso2.carbon.eventbridge.restapi.internal.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RestAPIContext implements HttpContext{

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getScheme().equals("https")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (request.getHeader("Authorization") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (authenticated(request)) {
            return true;

        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public URL getResource(String s) {
        return null;
    }

    @Override
    public String getMimeType(String s) {
        return null;
    }

    protected boolean authenticated(HttpServletRequest request) {
        String authzHeader = request.getHeader("Authorization");
        String usernameAndPassword = new String(Base64.decodeBase64(authzHeader.substring(6).getBytes()));

        int userNameIndex = usernameAndPassword.indexOf(":");
        String username = usernameAndPassword.substring(0, userNameIndex);
        String password = usernameAndPassword.substring(userNameIndex + 1);

        return Utils.getAuthenticationService().authenticate(username, password);
    }
}
