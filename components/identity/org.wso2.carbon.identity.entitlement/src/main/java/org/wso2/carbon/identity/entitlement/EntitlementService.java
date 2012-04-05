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
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;

/**
 * Entitlement Service class which exposes the PDP 
 */
public class EntitlementService extends AbstractAdmin {

	private static Log log = LogFactory.getLog(EntitlementService.class);

	/**
	 * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
	 * hand back to the PEP. PEP needs construct the XACML request before sending it to the
	 * EntitlementEngine
	 * @param request XACML request as a String Object
	 * @return XACML response as a String Object
	 * @throws Exception throws
	 */
	public String getDecision(String request) throws Exception {

        String response;
		try {
			if (log.isDebugEnabled()) {
				log.debug("XACML Request  :  " + request);
			}
			EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
					getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
			response = entitlementEngine.evaluate(request);
            if(log.isDebugEnabled()){
                log.debug("XACML Response  :  " + response);
            }
			return response;
		} catch (Exception e) {
			log.error("Error occurred while evaluating XACML request", e);
			throw new Exception("Error occurred while evaluating XACML request");
		}
	}

	/**
	 * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
	 * hand back to the PEP. Here PEP does not need construct the XACML request before sending it to the
	 * EntitlementEngine. Just can send the single attribute value. But here default attribute ids and data types
     * are used
	 * @param subject  subject
	 * @param resource  resource
	 * @param action  action
	 * @param environment environment
	 * @return  XACML response as a String Object
	 * @throws Exception throws
	 */
	public String getDecisionByAttributes(String subject, String resource, String action,
                                          String[] environment) throws Exception {

        String realEnvironment = null;    // TODO remove  environment array for API
        if(environment != null && environment.length > 0){
            realEnvironment = environment[0];
        }
		try {
			EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
					getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
            return entitlementEngine.evaluate(subject, resource, action, realEnvironment);
		} catch (Exception e) {
			log.error("Error occurred while evaluating XACML request", e);
			throw new Exception("Error occurred while evaluating XACML request");
		}
	}

}
