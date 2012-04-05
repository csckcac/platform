/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.data.publisher.activity.service.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.service.stub.ActivityPublisherAdminStub;
import org.wso2.carbon.bam.data.publisher.activity.service.stub.types.carbon.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.stub.types.carbon.XPathConfigData;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Client to the activity admin service, that uses the code generated stub to access backend
 * service.
 */
public class ActivityPublisherAdminClient {

    private static final Log log = LogFactory.getLog(ActivityPublisherAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.bam.data.publisher.activity.service.ui.i18n.Resources";
    private ActivityPublisherAdminStub stub;
    private ResourceBundle bundle;

    public ActivityPublisherAdminClient(String cookie,
                                        String backendServerURL,
                                        ConfigurationContext configCtx,
                                        Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ActivityPublisherAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ActivityPublisherAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public EventingConfigData getEventingConfigData() throws RemoteException {
        try {
            return stub.getEventingConfigData();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.eventing.config"), e);
        }
        return null;
    }

    public void setEventingConfigData(EventingConfigData eventingConfigData)
            throws RemoteException {
        try {
            stub.configureEventing(eventingConfigData);
        } catch (Exception e) {
            handleException(bundle.getString("cannot.set.eventing.config"), e);
        }
    }

    public void setXPathConfigData(XPathConfigData xpathConfigData) throws RemoteException {
        try {
            stub.configureXPathData(xpathConfigData);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.set.xpath.config") + " " + e.getMessage(), e);
        }
    }

    public XPathConfigData[] getXPathConfigData() throws RemoteException {
        try {
            return stub.getXPathData();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.xpath.config"), e);
        } catch (Exception e) {
            handleException(bundle.getString("cannot.get.xpath.config"), e);
        }

        return new XPathConfigData[0];
    }

    private void handleException(String msg, java.lang.Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }
}
