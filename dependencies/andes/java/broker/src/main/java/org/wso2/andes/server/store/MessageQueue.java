package org.wso2.andes.server.store;

import org.wso2.andes.framing.abstraction.ContentChunk;
import org.wso2.andes.server.message.EnqueableMessage;
import org.wso2.andes.server.queue.BaseQueue;
import org.wso2.andes.server.queue.IncomingMessage;

import java.util.List;

public interface MessageQueue {

    public void enqueueMessage(IncomingMessage message,List<? extends BaseQueue> queues);

    public void addMessageContent( ContentChunk src);

    public EnqueableMessage getMessage(String queueName);
}
