/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.auth;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.wso2.carbon.dataservices.core.DataServiceFault;

/**
 * This class represents a dynamic user authenticator based on a static XML configuration.
 */
public class ConfigurationBasedAuthenticator implements DynamicUserAuthenticator {

	private Map<String, String[]> credentialsMap;
	
	public ConfigurationBasedAuthenticator(String xmlConfig) throws DataServiceFault {
		try {
			JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			Configuration conf = (Configuration) unmarshaller.unmarshal(
					new ByteArrayInputStream(xmlConfig.getBytes()));
			this.credentialsMap = new HashMap<String, String[]>();
			for (Entry entry : conf.getEntries()) {
				this.credentialsMap.put(entry.getRequest(), 
						new String[] { entry.getUsername(), entry.getPassword() });
			}
		} catch (Exception e) {
			throw new DataServiceFault(e, 
					"Error in creating ConfigurationBasedAuthenticator: " + e.getMessage());
		}
	}
	
	public Map<String, String[]> getCredentialsMap() {
		return credentialsMap;
	}

	@Override
	public String[] lookupCredentials(String user) throws DataServiceFault {
		return this.getCredentialsMap().get(user);
	}
	
	@XmlRootElement (name = "configuration")
	public class Configuration {
		
		private List<Entry> entries;

		@XmlElement (name = "entry")
		public List<Entry> getEntries() {
			return entries;
		}

		public void setEntries(List<Entry> entries) {
			this.entries = entries;
		}
		
	}
	
	public class Entry {
		
		private String request;
		
		private String username;
		
		private String password;

		@XmlAttribute (name = "request", required = true)
		public String getRequest() {
			return request;
		}

		public void setRequest(String request) {
			this.request = request;
		}

		@XmlElement (name = "username")
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@XmlElement (name = "password")
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
	}

}
