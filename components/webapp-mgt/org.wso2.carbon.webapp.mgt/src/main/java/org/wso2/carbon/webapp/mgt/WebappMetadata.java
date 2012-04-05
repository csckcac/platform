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
package org.wso2.carbon.webapp.mgt;

/*
* Represents a Web Application
*/
public class WebappMetadata {
    private String context;
    private String displayName;
    private String webappFile;
    private boolean isRunning;
    private boolean isStarted;
    private long lastModifiedTime;
    private WebappStatistics statistics;
    private boolean isFaulty;
    private String faultException;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWebappFile() {
        return webappFile;
    }

    public void setWebappFile(String webappFile) {
        this.webappFile = webappFile;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public WebappStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(WebappStatistics statistics) {
        this.statistics = statistics;
    }

    public boolean isFaulty() {
        return isFaulty;
    }

    public void setFaulty(boolean faulty) {
        isFaulty = faulty;
    }

    public String getFaultException() {
        return faultException;
    }

    public void setFaultException(String faultException) {
        this.faultException = faultException;
    }
}
