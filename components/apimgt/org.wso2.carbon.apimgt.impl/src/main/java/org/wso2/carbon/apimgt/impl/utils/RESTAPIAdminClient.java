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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;

public class RESTAPIAdminClient extends AbstractAPIGatewayAdminClient {

    private RestApiAdminStub restApiAdminStub;
    private String qualifiedName;
    
    public RESTAPIAdminClient(APIIdentifier apiId) throws AxisFault {
        this.qualifiedName = apiId.getProviderName() + "--" + apiId.getApiName() +
                ":v" + apiId.getVersion();
        String url = getServerURL();
        String cookie = login(url);
        restApiAdminStub = new RestApiAdminStub(null, url + "RestApiAdmin");
        setup(restApiAdminStub, cookie);
    }

    public void addApi(APITemplateBuilder builder) throws AxisFault {
        try {
            String apiConfig = builder.getConfigStringForTemplate();
            restApiAdminStub.addApiFromString(apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while adding new API", e);
        }
    }

    public APIData getApi() throws AxisFault {
        try {
            return restApiAdminStub.getApiByName(qualifiedName);
        } catch (Exception e) {
            throw new AxisFault("Error while obtaining API information from gateway", e);
        }
    }

    public void updateApi(APITemplateBuilder builder) throws AxisFault{
        try {
            String apiConfig = builder.getConfigStringForTemplate();
            restApiAdminStub.updateApiFromString(qualifiedName, apiConfig);
        } catch (Exception e) {
            throw new AxisFault("Error while updating API", e);
        }
    }

    public void deleteApi() throws AxisFault{
        try {
            restApiAdminStub.deleteApi(qualifiedName);
        } catch (Exception e) {
            throw new AxisFault("Error while deleting API", e);
        }
    }
}
