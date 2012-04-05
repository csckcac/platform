/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.common;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cloud.csg.common.thrift.gen.CSGService;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class CSGUtils {

    private static final Log log = LogFactory.getLog(CSGUtils.class);

    private static Properties prop;

    static {
        prop = loadProperties("csg.properties");
    }

    /**
     * Returns an instance of CSG_TRANSPORT_NAME thrift client
     *
     * @param hostName           thrift server host name
     * @param port               thrift server port client should connect to
     * @param timeOut            the thrift client timeout
     * @param trustStorePath     the trust store to use for this client
     * @param trustStorePassWord the password of the trust store
     * @return a CSG_TRANSPORT_NAME thrift client
     */
    public static CSGService.Client getCSGThriftClient(
            final String hostName,
            final int port,
            final int timeOut,
            final String trustStorePath,
            final String trustStorePassWord) {
        try {
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();

            params.setTrustStore(trustStorePath, trustStorePassWord);

            TTransport transport = TSSLTransportFactory.getClientSocket(
                    hostName,
                    port,
                    timeOut,
                    params);
            TProtocol protocol = new TBinaryProtocol(transport);

            return new CSGService.Client(protocol);
        } catch (TTransportException e) {
            handleException("Could not initialize the Thrift client", e);
        }
        return null;
    }

    /**
     * Move elements between buffers. No need of additional synchronization locks,
     * BlockingQueue#drainTo is thread safe, but not atomic, which is not a problem.
     * See {@link BlockingQueue#drainTo(java.util.Collection, int)}
     *
     * @param src       source buffer
     * @param dest      destination buffer
     * @param blockSize blockSize of message bulk that need to move
     * @throws AxisFault in case of drains fails
     */
    public static void moveElements(BlockingQueue<Message> src,
                                    List<Message> dest,
                                    final int blockSize) throws AxisFault{
        try {
            src.drainTo(dest, blockSize);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage());
        }
    }

    public static String getWSO2KeyStoreFilePath() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        return config.getFirstProperty(
                RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
    }

    public static String getWSO2TrustStoreFilePath() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        return config.getFirstProperty("Security.TrustStore.Location");
    }

    public static String getWSO2KeyStorePassword() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String password = config.getFirstProperty(
                RegistryResources.SecurityManagement.SERVER_PRIVATE_KEY_PASSWORD);
        if (password == null) {
            password = "wso2carbon";
        }
        return password;
    }

    public static String getWSO2TrustStorePassword() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String password = config.getFirstProperty("Security.TrustStore.Password");
        if (password == null) {
            password = "wso2carbon";
        }
        return password;
    }

    public static void handleException(String msg, Throwable t) {
        log.error(msg, t);
        throw new RuntimeException(msg, t);
    }

    public static String getStringProperty(String name, String def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : (String) prop.get(name)) :
                val;
    }

    public static int getIntProperty(String name, int def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Integer.parseInt((String) prop.get(name))) :
                Integer.parseInt(val);
    }

    public static long getLongProperty(String name, long def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Long.parseLong((String) prop.get(name))) :
                Long.parseLong(val);
    }

    public static double getDoubleProperty(String name, double def) {
        String val = System.getProperty(name);
        return val == null ?
                (prop.get(name) == null ? def : Double.parseDouble((String) prop.get(name))) :
                Double.parseDouble(val);
    }

    public static String getQueueNameFromEPR(String targetEPR) {
        return targetEPR.substring(CSGConstant.CSG_TRANSPORT_PREFIX.length());
    }

    public static String getFullUserName(String userName, String domainName) {
        return domainName == null || "".equals(domainName) ? userName : userName + "@" + domainName;
    }

    public static String getCSGServiceName(String serviceName, String userName) {
        // default will be;
        // test1@test1.org-SimpleStockQuoteService-Proxy
        // admin-SimpleStockQuoteService-Proxy
        userName = getStringProperty(CSGConstant.CSG_PROXY_PREFIX, userName);
        String delimiter = getStringProperty(CSGConstant.CSG_PROXY_DELIMITER, "-");
        return userName + delimiter + serviceName;
    }

    public static String getCSGEPR(String tenantName, String serverName, String serviceName) {
        // multi-tenant case -> csg://tenant-name/server-name/service-name
        // standalone case  -> csg://server-name/service-name
        return CSGConstant.CSG_TRANSPORT_PREFIX + (tenantName != null ? tenantName + "/" : "") +
                serverName + "/" + serviceName;
    }

    public static String getCSGThriftServerHostName() throws SocketException {
        String hostName = CSGUtils.getStringProperty(CSGConstant.THRIFT_SERVER_HOST_NAME, null);
        if (hostName == null) {
            hostName = NetworkUtils.getLocalHostname();
        }
        return hostName;
    }

    public static int getCSGThriftServerPort() {
        int port = CSGUtils.getIntProperty(CSGConstant.THRIFT_SERVER_PORT, -1);

        if (port == -1) {  // user haven't provided any port via a system property
            ServerConfiguration config = ServerConfiguration.getInstance();
            String portStr = config.getFirstProperty(CSGConstant.CSG_CARBON_PORT);
            if (!"".equals(portStr) && portStr != null) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    port = CSGConstant.DEFAULT_PORT;
                }
            } else {
                port = CSGConstant.DEFAULT_PORT;
            }
        }
        return port;
    }

    public static String getPortFromServerURL(String serverURL) {
        //https://localhost:9443/ or //https://localhost:9444
        String socket = getSocketStringFromServerURL(serverURL);
        return socket.substring(socket.indexOf(":") + 1);
    }

    public static String getHostFromServerURL(String serverURL) {
        String socket = getSocketStringFromServerURL(serverURL);
        return socket.substring(0, socket.indexOf(":"));
    }

    public static String getUserNameFromTenantUserName(String tenantUserName) {
        return tenantUserName.contains("@") ? tenantUserName.substring(0, tenantUserName.indexOf("@")) : tenantUserName;
    }

    public static String getDomainNameFromTenantUserName(String tenantUserName) {
        return tenantUserName.contains("@") ? tenantUserName.substring(tenantUserName.indexOf("@") + 1) : null;
    }
    
    public static String getTryItURLFromWSDLURL(String wsdlURL){
        //http://localhost:8280/services/SimpleStockQuoteService?wsdl ->
        //http://localhost:8280/services/SimpleStockQuoteService?tryit
        return wsdlURL.substring(0, wsdlURL.indexOf("?wsdl")) + "?tryit";
    }

    private static String getSocketStringFromServerURL(String serverURL) {
        String socket = serverURL.substring("https://".length());
        if (socket.contains("/")) {
            socket = socket.replace("/", "");
        }
        return socket;
    }


    private static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (log.isDebugEnabled()) {
            log.debug("Loading a file '" + filePath + "' from classpath");
        }

        InputStream in = cl.getResourceAsStream(filePath);
        if (in == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load file  ' " + filePath + " '");
            }

            filePath = "repository/conf" +
                    File.separatorChar + filePath;
            if (log.isDebugEnabled()) {
                log.debug("Loading a file '" + filePath + "' from classpath");
            }

            in = cl.getResourceAsStream(filePath);
            if (in == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to load file  ' " + filePath + " '");
                }
            }
        }
        if (in != null) {
            try {
                properties.load(in);
            } catch (IOException e) {
                String msg = "Error loading properties from a file at :" + filePath;
                log.error(msg, e);
            }
        }
        return properties;
    }

    public static String getContentType(Map<String, String> trpHeaders) {
        // Following constant seems to be incorrectly deprecated, see source, HTTPConstants.CONTENT_TYPE
        return trpHeaders.get(HTTPConstants.CONTENT_TYPE);
    }

    public static String getPlainToken(String encryptedToken) throws CryptoException {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        String token = "";
        if (encryptedToken != null) {
            token = new String(cryptoUtil.base64DecodeAndDecrypt(encryptedToken));
        }
        return token;
    }

    public static boolean isServerAlive(String host, int port) {
        Socket s = null;
        boolean isAlive = true;
        try {
            s = new Socket(host, port);
        } catch (IOException e) {
            isAlive = false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return isAlive;
    }
}
