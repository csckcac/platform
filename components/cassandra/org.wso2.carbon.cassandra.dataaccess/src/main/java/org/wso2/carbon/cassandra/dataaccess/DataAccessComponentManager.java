package org.wso2.carbon.cassandra.dataaccess;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.authentication.SharedKeyAccessService;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
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

/**
 * Keeps required services for implementing DataAccess Service
 */

public class DataAccessComponentManager {

    private static Log log = LogFactory.getLog(DataAccessComponentManager.class);
    private static DataAccessComponentManager ourInstance = new DataAccessComponentManager();

    /* To be used to find cassandra component configuration*/
    private ClusterConfiguration clusterConfiguration;
    private static final String CASSANDRA_COMPONENT_CONF = File.separator + "repository" + File.separator + "conf"
                                                           + File.separator + "etc" + File.separator + "cassandra-component.xml";
    private SharedKeyAccessService sharedKeyAccessService;

    private boolean initialized = false;

    public static DataAccessComponentManager getInstance() {
        return ourInstance;
    }

    private DataAccessComponentManager() {
    }

    public void init(SharedKeyAccessService sharedKeyAccessService) {
        this.sharedKeyAccessService = sharedKeyAccessService;
        this.clusterConfiguration = ClusterConfigurationFactory.create(loadConfigXML());
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ClusterConfiguration getClusterConfiguration() {
        assertInitialized();
        return clusterConfiguration;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new DataAccessComponentException("Cassandra DataAccess component has not been initialized", log);
        }
    }

    public SharedKeyAccessService getSharedKeyAccessService() {
        assertInitialized();
        return sharedKeyAccessService;
    }


    /**
     * Helper method to load the cassandra server config
     *
     * @return OMElement representation of the cep config
     */
    private OMElement loadConfigXML() {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + CASSANDRA_COMPONENT_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + CASSANDRA_COMPONENT_CONF + ". Using the default configuration");
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
            throw new DataAccessComponentException(CASSANDRA_COMPONENT_CONF + "cannot be found in the path : " + path, e, log);
        } catch (XMLStreamException e) {
            throw new DataAccessComponentException("Invalid XML for " + CASSANDRA_COMPONENT_CONF + " located in " +
                                                   "the path : " + path, e, log);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
            }
        }
    }
}
