/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.ui.util;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for OAuth FE functionality.
 */
public class OAuthUIUtil {

    private static Log log = LogFactory.getLog(OAuthUIUtil.class);

    /**
     * Returns the corresponding absolute endpoint URL. e.g. https://localhost:9443/oauth2/access-token
     * @param endpointType It could be request-token endpoint, callback-token endpoint or access-token endpoint
     * @param oauthVersion OAuth version whether it is 1.0a or 2.0
     * @param request HttpServletRequest coming to the FE jsp
     * @return Absolute endpoint URL.
     */
    public static String getAbsoluteEndpointURL(String endpointType, String oauthVersion, HttpServletRequest request){
        // derive the hostname:port from the admin console url
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        String endpointURL = adminConsoleURL.substring(0, adminConsoleURL.indexOf("/carbon"));

        // get the servlet context from the OAuth version.
        String oauthServletContext = "/oauth2";
        if(oauthVersion.equals(OAuthConstants.OAuthVersions.VERSION_1A)){
            oauthServletContext = "/oauth";
        }
        return (endpointURL + oauthServletContext + endpointType);
    }

    /**
     * Extracts the username and password info from the HTTP Authorization Header
     * @param authorizationHeader "Basic " + base64encode(username + ":" + password)
     * @return String array with username and password.
     * @throws IdentityException If the decoded data is null.
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader)
            throws OAuthClientException{
        String[] splitValues = authorizationHeader.trim().split(" ");
        byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
        if (decodedBytes != null) {
            String userNamePassword = new String(decodedBytes);
            return userNamePassword.split(":");
        } else {
            String errMsg = "Error decoding authorization header. " +
                    "Could not retrieve user name and password.";
            log.debug(errMsg);
            throw new OAuthClientException(errMsg);
        }
    }

    public static String getRealmInfo(){
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String hostname = serverConfig.getFirstProperty("HostName");
        return "Basic realm=" + hostname;
    }
}
