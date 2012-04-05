/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.core.AbstractAdmin;

/**
 *
 */
public class APIKeyValidationService extends AbstractAdmin {

/**
     * Validates the access tokens issued for a particular user to access an API.
     * @param context Requested context
     * @param accessToken Provided access token
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version ,String accessToken)
            throws APIKeyMgtException, APIManagementException {
        ApiMgtDAO ApiMgtDAO = new ApiMgtDAO();
        return ApiMgtDAO.validateKey(context,version, accessToken);
    }

}
