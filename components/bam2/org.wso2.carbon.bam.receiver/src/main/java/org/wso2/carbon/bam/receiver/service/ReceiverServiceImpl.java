/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.dataobjects.EventData;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.receiver.ReceiverConstants;
import org.wso2.carbon.bam.receiver.ReceiverUtils;
import org.wso2.carbon.bam.receiver.authentication.ThriftAuthenticator;
import org.wso2.carbon.bam.receiver.authentication.ThriftSession;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.bam.service.SessionTimeOutException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReceiverServiceImpl implements ReceiverService.Iface {

    private static final Log log = LogFactory.getLog(ReceiverServiceImpl.class);

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void publish(Event event, String sessionId) throws SessionTimeOutException {

        boolean isAuthenticated;
        ThriftSession session;

        // This is to prevent session being invalidated during the time window between checking session
        // authentication and getting session info
        synchronized (sessionId.intern()) {

            isAuthenticated = ThriftAuthenticator.getInstance().isAuthenticated(sessionId);
            session = ThriftAuthenticator.getInstance().getSessionInfo(sessionId);

        }

        if (isAuthenticated) {

            // Put mandatory time stamp in case it's not present
            String date = formatter.format(new Date());

            Charset charset = Charset.forName("UTF-8");

            Map<String, ByteBuffer> meta = event.getMeta();

            try {
                if (!meta.containsKey(ReceiverConstants.TIMESTAMP_KEY_NAME)) {
                    meta.put(ReceiverConstants.TIMESTAMP_KEY_NAME, charset.newEncoder().encode(CharBuffer.
                            wrap(date)));
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to put timestamp for received event..", e);
                }
            }

            EventData eventData = new EventData();
            eventData.setEventData(event.getEvent());
            eventData.setCorrelationData(event.getCorrelation());
            eventData.setMetaData(event.getMeta());
            
            String userName = session.getUserName();
            String password = session.getPassword();
            
            Map<String, String> credentials = new HashMap<String, String>();
            credentials.put(PersistencyConstants.USER_NAME, userName);
            credentials.put(PersistencyConstants.PASSWORD, password);

            eventData.setCredentials(credentials);

            ReceiverUtils.getQueue().queue(eventData);

        } else {
            throw new SessionTimeOutException("Session expired. Retry with authentication..");
        }

        if (log.isDebugEnabled()) {
            Map<String, ByteBuffer> correlation = event.getCorrelation();
            Map<String, ByteBuffer> meta = event.getMeta();
            Map<String, ByteBuffer> eventData = event.getEvent();

            for (Map.Entry<String, ByteBuffer> entry : correlation.entrySet()) {
                log.debug("Correlation - Key : " + entry.getKey() + " Value : " + entry.getValue());
            }

            for (Map.Entry<String, ByteBuffer> entry : meta.entrySet()) {
                log.debug("Meta - Key : " + entry.getKey() + " Value : " + entry.getValue());
            }

            for (Map.Entry<String, ByteBuffer> entry : eventData.entrySet()) {
                log.debug("Event - Key : " + entry.getKey() + " Value : " + entry.getValue());
            }
        }
    }

}
