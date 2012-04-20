package org.apache.hadoop.security;
 
public class UserGroupInformationThreadLocal {
	private static ThreadLocal<UserGroupInformation> ugiThreadLocal = new ThreadLocal<UserGroupInformation>();
	public static void set(UserGroupInformation ugi) {
		ugiThreadLocal.set(ugi);
	}
	public static UserGroupInformation get() {
		return ugiThreadLocal.get();
	}
}
