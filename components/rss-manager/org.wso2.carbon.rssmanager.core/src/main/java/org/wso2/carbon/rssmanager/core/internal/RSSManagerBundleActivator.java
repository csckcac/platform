/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.rssmanager.core.RSSManagerUtil;
import org.wso2.carbon.rssmanager.core.dao.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.description.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;
import org.wso2.carbon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Console bundle activator implementation class.
 */
public class RSSManagerBundleActivator implements BundleActivator {

    private static Log log= LogFactory.getLog(RSSManagerBundleActivator.class);

    /**
     * Starts the Admin Console bundle.
     * @param bundleContext BundleContext
     * @throws Exception Exception
     */
    public void start(BundleContext bundleContext) throws Exception {
        try {
            Utils.registerDeployerServices(bundleContext);
        } catch(Throwable e) {
            String msg = "Failed To Register Admin Console Bundle As An OSGi Service";
            log.error(msg,e);
        }
        try {
            this.initRSSDatabase();
        } catch (Exception e) {
			log.error("Error in initialising RSS database", e);
		}
    }

    /**
     * Initialises the RSS DAO database by reading from the "wso2-rss-config.xml".
     * @throws RSSDAOException rssDaoException
     */
    private void initRSSDatabase() throws RSSDAOException {
    	/* adds the rss instances listed in the configuration file,
    	 * if any of them are already existing in the database, they will be skipped */
    	RSSConfig rssConfig = RSSManagerUtil.getRSSConfig();
    	/* Set "removeAll" doesn't work properly for some reason */
    	Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
    	for (RSSInstance tmpInst : rssConfig.getRssInstances()) {
    		rssInstances.put(tmpInst.getName(), tmpInst);
    	}
    	RSSDAO rssDAO = RSSDAOFactory.getRSSDAO();
    	for (RSSInstance tmpInst : rssDAO.getAllServiceProviderHostedRSSInstances()) {
    		rssInstances.remove(tmpInst.getName());
    	}
    	for (RSSInstance inst : rssInstances.values()) {
    		rssDAO.addRSSInstance(inst);
    	}
    }
    
    /**
     * Stops the Admin Console bundle. The content is intentionally letft blank as the
     * underlying OSGi environment handles the corresponding task.
     * @param bundleContext Nundle context
     * @throws Exception
     */
    public void stop(BundleContext bundleContext) throws Exception {
    }

}
