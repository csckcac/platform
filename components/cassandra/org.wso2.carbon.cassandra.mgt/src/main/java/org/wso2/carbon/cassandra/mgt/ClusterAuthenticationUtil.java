/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.cassandra.mgt;

import me.prettyprint.hector.api.Cluster;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDSComponent;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.UUID;

/**
 * This class used to get cluster details with proper authentication.
 */
public class ClusterAuthenticationUtil {

    HttpSession httpSession;
    String tennantDomain;
    private static final String USER_ACCESSKEY_ATTR_NAME = "cassandra.user.password";
    private static final String CASSANDRA_AUTH_CONF = "repository" + File.separator + "conf"
            + File.separator + "etc" + File.separator
            + "cassandra-auth.xml";
    private static final Log log = LogFactory.getLog(ClusterAuthenticationUtil.class);

    public ClusterAuthenticationUtil(HttpSession httpSession, String tennatDomain) {
        this.httpSession = httpSession;
        this.tennantDomain = tennatDomain;
    }

    public Cluster getCluster(ClusterInformation clusterInformation)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        Cluster cluster;
        boolean resetConnection = true;
        try {
            if (clusterInformation != null) {
                cluster = dataAccessService.getCluster(clusterInformation, resetConnection);
            } else {
                String sharedKey = getSharedKey();
                cluster = dataAccessService.getClusterForCurrentUser(sharedKey, resetConnection);
            }
            return cluster;
        } catch (Throwable e) {
            httpSession.removeAttribute(USER_ACCESSKEY_ATTR_NAME); //this allows to get a new key
            String message = "Error getting cluster";
            throw new CassandraServerManagementException(message, log);
        }
    }

    private String getSharedKey() throws AxisFault {

        String sharedKey = (String) httpSession.getAttribute(USER_ACCESSKEY_ATTR_NAME);
        if (sharedKey == null) {
            try {
                synchronized (CassandraKeyspaceAdmin.class) {
                    sharedKey =
                            (String) httpSession
                                    .getAttribute(USER_ACCESSKEY_ATTR_NAME);
                    if (sharedKey == null) {

                        OMElement cassandraAuthConfig = loadCassandraAuthConfigXML();
                        String epr = null;
                        OMElement serverEPR = cassandraAuthConfig.getFirstChildWithName(new QName("EPR"));
                        if (serverEPR != null) {
                            String url = serverEPR.getText();
                            if (url != null && !"".equals(url.trim())) {
                                epr = url;
                            }
                        }

                        String username = null;
                        OMElement cassandraUser = cassandraAuthConfig.getFirstChildWithName(new QName("User"));
                        if (cassandraUser != null) {
                            String user = cassandraUser.getText();
                            if (user != null && !"".equals(user.trim())) {
                                username = user;
                            }
                        }

                        String password = null;
                        OMElement cassandraPasswd = cassandraAuthConfig.getFirstChildWithName(new QName("Password"));
                        if (cassandraPasswd != null) {
                            String passwd = cassandraPasswd.getText();
                            if (passwd != null && !"".equals(passwd.trim())) {
                                password = passwd;
                            }
                        }
                        String targetUser = (String) httpSession.
                                getAttribute(ServerConstants.USER_LOGGED_IN);
                        String targetDomain = tennantDomain;
                        if (targetDomain != null) {
                            targetUser = targetUser + "@" + targetDomain;
                        }
                        sharedKey = UUID.randomUUID().toString();
                        httpSession.setAttribute(USER_ACCESSKEY_ATTR_NAME, sharedKey);
                        OMElement payload = getPayload(username, password, targetUser, sharedKey);
                        ServiceClient serviceClient = new ServiceClient(CassandraAdminDSComponent.getConfigCtxService()
                                .getClientConfigContext(), null);
                        Options options = new Options();
                        options.setAction("urn:injectAccessKey");
                        options.setProperty(Constants.Configuration.TRANSPORT_URL, epr);
                        serviceClient.setOptions(options);
                        serviceClient.sendRobust(payload);
                        serviceClient.cleanupTransport();
                    }
                }
            } catch (AxisFault e) {
                sharedKey = null;
                httpSession.removeAttribute(USER_ACCESSKEY_ATTR_NAME);
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        return sharedKey;
    }

    private OMElement getPayload(String username, String password, String targetUser,
                                       String accessKey) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://sharedkey.cassandra.carbon.wso2.org", "m0");

        OMElement getKey = factory.createOMElement("giveMeAccessKey", ns);

        OMElement usernameElem = factory.createOMElement("username", ns);
        OMElement passwordElem = factory.createOMElement("password", ns);
        OMElement targetUserElem = factory.createOMElement("targetUser", ns);
        OMElement targetAccessKeyElem = factory.createOMElement("accessKey", ns);

        usernameElem.setText(username);
        passwordElem.setText(password);
        targetUserElem.setText(targetUser);
        targetAccessKeyElem.setText(accessKey);

        getKey.addChild(usernameElem);
        getKey.addChild(passwordElem);
        getKey.addChild(targetUserElem);
        getKey.addChild(targetAccessKeyElem);

        return getKey;
    }

    private OMElement loadCassandraAuthConfigXML() {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + File.separator + CASSANDRA_AUTH_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + CASSANDRA_AUTH_CONF + ". Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<Cassandra/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            log.error(CASSANDRA_AUTH_CONF + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            log.error("Invalid XML for " + CASSANDRA_AUTH_CONF + " located in " +
                    "the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

}
