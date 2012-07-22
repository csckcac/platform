/*
 * Copyright The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.logging.service.data;

public class LoggingConfig {
	private String keyspace;
	private String user;
	private String password;
	private String colFamily;
	private String url;
	private boolean isCassandraServerAvailable;
	private String cluster;
	private String publisherURL;
	private String publisherUser;
	private String publisherPassword;
	private String archivedHost;
	private String archivedUser;
	private String archivedPassword;
	private String archivedPort;
	private String archivedRealm;
	
	public LoggingConfig() {

	}

	public LoggingConfig(String keyspace, String user, String password, String colFamily,
			String url) {
		super();
		this.keyspace = keyspace;
		this.user = user;
		this.password = password;
		this.colFamily = colFamily;
		this.url = url;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getColFamily() {
		return colFamily;
	}

	public void setColFamily(String colFamily) {
		this.colFamily = colFamily;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isCassandraServerAvailable() {
		return isCassandraServerAvailable;
	}

	public void setCassandraServerAvailable(boolean isCassandraServerAvailable) {
		this.isCassandraServerAvailable = isCassandraServerAvailable;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getPublisherURL() {
		return publisherURL;
	}

	public void setPublisherURL(String publisherURL) {
		this.publisherURL = publisherURL;
	}

	public String getPublisherUser() {
		return publisherUser;
	}

	public void setPublisherUser(String publisherUser) {
		this.publisherUser = publisherUser;
	}

	public String getPublisherPassword() {
		return publisherPassword;
	}

	public void setPublisherPassword(String publisherPassword) {
		this.publisherPassword = publisherPassword;
	}

	public String getArchivedHost() {
		return archivedHost;
	}

	public void setArchivedHost(String archivedHost) {
		this.archivedHost = archivedHost;
	}

	public String getArchivedUser() {
		return archivedUser;
	}

	public void setArchivedUser(String archivedUser) {
		this.archivedUser = archivedUser;
	}

	public String getArchivedPassword() {
		return archivedPassword;
	}

	public void setArchivedPassword(String archivedPassword) {
		this.archivedPassword = archivedPassword;
	}

	public String getArchivedPort() {
		return archivedPort;
	}

	public void setArchivedPort(String archivedPort) {
		this.archivedPort = archivedPort;
	}

	public String getArchivedRealm() {
		return archivedRealm;
	}

	public void setArchivedRealm(String archivedRealm) {
		this.archivedRealm = archivedRealm;
	}

	
}
