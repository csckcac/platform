package org.wso2.andes.server.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OnflightMessageTracker {
    private static Log log = LogFactory.getLog(OnflightMessageTracker.class);

    private int acktimeout = 10000; 
    private Map<Long,MsgData> msgId2MsgData = new LinkedHashMap<Long,MsgData>(); 
    private Map<String,Long> deliveryTag2MsgID = new HashMap<String,Long>();
    
    public class MsgData{
        final long msgID; 
        boolean ackreceived = false;
        final String queue; 
        final long timestamp; 
        final String deliveryID; 
        public MsgData(long msgID, boolean ackreceived, String queue, long timestamp, String deliveryID) {
            this.msgID = msgID;
            this.ackreceived = ackreceived;
            this.queue = queue; 
            this.timestamp = timestamp;
            this.deliveryID = deliveryID;
        }
    }
    
    private static OnflightMessageTracker instance = new OnflightMessageTracker();
    public static OnflightMessageTracker getInstance(){
        return instance; 
    }

    
    private OnflightMessageTracker(){
        
        /*
         * for all add and remove, following is executed, and it will remove the oldest entry if needed
         */
        msgId2MsgData = new LinkedHashMap<Long, MsgData>() {
            private static final long serialVersionUID = -8681132571102532817L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, MsgData> eldest) {
                MsgData msgData = eldest.getValue(); 
                boolean todelete = (System.currentTimeMillis() - msgData.timestamp) > (acktimeout*3);
                if(todelete){
                    if(!msgData.ackreceived){
                        log.debug("No ack received for delivery tag " + msgData.deliveryID + " and message id "+ msgData.msgID); 
                        //TODO notify the CassandraMessageFlusher to resend (it work now as well as flusher loops around, but this will be faster)
                    }
                    if(deliveryTag2MsgID.remove(msgData.deliveryID) == null){
                        log.error("Cannot find delivery tag " + msgData.deliveryID + " and message id "+ msgData.msgID);
                    }
                }
                return todelete;
            }
        };        
    }
    
    public synchronized boolean testMessage(long messageId){
        long currentTime = System.currentTimeMillis();
        MsgData mdata = msgId2MsgData.get(messageId); 
                
        if (mdata == null || (!mdata.ackreceived && (currentTime - mdata.timestamp) > acktimeout)) {
            return true; 
        }else{
            return false;
        }
    }
    
    
    public synchronized boolean testAndAddMessage(long deliveryTag, long messageId, String queue, int channelID){
        String deliveryID = new StringBuffer(String.valueOf(channelID)).append(deliveryTag).toString(); 
        long currentTime = System.currentTimeMillis();
        MsgData mdata = msgId2MsgData.get(messageId); 
        
        if(deliveryTag2MsgID.size() != msgId2MsgData.size()){
            log.error("Two maps are out of sync "+ deliveryTag2MsgID.size() + "!=" + msgId2MsgData.size());
        }
                
        if (mdata == null || (!mdata.ackreceived && (currentTime - mdata.timestamp) > acktimeout)) {
            if (deliveryTag2MsgID.containsKey(deliveryID)) {
                throw new RuntimeException("Delivery Tag reused, this should not happen");
            }
            if (mdata != null) {
                // message has sent once, we will clean that up
                deliveryTag2MsgID.remove(mdata.deliveryID); 
                msgId2MsgData.remove(messageId); 
            }
            deliveryTag2MsgID.put(deliveryID, messageId);
            msgId2MsgData.put(new Long(messageId), new MsgData(messageId, false, queue, currentTime, deliveryID));
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized MsgData ackReceived(long deliveryTag, long channelID){
        String deliveryID = new StringBuffer(String.valueOf(channelID)).append(deliveryTag).toString(); 
        Long messageid = deliveryTag2MsgID.get(deliveryID); 
        if(messageid != null){
            MsgData msgData = msgId2MsgData.get(messageid);
            if(msgData != null){
                msgData.ackreceived = true;
                return msgData;
            }else{
                throw new RuntimeException("No message data found for messageid "+ messageid); 
            }
        }else{
            throw new RuntimeException("No Message id found for delivery tag "+deliveryID); 
        }
    }
    
    public MsgData getMsgData(long deliveryTag){
        return msgId2MsgData.get(deliveryTag);
    }
    
    public static void main(String[] args) throws Exception {
        final OnflightMessageTracker tracker = new OnflightMessageTracker(); 
        final Random random = new Random(); 
        
        final ConcurrentLinkedQueue<Long> accpetedMessages = new ConcurrentLinkedQueue<Long>(); 
        final Set<Long> messagesSent = new TreeSet<Long>(); 
        
        final Semaphore semaphore = new Semaphore(0);
        int threadCount = 10; 
        
        final AtomicLong deliveryTag = new AtomicLong(); 
        
        for(int i=0;i<threadCount;i++){
            final int count = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("thread "+count + "started");
                    for(int j =0;j<500;j++){
                        try {
                            long deliveryTagnow = deliveryTag.incrementAndGet();
                            long msgID = count*10000 + random.nextInt(1000);
                            synchronized (messagesSent) {
                                messagesSent.add(msgID);
                            }
                            if(tracker.testAndAddMessage(deliveryTagnow, msgID, "queue1", 1)){
                                accpetedMessages.add(msgID);
                                Thread.sleep(random.nextInt(10));
                                tracker.ackReceived(deliveryTagnow,1);
                            } 
                            
                            if(j%10 == 0){
                                System.out.println("size =" + tracker.msgId2MsgData.size());
                            }
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    semaphore.release();
                    System.out.println("thread "+count + "done");
                }
            }).start();
        }
        semaphore.acquire(threadCount);
        
        Set<Long> uniqueItems = new HashSet<Long>(accpetedMessages);
        if(uniqueItems.size() != accpetedMessages.size()){
            List<Long> list2Sort = new ArrayList<Long>(accpetedMessages); 
            Collections.sort(list2Sort);
            System.out.println(list2Sort);
            throw new Exception("there are duplicated values "+ accpetedMessages.size() + " != "+ uniqueItems.size()); 
        }else{
            if(uniqueItems.size() != messagesSent.size()){
                throw new Exception("Some messages are missing "+ uniqueItems.size() + "!= "+ messagesSent.size() );
            }
            System.out.println("Test completed");
        }

    }
}
