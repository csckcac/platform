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
package org.wso2.carbon.endpoint.ui;

import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.common.to.AddressEndpointData;
import org.wso2.carbon.endpoint.common.to.DefaultEndpointData;
import org.wso2.carbon.endpoint.common.to.TemplateEndpointData;

import java.util.HashMap;

public class TestUtil {

    public static AddressEndpointData getAddressEndpoint(Template template) throws Exception {
        AddressEndpointData data = new AddressEndpointData();
        data.setAddress("http://bia.com/services/HelloService");
        data.setEpName(template.getName()+"_endpoint");
        data.setEpType(0);

        try {

        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    public static DefaultEndpointData getDefaultEndpoint(Template template) throws Exception{
        DefaultEndpointData data = new DefaultEndpointData();
        data.setEpName(template.getName()+"_endpoint");
        data.setEpType(1);
        return data;
    }

    public static TemplateEndpointData getTemplateEndpoint(String epName) throws Exception {
        TemplateEndpointData data = new TemplateEndpointData();
        try {
            data.setTargetTemplate(epName);
            String[]  paramMap = new String[2];
            paramMap[0] = epName+"_p1:"+"val1" ;
            paramMap[1] = epName+"_p2"+"val2" ;
            data.setParametersAsColonSepArray(paramMap);
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }


    private static void handleFault(Exception e) throws Exception {
        throw e;
    }

}
