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
package org.wso2.carbon.bam.core.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.common.TenantDO;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.session.UserRegistry;

/*
 * This class is used to get tenantID
 */
public class BAMTenantAdmin extends AbstractAdmin {
	private static final Log log = LogFactory.getLog(BAMTenantAdmin.class);

	public int getTenantId() throws BAMException {
		return ((UserRegistry) getGovernanceRegistry()).getTenantId();

	}

	public TenantDO getTenant(int tenantId) throws BAMException {
		TenantDO tenant = new TenantDO();
		tenant.setId(tenantId);
		return tenant;

	}
}
