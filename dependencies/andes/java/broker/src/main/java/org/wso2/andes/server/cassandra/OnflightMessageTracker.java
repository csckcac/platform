package org.wso2.andes.server.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OnflightMessageTracker {
    private static Log log = LogFactory.getLog(OnflightMessageTracker.class);

    private int acktimeout = 10000; 
    private Map<Long,MsgData> msgId2MsgData = new HashMap<Long,MsgData>(); 
    private TreeMap<Long, Long> timesortedMsgIds = new TreeMap<Long, Long>();
    private Map<Long,Long> deliveryTag2MsgID = new HashMap<Long,Long>();
    
    public class MsgData{
        long msgID; 
        boolean ackreceived = false;
        String queue; 
        long timestamp; 
        long deliveryTag; 
        public MsgData(long msgID, boolean ackreceived, String queue, long timestamp, long deliveryTag) {
            this.msgID = msgID;
            this.ackreceived = ackreceived;
            this.queue = queue; 
            this.timestamp = timestamp;
            this.deliveryTag = deliveryTag;
        }
    }
    
    private static OnflightMessageTracker instance = new OnflightMessageTracker();
    public static OnflightMessageTracker getInstance(){
        return instance; 
    }

    
    private OnflightMessageTracker(){
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true){
                    try {
                        synchronized (this) {
                            SortedMap<Long, Long> entries2Remove = timesortedMsgIds.headMap(System.currentTimeMillis()
                                    - acktimeout * 3);
                            Iterator<Entry<Long, Long>> items2Remove = entries2Remove.entrySet().iterator();
                            while (items2Remove.hasNext()) {
                                Long mesageID = items2Remove.next().getValue();
                                items2Remove.remove();
                                MsgData msgData = msgId2MsgData.remove(mesageID);
                                if (msgData == null) {
                                    log.error("Cannot find key " + mesageID
                                            + " to remove from msgId2MsgData: timesortedMsgIds="
                                            + timesortedMsgIds.size() + " msgId2MsgData=" + msgId2MsgData.size()
                                            + " deliveryTag2MsgID=" + deliveryTag2MsgID.size());
                                } else {
                                    if(!msgData.ackreceived){
                                        log.warn("No ack received for deliverytag"+ msgData.deliveryTag + " and "+ msgData.msgID); 
                                    }
                                    if (deliveryTag2MsgID.remove(msgData.deliveryTag) == null) {
                                        log.error("Cannot find delivery tag " + deliveryTag2MsgID);
                                    }
                                    System.out.println("removed delivery tag " + msgData.deliveryTag);
                                }
                            }
                            log.info("timesortedMsgIds="
                                            + timesortedMsgIds.size() + " msgId2MsgData=" + msgId2MsgData.size()
                                            + " deliveryTag2MsgID=" + deliveryTag2MsgID.size());
                        }
                        Thread.sleep(60000);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
    
    
    public synchronized boolean testAndAddMessage(long deliveryTag, long messageId, String queue){
        long currentTime = System.currentTimeMillis();
        MsgData mdata = msgId2MsgData.get(messageId); 
                
        if (mdata == null || (!mdata.ackreceived && (currentTime - mdata.timestamp) > acktimeout)) {
            if (mdata != null) {
                // message has sent once, we will clean that up
                if (mdata.msgID != messageId) {
                    throw new RuntimeException("Delivery Tag reused, this should not happen");
                }
                timesortedMsgIds.remove(mdata.timestamp);
                deliveryTag2MsgID.remove(mdata.deliveryTag); 
            }
            msgId2MsgData.put(new Long(messageId), new MsgData(messageId, false, queue, currentTime, deliveryTag));
            timesortedMsgIds.put(currentTime, messageId);
            deliveryTag2MsgID.put(deliveryTag, messageId);
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized MsgData ackReceived(long deliveryTag){
        Long messageid = deliveryTag2MsgID.get(deliveryTag); 
        if(messageid != null){
            MsgData msgData = msgId2MsgData.get(messageid);
            if(msgData != null){
                msgData.ackreceived = true;
                return msgData;
            }else{
                throw new RuntimeException("No message data found for messageid "+ messageid); 
            }
        }else{
            throw new RuntimeException("No Message id found for delivery tag "+deliveryTag); 
        }
    }
    
    public MsgData getMsgData(long deliveryTag){
        return msgId2MsgData.get(deliveryTag);
    }
    
    public static void main(String[] args) throws Exception {
        final OnflightMessageTracker tracker = new OnflightMessageTracker(); 
        final Random random = new Random(); 
        
        final ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>(); 
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
                    for(int j =0;j<100;j++){
                        try {
                            long deliveryTagnow = deliveryTag.incrementAndGet();
                            long msgID = count*10000 + random.nextInt(1000);
                            messagesSent.add(msgID);
                            if(tracker.testAndAddMessage(deliveryTagnow, msgID, "queue1")){
                                queue.add(msgID);
                                Thread.sleep(random.nextInt(10));
                                tracker.ackReceived(deliveryTagnow);
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
        
        Set<Long> set = new HashSet<Long>(queue);
        if(set.size() != queue.size()){
            List<Long> list2Sort = new ArrayList<Long>(queue); 
            Collections.sort(list2Sort);
            System.out.println(list2Sort);
            throw new Exception("there are duplicated values "+ queue.size() + " != "+ set.size()); 
        }else{
            if(set.size() != messagesSent.size()){
                throw new Exception("Some messages are missing");
            }
            System.out.println("Test completed");
        }

    }
}
