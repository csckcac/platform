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


package org.wso2.carbon.analytics.hive.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub.QueryResult;

import java.rmi.RemoteException;

public class HiveExecutionClient {

    private static Log log = LogFactory.getLog(HiveExecutionClient.class);

    private HiveExecutionServiceStub stub;

    public HiveExecutionClient(String cookie,
                               String backEndServerURL,
                               ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "HiveExecutionService";
        stub = new HiveExecutionServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }


    public boolean saveConfiguration(String driver, String url, String username, String password) throws RemoteException, HiveExecutionServiceHiveExecutionException {
        try {
            return stub.setConnectionParameters(driver,url, username, password);
        } catch (RemoteException e) {
          log.error(e);
          throw e;
        } catch (HiveExecutionServiceHiveExecutionException e) {
          log.error(e);
          throw e;
        }
    }

    public QueryResult[] executeScript(String script) {
        try {
            QueryResult[] res = stub.executeHiveScript(script);
            return res;
           // return generateDisplayString(res);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return null;
        } catch (HiveExecutionServiceHiveExecutionException e) {
            log.error(e.getFaultMessage(), e);
            return null;
        }
    }



}
