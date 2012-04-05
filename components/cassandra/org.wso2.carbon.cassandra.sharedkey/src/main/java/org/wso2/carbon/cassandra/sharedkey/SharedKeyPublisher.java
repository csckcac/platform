package org.wso2.carbon.cassandra.sharedkey;

import org.wso2.carbon.cassandra.server.CarbonCassandraAuthenticator;
import org.wso2.carbon.cassandra.sharedkey.internal.CassandraSharedKeyDSComponent;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class SharedKeyPublisher {

	public void injectAccessKey(String username, String password, String targetUser, 
			                                               String accessKey) throws Exception {
		RealmService realmService = CassandraSharedKeyDSComponent.getRealmService();
		if (realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
				         getUserStoreManager().authenticate(username, password)) {
			CarbonCassandraAuthenticator.addToCache(targetUser, accessKey);
		}
		return;
	}
}
