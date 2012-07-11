package org.wso2.carbon.appfactory.core.build;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

import javax.xml.stream.XMLStreamException;


public class DefaultBuildDriverListener implements BuildDriverListener {

    private static final Log log = LogFactory.getLog(DefaultBuildDriverListener.class);
    private static final String EPR = "http://localhost:9763/services/ArtifactCreateCallbackService";

    @Override
    public void onBuildSuccessful(String applicationId, String version, String revision, File file) {
        ArtifactStorage storage = ServiceHolder.getArtifactStorage();
        //storage.storeArtifact(applicationId, version, revision, file);

        // call the bpel back
        sendMessageToCreateArtifactCallback(applicationId, version, revision);

    }

    @Override
    public void onBuildFailure(String applicationId, String version, String revision, File file)
            throws AppFactoryException {


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
                    client.getOptions().setTo(new EndpointReference(EPR));

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
        String payload = "<p:callbackMessgae xmlns:p=\"http://localhost:9763/services/ArtifactCreateCallbackService\"><applicationId>" + applicationId +
                "</applicationId><revision>" + revision + "</revision><version>" + version + "</version></p:callbackMessgae>";

        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }


}
