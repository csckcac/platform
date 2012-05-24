package org.apache.hadoop.security;
 
//WSO2 Fix: This is the key to solving the problem of Hadoop when it comes to running JobTracker, NameNode, JobClient on the same JVM
//with multiple Kerberos pricipals active on the JVM simultaneously. This class helps us to keep static UserGroupInformation instances local
//to each thread and pass it down to threads spawned by that thread. It is up to the rest of the WSO2 modifications to the Hadoop to use this 
//feature appropriately.

public class UserGroupInformationThreadLocal {
	private static ThreadLocal<UserGroupInformation> ugiThreadLocal = new InheritableThreadLocal<UserGroupInformation>();
	public static void set(UserGroupInformation ugi) {
		ugiThreadLocal.set(ugi);
	}
	public static UserGroupInformation get() {
		return ugiThreadLocal.get();
	}
        public static void remove() {
                ugiThreadLocal.remove();
        }
}
