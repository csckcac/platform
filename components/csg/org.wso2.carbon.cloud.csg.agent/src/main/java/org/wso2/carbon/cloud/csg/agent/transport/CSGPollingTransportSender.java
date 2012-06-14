/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.agent.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.wso2.carbon.cloud.csg.agent.CSGAgentBuffers;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * CSG Polling transport sender implementation
 */
public class CSGPollingTransportSender extends AbstractTransportSender {

    @Override
    public void init(ConfigurationContext cfgCtx,
                     TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
    }


    @Override
    public void sendMessage(MessageContext msgCtx,
                            String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {
        // we could not use addressing information for correlation due to the following reasons
        // 1. There can be messages with no addressing information
        // 2. Since message content is not touch there is no way to read the message ID
        String relatesTo = (String) msgCtx.getOperationContext().getMessageContext(
                WSDL2Constants.MESSAGE_LABEL_IN).getProperty(CSGConstant.CSG_CORRELATION_KEY);
        if (log.isDebugEnabled()) {
            log.debug("A response was received without addressing information. " +
                    "Correlation key '" + relatesTo + "' calculated from the IN message context");
        }

        Message thriftMsg = new Message();
        thriftMsg.setMessageId(relatesTo);
        thriftMsg.setSoapAction(msgCtx.getSoapAction());        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            if (msgCtx.isDoingREST()) {
                // result comes as the body of envelope
                msgCtx.getEnvelope().getBody().getFirstElement().serialize(out);
            } else {
                msgCtx.getEnvelope().serialize(out);
            }
            // handle other cases MTOM, SWA if required
        } catch (XMLStreamException e) {
            handleException("Cloud not serialize the request message", e);
        }
        thriftMsg.setMessage(out.toByteArray());

        CSGAgentBuffers buf = (CSGAgentBuffers)
                msgCtx.getConfigurationContext().getProperty(CSGConstant.CSG_POLLING_TRANSPORT_BUF_KEY);

        buf.addResponseMessage(thriftMsg);
    }
}
