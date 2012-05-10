package org.wso2.carbon.appfactory.user.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.user.registration.util.Util;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.user.registration" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="emailverification.service" interface=
 *                "org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setEmailVerificationService"
 *                unbind="unsetEmailVerificationService"
 *
 */
public class UserRegistrationServiceComponent {
    private static Log log = LogFactory.getLog(UserRegistrationServiceComponent.class);
    private static ConfigurationContextService configContextService = null;

    protected void activate(ComponentContext context) {


            log.debug("*******  bundle is activated ******* ");

    }

    protected void deactivate(ComponentContext context) {

        log.debug("*******  bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        Util.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        configContextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
    }


    protected void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(emailService);
    }

    protected void unsetEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(null);
    }


}
