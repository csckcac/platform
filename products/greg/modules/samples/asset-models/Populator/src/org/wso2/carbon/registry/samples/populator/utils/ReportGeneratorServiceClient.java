package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceCryptoExceptionException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceStub;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.rmi.RemoteException;

public class ReportGeneratorServiceClient {

    ReportingAdminServiceStub stub;

    public ReportGeneratorServiceClient(String cookie, String serverURL,
                                        ConfigurationContext configContext) throws RegistryException {
        String epr = serverURL + "ReportingAdminService";
        try{
            stub = new ReportingAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate handler management service client. " + axisFault.getMessage();
            throw new RegistryException(msg, axisFault);
        }
    }

    public void saveReport(ReportConfigurationBean configurationBean)
            throws ReportingAdminServiceRegistryExceptionException,
            ReportingAdminServiceCryptoExceptionException, RemoteException {
        stub.saveReport(configurationBean);
    }
}
