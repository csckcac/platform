package org.wso2.carbon.appfactory.project.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.project.mgt.service.ProjectInfoBean;
import org.wso2.carbon.appfactory.project.mgt.service.ProjectManagementService;
import org.wso2.carbon.appfactory.project.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;


public class ProjectManagementServiceImpl extends AbstractAdmin  {
    private static Log log= LogFactory.getLog(ProjectManagementServiceImpl.class);
   
    public String createProject(ProjectInfoBean project) {
        RealmService realmService=Util.getRealmService();
        TenantManager tenantManager=realmService.getTenantManager();
        Tenant tenant=new Tenant();
        tenant.setDomain(project.getProjectKey());
        tenant.setAdminName(project.getOwnerUserName());
        tenant.setActive(true);
        try {
            RealmConfiguration realmConfig = realmService.getBootstrapRealmConfiguration();
            TenantMgtConfiguration tenantMgtConfiguration = realmService.getTenantMgtConfiguration();
            MultiTenantRealmConfigBuilder builder = realmService.getMultiTenantRealmConfigBuilder();
            RealmConfiguration realmConfigToPersist =
                    builder.getRealmConfigForTenantToPersist(realmConfig, tenantMgtConfiguration,
                                                             tenant, -1);
            tenant.setRealmConfig(realmConfigToPersist);

        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String msg="Tenant creation is failed to "+tenant.getDomain();
            e.printStackTrace();
        }
        try {
            tenantManager.addTenant(tenant);
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
       //To change body of implemented methods use File | Settings | File Templates.
        return tenant.getDomain();
    }

   
    public boolean addUserToProject(String projectKey, String userName) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        String roles[]={"admin"};
        try {
            UserRealm realm=Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(projectKey));
            realm.getUserStoreManager().addUser(userName,null,roles,null,null);
            return true;
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;  
    }

    
    public boolean removeUserFromProject(String projectKey, String userName) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        try {
            UserRealm realm=Util.getRealmService().getTenantUserRealm(tenantManager.getTenantId(projectKey));
            realm.getUserStoreManager().deleteUser(userName);
            return true;
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

   
    public boolean revokeProject(String projectKey) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        try {
            tenantManager.deleteTenant(tenantManager.getTenantId(projectKey));
            return  true;
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }


    public boolean isProjectKeyAvailable(String projectKey) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        int tenantID=-1;
        try {
            tenantID=tenantManager.getTenantId(projectKey) ;
        } catch (UserStoreException e) {
           
        }
        return tenantID<0;  
    }
                                                                                                                                
  
    public String getEmailOfUser(String userName) {
        TenantManager tenantManager=Util.getRealmService().getTenantManager();
        try {
            UserRealm realm=Util.getRealmService().getTenantUserRealm(0);
            String email=realm.getUserStoreManager().getUserClaimValue(userName,"http://wso2.org/claims/emailaddress",null);
            return email;
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
