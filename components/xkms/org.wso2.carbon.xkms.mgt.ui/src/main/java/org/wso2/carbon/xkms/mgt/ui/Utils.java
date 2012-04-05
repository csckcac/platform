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
package org.wso2.carbon.xkms.mgt.ui;

import org.wso2.carbon.xkms.mgt.stub.types.XKMSConfigData;

import javax.servlet.http.HttpServletRequest;

public class Utils {

    public static XKMSConfigData populateXKMSConfig(HttpServletRequest request) {

        String paramValue;
        XKMSConfigData configData = new XKMSConfigData();

        if (request.getParameter(XKMSMgtConstants.SERVER_AUTHENTICATION_CODE) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.SERVER_AUTHENTICATION_CODE);
            configData.setAuthenCode(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.KEY_STORE_LOCATION) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.KEY_STORE_LOCATION);
            configData.setKeystoreLocation(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.KEY_STORE_PASSWORD) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.KEY_STORE_PASSWORD);
            configData.setKeystorePassword(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.SERVER_CERT_ALIACE) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.SERVER_CERT_ALIACE);
            configData.setServerCertAlias(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.SERVER_KEY_PASSWORD) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.SERVER_KEY_PASSWORD);
            configData.setServerKeyPassword(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.ISSUER_CERT_ALIACE) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.ISSUER_CERT_ALIACE);
            configData.setIssuerCertAlias(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.ISSUER_KEY_PASSWORD) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.ISSUER_KEY_PASSWORD);
            configData.setIssuerKeyPassword(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.DEFAULT_EXPIRY_INTERVAL) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.DEFAULT_EXPIRY_INTERVAL);
            int interval = Integer.parseInt(paramValue);
            configData.setDefaultExpriyInterval(interval);
        }
        if (request.getParameter(XKMSMgtConstants.DEFAULT_PRIVATE_KEY_PASSWORD) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.DEFAULT_PRIVATE_KEY_PASSWORD);
            configData.setDefaultPrivateKeyPassword(paramValue);
        }
        if (request.getParameter(XKMSMgtConstants.ENABLE_PERSISTENCE) != null) {
            paramValue = request.getParameter(XKMSMgtConstants.ENABLE_PERSISTENCE);
            configData.setPersistenceEnabled(Boolean.parseBoolean(paramValue));
        }

        return configData;
    }

    /**
     * @param value input string 
     * @return  the input string if not null, empty string if it is null
     */
    public static String getInputFieldValue(String value){

        if (value != null) {
            return value;
        } else {
            return "";
        }        
    }


}
