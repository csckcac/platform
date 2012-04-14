package org.wso2.carbon.registry.samples.populator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.extensions.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.resource.services.utils.InputStreamBasedDataSource;
import org.wso2.carbon.registry.samples.populator.utils.CommandHandler;
import org.wso2.carbon.registry.samples.populator.utils.PopulatorHandlerManagerServiceClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class Main {
    private static String cookie;

    private static void setSystemProperties(){
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator +"wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode","true");
    }

    public static void main(String[] args) {
        try{
            CommandHandler.setInputs(args);
            setSystemProperties();

            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(
                    CommandHandler.getServiceURL(), CommandHandler.getUsername(), CommandHandler.getPassword(), configContext) {
                public void setCookie(String cookie) {
                    Main.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            File extensionFolder = new File(CommandHandler.getRxtFileLocation());
            File[] extensions = extensionFolder.listFiles();
            if(extensions != null){
                for(File extension : extensions){
                    String extensionName = getResourceName(extension.getAbsolutePath());
                    if(extensionName.endsWith(".rxt")){
                        org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient resourceServiceClient
                            = new org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient(cookie, CommandHandler.getServiceURL(),configContext);
                        DataSource dataSource = new InputStreamBasedDataSource(new FileInputStream(new File(extension.getAbsolutePath())));
                        DataHandler dataHandler = new DataHandler(dataSource);
                        resourceServiceClient.addResource("/_system/governance/repository/components/org.wso2.carbon.governance/types/"+extensionName,
                                "application/vnd.wso2.registry-ext-type+xml", null, dataHandler, null);
                    }
                }
            }

            String handlerJarLocation = CommandHandler.getHandlerJarLocation();
            if(handlerJarLocation != null){
                File folder = new File(handlerJarLocation);
                File[] filesList = folder.listFiles();
                if(filesList != null){
                    for(File file : filesList){
                        String name = file.getName();
                        if(file.isFile() && name.endsWith(".jar")){
                            String fileName = getResourceName(file.getAbsolutePath());
                            ResourceServiceClient extensionResourceServiceClient = new ResourceServiceClient(cookie, CommandHandler.getServiceURL(),configContext);
                            DataSource dataSource =new InputStreamBasedDataSource(new FileInputStream(new File(file.getAbsolutePath())));
                            DataHandler dataHandler = new DataHandler(dataSource);
                            extensionResourceServiceClient.addExtension(fileName, dataHandler);
                        }
                    }
                }
            }

            StAXOMBuilder builder = new StAXOMBuilder(CommandHandler.getHandlerDef());
            OMElement handlersOMElement = builder.getDocumentElement();
            Iterator<OMElement> handlers = handlersOMElement.getChildElements();
            while(handlers.hasNext()){
                OMElement handler = handlers.next();
                PopulatorHandlerManagerServiceClient handlerManagementServiceClient
                        = new PopulatorHandlerManagerServiceClient(cookie, CommandHandler.getServiceURL(),configContext);
                handlerManagementServiceClient.newHandler(handler.toString());
            }

        } catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static String getResourceName(String fileLocation){
        String[] s =  fileLocation.replace("\\", "/").split("/");
        return s[s.length-1];
    }
}