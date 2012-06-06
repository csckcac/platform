/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.appfactory.user.registration.util;

import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;

/**
 * A static class for holding EmailVerificationSubscriber
 */
public class Util {
    private static EmailVerifcationSubscriber emailVerificationService = null;
    private static AppFactoryConfiguration configuration;

    public static synchronized void setEmailVerificationService(
            EmailVerifcationSubscriber emailService) {
        Util.emailVerificationService = emailService;
    }

    public static EmailVerifcationSubscriber getEmailVerificationService() {
        return emailVerificationService;
    }

    public static AppFactoryConfiguration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(AppFactoryConfiguration configuration) {
        Util.configuration = configuration;
    }
}
