/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.automation.core.utils.coreutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;

/**
 * contain utility methods for all platform wide settings
 */
public class PlatformUtil {
    private static final Log log = LogFactory.getLog(PlatformUtil.class);

    /**
     * This method will read the carbon zip system property and return it for given product name
     *
     * @param productName name of the product
     * @return product distribution location
     */
    public static String getCarbonZipLocation(String productName) {
        String location;
        if (ProductConstant.APP_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("as.carbon.zip");
        } else if (ProductConstant.ESB_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("esb.carbon.zip");
        } else if (ProductConstant.BPS_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("bps.carbon.zip");
        } else if (ProductConstant.DSS_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("dss.carbon.zip");
        } else if (ProductConstant.MB_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("mb.carbon.zip");
        } else if (ProductConstant.CEP_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("cep.carbon.zip");
        } else if (ProductConstant.GS_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("gs.carbon.zip");
        } else if (ProductConstant.BRS_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("brs.carbon.zip");
        } else if (ProductConstant.GREG_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("greg.carbon.zip");
        } else if (ProductConstant.MANAGER_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("manager.carbon.zip");
        } else if (ProductConstant.LB_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("lb.carbon.zip");
        } else if (ProductConstant.AM_SERVER_NAME.equalsIgnoreCase(productName)) {
            location = System.getProperty("am.carbon.zip");
        } else {
            log.warn("Product Name not found, invalid product");
            return null;
        }
        return location;
    }

    public static void setKeyStoreProperties() {
        EnvironmentBuilder builder = new EnvironmentBuilder();
        System.setProperty("javax.net.ssl.trustStore", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", builder.getFrameworkSettings().
                getEnvironmentVariables().getKeyStrorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        if (log.isDebugEnabled()) {
            log.debug("javax.net.ssl.trustStore :" + System.getProperty("javax.net.ssl.trustStore"));
            log.debug("javax.net.ssl.trustStorePassword :" + System.getProperty("javax.net.ssl.trustStorePassword"));
            log.debug("javax.net.ssl.trustStoreType :" + System.getProperty("javax.net.ssl.trustStoreType"));
        }
    }
}
