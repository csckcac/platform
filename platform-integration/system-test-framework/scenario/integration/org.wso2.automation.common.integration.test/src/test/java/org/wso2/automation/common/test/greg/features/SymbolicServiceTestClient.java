/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.automation.common.test.greg.features;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import static org.testng.Assert.*;

import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.rmi.RemoteException;

public class SymbolicServiceTestClient {
    private static final Log log = LogFactory.getLog(SymbolicServiceTestClient.class);
    private static AdminServiceResourceAdmin admin_service_resource_admin;
    private String sessionCookie;


    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(3);
        EnvironmentVariables gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        String gregBackEndUrl = gregServer.getBackEndUrl();

        admin_service_resource_admin = new AdminServiceResourceAdmin(gregBackEndUrl);


    }

    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a collection @ root level",
          priority = 1)
    public void testAddSymbolinktoCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String parentPath = "/";
        String collectionName = "admin_service";
        String symbolinkName = "symb_" + collectionName;
        String targetPath = parentPath + collectionName;
        try {
            admin_service_resource_admin.addCollection(sessionCookie, parentPath, collectionName,
                                                       "default", "");
            log.info("***Collection successfully Created***");

            admin_service_resource_admin.addSymbolink(sessionCookie, parentPath, symbolinkName,
                                                      targetPath);
            log.info("***Symbolink successfully Created***");

            CollectionContentBean collectionContentBean = admin_service_resource_admin.
                    getCollectionContent(sessionCookie, "/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i < childPath.length; i++) {
                    if (childPath[i].equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(childPath[i].equalsIgnoreCase("/" + symbolinkName),
                                   "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            admin_service_resource_admin.deleteResource(sessionCookie, targetPath);
            log.info("*************Add Symbolink to Root Level Collection test - Passed **********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Root Level Collection test - Failed :" + e.getMessage());
            throw new RemoteException("Add Symbolink to Root Level Collection test - Failed :" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Root Level Collection test - Failed :" + e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Root Level Collection test - Failed :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a resource @ root level",
          priority = 2)
    public void testAddSymbolinktoResource()
            throws RemoteException, ResourceAdminServiceExceptionException {
        String parentPath = "/";
        String resource_name = "resource.txt";
        String symbolinkName = "symb_" + resource_name;
        String targetPath = parentPath + resource_name;
        try {
            admin_service_resource_admin.addTextResource(sessionCookie, parentPath, resource_name,
                                                         "", "", "");
            log.info("Successfully resource.txt created :");

            admin_service_resource_admin.addSymbolink(sessionCookie, parentPath, symbolinkName,
                                                      targetPath);
            log.info("Successfully symb_resource symbolink created");

            CollectionContentBean collectionContentBean = admin_service_resource_admin.
                    getCollectionContent(sessionCookie, "/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i < childPath.length; i++) {
                    if (childPath[i].equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(childPath[i].equalsIgnoreCase("/" + symbolinkName),
                                   "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            admin_service_resource_admin.deleteResource(sessionCookie, targetPath);
            log.info("*************Add Symbolink to Root Level Resource test - Passed **********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Root Level Resource test - Failed :" + e.getMessage());
            throw new RemoteException("Add Symbolink to Root Level Resource test - Failed :" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Root Level Resource test - Failed :" + e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Root Level Resource test - Failed :" + e.getMessage());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a collection @ general level",
          priority = 3)
    public void testAddSymbolinktoGeneralCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String parentPath = "/";
        String collectionName = "collection/symbolink/test";
        String symbolinkName = "symb_" + "test";
        String targetPath = parentPath + collectionName;
        try {
            admin_service_resource_admin.addCollection(sessionCookie, parentPath, collectionName,
                                                       "default", "");
            log.info("***Collection successfully Created***");

            admin_service_resource_admin.addSymbolink(sessionCookie, parentPath, symbolinkName,
                                                      targetPath);
            log.info("***Symbolink successfully Created***");

            CollectionContentBean collectionContentBean = admin_service_resource_admin.
                    getCollectionContent(sessionCookie, "/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i < childPath.length; i++) {
                    if (childPath[i].equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(childPath[i].equalsIgnoreCase("/" + symbolinkName),
                                   "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            admin_service_resource_admin.deleteResource(sessionCookie, "/collection");
            log.info("*********Add Symbolink to Collection at General level test - Passed ********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Collection at General level test - Failed :" + e.getMessage());
            throw new RemoteException("Add Symbolink to Collection at General level test- Failed :" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink Collection at General level test - Failed :" + e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink Collection at General level test - Failed :" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a symbolink to a resource @ root level",
          priority = 4)
    public void testAddSymbolinktoGeneralResource()
            throws RemoteException, ResourceAdminServiceExceptionException {
        String parentPath = "/";
        String resource_name = "resource/symbolink/test123.txt";
        String symbolinkName = "symb_" + "test123.txt";
        String targetPath = parentPath + resource_name;
        try {
            admin_service_resource_admin.addTextResource(sessionCookie, parentPath, resource_name,
                                                         "", "", "");
            log.info("Successfully resource.txt created :");

            admin_service_resource_admin.addSymbolink(sessionCookie, parentPath, symbolinkName,
                                                      targetPath);
            log.info("Successfully symb_resource symbolink created");

            CollectionContentBean collectionContentBean = admin_service_resource_admin.
                    getCollectionContent(sessionCookie, "/");

            if (collectionContentBean.getChildCount() > 0) {
                String[] childPath = collectionContentBean.getChildPaths();
                for (int i = 0; i < childPath.length; i++) {
                    if (childPath[i].equalsIgnoreCase("/" + symbolinkName)) {
                        assertTrue(childPath[i].equalsIgnoreCase("/" + symbolinkName),
                                   "Symbolink Not Present :" + symbolinkName);
                        log.info("Symbolink Matched Successfully :" + symbolinkName);
                    }
                }
            }
            admin_service_resource_admin.deleteResource(sessionCookie, "/resource");
            log.info("******Add Symbolink to Resource at General Level test - Passed ********");
        } catch (RemoteException e) {
            log.info("Add Symbolink to Resource at General Level test - Failed :" + e.getMessage());
            throw new RemoteException("Add Symbolink to Resource at General Level test - Failed :" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.info("Add Symbolink to Resource at General Level test - Failed :" + e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Add Symbolink to Resource at General Level test - Failed :" + e.getMessage());
        }
    }
}
