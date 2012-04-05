package org.wso2.carbon.hosting.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.container.mgt.stub.services.ContainerManagementAdminStub;

/**
 * Created by IntelliJ IDEA.
 * User: amila
 * Date: Mar 21, 2012
 * Time: 6:23:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContainerMgtClient {

    private static Log log = LogFactory.getLog(ContainerMgtClient.class);

    private ContainerManagementAdminStub stub;

    public ContainerMgtClient(ConfigurationContext configCtx, String backendServerURL,
                                String cookie) throws AxisFault {

        String serviceURL = backendServerURL + "ContainerManagementAdmin";
        stub = new ContainerManagementAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void createContainer(String tenantDomain, String zone, String template) throws Exception{

        try {
            stub.createContainer(tenantDomain, "123", zone, template);
        } catch (Exception e) {
            log.error("Error occurred while creating container." + e.getMessage());
            throw e;
        }
    }
}
