package org.wso2.carbon.appfactory.hostobjects.configuration;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;

/**
 * This is a host object used to get app factory configuration.
 * This will not expose all the configuration details as it is available in java script code.
 */
public class AppFactoryConfigurationReader extends ScriptableObject {
    private static final String hostObjectName = "AppFactoryConfigurationReader";
    private static AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigurationHolder.getInstance().getAppFactoryConfiguration();

    @Override
    public String getClassName() {
        return hostObjectName;
    }

 public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr)
            throws Exception {
        return new AppFactoryConfigurationReader();
    }

    public String jsFunction_getSsoRelyingPartyName() throws Exception {
        return appFactoryConfiguration.getSsoRelyingPartyName();
    }

    public String jsFunction_getSsoIdentityAlias() throws Exception {
        return appFactoryConfiguration.getSsoIdentityAlias();
    }

    public String jsFunction_getSsoIdentityProviderEpr() throws Exception {
        return appFactoryConfiguration.getSsoIdentityProviderEpr();
    }

    public String jsFunction_getSsoKeyStoreName() throws Exception {
        return appFactoryConfiguration.getSsoKeyStoreName();
    }

    public String jsFunction_getSsoKeyStorePassword() throws Exception {
        return appFactoryConfiguration.getSsoKeyStorePassword();
    }

    public String jsFunction_getWebServiceEPRActivateUser() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRActivateUser();
    }

    public String jsFunction_getWebServiceEPRCreateApplication() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRCreateApplication();
    }

    public String jsFunction_getWebServiceEPRCreateRepo() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRCreateRepo();
    }

    public String jsFunction_getWebServiceEPRCreateUser() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRCreateUser();
    }

    public String jsFunction_getWebServiceEPREmailVerificationService() throws Exception {
        return appFactoryConfiguration.getWebServiceEPREmailVerificationService();
    }

    public String jsFunction_getWebServiceEPRAddUserToApplication() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRAddUserToApplication();
    }

    public String jsFunction_getWebServiceEPRGetRolesOfUserForApplication() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRGetRolesOfUserForApplication();
    }

    public String jsFunction_getWebServiceEPRGetUsersOfApplication() throws Exception {
        return appFactoryConfiguration.getWebServiceEPRGetUsersOfApplication();
    }
    public String jsFunction_getAdminUserName() throws Exception {
        return appFactoryConfiguration.getAdminUserName();
    }
    public String jsFunction_getAdminPassword() throws Exception {
        return appFactoryConfiguration.getAdminPassword();
    }

}
