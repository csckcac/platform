/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.broker.core.internal.brokers.jms;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

import javax.jms.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

public class JMSMessageListener implements MessageListener {
    private static final Log log = LogFactory.getLog(JMSMessageListener.class);
    private BrokerListener brokerListener = null;

    public JMSMessageListener(BrokerListener brokerListener) {
        this.brokerListener = brokerListener;
    }

    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            // create OMElement using message text
            try {
                XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        textMessage.getText().getBytes()));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement omMessage = builder.getDocumentElement();
                brokerListener.onEvent(omMessage);
            } catch (XMLStreamException e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to get OMElement from " + textMessage, e);
                }
            } catch (JMSException e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to get text from " + textMessage, e);
                }
            } catch (BrokerEventProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error(e);
                }
            }
        } else if (message instanceof MapMessage){
            MapMessage mapMessage = (MapMessage) message;
            Map event = new HashMap();
            try {
                Enumeration names = mapMessage.getMapNames();
                Object name;
                while (names.hasMoreElements()){
                    name = names.nextElement();
                    event.put(name, mapMessage.getObject((String) name));
                }
                brokerListener.onEvent(event);

            } catch (JMSException e) {
                log.error("Can not read the map message ", e);
            } catch (BrokerEventProcessingException e) {
                log.error("Can not send the message to broker ", e);
            }
        }
    }
}
