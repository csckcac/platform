/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.url.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.UrlMapperConstants;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a CarbonTomcatValve which hadles the request for services when a tenant specifies service
 * specific id in the url.
 */
public class UrlMapperValve implements CarbonTomcatValve {
    private static final Log log = LogFactory.getLog(UrlMapperValve.class);

    /**
     * This method is called when valve execute
     * @param request
     *         HttpServletRequest
     * @param response
     *         HttpServletResponse
     */
    public void invoke(HttpServletRequest request, HttpServletResponse response) {
        try {
			process(request, response);
		} catch (Exception e) {
			 log.error("error in forwarding the url", e);	
		}
    }

    /**
     * @param request
     * @param response
     * @throws Exception 
     * @throws UrlMapperException 
     */
    private void process(HttpServletRequest request, HttpServletResponse response) throws Exception  {
        //  URLMapperService urlMapperService = new URLMapperService();
        String serverName = request.getServerName();
        String queryString = request.getQueryString();
    	String uri;
		try {
			uri = HostUtil.getServiceNameForHost(serverName);
		} catch (UrlMapperException e1) {
			 log.error("error in retriving  the service url", e1);
			 throw e1;
		}
        if (uri != null) {
            if (queryString != null) {
                try {
                    // get the actual service url which is mapping with this
                    String endUrl = UrlMapperConstants.HostProperties.SERVICE_IDENTIFIER+uri + "?" + queryString;
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher(endUrl);
                    requestDispatcher.forward(request, response);
                    return;
                } catch (Exception e) {
                    log.error("error in forwarding the url", e);
                    throw e;
                }
            } else {
                //have to implement
                RequestDispatcher patcher = request
                        .getRequestDispatcher("");
                try {

                    patcher.forward(request, response);
                    return;
                } catch (Exception e) {
                    log.error("error in forwarding the url", e);
                    throw e;
                }

            }
        }
    }
}