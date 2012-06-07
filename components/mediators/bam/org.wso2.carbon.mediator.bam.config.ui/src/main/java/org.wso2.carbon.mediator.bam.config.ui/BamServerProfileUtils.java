/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediator.bam.config.ui;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
/*import org.wso2.carbon.governance.api.uri.UriManager;
import org.wso2.carbon.governance.api.uri.dataobjects.Uri;*/
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class BamServerProfileUtils {

    private Registry itsGovernanceRegistry;
    private Registry registry;

    private void setSystemProperties(){
//        System.setProperty("carbon.home", "/home/maninda/Desktop/ESB_ui/wso2esb-4.5.0-SNAPSHOT");
        System.setProperty("carbon.home", CarbonUtils.getCarbonHome());
//        String trustStore = "/home/maninda/Desktop/ESB_ui/wso2esb-4.5.0-SNAPSHOT/repository/resources/security/wso2carbon.jks";
        String trustStore = CarbonUtils.getCarbonHome() + File.separator +  "repository"
                            + File.separator + "resources" + File.separator + "security"
                            + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        //System.setProperty("carbon.repo.write.mode","true");
    }

    private RemoteRegistryService initialize(){
        String remoteRegistryUrl = "https://localhost:9444/registry";
        String username = "admin";
        String password = "admin";
        try {
            return new RemoteRegistryService(remoteRegistryUrl, username, password);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private void createRegistry (RemoteRegistryService registryServiceRef){
        try {
            registry = registryServiceRef.getSystemRegistry();
            itsGovernanceRegistry = registryServiceRef.getGovernanceSystemRegistry();
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) throws Exception{
        /*RemoteRegistryService rootRegService;

        setSystemProperties();
        rootRegService = initialize();
        createRegistry(rootRegService);*/
//        addResource();
//        getGovernanceArtifacts();
    }

    public void addResource(String ip, String port, String userName, String password,
                            String bamServerProfileLocation){
        //String path = "/_system/governance/resource1.txt";

        RemoteRegistryService rootRegService;
        setSystemProperties();
        try {
            rootRegService = initialize();
            createRegistry(rootRegService);
            Resource storeResource;
            storeResource = registry.newResource();

            MediatorConfigurationXml mediatorConfigurationXml = new MediatorConfigurationXml();
            OMElement storeXml = mediatorConfigurationXml.buildServerProfile(ip, port, userName, password);
            String stringStoreXml = storeXml.toString();

            storeResource.setContent(stringStoreXml);
            registry.put(bamServerProfileLocation,storeResource);

            /*MediatorConfigurationXml mediatorConfigurationXml = new MediatorConfigurationXml();
            OMElement oe = mediatorConfigurationXml.buildServerProfile("localhost", "7611", "admin", "admin");
            String s = oe.toString();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1" + s);

            OMElement oe2 = new StAXOMBuilder(new ByteArrayInputStream(s.getBytes())).getDocumentElement();

            String s2 = oe2.toString();
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + s2);*/

        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String getResource(String bamServerProfileLocation){

            //String path = "/_system/governance/resource1.txt";
            Resource resource;
            RemoteRegistryService rootRegService;
            setSystemProperties();
            rootRegService = initialize();
            createRegistry(rootRegService);
        try {
            resource = registry.get(bamServerProfileLocation);
            return new String((byte[])resource.getContent());

            //System.out.println("##############################################" + new String((byte[])resource.getContent()));
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        Resource resource;
        RemoteRegistryService rootRegService;
        setSystemProperties();
        rootRegService = initialize();
        createRegistry(rootRegService);
        try {
            return registry.resourceExists(bamServerProfileLocation);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }
}