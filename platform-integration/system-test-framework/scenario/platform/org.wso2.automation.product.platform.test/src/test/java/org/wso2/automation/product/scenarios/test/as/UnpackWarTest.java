/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.product.scenarios.test.as;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.WebAppUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.testng.Assert.fail;

/**
 * After starting AS with -Dcarbon.unpack.wars parameter.
 * It should explode the deployed .war files
 * <p/>
 * Patch automation : https://wso2.org/jira/browse/CARBON-11832
 */
public class UnpackWarTest {
    private static final Log log = LogFactory.getLog(UnpackWarTest.class);

    @Test(alwaysRun = true)
    public void testWar() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().as(1);
        ManageEnvironment environment = builder.build();
        String webAppURL = environment.getAs().getWebAppURL() + "/patchsample/carbon";
        WebAppUtil.waitForWebAppDeployment(webAppURL, "Using WSO2 Registry");

        webAppURL = environment.getAs().getWebAppURL() + "/patchsample/carbon/registry/index.jsp";
        webAppTest(webAppURL, "ISSUE STATUS =  null");

    }


    protected void webAppTest(String url, String content) throws IOException {
        BufferedReader in;

        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int code = connection.getResponseCode(); //get response code to check whether it is 200 or 404

        //check connection for success
        if (code == 200) {
            log.info("Connected to webapp successfully");
            in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                if (inputLine.contains(content)) {
                    System.out.println(inputLine);
                    fail("Issue still available");
                }
            }
            in.close();
        } else {
            log.debug("webapp connection returned HTTP " + code + " error");
        }
    }
}
