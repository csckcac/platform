/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.automation.api.clients.registry;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.rmi.RemoteException;


public class ResourceAdminServiceClient {
    private static final Log log = LogFactory.getLog(ResourceAdminServiceClient.class);

    private final String serviceName = "ResourceAdminService";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private String endPoint;

    private static final String MEDIA_TYPE_WSDL = "application/wsdl+xml";
    private static final String MEDIA_TYPE_SCHEMA = "application/x-xsd+xml";
    private static final String MEDIA_TYPE_POLICY = "application/policy+xml";
    private static final String MEDIA_TYPE_GOVERNANCE_ARCHIVE = "application/vnd.wso2.governance-archive";

    public ResourceAdminServiceClient(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
    }

    public boolean addResource(String sessionCookie, String destinationPath, String mediaType,
                               String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        if (log.isDebugEnabled()) {
            log.debug("Destination Path :" + destinationPath);
            log.debug("Media Type :" + mediaType);
        }

        return resourceAdminServiceStub.addResource(destinationPath, mediaType, description, dh, null);

    }

    public ResourceData[] getResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {
        ResourceData[] rs;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        rs = resourceAdminServiceStub.getResourceData(new String[]{destinationPath});

        return rs;
    }

    public CollectionContentBean getCollectionContent(String sessionCookie, String destinationPath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        CollectionContentBean collectionContentBean;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        try {
            collectionContentBean = resourceAdminServiceStub.getCollectionContent(destinationPath);
        } catch (RemoteException e) {
            log.error("Resource getting failed due to RemoteException : " + e);
            throw new RemoteException("Resource getting failed due to RemoteException :" +
                                      e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Resource getting failed due to ResourceAdminServiceExceptionException : " +
                      e);
            throw new ResourceAdminServiceExceptionException(
                    "Resource getting failed due to ResourceAdminServiceExceptionException:" +
                    e);
        }

        return collectionContentBean;
    }

    public boolean deleteResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        return resourceAdminServiceStub.delete(destinationPath);
    }

    public void addWSDL(String sessionCookie, String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {

        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        log.debug(fileName);
        resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_WSDL, description, dh, null);
    }

    public void addWSDL(String sessionCookie, String resourceName, String description,
                        String fetchURL)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_WSDL, description, fetchURL, null);
    }

    public void addSchema(String sessionCookie, String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_SCHEMA, description, dh, null);
    }

    public void addSchema(String sessionCookie, String resourceName, String description,
                          String fetchURL) throws ResourceAdminServiceExceptionException,
                                                  RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_SCHEMA, description, fetchURL, null);

    }

    public void addPolicy(String sessionCookie, String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_POLICY, description, dh, null);
    }

    public void addPolicy(String sessionCookie, String resourceName, String description,
                          String fetchURL)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_POLICY, description, fetchURL, null);
    }

    public void uploadArtifact(String sessionCookie, String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_GOVERNANCE_ARCHIVE, description, dh, null);
    }

    public void addCollection(String sessionCookie, String parentPath, String collectionName,
                              String mediaType, String description)
            throws ResourceAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.addCollection(parentPath, collectionName, mediaType, description);
    }


    public void addSymbolicLink(String sessionCookie, String parentPath, String name,
                                String targetPath)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.addSymbolicLink(parentPath, name, targetPath);
    }

    public void addTextResource(String sessionCookie, String parentPath, String fileName,
                                String mediaType, String description, String content)
            throws RemoteException, ResourceAdminServiceExceptionException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        resourceAdminServiceStub.addTextResource(parentPath, fileName, mediaType, description, content);
    }

    public void addResourcePermission(String sessionCookie, String pathToAuthorize,
                                      String roleToAuthorize,
                                      String actionToAuthorize, String permissionType)
            throws RemoteException, ResourceAdminServiceResourceServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        resourceAdminServiceStub.addRolePermission(pathToAuthorize, roleToAuthorize, actionToAuthorize, permissionType);

    }

    public String getProperty(String sessionCookie, String resourcePath, String key)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        return resourceAdminServiceStub.getProperty(resourcePath, key);

    }

    public MetadataBean getMetadata(String sessionCookie, String resourcePath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        return resourceAdminServiceStub.getMetadata(resourcePath);
    }

    public ContentBean getResourceContent(String sessionCookie, String resourcePath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        return resourceAdminServiceStub.getContentBean(resourcePath);
    }

    public ResourceData[] getResourceData(String sessionCookie, String resourcePath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        String[] resourceArray = {resourcePath};

        return resourceAdminServiceStub.getResourceData(resourceArray);

    }

    public String getHumanReadableMediaTypes(String sessionCookie, String resourcePath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        return resourceAdminServiceStub.getHumanReadableMediaTypes();

    }
}
