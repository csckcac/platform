package org.wso2.carbon.bam.eventreceiver;

import java.io.File;

public class KeyStoreUtil {

    File filePath;

    public static void setTrustStoreParams() {
        File filePath = new File("src/test/resources");
        if (!filePath.exists()) {
            filePath = new File("org.wso2.carbon.agent.server/src/test/resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        String trustStore = filePath.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

    }

    public static void setKeyStoreParams() {
        File filePath = new File("src/test/resources");
        if (!filePath.exists()) {
            filePath = new File("org.wso2.carbon.agent.server/src/test/resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        String keyStore = filePath.getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", keyStore + "/wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

    }
}
