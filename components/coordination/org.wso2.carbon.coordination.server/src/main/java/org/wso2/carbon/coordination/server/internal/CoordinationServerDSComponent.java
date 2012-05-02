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
package org.wso2.carbon.coordination.server.internal;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.coordination.server.CoordinationServer;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This class represents the Coordination declarative service component.
 * @scr.component name="coordination.server.component" immediate="true"
 */
public class CoordinationServerDSComponent {
	
	private Log log = LogFactory.getLog(CoordinationServerDSComponent.class);
	
	private CoordinationServer server;
	
	public CoordinationServer getServer() {
		return server;
	}
		
	protected void activate(ComponentContext ctx) {
		if (log.isDebugEnabled()) {
			log.debug("Starting Coordination component initialization..");
		}
		try {
			if (this.getServer() == null) {
				String configPath = CarbonUtils.getCarbonConfigDirPath() + 
						File.separator + "etc" + File.separator + "zoo.cfg";
				this.server = new CoordinationServer(new String(
						CarbonUtils.getBytesFromFile(new File(configPath))));
				this.server.start();
			}
		} catch (Exception e) {
			log.error("Eror in initializing Coordination component: " + e.getMessage(), e);
		}
	}
	
	protected void deactivate(ComponentContext ctx) {
		if (log.isDebugEnabled()) {
			log.debug("Coordination component deactivated");
		}
	}

}
