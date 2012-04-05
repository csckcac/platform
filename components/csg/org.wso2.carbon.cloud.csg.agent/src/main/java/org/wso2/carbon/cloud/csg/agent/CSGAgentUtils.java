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
package org.wso2.carbon.cloud.csg.agent;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.SynapseBinaryDataSource;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.cloud.csg.agent.client.AuthenticationClient;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.cloud.csg.common.CSGServerBean;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import javax.activation.DataHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;

public class CSGAgentUtils {

    private static Log log = LogFactory.getLog(CSGAgentUtils.class);

    /**
     * Returns the session cookie given the admin credentials
     *
     * @param serverUrl  the server url
     * @param userName   user name
     * @param password   password
     * @param domainName Domain Name
     * @param hostName   host name of the remote server
     * @return the session cookie
     * @throws java.net.SocketException throws in case of a socket exception
     * @throws org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException
     *                                  throws in case of a authentication failure
     * @throws java.rmi.RemoteException throws in case of a connection failure
     */
    public static String getSessionCookie(String serverUrl, String userName, String password,
                                          String domainName, String hostName) throws
            SocketException, RemoteException, LoginAuthenticationExceptionException {
        AuthenticationClient authClient = new AuthenticationClient();
        return authClient.getSessionCookie(serverUrl, userName, password, hostName, domainName);
    }

    /**
     * Create the {@link org.wso2.carbon.cloud.csg.common.CSGServerBean} from the registry resource
     *
     * @param resource the csg server meta information collection
     * @return the CSGServer bean created from the meta information
     * @throws org.wso2.carbon.cloud.csg.common.CSGException
     *          throws in case of an error
     */
    public static CSGServerBean getCSGServerBean(Resource resource) throws CSGException {
        CSGServerBean bean = new CSGServerBean();
        try {
            bean.setHost(resource.getProperty(CSGConstant.CSG_SERVER_HOST));
            bean.setName(resource.getProperty(CSGConstant.CSG_SERVER_NAME));
            bean.setUserName(resource.getProperty(CSGConstant.CSG_SERVER_USER_NAME));
            bean.setPort(resource.getProperty(CSGConstant.CSG_SERVER_PORT));
            bean.setDomainName(resource.getProperty(CSGConstant.CSG_SERVER_DOMAIN_NAME));

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            String plainPassWord = new String(cryptoUtil.base64DecodeAndDecrypt(
                    resource.getProperty(CSGConstant.CSG_SERVER_PASS_WORD)));
            bean.setPassWord(plainPassWord);

            return bean;
        } catch (CryptoException e) {
            throw new CSGException("Could not get the CSG server information from the resource: " +
                    resource, e);
        }
    }

    /**
     * Persist the server into registry
     *
     * @param registry  registry instance
     * @param csgServer csg server instance
     * @throws CSGException throws in case of an error
     */
    public static void persistServer(org.wso2.carbon.registry.core.Registry registry,
                                     CSGServerBean csgServer) throws CSGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        try {
            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            Collection collection = registry.newCollection();
            if (!registry.resourceExists(CSGConstant.REGISTRY_CSG_RESOURCE_PATH)) {
                registry.put(CSGConstant.REGISTRY_CSG_RESOURCE_PATH, collection);
                if (!registry.resourceExists(CSGConstant.REGISTRY_SERVER_RESOURCE_PATH)) {
                    registry.put(CSGConstant.REGISTRY_SERVER_RESOURCE_PATH, collection);
                }
            }

            Resource resource = registry.newResource();
            resource.addProperty(CSGConstant.CSG_SERVER_NAME, csgServer.getName());
            resource.addProperty(CSGConstant.CSG_SERVER_HOST, csgServer.getHost());
            resource.addProperty(CSGConstant.CSG_SERVER_USER_NAME, csgServer.getUserName());
            resource.addProperty(CSGConstant.CSG_SERVER_PORT, csgServer.getPort());
            resource.addProperty(CSGConstant.CSG_SERVER_DOMAIN_NAME, csgServer.getDomainName());

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            resource.addProperty(CSGConstant.CSG_SERVER_PASS_WORD,
                    cryptoUtil.encryptAndBase64Encode(csgServer.getPassWord()
                            .getBytes()));

            registry.put(CSGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServer.getName(),
                    resource);

        } catch (Exception e) {
            isTransactionSuccess = false;
            throw new CSGException("Error occurred while saving the content into registry", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    log.error("Error occurred while trying to rollback or commit the transaction",
                            re);
                }
            }

        }
    }

    /**
     * Check if we have a client axis2.xml ( for example in ESB)
     *
     * @return true if we have client axis2.xml, false otherwise
     */
    public static boolean isClientAxis2XMLExists() {
        File f = new File(CSGConstant.CLIENT_AXIS2_XML);
        return f.exists();
    }

    public static OMNode getOMElementFromURI(String wsdlURI) throws CSGException {
        if (wsdlURI == null || "null".equals(wsdlURI)) {
            throw new CSGException("Can't create URI from a null value");
        }
        URL url;
        try {
            url = new URL(wsdlURI);
        } catch (MalformedURLException e) {
            throw new CSGException("Invalid URI reference '" + wsdlURI + "'");
        }
        URLConnection connection;
        connection = getURLConnection(url);
        if (connection == null) {
            throw new CSGException("Cannot create a URLConnection for given URL : " + url);
        }
        connection.setReadTimeout(getReadTimeout());
        connection.setConnectTimeout(getConnectTimeout());
        connection.setRequestProperty("Connection", "close"); // if http is being used
        InputStream inStream = null;

        try {
            inStream = connection.getInputStream();
            StAXOMBuilder builder = new StAXOMBuilder(inStream);
            OMElement doc = builder.getDocumentElement();
            doc.build();
            return doc;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.info("Content at URL : " + url + " is non XML..");
            }
            return readNonXML(url);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.warn("Error while closing the input stream to: " + url, e);
            }
        }
    }

    private static int getReadTimeout() {
        return Integer.parseInt(CSGUtils.getStringProperty(
                CSGConstant.READTIMEOUT,
                String.valueOf(CSGConstant.DEFAULT_READTIMEOUT)));

    }

    private static int getConnectTimeout() {
        return Integer.parseInt(CSGUtils.getStringProperty(
                CSGConstant.CONNECTTIMEOUT,
                String.valueOf(CSGConstant.DEFAULT_CONNECTTIMEOUT)));
    }

    private static OMNode readNonXML(URL url) throws CSGException {

        try {
            // Open a new connection
            URLConnection newConnection = getURLConnection(url);
            if (newConnection == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot create a URLConnection for given URL : " + url);
                }
                return null;
            }

            BufferedInputStream newInputStream = new BufferedInputStream(
                    newConnection.getInputStream());

            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            return omFactory.createOMText(
                    new DataHandler(new SynapseBinaryDataSource(newInputStream,
                            newConnection.getContentType())), true);

        } catch (IOException e) {
            throw new CSGException("Error when getting a stream from resource's content", e);
        }
    }

    private static URLConnection getURLConnection(URL url) throws CSGException {
        URLConnection connection;
        if (url.getProtocol().equalsIgnoreCase("https")) {
            String msg = "Connecting through doesn't support";
            log.error(msg);
            throw new CSGException(msg);
        } else {
            try {
                connection = url.openConnection();
            } catch (IOException e) {
                throw new CSGException("Could not open the URL connection", e);
            }
        }
        connection.setReadTimeout(getReadTimeout());
        connection.setConnectTimeout(getConnectTimeout());
        connection.setRequestProperty("Connection", "close"); // if http is being used
        return connection;
    }
}
