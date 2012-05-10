package org.wso2.carbon.appfactory.project.mgt.util;


import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

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

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
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


}
