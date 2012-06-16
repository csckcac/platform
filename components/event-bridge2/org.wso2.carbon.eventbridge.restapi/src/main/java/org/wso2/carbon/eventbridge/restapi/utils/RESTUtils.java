package org.wso2.carbon.eventbridge.restapi.utils;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.eventbridge.commons.Credentials;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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
public class RESTUtils {
    public static Map<String,String> extractAuthHeaders(HttpServletRequest request) {
        String authzHeader = request.getHeader("Authorization");
        String usernameAndPassword = new String(Base64.decodeBase64(authzHeader.substring(6).getBytes()));

        int userNameIndex = usernameAndPassword.indexOf(":");
        Map<String,String> credentials = new HashMap<String, String>();

        credentials.put("username", usernameAndPassword.substring(0, userNameIndex));
        credentials.put("password", usernameAndPassword.substring(userNameIndex + 1));

        return credentials;
    }

    public static Credentials extractAuthHeaders(String authHeader) {

        String usernameAndPassword = new String(Base64.decodeBase64(authHeader.substring(6).getBytes()));

        int userNameIndex = usernameAndPassword.indexOf(":");
        Credentials credentials = new Credentials(usernameAndPassword.substring(0, userNameIndex),
                usernameAndPassword.substring(userNameIndex + 1));

        return credentials;
    }

}
