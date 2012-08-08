package org.wso2.andes.server.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.AMQException;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.configuration.ClusterConfiguration;
import org.wso2.andes.server.message.AMQMessage;
import org.wso2.andes.server.protocol.AMQProtocolSession;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.queue.QueueEntry;
import org.wso2.andes.server.store.CassandraMessageStore;
import org.wso2.andes.server.subscription.Subscription;
import org.wso2.andes.server.subscription.SubscriptionImpl;

/**
 * <code>CassandraMessageFlusher</code> Handles the task of polling the user queues and flushing
 * the messages to subscribers
 * There will be one Flusher per Queue Per Node
 */
public class CassandraMessageFlusher extends Thread{


    private Map<String,CassandraSubscription> cassandraSubscriptions;
    private AMQQueue queue;

    private boolean running = true;



    private static Log log = LogFactory.getLog(CassandraMessageFlusher.class);

    private ExecutorService executor =  null;


    private int messageCountToRead = 20;


    private int ackTime;


    private long lastProcessedId = 0;

    private int resetCounter;

    private int resetCount = 50;
    
    private long messageProcessed = 0;
    
    
    private List<ExecutorService> senderQueues;

    public CassandraMessageFlusher(AMQQueue queue ,Map<String,CassandraSubscription> cassandraSubscriptions) {

        this.cassandraSubscriptions = cassandraSubscriptions;
        this.queue = queue;

        ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
        this.messageCountToRead = clusterConfiguration.
                getMessageBatchSizeForSubscribers();

        this.executor = Executors.newFixedThreadPool(clusterConfiguration.getFlusherPoolSize());

        this.ackTime = ClusterResourceHolder.getInstance().getClusterConfiguration().getMaxAckWaitTime();
        
        //Following senders sends messages to end users, we submit all messages to same subscriber to one sender
        
        
        senderQueues = new ArrayList<ExecutorService>();
        for(int i =0;i< clusterConfiguration.getFlusherPoolSize(); i++ ){
            senderQueues.add(Executors.newFixedThreadPool(1)); 
        }
        System.out.println("Queue worker started");
    }

    @Override
    public void run() {
        long iterations = 0; 
        while (running) {
            // 1) Get configured Number of Messages
            // 2) Send the batch to subscribers asynchronously.
            // 3) wait will all the acks came for a timeout period
            // 4) dequeue acked messages
            
            //This is to avoid the worker queue been full with too many pending tasks
            // those pending tasks are best left in cassandra until we have some breathing room
            try {
                int workqueueSize = 0; 
                for(ExecutorService executor: senderQueues){
                   workqueueSize =workqueueSize + ((ThreadPoolExecutor)executor).getQueue().size();
                }

                if(workqueueSize > 1000){
                    try {
                        if(workqueueSize > 5000){
                            log.error("Flusher queue is growing, and this should not happen. Please check cassandra Flusher"); 
                        }
                        
                        log.info("skipping content cassandra reading thread as flusher queue has "+ workqueueSize + " tasks");
                        Thread.sleep(ClusterResourceHolder.getInstance().getClusterConfiguration().
                                        getQueueWorkerInterval());
                        continue; 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                if(resetOffset()) {
                    lastProcessedId = 0;
                }
                CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                        getCassandraMessageStore();
                //Here we read messages from the user queue
                List<QueueEntry> messages = messageStore.
                        getMessagesFromUserQueue(queue
                                , messageCountToRead,lastProcessedId);
                
                //If we have read all messages we asked for, we increase the reading count. Else we reduce it. 
                if(messages.size() == messageCountToRead) {
                    messageCountToRead += 10;
                    if(messageCountToRead > (ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize())) {
                        messageCountToRead =  ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize()-1;
                    }
                } else {
                    messageCountToRead-=10;
                    if(messageCountToRead < 20) {
                        messageCountToRead=20;
                    }
                }

                //Then we schedule them to be sent to subcribers
                if (messages.size() > 0 && cassandraSubscriptions.size() > 0) {

                    Iterator<CassandraSubscription> subs = cassandraSubscriptions.values().iterator();
                    for (int i = 0; i < messages.size(); i++) {
                        QueueEntry message = messages.get(i);

                        try {

                            Subscription subscription;
                            AMQProtocolSession session;
                            CassandraSubscription cassandraSubscription;
                            if(subs.hasNext()) {
                                cassandraSubscription = subs.next();
                            } else {
                                subs = cassandraSubscriptions.values().iterator();
                                if (subs.hasNext()) {
                                    cassandraSubscription = subs.next();
                                }else{
                                    break;
                                }

                            }

                            subscription = cassandraSubscription.getSubscription();
                            session = cassandraSubscription.getSession();

                            ((AMQMessage) message.getMessage()).setClientIdentifier(session);
                            
                            ByteBuffer buf = ByteBuffer.allocate(100); 
                            int readCount = message.getMessage().getContent(buf, 0);
                            log.debug("readFromCassandra("+ message.getMessage().getMessageNumber() + ")" + new String(buf.array(),0, readCount)); 
                            deliverAsynchronously(subscription, message);
                            messageProcessed++;

                            if (i == messages.size() -1) {
                                //long old = lastProcessedId;
                                lastProcessedId = message.getMessage().getMessageNumber();

                            }
                        } catch (Exception e) {
                            log.error("Unexpected Error in Message Flusher Task " +
                                    "while delivering the message : ", e);
                            e.printStackTrace();
                        }
                    }
                    iterations++;
                    if(messageProcessed > 10 || workqueueSize > 100){
                        log.info("[Flusher]read="+ messages.size() + " tot= "+ messageProcessed + ". queue size = "+ workqueueSize); 
                    }
                    messages.clear();
                } else {

                    if(messages.size() ==0 ) {

                        resetOffset();
                    }


                    try {
                        Thread.sleep(ClusterResourceHolder.getInstance().getClusterConfiguration().
                                getQueueWorkerInterval());
                    } catch (InterruptedException ignored) {

                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }





    }



    public AMQQueue getQueue() {
        return queue;
    }



    private void deliverAsynchronously(final Subscription subscription , final QueueEntry message) {
        if(OnflightMessageTracker.getInstance().testMessage(message.getMessage().getMessageNumber())){
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Send called ");
                    String oldName = Thread.currentThread().getName();
                    Thread.currentThread().setName("MessageFlusher-AsyncDelivery-Thread : " + oldName);
                    try {
                        if (subscription instanceof SubscriptionImpl.AckSubscription) {
                            subscription.send(message);

                        } else {
                            log.error("Unexpected Subscription Implementation : " +
                                    subscription !=null?subscription.getClass().getName():null);
                        }
                    } catch (AMQException e) {
                        log.error("Error while delivering message " ,e);
                    } catch (Throwable e) {
                         log.error("Error while delivering message " ,e);
                    }
                }
            };
            int senderIndex = subscription.hashCode()%senderQueues.size(); 
            senderQueues.get(senderIndex).execute(r); 
        }
    }


    public void stopFlusher() {
        running = false;
    }



    private  boolean resetOffset() {

        if(resetCounter++ > resetCount ) {
            resetCounter=0;
            return true;
        }

        return false;
    }
}
