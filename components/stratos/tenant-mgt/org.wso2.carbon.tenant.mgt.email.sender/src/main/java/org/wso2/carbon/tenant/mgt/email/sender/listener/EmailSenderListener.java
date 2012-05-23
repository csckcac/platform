package org.wso2.carbon.tenant.mgt.email.sender.listener;

import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.tenant.mgt.email.sender.util.TenantMgtEmailSenderUtil;
import org.wso2.carbon.user.core.UserStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EmailSenderListener implements TenantMgtListener {
    
    private static final int EXEC_ORDER = 20;
    private static final Log log = LogFactory.getLog(EmailSenderListener.class);

    public void addTenant(TenantInfoBean tenantInfoBean) throws UserStoreException {
        try {
            TenantMgtEmailSenderUtil.sendEmail(tenantInfoBean);
        } catch (Exception e) {
            String message = "Error sending tenant creation Mail to tenant domain " 
                + tenantInfoBean.getTenantDomain();
            log.error(message, e);
            throw new UserStoreException(message, e);
        }
        TenantMgtEmailSenderUtil.notifyTenantCreationToSuperAdmin(tenantInfoBean);
    }

    public int getListenerOrder() {
        return EXEC_ORDER;
    }

    public void renameTenant(int tenantId, String oldDomainName, 
                             String newDomainName) throws UserStoreException {
        // Do nothing. 

    }

    public void updateTenant(TenantInfoBean tenantInfoBean) throws UserStoreException {
        if ((tenantInfoBean.getAdminPassword() != null) && 
                (!tenantInfoBean.getAdminPassword().equals(""))) {
            try {
                TenantMgtEmailSenderUtil.notifyResetPassword(tenantInfoBean);
            } catch (Exception e) {
                String message = "Error sending tenant update Mail to tenant domain " 
                    + tenantInfoBean.getTenantDomain();
                log.error(message, e);
                throw new UserStoreException(message, e);
            }
        }
    }

}
