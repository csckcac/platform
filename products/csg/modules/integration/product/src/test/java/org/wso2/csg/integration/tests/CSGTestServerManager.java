/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.csg.integration.tests;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.fail;

public class CSGTestServerManager extends TestServerManager {

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 60000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {
        // copy the csg.properties file into the conf directory
        String targetPath = "repository" + File.separator + "conf" + File.separator;
        copyResourceFile("csg.properties", targetPath + "csg.properties", carbonHome);

    }

    private void copyResourceFile(String sourcePath, String targetPath, String carbonHome)
            throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(sourcePath);
        if (in == null) {
            fail("Unable to locate the specified configuration resource: " + sourcePath);
        }

        File target = new File(carbonHome + File.separator + targetPath);
        FileOutputStream fos = new FileOutputStream(target);

        byte[] data = new byte[1024];
        int i;
        while ((i = in.read(data)) != -1) {
            fos.write(data, 0, i);
        }
        fos.flush();
        fos.close();
        in.close();
    }
}
