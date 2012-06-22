package org.wso2.carbon.bam.kpiagent;

import org.apache.log4j.Logger;
import org.wso2.carbon.eventbridge.agent.thrift.Agent;
import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;
import org.wso2.carbon.eventbridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.eventbridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.exception.*;

import javax.security.sasl.AuthenticationException;
import java.net.*;
import java.util.Enumeration;

public class KPIAgent {
    private static Logger logger = Logger.getLogger(KPIAgent.class);
    public static final String STREAM_NAME1 = "org.wso2.bam.kpiii";
    public static final String STREAM_NAME2 = "org.wso2.bam.kpiii";
    public static final String STREAM_NAME3 = "org.wso2.bam.kpi.bar";
    public static final String VERSION1 = "1.0.5";
    public static final String VERSION2 = "1.1.5";
    public static final String VERSION3 = "2.0.5";


    public static void main(String[] args) throws AgentException, MalformedStreamDefinitionException,
            StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, MalformedURLException,
            AuthenticationException, NoStreamDefinitionExistException,
            org.wso2.carbon.eventbridge.commons.exception.AuthenticationException, TransportException, SocketException {
        System.out.println("Starting BAM KPI Agent");
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        Agent agent = new Agent(agentConfiguration);
        String host;
        //host = InetAddress.getLocalHost().getHostAddress();
        host = getLocalAddress().getHostAddress();
        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":7611", "admin", "admin", agent);

        String streamId1 = null;
        String streamId2 = null;
        String streamId3 = null;


        try {
            streamId1 = dataPublisher.findEventStream(STREAM_NAME1, VERSION1);
//            streamId2 = dataPublisher.findEventStream(STREAM_NAME2, VERSION2);
//            streamId3 = dataPublisher.findEventStream(STREAM_NAME3, VERSION3);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId1 = dataPublisher.defineEventStream("{" +
                    "  'name':'" + STREAM_NAME1 + "'," +
                    "  'version':'" + VERSION1 + "'," +
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

//            streamId2 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME2 +"'," +
//                    "  'version':'"+ VERSION2 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");
//
//            streamId3 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME3 +"'," +
//                    "  'version':'"+ VERSION3 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");
//            //Define event stream
        }

        try {
//            streamId1 = dataPublisher.findEventStream(STREAM_NAME1, VERSION1);
            streamId2 = dataPublisher.findEventStream(STREAM_NAME2, VERSION2);
//            streamId3 = dataPublisher.findEventStream(STREAM_NAME3, VERSION3);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
//            streamId1 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME1 +"'," +
//                    "  'version':'"+ VERSION1 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");

            streamId2 = dataPublisher.defineEventStream("{" +
                    "  'name':'" + STREAM_NAME2 + "'," +
                    "  'version':'" + VERSION2 + "'," +
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
//
//            streamId3 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME3 +"'," +
//                    "  'version':'"+ VERSION3 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");
//            //Define event stream
        }

        try {
//            streamId1 = dataPublisher.findEventStream(STREAM_NAME1, VERSION1);
//            streamId2 = dataPublisher.findEventStream(STREAM_NAME2, VERSION2);
            streamId3 = dataPublisher.findEventStream(STREAM_NAME3, VERSION3);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
//            streamId1 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME1 +"'," +
//                    "  'version':'"+ VERSION1 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");

//            streamId2 = dataPublisher.defineEventStream("{" +
//                    "  'name':'"+ STREAM_NAME2 +"'," +
//                    "  'version':'"+ VERSION2 +"'," +
//                    "  'nickName': 'KPI'," +
//                    "  'description': 'Product Sale'," +
//                    "  'metaData':[" +
//                    "          {'name':'clientType','type':'STRING'}" +
//                    "  ]," +
//                    "  'payloadData':[" +
//                    "          {'name':'item','type':'STRING'}," +
//                    "          {'name':'quantity','type':'DOUBLE'}," +
//                    "          {'name':'total','type':'DOUBLE'}," +
//                    "          {'name':'user','type':'STRING'}" +
//                    "  ]" +
//                    "}");
//
            streamId3 = dataPublisher.defineEventStream("{" +
                    "  'name':'" + STREAM_NAME3 + "'," +
                    "  'version':'" + VERSION3 + "'," +
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
//            //Define event stream
        }


        //Publish event for a valid stream
        if (!streamId1.isEmpty()) {
            System.out.println("Stream ID: " + streamId1);

            for (int i = 0; i < 100; i++) {
                publishEvents(dataPublisher, streamId1, i);
                publishEvents(dataPublisher, streamId2, i);
                publishEvents(dataPublisher, streamId3, i);
                System.out.println("Events published : " + (i + 1) * 7 * 3);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int i) throws AgentException {
        Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Milk", 20, 3600.0, "John"});
        dataPublisher.publish(eventOne);
        Event eventTwo = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Cake", 40, 3400.0, "Rick"});
        dataPublisher.publish(eventTwo);
        Event eventThree = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Butter", 2, 150.0, "Marc"});
        dataPublisher.publish(eventThree);
        Event eventFour = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Fanta", 2, 520.0, "Tom"});
        dataPublisher.publish(eventFour);
        Event eventFive = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Bread", 3, 260.0, "Mary"});
        dataPublisher.publish(eventFive);
        Event eventSix = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Orange", 1, 520.0, "Ivan"});
        dataPublisher.publish(eventSix);
        Event eventSeven = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                new Object[]{"Sugar", 2, 150.0, "Paul"});
        dataPublisher.publish(eventSeven);

    }

    public static InetAddress getLocalAddress() throws SocketException
    {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while( ifaces.hasMoreElements() )
        {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while( addresses.hasMoreElements() )
            {
                InetAddress addr = addresses.nextElement();
                if( addr instanceof Inet4Address && !addr.isLoopbackAddress() )
                {
                    return addr;
                }
            }
        }

        return null;
    }
}
