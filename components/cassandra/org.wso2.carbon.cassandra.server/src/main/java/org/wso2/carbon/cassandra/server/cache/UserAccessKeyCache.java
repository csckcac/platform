package org.wso2.carbon.cassandra.server.cache;

import org.wso2.carbon.caching.core.BaseCache;

public class UserAccessKeyCache extends BaseCache {

	public final static String CASSANDRA_ACCESS_KEY_CACHE = "CASSANDRA_ACCESS_KEY_CACHE";

	private static UserAccessKeyCache accessKeyCache = null;

	private UserAccessKeyCache() {
		super(CASSANDRA_ACCESS_KEY_CACHE);
	}

	public synchronized static UserAccessKeyCache getInstance() {
		if (accessKeyCache == null) {
			accessKeyCache = new UserAccessKeyCache();
		}
		return accessKeyCache;
	}

}
