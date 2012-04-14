package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class PopulatorHandlerManagerServiceClient {

    private HandlerManagementServiceStub stub;

    public PopulatorHandlerManagerServiceClient(String cookie, String backendServerURL, ConfigurationContext configContext) throws RegistryException {
        String epr = backendServerURL + "HandlerManagementService";
        try {
            stub = new HandlerManagementServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate handler management service client. " + axisFault.getMessage();
            throw new RegistryException(msg, axisFault);
        }
    }

    public void newHandler(String payload) throws ExceptionException, RemoteException, XMLStreamException {
        stub.createHandler(payload);
    }
}

