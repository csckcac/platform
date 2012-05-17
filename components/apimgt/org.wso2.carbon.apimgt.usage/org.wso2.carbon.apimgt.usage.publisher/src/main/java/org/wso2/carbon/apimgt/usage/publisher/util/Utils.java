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


package org.wso2.carbon.apimgt.usage.publisher.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;

import java.net.InetAddress;
import java.util.Map;

public class Utils {

    private static Log log   = LogFactory.getLog(Utils.class);

    /**
     * Extract the customer key from the OAuth Authentication header
     * @param authHeader Header string
     * @return extracted customer key value
     */
    private static String extractCustomerKeyFromAuthHeader(String authHeader) {

        // Expected header :
        //OAuth oauth_consumer_key="nq21LN39VlKe6OezcOndBx",
        // oauth_signature_method="HMAC-SHA1", oauth_signature="DZKyT75hiOIdtMGCU%2BbITArs4sU%3D",
        // oauth_timestamp="1328590467", oauth_nonce="7031216264696", oauth_version="1.0"

        String  consumerKey = null;

        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(APIMgtUsagePublisherConstants._OAUTH_HEADERS_SPLITTER);
        if (headers != null && headers.length > 0) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split(APIMgtUsagePublisherConstants.HEADER_SEGMENT_DELIMITER);
                if (elements != null && elements.length > 1) {
                    int j = 0;
                    for (String element : elements) {
                        if(element != null && !"".equals(element.trim()) && APIMgtUsagePublisherConstants._OAUTH_CONSUMER_KEY.equals(elements[j].trim())){
                            return consumerKey = removeLeadingAndTrailing(elements[j+1].trim());
                        }
                        j++;
                    }
/*
                    if (OAUTH_CONSUMER_KEY.equals(elements[0].trim())) {
                        consumerKey = removeLeadingAndTrailing(elements[1].trim());
                    }
*/
                }
            }
        }

        return consumerKey;
    }

    /**
     * Help to extract consumer key from OAuth header
     * @param base
     * @return
     */
    private static String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static String extractCustomerKeyFromSynapseMessageContext(MessageContext mc){
        String consumerKey;
        Object headers = ((Axis2MessageContext)mc).getAxis2MessageContext().getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS) ;
        consumerKey = extractCustomerKeyFromAuthHeader((String) ((Map) headers).get(
                APIMgtUsagePublisherConstants.AUTHORIZATION_HEADER));
        return consumerKey;
    }

}

