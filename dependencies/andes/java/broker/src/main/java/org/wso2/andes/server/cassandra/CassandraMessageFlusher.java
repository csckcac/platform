package org.wso2.andes.server.cassandra;

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


    private int messageCount = 20;


    private int ackTime;


    private long lastProcessedId = 0;

    private int resetCounter;

    private int resetCount = 50;

    public CassandraMessageFlusher(AMQQueue queue ,Map<String,CassandraSubscription> cassandraSubscriptions) {

        this.cassandraSubscriptions = cassandraSubscriptions;
        this.queue = queue;

        ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
        this.messageCount = clusterConfiguration.
                getMessageBatchSizeForSubscribers();

        this.executor = Executors.newFixedThreadPool(clusterConfiguration.getFlusherPoolSize());

        this.ackTime = ClusterResourceHolder.getInstance().getClusterConfiguration().getMaxAckWaitTime();
    }

    @Override
    public void run() {

        while (running) {
            // 1) Get configured Number of Messages
            // 2) Send the batch to subscribers asynchronously.
            // 3) wait will all the acks came for a timeout period
            // 4) dequeue acked messages
            
            //This is to avoid the worker queue been full with too many pending tasks
            // those pending tasks are best left in cassandra until we have some breathing room
            int workqueueSize = ((ThreadPoolExecutor)executor).getQueue().size(); 
            if(workqueueSize > 1000){
                try {
                    log.info("skipping content cassandra reading thread as flusher queue has "+ workqueueSize + " tasks");
                    Thread.sleep(ClusterResourceHolder.getInstance().getClusterConfiguration().
                                    getQueueWorkerInterval());
                    continue; 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {

                if(resetOffset()) {
                    lastProcessedId = 0;
                }
                CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                        getCassandraMessageStore();
                List<QueueEntry> messages = messageStore.
                        getMessagesFromUserQueue(queue
                                , messageCount,lastProcessedId);
                if(messages.size() == messageCount) {
                    messageCount += 10;
                    if(messageCount > (ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize())) {
                        messageCount =  ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize()-1;
                    }
                } else {
                    messageCount-=10;
                    if(messageCount < 20) {
                        messageCount=20;
                    }
                }

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
                            deliverAsynchronously(subscription, message);

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
        Runnable r = new Runnable() {
            @Override
            public void run() {
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
       executor.execute(r);
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
