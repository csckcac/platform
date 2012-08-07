/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.identity.provider.openid.ui.handlers;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDParameterDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.TenantUtils;

/**
 * Contains OpenID related utility functions
 */
public class OpenIDUtil {

    private static Log log = LogFactory.getLog(OpenIDUtil.class);

    private static final Set<Character> UNRESERVED_CHARACTERS = new HashSet<Character>();

    public static String getUserName(String rquestUrl) throws IdentityException {
        String caller = null;
        String path = null;
        URI uri = null;
        String contextPath = "/openid/";

        try {
			uri = new URI(rquestUrl);
			path = uri.getPath();
		} catch (URISyntaxException e) {
			throw new IdentityException("Invalid OpenID", e);
		}

		caller = path.substring(path.indexOf(contextPath)
				+ contextPath.length(), path.length());
		return caller;
    }

    /**
     * Generate OpenID for a given user.
     * 
     * @param user User
     * @return Generated OpenID
     * @throws IdentityProviderException
     */
    public static String generateOpenID(String user, String openIDUserUrl) throws IdentityException {
        String openID = null;
        URI uri = null;
        URL url = null;

        user = normalizeUrlEncoding(user);
        openID = String.format(openIDUserUrl, user);

        try {
            uri = new URI(openID);
        } catch (URISyntaxException e) {
            log.error("Invalid OpenID URL :" + openID, e);
            throw new IdentityException("Invalid OpenID URL");
        }

        try {
            url = uri.normalize().toURL();
            if (url.getQuery() != null || url.getRef() != null) {
                log.error("Invalid user name for OpenID :" + openID);
                throw new IdentityException("Invalid user name for OpenID");
            }
        } catch (MalformedURLException e) {
            log.error("Malformed OpenID URL :" + openID, e);
            throw new IdentityException("Malformed OpenID URL");
        }

        openID = url.toString();

        if (log.isDebugEnabled()) {
            log.debug("OpenID generated successfully : " + openID);
        }

        return openID;
    }

    /**
     * @param text
     * @return
     */
    private static String normalizeUrlEncoding(String text) {

        if (text == null)
            return null;

        int len = text.length();
        StringBuffer normalized = new StringBuffer(len);

        for (int i = 0; i < len; i++) {
            char current = text.charAt(i);
            if (current == '%' && i < len - 2) {
                String percentCode = text.substring(i, i + 3).toUpperCase();
                try {
                    String str = URLDecoder.decode(percentCode, "ISO-8859-1");
                    char chr = str.charAt(0);
                    if (UNRESERVED_CHARACTERS.contains(Character.valueOf(chr)))
                        normalized.append(chr);
                    else
                        normalized.append(percentCode);
                } catch (UnsupportedEncodingException e) {
                    normalized.append(percentCode);
                }
                i += 2;
            } else {
                normalized.append(current);
            }
        }
        return normalized.toString();
    }

    /**
     * Normalize the provided relying party URL
     * 
     * @param rpUrl Relying party URL to be normalized
     * @return Normalized relying party URL
     * @throws RelyingPartyException
     */
    public static String getRelyingPartyUrl(String rpUrl) throws IdentityException {
        URI uri = null;
        URL url = null;

        try {
            uri = new URI(rpUrl);
        } catch (URISyntaxException e) {
            log.error("Invalid relying party URL :" + rpUrl, e);
            throw new IdentityException("Invalid relying party URL");
        }

        try {
            url = uri.normalize().toURL();
            url = new URL(url.getProtocol().toLowerCase(), url.getHost().toLowerCase(), url
                    .getPort(), url.getPath());
            return url.toString();
        } catch (MalformedURLException e) {
            log.error("Malformed relying party URL :" + rpUrl, e);
            throw new IdentityException("Malformed relying party URL");
        }
    }

    public static OpenIDParameterDTO[] getOpenIDAuthRequest(ParameterList request) {
        OpenIDParameterDTO[] params = null;
        List list = null;

        list = request.getParameters();
        params = new OpenIDParameterDTO[list.size()];
        int i = 0;
        for (Object object : list) {
            Parameter param = (Parameter) object;
            OpenIDParameterDTO openIDParameterDTO = new OpenIDParameterDTO();
            openIDParameterDTO.setName(param.getKey());
            openIDParameterDTO.setValue(param.getValue());
            params[i++] = openIDParameterDTO;
        }
        return params;
    }

    public static OpenIDParameterDTO[] getOpenIDAuthRequest(HttpServletRequest request) {
        List<OpenIDParameterDTO> params = null;
        params = getOpenIDAuthRequestAsList(request);
        return params.toArray(new OpenIDParameterDTO[params.size()]);
    }

    public static List<OpenIDParameterDTO> getOpenIDAuthRequestAsList(HttpServletRequest request) {
        Map parameterMap = null;
        Iterator keysIter = null;
        List<OpenIDParameterDTO> params = null;
        OpenIDParameterDTO param = null;

        parameterMap = request.getParameterMap();
        keysIter = parameterMap.keySet().iterator();
        params = new ArrayList<OpenIDParameterDTO>();
        while (keysIter.hasNext()) {
            String name = (String) keysIter.next();
            Object v = parameterMap.get(name);

            String value;
            if (v instanceof String[]) {
                String[] values = (String[]) v;
                if (values.length > 1 && name.startsWith("openid."))
                    throw new IllegalArgumentException("Multiple parameters with the same name: "
                            + values);

                value = values.length > 0 ? values[0] : null;
            } else if (v instanceof String) {
                value = (String) v;
            } else {
                value = "";
            }

            param = new OpenIDParameterDTO();
            param.setName(name);
            param.setValue(value);
            params.add(param);
        }

        return params;
    }

    /**
     * Find the OpenID corresponding to the given user name.
     * 
     * @param userName User name
     * @return OpenID corresponding the given user name.
     * @throws IdentityProviderException
     */
    public static String getOpenID(String userName) throws Exception {
        return generateOpenID(userName, "");
    }

    public static String getFronEndUrl(String openId, HttpServletRequest request, String relativeUrl)
            throws Exception {
        String tenant = TenantUtils.getDomainNameFromOpenId(openId);
        if (getHostName().equals(tenant)) {
            tenant = null;
        }

        String frontEndUrl = getAdminConsoleURL(request) + relativeUrl;;

        if (tenant != null && tenant.trim().length() > 0) {
            return frontEndUrl.replace("/carbon/", "/t/" + tenant + "/carbon/");
        }

        return frontEndUrl;
    }

    public static String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (url.indexOf("/openidserver/") != -1) {
            url = url.replace("/openidserver", "");
        }
        return url;
    }

    private static String getHostName() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        if (serverConfig.getFirstProperty("HostName") != null) {
            return TenantUtils.getDomainNameFromOpenId(serverConfig.getFirstProperty("HostName"));
        } else {
            return "localhost";
        }
    }

}