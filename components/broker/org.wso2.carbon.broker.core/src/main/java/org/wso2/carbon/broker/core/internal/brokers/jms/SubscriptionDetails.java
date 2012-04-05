/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.broker.core.internal.brokers.jms;

import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.JMSException;

public class SubscriptionDetails {

    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;

    public SubscriptionDetails(TopicConnection topicConnection,
                               TopicSession topicSession,
                               TopicSubscriber topicSubscriber) {
        this.topicConnection = topicConnection;
        this.topicSession = topicSession;
        this.topicSubscriber = topicSubscriber;
    }

    public void close() throws JMSException {
        this.topicSubscriber.close();
        this.topicSession.close();
        this.topicConnection.stop();
        this.topicConnection.close();
    }

    public TopicConnection getTopicConnection() {
        return topicConnection;
    }

    public void setTopicConnection(TopicConnection topicConnection) {
        this.topicConnection = topicConnection;
    }

    public TopicSession getTopicSession() {
        return topicSession;
    }

    public void setTopicSession(TopicSession topicSession) {
        this.topicSession = topicSession;
    }

    public TopicSubscriber getTopicSubscriber() {
        return topicSubscriber;
    }

    public void setTopicSubscriber(TopicSubscriber topicSubscriber) {
        this.topicSubscriber = topicSubscriber;
    }
}
