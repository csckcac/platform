package org.wso2.carbon.appfactory.project.mgt.internal;

import org.wso2.carbon.appfactory.project.mgt.service.ProjectInfoBean;
import org.wso2.carbon.appfactory.project.mgt.service.ProjectManagementService;
import org.wso2.carbon.appfactory.project.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;


public class ProjectManagementServiceImpl extends AbstractAdmin implements ProjectManagementService {
    @Override
    public String createProject(ProjectInfoBean project) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        UserStoreManager userStoreManager;
        try {
            userStoreManager= (UserStoreManager) Util.getRealmService().getTenantUserRealm(0);
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Tenant tenant=new Tenant();
        tenant.setDomain(project.getProjectKey());
        tenant.setAdminName(tenant.getAdminName());
        tenant.setActive(true);
        try {
            tenantManager.addTenant(tenant);
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
       //To change body of implemented methods use File | Settings | File Templates.
        return null;
    }

    @Override
    public boolean addUserToProject(String projectKey, String userName) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeUserFromProject(String projectKey, String userName) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean revokeProject(String projectKey) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isProjectKeyAvailable(String projectKey) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEmailOfUser(String userName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
