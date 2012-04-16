package org.wso2.andes.server.store;


import org.wso2.andes.framing.AMQShortString;
import org.wso2.andes.framing.abstraction.ContentChunk;
import org.wso2.andes.server.queue.BaseQueue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CassandraMessage implements Serializable{

private ArrayList<? extends BaseQueue> _destinationQueues;

    private long _expiration;

    private AMQShortString _exchange;


    private int _receivedChunkCount = 0;
    private List<ContentChunk> _contentChunks = new ArrayList<ContentChunk>();

    public ArrayList<? extends BaseQueue> get_destinationQueues() {
        return _destinationQueues;
    }

    public void set_destinationQueues(ArrayList<? extends BaseQueue> _destinationQueues) {
        this._destinationQueues = _destinationQueues;
    }

    public long get_expiration() {
        return _expiration;
    }

    public void set_expiration(long _expiration) {
        this._expiration = _expiration;
    }

    public AMQShortString get_exchange() {
        return _exchange;
    }

    public void set_exchange(AMQShortString _exchange) {
        this._exchange = _exchange;
    }

    public int get_receivedChunkCount() {
        return _receivedChunkCount;
    }

    public void set_receivedChunkCount(int _receivedChunkCount) {
        this._receivedChunkCount = _receivedChunkCount;
    }

    public List<ContentChunk> get_contentChunks() {
        return _contentChunks;
    }

    public void set_contentChunks(List<ContentChunk> _contentChunks) {
        this._contentChunks = _contentChunks;
    }
}
