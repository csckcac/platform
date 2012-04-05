package org.apache.qpid.server.store;

import org.apache.qpid.framing.abstraction.ContentChunk;
import org.apache.qpid.server.message.AMQMessage;
import org.apache.qpid.server.message.EnqueableMessage;
import org.apache.qpid.server.queue.BaseQueue;
import org.apache.qpid.server.queue.IncomingMessage;

import java.nio.ByteBuffer;
import java.util.List;

public interface MessageQueue {

    public void enqueueMessage(IncomingMessage message,List<? extends BaseQueue> queues);

    public void addMessageContent( ContentChunk src);

    public EnqueableMessage getMessage(String queueName);
}
