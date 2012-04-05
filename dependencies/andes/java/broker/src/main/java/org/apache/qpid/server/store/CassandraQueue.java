package org.apache.qpid.server.store;


import org.apache.qpid.framing.abstraction.ContentChunk;
import org.apache.qpid.server.ClusterResourceHolder;
import org.apache.qpid.server.cassandra.CassandraMessageFlusher;
import org.apache.qpid.server.message.AMQMessage;
import org.apache.qpid.server.message.EnqueableMessage;
import org.apache.qpid.server.queue.BaseQueue;
import org.apache.qpid.server.queue.IncomingMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CassandraQueue implements MessageQueue{

    int _bodyLengthReceived = 0;
     private List<ContentChunk> _contentChunks = new ArrayList<ContentChunk>();
    @Override
    public void enqueueMessage(IncomingMessage message, List<? extends BaseQueue> queues) {
        for(BaseQueue queue : queues){
            ClusterResourceHolder.getInstance().getCassandraMessageStore().addMessage(message);
        }
    }


    @Override
    public EnqueableMessage getMessage(String queueName) {
        return null;
    }


    @Override
    public void addMessageContent(ContentChunk contentChunk) {

    }
}
