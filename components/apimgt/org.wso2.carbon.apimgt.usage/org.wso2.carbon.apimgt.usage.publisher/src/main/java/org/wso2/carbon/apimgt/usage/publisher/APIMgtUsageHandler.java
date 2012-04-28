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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.util.Utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private APIMgtUsageConfigHolder configHolder = new APIMgtUsageConfigHolder();
    private volatile APIMgtUsageBAMDataPublisher publisher;
    private boolean enabled = UsageComponent.getApiMgtConfigReaderService().isEnabled();

    private static Log log   = LogFactory.getLog(APIMgtUsageHandler.class);

        public boolean handleRequest(MessageContext mc) {
            if (!enabled) {
                return true;
            }

            if (publisher == null) {
                synchronized (this){
                    if (publisher == null){
                        publisher = new APIMgtUsageBAMDataPublisher(configHolder);
                    }
                }
            }

            String currentTime = String.valueOf(System.currentTimeMillis());
            RequestPublisherDTO requestPublisherDTO;
            try{
                mc.setProperty("requestTime",currentTime);

                requestPublisherDTO = new RequestPublisherDTO();

                String consumerKey;
                Object headers = ((Axis2MessageContext)mc).getAxis2MessageContext().getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS) ;
                consumerKey = Utils.extractCustomerKeyFromAuthHeader((String) ((Map) headers).get(APIMgtUsagePublisherConstants.AUTHORIZATION_HEADER));
                if (consumerKey == null){
                    log.error("consumerKey is null in request context",new NullPointerException());
                }
                requestPublisherDTO.setConsumerKey(consumerKey);
                mc.setProperty("consumerKey",consumerKey);

                if (mc.getProperty(RESTConstants.REST_API_CONTEXT) == null){
                    log.error("API context is null in request context",new NullPointerException());
                }
                requestPublisherDTO.setContext((String) mc.getProperty(RESTConstants.REST_API_CONTEXT));

                if (mc.getProperty(RESTConstants.SYNAPSE_REST_API) == null){
                    log.error("API is null in request context",new NullPointerException());
                }
                requestPublisherDTO.setApi((String) mc.getProperty(RESTConstants.SYNAPSE_REST_API));

                if (mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH) == null){
                    log.error("Request path is null in request context",new NullPointerException());
                }
                Pattern pattern = Pattern.compile("^/.+?/.+?([/?].+)$");
                Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
                if(matcher.find()){
                      requestPublisherDTO.setResource(matcher.group(1));
                      mc.setProperty("resource",matcher.group(1));
                }

                if (((Axis2MessageContext) mc).getAxis2MessageContext().getProperty("HTTP_METHOD") == null){
                    log.error("HTTP method is null in request context",new NullPointerException());
                }
                String method = (String) ((Axis2MessageContext) mc).getAxis2MessageContext().getProperty("HTTP_METHOD");
                requestPublisherDTO.setMethod(method);
                mc.setProperty("method",method);

                if (mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION) == null){
                    log.error("API version is null in request context",new NullPointerException());
                }
                requestPublisherDTO.setVersion((String) mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

                requestPublisherDTO.setRequestTime(currentTime);

                publisher.publishEvent(requestPublisherDTO);
            } catch (Exception e){
                log.error("Exception occurred while executing API usage request handler",e);
            }
            return true; // Should never stop the message flow
        }

        public boolean handleResponse(MessageContext mc) {
            if (!enabled) {
                return true;
            }
            Long currentTime = System.currentTimeMillis();
            ResponsePublisherDTO responsePublisherDTO = new ResponsePublisherDTO();
            try{
                responsePublisherDTO.setResponseTime(currentTime.toString());

                Long serviceTime = currentTime - Long.parseLong((String)mc.getProperty("requestTime"));
                responsePublisherDTO.setServiceTime(serviceTime.toString());

                if (mc.getProperty("consumerKey") == null){
                    log.error("consumerKey is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setConsumerKey((String)mc.getProperty("consumerKey"));

                if (mc.getProperty(RESTConstants.REST_API_CONTEXT) == null){
                    log.error("API context is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setContext((String) mc.getProperty(RESTConstants.REST_API_CONTEXT));

                if (mc.getProperty(RESTConstants.SYNAPSE_REST_API) == null){
                    log.error("API is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setApi((String) mc.getProperty(RESTConstants.SYNAPSE_REST_API));

                if (mc.getProperty("resource") == null){
                    log.error("Resource is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setResource((String) mc.getProperty("resource"));

                if (mc.getProperty("method") == null){
                    log.error("HTTP method is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setMethod((String)mc.getProperty("method"));

                if (mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION) == null){
                    log.error("API version is null in response context",new NullPointerException());
                }
                responsePublisherDTO.setVersion((String) mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

                publisher.publishEvent(responsePublisherDTO);
            } catch (Exception e){
                log.error("Exception occurred while executing API usage response handler",e);
            }
            return true; // Should never stop the message flow
        }

        public APIMgtUsageBAMDataPublisher getAPIMgtUsageBAMDataPublisher(){
            return publisher;
        }

}
