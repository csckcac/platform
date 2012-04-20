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
package org.wso2.platform.test.core.utils.seleniumutils;

import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

import java.io.File;

public class GRegBackEndURLEvaluator {

    public String getBackEndURL() {
        String baseUrl;

        FrameworkProperties properties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ProductVariables gregProperties = properties.getProductVariables();

        if (gregProperties.getWebContextRoot() != null) {
            baseUrl = "https://" + gregProperties.getHostName() + ":" + gregProperties.getHttpsPort()
                      + "/" + gregProperties.getWebContextRoot() + "/"
                      + "carbon" + "/";
        } else {
            baseUrl = "https://" + properties.getProductVariables().getHostName() + ":" +
                      properties.getProductVariables().getHttpsPort() + "/" +
                      "carbon" + "/";

        }
        return baseUrl;
    }
}
