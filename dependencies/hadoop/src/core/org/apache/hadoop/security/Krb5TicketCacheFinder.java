package org.apache.hadoop.security;

import org.apache.hadoop.conf.Configuration;

//WSO2 Fix: Read the comment below

/**
 * This interface has to be implemented by a provider that looks for a custom ticket cache.
 * This interface is added to WSO2's carbonized hadoop version to get the ticket cache 
 * asscociated with the tenant submitting a Hadoop jobs or interacting with HDFS.
 **/

public interface Krb5TicketCacheFinder {
	/**
         * Set current Hadoop Configuration
	 */
	//public void setConf(Configuration conf);

	/**
	 * Get current hadoop Configuration
	 */
	//public Configuration getConf();

	/**
	 * Returns the ticket cache of the calling tenant.
	 * Only applicable to files system based Kerberos ticket caches.
	 */
	public String getTenantTicketCache();
}
