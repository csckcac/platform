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
package org.wso2.automation.common.test.greg.multitenancy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserCreator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;


public class WsdlImportServiceTestClient {
    private static final Log log = LogFactory.getLog(WsdlImportServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static WSRegistryServiceClient registry_testUser = null;
    private static WSRegistryServiceClient registry_diffDomainUser1 = null;
    private static Registry governance = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, RemoteException, UserAdminException,
                              LoginAuthenticationExceptionException {
        int tenantId = 3;
        int diff_DomainUser = 6;
        int tenantID_testUser = 3;
        String userID = "testuser1";
        String userPassword = "test123";
        String roleName = "admin";

        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        registry_diffDomainUser1 = new RegistryProvider().getRegistry(diff_DomainUser, ProductConstant.GREG_SERVER_NAME);

        GregUserCreator GregUserCreator = new GregUserCreator();
        GregUserCreator.deleteUsers(tenantID_testUser, userID);
        GregUserCreator.addUser(tenantID_testUser, userID, userPassword, roleName);
        registry_testUser = GregUserCreator.getRegistry(tenantID_testUser, userID, userPassword);
        governance = new RegistryProvider().getGovernance(registry, tenantId);
        //delete wsdl
        deleteWsdl();
    }

    @Test(groups = {"wso2.greg"}, description = "test multi tenancy scenario add WSDL ", priority = 1)
    public void testaddWSDL() throws RegistryException {
        String wsdl_url = "http://geocoder.us/dist/eg/clients/GeoCoder.wsdl";
        String wsdl_path = "/_system/governance/trunk/wsdls/us/geocoder/rpc/geo/coder/us/GeoCoder.wsdl";
        String association_path = "/_system/governance/trunk/services/us/geocoder/rpc/geo/coder/us/GeoCode_Service";
        String service_namespace = "http://rpc.geocoder.us/Geo/Coder/US/";
        String service_name = "GeoCode_Service";
        String service_path = "/_system/governance/trunk/services/us/geocoder/rpc/geo/coder/us/GeoCode_Service";
        String keyword1 = "?xml version=";
        String keyword2 = "ArrayOfGeocoderResult";


        try {
            createWsdl(wsdl_url);                                       //create wsdl
            verifyResourceExists(wsdl_path);                            // Assert resource exists with differtn users
            getAssociationPath(wsdl_path, association_path);            //Assert Association path exsist with different users
            wsdlContentAssertion(wsdl_path, keyword1, keyword2);         //Assert wsdl content
            checkServiceExsist(service_namespace, service_name, service_path);      //Assert Service Exsist
            deleteWsdl();                                               //Delete Resources
            verifyResourcesDeleted(wsdl_path);                          //Assert resources has been deleted properly
            log.info("*******************Multi Tenancy Wsdl Import Service Test Client- Passed********************");
        } catch (RegistryException e) {
            log.error("Multi Tenancy Wsdl Import Service Test Client -Failed:" + e);
            throw new RegistryException("Multi Tenancy Wsdl Import Service Test Client - Failed:" + e);
        }
    }

    private void createWsdl(String wsdl_url) throws RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdl.addAttribute("version", "1.0.0");
            wsdl.addAttribute("creator", "Aaaa");
            wsdlManager.addWsdl(wsdl);
            log.info("wsdl was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable to add WSDL:" + e);
            throw new RegistryException("Unable to add WSDL :" + e);
        }
    }

    private void wsdlContentAssertion(String wsdl_path, String keyword1, String keyword2)
            throws RegistryException {
        String content_adminUser;
        String content_testUser;
        String content_diffDomainUser = null;
        try {
            Resource r1;
            r1 = registry.get(wsdl_path);
            content_adminUser = new String((byte[]) r1.getContent());
            //Assert admin user -admin123@wso2manualQA0006.org
            assertTrue(content_adminUser.indexOf(keyword1) > 0, "Assert Content wsdl file - key word 1");
            assertTrue(content_adminUser.indexOf(keyword2) > 0, "Assert Content wsdl file - key word 2");
            // Assert Test user - testuser1@wso2manualQA0006.org
            Resource r2;
            r2 = registry_testUser.get(wsdl_path);
            content_testUser = new String((byte[]) r2.getContent());
            assertTrue(content_testUser.indexOf(keyword1) > 0, "Assert Content wsdl file - key word 1");
            assertTrue(content_testUser.indexOf(keyword2) > 0, "Assert Content wsdl file - key word 2");
            Resource r3 = registry.newResource();

            try {
                assertNotNull(registry_diffDomainUser1.get(wsdl_path), "Cannot get WSDL resource by different tenant");

            } catch (RegistryException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot get WSDL resource by different tenant:" + e);
                }
            }
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path), "wsdl resource exits");

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Registry Exception thrown:" + e);
            throw new RegistryException("Registry Exception thrown:" + e);
        }
    }

    public void checkServiceExsist(String service_namespace, String service_name,
                                   String service_path) throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services;
        try {
            services = serviceManager.getAllServices();
            for (Service service : services) {
                if (service.getQName().equals(new QName(service_namespace, service_name))) {
                    //Assert service exisits admin user -admin123@wso2manualQA0006.org
                    assertTrue(registry.resourceExists(service_path), "Service does not Exist :");
                    //Assert service exisits Test user - testuser1@wso2manualQA0006.org
                    assertTrue(registry_testUser.resourceExists(service_path), "Service Exist :");
                    //Assert service does not exists admin user -admin123@wso2manualQA0004.org
                    assertFalse(registry_diffDomainUser1.resourceExists(service_path), "Service does not Exist :");
                } else {
                }
            }
        } catch (GovernanceException e) {
            log.error("checkServiceExsist Exception thrown:" + e);
            throw new GovernanceException("checkServiceExsist Exception thrown:" + e);
        } catch (RegistryException e) {
            log.error("checkServiceExsist Exception thrown:" + e);
            throw new RegistryException("checkServiceExsist Exception thrown:" + e);
        }
    }

    private void deleteWsdl() throws RegistryException {
        //delete wsdls
        try {
            registry.delete("/_system/governance/trunk/wsdls");
            registry_testUser.delete("/_system/governance/trunk/wsdls");
            registry_diffDomainUser1.delete("/_system/governance/trunk/wsdls");

            registry.delete("/_system/governance/trunk/services");
            registry_testUser.delete("/_system/governance/trunk/services");
            registry_diffDomainUser1.delete("/_system/governance/trunk/services");
        } catch (RegistryException e) {
            log.error("deleteWsdl RegistryException thrown:" + e);
            throw new RegistryException("deleteWsdl RegistryException thrown:" + e);
        }
    }

    private void verifyResourceExists(String wsdl_path) throws RegistryException {
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            assertTrue(registry.resourceExists(wsdl_path), "wsdl Exists :");
            // Assert Test user - testuser1@wso2manualQA0006.org
            assertTrue(registry_testUser.resourceExists(wsdl_path), "wsdl exists:");
            // Assert differnt doamin user 1
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path), "wsdl exists:");
        } catch (RegistryException e) {
            log.error("verifyResourceExists RegistryException thrown:" + e);
            throw new RegistryException("verifyResourceExists RegistryException thrown:" + e);
        }
    }

    private void verifyResourcesDeleted(String wsdl_path) throws RegistryException {
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            assertFalse(registry.resourceExists(wsdl_path), "wsdl Exists :");
            // Assert Test user - testuser1@wso2manualQA0006.org
            assertFalse(registry_testUser.resourceExists(wsdl_path), "wsdl exists:");
            // Assert differnt doamin user 1
            assertFalse(registry_diffDomainUser1.resourceExists(wsdl_path), "wsdl exists:");
        } catch (RegistryException e) {
            log.error("verifyResourcesDeleted( RegistryException thrown:" + e);
            throw new RegistryException("verifyResourcesDeleted( RegistryException thrown:" + e);
        }
    }


    public void getAssociationPath(String wsdl_path, String association_path)
            throws RegistryException {
        Association[] associations;
        Association[] associations_testuser;
        Association[] associations_diffDomainUser;
        try {
            //Assert admin user -admin123@wso2manualQA0006.org
            associations = registry.getAssociations(wsdl_path, "usedBy");
            assertTrue(associations[1].getDestinationPath().equalsIgnoreCase(association_path), "Association Path exsits :");
            // Assert Test user - testuser1@wso2manualQA0006.org
            associations_testuser = registry_testUser.getAssociations(wsdl_path, "usedBy");
            assertTrue(associations_testuser[1].getDestinationPath().equalsIgnoreCase(association_path), "Association Path exsits :");
            //Assert admin user -admin123@wso2manualQA0004.org
            associations_diffDomainUser = registry_diffDomainUser1.getAssociations(wsdl_path, "usedBy");

            if (associations_diffDomainUser.length > 0) {
                assertTrue(associations_diffDomainUser[1].getDestinationPath().equalsIgnoreCase(association_path), "Association Path exsits :");
            }
        } catch (RegistryException e) {
            log.error("getAssociationPath Exception thrown:" + e);
            throw new RegistryException("getAssociationPath Exception thrown:" + e);
        }
    }


}
