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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;

public class PolicyAdmin {

    private PolicyStore store;
    private static Log log = LogFactory.getLog(PolicyAdmin.class);

    public PolicyAdmin(PolicyStore store) {
        this.store = store;
    }

    public void addOrUpdatePolicy(PolicyDTO policy) throws IdentityException {
        store.addOrUpdatePolicy(policy);
    }

    public void removePolicy(PolicyDTO policy) throws IdentityException {
        store.removePolicy(policy.getPolicyId());
    }
}
