package org.apache.qpid.server.store;


import org.apache.qpid.server.binding.Binding;
import org.apache.qpid.server.exchange.Exchange;

public interface ExchangeManager {

    public void createExchange(Exchange exchange);

    public Exchange getExchange();

    public void addBinding(Exchange exchange, Binding binding);


}
