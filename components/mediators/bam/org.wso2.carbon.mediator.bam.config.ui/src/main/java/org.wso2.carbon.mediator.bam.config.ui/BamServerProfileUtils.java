/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.bam.config.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.mediator.bam.config.BamServerConfigXml;

import java.rmi.RemoteException;
import java.util.Locale;

public class BamServerProfileUtils {

    private BamServerProfileConfigAdminClient client;

    public BamServerProfileUtils(String cookie, String backendServerURL, ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addResource(String ip, String port, String userName, String password,
                            String bamServerProfileLocation){
        BamServerConfigXml mediatorConfigurationXml = new BamServerConfigXml();
        OMElement storeXml = mediatorConfigurationXml.buildServerProfile(ip, port, userName, password);
        String stringStoreXml = storeXml.toString();

        try {
            client.saveResourceString(stringStoreXml, bamServerProfileLocation);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public String getResource(String bamServerProfileLocation){
        try {
            return client.getResourceString(bamServerProfileLocation);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        try {
            return client.resourceAlreadyExists(bamServerProfileLocation);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }
}