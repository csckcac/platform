/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.identity.sso.saml.ui.util;

public class SAMLSSOUIUtil {

    private static int singleLogoutRetryCount = 5;
    private static long singleLogoutRetryInterval = 60000;

    public static int getSingleLogoutRetryCount() {
        return singleLogoutRetryCount;
    }

    public static void setSingleLogoutRetryCount(int singleLogoutRetryCount) {
        SAMLSSOUIUtil.singleLogoutRetryCount = singleLogoutRetryCount;
    }

    public static long getSingleLogoutRetryInterval() {
        return singleLogoutRetryInterval;
    }

    public static void setSingleLogoutRetryInterval(long singleLogoutRetryInterval) {
        SAMLSSOUIUtil.singleLogoutRetryInterval = singleLogoutRetryInterval;
    }

    /**
     * This check if the status code is 2XX, check value between 200 and 300
     * @param status
     * @return
     */
    public static boolean isHttpSuccessStatusCode(int status) {
        return status >= 200 && status < 300;
    }
}
