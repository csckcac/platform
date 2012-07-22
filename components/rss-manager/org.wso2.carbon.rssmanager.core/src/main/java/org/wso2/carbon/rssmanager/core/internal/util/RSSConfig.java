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
package org.wso2.carbon.rssmanager.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSManagerFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a WSO2 RSS configuration.
 */
public class RSSConfig {

    private List<RSSInstance> systemRSSInstances;

    private DataSource dataSource;

    private String rssType;

    private RSSManager rssManager;

    private static RSSConfig currentRSSConfig;

    private static final Log log = LogFactory.getLog(RSSConfig.class);

    /**
     * Retrieves the RSS config reading the rss-instance configuration file.
     *
     * @return RSSConfig
     */
    public static synchronized RSSConfig getInstance() throws RSSManagerException {
        if (currentRSSConfig == null) {
            throw new RSSManagerException("RSS configuration is not initialized and is null");
        }
        return currentRSSConfig;
    }

    public static void init() throws RSSManagerException {
        String rssConfigXMLPath = CarbonUtils.getCarbonConfigDirPath()
                + File.separator + "etc" + File.separator
                + RSSManagerConstants.RSS_CONFIG_XML_NAME;
        try {
            currentRSSConfig = new RSSConfig(AXIOMUtil.stringToOM(
                    new String(CarbonUtils.getBytesFromFile(new File(rssConfigXMLPath)))));
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while initializing RSS config", e);
        }
    }

    @SuppressWarnings("unchecked")
    private RSSConfig(OMElement configEl) throws RSSManagerException {
        Iterator<OMElement> tmpItr = configEl.getChildrenWithLocalName("rss-type");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("RSS type is missing");
        }
        OMElement rssTypeEl = tmpItr.next();
        this.rssType = rssTypeEl.getText().trim();
        this.rssManager = RSSManagerFactory.getRSSManager(this.getRssType());

        tmpItr = configEl.getChildrenWithLocalName("rss-mgt-repository");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("RSS management repository configuration is missing");
        }

        OMElement rssMgtRepositoryConfigEl = tmpItr.next();
        tmpItr = rssMgtRepositoryConfigEl.getChildrenWithLocalName("datasource-config");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("RSS management repository datasource configuration " +
                    "is missing");
        }
        OMElement dsEl = tmpItr.next();
        this.dataSource = RSSManagerUtil.createDataSource(dsEl);

        /* populate WSO2 RSS instances */
        this.systemRSSInstances = new ArrayList<RSSInstance>();
        OMElement systemRSSInstancesEl =
                (OMElement) configEl.getChildrenWithLocalName("system-rss-instances").next();
        if (systemRSSInstancesEl != null) {
            Iterator<OMElement> instances =
                    systemRSSInstancesEl.getChildrenWithLocalName("system-rss-instance");
            while (instances.hasNext()) {
                this.systemRSSInstances.add(this.createRSSInstanceFromXMLConfig(instances.next()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private RSSInstance createRSSInstanceFromXMLConfig(
            OMElement rssInstEl) throws RSSManagerException {
        Iterator<OMElement> tmpItr = rssInstEl.getChildrenWithLocalName("name");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server instance name is missing in RSS database " +
                    "definition");
        }
        OMElement tmpEl = tmpItr.next();
        String name = tmpEl.getText().trim();

        tmpItr = rssInstEl.getChildrenWithLocalName("dbms-type");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server instance DBMS type is missing in RSS database " +
                    "definition");
        }
        tmpEl = tmpItr.next();
        String dbmsType = tmpEl.getText().trim();

        tmpItr = rssInstEl.getChildrenWithLocalName("server-category");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server category is missing in RSS database definition");
        }
        tmpEl = tmpItr.next();
        String serverCategory = tmpEl.getText().trim();

        tmpItr = rssInstEl.getChildrenWithLocalName("admin-datasource-config");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Administrative datasource configuration of the RSS " +
                    "instance is missing");
        }
        OMElement adminDSConfigEl = tmpItr.next();
        tmpItr = adminDSConfigEl.getChildrenWithLocalName("url");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server instance URL is missing in RSS database " +
                    "definition");
        }
        tmpEl = tmpItr.next();
        String serverURL;
        try {
            serverURL = RSSManagerUtil.validateRSSInstanceUrl(tmpEl.getText().trim());
        } catch (Exception e) {
            throw new RSSManagerException("Malformed RSS instance URL");
        }

        tmpItr = adminDSConfigEl.getChildrenWithLocalName("username");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server instance admin username is missing in RSS " +
                    "database definition");
        }
        tmpEl = tmpItr.next();
        String adminUsername = tmpEl.getText().trim();

        tmpItr = adminDSConfigEl.getChildrenWithLocalName("password");
        if (!tmpItr.hasNext()) {
            throw new RSSManagerException("Server instance service admin password is missing " +
                    "in RSS database definition");
        }
        tmpEl = tmpItr.next();
        String adminPassword = tmpEl.getText().trim();

        return new RSSInstance(name, serverURL, dbmsType,
                RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE, serverCategory, adminUsername,
                adminPassword, MultitenantConstants.SUPER_TENANT_ID);
    }

    public Connection getRSSDBConnection() throws RSSManagerException {
        if (this.getDataSource() == null) {
            throw new RSSManagerException("RSS manager repository datasource is not initialized");
        }
        try {
            return this.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RSSManagerException("Error acquiring a connection from RSS metadata " +
                    "repository datasource", e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public List<RSSInstance> getSystemRSSInstances() {
        return systemRSSInstances;
    }

    public String getRssType() {
        return rssType;
    }

    public RSSManager getRssManager() {
        return rssManager;
    }

}
