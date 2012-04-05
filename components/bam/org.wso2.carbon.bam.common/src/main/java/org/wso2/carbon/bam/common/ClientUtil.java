package org.wso2.carbon.bam.common;

import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.Operation;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.Server;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.ServerWithCategory;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.Service;
import org.wso2.carbon.bam.util.BAMConstants;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class used to store the client specific utility methods
 */
public class ClientUtil {
    public static String getBackendEPR(String backendServerURL, String serviceName) throws MalformedURLException {
        String servicesURL;
        if (backendServerURL.startsWith("local:/")) {
            servicesURL = "local:/";
        } else {
            URL url = new URL(backendServerURL);

            servicesURL = url.getProtocol() + "://" + url.getHost();

            if (url.getPort() > 0) {
                servicesURL += ":" + url.getPort();
            }

            String path = url.getPath();
            if (path != null && path.length() > 0 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
                servicesURL += "/" + path;
            }
        }
        return servicesURL + "/services/" + serviceName;
    }

    public static ServerDO convertServerToServerDO(Server monitorServer) {

        ServerDO server = new ServerDO();
        server.setServerType(monitorServer.getServerType());
        server.setId(monitorServer.getServerID());
        server.setTenantID(monitorServer.getTenentID());
        server.setDescription(monitorServer.getServerDesc());
        server.setServerURL(monitorServer.getServerURL());
        server.setActive(monitorServer.getIsActive());
        server.setServerType(monitorServer.getServerType());
        server.setCategory(monitorServer.getStatCategory());
        server.setSubscriptionID(monitorServer.getSubscriptionID());
        server.setSubscriptionEPR(monitorServer.getEpr());

        if (monitorServer.getIsActive()) {
            server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
        } else {
            server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
        }

        server.setPassword(monitorServer.getPassword());
        server.setUserName(monitorServer.getUsername());

        return server;
    }

    public static ServerDO convertToServerDOWithCategoryName(ServerWithCategory monitorServer) {

        ServerDO server = new ServerDO();
        server.setServerType(monitorServer.getServerType());
        server.setId(monitorServer.getServerID());
        server.setTenantID(monitorServer.getTenentID());
        server.setDescription(monitorServer.getServerDesc());
        server.setServerURL(monitorServer.getServerURL());
        server.setActive(monitorServer.getIsActive());
        server.setServerType(monitorServer.getServerType());
        server.setCategory(monitorServer.getStatCategory());
        server.setCategoryName(monitorServer.getStatCategoryName());
        server.setSubscriptionID(monitorServer.getSubscriptionID());
        server.setSubscriptionEPR(monitorServer.getEpr());
        server.setActive(monitorServer.getIsActive());
        server.setPassword(monitorServer.getPassword());
        server.setUserName(monitorServer.getUsername());

        return server;
    }

    public static ServiceDO convertServiceToServiceDO(Service service) {

        ServiceDO serviceDO = new ServiceDO();
        serviceDO.setId(Integer.parseInt(service.getServiceID()));
        serviceDO.setName(service.getServiceName());
        serviceDO.setDescription(service.getServiceDesc());
        serviceDO.setServerID(Integer.parseInt(service.getServerID()));

        return serviceDO;

    }

    public static OperationDO convertOperationToOperationDO(Operation operation) {
        OperationDO operationDO = new OperationDO();
        operationDO.setDescription(operation.getOperationDesc());
        operationDO.setOperationID(Integer.parseInt(operation.getOperationID()));
        operationDO.setName(operation.getOperationName());
        operationDO.setServiceID(Integer.parseInt(operation.getServiceID()));

        return operationDO;

    }
}