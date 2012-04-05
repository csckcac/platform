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

package org.wso2.automation.product.scenarios.test.esb;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class HttpGetTest {
    private static final Log log = LogFactory.getLog(HttpGetTest.class);

    @Test(alwaysRun = true)
    public void testHttpGet() throws IOException {

        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        ManageEnvironment environment = builder.build();

        int nHttp_port = Integer.parseInt(environment.getEsb().getProductVariables().getNhttpPort());
        String hostName = environment.getEsb().getProductVariables().getHostName();

        Socket socket = new Socket(hostName, nHttp_port);
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        wr.write("GET /services/ch_suva_wsx_mix_mitarbeiter_open_benutzerfinder_v1?wsdl HTTP/1.1\r\n");
        wr.write("Host: " + hostName + ":" + nHttp_port + "\r\n");
        wr.write("\r\n");
        wr.write("\r\n");
        wr.flush();
        log.info("Done writing...\n");

        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String str;
        while ((str = rd.readLine()) != null) {
            log.info(str);
            if (str.contains("HTTP/1.0 400 Bad Request")) {
                Assert.fail("http get request failed.");
            }
        }
        rd.close();
        socket.close();
    }
}
