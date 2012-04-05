package org.wso2.carbon.messagebox.internal.qpid;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.messagebox.MessageBoxConfigurationException;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.MessageBoxServiceFactory;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.messagebox.internal.registry.RegistryQueueManager;
import org.wso2.carbon.qpid.stub.service.QpidAdminServiceStub;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.namespace.QName;
import java.net.SocketException;
import java.rmi.RemoteException;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class JMSMessageBoxServiceFactory implements MessageBoxServiceFactory {
    public static final String MB_REMOTE_MESSGE_BROKER = "remoteMessageBroker";
    public static final String MB_HOST_NAME = "hostName";
    public static final String MB_SERVICE_PORT = "servicePort";
    public static final String MB_USER_NAME = "userName";
    public static final String MB_PASSWORD = "password";
    public static final String MB_QPID_PORT = "qpidPort";
    public static final String MB_CLIENT_ID = "clientID";
    public static final String MB_VIRTUAL_HOST_NAME = "virtualHostName";
    public static final String MB_TYPE = "type";
    public static final String MB_WEB_CONTEXT = "webContext";

    public void setQueueConnectionManager(OMElement config)
            throws MessageBoxConfigurationException {

        String type = config.getAttributeValue(new QName(null, MB_TYPE));
        QueueConnectionManager queueConnectionManager = QueueConnectionManager.getInstance();
        queueConnectionManager.setType(type);
        if (QueueConnectionManager.MB_TYPE_REMOTE.equals(type)) {
            OMElement remoteQpidAdminService =
                    config.getFirstChildWithName(new QName(MessageBoxConstants.MB_CONF_NAMESPACE,
                                                           MB_REMOTE_MESSGE_BROKER));

            String hostName = getValue(remoteQpidAdminService, MB_HOST_NAME);
            String servicePort = getValue(remoteQpidAdminService, MB_SERVICE_PORT);
            String userName = getValue(remoteQpidAdminService, MB_USER_NAME);
            String password = getValue(remoteQpidAdminService, MB_PASSWORD);
            String qpidPort = getValue(remoteQpidAdminService, MB_QPID_PORT);
            String clientID = getValue(remoteQpidAdminService, MB_CLIENT_ID);
            String virtualHostName = getValue(remoteQpidAdminService, MB_VIRTUAL_HOST_NAME);
            String webContext = getValue(remoteQpidAdminService, MB_WEB_CONTEXT);
            if (!webContext.trim().endsWith("/")) {
                webContext += "/";
            }

            ConfigurationContext clientConfigurationContext =
                    MessageBoxServiceValueHolder.getInstance().getConfigurationContextService().getClientConfigContext();
            try {
                String servicesString = "https://" + hostName + ":" + servicePort + webContext + "services/";
                AuthenticationAdminServiceStub stub =
                        new AuthenticationAdminServiceStub(clientConfigurationContext, servicesString + "AuthenticationAdmin");
                stub._getServiceClient().getOptions().setManageSession(true);
                boolean isAuthenticated = stub.login(userName, password, NetworkUtils.getLocalHostname());

                if (isAuthenticated) {
                    ServiceContext serviceContext = stub._getServiceClient().getLastOperationContext().getServiceContext();
                    String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
                    QpidAdminServiceStub qpidAdminServiceStub = new QpidAdminServiceStub(clientConfigurationContext, servicesString + "QpidAdminService");
                    qpidAdminServiceStub._getServiceClient().getOptions().setManageSession(true);
                    qpidAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);
                    String accessKey = qpidAdminServiceStub.getAccessKey();

                    queueConnectionManager.setHostName(hostName);
                    queueConnectionManager.setAccessKey(accessKey);
                    queueConnectionManager.setQpidPort(qpidPort);
                    queueConnectionManager.setClientID(clientID);
                    queueConnectionManager.setVirtualHostName(virtualHostName);

                } else {
                    throw new MessageBoxConfigurationException("Can not authenticate to the remote messge broker ");
                }
            } catch (AxisFault axisFault) {
                throw new MessageBoxConfigurationException("Can not connect to the remote Qpid Service ", axisFault);
            } catch (SocketException e) {
                throw new MessageBoxConfigurationException("Can not connect to the remote Qpid Service ", e);
            } catch (AuthenticationExceptionException e) {
                throw new MessageBoxConfigurationException("Can not connect to the remote Qpid Service ", e);
            } catch (RemoteException e) {
                throw new MessageBoxConfigurationException("Can not connect to the remote Qpid Service ", e);
            }
        }


    }

    public static String getValue(OMElement omElement, String localPart) {
        OMElement childElement =
                omElement.getFirstChildWithName(
                        new QName(omElement.getNamespace().getNamespaceURI(), localPart));
        return childElement.getText();
    }

    public MessageBoxService getMessageBoxService(OMElement config)
            throws MessageBoxConfigurationException {
        try {
            JMSMessageBoxService messageBoxService = new JMSMessageBoxService();
            messageBoxService.setQueueManager(new RegistryQueueManager());
            messageBoxService.setRegistryMessageBoxHandler(new RegistryMessageBoxHandler());
            messageBoxService.setAuthorizationHandler(new MessageBoxAuthorizationHandler());
            setQueueConnectionManager(config);
            return messageBoxService;
        } catch (MessageBoxException e) {
            throw new MessageBoxConfigurationException("Failed to instantiate JMSMessageBoxService class.", e);
        }
    }
}
