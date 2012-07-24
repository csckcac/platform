/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.core.deploy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This service will deploy an artifact (specified as a combination of
 * application, stage, version and revision) to a set of servers associated with
 * specified stage ( e.g. QA, PROD)
 * 
 */
public class ApplicationDeployer {

    private static final Log log = LogFactory.getLog(ApplicationDeployer.class);
    private static final String NOTIFICATION_EPR = "https://localhost:9443/services/EventNotificationService";
    private static final String EVENT = "deployment";

    /**
     * Deploys the Artifact to specified stage.
     * 
     * @param applicationId
     *            The application Id.
     * @param stage
     *            The stage to deploy ( e.g. QA, PROD)
     * @param version
     *            Version of the application
     * @param revision
     *            Revision of the application return
     * @return An array of {@link ArtifactDeploymentStatusBean} indicating the
     *         status of each deployment operation.
     * @throws AppFactoryException
     */
    public ArtifactDeploymentStatusBean[] deployArtifact(String applicationId,
                                                         String stage, String version,
                                                         String revision)
                                                         throws AppFactoryException {

        ArtifactStorage storage = ServiceHolder.getArtifactStorage();
        File file = storage.retrieveArtifact(applicationId, version, revision);

        String key =
                     new StringBuilder(AppFactoryConstants.DEPLOYMENT_STAGES).append(".")
                                                                             .append(stage)
                                                                             .append(".")
                                                                             .append(AppFactoryConstants.DEPLOYMENT_URL)
                                                                             .toString();
        String[] deploymentServerUrls =
                                        ServiceHolder.getAppFactoryConfiguration()
                                                     .getProperties(key);

        if (deploymentServerUrls.length == 0) {
            handleException("No deployment paths are configured for stage:" + stage);
        }

        ArtifactDeploymentStatusBean[] artifactDeploymentStatuses =
                                                                   new ArtifactDeploymentStatusBean[deploymentServerUrls.length];

        for (int i = 0; i < deploymentServerUrls.length; i++) {
            try {
                String deploymentServerIp =
                                            getDeploymentHostFromUrl(deploymentServerUrls[i]);

                ArtifactUploadClient artifactUploadClient =
                                                            new ArtifactUploadClient(
                                                                                     deploymentServerUrls[i]);

                UploadedFileItem uploadedFileItem = new UploadedFileItem();
                DataHandler dataHandler = new DataHandler(new FileDataSource(file));
                uploadedFileItem.setDataHandler(dataHandler);
                uploadedFileItem.setFileName(file.getName());
                uploadedFileItem.setFileType("jar");
                UploadedFileItem[] uploadedFileItems = { uploadedFileItem };

                if (artifactUploadClient.authenticate(getAdminUsername(applicationId),
                                                      getServerAdminPassword(),
                                                      deploymentServerIp)) {

                    artifactUploadClient.uploadCarbonApp(uploadedFileItems);
                    log.debug(file.getName() + " is successfully uploaded.");
                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                                    " to deploy the artifact:" + file.getName());
                }

                artifactDeploymentStatuses[i] =
                                                new ArtifactDeploymentStatusBean(
                                                                                 applicationId,
                                                                                 stage,
                                                                                 version,
                                                                                 revision,
                                                                                 deploymentServerUrls[i],
                                                                                 "success",
                                                                                 null);

            } catch (Exception e) {

                artifactDeploymentStatuses[i] =
                                                new ArtifactDeploymentStatusBean(
                                                                                 applicationId,
                                                                                 stage,
                                                                                 version,
                                                                                 revision,
                                                                                 deploymentServerUrls[i],
                                                                                 "failed",
                                                                                 e.getMessage());

                handleException("Failed to upload the artifact:" + file.getName() +
                                " of application:" + applicationId +
                                " to deployment location:" + deploymentServerUrls[i]);
            }

        }
        sendDeploymentNotification(applicationId,String.valueOf(isDeploymentSuccessful(artifactDeploymentStatuses)));

        return artifactDeploymentStatuses;
    }
    
    private Boolean isDeploymentSuccessful(ArtifactDeploymentStatusBean[] deploymentStatusBeans) {
        for(ArtifactDeploymentStatusBean deploymentStatus : deploymentStatusBeans) {
            if(false == Boolean.valueOf(deploymentStatus.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private String getAdminUsername() {
        return ServiceHolder.getAppFactoryConfiguration()
                       .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }

    private String getAdminUsername(String applicationId) {
        return ServiceHolder.getAppFactoryConfiguration()
                            .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME) +
               "@" + applicationId;
    }

    private String getServerAdminPassword() {
        return ServiceHolder.getAppFactoryConfiguration()
                            .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
    }

    private void handleException(String msg) throws AppFactoryException {
        log.error(msg);
        throw new AppFactoryException(msg);
    }

    private void handleException(String msg, Throwable throwable)
                                                                 throws AppFactoryException {
        log.error(msg, throwable);
        throw new AppFactoryException(msg, throwable);
    }

    private String getDeploymentHostFromUrl(String url) throws AppFactoryException {
        String hostName = null;
        try {
            URL deploymentURL = new URL(url);
            hostName = deploymentURL.getHost();
        } catch (MalformedURLException e) {
            handleException("Deployment url is malformed.", e);
        }

        return hostName;
    }

    private void sendDeploymentNotification(final String applicationId, final String result) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
                try {
                    //Create a service client
                    ServiceClient client = new ServiceClient();

                    //Set the endpoint address
                    client.getOptions().setTo(new EndpointReference(NOTIFICATION_EPR));
                    CarbonUtils.setBasicAccessSecurityHeaders(getAdminUsername(), getServerAdminPassword(), false, client);

                    //Make the request and get the response
                    client.sendRobust(getNotificationPayload(applicationId, EVENT, result));
                } catch (AxisFault e) {
                    log.error(e);
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    private static OMElement getNotificationPayload(String applicationId, String event,
                                                    String result)
            throws XMLStreamException, javax.xml.stream.XMLStreamException {

        String payload = "<ser:publishEvent xmlns:ser=\"http://service.notification.events.appfactory.carbon.wso2.org\">" +
                         "<ser:event xmlns:ser=\"http://service.notification.events.appfactory.carbon.wso2.org\">" +
                         "<xsd:applicationId xmlns:xsd=\"http://service.notification.events.appfactory.carbon.wso2.org/xsd\">" + applicationId + "</xsd:applicationId>" +
                         "<xsd:event xmlns:xsd=\"http://service.notification.events.appfactory.carbon.wso2.org/xsd\">" + event + "</xsd:event>" +
                         "<xsd:result xmlns:xsd=\"http://service.notification.events.appfactory.carbon.wso2.org/xsd\">" + result + "</xsd:result>" +
                         "</ser:event></ser:publishEvent>";
        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }

}
