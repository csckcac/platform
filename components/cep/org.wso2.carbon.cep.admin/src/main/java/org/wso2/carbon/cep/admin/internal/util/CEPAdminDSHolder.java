package org.wso2.carbon.cep.admin.internal.util;

import org.wso2.carbon.cep.core.CEPServiceInterface;

/**
 * This class is used to keep a reference to CEPServiceInterface so that
 * it can be accessed from the CEPAdminService
 */
public class CEPAdminDSHolder {

    private CEPServiceInterface cepService;

    private static CEPAdminDSHolder instance = new CEPAdminDSHolder();

    private CEPAdminDSHolder(){

    }

    public static CEPAdminDSHolder getInstance(){
        return instance;
    }

    public CEPServiceInterface getCEPService(){
        return this.cepService;
    }

    public void registerCEPService(CEPServiceInterface cepService){
        this.cepService = cepService;
    }

     public void unRegisterCEPService(CEPServiceInterface cepService){
        this.cepService = null;
    }

}
