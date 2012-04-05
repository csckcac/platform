package org.wso2.carbon.gadget.ide.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.gadget.ide.stub.GadgetIDEAdminException;
import org.wso2.carbon.gadget.ide.stub.GadgetIDEAdminStub;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class GadgetIDEAdminClient {
    private static final Log log = LogFactory.getLog(GadgetIDEAdminClient.class);
    private static final String BUNDLE_NAME = "org.wso2.carbon.gadget.ide.ui.i18n.Resources";
    private GadgetIDEAdminStub stub;
    private ResourceBundle bundle;
    private String operationSig;

    public GadgetIDEAdminClient(String cookie,
                                String backendServerURL,
                                ConfigurationContext configCtx,
                                Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "GadgetIDEAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

        stub = new GadgetIDEAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getOperations(String uri, String binding) throws RemoteException {
        String[] operations = new String[0];
        try {
            operations = stub.getOperations(uri, binding);
            return operations;
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.get.operations"), e);
        }
        return null;
    }

    public boolean saveTempSettings(String settings) throws RemoteException {
        try {
            return stub.saveTempSettings(settings);
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.save.settings"), e);
        }
        return false;
    }

    public boolean generateCode() throws RemoteException {
        try {
            return stub.generateCode();
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.generate.code"), e);
        }
        return false;
    }

    private void logAndThrow(String msg, java.lang.Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }

    public String[] getEndpoints(String uri) throws RemoteException {
        try {
            String[] endpoints = stub.getEndpoints(uri);
            return endpoints;
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.get.endpoints"), e);
        }
        return null;
    }

    public String getStub(String uri, String endpoint, String operation) throws RemoteException {
        try {
            return stub.getStub(uri);
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.generate.stub"), e);
        }
        return null;
    }

	public String getOperationSig(String uri, String endpoint, String operation) throws RemoteException {
		try {
			return stub.getOperationSig(uri, endpoint, operation);
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.get.sig"), e);
		}
		return null;
	}

    public String deploy(String gadgetXmlName) throws RemoteException {
        try {
            return stub.deploy(gadgetXmlName);
        } catch (GadgetIDEAdminException e) {
            logAndThrow(bundle.getString("cannot.deploy"), e);
        }
        return null;
    }}
