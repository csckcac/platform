/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;

/**
 * This class represents the Coordination server.
 */
public class CoordinationServer extends Thread {

	private static final Log log = LogFactory.getLog(CoordinationServer.class);
		
	private QuorumPeerConfig clusteredConfig;
	
	private ServerConfig standaloneConfig;
	
	private boolean clustered;
	
	public CoordinationServer(String configContent) throws CoordinationException {
		Properties configProps = new Properties();
		try {
			configProps.load(new ByteArrayInputStream(configContent.getBytes()));
			this.clusteredConfig = new QuorumPeerConfig();
			this.clusteredConfig.parseProperties(configProps);
			this.clustered = true;
			if (this.getClusteredConfig().getServers().size() == 0) {
				/* goto standalone mode */
				this.standaloneConfig = new ServerConfig();
				this.standaloneConfig.readFrom(this.getClusteredConfig());
				this.clustered = false;
			}
		} catch (Exception e) {
			throw new CoordinationException(ExceptionCode.CONFIGURATION_ERROR, e);
		}
	}
	
	private boolean isClustered() {
		return clustered;
	}
	
	private QuorumPeerConfig getClusteredConfig() {
		return clusteredConfig;
	}
	
	private ServerConfig getStandaloneConfig() {
		return standaloneConfig;
	}
	
	public void run() {
		try {			
			if (this.isClustered()) {
				log.info("Starting Coordination server in clustered mode...");
				new QuorumPeerMain().runFromConfig(this.getClusteredConfig());
			} else {
				log.info("Starting Coordination server in standalone mode...");
				new ZooKeeperServerMain().runFromConfig(this.getStandaloneConfig());
			}
		} catch (IOException e) {
			log.error("Error starting Coordination server", e);
		}
	}
	
}
