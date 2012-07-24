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

import org.apache.catalina.connector.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.mapper.MappingData;
import org.wso2.carbon.context.ApplicationContext;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;

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
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
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
    private void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String serverName = request.getServerName();
        String uri = ApplicationContext.getCurrentApplicationContext().
                getApplicationFromUrlMapping(serverName);

        if ((uri != null) && (uri.contains("services"))) {
            //rewriting the request with actual service url in order to retrieve the resource
            String filterUri = uri.substring(0, uri.length() - 1);
            Request connectorReq = (Request) request;
            MappingData mappingData = connectorReq.getMappingData();
            org.apache.coyote.Request coyoteRequest = connectorReq.getCoyoteRequest();

            MessageBytes requestPath = MessageBytes.newInstance();
            requestPath.setString(filterUri);
            mappingData.requestPath = requestPath;
            MessageBytes pathInfo = MessageBytes.newInstance();
            pathInfo.setString(filterUri);
            mappingData.pathInfo = pathInfo;

            coyoteRequest.requestURI().setString(filterUri);
            coyoteRequest.decodedURI().setString(filterUri);
            if (request.getQueryString() != null) {
                coyoteRequest.unparsedURI().setString(filterUri + "?" + request.getQueryString());
            } else {
                coyoteRequest.unparsedURI().setString(filterUri);
            }
            connectorReq.getConnector().
                    getMapper().map(connectorReq.getCoyoteRequest().serverName(),
                    connectorReq.getCoyoteRequest().decodedURI(), null,
                    mappingData);
            //connectorReq.setHost((Host)DataHolder.getInstance().getCarbonTomcatService().getTomcat().getEngine().findChild("testapp.wso2.com"));
            connectorReq.setCoyoteRequest(coyoteRequest);
        }
    }
    
    public boolean equals(Object valve){
        return this.toString() == valve.toString();
    }
    
    public String toString() {
        return "valve for url-mapping";
    }
}