/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerEngine;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TenantAnalyzerInitializer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(TenantAnalyzerInitializer.class);

    private static final String MONITOR_CONFIG_LOCATION = "monitor.config.location";

    private static final String MONITOR_CONFIG = "monitor-config";

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain =
                SuperTenantCarbonContext.getCurrentContext(configurationContext).getTenantDomain();

        log.info("Intializing the analyzer Configuration for the tenant domain : " + tenantDomain);

        try {
            
            File tenantAxis2Repo = new File(
                    configurationContext.getAxisConfiguration().getRepository().getFile());
            File analyzerConfigDir = new File(tenantAxis2Repo, MONITOR_CONFIG);
            if (!analyzerConfigDir.exists()) {
                if (!analyzerConfigDir.mkdir()) {
                    log.fatal("Couldn't create the monitor-config root on the file system " +
                              "for the tenant domain : " + tenantDomain);
                    return;
                }
            }
            String analyzerConfigsDirLocation = analyzerConfigDir.getAbsolutePath();

            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

            axisConfig.addParameter(MONITOR_CONFIG_LOCATION, analyzerConfigsDirLocation);

            String analyzerFilePath = analyzerConfigsDirLocation +
                                      File.separator + AnalyzerConfigConstants.ANALYZER_FILE_NAME;
            InputStream analyzerStream = new FileInputStream(analyzerFilePath);

            TaskManager taskManager =
                    Utils.getTaskService().getTaskManager(
                            BAMAnalyzerServiceComponent.BAM_ANALYSER_TASK);
            Utils.setEngine(new AnalyzerEngine(taskManager));
            if (log.isDebugEnabled()) {
                log.debug("BAM analyzer bundle is activated");
            }
        } catch (Exception e) {
            log.error("Couldn't initialize monitoring configuration for tenant: " + tenantDomain, e);
        }

    }
}
