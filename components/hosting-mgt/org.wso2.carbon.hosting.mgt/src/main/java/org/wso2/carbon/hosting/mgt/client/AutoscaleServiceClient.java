
/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hosting.mgt.client;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.load.balance.autoscaler.service.stub.AutoscalerServiceStub;

import java.rmi.RemoteException;

/**
 * This is the client class this calls Autoscaler service.
 */
public class AutoscaleServiceClient {
    
    private AutoscalerServiceStub stub;
    
    private static final Log log = LogFactory.getLog(AutoscaleServiceClient.class);


    public AutoscaleServiceClient(String epr) throws AxisFault {

        try {
            long timeOut = 270000;
            if(!(System.getProperty("autoscaler.time.out") == null)){
                timeOut = Long.parseLong(System.getProperty("autoscaler.time.out"));
            }
            stub = new AutoscalerServiceStub(epr);
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeOut);

        } catch (AxisFault axisFault) {
            String msg =
                "Failed to initiate AutoscalerService client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new AxisFault(msg, axisFault);
        }
    }
    
    public boolean init(boolean isSpi) throws AxisFault {

        try {
            return stub.initAutoscaler(isSpi);
        } catch (RemoteException e) {
            String msg = "Error while calling init() from autoscaler client";
            throw new AxisFault(msg);
        }
    }
    
    public boolean startInstance(String domainName) throws Exception{
        return stub.startInstance(domainName, null);

    }


    public boolean terminateSpiInstance(String ip) throws Exception {
        
        return stub.terminateSpiInstance(ip);
    }

}
