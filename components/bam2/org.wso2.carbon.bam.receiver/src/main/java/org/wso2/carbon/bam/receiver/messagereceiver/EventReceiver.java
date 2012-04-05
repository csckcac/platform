/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver.messagereceiver;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.receiver.ReceiverConstants;
import org.wso2.carbon.bam.receiver.ReceiverUtils;
import org.wso2.carbon.bam.receiver.event.RawEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Event Receiver is the entry point for events to the BAM server
 *
 * Events should be of the form:
 * <soapenv:Body>
 *     <bam:data>
 *      <name1>value1</name1>
 *      <name2>value2</name2>
 *      .....
 *     </bam:data>
 * </soapenv:Body>
 */
public class EventReceiver extends AbstractMessageReceiver{

    private static final Log log = LogFactory.getLog(EventReceiver.class);

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {


        // need to build message before placing in queue for later processing
        messageContext.getEnvelope().build();

        Map<String, String> mandatoryValuesMap = new HashMap<String, String>();

        // Being defensive and adding time stamp, in case time stamp is not present in the event
        mandatoryValuesMap.put(ReceiverConstants.TIMESTAMP_KEY_NAME, formatter.format(new Date()));

        // queue it
        //ReceiverUtils.getQueue().queue(new RawEvent(mandatoryValuesMap, messageContext.getEnvelope().getBody()));

    }

}
