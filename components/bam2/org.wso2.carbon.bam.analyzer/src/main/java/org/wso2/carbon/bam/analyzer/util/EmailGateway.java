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
package org.wso2.carbon.bam.analyzer.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Session;
import java.util.Properties;

public class EmailGateway {

    private static final Log log = LogFactory.getLog(EmailGateway.class);

    private Session mailSession = null;



    private static EmailGateway instance = null;

    public Session getMailSession() {
        return mailSession;
    }

    protected EmailGateway() {
        init();
    }

    public static EmailGateway getInstance() {
        if (instance == null) {
            synchronized (EmailGateway.class) {
                if (instance == null) {
                    instance = new EmailGateway();
                }
            }
        }
        return instance;
    }

    private void init() {
        Properties props = new Properties();
        props.put("mail.smtps.host", "smtp.gmail.com");
        props.put("mail.smtps.port", "465");
        props.put("mail.smtps.auth", "true");

        mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(false);

    }
}
