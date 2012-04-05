/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.stratos.ms.sample.installer.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.mashup.utils.MashupConstants;

import java.io.File;
import java.io.IOException;

/**
 * This class handles installing of samples
 */
public class SampleInstaller {

    private static final Log log = LogFactory.getLog(SampleInstaller.class);
    private String serverDir = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                               File.separator + "deployment" + File.separator + "server";
    private String tenantsDir = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                File.separator + "tenants";

    public void installMashupSamples(int tenantId) throws IOException {
        log.info("Installing Mashup samples for tenant ");
        // Copying the sample Mashups
        File srcMashups =
                new File(serverDir + File.separator + MashupConstants.JS_SERVICE_REPO +
                                  File.separator + "admin");
		File jsServices = new File(tenantsDir + File.separator + tenantId + File.separator +
                         MashupConstants.JS_SERVICE_REPO);
		if(!jsServices.exists() && !jsServices.mkdir()) {
			throw new IOException("Fail to create the directory: " + jsServices.getAbsolutePath());
		}
        File targetMashups = new File(jsServices, "admin");
        if (srcMashups.exists()) {
            FileManipulator.copyDir(srcMashups, targetMashups);
        }
    }
}
