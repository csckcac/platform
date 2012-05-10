package org.wso2.carbon.appfactory.user.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.user.registration.util.Util;
import org.wso2.carbon.email.sender.api.EmailSenderConfiguration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.email.sender.api.EmailSender;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EmailSenderService {
    private static final Log log=LogFactory.getLog(EmailSenderService.class);

    public boolean sendActivationEmail(String userName, String firstName, String activationKey,
                                 String email,String projectName,String projectKey,String config) {
        EmailVerifcationSubscriber emailverifier = Util.getEmailVerificationService();
        Map<String,String> userProperties=new HashMap<String, String>();
        Map<String, String> datatostore = new HashMap<String, String>();
        datatostore.put("first-name",
                        firstName);
        datatostore.put("email", email);
        datatostore.put("admin", userName);
        datatostore.put("confirmationKey", activationKey);

        try {
            emailverifier.requestUserVerification(datatostore,loadEmailVerificationConfig(config));
        } catch (Exception e) {
            log.error("Email sending is failed ",e);
        }
        return true;

    }
    public boolean sendMail(String userName, String firstName,String email,String projectName,
                            String projectKey,String config) {
       EmailSender sender;
        sender = new EmailSender(loadEmailSenderConfiguration(config));
        Map<String,String> userParams=new HashMap<String, String>();
        userParams.put("userName",userName);
        userParams.put("firstName",firstName);
        userParams.put("projectName",projectName);
        userParams.put("projectKey",projectKey);
        try {
            sender.sendEmail(email,userParams);
        } catch (Exception e) {
          log.error("Email sending is failed ",e);

        }
        return true;

    }
    private EmailVerifierConfig loadEmailVerificationConfig(String configFile) {
        String configFilePath= CarbonUtils.getCarbonConfigDirPath()+File.separator+"email"+
                               File.separator+configFile;
        EmailVerifierConfig emailVerfierConfig=
           org.wso2.carbon.email.verification.util.Util.loadeMailVerificationConfig(configFilePath);
        return   emailVerfierConfig;
    }
    private EmailSenderConfiguration loadEmailSenderConfiguration(String configFile) {
        String configFilePath= CarbonUtils.getCarbonConfigDirPath()+File.separator+"email"+
                               File.separator+configFile;
        EmailSenderConfiguration emailVerfierConfig=
                EmailSenderConfiguration.loadEmailSenderConfiguration(configFile);
        return   emailVerfierConfig;
    }
}
