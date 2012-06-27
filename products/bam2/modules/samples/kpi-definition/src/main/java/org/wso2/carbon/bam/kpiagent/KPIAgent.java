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
import java.util.Random;

public class KPIAgent {
    private static Logger logger = Logger.getLogger(KPIAgent.class);
    public static final String STREAM_NAME1 = "org.wso2.bam.kpi";
    public static final String STREAM_NAME2 = "org.wso2.bam.kpiii";
    public static final String STREAM_NAME3 = "org.wso2.bam.retailstore.kpi";
    public static final String VERSION1 = "1.0.5";
    public static final String VERSION2 = "1.1.5";
    public static final String VERSION3 = "2.0.5";

    public static final String[] foods = {"Rice", "Wheat", "Oat", "Millet", "Corn"};
    public static final String[] users = {"James", "Mary" , "John", "Peter", "Harry" , "Tom", "Paul"};
    public static final int[] quantity = {2, 6, 3, 4, 7};
    public static final int[] price = {120, 130, 125, 140, 115};

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

        host = getLocalAddress().getHostAddress();
        //create data publisher

        DataPublisher dataPublisher = new DataPublisher("tcp://" +host+ ":7611", "admin", "admin", agent);

        String streamId1 = null;
        String streamId2 = null;
        String streamId3 = null;

        try {
            streamId1 = dataPublisher.findEventStream(STREAM_NAME1, VERSION1);
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
                    "          {'name':'total','type':'INT'}," +
                    "          {'name':'user','type':'STRING'}" +
                    "  ]" +
                    "}");
//            //Define event stream
        }

        try {
            streamId2 = dataPublisher.findEventStream(STREAM_NAME2, VERSION2);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
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
                    "          {'name':'total','type':'INT'}," +
                    "          {'name':'user','type':'STRING'}" +
                    "  ]" +
                    "}");
//            //Define event stream
        }

        try {
            streamId3 = dataPublisher.findEventStream(STREAM_NAME3, VERSION3);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
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
                    "          {'name':'total','type':'INT'}," +
                    "          {'name':'user','type':'STRING'}" +
                    "  ]" +
                    "}");
//            //Define event stream
        }


        //Publish event for a valid stream
        if (!streamId3.isEmpty()) {
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
                new Object[]{getRandomProduct(), getRandomQuantity(), getRandomPrice(), getRandomUser()});
        dataPublisher.publish(eventOne);
    }

    private static String getRandomProduct(){
      return foods[getRandomId(5)];
    }

    private static String getRandomUser(){
       return users[getRandomId(7)];
    }

    private static int getRandomQuantity(){
        return quantity[getRandomId(5)];
    }

    private static int getRandomPrice(){
        return price[getRandomId(5)];
    }



    private static int getRandomId(int i){
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
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
