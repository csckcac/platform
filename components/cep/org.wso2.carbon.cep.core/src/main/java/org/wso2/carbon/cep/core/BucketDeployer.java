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
package org.wso2.carbon.cep.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.CEPService;
import org.wso2.carbon.cep.core.internal.builder.CEPBucketBuilder;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * Deploy cep buckets as axis2 service
 */
@SuppressWarnings("unused")
public class BucketDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(BucketDeployer.class);
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfig;
    private String repositoryDirectory = null;
    private String extension = null;

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the bucket file, create a cep buckets and deploy it
     *
     * @param deploymentFileData information about the cep bucket
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String path = deploymentFileData.getAbsolutePath();
        File bucketFile = new File(path);

        BufferedInputStream inputStream = null;
        OMElement bucketElement = null;

        try {

            try {
                //read  and build the bucket from file
                inputStream = new BufferedInputStream(new FileInputStream(bucketFile));
                XMLStreamReader parser = XMLInputFactory.newInstance().
                        createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                bucketElement = builder.getDocumentElement();
                bucketElement.build();

            } catch (FileNotFoundException e) {
                String errorMessage = " .xml file cannot be found in the path : " + path;
                log.error(errorMessage, e);
                throw new DeploymentException(errorMessage, e);
            } catch (XMLStreamException e) {
                String errorMessage = "Invalid XML for " + bucketFile.getName() + " located in the path : " + path;
                log.error(errorMessage, e);
                throw new DeploymentException(errorMessage, e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    String errorMessage = "Can not close the input stream";
                    log.error(errorMessage, e);
                    throw new DeploymentException(errorMessage, e);
                }
            }

            if (!new QName(CEPConstants.CEP_CONF_NAMESPACE, CEPConstants.CEP_CONF_ELE_BUCKET).equals(bucketElement.getQName())) {
                throw new DeploymentException("Invalid root element " + bucketElement.getQName() + " in " + bucketFile.getName());
            }

            //If the CepService is present adds buckets element to the registry.
            // else keep that in memory for CepService to add them to the registry
            if (bucketElement != null) {

                CEPService cepService = CEPServiceValueHolder.getCepService();
                if (null != cepService) {
                    AxisConfiguration axisConfiguration =
                            CEPServiceValueHolder.getConfigurationContextService()
                                    .getServerConfigContext().getAxisConfiguration();

                    CEPBucketBuilder.addNewBucketToRegistry(bucketElement);
                    CEPBucketBuilder.loadBucketFromRegistry(cepService, axisConfiguration, bucketElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_NAME)));
                } else {
                    CEPServiceValueHolder.getInstance().getUnDeployedBuckets().add(bucketElement);
                }

            }

            super.deploy(deploymentFileData);

        } catch (CEPConfigurationException e) {
            String errorMessage = "wrong configuration provided for adding " + bucketFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (Throwable t) {
            /*
            Even though catching Throwable is not recommended, we do not want
            the server to fail on errors like NoClassDefFoundError
            */
            log.error("The deployment of " + bucketFile.getName() + " is not valid.", t);
            throw new DeploymentException(t);

        }
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Removing already deployed bucket
     *
     * @param fileName the path to the bucket to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String fileName) throws DeploymentException {
        // do nothing
        super.undeploy(fileName);
    }

    public void setDirectory(String directory) {
        this.repositoryDirectory = directory;
    }


}


