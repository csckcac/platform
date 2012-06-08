package org.wso2.carbon.bam.service.data.publisher.publish;


import org.wso2.carbon.bam.service.data.publisher.data.Event;

public class EventPublisher {

    public void publish (Event event){
/*
        Map<String,Object> correlationMap = event.getCorrelationData();
        Map<String,Object> metaData = event.getMetaData();
        Map<String,Object> eventData = event.getEventData();


        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);

        //create data publisher
        try {
            DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7611", "admin", "admin", agent);

            //Define event stream
            String streamId = dataPublisher.defineEventStream("{" +
                                                              "  'name':'org.wso2.bam.service.data.publisher'," +
                                                              "  'version':'1.0.0'," +
                                                              "  'nickName': 'KPI'," +
                                                              "  'description': 'Product Sale'," +
                                                              "  'metaData':[" +
                                                              "          {'name':'clientType','type':'STRING'}" +
                                                              "  ],"+
                                                              "  'correlationData':[" +
                                                              "          {'name':'correlationId','type':'STRING'}" +
                                                              "  ]," +
                                                              "  'payloadData':[" +
                                                              "          {'name':'item','type':'STRING'}," +
                                                              "          {'name':'quantity','type':'DOUBLE'}," +
                                                              "          {'name':'total','type':'DOUBLE'}," +
                                                              "          {'name':'user','type':'STRING'}" +
                                                              "  ]" +
                                                              "}");

        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AgentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransportException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (StreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrongEventTypeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
*/

    }
}
