package org.apache.qpid.server.store;


import org.apache.qpid.server.message.AMQMessage;
import org.apache.qpid.server.queue.AMQQueue;

public interface QueueManager {

    public void createQueue(AMQQueue amqQueue);

    public AMQQueue getQueue(String queueName);

    public void addMessage(AMQQueue queue, AMQMessage message);

    public AMQMessage popQueue(String queueName);

}
