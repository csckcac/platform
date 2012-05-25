/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.handlers.security.stub.APIAuthenticationServiceStub;
import org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping;

import java.util.List;

public class APIAuthenticationAdminClient extends AbstractAPIGatewayAdminClient {

    private APIAuthenticationServiceStub stub;

    public APIAuthenticationAdminClient() throws AxisFault {
        String url = getServerURL();
        String cookie = login(url);
        stub = new APIAuthenticationServiceStub(null, url + "APIAuthenticationService");
        setup(stub, cookie);
    }

    public void invalidateKeys(List<APIKeyMapping> mappings) throws AxisFault {
        try {
            stub.invalidateKeys(mappings.toArray(new APIKeyMapping[mappings.size()]));
        } catch (Exception e) {
            throw new AxisFault("Error while invalidating API keys", e);
        }
    }

}
