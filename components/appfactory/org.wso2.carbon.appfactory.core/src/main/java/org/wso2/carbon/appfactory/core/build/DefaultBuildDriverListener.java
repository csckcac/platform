package org.wso2.carbon.appfactory.core.build;

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
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;


public class DefaultBuildDriverListener implements BuildDriverListener {

    private static final Log log = LogFactory.getLog(DefaultBuildDriverListener.class);
    private static final String ARTIFACT_CREATE_EPR = "http://localhost:9763/services/ArtifactCreateCallbackService";
    private static final String NOTIFICATION_EPR = "https://localhost:9443/services/EventNotificationService";
    private static final String EVENT = "build";
    private static final String SUCCESS = "successful";
    private static final String FAILED = "failed";

    @Override
    public void onBuildSuccessful(String applicationId, String version, String revision, File file) {
        ArtifactStorage storage = ServiceHolder.getArtifactStorage();
        //storage.storeArtifact(applicationId, version, revision, file);
        // call the bpel back
        sendMessageToCreateArtifactCallback(applicationId, version, revision);
        sendEventNotification(applicationId,EVENT,SUCCESS);

    }

    @Override
    public void onBuildFailure(String applicationId, String version, String revision, File file)
            throws AppFactoryException {
        sendEventNotification(applicationId,EVENT,FAILED);
    }

    public void sendMessageToCreateArtifactCallback(final String applicationId, final String version, final String revision) {
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
                    client.getOptions().setTo(new EndpointReference(ARTIFACT_CREATE_EPR));

                    //Make the request and get the response
                    client.sendRobust(getPayload(applicationId, version, revision));
                } catch (AxisFault e) {
                    log.error(e);
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    private static OMElement getPayload(String applicationId, String version, String revision) throws XMLStreamException, javax.xml.stream.XMLStreamException {
        String payload = "<p:callbackMessgae xmlns:p=\"http://localhost:9763/services/ArtifactCreateCallbackService\"><p:applicationId>" + applicationId +
                "</p:applicationId><p:revision>" + revision + "</p:revision><p:version>" + version + "</p:version></p:callbackMessgae>";

        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }

    public void sendEventNotification(final String applicationId, final String event, final String result) {
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
                    client.sendRobust(getNotificationPayload(applicationId, event, result));
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

    private String getAdminUsername() {
        return ServiceHolder.getAppFactoryConfiguration()
                       .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }

    private String getServerAdminPassword() {
        return ServiceHolder.getAppFactoryConfiguration()
                .getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
    }
}
