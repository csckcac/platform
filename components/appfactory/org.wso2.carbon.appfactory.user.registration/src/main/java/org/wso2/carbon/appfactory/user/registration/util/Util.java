package org.wso2.carbon.appfactory.user.registration.util;

import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: aja
 * Date: 4/26/12
 * Time: 6:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    private static RegistryService registryService;
    private static RealmService realmService;
    private static EmailVerifcationSubscriber emailVerificationService = null;
    private static EmailVerifierConfig emailVerfierConfig = null;


    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static synchronized void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        if(emailVerificationService==null){
            emailVerificationService=emailService;
        }
    }



    public static synchronized void setRegistryService(RegistryService registryService) {
        if(registryService==null){
            registryService=registryService;
        }
    }

    public static synchronized void setRealmService(RealmService realmService) {
        if(realmService==null){
            realmService=realmService;
        }
    }

    public static EmailVerifcationSubscriber getEmailVerificationService() {
        return emailVerificationService;
    }

    public static EmailVerifierConfig getEmailVerfierConfig() {
        return emailVerfierConfig;
    }
}
