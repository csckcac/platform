/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.mediator.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

public abstract class ESBMediatorTest {
    protected Log log = LogFactory.getLog(getClass());
    protected StockQuoteClient axis2Client;
    protected EnvironmentVariables esbServer;
    protected UserInfo userInfo;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        userInfo = UserListCsvReader.getUserInfo(1);
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);

        esbServer = builder.build().getEsb();
    }

    public void cleanup() {
        axis2Client.destroy();
        userInfo = null;
        esbServer = null;
    }

    protected String getMainSequenceURL() {
        return "http://" + esbServer.getProductVariables().getHostName() + ":" +
               esbServer.getProductVariables().getNhttpPort();
    }

    protected String getProxyServiceURL(String proxyServiceName) {
        return "http://" + esbServer.getProductVariables().getHostName() + ":" +
               esbServer.getProductVariables().getNhttpPort() + "/services/" + proxyServiceName;
    }

    protected void loadSampleESBConfiguration(int sampleNo) throws Exception {
        ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
        esbUtils.loadSampleESBConfiguration(sampleNo, esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        Thread.sleep(10000);
    }

    protected void loadESBConfigurationFromClasspath(String filePath) throws Exception {
        ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
        esbUtils.loadESBConfigurationFromClasspath(filePath, esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        Thread.sleep(10000);
    }
}
