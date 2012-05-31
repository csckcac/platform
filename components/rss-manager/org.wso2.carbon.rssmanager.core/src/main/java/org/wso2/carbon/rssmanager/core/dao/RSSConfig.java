/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.dao;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSDAOException;
import org.wso2.carbon.rssmanager.core.description.RSSInstance;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a WSO2 RSS configuration.
 */
public class RSSConfig {

	private String rssDatabaseDriver;
	
	private String rssDatabaseURL;
	
	private String rssUsername;
	
	private String rssPassword;
	
	private List<RSSInstance> rssInstances;

	@SuppressWarnings("unchecked")
	public RSSConfig(OMElement configDocument) throws RSSDAOException {
        Iterator<OMElement> tmpItr = configDocument.getChildrenWithLocalName("rss-database");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("The RSS database definition is missing");
		}
		OMElement rssDBEl = tmpItr.next();
		
		tmpItr = rssDBEl.getChildrenWithLocalName("driver");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Database driver is missing in RSS database definition");
		}
		OMElement tmpEl = tmpItr.next();
		this.rssDatabaseDriver = tmpEl.getText().trim();
		
		tmpItr = rssDBEl.getChildrenWithLocalName("url");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Database URL is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		this.rssDatabaseURL = tmpEl.getText().trim();
		
		tmpItr = rssDBEl.getChildrenWithLocalName("username");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Database username is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		this.rssUsername = tmpEl.getText().trim();
		
		tmpItr = rssDBEl.getChildrenWithLocalName("password");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Database password is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		this.rssPassword = tmpEl.getText().trim();
		
		/* populate WSO2 RSS instances */
		this.rssInstances = new ArrayList<RSSInstance>();
		OMElement wso2InstancesEl = (OMElement) configDocument.getChildrenWithLocalName("wso2-rss-instances").next();
		if (wso2InstancesEl != null) {
			Iterator<OMElement> instances = wso2InstancesEl.getChildrenWithLocalName("wso2-rss-instance");
			while (instances.hasNext()) {
				this.rssInstances.add(this.createRSSInstanceFromXMLConfig(instances.next()));
			}
		}
		
		/* do some jdbc initialization */
		try {
			Class.forName(this.getRssDatabaseDriver());
		} catch (ClassNotFoundException e) {
			throw new RSSDAOException("Cannot load JDBC driver", e);
		}
	}
	
	public Connection getRSSDBConnection() throws RSSDAOException {
		try {
			return DriverManager.getConnection(this.getRssDatabaseURL(), 
					this.getRssUsername(), this.getRssPassword());
		} catch (SQLException e) {
			throw new RSSDAOException("Error in creating new RSS database connection", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private RSSInstance createRSSInstanceFromXMLConfig(OMElement rssInstEl) throws RSSDAOException {
		Iterator<OMElement> tmpItr = rssInstEl.getChildrenWithLocalName("name");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server instance name is missing in RSS database definition");
		}
		OMElement tmpEl = tmpItr.next();
		String name = tmpEl.getText().trim();
		
		tmpItr = rssInstEl.getChildrenWithLocalName("server-url");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server instance URL is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
        String serverURL;
        try {
            serverURL = RSSManagerCommonUtil.validateRSSInstanceUrl(tmpEl.getText().trim());
        } catch (URISyntaxException e) {
            throw new RSSDAOException("Malfored RSS instance URL");
        }

        tmpItr = rssInstEl.getChildrenWithLocalName("dbms-type");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server instance DBMS type is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		String dbmsType = tmpEl.getText().trim();

        tmpItr = rssInstEl.getChildrenWithLocalName("server-category");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server category is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		String serverCategory = tmpEl.getText().trim();
		
		tmpItr = rssInstEl.getChildrenWithLocalName("service-username");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server instance adming username is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();		
		String adminUsername = tmpEl.getText().trim();
		
		tmpItr = rssInstEl.getChildrenWithLocalName("service-password");
		if (!tmpItr.hasNext()) {
			throw new RSSDAOException("Server instance service password is missing in RSS database definition");
		}
		tmpEl = tmpItr.next();
		String adminPassword = tmpEl.getText().trim();
		
		return new RSSInstance(0, name, serverURL, dbmsType, 
				RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE, serverCategory, adminUsername, adminPassword, 0);
	}

	public String getRssDatabaseDriver() {
		return rssDatabaseDriver;
	}

	public String getRssDatabaseURL() {
		return rssDatabaseURL;
	}

	public String getRssUsername() {
		return rssUsername;
	}

	public String getRssPassword() {
		return rssPassword;
	}

	public List<RSSInstance> getRssInstances() {
		return rssInstances;
	}

}
