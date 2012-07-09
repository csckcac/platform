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

package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class Axis2Setter extends EnvironmentSetter {

    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();

        String hostName = null;
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("axis2.host.name", "localhost"));
        }
        String httpPort = (prop.getProperty("axis2.http.port", "9764"));
        String httpsPort = (prop.getProperty("axis2.https.port", "9444"));
        String webContextRoot = (prop.getProperty("axis2.webContext.root", null));
        productVariables.setProductVariables
                (hostName, httpPort, httpsPort, webContextRoot,
                 productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}