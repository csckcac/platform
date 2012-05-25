/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log   = LogFactory.getLog(APIMgtUsageHandler.class);

    private APIMgtUsageConfigHolder configHolder = new APIMgtUsageConfigHolder();
    private volatile APIMgtUsageBAMDataPublisher publisher;

    private boolean enabled = UsageComponent.getApiMgtConfigReaderService().isEnabled();

    public boolean handleRequest(MessageContext mc) {
        String currentTime = String.valueOf(System.currentTimeMillis());

        if (!enabled) {
            return true;
        }

        if (publisher == null) {
            synchronized (this){
                if (publisher == null) {
                    log.debug("Initializing BAM data publisher");
                    publisher = new APIMgtUsageBAMDataPublisher(configHolder);
                }
            }
        }

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(mc);
        String consumerKey = "";
        if (authContext != null) {
            consumerKey = authContext.getApiKey();
        }
        String context = (String)mc.getProperty(RESTConstants.REST_API_CONTEXT);
        String api_version =  (String)mc.getProperty(RESTConstants.SYNAPSE_REST_API);
        String api = api_version.split(":")[0];
        if (api.contains("--")) {
            api = api.split("--")[0];
        }
        String version = (String)mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String resource = extractResource(mc);
        String method =  (String)((Axis2MessageContext) mc).getAxis2MessageContext().getProperty(
                Constants.Configuration.HTTP_METHOD);

        RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
        requestPublisherDTO.setConsumerKey(consumerKey);
        requestPublisherDTO.setContext(context);
        requestPublisherDTO.setApi_version(api_version);
        requestPublisherDTO.setApi(api);
        requestPublisherDTO.setVersion(version);
        requestPublisherDTO.setResource(resource);
        requestPublisherDTO.setMethod(method);
        requestPublisherDTO.setRequestTime(currentTime);
        publisher.publishEvent(requestPublisherDTO);

        mc.setProperty(APIMgtUsagePublisherConstants.CONSUMER_KEY, consumerKey);
        mc.setProperty(APIMgtUsagePublisherConstants.CONTEXT, context);
        mc.setProperty(APIMgtUsagePublisherConstants.API_VERSION, api_version);
        mc.setProperty(APIMgtUsagePublisherConstants.API, api);
        mc.setProperty(APIMgtUsagePublisherConstants.VERSION, version);
        mc.setProperty(APIMgtUsagePublisherConstants.RESOURCE, resource);
        mc.setProperty(APIMgtUsagePublisherConstants.HTTP_METHOD, method);
        mc.setProperty(APIMgtUsagePublisherConstants.REQUEST_TIME, currentTime);

        return true;
    }

    public boolean handleResponse(MessageContext mc) {
        Long currentTime = System.currentTimeMillis();

        if (!enabled) {
            return true;
        }

        Long serviceTime = currentTime - Long.parseLong((String)mc.getProperty(
                APIMgtUsagePublisherConstants.REQUEST_TIME));

        ResponsePublisherDTO responsePublisherDTO = new ResponsePublisherDTO();
        responsePublisherDTO.setConsumerKey((String)mc.getProperty(APIMgtUsagePublisherConstants.CONSUMER_KEY));
        responsePublisherDTO.setContext((String) mc.getProperty(APIMgtUsagePublisherConstants.CONTEXT));
        responsePublisherDTO.setApi_version((String) mc.getProperty(APIMgtUsagePublisherConstants.API_VERSION));
        responsePublisherDTO.setApi((String) mc.getProperty(APIMgtUsagePublisherConstants.API));
        responsePublisherDTO.setVersion((String) mc.getProperty(APIMgtUsagePublisherConstants.VERSION));
        responsePublisherDTO.setResource((String) mc.getProperty(APIMgtUsagePublisherConstants.RESOURCE));
        responsePublisherDTO.setMethod((String)mc.getProperty(APIMgtUsagePublisherConstants.HTTP_METHOD));
        responsePublisherDTO.setResponseTime(String.valueOf(currentTime));
        responsePublisherDTO.setServiceTime(String.valueOf(serviceTime));
        publisher.publishEvent(responsePublisherDTO);

        return true; // Should never stop the message flow
    }

    private String extractResource(MessageContext mc){
        String resource = "/";
        Pattern pattern = Pattern.compile("^/.+?/.+?([/?].+)$");
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()){
            resource = matcher.group(1);
        }
        return resource;
    }

}
