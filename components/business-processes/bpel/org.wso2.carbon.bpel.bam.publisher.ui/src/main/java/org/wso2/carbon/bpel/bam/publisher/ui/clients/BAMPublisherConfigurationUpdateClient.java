package org.wso2.carbon.bpel.bam.publisher.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.bam.publisher.stub.BAMServerInfoManagementServiceStub;
import org.wso2.carbon.bpel.bam.publisher.stub.BamServerInformation;
import org.wso2.carbon.bpel.bam.publisher.stub.Fault;

import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Service Client for "BAMServerInfoManagementService" admin service
 */
public class BAMPublisherConfigurationUpdateClient {
    private static Log log = LogFactory.getLog(BAMPublisherConfigurationUpdateClient.class);

    private BAMServerInfoManagementServiceStub stub;

    public BAMPublisherConfigurationUpdateClient(String cookie, String backendServerURL,
                                                 ConfigurationContext configContext,
                                                 Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "BAMServerInfoManagementService";
        stub = new BAMServerInfoManagementServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String updateBAMServerConfiguration(String bamServerURL, String username,
                        String password, int thriftPort, boolean enableSocketTransport)
                            throws RemoteException, Fault {
        try {
            return stub.updateBamServerInformation(bamServerURL, username, password, thriftPort,
                                                   enableSocketTransport, false);
        } catch (RemoteException e) {
            log.error("updateBamServerInformation operation failed", e);
            throw e;
        } catch (Fault fault) {
            log.error("updateBamServerInformation operation failed: " +
                    fault.getFaultMessage().getBamServerInformationFault(), fault);
            throw fault;
        }
    }

    public BamServerInformation getBAMServerConfiguration() throws RemoteException, Fault {
        try {
            return stub.getBamServerInformation();
        } catch (RemoteException e) {
            log.error("getBamServerInformation operation failed", e);
            throw e;
        } catch (Fault fault) {
            log.error("getBamServerInformation operation failed: " +
                    fault.getFaultMessage().getBamServerInformationFault(), fault);
        }
        return null;
    }
}
