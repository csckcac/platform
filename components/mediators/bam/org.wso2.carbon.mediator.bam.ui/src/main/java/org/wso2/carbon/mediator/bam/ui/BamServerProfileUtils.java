package org.wso2.carbon.mediator.bam.ui;


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
        System.setProperty("carbon.repo.write.mode","true");
    }

    private RemoteRegistryService initialize() throws Exception {
        String remoteRegistryUrl = "https://localhost:9443/registry";
        String username = "admin";
        String password = "admin";
        return new RemoteRegistryService(remoteRegistryUrl, username, password);
    }

    private void createRegistry (RemoteRegistryService registryServiceRef) throws Exception{
        registry = registryServiceRef.getSystemRegistry();
        itsGovernanceRegistry = registryServiceRef.getGovernanceSystemRegistry();
    }

    public static void main(String[] args) throws Exception{
        /*RemoteRegistryService rootRegService;

        setSystemProperties();
        rootRegService = initialize();
        createRegistry(rootRegService);*/
//        addArtifacts();
//        getGovernanceArtifacts();
    }

    public void addArtifacts(){
        String path = "/_system/governance/resource1.txt";

        RemoteRegistryService rootRegService;
        setSystemProperties();
        try {
            rootRegService = initialize();
            createRegistry(rootRegService);

            Resource resource1;
            resource1 = registry.newResource();

            resource1.setContent("I like it.");

            registry.put(path,resource1);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /*public static void getGovernanceArtifacts() throws RegistryException {

        UriManager manager = new UriManager(itsGovernanceRegistry);
        Uri wsdlUri = manager.newUri(new QName("listing5.wsdl"));
        wsdlUri.setAttribute("overview_name", "listing5.wsdl");
        wsdlUri.setAttribute("overview_type", "WSDL");
        wsdlUri.addAttribute("overview_uri",
                             "http://people.wso2.com/~evanthika/wsdls/listing5.wsdl");
        manager.addUri(wsdlUri);


        Uri wsdl2Uri = manager.newUri(new QName("BizService.wsdl"));
        wsdl2Uri.setAttribute("overview_name", "BizService.wsdl");
        wsdl2Uri.setAttribute("overview_type", "WSDL");
        wsdl2Uri.addAttribute("overview_uri",
                              "http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/" +
                              "org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        manager.addUri(wsdl2Uri);

        Uri schemaUri = manager.newUri(new QName("purchasing_dup.xsd"));
        schemaUri.setAttribute("overview_name", "purchasing_dup.xsd");
        schemaUri.setAttribute("overview_type", "XSD");
        schemaUri.addAttribute("overview_uri",
                               "http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/" +
                               "org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing_dup.xsd");
        manager.addUri(schemaUri);


        Uri policyUri = manager.newUri(new QName("policy.xml"));
        policyUri.setAttribute("overview_name", "policy.xml");
        policyUri.setAttribute("overview_type", "Policy");
        policyUri.addAttribute("overview_uri",
                               "http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/" +
                               "org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml");
        manager.addUri(policyUri);

    }*/
}