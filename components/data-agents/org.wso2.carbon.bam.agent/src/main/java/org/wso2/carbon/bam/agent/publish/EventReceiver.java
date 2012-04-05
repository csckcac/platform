package org.wso2.carbon.bam.agent.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class EventReceiver {

    private static final Log log = LogFactory.getLog(EventReceiver.class);

    private String url;
    private String userName;
    private String password;
    private boolean isHttpTransportEnabled;
    private boolean isSocketTransportEnabled;
    private int port;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHttpTransportEnabled() {
        return isHttpTransportEnabled;
    }

    public void setHttpTransportEnabled(boolean httpTransportEnabled) {
        isHttpTransportEnabled = httpTransportEnabled;
    }

    public boolean isSocketTransportEnabled() {
        return isSocketTransportEnabled;
    }

    public void setSocketTransportEnabled(boolean socketTransportEnabled) {
        isSocketTransportEnabled = socketTransportEnabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventReceiver receiver = (EventReceiver) o;

        if (isHttpTransportEnabled != receiver.isHttpTransportEnabled) return false;
        if (isSocketTransportEnabled != receiver.isSocketTransportEnabled) return false;
        if (port != receiver.port) return false;
        if (!password.equals(receiver.password)) return false;
        if (!url.equals(receiver.url)) return false;
        if (!userName.equals(receiver.userName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (isHttpTransportEnabled ? 1 : 0);
        result = 31 * result + (isSocketTransportEnabled ? 1 : 0);
        result = 31 * result + port;
        return result;
    }
}
