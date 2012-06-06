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
package org.wso2.carbon.appfactory.user.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.user.registration.util.Util;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;

/**
 * @scr.component name="org.wso2.carbon.appfactory.user.registration" immediate="true"
 * @scr.reference name="emailverification.service" interface=
 * "org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 * cardinality="1..1" policy="dynamic"
 * bind="setEmailVerificationService"
 * unbind="unsetEmailVerificationService"
 * @scr.reference name="appfactory.common"
 * interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration" cardinality="1..1"
 * policy="dynamic" bind="setAppFactoryConfiguration" unbind="unsetAppFactoryConfiguration"
 */
public class UserRegistrationServiceComponent {
    private static Log log = LogFactory.getLog(UserRegistrationServiceComponent.class);

    protected void activate(ComponentContext context) {
        log.debug("*******UserRegistration Service  bundle is activated ******* ");

    }

    protected void deactivate(ComponentContext context) {

        log.debug("*******UserRegistration Service  bundle is deactivated ******* ");
    }

    protected void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(emailService);
    }

    protected void unsetEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        Util.setConfiguration(appFactoryConfiguration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        Util.setConfiguration(null);
    }

}
