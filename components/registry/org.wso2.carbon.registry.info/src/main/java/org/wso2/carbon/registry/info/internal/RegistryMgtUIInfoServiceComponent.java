/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.info.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;
import org.wso2.carbon.registry.info.Utils;

/**
 * @scr.component name="org.wso2.carbon.registry.info" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="registry.eventing.service"
 * interface="org.wso2.carbon.registry.eventing.services.EventingService" cardinality="0..1"
 * policy="dynamic" bind="setRegistryEventingService" unbind="unsetRegistryEventingService"
 * @scr.reference name="registry.subscription.email.verification.service"
 * interface="org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService" cardinality="0..1"
 * policy="dynamic" bind="setSubscriptionEmailVerficationService" unbind="unsetSubscriptionEmailVerficationService"
 */
public class RegistryMgtUIInfoServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIInfoServiceComponent.class);

    private ServiceRegistration infoServiceRegistration = null;

    protected void activate(ComponentContext context) {
        // TODO: uncomment when the backend-frontend seperation when running in same vm is completed
        // infoServiceRegistration = context.getBundleContext().registerService(
        //        IInfoService.class.getName(), new InfoService(), null);
        log.debug("******* Registry Info Management bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (infoServiceRegistration != null) {
            infoServiceRegistration.unregister();
            infoServiceRegistration = null;
        }
        log.debug("******* Registry Info UI Management bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setRegistryEventingService(EventingService eventingService) {
        Utils.setRegistryEventingService(eventingService);
        log.debug("Successfully set registry eventing service");
    }

    protected void unsetRegistryEventingService(EventingService eventingService) {
        Utils.setRegistryEventingService(null);
    }

    protected void setSubscriptionEmailVerficationService(SubscriptionEmailVerficationService
            subscriptionEmailVerficationService) {
        Utils.setSubscriptionEmailVerficationService(subscriptionEmailVerficationService);
        log.debug("Successfully set subscription e-mail verification service");
    }

    protected void unsetSubscriptionEmailVerficationService(SubscriptionEmailVerficationService
            subscriptionEmailVerficationService) {
        Utils.setSubscriptionEmailVerficationService(null);
    }
}
