package org.wso2.andes.server.virtualhost;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.andes.AMQException;
import org.wso2.andes.framing.AMQShortString;
import org.wso2.andes.framing.FieldTable;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.binding.BindingFactory;
import org.wso2.andes.server.cluster.coordination.SubscriptionListener;
import org.wso2.andes.server.exchange.Exchange;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.queue.AMQQueueFactory;
import org.wso2.andes.server.store.ConfigurationRecoveryHandler;

import java.nio.ByteBuffer;
import java.util.Map;

public class VirtualHostConfigSynchronizer implements
        ConfigurationRecoveryHandler.QueueRecoveryHandler,
        ConfigurationRecoveryHandler.ExchangeRecoveryHandler,
        ConfigurationRecoveryHandler.BindingRecoveryHandler , SubscriptionListener {

    private final VirtualHost _virtualHost;
//     private final Map<String, Integer> _queueRecoveries = new TreeMap<String, Integer>();
    private static final Logger _logger = Logger.getLogger(VirtualHostConfigSynchronizer.class);
    private int _syncInterval;
    private boolean running = false;

    private static Log log = LogFactory.getLog(VirtualHostConfigSynchronizer.class);


    public VirtualHostConfigSynchronizer(VirtualHost _virtualHost, int synchInterval) {
        this._virtualHost = _virtualHost;
        this._syncInterval = synchInterval;
    }

    @Override
    public void binding(String exchangeName, String queueName, String bindingKey, ByteBuffer buf) {

        try {
            Exchange exchange = _virtualHost.getExchangeRegistry().getExchange(exchangeName);
            if (exchange == null) {
                _logger.error("Unknown exchange: " + exchangeName + ", cannot bind queue : " + queueName);
                return;
            }

            AMQQueue queue = _virtualHost.getQueueRegistry().getQueue(new AMQShortString(queueName));
            if (queue == null) {
                _logger.error("Unknown queue: " + queueName + ", cannot be bound to exchange: " + exchangeName);
            } else {
                FieldTable argumentsFT = null;
                if (buf != null) {
                    argumentsFT = new FieldTable(org.apache.mina.common.ByteBuffer.wrap(buf), buf.limit());
                }

                BindingFactory bf = _virtualHost.getBindingFactory();

                Map<String, Object> argumentMap = FieldTable.convertToMap(argumentsFT);

                if (bf.getBinding(bindingKey, queue, exchange, argumentMap) == null) {

                    _logger.info("Restoring binding: (Exchange: " + exchange.getNameShortString() + ", Queue: " + queueName
                            + ", Routing Key: " + bindingKey + ", Arguments: " + argumentsFT + ")");

                    bf.restoreBinding(bindingKey, queue, exchange, argumentMap);
                }
            }
        } catch (AMQException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completeBindingRecovery() {

    }

    @Override
    public void exchange(String exchangeName, String type, boolean autoDelete) {
        try {
            Exchange exchange;
            AMQShortString exchangeNameSS = new AMQShortString(exchangeName);
            exchange = _virtualHost.getExchangeRegistry().getExchange(exchangeNameSS);
            if (exchange == null) {
                exchange = _virtualHost.getExchangeFactory().createExchange(exchangeNameSS, new AMQShortString(type), true, autoDelete, 0);
                _virtualHost.getExchangeRegistry().registerExchange(exchange);
            }
        } catch (AMQException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConfigurationRecoveryHandler.BindingRecoveryHandler completeExchangeRecovery() {
        return null;
    }

    @Override
    public void queue(String queueName, String owner, boolean exclusive, FieldTable arguments) {

        try {
            AMQShortString queueNameShortString = new AMQShortString(queueName);

            AMQQueue q = _virtualHost.getQueueRegistry().getQueue(queueNameShortString);

            if (q == null) {
                q = AMQQueueFactory.createAMQQueueImpl(queueNameShortString, true, owner == null ? null : new AMQShortString(owner), false, exclusive, _virtualHost,
                        arguments);
                _virtualHost.getQueueRegistry().registerQueue(q);
            }


//            //Record that we have a queue for recovery
//            _queueRecoveries.put(queueName, 0);
        } catch (AMQException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public ConfigurationRecoveryHandler.ExchangeRecoveryHandler completeQueueRecovery() {
        return null;
    }

    public void start() {
        if (!running) {
            running = true;
            Thread t = new Thread(new VirtualHostConfigSynchronizingTask(this));
            t.setName(this.getClass().getSimpleName());
            t.start();

        }
    }

    @Override
    public void subscriptionsChanged() {
        if (ClusterResourceHolder.getInstance().getCassandraMessageStore() != null &&
                ClusterResourceHolder.getInstance().getCassandraMessageStore().isConfigured()) {

            try {
                ClusterResourceHolder.getInstance().getCassandraMessageStore().synchExchanges(this);
                ClusterResourceHolder.getInstance().getCassandraMessageStore().synchQueues(this);
                ClusterResourceHolder.getInstance().getCassandraMessageStore().synchBindings(this);
            } catch (Exception e) {
                log.error("Error while syncing Virtual host details ");
            }
        }
    }


    private class VirtualHostConfigSynchronizingTask implements Runnable {
        private VirtualHostConfigSynchronizer syc;
        public VirtualHostConfigSynchronizingTask(VirtualHostConfigSynchronizer synchronizer) {
            this.syc = synchronizer;
        }


        @Override
        public void run() {
            while (running) {

                try {
                    if (ClusterResourceHolder.getInstance().getCassandraMessageStore() != null &&
                            ClusterResourceHolder.getInstance().getCassandraMessageStore().isConfigured()) {
                        ClusterResourceHolder.getInstance().getCassandraMessageStore().synchExchanges(syc);
                        ClusterResourceHolder.getInstance().getCassandraMessageStore().synchQueues(syc);
                        ClusterResourceHolder.getInstance().getCassandraMessageStore().synchBindings(syc);
                    }
                } catch (Exception e) {
                    log.error("Error while syncing Virtual host details ");
                }
                try {
                    Thread.sleep(_syncInterval * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }

            }
        }
    }

}
