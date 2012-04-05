/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.automation.ravana.test.ravanautils;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.admin.service.AdminServiceSynapseConfigAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.dbutils.MySqlDatabaseManager;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.SQLException;

import static org.testng.Assert.*;

public abstract class RavanaTestMaster {
    private static final Log logRavanaTemplate = LogFactory.getLog(RavanaTestMaster.class);
    public static MySqlDatabaseManager mysqlDBMgt = null;


    @BeforeClass
    public void initializeProperties() throws Exception {
        updateSynapseConfiguration();
        createConnection();
    }

    @AfterClass
    public void cleanup() throws SQLException {
        mysqlDBMgt.disconnect();
    }

    private void createConnection() throws ClassNotFoundException, SQLException {
        FrameworkProperties properties = new FrameworkProperties();
        String dbInstanceName = "ravana";
        String connectionURL = properties.getRavana().getJdbc_Url();
        String password = properties.getRavana().getDBpassword();
        String userName = properties.getRavana().getDbUser();

        mysqlDBMgt = new MySqlDatabaseManager(connectionURL, userName, password);

    }

    private static void updateSynapseConfiguration() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(0);
        ManageEnvironment environment = builder.build();
        String backendURL = environment.getEsb().getBackEndUrl();
        String synapseFileLocation = builder.getFrameworkSettings().getRavana().getFrameworkPath() + File.separator +
                                     "scenario" + File.separator + "wso2" + File.separator + "synapse.xml";
        logRavanaTemplate.debug("Synapse file content" + convertXMLFileToString(synapseFileLocation));
        UserInfo tenantDetails = UserListCsvReader.getUserInfo(0);
        String sessionCookie = environment.getEsb().getSessionCookie();
        AdminServiceSynapseConfigAdmin synapseConfigAdmin =
                new AdminServiceSynapseConfigAdmin(sessionCookie, backendURL);
        assertTrue(synapseConfigAdmin.updateConfiguration(convertXMLFileToString(synapseFileLocation)),
                   "Synapse configuration update failed");
        logRavanaTemplate.info("synapse configuration updated");
        Thread.sleep(1000 * 30); // waif for 30 sec

    }

    private static String convertXMLFileToString(String fileName) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(new File(fileName));
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        return stw.toString();
    }

}
