package org.wso2.carbon.stratos.common.events;

import org.wso2.carbon.stratos.common.exception.StratosException;

public interface StratosEventListener {

    public void onTenantActivation(int tenantId) throws StratosException;
    
    public void onTenantDeactivation(int tenantId) throws StratosException;

    public void onSubscriptionPlanChange(int tenentId, String oldPlan, String newPlan) throws StratosException;

}
