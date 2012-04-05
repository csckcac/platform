package org.wso2.carbon.cep.admin.internal.util;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cep.core.CEPServiceInterface;

/**
 * this class is used to get the CEPServiceInterface service. it is used to send the
 * requests received from the Admin service to real cep engine
 * @scr.component name="cepadmin.component" immediate="true"
 * @scr.reference name="cep.service"
 * interface="org.wso2.carbon.cep.core.CEPServiceInterface" cardinality="1..1"
 * policy="dynamic" bind="setCEPService" unbind="unSetCEPService"
 */

public class CEPAdminDS {
       protected void activate(ComponentContext context) {

       }

       protected void setCEPService(CEPServiceInterface cepService) {
           CEPAdminDSHolder.getInstance().registerCEPService(cepService);
       }

       protected void unSetCEPService(CEPServiceInterface cepService) {
           CEPAdminDSHolder.getInstance().unRegisterCEPService(cepService);
       }

}
