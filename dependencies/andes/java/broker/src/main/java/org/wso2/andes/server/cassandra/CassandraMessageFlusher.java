package org.wso2.andes.server.cassandra;

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

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    private WorkerPool executor =  null;


    private int messageCount = 20;


    private int ackTime;

    public CassandraMessageFlusher(AMQQueue queue ,Map<String,CassandraSubscription> cassandraSubscriptions) {

        this.cassandraSubscriptions = cassandraSubscriptions;
        this.queue = queue;

        ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
        this.messageCount = clusterConfiguration.
                getMessageBatchSizeForSubscribers();

        this.executor = new WorkerPool(clusterConfiguration.getFlusherPoolSize());

        this.ackTime = ClusterResourceHolder.getInstance().getClusterConfiguration().getMaxAckWaitTime();
    }

    @Override
    public void run() {

        while (running) {
            // 1) Get configured Number of Messages
            // 2) Send the batch to subscribers asynchronously.
            // 3) wait will all the acks came for a timeout period
            // 4) dequeue acked messages

            try {
                CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                        getCassandraMessageStore();
                List<QueueEntry> messages = messageStore.
                        getMessagesFromUserQueue(queue
                                , messageCount);
                if(messages.size() == messageCount) {
                    messageCount += 10;
                    if(messageCount > (ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize() -10)) {
                        messageCount =  ClusterResourceHolder.getInstance().getClusterConfiguration().getFlusherPoolSize() -10;
                    }
                } else {
                    messageCount-=10;
                    if(messageCount < 20) {
                        messageCount=20;
                    }
                }
                Semaphore barrier;

                if (messages.size() > 0 && cassandraSubscriptions.size() > 0) {

                    barrier = new Semaphore(messages.size());
                    barrier.acquire(messages.size());
                    Iterator<CassandraSubscription> subs = cassandraSubscriptions.values().iterator();
                    for (QueueEntry message : messages) {
                        try {

                            Subscription subscription;
                            AMQProtocolSession session;
                            CassandraSubscription cassandraSubscription;
                            if(subs.hasNext()) {
                                cassandraSubscription = subs.next();
                            } else {
                                subs = cassandraSubscriptions.values().iterator();
                                cassandraSubscription = subs.next();

                            }

                            subscription = cassandraSubscription.getSubscription();
                            session = cassandraSubscription.getSession();

                            ((AMQMessage) message.getMessage()).setClientIdentifier(session);

                            deliverAsynchronously(subscription, message, barrier);
                        } catch (Exception e) {
                            log.error("Unexpected Error in Message Flusher Task " +
                                    "while delivering the message : ", e);
                            e.printStackTrace();
                        }
                    }

                    try {
                        barrier.tryAcquire(messages.size(),ackTime*messages.size(), TimeUnit.SECONDS);



                    } catch (InterruptedException e) {

                        //Ignore
                    }


                } else {
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



    private void deliverAsynchronously(final Subscription subscription , final QueueEntry message ,
                                       final Semaphore barrier) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if (subscription instanceof SubscriptionImpl.AckSubscription) {


                        subscription.send(message);

                        ArrayList<QueueEntry> msg = new ArrayList<QueueEntry>();
                        msg.add(message);

                        ClusterResourceHolder.getInstance().getCassandraMessageStore().
                                dequeueMessages(queue, msg);
                        barrier.release();

                    }
                } catch (AMQException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

       executor.execute(r);

    }


    public void stopFlusher() {
        running = false;
    }

    public class WorkerPool {
        private final int nThreads;
        private final PoolWorker[] threads;
        private final LinkedList queue;

        public WorkerPool(int nThreads) {
            this.nThreads = nThreads;
            queue = new LinkedList();
            threads = new PoolWorker[this.nThreads];

            for (int i = 0; i < this.nThreads; i++) {
                threads[i] = new PoolWorker();
                threads[i].start();
            }
        }

        public void execute(Runnable r) {
            synchronized (queue) {
                queue.addLast(r);
                queue.notify();
            }
        }

        private class PoolWorker extends Thread {
            public void run() {
                Runnable r;

                while (true) {
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            try {
                                queue.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }

                        r = (Runnable) queue.removeFirst();
                    }

                    // If we don't catch Throwable,
                    // the pool could leak threads
                    try {
                        r.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
