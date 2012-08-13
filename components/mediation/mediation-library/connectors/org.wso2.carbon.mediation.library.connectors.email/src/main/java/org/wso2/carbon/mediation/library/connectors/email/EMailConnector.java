/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediation.library.connectors.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;
import org.wso2.carbon.mediation.library.connectors.core.util.ConnectorUtils;

public class EMailConnector extends AbstractConnector {

    @Override
    public void connect() throws ConnectException {
        Email email = new SimpleEmail();
        email.setHostName(getParameter("host"));
        email.setSmtpPort(Integer.parseInt(getParameter("smtp_port")));
        email.setAuthenticator(new DefaultAuthenticator(getParameter("username"),
                                                        getParameter("password")));
        email.setSSL(true);
        try {
            email.setFrom(getParameter("from"));
            email.setSubject(getParameter("subject"));
            email.setMsg(getParameter("msg"));
            email.addTo(getParameter("to"));
            email.send();
            System.out.println("SUCCESS");
        } catch (EmailException e) {
            System.out.println("ERROR : " + e.getMessage());

        }
    }
}
