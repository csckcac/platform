package org.apache.hadoop.security;

/**
 * This interface has to be implemented by a provider that looks for a custom ticket cache.
 * This interface is added to WSO2's carbonized hadoop version to get the ticket cache 
 * asscociated with the tenant submitting Hadoop jobs or interacting with HDFS.
 **/

public interface Krb5TicketCacheFinder {
	/**
	 * Returns the ticket cache of the calling tenant.
	 * Only applicable to files system based Kerberos ticket caches.
	 */
	public String getTenantTicketCache();
}