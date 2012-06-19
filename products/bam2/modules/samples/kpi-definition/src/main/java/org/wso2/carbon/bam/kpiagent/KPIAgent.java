package org.wso2.carbon.bam.kpiagent;

import org.apache.log4j.Logger;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.exception.*;
import org.wso2.carbon.agent.conf.AgentConfiguration;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;

import java.net.MalformedURLException;


public class KPIAgent {
    private static Logger logger = Logger.getLogger(KPIAgent.class);

    public static void main(String[] args) throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, MalformedURLException, AuthenticationException, TransportException, NoStreamDefinitionExistException {
        System.out.println("Starting BAM KPI Agent");
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        Agent agent = new Agent(agentConfiguration);
        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7631", "admin", "admin", agent);
        //Define event stream
        String streamId = dataPublisher.defineEventStream("{" +
                "  'name':'org.wso2.bam.kpisample'," +
                "  'version':'1.0.0'," +
                "  'nickName': 'KPI'," +
                "  'description': 'Product Sale'," +
                "  'metaData':[" +
                "          {'name':'clientType','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'item','type':'STRING'}," +
                "          {'name':'quantity','type':'INT'}," +
                "          {'name':'total','type':'DOUBLE'}," +
                "          {'name':'user','type':'STRING'}" +
                "  ]" +
                "}");

        //Publish event for a valid stream
        if (!streamId.isEmpty()) {
            System.out.println("Stream ID: " + streamId);
            Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Milk", 20, 3600.0, "John"});
            dataPublisher.publish(eventOne);
            Event eventTwo = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Cake", 40, 3400.0, "Rick"});
            dataPublisher.publish(eventTwo);
            Event eventThree = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Butter", 2, 150.0, "Marc"});
            dataPublisher.publish(eventThree);
            Event eventFour = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Fanta", 2, 520.0, "Tom"});
            dataPublisher.publish(eventFour);
            Event eventFive = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Bread", 3, 260.0, "Mary"});
            dataPublisher.publish(eventFive);
            Event eventSix = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Orange", 1, 520.0, "Ivan"});
            dataPublisher.publish(eventSix);
            Event eventSeven = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, new Object[]{"Sugar", 2, 150.0, "Paul"});
            dataPublisher.publish(eventSeven);
            dataPublisher.stop();
        }
    }
}
