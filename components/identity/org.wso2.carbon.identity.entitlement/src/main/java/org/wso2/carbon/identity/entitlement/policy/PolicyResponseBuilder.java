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
package org.wso2.carbon.identity.entitlement.policy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.wso2.balana.ctx.ResponseCtx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * XACML repose is built
 */
public class PolicyResponseBuilder {

	private static Log log = LogFactory.getLog(PolicyResponseBuilder.class);
    
    /**
     * creates XACML response from  ResponseCtx
     * @param responseCtx  XACML ResponseCtx object
     * @return XACML response as a String object
     */
    public String getXacmlResponse(ResponseCtx responseCtx) {
        OutputStream stream = new ByteArrayOutputStream();
        responseCtx.encode(stream);
        String response = stream.toString();
        try {
            stream.close();
        } catch (IOException e) {
            log.error("Error in closing XACML response out stream");
        }
        return response;
    }
}
