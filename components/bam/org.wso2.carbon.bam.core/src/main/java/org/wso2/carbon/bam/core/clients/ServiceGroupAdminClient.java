package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.BAMConstants;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.service.mgt.stub.ServiceGroupAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaData;

import java.rmi.RemoteException;

public class ServiceGroupAdminClient extends AbstractAdminClient<ServiceGroupAdminStub>{

    private static Log log = LogFactory.getLog(ServiceGroupAdminClient.class);

    public ServiceGroupAdminClient(String serverURL) throws AxisFault {

        String serviceURL = generateURL(new String[]{serverURL, BAMConstants.SERVICES_SUFFIX, BAMConstants.SERVICE_GROUP_ADMIN_SERVICE});
        stub = new ServiceGroupAdminStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public ServiceGroupAdminClient(String serverURL, String sessionCookie) throws AxisFault {
         this(serverURL);
        setSessionCookie(sessionCookie);
    }

    public ServiceGroupMetaData[] getAllServiceGroups() throws RemoteException {
       return stub.listServiceGroups(null, null, 0).getServiceGroups();
    }
}
