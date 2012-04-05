/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.mediator;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Utils class
 */
public class EntitlementMediatorUtils {


    
	/**
	 * Return an instance of a named cache that is common to all tenants.
	 *
	 * @param name the name of the cache.
	 *
	 * @return the named cache instance.
	 */
	public static Cache getCommonCache(String name) {
		// We create a single cache for all tenants. It is not a good choice to create per-tenant
		// caches in this case. We qualify tenants by adding the tenant identifier in the cache key.
		CarbonContextHolder currentContext = CarbonContextHolder.getCurrentCarbonContextHolder();
		currentContext.startTenantFlow();
		try {
			currentContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
			return CacheManager.getInstance().getCache(name);
		} finally {
			currentContext.endTenantFlow();
		}
	}


    /**
     * This creates the XACML 2.0 Request element from available attribute values
     * @param userName userName
     * @param resource resourceName
     * @param action   Action
     * @return  XACML 2.0 request as String 
     */
    public static String createXACML2Request(String userName, String resource, String action) {



        return "<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Resource><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>" + resource +
                "</AttributeValue></Attribute></Resource><Subject><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>" + userName +
                "</AttributeValue></Attribute></Subject><Action><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>" + action +
                "</AttributeValue></Attribute></Action><Environment/></Request>";

    }


}
