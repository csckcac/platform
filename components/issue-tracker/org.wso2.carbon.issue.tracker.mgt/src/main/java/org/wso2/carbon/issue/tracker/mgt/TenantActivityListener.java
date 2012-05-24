package org.wso2.carbon.issue.tracker.mgt;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.issue.tracker.adapter.api.GenericCredentials;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.core.AccountInfo;
import org.wso2.carbon.issue.tracker.mgt.internal.LdapGroupManager;
import org.wso2.carbon.issue.tracker.mgt.internal.OTUserAssociation;
import org.wso2.carbon.jira.reporting.adapterImpl.SupportJiraUser;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  this class implements methods to be called on tenant activities such as tenant registration, activation
 * and deactivation.
 */
public class TenantActivityListener implements TenantMgtListener {

    private static Log log = LogFactory.getLog(TenantActivityListener.class);
    private static final int EXEC_ORDER = 60;

    // user should be removed from JIRA user groups
    public void onTenantDeactivation(int i) {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        if (tenantId != 0) {

            IssueTrackerAdmin admin = new IssueTrackerAdmin();
            try {
                AccountInfo accountInfo = admin.getAccountWhenService();
                String email = accountInfo.getEmail();
                String username = accountInfo.getUid();

                SupportJiraUser user = new SupportJiraUser();
                user.setEmail(email);
                user.setUsername(username);
                user.setCredentials(accountInfo.getCredentials());

                // get admin auth token
                OTUserAssociation otUserAssociation = new LdapGroupManager();
                String authToken = otUserAssociation.getJiraAdminAuthToken();

                GenericCredentials credentials = accountInfo.getCredentials();

                // if user hasnt created an account credentials are null, in such case use default jira url
                String url = null;
                if (credentials != null) {
                    url = credentials.getUrl();
                }

                if (null == url || "".equals(url)) {
                    url = admin.getJiraUrlFromConfig();
                }

                otUserAssociation.removeUserFromAllGroups(authToken, user, url);

            } catch (IssueTrackerException e) {
                log.error("Error removing the user from support JIRA user groups. Reason : " + e.getMessage(), e);
            }

        }

    }


    public void onTenantActivation(int i) throws StratosException {
        // we don't do anything at activation time
    }


    // switch groups
    public void onSubscriptionPlanChange(int i, String oldPlan, String newPlan) {

        //only if this request comes from a tenant we should allow.upon super tenant changing the subscription of 
        // a tenant this logic will not be called. this restriction is due to following reasons.
        // 1. tenant can be associated with support/OT with any email,this need not to be necessarily the one used for registering
        // 2. there is no way to get the OT details of a tenant at the moment by super tenant

        //todo : implement this to automate user group switching for tenants by super tenant

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        if (tenantId != 0) {

            IssueTrackerAdmin admin = new IssueTrackerAdmin();
            try {
                AccountInfo accountInfo = admin.getAccountWhenService();

                if(null !=accountInfo){

                String email = accountInfo.getEmail();
                String username = accountInfo.getUid();

                SupportJiraUser user = new SupportJiraUser();
                user.setEmail(email);
                user.setUsername(username);
                user.setCredentials(accountInfo.getCredentials());

                // get admin auth token
                OTUserAssociation otUserAssociation = new LdapGroupManager();
                String authToken = otUserAssociation.getJiraAdminAuthToken();

                GenericCredentials credentials = accountInfo.getCredentials();

                // if user hasn't created an account credentials are null, in such case use default jira url
                String url = null;
                if (credentials != null) {
                    url = credentials.getUrl();
                }


                boolean isSubscriptionFree = admin.isTenantSubscriptionFree(i);

                if (null == url || "".equals(url)) {
                    url = admin.getJiraUrlFromConfig();
                }

                // first we need to remove the user from the other group to prevent him from reporting issues, browsing the project etc
                otUserAssociation.removeUserFromGroup(authToken, user, isSubscriptionFree, url);

                //add user to relevant group
                otUserAssociation.addUserToGroup(authToken, user, isSubscriptionFree, url);
                }

            } catch (IssueTrackerException e) {
                log.error("Error changing support JIRA user group upon subscription plan change. Reason : " + e.getMessage(), e);
            }

        }
    }

    public void onTenantInitialActivation(int arg0) throws StratosException {
        // Nothing to do
        
    }

    public int getListenerOrder() {
        return EXEC_ORDER;
    }

    public void onTenantCreate(TenantInfoBean arg0) throws StratosException {
        // Nothing to do
        
    }

    public void onTenantRename(int arg0, String arg1, String arg2) throws StratosException {
        // Nothing to do
        
    }

    public void onTenantUpdate(TenantInfoBean arg0) throws StratosException {
        // Nothing to do
        
    }
}
