package org.wso2.andes.server.store;


import org.wso2.andes.framing.abstraction.ContentChunk;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.message.EnqueableMessage;
import org.wso2.andes.server.queue.BaseQueue;
import org.wso2.andes.server.queue.IncomingMessage;

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
