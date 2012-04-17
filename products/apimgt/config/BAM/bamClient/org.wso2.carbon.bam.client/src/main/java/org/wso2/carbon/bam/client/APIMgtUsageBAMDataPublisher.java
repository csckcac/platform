package org.wso2.carbon.bam.client;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.log4j.BasicConfigurator;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.utils.FileUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIMgtUsageBAMDataPublisher {

    /**
     * EventReceiver instance that holds config of the receiver in BAM server
     */
    private EventReceiver eventReceiver;

    private Agent agent;


    String bamServerThriftPort;
    String bamServerURL;
    String bamServerUser;
    String bamServerPassword;
    String bamAgentTrustStore;
    String bamAgentTrustStorePassword;

    /**
     * init using constructor
     */
    public APIMgtUsageBAMDataPublisher() throws XMLStreamException, IOException {
        this.eventReceiver = createEventReceiver();
        AgentConfiguration configuration = new AgentConfiguration();
        agent = new Agent(configuration);
    }

    private EventReceiver createEventReceiver() throws IOException, XMLStreamException {

        String config = FileUtil.readFileToString(System.getProperty("configFilePath") + "amConfig.xml");

        OMElement omElement = AXIOMUtil.stringToOM(config);

        OMElement apiMGTDataAgentConfig = omElement.getFirstChildWithName(new QName("apiMGTDataAgentConfig"));

        bamServerThriftPort = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamServerThriftPort")).getText();
        bamServerURL = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamServerURL")).getText();
        bamServerUser = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamServerUser")).getText();
        bamServerPassword = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamServerPassword")).getText();
        bamAgentTrustStore = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamAgentTrustStore")).getText();
        
        String carbonHome = System.getProperty("carbon.home");

        bamAgentTrustStore = carbonHome + File.separator + bamAgentTrustStore;
        bamAgentTrustStorePassword = apiMGTDataAgentConfig.getFirstChildWithName(
                new QName("bamAgentTrustStorePassword")).getText();


        EventReceiver eventReceiver = new EventReceiver();
        System.setProperty("javax.net.ssl.trustStore",
                           bamAgentTrustStore);
        System.setProperty("javax.net.ssl.trustStorePassword",
                           bamAgentTrustStorePassword);
        eventReceiver.setUrl(bamServerURL);
        eventReceiver.setUserName(bamServerUser);
        eventReceiver.setPassword(bamServerPassword);
        eventReceiver.setPort(Integer.parseInt(
                bamServerThriftPort));
        eventReceiver.setSocketTransportEnabled(true);
        return eventReceiver;

    }


    public void publishEvent() {

        Event event = new Event();
        event.setCorrelation(createCorrelationMap());
        event.setEvent(createEventMap());
        event.setMeta(createMetaDataMap());

        List<Event> events = new ArrayList<Event>();
        events.add(event);

        BasicConfigurator.configure();
        agent.publish(events, eventReceiver);
    }

    private Map<String, ByteBuffer> createMetaDataMap() {
        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
        // not used, but need as it is not sure how BAM will work.
        metaDataMap.put("metaKey", ByteBuffer.wrap("metaValue".getBytes()));
        return metaDataMap;
    }

    private  Map<String, ByteBuffer> createCorrelationMap() {
        Map<String, ByteBuffer> correlationMap = new HashMap<String, ByteBuffer>();
        // not used, but need as it is not sure how BAM will work.
        correlationMap.put("correlationKey", ByteBuffer.wrap("correlationValue".getBytes()));
        return correlationMap;
    }

    private  Map<String, ByteBuffer> createEventMap() {
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        // not used, but need as it is not sure how BAM will work.
        eventMap.put("eventKey", ByteBuffer.wrap("eventValue".getBytes()));
        return eventMap;
    }


/*    private static String readContents(String path) {

        StringBuilder contents = new StringBuilder();

        try {

            BufferedReader input = new BufferedReader(new FileReader(path));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }*/
}
